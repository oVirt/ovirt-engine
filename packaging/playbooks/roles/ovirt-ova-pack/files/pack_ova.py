#!/usr/bin/python

import io
import os
import sys
import tarfile
import time


from subprocess import call
from subprocess import check_output

TAR_BLOCK_SIZE = 512
NUL = b"\0"

path_to_offset = {}


def create_tar_info(name, size):
    info = tarfile.TarInfo(name)
    info.size = size
    info.mtime = time.time()
    return info


def pad_to_block_size(file):
    remainder = file.tell() % TAR_BLOCK_SIZE
    if remainder:
        padding_size = TAR_BLOCK_SIZE - remainder
        file.write(NUL * padding_size)


def write_ovf(entity, ova_file, ovf):
    print ("writing ovf: %s" % ovf)
    tar_info = create_tar_info(entity + ".ovf", len(ovf))
    ova_file.write(tar_info.tobuf())
    ova_file.write(ovf)
    pad_to_block_size(ova_file)
    os.fsync(ova_file.fileno())


def convert_disks(ova_path):
    for path, offset in path_to_offset.iteritems():
        print ("converting disk: %s, offset %s" % (path, offset))
        output = check_output(['losetup', '--find', '--show', '-o', offset,
                               ova_path])
        loop = output.splitlines()[0]
        try:
            call(['qemu-img', 'convert', '-T', 'none', '-O', 'qcow2', path,
                  loop])
        finally:
            call(['losetup', '-d', loop])


def write_disk_headers(ova_file, disks_info):
    for disk_info in disks_info:
        # disk_info is of the following structure: <full path>::<size in bytes>
        idx = disk_info.index('::')
        disk_path = disk_info[:idx]
        disk_size = int(disk_info[idx+2:])
        print ("skipping disk: path=%s size=%d" % (disk_path, disk_size))
        disk_name = os.path.basename(disk_path)
        tar_info = create_tar_info(disk_name, disk_size)
        # write tar info
        ova_file.write(tar_info.tobuf())
        path_to_offset[disk_path] = str(ova_file.tell())
        ova_file.seek(disk_size, 1)
    os.fsync(ova_file.fileno())


def write_null_blocks(ova_file):
    ova_file.write(NUL * 2 * TAR_BLOCK_SIZE)


if len(sys.argv) < 3:
    print ("Usage: pack_ova.py <vm/template> output_path ovf [disks_info]")
    sys.exit(2)

entity = sys.argv[1]
ova_path = sys.argv[2]
ovf = sys.argv[3]
with io.open(ova_path, "wb") as ova_file:
    write_ovf(entity, ova_file, ovf)
    if len(sys.argv) > 4:
        disks_info = sys.argv[4]
        write_disk_headers(ova_file, disks_info.split('+'))
    # write two null blocks at the end of the file
    write_null_blocks(ova_file)
convert_disks(ova_path)
