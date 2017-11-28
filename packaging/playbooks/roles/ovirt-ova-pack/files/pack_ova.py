#!/usr/bin/python

import io
import os
import sys
import tarfile
import time

BLOCKSIZE = 512
NUL = "\0"
buf = bytearray(4096)


def create_tar_info(name, size):
    info = tarfile.TarInfo(name)
    info.size = size
    info.mtime = time.time()
    return info


def pad_to_block_size(file, size):
    remainder = size % BLOCKSIZE
    if remainder > 0:
        file.write(NUL * (BLOCKSIZE - remainder))


def write_ovf(ova_file, ovf):
    print ("writing ovf: %s" % ovf)
    ovf_size = len(ovf.encode('utf-8'))
    tar_info = create_tar_info("vm.ovf", ovf_size)
    ova_file.write(tar_info.tobuf())
    ova_file.write(ovf)
    pad_to_block_size(ova_file, ovf_size)


def write_disk(ova_file, disk_path, disk_size):
    print ("writing disk: path=%s size=%d" % (disk_path, disk_size))
    disk_name = os.path.basename(disk_path)
    tar_info = create_tar_info(disk_name, disk_size)
    ova_file.write(tar_info.tobuf())
    with io.FileIO(disk_path, "r+") as disk_file:
        while disk_file.readinto(buf):
            ova_file.write(buf)
    pad_to_block_size(ova_file, disk_size)


def write_disks(ova_file, disks_info):
    for disk_info in disks_info:
        # disk_info is of the following structure: <full path>::<size in bytes>
        idx = disk_info.index('::')
        disk_path = disk_info[:idx]
        disk_size = int(disk_info[idx+2:])
        write_disk(ova_file, disk_path, disk_size)


def write_null_blocks(ova_file):
    empty_block = NUL * BLOCKSIZE
    ova_file.write(empty_block)
    ova_file.write(empty_block)


if len(sys.argv) < 3:
    print ("Usage: pack_ova.py output_path ovf [disks_info]")
    quit()

ova_path = sys.argv[1]
print ("opening for write: %s" % ova_path)
with io.FileIO(ova_path, "w") as ova_file:
    write_ovf(ova_file, sys.argv[2])
    write_disks(ova_file, sys.argv[3:])
    # writing two null blocks at the end of the file
    write_null_blocks(ova_file)
