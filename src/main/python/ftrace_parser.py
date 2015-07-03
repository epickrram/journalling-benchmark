import sys
import re

TIMESTAMP_REGEX = ".* ([0-9]+\.[0-9]{6}):.*"
SYSCALL_EVENT_REGEX = ".* sys_([^:]+):.*"
NO_TIMESTAMP = -1

CALL_ENTER_TIMESTAMP = dict()
CALL_ENTER_LINE = dict()

def parse_timestamp(line):
    return int(float(line.strip()) * 1000000)

for line in sys.stdin:
    try:
        timestamp_match = re.search(TIMESTAMP_REGEX, line)
        syscall_match = re.search(SYSCALL_EVENT_REGEX, line)

        if timestamp_match is not None and syscall_match is not None:
            timestamp_micros = parse_timestamp(timestamp_match.group(1))
            event = syscall_match.group(1)
            syscall_id = "_".join(event.split("_")[1:])

            if event.find("enter_") == 0:
                CALL_ENTER_TIMESTAMP[syscall_id] = timestamp_micros
                CALL_ENTER_LINE[syscall_id] = line
            elif event.find("exit_") == 0:
                delta_micros = timestamp_micros - CALL_ENTER_TIMESTAMP[syscall_id]
                print str(delta_micros) + " " + syscall_id


    except ValueError:
        sys.stderr.write("Failed to parse: " + line + "\n")


