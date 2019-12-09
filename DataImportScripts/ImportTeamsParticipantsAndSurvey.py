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


EXCEL_FILE = ""
MYSQL_SEVER = "127.0.0.1"
MYSQL_USER = "data_entry"
MYSQL_PASS = ""
MYSQL_DB = "Learning2019"

import mysql.connector
import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='ImportTeamsParticipantsAndSurvey')
    parser.add_argument('--pass')
    parsed_args = parser.parse_args()
    MYSQL_PASS = vars(parsed_args)['pass']

    cnx = mysql.connector.connect(user=MYSQL_USER, password=MYSQL_PASS,
                                host=MYSQL_SEVER,
                                database=MYSQL_DB, use_pure=True)
    cnx.close()