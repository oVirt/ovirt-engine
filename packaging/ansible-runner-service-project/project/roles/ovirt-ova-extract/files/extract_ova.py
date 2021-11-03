import io
import json
import mmap
import os
import pwd
import sys
import time


from contextlib import closing
from subprocess import CalledProcessError
from subprocess import call
from subprocess import check_call
from subprocess import check_output

import six
import yaml

NUL = b"\0"
TAR_BLOCK_SIZE = 512

python2 = sys.version_info < (3, 0)


def from_bytes(string):
    return (string.decode('utf-8')
            if isinstance(string, six.binary_type) else string)


def extract_disk(ova_path, offset, image_path, image_format):
    start_time = time.time()
    while True:
        try:
            output = check_output(['losetup', '--find', '--show', '-o',
                                   str(offset), ova_path])
        except CalledProcessError:
            if time.time() - start_time > 10:
                raise
            time.sleep(1)
        else:
            break

    loop = from_bytes(output.splitlines()[0])
    call(['udevadm', 'settle'])
    loop_stat = os.stat(loop)
    vdsm_user = pwd.getpwnam('vdsm')
    os.chown(loop, vdsm_user.pw_uid, vdsm_user.pw_gid)
    try:
        qemu_cmd = ("qemu-img convert -O %s '%s' '%s'"
                    % (image_format, loop, image_path))
        check_call(['su', '-p', '-c', qemu_cmd, 'vdsm'])
    except CalledProcessError as exc:
        print("qemu-img conversion failed with error: ", exc.returncode)
        raise
    finally:
        os.chown(loop, loop_stat.st_uid, loop_stat.st_gid)
        call(['losetup', '-d', loop])


def nts(s, encoding, errors):
    """
    Convert a null-terminated bytes object to a string.
    Taken from tarfile.py (python 3).
    """
    p = s.find(NUL)
    if p != -1:
        s = s[:p]
    return (s if python2 else s.decode(encoding, errors))


def nti(s):
    """
    Convert a number field to a python number.
    Inspired by tarfile.py.
    It is customized to support both python 2 and python 3
    and the prefix 0o377 is ignored because we use this
    function to parse only non-negative values.
    """
    if s[0] != (chr(0o200) if python2 else 0o200):
        try:
            s = nts(s, "ascii", "strict")
            n = int(s.strip() or "0", 8)
        except ValueError:
            print('invalid header')
            raise
    else:
        n = 0
        r = six.moves.xrange(len(s) - 1)
        for i in r:
            n <<= 8
            n += ord(s[i + 1]) if python2 else s[i + 1]
    return n


def extract_disks(ova_path, image_paths_and_formats, image_mappings):
    try:
        fd = os.open(ova_path, os.O_RDONLY | os.O_DIRECT)
    except OSError:
        fd = os.open(ova_path, os.O_RDONLY)
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
            if name.lower().endswith('ovf') or name.lower().endswith('dat'):
                jump = size
                # ovf is typically not aligned to 512 bytes blocks
                remainder = size % TAR_BLOCK_SIZE
                if remainder:
                    jump += TAR_BLOCK_SIZE - remainder
                ova_file.seek(jump, 1)
            elif name == 'pad':
                ova_file.seek(size, 1)
            else:
                image_guid = image_mappings[name] if image_mappings else name
                for image_path_and_format in \
                        six.iteritems(image_paths_and_formats):
                    image_path = image_path_and_format[0]
                    image_format = image_path_and_format[1]
                    if image_guid in image_path:
                        extract_disk(ova_path, ova_file.tell(), image_path,
                                     image_format)
                        ova_file.seek(size, 1)
                        break


if len(sys.argv) < 4:
    print("Usage: extract_ova.py ova_path disks_paths image_mappings")
    sys.exit(2)

extract_disks(sys.argv[1], json.loads(sys.argv[2]), yaml.load(sys.argv[3]))
