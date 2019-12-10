#!/usr/bin/env python

"""
Pass2.py: This script replaces new-lines in the description column.

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

def writeline_replace_newline(fp, line):
    fp.writelines(line.replace('\n', ' ') + '\n')

def process_line(line):
    FIRST_COLUMNS = 34
    line_segments = line.split(',')
    line_out = ""
    #print(len(line_segments))
    for i in range(FIRST_COLUMNS):
        #print(str(i) + " : " + line_segments[i])
        line_out += line_segments[i] + ', '
    
    while (line_segments[i] != "Total Cost"):
        i += 1
    
    line_out += '"'
    for j in range(FIRST_COLUMNS, i):
        if j == (i - 1):
            line_out += line_segments[j] + '", '
        else:
            line_out += line_segments[j] + "; " 

    for k in range(i, len(line_segments)):
        line_out += line_segments[k] + ', '

    return line_out

def process_file(fname):
    fp_out = open(fname + "_p2.csv", "w")
    
    with open(fname, "r") as fp:
        first_line = fp.readline()
        fp_out.writelines(first_line)
        line = fp.readline()
        while line:
            real_line = process_line(line)
            writeline_replace_newline(fp_out, real_line)
            line = fp.readline()
            
    fp_out.close()
    
if __name__ == '__main__':
    fname = sys.argv[1]
    print('Processing ' + fname)
    process_file(fname)