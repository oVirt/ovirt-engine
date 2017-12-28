#!/usr/bin/python

import io
import mmap
import os
import sys
import tarfile
import time


from contextlib import closing

TAR_BLOCK_SIZE = 512
NUL = b"\0"
BUF_SIZE = 8 * 1024**2


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


def write_ovf(ova_path, ovf):
    ovf = ovf.encode('utf-8')
    print ("writing ovf: %s" % ovf)
    with io.open(ova_path, "r+b") as ova_file:
        tar_info = create_tar_info("vm.ovf", len(ovf))
        ova_file.write(tar_info.tobuf())
        ova_file.write(ovf)
        pad_to_block_size(ova_file)
        os.fsync(ova_file.fileno())


def write_disk(ova_path, disk_path, disk_size):
    print ("writing disk: path=%s size=%d" % (disk_path, disk_size))
    disk_name = os.path.basename(disk_path)
    tar_info = create_tar_info(disk_name, disk_size)
    with io.open(ova_path, "a+b") as ova_file:
        # write tar info
        ova_file.write(tar_info.tobuf())
        os.fsync(ova_file.fileno())

    fd = os.open(ova_path, os.O_RDWR | os.O_DIRECT | os.O_APPEND)
    with io.FileIO(fd, "a+", closefd=True) as ova_file:
        # write the disk content
        buf = mmap.mmap(-1, BUF_SIZE)
        fd = os.open(disk_path, os.O_RDONLY | os.O_DIRECT)
        with closing(buf), \
                io.FileIO(fd, "r", closefd=True) as image:
            while True:
                read = image.readinto(buf)
                if read == 0:
                    break  # done
                written = 0
                while written < read:
                    wbuf = buffer(buf, written, read - written)
                    written += ova_file.write(wbuf)
        os.fsync(ova_file.fileno())


def write_disks(ova_path, disks_info):
    for disk_info in disks_info:
        # disk_info is of the following structure: <full path>::<size in bytes>
        idx = disk_info.index('::')
        disk_path = disk_info[:idx]
        disk_size = int(disk_info[idx+2:])
        write_disk(ova_path, disk_path, disk_size)


def write_null_blocks(ova_file):
    with io.open(ova_path, "a+b") as ova_file:
        ova_file.write(NUL * 2 * TAR_BLOCK_SIZE)


if len(sys.argv) < 3:
    print ("Usage: pack_ova.py output_path ovf [disks_info]")
    sys.exit(2)

ova_path = sys.argv[1]
ovf = sys.argv[2]
write_ovf(ova_path, ovf)
if len(sys.argv) > 3:
    disks_info = sys.argv[3]
    write_disks(ova_path, disks_info.split('+'))
# write two null blocks at the end of the file
write_null_blocks(ova_path)
