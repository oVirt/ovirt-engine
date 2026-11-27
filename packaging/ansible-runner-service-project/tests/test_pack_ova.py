"""Tests for roles/ovirt-ova-pack/files/pack_ova.py.

Run with: python3 -m pytest packaging/ansible-runner-service-project/tests

test_pack fakes losetup and qemu-img so it runs unprivileged. test_pack_real
packs a real disk and needs an oVirt host (root, losetup, qemu-img and the
vdsm user); it is skipped elsewhere.
"""

import errno
import importlib.util
import io
import json
import os
import pwd
import shlex
import shutil
import subprocess
import sys
import tarfile
import tempfile


from xml.dom import minidom

import pytest

PACK_OVA = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), os.pardir, "project", "roles",
    "ovirt-ova-pack", "files", "pack_ova.py")

TAR = 512
FS = 4096

OVF = (
    '<?xml version="1.0" encoding="UTF-8"?>'
    '<ovf:Envelope xmlns:ovf="http://schemas.dmtf.org/ovf/envelope/1/">'
    '<References>'
    '<File ovf:href="disk-a" ovf:id="disk-a" ovf:size="{a}"></File>'
    '<File ovf:href="disk-b" ovf:id="disk-b" ovf:size="{b}"></File>'
    '<File ovf:href="nvram.dat" ovf:id="nvram.dat" ovf:size="4"></File>'
    '</References>'
    '<Section>'
    '<Disk ovf:diskId="d-a" ovf:populatedSize="{a}" ovf:fileRef="disk-a">'
    '</Disk>'
    '<Disk ovf:diskId="d-b" ovf:populatedSize="{b}" ovf:fileRef="disk-b">'
    '</Disk>'
    '</Section>'
    '</ovf:Envelope>')


def load_pack_ova():
    spec = importlib.util.spec_from_file_location("pack_ova", PACK_OVA)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


pack_ova = load_pack_ova()


def ovf_sizes(ovf):
    doc = minidom.parseString(ovf)
    files = {e.getAttribute("ovf:href"): int(e.getAttribute("ovf:size"))
             for e in doc.getElementsByTagName("File")}
    populated = {e.getAttribute("ovf:fileRef"):
                 int(e.getAttribute("ovf:populatedSize"))
                 for e in doc.getElementsByTagName("Disk")
                 if e.hasAttribute("ovf:populatedSize")}
    return files, populated


def write_sparse(path, size, extents):
    """Create a sparse file with data in the given (offset, length) ranges."""
    with open(path, "wb") as f:
        f.truncate(size)
        for offset, length in extents:
            f.seek(offset)
            f.write(b"\xaa" * length)
        f.flush()
        os.fsync(f.fileno())


@pytest.fixture
def sparse_dir(tmp_path):
    probe = tmp_path / "probe"
    write_sparse(probe, 64 * FS, [(0, FS)])
    with open(probe, "rb") as f:
        if os.lseek(f.fileno(), 0, os.SEEK_HOLE) == 64 * FS:
            pytest.skip("the filesystem does not report holes")
    return tmp_path


@pytest.mark.parametrize("extents, offset, size, expected", [
    ([], 0, 64 * FS, 0),
    ([(0, 8 * FS)], 0, 64 * FS, 8 * FS),
    ([(0, FS), (16 * FS, 2 * FS)], 0, 64 * FS, 18 * FS),
    ([(10 * FS, 2 * FS), (50 * FS, 2 * FS)], 8 * FS, 32 * FS, 12 * FS),
    ([(30 * FS, 20 * FS)], 8 * FS, 32 * FS, 40 * FS),
    ([(50 * FS, 2 * FS)], 8 * FS, 32 * FS, 8 * FS),
])
def test_find_last_data_end(sparse_dir, extents, offset, size, expected):
    path = sparse_dir / "disk"
    write_sparse(path, 64 * FS, extents)
    with open(path, "rb") as f:
        end = pack_ova.find_last_data_end(f.fileno(), offset, size)
    assert end == expected


