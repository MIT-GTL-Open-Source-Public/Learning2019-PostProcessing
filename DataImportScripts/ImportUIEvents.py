#!/usr/bin/env python

"""
ImportUIEvents.py: 
This script loads data from a MS-Excel file that contains UI 
"fingerprints" and simulation trajectory data into a MySQL 
relational database.

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


CSV_MAP = {
    "M, S1, Station 1": "Masie_20191028_080157_1_p3.csv",
    "M, S1, Station 2": "Masie_20191028_080143_2_p3.csv",
    "M, S1, Station 3": "Masie_20191028_080159_3_p3.csv",
    "M, S1, Station 4": "Masie_20191028_080101_4_p3.csv",

    "M, S2, Station 1": "Masie_20191028_104552_1_p3.csv",
    "M, S2, Station 2": "Masie_20191028_104555_2_p3.csv",
    "M, S2, Station 3": "Masie_20191028_104638_3_p3.csv",
    "M, S2, Station 4": "Masie_20191028_104654_4_p3.csv",

    "M, S2, Station 6": "Masie_20191028_104059_6_p3.csv",
    "M, S2, Station 7": "Masie_20191028_104238_7_p3.csv",
    "M, S2, Station 8": "Masie_20191028_103954_8_p3.csv",

    "M, S3, Station 1": "Masie_20191028_133506_1_p3.csv",
    "M, S3, Station 3": "Masie_20191028_133533_3_p3.csv",
    "M, S3, Station 4": "Masie_20191028_133443_4_p3.csv",

    "M, S3, Station 7": "Masie_20191028_133328_7_p3.csv",
    "M, S3, Station 8": "Masie_20191028_133058_8_p3.csv",

    "M, S4, Station 1": "Masie_20191028_145417_1_p3.csv",
    "M, S4, Station 2": "Masie_20191028_145403_2_p3.csv", 
    "M, S4, Station 3": "Masie_20191028_145424_3_p3.csv",
    "M, S4, Station 4": "Masie_20191028_145347_4_p3.csv",

    "M, S4, Station 5": "Masie_20191028_133119_5_p3.csv",
    "M, S4, Station 6": "Masie_20191028_133137_6_p3.csv",
    "M, S4, Station 7": "Masie_20191028_145135_7_p3.csv",
    "M, S4, Station 8": "Masie_20191028_145116_8_p3.csv", 

    #For T, S1:

    "T, S1, Station 1": "Masie_20191029_065822_1_p3.csv",
    "T, S1, Station 4": "Masie_20191029_065859_4_p3.csv",

    "T, S1, Station 5": "Masie_20191029_065417_5_p3.csv",
    "T, S1, Station 6": "Masie_20191029_065445_6_p3.csv",
    "T, S1, Station 7": "Masie_20191029_065320_7_p3.csv",
    "T, S1, Station 8": "Masie_20191029_065350_8_p3.csv",

    #For T, S2:

    "T, S2, Station 1": "Masie_20191029_090433_1_p3.csv",
    "T, S2, Station 2": "Masie_20191029_090421_2_p3.csv",
    "T, S2, Station 4": "Masie_20191029_090346_4_p3.csv",

    "T, S2, Station 5": "Masie_20191029_090319_5_p3.csv",
    "T, S2, Station 6": "Masie_20191029_090339_6_p3.csv",
    "T, S2, Station 8": "Masie_20191029_090233_8_p3.csv",

    #For T, S3:

    "T, S3, Station 1": "Masie_20191029_131446_1_p3.csv",
    "T, S3, Station 2": "Masie_20191029_131439_2_p3.csv",
    "T, S3, Station 3": "Masie_20191029_131502_3_p3.csv",
    "T, S3, Station 4": "Masie_20191029_131517_4_p3.csv",

    "T, S3, Station 5": "Masie_20191029_102136_5_p3.csv",
    "T, S3, Station 6": "Masie_20191029_101851_6_p3.csv",
    "T, S3, Station 7": "Masie_20191029_090130_7_p3.csv",
    "T, S3, Station 8": "Masie_20191029_101949_8_p3.csv",

    #For T, S4:

    "T, S4, Station 2": "Masie_20191029_144705_2_p3.csv",
    "T, S4, Station 3": "Masie_20191029_144716_3_p3.csv",
    "T, S4, Station 4": "Masie_20191029_144938_4_p3.csv",

    "T, S4, Station 5": "Masie_20191029_144417_5_p3.csv",
    "T, S4, Station 6": "Masie_20191029_144416_6_p3.csv",
    "T, S4, Station 7": "Masie_20191029_144241_7_p3.csv",
}

CSV_DATA_FOLDER = "~/Learning2019Data/SanitizedData/Processed-WithTeamID"

MYSQL_SEVER = "127.0.0.1"
MYSQL_USER = "data_entry"
MYSQL_PASS = ""
MYSQL_DB = "Learning2019"

import mysql.connector
import argparse
import os
import csv

class UIEventDataEntry:

    cnx = None # database connection

    def enterAllData(self):
        # iterate through teams
        select_query = "SELECT id, day, session, station from Learning2019.Teams"
        self.cnx = mysql.connector.connect(
                        user=MYSQL_USER, password=MYSQL_PASS,
                        host=MYSQL_SEVER,
                        database=MYSQL_DB, use_pure=True)
        cursor = self.cnx.cursor()
        cursor.execute(select_query)
        for (id, day, session, station) in cursor:
            key = f"{day}, {session}, Station {station}"
            print(f"Processing Team {id}: {key}")
            self.insertUIEventsForTeam(id, key)

        self.cnx.commit()
        cursor.close()
        self.cnx.close()
        return

    def insertUIEventsForTeam(self, team_id, team_key):
        csv_file = CSV_MAP[team_key]
        csv_fpath = os.path.join(CSV_DATA_FOLDER, csv_file)
        csv_fpath = os.path.expanduser(csv_fpath)
        with open(csv_fpath) as fp:
            csv.register_dialect('MyDialect', quotechar='"', skipinitialspace=True, quoting=csv.QUOTE_NONE, lineterminator='\n', strict=True)
            reader = csv.DictReader(fp, dialect='MyDialect')
            for row in reader:
                print(row)
                #print(row['Team-ID'])
                break
        return

if __name__ == "__main__":
    # The password is an external argument so that it doesn't need to be checked into 
    # public version control
    parser = argparse.ArgumentParser(prog='ImportTeamsParticipantsAndSurvey')
    parser.add_argument('--pass')
    parsed_args = parser.parse_args()
    MYSQL_PASS = vars(parsed_args)['pass']

    dE = UIEventDataEntry()
    dE.enterAllData()
