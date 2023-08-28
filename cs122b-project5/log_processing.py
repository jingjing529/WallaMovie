import re

log_file = 'test.txt'  # Replace with your log file name

ts_samples = []
tj_samples = []

with open(log_file, 'r') as file:

    for line in file:
        print(line)
        ts_match = re.search(r'TS: (\d+) milliseconds', line)
        tj_match = re.search(r'TJ: (\d+) milliseconds', line)
        if ts_match:
            ts_samples.append(int(ts_match.group(1)))
        if tj_match:
            tj_samples.append(int(tj_match.group(1)))

if ts_samples:
    average_ts = sum(ts_samples) / len(ts_samples)
    print(f'Average TS: {average_ts} milliseconds')
else:
    print('No TS samples found')

if tj_samples:
    average_tj = sum(tj_samples) / len(tj_samples)
    print(f'Average TJ: {average_tj} milliseconds')
else:
    print('No TJ samples found')
