#!/bin/bash

if [ $(ls $(git rev-parse --show-toplevel)/backend/manager/dbscripts/upgrade | grep -P '\d{2}_\d{2}.\d{2,8}' -o | uniq -d | wc -l) -ne 0 ]; then
 version=$(ls $(git rev-parse --show-toplevel)/backend/manager/dbscripts/upgrade | grep -P '\d{2}_\d{2}.\d{2,8}' -o | uniq -d)
 echo "Found duplicate upgrade scripts with version ${version}, please resolve and retry"
 exit 1
fi
exit 0


