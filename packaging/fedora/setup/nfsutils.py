import subprocess
import os
import uuid
import hashlib
import logging
import basedefs
import common_utils as utils
import output_messages
import shutil

SELINUX_RW_LABEL = "public_content_rw_t"

SHA_CKSUM_TAG = "_SHA_CKSUM"

_preprocessLine = lambda line : unicode.encode(unicode(line), 'ascii', 'xmlcharrefreplace')

def addNfsExport(path, authInfo, comment=None, exportFilePath=basedefs.FILE_ETC_EXPORTS):
    logging.debug("adding path %s to %s" % (path, exportFilePath))
    line = path + "\t"
    for ip, mask, options in authInfo:
        line += ip
        if mask:
            line += "/" + mask

        line += "(%s)\t" % (",".join(options),)

    if comment:
        line += "#" + comment

    exportFile = open(exportFilePath, "a")
    try:
        exportFile.write(line + "\n")
    finally:
        exportFile.close()


def _backupOldNfsExports(exportFilePath=basedefs.FILE_ETC_EXPORTS):
    logging.debug("Backup old NFS exports configuration file")
    dateTimeSuffix = utils.getCurrentDateTime()
    backupFile = "%s.%s.%s" % (exportFilePath, "BACKUP", dateTimeSuffix)
    logging.debug("Backing up %s into %s", exportFilePath, backupFile)
    shutil.move(exportFilePath, backupFile)


def cleanNfsExports(comment, exportFilePath=basedefs.FILE_ETC_EXPORTS):
    """
    Remove all the lines added by engine-setup marked by comment from
    exportFilePath.
    """
    # TODO: add support for /etc/exports.d
    if not "#" in comment:
        comment = "#%s" % comment
    removed_exports = []
    new_lines = []
    with open(exportFilePath, "r") as exportFile:
        lines = exportFile.readlines()
        for line in lines:
            if comment in line or "#rhev installer" in line:
                logging.debug("removing %s from %s" % (line, exportFilePath))
                path = line.split("\t")[0]
                removed_exports.append(path)
                continue
            else:
                new_lines.append(line)
    if len(removed_exports) == 0:
        # Unchanged
        return removed_exports
    # backup existing configuration and rewrite it
    _backupOldNfsExports(exportFilePath)
    with open(exportFilePath, "w") as exportFile:
        exportFile.writelines(new_lines)
    os.chmod(exportFilePath, 0644)
    cmd = [ basedefs.EXEC_RESTORECON, exportFilePath ]
    utils.execCmd(cmdList=cmd, failOnError=True,
                  msg=output_messages.ERR_REFRESH_SELINUX_CONTEXT)
    return removed_exports


def setSELinuxContextForDir(path, contextName):
    logging.debug("setting selinux context for %s" % path)
    if path.endswith("/"):
        path = path[:-1]
    pattern = "%s(/.*)?" % path

    # Run semanage
    cmd = [
        basedefs.EXEC_SEMANAGE,
        "fcontext",
        "-a",
        "-t", SELINUX_RW_LABEL,
        pattern,
    ]
    utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_SET_SELINUX_NFS_SHARE)

    cmd = [
        basedefs.EXEC_RESTORECON, "-r", path
    ]
    utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_REFRESH_SELINUX_CONTEXT)

DEFAULT_MD = {
        "CLASS" : "Iso",
        "DESCRIPTION" : "isofun",
        "IOOPTIMEOUTSEC" : "1",
        "LEASERETRIES" : "3",
        "LEASETIMESEC" : "5",
        "LOCKPOLICY" : "",
        "LOCKRENEWALINTERVALSEC" : "5",
        "POOL_UUID" : "",
        "REMOTE_PATH" : "no.one.reads.this:/rhev",
        "ROLE" : "Regular",
        "SDUUID" : "",
        "TYPE" : "NFS",
        "VERSION" : "0"}

# This is modified from persitantDict
def writeMD(filePath, md):
    logging.debug("generating metadata")
    checksumCalculator = hashlib.sha1()
    lines = []
    keys = md.keys()
    keys.sort()
    for key in keys:
        value = md[key]
        line = "=".join([key, str(value).strip()])
        checksumCalculator.update(_preprocessLine(line))
        lines.append(line)

    computedChecksum = checksumCalculator.hexdigest()
    logging.debug("checksum of metadata is %s" % (computedChecksum))
    lines.append("=".join([SHA_CKSUM_TAG, computedChecksum]))

    logging.debug("writing metadata file (%s)" % (filePath))
    f = open(filePath, "w")
    f.writelines([l + "\n" for l in lines])
    f.flush()
    os.fsync(f.fileno())
    f.close()

#. Iso domain structure
#`-- 2325a2fa-4bf4-47c4-81e1-f60d80dbe968
#    |-- dom_md
#    |   |-- ids
#    |   |-- inbox
#    |   |-- leases
#    |   |-- metadata
#    |   `-- outbox
#    `-- images
#        `-- 11111111-1111-1111-1111-111111111111

#since the uuid package is not supported in python v2.4
#we had to use this implementation
def generateUUID():
    logging.debug("Generating unique uuid")
    generateUUID = str(uuid.uuid4())
    return generateUUID

def createISODomain(path, description, sdUUID):
    logging.debug("creating iso domain for %s. uuid: %s" % (path, sdUUID))
    basePath = os.path.join(path, sdUUID)
    os.mkdir(basePath)
    imagesDir = os.path.join(basePath, "images")
    os.mkdir(imagesDir)
    os.mkdir(os.path.join(imagesDir, "11111111-1111-1111-1111-111111111111"))

    domMdDir = os.path.join(basePath, "dom_md")
    os.mkdir(domMdDir)
    logging.debug("creating empty files")
    for fname in ("ids", "inbox", "leases", "outbox"):
        f = open(os.path.join(domMdDir, fname), "w")
        f.write("\0")
        f.flush()
        f.close()

    logging.debug("writing metadata")
    mdFilePath = os.path.join(domMdDir, "metadata")
    md = DEFAULT_MD.copy()
    md.update({"SDUUID" : sdUUID, "DESCRIPTION" : description})
    writeMD(mdFilePath, md)

    logging.debug("setting directories & files permissions to %s:%s" % (basedefs.CONST_VDSM_UID, basedefs.CONST_KVM_GID))
    os.chmod(path, 0755)
    for base, dirs, files  in os.walk(path):
        allFsObjects = [base]
        allFsObjects.extend([os.path.join(base, fname) for fname in files])
        for fname in allFsObjects:
            os.chown(fname, basedefs.CONST_VDSM_UID, basedefs.CONST_KVM_GID)

def refreshNfsExports():
    logging.debug("refreshing NFS exports")
    p = subprocess.Popen([basedefs.EXEC_EXPORTFS, "-a"], stderr=subprocess.PIPE)
    _, err = p.communicate()
    rc = p.returncode
    if rc != 0:
        raise RuntimeError("Could not refresh NFS exports (%d: %s)" % (rc, err))

