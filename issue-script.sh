#!/bin/bash

echo "This is a custom script for de-duplication of issues"

ls -lrt | grep -i 10.91-REL | awk {'print "./" $9 "/image-multiscan/" $9 ".csv"'} | xargs -I {} cat  {} | awk -F"," {'print $6 "," $7 "," $8 "," $13 "," $14 "," $9 "," $10'}  | sort -n -n | uniq> unique-cve.csv
