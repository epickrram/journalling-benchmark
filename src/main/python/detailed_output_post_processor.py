#!/usr/bin/python

import sys
import re

HISTOGRAM_VALUE_LINE = re.compile('^\s+([\d\.]+)\s+([\d\.]+)\s+([\d\.]+)\s+([\d\.]+)\s+$')

def process(input_file, label):
    histogram_data = []
    counter = 0
    gnuplot_command = ""
    for line in open(input_file):
        value_match = HISTOGRAM_VALUE_LINE.search(line)
        if line.find('Value') > -1 and line.find('Percentile') > -1:
            if len(histogram_data) != 0:
                filename = label + '-' + str(counter) + '.csv'
                output_file = open(filename, 'w')
                gnuplot_command += '"' + filename + '" using 3:1 with lines ls 1, '
                for data_tuple in histogram_data:
                    output_file.write(','.join(data_tuple))
                    output_file.write('\n')
                output_file.close()
                histogram_data = []
                counter += 1
            
        elif value_match:
            value = value_match.group(1)
            percentile = value_match.group(2)
            reciprocal = value_match.group(4)
            histogram_data.append([value, percentile, reciprocal])

    print gnuplot_command

if '__main__' == __name__:
    input_file = sys.argv[1]
    label = sys.argv[2]
    process(input_file, label)
