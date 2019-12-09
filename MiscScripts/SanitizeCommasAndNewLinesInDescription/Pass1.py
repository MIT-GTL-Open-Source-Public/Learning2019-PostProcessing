#!/usr/bin/env python

"""
Pass1.py: This script replaces new-lines in the description column.

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

def process_file(fname):
    fp_out = open(fname + "_processed.csv", "w")
    real_line = ""
    
    with open(fname, "r") as fp:
        first_line = fp.readline()
        fp_out.writelines(first_line)
        line = fp.readline()
        while line:
            if (len(line) > 4) and (line[:4] == "2019"):
                if len(real_line) > 0:
                    writeline_replace_newline(fp_out, real_line)
                real_line = line
            else:
                real_line += " " + line
            line = fp.readline()
        writeline_replace_newline(fp_out, real_line)
    fp_out.close()
    
if __name__ == '__main__':
    fname = sys.argv[1]
    print('Processing ' + fname)
    process_file(fname)