def fake_lseek(results):
    results = iter(results)

    def lseek(fd, pos, whence):
        result = next(results)
        if isinstance(result, OSError):
            raise result
        return result
    return lseek


def os_error(code):
    return OSError(code, os.strerror(code))


@pytest.mark.parametrize("results, expected", [
    ([0, 100, os_error(errno.ENXIO)], 100),
    ([os_error(errno.EINVAL)], 1000),
])
def test_find_last_data_end_errno(monkeypatch, results, expected):
    monkeypatch.setattr(pack_ova.os, "lseek", fake_lseek(results))
    assert pack_ova.find_last_data_end(0, 0, 1000) == expected


@pytest.mark.parametrize("results", [
    [0, 100, os_error(errno.EIO)],
    [0, os_error(errno.EIO)],
])
def test_find_last_data_end_raises_io_errors(monkeypatch, results):
    monkeypatch.setattr(pack_ova.os, "lseek", fake_lseek(results))
    with pytest.raises(OSError) as e:
        pack_ova.find_last_data_end(0, 0, 1000)
    assert e.value.errno == errno.EIO


def update(ovf, sizes):
    encoded = ovf.encode()
    ova = io.BytesIO(b"h" * TAR + encoded + b"t" * TAR)
    pack_ova.update_ovf_sizes(ova, ovf, sizes)
    data = ova.getvalue()
    assert len(data) == 2 * TAR + len(encoded)
    assert data.endswith(b"t" * TAR)
    return data[TAR:TAR + len(encoded)]


def test_update_ovf_sizes():
    body = update(OVF.format(a=393216, b=393216),
                  {"disk-a": 204288, "disk-b": 98304})
    files, populated = ovf_sizes(body)
    assert files == {"disk-a": 204288, "disk-b": 98304, "nvram.dat": 4}
    assert populated == {"disk-a": 204288, "disk-b": 98304}


def test_update_ovf_sizes_keeps_missing_populated_size():
    ovf = OVF.format(a=393216, b=393216).replace(
        ' ovf:populatedSize="393216" ovf:fileRef="disk-b"',
        ' ovf:fileRef="disk-b"')
    files, populated = ovf_sizes(update(ovf, {"disk-b": 98304}))
    assert files["disk-b"] == 98304
    assert "disk-b" not in populated


def test_update_ovf_sizes_raises_when_it_does_not_fit():
    ovf = minidom.parseString(OVF.format(a=9999, b=9999).encode()).toxml(
        encoding="UTF-8").decode()
    ova = io.BytesIO(b"h" * TAR + ovf.encode())
    with pytest.raises(RuntimeError):
        pack_ova.update_ovf_sizes(ova, ovf, {"disk-a": 12288})
    assert ova.getvalue() == b"h" * TAR + ovf.encode()


class FakeHost:
    """Stand-in for losetup, udevadm and "su vdsm -c qemu-img". The fake
    qemu-img copies the source disk to the OVA at the loop device's offset
    and skips zero blocks, like qemu-img's sparse output."""

    def __init__(self, monkeypatch, tmp_path):
        self.tmp_path = tmp_path
        self.loops = {}
        monkeypatch.setattr(pack_ova, "check_output", self.losetup)
        monkeypatch.setattr(pack_ova, "check_call", self.su)
        monkeypatch.setattr(pack_ova, "call", lambda cmd: 0)
        monkeypatch.setattr(pack_ova.pwd, "getpwnam",
                            lambda name: pwd.getpwuid(os.getuid()))

    def losetup(self, cmd):
        assert cmd[:4] == ["losetup", "--find", "--show", "-o"]
        loop = str(self.tmp_path / ("loop%d" % len(self.loops)))
        open(loop, "w").close()
        self.loops[loop] = (cmd[5], int(cmd[4]))
        return (loop + "\n").encode()

    def su(self, cmd):
        source, loop = shlex.split(cmd[3])[-2:]
        ova_path, offset = self.loops[loop]
        fd = os.open(ova_path, os.O_WRONLY)
        try:
            with open(source, "rb") as f:
                pos = 0
                for block in iter(lambda: f.read(FS), b""):
                    if block.strip(b"\0"):
                        os.pwrite(fd, block, offset + pos)
                    pos += len(block)
        finally:
            os.close(fd)


