#!/usr/bin/python

import os
import sys
import tarfile

if len(sys.argv) < 3:
    print ("Usage: query_ova.py ova_path list_directory")
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


def get_ovf_from_dir(ova_path, list_directory):
    files = os.listdir(ova_path)
    for filename in files:
        if is_ovf(filename):
            ovf_file = open(os.path.join(ova_path, filename))
            return ovf_file.read()
    else:
        if list_directory != 'True':
            raise Exception('Failed to find OVF in dir %s' % ova_path)

        ova_to_ovf = {}
        for filename in files:
            file_path = os.path.join(ova_path, filename)
            if os.path.isfile(file_path) and tarfile.is_tarfile(file_path):
                try:
                    ova_to_ovf[filename] = get_ovf_from_ova_file(file_path)
                except Exception:
                    pass

        if ova_to_ovf:
            pairs = '::'.join("%s=%s" % (key, val) for
                              (key, val) in ova_to_ovf.iteritems())
            return '{%s}' % (pairs)
        else:
            raise Exception('Failed to find OVF in dir %s' % ova_path)


ova_path = sys.argv[1]
if os.path.isfile(ova_path):
    ovf = get_ovf_from_ova_file(ova_path)
else:
    ovf = get_ovf_from_dir(ova_path, sys.argv[2])

print (ovf)
