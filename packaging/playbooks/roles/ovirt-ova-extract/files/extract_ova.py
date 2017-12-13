#!/usr/bin/python

import math
import sys
import tarfile

from ovirt_imageio_common import directio

BUF_SIZE = 8 * 1024 * 1024
TAR_BLOCK_SIZE = 512


class SendAdapter(object):
    def __init__(self, send):
        self.iter = iter(send)

    def read(self, size):
        # we use the same buffer size in both Send and Receive
        # so we will have 1 chunk at most
        return self.iter.next()

    def finish(self):
        pass


def extract_disk(ova_path, pos, disk_size, image_path):
    send = directio.Send(ova_path, None, offset=pos, size=disk_size,
                         buffersize=BUF_SIZE)
    op = directio.Receive(image_path, SendAdapter(send), size=disk_size,
                          buffersize=BUF_SIZE)
    op.run()


def extract_disks(ova_path, ova_entries, image_paths):
    pos = 0
    for ova_entry in ova_entries:
        # skip the header
        pos += TAR_BLOCK_SIZE
        entry_size = ova_entry.size
        for image_path in image_paths:
            if ova_entry.name in image_path:
                extract_disk(ova_path, pos, entry_size, image_path)
                break
        entry_num_of_blocks = math.ceil(entry_size * 1.0 / TAR_BLOCK_SIZE)
        padded_entry_size = int(entry_num_of_blocks * TAR_BLOCK_SIZE)
        pos += padded_entry_size


if len(sys.argv) < 3:
    print ("Usage: extract_ova.py ova_path disks_paths")
    sys.exit(2)

ova_path = sys.argv[1]
with tarfile.open(ova_path) as ova_file:
    ova_entries = ova_file.getmembers()
extract_disks(ova_path, ova_entries, sys.argv[2].split(','))