@pytest.mark.parametrize("padding", ["true", "false"])
def test_pack(sparse_dir, monkeypatch, padding):
    FakeHost(monkeypatch, sparse_dir)
    # disk-a has trailing zeros to trim; disk-b fills its measured size,
    # which alignment padding pushes from 5 to 6 digits.
    disk_a = sparse_dir / "a.img"
    disk_a.write_bytes(b"\x11" * 3 * FS + b"\0" * 5 * FS)
    disk_b = sparse_dir / "b.img"
    disk_b.write_bytes(b"\x22" * 99840)
    measured = {"disk-a": 12 * FS, "disk-b": 99840}
    disks = {str(disk_a): {"size": measured["disk-a"], "name": "disk-a"},
             str(disk_b): {"size": measured["disk-b"], "name": "disk-b"}}
    ova = sparse_dir / "vm.ova"

    pack_ova.main(["pack_ova.py", "vm", str(ova),
                   OVF.format(a=measured["disk-a"], b=measured["disk-b"]),
                   json.dumps(disks), "", "abcd", padding])

    alignment = FS if padding == "true" else TAR
    with tarfile.open(ova) as tar:
        members = tar.getmembers()
        assert [m.name for m in members][0] == "vm.ovf"
        files, populated = ovf_sizes(tar.extractfile(members[0]).read())
        for name, source in (("disk-a", disk_a), ("disk-b", disk_b)):
            member = tar.getmember(name)
            assert files[name] == populated[name] == member.size
            assert member.offset_data % alignment == 0
            data = tar.extractfile(member).read()
            assert data.rstrip(b"\0") == source.read_bytes().rstrip(b"\0")
        assert tar.getmember("disk-a").size < measured["disk-a"]
        assert files["nvram.dat"] == tar.getmember("nvram.dat").size == 4
        last = members[-1]
    end = last.offset_data + -(-last.size // TAR) * TAR
    assert os.path.getsize(ova) == end + 2 * TAR


def can_pack_for_real():
    try:
        pwd.getpwnam("vdsm")
    except KeyError:
        return False
    return (os.geteuid() == 0 and shutil.which("losetup") is not None
            and shutil.which("qemu-img") is not None)


@pytest.mark.skipif(not can_pack_for_real(),
                    reason="needs root, losetup, qemu-img and the vdsm user")
def test_pack_real():
    workdir = tempfile.mkdtemp(dir="/var/tmp")
    try:
        os.chmod(workdir, 0o755)
        raw = os.path.join(workdir, "disk.raw")
        write_sparse(raw, 64 * 1024 * 1024,
                     [(0, 1024 * 1024), (40 * 1024 * 1024, 64 * 1024)])
        os.chmod(raw, 0o644)
        measure = subprocess.check_output(
            ["qemu-img", "measure", "--output=json", "-O", "qcow2", raw])
        measured = json.loads(measure)["required"]
        ova = os.path.join(workdir, "vm.ova")

        subprocess.check_call([
            sys.executable, PACK_OVA, "vm", ova,
            OVF.format(a=measured, b=measured),
            json.dumps({raw: {"size": measured, "name": "disk-a"}}),
            "", "", "true"])

        with tarfile.open(ova) as tar:
            member = tar.getmember("disk-a")
            files, populated = ovf_sizes(
                tar.extractfile(tar.getmembers()[0]).read())
        assert files["disk-a"] == populated["disk-a"] == member.size
        assert member.size < measured + FS
        image = "json:" + json.dumps({
            "driver": "qcow2",
            "file": {"driver": "raw", "offset": member.offset_data,
                     "size": member.size,
                     "file": {"driver": "file", "filename": ova}}})
        subprocess.check_call(["qemu-img", "compare", "-f", "raw", raw, image])
    finally:
        shutil.rmtree(workdir)
