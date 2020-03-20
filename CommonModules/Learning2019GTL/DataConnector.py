#!/usr/bin/env python

"""
Learning2019GTL.DataConnector.py: 

This file holds some classes for connecting to the database.
"""

__author__      = "Prakash Manandhar"
__copyright__ = "Copyright 2019, Prakash Manandhar"
__credits__ = ["Prakash Manandhar"]
__license__ = "MIT"
__version__ = "1.0.0"
__maintainer__ = "Prakash Manandhar"
__email__ = "engineer.manandhar@gmail.com"
__status__ = "Production"

import mysql.connector

MYSQL_SEVER = "127.0.0.1"
MYSQL_USER = "data_entry"
MYSQL_PASS = "Sajil0" # change password to one you used
MYSQL_DB = "Learning2019"
MYSQL_AUTH = "mysql_native_password"

import numpy as np

class UISimulationResult:
    buildings = [False] * 6
    floors = [0] * 6
    tA = [0] * 6
    tB = [0] * 6
    tC = [0] * 6
    
    interaction_score = 0.0
    total_cost = 0.0
    diversity_score = 0.0
    walking_time = 0.0

    def getDensity(self):
        density = np.zeros(6)
        SQFT_PER_FLOOR = 50e3
        for i in range(6):
            if self.buildings[i]:
                area       = SQFT_PER_FLOOR*self.floors[i]
                population = self.tA[i] + self.tB[i] + self.tC[i]
                if population == 0:
                    density[i] = -1.0
                else:
                    density[i] = area/population 
            else:
                density[i] = -1.0
        return density

    def getTotalPopulation(self):
        return np.sum(self.tA) + np.sum(self.tB) + np.sum(self.tC)

class Team:
    day = '' # M or T (Monday or Tuesday)
    session = '' # S1, S2, S3 or S4
    room = '' # R1 or R2
    station = '' # 1, 2, 3, or 4
    condition = '' # A, B, or C
    team_id = -1 # team id in database

    def __str__(self):
        return f" Day: {self.day}, Session: {self.session}," +\
               f" Room: {self.room}, Station: {self.station}" +\
               f" Condition: {self.condition}, DbTeamID: {self.team_id}"
    
    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.__dict__ == other.__dict__
        else:
            return False

    def getStringID(self):
        return self.day + self.session + \
            self.room + self.condition + \
                '-' + self.station
    
    def getCondition(self, team_string):
        t = team_string.split(', ')
        query = "SELECT id, Teams.condition FROM TEAMS WHERE day = %s AND session = %s AND station = %s"
        self.cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True,
                        auth_plugin=MYSQL_AUTH) # change username and password to one you use
        cursor = self.cnx.cursor(buffered=True)
        cursor.execute(query, (t[0], t[1], t[2][-1]))
        for (id, condition) in cursor:
            self.team_id = id
            self.condition = condition
        self.cnx.commit()
        cursor.close()
        self.cnx.close()
        return self.condition

    def getTeamByID(self, team_id):
        query = "SELECT Teams.id, Teams.day, Teams.session, Teams.room, Teams.station, Teams.condition" + \
                " FROM Learning2019.Teams WHERE Teams.id = %s"
        self.cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True,
                        auth_plugin=MYSQL_AUTH) # change username and password to one you use
        cursor = self.cnx.cursor(buffered=True)
        cursor.execute(query, (team_id, ))
        for (row_id, day, session, room, station, condition) in cursor:
            if row_id == team_id:
                self.team_id = team_id
                self.day = day
                self.session = session
                self.room = room
                self.station = station
                self.condition = condition
                break
        self.cnx.commit()
        cursor.close()
        self.cnx.close()
    
    def getSimulationUIEventsByTeamID(self, team_id):
        query = "SELECT Cost, Interaction, Team_Mix, Walking," + \
                    " b_1, f_1, tA_1, tB_1, tC_1," + \
                    " b_2, f_2, tA_2, tB_2, tC_2," + \
                    " b_3, f_3, tA_3, tB_3, tC_3," + \
                    " b_4, f_4, tA_4, tB_4, tC_4," + \
                    " b_5, f_5, tA_5, tB_5, tC_5," + \
                    " b_6, f_6, tA_6, tB_6, tC_6 " + \
                    " FROM Learning2019.UIEvents WHERE team_id = %s " +\
                    " AND type = 'run_simulation'"
        self.cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True,
                        auth_plugin=MYSQL_AUTH) # change username and password to one you use
        cursor = self.cnx.cursor(buffered=True)
        cursor.execute(query, (team_id, ))
        ui_events = list()
        for (cost, interaction, diversity_score, walking_time,
                 b_1, f_1, tA_1, tB_1, tC_1,
                 b_2, f_2, tA_2, tB_2, tC_2,
                 b_3, f_3, tA_3, tB_3, tC_3,
                 b_4, f_4, tA_4, tB_4, tC_4,
                 b_5, f_5, tA_5, tB_5, tC_5,
                 b_6, f_6, tA_6, tB_6, tC_6) in cursor:
            event = UISimulationResult()
            event.interaction_score = interaction
            event.total_cost = cost
            event.diversity_score = diversity_score
            event.walking_time = walking_time
            event.buildings = [b_1, b_2, b_3, b_4, b_5, b_6]
            event.floors = [f_1, f_2, f_3, f_4, f_5, f_6]   
            event.tA = [tA_1, tA_2, tA_3, tA_4, tA_5, tA_6]   
            event.tB = [tB_1, tB_2, tB_3, tB_4, tB_5, tB_6]   
            event.tC = [tC_1, tC_2, tC_3, tC_4, tC_5, tC_6]   
 
            ui_events.append(event)
        self.cnx.commit()
        cursor.close()
        self.cnx.close()
        return ui_events

    def getSimulationUIEventsByTeamID_TotalPopulationDensityConstraint(self,
        team_id, total_population, min_density):
        ui_events_all = self.getSimulationUIEventsByTeamID(team_id)
        ui_events_ret = []
        for ui_event in ui_events_all:
            density = ui_event.getDensity()
            densityCorrect = True
            for i in range(6):
                if ((density[i] < min_density) and \
                    (density[i] > 0.0)):
                        densityCorrect = False
                        break
            if ( densityCorrect ) and \
               ( ui_event.getTotalPopulation() == total_population ):
                ui_events_ret.append(ui_event)
        return ui_events_ret

    def getSimulationUIEventsByTeamIDBetweenDensities(self, 
        team_id, minmax_densities):
        ui_events_all = self.getSimulationUIEventsByTeamID(team_id)
        ui_events_ret = []
        for ui_event in ui_events_all:
            density = ui_event.getDensity()
            densityCorrect = True
            for i in range(6):
                if ((density[i] < minmax_densities[0]) or \
                   (density[i] > minmax_densities[1])) and \
                   (density[i] > 0.0):
                        densityCorrect = False
                        break
            if densityCorrect:
                ui_events_ret.append(ui_event)
        return ui_events_ret
        

