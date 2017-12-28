#!/usr/bin/python

import os
import sys
import tarfile

if len(sys.argv) < 2:
    print ("Usage: query_ova.py ova_path")
    sys.exit(2)


def is_ovf(filename):
    return filename.lower().endswith('.ovf')


def get_ovf_from_ova_file(ova_path):
    with tarfile.open(ova_path) as ova_file:
        for ova_entry in ova_file.getmembers():
            if is_ovf(ova_entry.name):
                ovf_file = ova_file.extractfile(ova_entry)
                ovf = ovf_file.read()
                break
        else:
            raise Exception('Failed to find OVF in file %s' % ova_path)
    return ovf


def get_ovf_from_ova_dir(ova_path):
    for filename in os.listdir(ova_path):
        if is_ovf(filename):
            ovf_file = open(os.path.join(ova_path, filename))
            ovf = ovf_file.read()
            break
    else:
        raise Exception('Failed to find OVF in dir %s' % ova_path)
    return ovf


ova_path = sys.argv[1]
if os.path.isfile(ova_path):
    ovf = get_ovf_from_ova_file(ova_path)
else:
    ovf = get_ovf_from_ova_dir(ova_path)

print (ovf)
