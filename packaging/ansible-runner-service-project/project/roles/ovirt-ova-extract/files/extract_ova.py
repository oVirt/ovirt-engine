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

NUL = b"\0"
TAR_BLOCK_SIZE = 512


def from_bytes(string):
    return (string.decode('utf-8')
            if isinstance(string, bytes) else string)


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
    """Convert a null-terminated bytes object to a string."""
    p = s.find(NUL)
    if p != -1:
        s = s[:p]
    return s.decode(encoding, errors)


def nti(s):
    """Convert a tar number field to a python number."""
    if s[0] != 0o200:
        try:
            return int(nts(s, "ascii", "strict").strip() or "0", 8)
        except ValueError:
            print('invalid header')
            raise
    n = 0
    for i in range(len(s) - 1):
        n = (n << 8) + s[i + 1]
    return n


def extract_disks(ova_path, disks):
    """Walk the OVA tar and extract each disk listed in `disks`.

    `disks` is a map keyed by tar member name:
        {tar_name: {"path": target_path, "format": "raw"|"qcow2"}, ...}
    Tar members not in the map (vm.ovf, nvram.dat, pad alignment) are skipped.
    """
    try:
        fd = os.open(ova_path, os.O_RDONLY | os.O_DIRECT)
    except OSError:
        fd = os.open(ova_path, os.O_RDONLY)
    buf = mmap.mmap(-1, TAR_BLOCK_SIZE)
    with io.FileIO(fd, "r", closefd=True) as ova_file, closing(buf):
        while True:
            ova_file.readinto(buf)
            info = buf.read(TAR_BLOCK_SIZE)
            if info == NUL * TAR_BLOCK_SIZE:
                break
            buf.seek(0)
            name = nts(info[0:100], 'utf-8', 'surrogateescape')
            size = nti(info[124:136])
            target = disks.get(name)
            if target is not None:
                extract_disk(ova_path, ova_file.tell(),
                             target["path"], target["format"])
            # Skip past the entry's body, rounded up to the next 512-byte boundary.
            aligned = (size + TAR_BLOCK_SIZE - 1) & ~(TAR_BLOCK_SIZE - 1)
            ova_file.seek(aligned, 1)


if len(sys.argv) < 3:
    print("Usage: extract_ova.py ova_path disks_json")
    sys.exit(2)

extract_disks(sys.argv[1], json.loads(sys.argv[2]))
