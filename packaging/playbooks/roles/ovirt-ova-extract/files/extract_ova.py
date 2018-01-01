#!/usr/bin/python

import io
import mmap
import os
import sys


from contextlib import closing

NUL = b"\0"
BUF_SIZE = 8 * 1024**2
TAR_BLOCK_SIZE = 512


def extract_disk(ova_file, disk_size, image_path):
    fd = os.open(image_path, os.O_RDWR | os.O_DIRECT)
    buf = mmap.mmap(-1, BUF_SIZE)
    with closing(buf), io.FileIO(fd, "r+", closefd=True) as image:
        copied = 0
        while copied < disk_size:
            read = ova_file.readinto(buf)
            remaining = disk_size - copied
            if remaining < read:
                # read too much (disk size is not aligned
                # with BUF_SIZE), thus need to go back
                ova_file.seek(remaining - read, 1)
                read = remaining
            written = 0
            while written < read:
                wbuf = buffer(buf, written, read - written)
                written += image.write(wbuf)
            copied += written


def nts(s, encoding, errors):
    """
    Convert a null-terminated bytes object to a string.
    Taken from tarfile.py.
    """
    p = s.find(b"\0")
    if p != -1:
        s = s[:p]
    return s.decode(encoding, errors)


def nti(s):
    """
    Convert a number field to a python number.
    Taken from tarfile.py.
    """
    if s[0] in (0o200, 0o377):
        n = 0
        for i in range(len(s) - 1):
            n <<= 8
            n += s[i + 1]
        if s[0] == 0o377:
            n = -(256 ** (len(s) - 1) - n)
    else:
        try:
            s = nts(s, "ascii", "strict")
            n = int(s.strip() or "0", 8)
        except ValueError:
            print 'invalid header'
            raise
    return n


def extract_disks(ova_path, image_paths):
    fd = os.open(ova_path, os.O_RDONLY | os.O_DIRECT)
    buf = mmap.mmap(-1, TAR_BLOCK_SIZE)
    with io.FileIO(fd, "r", closefd=True) as ova_file, \
            closing(buf):
        while True:
            # read next tar info
            ova_file.readinto(buf)
            info = buf.read(512)
            # tar files end with NUL blocks
            if info == NUL*512:
                break
            # preparation for the next iteration
            buf.seek(0)
            # extract the next disk to the corresponding image
            name = nts(info[0:100], 'utf-8', 'surrogateescape')
            size = nti(info[124:136])
            if name.lower().endswith('ovf'):
                jump = size
                # ovf is typically not aligned to 512 bytes blocks
                remainder = size % TAR_BLOCK_SIZE
                if remainder:
                    jump += TAR_BLOCK_SIZE - remainder
                ova_file.seek(jump, 1)
            else:
                for image_path in image_paths:
                    if name in image_path:
                        extract_disk(ova_file, size, image_path)
                        break


if len(sys.argv) < 3:
    print ("Usage: extract_ova.py ova_path disks_paths")
    sys.exit(2)

extract_disks(sys.argv[1], sys.argv[2].split('+'))
