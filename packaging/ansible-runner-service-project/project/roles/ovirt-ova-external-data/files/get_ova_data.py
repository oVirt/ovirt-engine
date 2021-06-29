#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#


import sys
import tarfile

if len(sys.argv) != 2:
    print("Usage: get_ova_data.py ova_path")
    sys.exit(2)


def is_tpm(filename):
    return filename.lower() == "tpm.dat"


def is_nvram(filename):
    return filename.lower() == "nvram.dat"


def from_bytes(ovf):
    return ovf.decode('utf-8') if isinstance(ovf, bytes) else ovf


def get_external_data_from_ova_file(ova_path, templates=None):
    external_data = ''
    tpm_found = False
    nvram_found = False
    with tarfile.open(ova_path) as ova_file:
        for ova_entry in ova_file.getmembers():
            name = ova_entry.name
            if is_tpm(name):
                external_data += ';tpm='
                tpm_file = ova_file.extractfile(ova_entry)
                external_data += from_bytes(tpm_file.read())
                tpm_found = True
            elif is_nvram(name):
                external_data += ';nvram='
                nvram_file = ova_file.extractfile(ova_entry)
                external_data += from_bytes(nvram_file.read())
                nvram_found = True
            if tpm_found and nvram_found:
                break
    return external_data


external_data = get_external_data_from_ova_file(sys.argv[1])

print(external_data)
