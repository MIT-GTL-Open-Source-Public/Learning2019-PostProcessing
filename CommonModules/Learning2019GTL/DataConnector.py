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
    
    def getCondition(self, team_string):
        t = team_string.split(', ')
        query = "SELECT Teams.condition FROM TEAMS WHERE day = %s AND session = %s AND station = %s"
        self.cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True) # change username and password to one you use
        cursor = self.cnx.cursor(buffered=True)
        cursor.execute(query, (t[0], t[1], t[2][-1]))
        for (condition) in cursor:
            cond_ret = condition
        self.cnx.commit()
        cursor.close()
        self.cnx.close()
        return cond_ret

