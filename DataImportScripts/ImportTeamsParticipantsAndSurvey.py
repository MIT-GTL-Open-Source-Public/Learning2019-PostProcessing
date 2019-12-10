#!/usr/bin/env python

"""
ImportTeamsParticipantsAndSurvey.py: 
This script loads data from a MS-Excel file that contains team, participant and pre- and post-survey
data into a MySQL relational database.

This file was written to automate a very specific part of the project,
and may only be generically useful as a template, not as a reusable
component.
"""

__author__      = "Prakash Manandhar"
__copyright__ = "Copyright 2019, Prakash Manandhar"
__credits__ = ["Prakash Manandhar"]
__license__ = "MIT"
__version__ = "1.0.0"
__maintainer__ = "Prakash Manandhar"
__email__ = "engineer.manandhar@gmail.com"
__status__ = "Production"

EXCEL_FILE = "~/Learning2019Data/Waivers and Surveys - Learning 2019.xlsx"
EXCEL_SHEET = "Data"
EXCEL_HEADER_ROWS = 5 # skip these rows for actual data
EXCEL_ACTIVE_COL = ord('F') - ord('A') # column F, is active
EXCEL_DAY_COL = ord('A') - ord('A') # column A
EXCEL_SESSION_COL = ord('B') - ord('A') # column B
EXCEL_ROOM_COL = ord('C') - ord('A') # column C
EXCEL_CHAIR_COL = ord('D') - ord('A') # column D
EXCEL_CONDITION_COL = ord('I') - ord('A') # column E
EXCEL_STATION_COL = ord('H') - ord('A') # column H, also called Team

EXCEL_PRESURVEY_COL_START = ord('J') - ord('A')
EXCEL_PRESURVEY_COL_END   = ord('N') - ord('A')

EXCEL_POSTSURVEY_COL_START = ord('P') - ord('A')
EXCEL_POSTSURVEY_COL_END   = ord('W') - ord('A')


MYSQL_SEVER = "127.0.0.1"
MYSQL_USER = "data_entry"
MYSQL_PASS = ""
MYSQL_DB = "Learning2019"

import mysql.connector
import argparse
import xlrd

class Team:
    day = '' # M or T (Monday or Tuesday)
    session = '' # S1, S2, S3 or S4
    room = '' # R1 or R2
    station = '' # 1, 2, 3, or 4
    condition = '' # A, B, or C

    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.__dict__ == other.__dict__
        else:
            return False

    def getStringID(self):
        return self.day + self.session + \
            self.room + self.condition + \
                '-' + self.station

class Participant:
    team = None # team object
    chair = '' # which chair was the participant seated?
    pre_survey  = '' # a string representing pre-survey data
    post_survey = '' # a string representing post-survey data    

    def __init__(self, team):
        self.team = team

class ExcelProcessor:
    wb = None # workbook
    sheet = None # sheet
    rows = None # all the rows
    data_remaining = True

    def __init__(self):
        self.wb = xlrd.open_workbook(EXCEL_FILE)
        self.sheet = self.wb.sheet_by_name(EXCEL_SHEET)
        self.rows = self.sheet.get_rows()
        for _ in range(EXCEL_HEADER_ROWS):
            self.rows.__next__()


    def getTeam(self, row):
        t = Team()
        t.day = row[EXCEL_DAY_COL].value
        t.session = row[EXCEL_SESSION_COL].value
        t.room = row[EXCEL_ROOM_COL].value
        t.station = row[EXCEL_STATION_COL].value
        t.condition = row[EXCEL_CONDITION_COL].value[0]
        return t

    def concatenateSurveyOptions(self, row, start, end):
        s = ''
        for i in range(start, end + 1):
            option = row[i].value
            if len(option) != 1:
                option = ' '
            s += option
        return s

    def getParticipant(self, row, team):
        p = Participant(team)
        p.chair = row[EXCEL_CHAIR_COL].value
        p.pre_survey = self.concatenateSurveyOptions(row, 
            EXCEL_PRESURVEY_COL_START, EXCEL_PRESURVEY_COL_END)
        p.post_survey = self.concatenateSurveyOptions(row, 
            EXCEL_POSTSURVEY_COL_START, EXCEL_POSTSURVEY_COL_END)
        return p

    def getNextParticipant(self):
        p = None # participant
        if self.data_remaining:
            try:
                # find next "active" row
                row = self.rows.__next__()
                #print(row[EXCEL_ACTIVE_COL].value)
                while(row[EXCEL_ACTIVE_COL].value != 1):
                    row = self.rows.__next__()
                    #print(row[EXCEL_ACTIVE_COL].value)
                t = self.getTeam(row)
                p = self.getParticipant(row, t)
            except:
                self.data_remaining = False
                p = None
            
        return p

class DataEntry:

    excelP = None # Excel connection

    def __init__(self, excelP):
        self.excelP = excelP
        
    def enterAllData(self):
        exp_no = self.insertExperiment()
        self.insertTeamsAndParticipants(exp_no)

    def insertExperiment(self):
        insert_experiment = (
            "INSERT INTO Learning2019.Experiments (id, description) "
            "VALUES (%s, %s)")
        cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True)
        cursor = cnx.cursor()
        cursor.execute(insert_experiment, (0, "Learning 2019 at Orlando"))
        exp_no = cursor.lastrowid
        print(f"Experient ID {exp_no} inserted into DB.")
        cnx.commit()
        cursor.close()
        cnx.close()
        return exp_no

    def insertTeam(self, experiment, team):
        insert_team = (
            "INSERT INTO Learning2019.Teams "
            "(id, experiment_id, day, session, room, station, condition) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s)")
        
        cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True)
        cursor = cnx.cursor()
        cursor.execute(insert_team, 
            (0, experiment, team.day, team.session, 
             team.room, team.station, team.condition))
        team_id = cursor.lastrowid
        print(f"Team ID {team_id} inserted into DB.")
        cnx.commit()
        cursor.close()
        cnx.close()
        return team_id

    def insertTeamsAndParticipants(self, exp_no):
        p = self.excelP.getNextParticipant()
        old_team = None
        while p is not None:
            if old_team is None:
                old_team = p.team
                team_id = self.insertTeam(exp_no, p.team)
            

if __name__ == "__main__":
    # The password is an external argument so that it doesn't need to be checked into 
    # public version control
    parser = argparse.ArgumentParser(prog='ImportTeamsParticipantsAndSurvey')
    parser.add_argument('--pass')
    parsed_args = parser.parse_args()
    MYSQL_PASS = vars(parsed_args)['pass']

    excelP = ExcelProcessor()
    dataE = DataEntry(excelP)
    dataE.enterAllData()
