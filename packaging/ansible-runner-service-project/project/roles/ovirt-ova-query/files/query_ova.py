import os
import sys
import tarfile

import six

if len(sys.argv) < 4:
    print("Usage: query_ova.py <vm/template> ova_path list_directory")
    sys.exit(2)


def is_ovf(filename):
    return filename.lower().endswith('.ovf')


def is_ova(filename):
    return filename.lower().endswith('.ova')


def match_entity(filename, templates):
    template_ovf = filename == "template.ovf"
    return template_ovf if templates else not template_ovf


def from_bytes(ovf):
    return (ovf.decode('utf-8')
            if isinstance(ovf, six.binary_type) else ovf)


def get_ovf_from_ova_file(ova_path, templates=None):
    with tarfile.open(ova_path) as ova_file:
        for ova_entry in ova_file.getmembers():
            name = ova_entry.name
            if is_ovf(name):
                if templates is not None and not match_entity(name,
                                                              templates):
                    raise Exception('Not the entity we look for')
                ovf_file = ova_file.extractfile(ova_entry)
                ovf = from_bytes(ovf_file.read())
                break
        else:
            raise Exception('Failed to find OVF in file %s' % ova_path)
    return ovf


def get_ovf_from_dir(ova_path, list_directory, templates):
    files = os.listdir(ova_path)
    for filename in files:
        if is_ovf(filename):
            ovf_file = open(os.path.join(ova_path, filename))
            return from_bytes(ovf_file.read())
    else:
        if list_directory != 'True':
            raise Exception('Failed to find OVF in dir %s' % ova_path)

        ova_to_ovf = {}
        for filename in files:
            if not is_ova(filename):
                continue
            file_path = os.path.join(ova_path, filename)
            if os.path.isfile(file_path) and tarfile.is_tarfile(file_path):
                try:
                    ova_to_ovf[filename] = get_ovf_from_ova_file(file_path,
                                                                 templates)
                except Exception:
                    pass

        if ova_to_ovf:
            pairs = '::'.join("%s=%s" % (key, val) for
                              (key, val) in six.iteritems(ova_to_ovf))
            return '{%s}' % (pairs)
        else:
            raise Exception('Failed to find OVF in dir %s' % ova_path)


ova_path = sys.argv[2]
if os.path.isfile(ova_path):
    ovf = get_ovf_from_ova_file(ova_path)
else:
    templates = sys.argv[1] == "template"
    ovf = get_ovf_from_dir(ova_path, sys.argv[3], templates)

print(ovf)
