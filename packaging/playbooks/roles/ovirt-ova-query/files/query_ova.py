#!/usr/bin/python

import sys
import tarfile

if len(sys.argv) < 2:
    print ("Usage: query_ova.py ova_path")
    quit()

ova_path = sys.argv[1]
with tarfile.open(ova_path) as ova_file:
    ova_entries = ova_file.getmembers()
    ovf_file = ova_file.extractfile(ova_entries[0])
    print (ovf_file.read())
