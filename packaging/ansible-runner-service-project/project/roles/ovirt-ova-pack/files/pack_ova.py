import io
import json
import os
import pwd
import shlex
import sys
import tarfile
import time


from subprocess import CalledProcessError
from subprocess import call
from subprocess import check_call
from subprocess import check_output

TAR_BLOCK_SIZE = 512
FS_BLOCK_SIZE = 4096
NUL = b"\0"


def from_bytes(string):
    return (string.decode('utf-8')
            if isinstance(string, bytes) else string)


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
    print("writing ovf: %s" % ovf)
    encoded_ovf = ovf.encode()
    tar_info = create_tar_info(entity + ".ovf", len(encoded_ovf))
    buf = tar_info.tobuf(format=tarfile.GNU_FORMAT)
    ova_file.write(buf)
    ova_file.write(encoded_ovf)
    pad_to_block_size(ova_file)


def write_file(name, ova_file, data):
    print("writing file: %s" % name)
    tar_info = create_tar_info(name, len(data))
    buf = tar_info.tobuf(format=tarfile.GNU_FORMAT)
    ova_file.write(buf)
    ova_file.write(data.encode())
    pad_to_block_size(ova_file)


def write_padding_file(ova_file, padding_size):
    file_size = padding_size - TAR_BLOCK_SIZE
    # padding_size - the padding needed;
    # file_size - the size of the padding file
    #             (minus one block for the header).
    tar_info = create_tar_info("pad", file_size)
    buf = tar_info.tobuf(format=tarfile.GNU_FORMAT)
    ova_file.write(buf)
    pad_to_block_size(ova_file)
    if file_size:
        ova_file.write(NUL * file_size)


def pad_to_fs_block_size(ova_file):
    if padding != "true":
        print("padding is switched off")
        return
    remainder = (ova_file.tell() + TAR_BLOCK_SIZE) % FS_BLOCK_SIZE
    # remainder is a multiple of TAR_BLOCK_SIZE, because everything in TAR
    # file is aligned to TAR_BLOCK_SIZE
    if remainder:
        # remainder is at least TAR_BLOCK_SIZE here
        write_padding_file(ova_file, FS_BLOCK_SIZE - remainder)


def find_last_data_end(fd, offset, size):
    """Find the end of the last data region in a sparse file."""
    end_of_region = offset + size
    pos = offset
    last_data_end = offset
    while pos < end_of_region:
        try:
            data_start = os.lseek(fd, pos, os.SEEK_DATA)
        except OSError:
            break
        if data_start >= end_of_region:
            break
        try:
            hole_start = os.lseek(fd, data_start, os.SEEK_HOLE)
        except OSError:
            hole_start = end_of_region
        last_data_end = min(hole_start, end_of_region)
        pos = hole_start
    return last_data_end


def convert_disk_and_trim(ova_file, disk_path, disk_size, disk_name):
    """Convert a single disk into a loop device over a sparse region
    and trim the last empty portion."""
    header_offset = ova_file.tell()
    data_offset = header_offset + TAR_BLOCK_SIZE
    print("converting disk: %s, offset %s" % (disk_path, data_offset))

    # Extend the OVA file to the pre-discovered worst case size using truncate,
    # which creates a sparse file, which also passes through the loopback
    # device and enables qemu-img to trim the disk during conversion.
    ova_file.flush()
    os.ftruncate(ova_file.fileno(), data_offset + disk_size)

    start_time = time.time()
    while True:
        try:
            output = check_output(['losetup', '--find', '--show', '-o',
                                   str(data_offset), ova_file.name])
        except CalledProcessError:
            if time.time() - start_time > 10:
                raise
            time.sleep(1)
        else:
            break

    loop = from_bytes(output.splitlines()[0])
    loop_stat = os.stat(loop)
    call(['udevadm', 'settle'])
    vdsm_user = pwd.getpwnam('vdsm')
    os.chown(loop, vdsm_user.pw_uid, vdsm_user.pw_gid)
    try:
        qemu_cmd = " ".join([shlex.quote(a) for a in [
            "qemu-img", "convert", "-p", "-T", "none", "-O", "qcow2",
            disk_path, loop,
        ]])
        check_call(['su', '-p', '-c', qemu_cmd, 'vdsm'])
    except CalledProcessError as exc:
        print("qemu-img conversion failed with error: ", exc.returncode)
        raise
    finally:
        os.chown(loop, loop_stat.st_uid, loop_stat.st_gid)
        call(['losetup', '-d', loop])

    # Flush dirty pages to disk so that the filesystem extent map is
    # updated and SEEK_DATA/SEEK_HOLE can work properly. Without this
    # delayed allocation can leave large write-backs pending and cause
    # SEEK_HOLE to report allocated region as a hole leading to data loss.
    os.sync()

    actual_end = find_last_data_end(ova_file.fileno(), data_offset, disk_size)
    actual_size = actual_end - data_offset
    print("disk %s: actual size: %d bytes (reserved %d)"
          % (disk_path, actual_size, disk_size))

    # When FS padding is enabled, extend the data size to a multiple of
    # FS_BLOCK_SIZE, keeping the TAR header in mind (the key point is to
    # keep the next disk's data region aligned to FS_BLOCK_SIZE).
    # Otherwise just align to TAR_BLOCK_SIZE.
    if padding == "true":
        aligned_size = ((actual_size + TAR_BLOCK_SIZE + FS_BLOCK_SIZE - 1)
                        // FS_BLOCK_SIZE) * FS_BLOCK_SIZE - TAR_BLOCK_SIZE
    else:
        aligned_size = ((actual_size + TAR_BLOCK_SIZE - 1)
                        // TAR_BLOCK_SIZE) * TAR_BLOCK_SIZE

    if aligned_size > actual_size:
        print("disk %s: padding with %d bytes for alignment"
              % (disk_path, aligned_size - actual_size))
        actual_size = aligned_size

    ova_file.seek(header_offset)
    tar_info = create_tar_info(disk_name, actual_size)
    buf = tar_info.tobuf(format=tarfile.GNU_FORMAT)
    ova_file.write(buf)

    new_end = data_offset + actual_size
    ova_file.truncate(new_end)
    ova_file.seek(new_end)


def write_null_blocks(ova_file):
    ova_file.write(NUL * 2 * TAR_BLOCK_SIZE)


if len(sys.argv) < 3:
    print("Usage: pack_ova.py <vm/template> output_path ovf"
          " [disks_info [tpm_data] [nvram_data] [padding]]")
    sys.exit(2)

entity = sys.argv[1]
ova_path = sys.argv[2]
ovf = sys.argv[3]
disks_info = sys.argv[4]
tpm_data = sys.argv[5]
nvram_data = sys.argv[6]
padding = sys.argv[7]
with io.open(ova_path, "wb") as ova_file:
    write_ovf(entity, ova_file, ovf)
    if len(tpm_data) > 0:
        write_file("tpm.dat", ova_file, tpm_data)
    if len(nvram_data) > 0:
        write_file("nvram.dat", ova_file, nvram_data)
    pad_to_fs_block_size(ova_file)
    for disk_path, disk_info in json.loads(disks_info).items():
        if isinstance(disk_info, dict):
            disk_size = disk_info["size"]
            disk_name = disk_info["name"]
        else:
            disk_size = disk_info
            disk_name = os.path.basename(disk_path)
        convert_disk_and_trim(ova_file, disk_path, disk_size, disk_name)
    # write two null blocks at the end of the file
    write_null_blocks(ova_file)
