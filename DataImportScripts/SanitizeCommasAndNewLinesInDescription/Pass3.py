#!/usr/bin/env python

"""
Pass3.py: This script replaces commas in the description column and adds a Team-ID column.

This file was written to automate a very specific part of the project,
and may only be generically useful as a template, not as a reusable
component.
"""

__author__      = "Prakash Manandhar, Keran Rong"
__copyright__ = "Copyright 2019, Prakash Manandhar, Keran Rong"
__credits__ = ["Prakash Manandhar", "Keran Rong"]
__license__ = "MIT"
__version__ = "1.0.0"
__maintainer__ = "Prakash Manandhar"
__email__ = "engineer.manandhar@gmail.com"
__status__ = "Production"

import sys
from datetime import datetime

num_simulations = 0
lowest_cost = 0
average_num_blds = 0
average_num_flrs = 0
best_interaction = 0


def writeline_replace_newline(fp, line):
    fp.writelines(line.replace('\n', ' ') + '\n')

def parse_timestamp(timestamp_str):
    date_str = timestamp_str[:10]
    date_str.replace('-', '/')
    time_str = timestamp_str[11:19]
    return date_str, time_str


def process_line(line, station, period, first_time_str):

    FIRST_COLUMNS = 34
    line_segments = line.split(',')

    team_id = f'{period}-{station}'    
    line_out = f'{station}, {period}, {team_id}'

    date_str, time_str = parse_timestamp(line_segments[0])
    line_out += f', {date_str}, {time_str}'

    FMT = '%H:%M:%S'
    tdelta = datetime.strptime(time_str, FMT) - datetime.strptime(first_time_str, FMT)
    #print(tdelta)
    #print(time_str, first_time_str)
    line_out += f', {str(tdelta)}, '

    #print(len(line_segments))
    for i in range(1, len(line_segments)):
        #print(str(i) + " : " + line_segments[i])
        line_out += line_segments[i]
        if (i < len(line_segments) - 1):
             line_out += ', '

    if (len(line_segments) > 41):
        num_floors = int(line_segments[ 3]) + \
                    int(line_segments[ 8]) + \
                    int(line_segments[13]) + \
                    int(line_segments[18]) + \
                    int(line_segments[23]) + \
                    int(line_segments[28])
        line_out += f'{num_floors}'

    return line_out

def process_file(fname, station, period, time_substring):
    fp_out = open(time_substring + "_p3.csv", "w")
    #fp_params_out = open(fname + "_p3.params.txt", "w")
    real_line = ""
    
    with open(fname, "r") as fp:
        first_line = fp.readline()
        first_line = first_line.replace("Timestamp,Type,", "Station, Period, Team-ID, Date, Time, TimeFromStart, Type,")
        writeline_replace_newline(fp_out, first_line + ", Cost, Walking, Team Mix, Interaction, #floors")
        
        line = fp.readline()
        line_segments = line.split(',')
        date_str, time_str = parse_timestamp(line_segments[0])
        real_line = process_line(line, station, period, time_str)
        writeline_replace_newline(fp_out, real_line)

        line = fp.readline()    
        while line:
            real_line = process_line(line, station, period, time_str)
            writeline_replace_newline(fp_out, real_line)
            line = fp.readline()
            
    fp_out.close()
    
if __name__ == '__main__':

    team_id_map = {
        "28_08": 1,
        "28_10": 2,
        "28_13": 3,
        "28_14": 4,
        "29_06": 5,
        "29_09": 6,
        "29_10": 6,
        "29_13": 7,
        "29_14": 8
    }

    fname = sys.argv[1]
    fname_split = fname.split(".")
    team_id = fname_split[0]
    station = int(fname_split[0][-1])

    time_substring = fname[12:17]
    period = team_id_map[time_substring]
    print('Processing ' + fname)
    process_file(fname, station, period, fname_split[0])