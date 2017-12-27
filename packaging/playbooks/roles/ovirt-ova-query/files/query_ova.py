#!/usr/bin/python

import sys
import tarfile

if len(sys.argv) < 2:
    print ("Usage: query_ova.py ova_path")
    sys.exit(2)

ova_path = sys.argv[1]
with tarfile.open(ova_path) as ova_file:
    for ova_entry in ova_file.getmembers():
        if ova_entry.name.lower().endswith('ovf'):
            ovf_file = ova_file.extractfile(ova_entry)
            print (ovf_file.read())
            break
