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

EXCEL_FILE = "C:/Learning2019Data/Waivers and Surveys - Learning 2019.xlsx"
EXCEL_SHEET = "Data"
MYSQL_SEVER = "127.0.0.1"
MYSQL_USER = "data_entry"
MYSQL_PASS = ""
MYSQL_DB = "Learning2019"

import mysql.connector
import argparse
import xlrd

class ExcelProcessor:
    wb = None # workbook
    sheet = None # sheet

    def __init__(self):
        self.wb = xlrd.open_workbook(EXCEL_FILE)
        self.sheet = self.wb.sheet_by_name(EXCEL_SHEET)

class DataEntry:

    excelP = None # Excel connection

    def __init__(self, excelP):
        self.excelP = excelP
        
    def enterAllData(self):
        self.insertExperiment()

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

    def __del__(self):
        #self.cnx.close()
        return

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
