"""
contains all common and re-usable code for rhevm-setup and sub packages
"""
import grp
import pwd
import logging
import subprocess
import re
from StringIO import StringIO
import output_messages
import traceback
import os
import sys
import basedefs
import datetime
import libxml2
import types
import shutil
import time
import tempfile
import csv
import miniyum
import string
import random

# Create a special Exception class, so that it could be caught.
class RetryFailException(Exception):
    pass

"""
ENUM implementation for python (from the vdsm team)
usage:
    #define
    enum = Enum(Key1=1, Key2=2)

    #use
    type = enum.Key1
    print type => (prints 1)
    # reverse lookup
    print enum[2] => (prints Key2)
    # value lookup
    print enum.parse("Key1") => (prints 1)
"""
class Enum(object):
    """
    A nice class to handle Enums gracefullly.
    """
    def __init__(self, **pairs):
        #Generate reverse dict
        self._reverse = dict([(b, a) for a, b in pairs.iteritems()])



        #Generate attributes
        for key, value in pairs.iteritems():
            setattr(self, key, value)

    def __getitem__(self, index):
        return self._reverse[index]

    def __iter__(self):
        return self._reverse.itervalues()

    def parse(self, value):
        #If value is enum name convert to value
        if isinstance(value, str):
            if hasattr(self, value):
                return getattr(self, value)
            #If value is a number assume parsing meant converting the value to int
            #if you can think of a more generic way feel free to change
            if value.isdigit():
                value = int(value)

        #If not check if value is a value of the enum
        if value in self._reverse:
            return value

        #Enum doesn't know this value
        raise ValueError(output_messages.ERR_EXP_VALUE_ERR%(value))

class ConfigFileHandler:
    def __init__(self, filepath):
        self.filepath = filepath
    def open(self):
        pass
    def close(self):
        pass
    def editParams(self, paramsDict):
        pass
    def delParams(self, paramsDict):
        pass

class TextConfigFileHandler(ConfigFileHandler):
    def __init__(self, filepath, sep="=", comment="#"):
        ConfigFileHandler.__init__(self, filepath)
        self.data = []
        self.sep = sep
        self.comment = comment

    def open(self):
        fd = file(self.filepath)
        self.data = fd.readlines()
        fd.close()

    def close(self):
        fd = file(self.filepath, 'w')
        for line in self.data:
            fd.write(line)
        fd.close()

    def getParam(self, param):
        value = None
        for line in self.data:
            found = re.match("\s*%s\s*\%s\s*(.+)$" % (param, self.sep), line)
            if found:
                value = found.group(1)
        return value

    def renameParam(self, current_param_name, new_param_name):
        if current_param_name == new_param_name:
            return
        changed = False
        for i, line in enumerate(self.data[:]):
            # If the line begins with comment, skip it
            if line.startswith(self.comment):
                continue

            # Otherwise, check the param. Create param pattern first, to be
            # like "<spaces>PARAM_NAME<spaces>SEPARATOR"
            pattern = "\s*%s\s*%s"
            # If the new_param already present, break the loop
            if re.match(pattern % (new_param_name, self.sep), line):
                changed = True
                break
            # Else, if the current_param found, rename it and break the loop
            if re.match(pattern % (current_param_name, self.sep), line):
                self.data[i] = line.replace(current_param_name, new_param_name)
                changed = True
                break

        # If new_param nor old_param were found, add it at the end of the file
        if not changed:
            self.data.append("%s%s\n"%(new_param_name, self.sep))

    def editParam(self, param, value, uncomment=False):
        change_index = -1

        # Find the index of the line containing the parameter that
        # we want to modify:
        for i, line in enumerate(self.data[:]):
            if re.match(
                "\s*%s\s*%s" % (
                    param,
                    self.sep
                ),
                line
            ):
                change_index = i
                break

        # If we can't find a line containing the parameter then try to find
        # a commented line containing it (we will later replace the commented
        # line with the new value):
        if change_index == -1 and uncomment:
            for i, line in enumerate(self.data[:]):
                if re.match(
                    "\s*%s\s*%s\s*%s" % (
                        self.comment,
                        param,
                        self.sep
                    ),
                    line
                ):
                    change_index = i
                    break

        # If the old parameter was found replace it with the new
        # value, otherwise append a new line with the new value:
        new = "%s%s%s\n" % (param, self.sep, value)
        if change_index == -1:
            self.data.append(new)
        else:
            self.data[i] = new

    def editLine(self, regexp, newLine, failOnError=False, errMsg=output_messages.ERR_FAILURE):
        changed = False
        for i, line in enumerate(self.data[:]):
            if not re.match("\s*#", line):
                if re.match(regexp, line):
                    self.data[i] = newLine
                    changed = True
                    break
        if not changed:
            if failOnError:
                raise Exception(errMsg)
            else:
                logging.warn(errMsg)

    def delParams(self, paramsDict):
        '''
        This function will comment out the params provided by paramsDict
        '''

        # Find the index of the line containing the parameter that
        # we want to modify:
        for param in paramsDict:
            if self.getParam(param):
                self.renameParam(param, self.comment + param)

class XMLConfigFileHandler(ConfigFileHandler):
    def __init__(self, filepath):
        ConfigFileHandler.__init__(self, filepath)
        self.content = []

    def open(self):
        with open(self.filepath, 'r') as f:
            self.content = f.readlines()

        libxml2.keepBlanksDefault(0)
        self.doc = libxml2.parseFile(self.filepath)
        self.ctxt = self.doc.xpathNewContext()

    def close(self):
        self.doc.saveFormatFile(self.filepath,1)
        self.doc.freeDoc()
        self.ctxt.xpathFreeContext()

    def xpathEval(self, xpath):
        return self.ctxt.xpathEval(xpath)

    def registerNs(self, nsPrefix, uri):
        return self.ctxt.xpathRegisterNs(nsPrefix, uri)

    def getNs(self, ns):
        for line in self.content:
            # Match line includes xmlns=NS:X:X
            match = re.match("(.*)xmlns=\"(%s:\d\.\d*)(.*)" % (ns), line)
            if match:
                return match.group(2)

        raise Exception(output_messages.ERR_EXP_UPD_XML_FILE % self.filepath)

    def editParams(self, paramsDict):
        editAllOkFlag = True
        if type(paramsDict) != types.DictType:
            raise Exception(output_messages.ERR_EXP_ILLG_PARAM_TYPE)
        for key in paramsDict.iterkeys():
            editOkFlag = False
            nodeList = self.ctxt.xpathEval(key)
            if len(nodeList) == 1:
                nodeList[0].setContent(paramsDict[key])
                editOkFlag = True
            elif len(nodeList) == 0:
                parentNode = os.path.dirname(key)
                parentNodeList = self.ctxt.xpathEval(parentNode)
                if len(parentNodeList) == 1:
                    newNode = libxml2.newNode(os.path.basename(key))
                    newNode.setContent(paramsDict[key])
                    parentNodeList[0].addChild(newNode)
                    editOkFlag = True
            if not editOkFlag:
                logging.error("Failed editing %s" %(key))
                editAllOkFlag = False
        if not editAllOkFlag:
            return -1

    def delParams(self, paramsDict):
        pass

    def removeNodes(self, xpath):
        nodes = self.xpathEval(xpath)

       #delete the node
        for node in nodes:
            node.unlinkNode()
            node.freeNode()

    def addNodes(self, xpath, xml):
        """
        Add a given xml into a specific point specified by the given xpath path into the xml object
        xml can be either a libxml2 instance or a string which contains a valid xml
        """
        parentNode = self.xpathEval(xpath)[0]
        if not parentNode:
            raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(parentNode)))

        if isinstance(xml, str):
            newNode = libxml2.parseDoc(xml)
        elif isinstance(xml, libxml2.xmlDoc):
            newNode = xml
        else:
            raise Exception(output_messages.ERR_EXP_UNKN_XML_OBJ)

        # Call xpathEval to strip the metadata string from the top of the new xml node
        parentNode.addChild(newNode.xpathEval('/*')[0])


class MiniYumSink(miniyum.MiniYumSinkBase):
    """miniyum user interaction sink.

    We want logging to go into our own internal log.

    But we do want user to approve new gnupg keys.

    """

    def _currentfds(self):
        fds = []
        try:
            fds.append(os.dup(sys.stdin.fileno()))
            fds.append(os.dup(sys.stdout.fileno()))
            fds.append(os.dup(sys.stderr.fileno()))

            return fds
        except:
            for fd in fds:
                os.close(fd)
            raise

    def __init__(self):
        super(MiniYumSink, self).__init__()
        self._fds = None
        self._fds = self._currentfds()

    def __del__(self):
        if self._fds != None:
            for fd in self._fds:
                os.close(fd)

    def verbose(self, msg):
        super(MiniYumSink, self).verbose(msg)
        logging.debug("YUM: VERB: %s" % msg)

    def info(self, msg):
        super(MiniYumSink, self).info(msg)
        logging.info("YUM: OK:   %s" % msg)

    def error(self, msg):
        super(MiniYumSink, self).error(msg)
        logging.error("YUM: FAIL: %s" % msg)

    def keepAlive(self, msg):
        super(MiniYumSink, self).keepAlive(msg)

    def askForGPGKeyImport(self, userid, hexkeyid):
        logging.warning("YUM: APPROVE-GPG: userid=%s, hexkeyid=%s" % (
            userid,
            hexkeyid
        ))
        save = self._currentfds()
        for i in range(3):
            os.dup2(self._fds[i], i)
        print output_messages.WARN_INSTALL_GPG_KEY % (userid, hexkeyid)
        ret = askYesNo(output_messages.INFO_PROCEED)
        for i in range(3):
            os.dup2(save[i], i)
        return ret

def getXmlNode(xml, xpath):
    nodes = xml.xpathEval(xpath)
    if len(nodes) != 1:
        raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(nodes)))
    return nodes[0]

def setXmlContent(xml, xpath,content):
    node = xml.xpathEval(xpath)
    if len(node) == 0:
        parentNode = xml.xpathEval(os.path.dirname(xpath))
        if len(parentNode) == 1:
            parentNode[0].newChild(None, os.path.basename(xpath), content)
        else:
            raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(parentNode)))
    elif len(xml.xpathEval(xpath)) == 1:
        node = getXmlNode(xml, xpath)
        node.setContent(content)
    else:
        raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(node)))

def getColoredText (text, color):
    ''' gets text string and color
        and returns a colored text.
        the color values are RED/BLUE/GREEN/YELLOW
        everytime we color a text, we need to disable
        the color at the end of it, for that
        we use the NO_COLOR chars.
    '''
    return color + text + basedefs.NO_COLOR

def isTcpPortOpen(port):
    """
    checks using lsof that a given tcp port is not open
    and being listened upon.
    if port is open, returns the process name & pid that use it
    """
    answer = False
    process = False
    pid = False
    logging.debug("Checking if TCP port %s is open by any process" % port)
    cmd = [
        basedefs.EXEC_LSOF,
        "-i", "-n", "-P",
    ]
    output, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_LSOF)
	#regex catches:
	#java      17564    jboss   90u  IPv4 1251444      0t0  TCP *:3873 (LISTEN)
    pattern=re.compile("^(\w+)\s+(\d+)\s+.+TCP\s\*\:(%s)\s\(LISTEN\)$" % (port))
    list = output.split("\n")
    for line in list:
        result = re.match(pattern, line)
        if result:
            process = result.group(1)
            pid = result.group(2)
            answer = True
            logging.debug("TCP port %s is open by process %s, PID %s" % (port, process, pid))
    return (answer, process, pid)

def getPgPassEnv():
    # .pgpass definition
    if os.path.exists(basedefs.DB_PASS_FILE):
        return { "PGPASSFILE" : basedefs.DB_PASS_FILE }
    else:
        raise Exception(output_messages.ERR_PGPASS)

def execCmd(cmdList, cwd=None, failOnError=False, msg=output_messages.ERR_RC_CODE, maskList=[], useShell=False, usePipeFiles=False, envDict=None):
    """
    Run external shell command with 'shell=false'
    receives a list of arguments for command line execution
    """
    # All items in the list needs to be strings, otherwise the subprocess will fail
    cmd = [str(item) for item in cmdList]

    # We need to join cmd list into one string so we can look for passwords in it and mask them
    logCmd = _maskString((' '.join(cmd)), maskList)

    logging.debug("Executing command --> '%s' in working directory '%s'" % (logCmd, cwd or os.getcwd()))

    stdErrFD = subprocess.PIPE
    stdOutFD = subprocess.PIPE
    stdInFD = subprocess.PIPE

    if usePipeFiles:
        (stdErrFD, stdErrFile) = tempfile.mkstemp(dir="/tmp")
        (stdOutFD, stdOutFile) = tempfile.mkstemp(dir="/tmp")
        (stdInFD, stdInFile) = tempfile.mkstemp(dir="/tmp")

    # Copy os.environ and update with envDict if provided
    env = os.environ.copy()
    env.update(envDict or {})

    # We use close_fds to close any file descriptors we have so it won't be copied to forked childs
    proc = subprocess.Popen(
        cmd,
        stdout=stdOutFD,
        stderr=stdErrFD,
        stdin=stdInFD,
        cwd=cwd,
        shell=useShell,
        close_fds=True,
        env=env,
    )

    out, err = proc.communicate()
    if usePipeFiles:
        with open(stdErrFile, 'r') as f:
            err = f.read()
        os.remove(stdErrFile)

        with open(stdOutFile, 'r') as f:
            out = f.read()
        os.remove(stdOutFile)
        os.remove(stdInFile)

    logging.debug("output = %s"%(out))
    logging.debug("stderr = %s"%(err))
    logging.debug("retcode = %s"%(proc.returncode))
    output = out + err
    if failOnError and proc.returncode != 0:
        raise Exception(msg)
    return ("".join(output.splitlines(True)), proc.returncode)

def execRemoteSqlCommand(userName, dbHost, dbPort, dbName, sqlQuery, failOnError=False, errMsg=output_messages.ERR_SQL_CODE):
    logging.debug("running sql query '%s' on db server: \'%s\'." % (sqlQuery, dbHost))
    cmd = [
        basedefs.EXEC_PSQL,
        "-h", dbHost,
        "-p", dbPort,
        "-U", userName,
        "-d", dbName,
        "-c", sqlQuery,
    ]
    return execCmd(cmdList=cmd, failOnError=failOnError, msg=errMsg, envDict=getPgPassEnv())

def parseRemoteSqlCommand(userName, dbHost, dbPort, dbName, sqlQuery, failOnError=False, errMsg=output_messages.ERR_SQL_CODE):
    ret = []
    sqlQuery = "copy (%s) to stdout with csv header;" % sqlQuery.replace(";", "")
    out, rc = execRemoteSqlCommand(
        userName,
        dbHost,
        dbPort,
        dbName,
        sqlQuery,
        failOnError,
        errMsg
    )
    if rc == 0:
        # we want reusable list, so load all into memory
        ret = [x for x in csv.DictReader(out.splitlines(True))]

    return ret, rc

def replaceWithLink(target, link):
    """
    replace link with a symbolic link to source
    if link does not exist, simply create the link
    """
    try:
        #TODO: export create symlink to utils and reuse in all rhevm-setup
        if os.path.exists(link):
            if os.path.islink(link):
                logging.debug("removing link %s" % link)
                os.unlink(link)
            elif os.path.isdir(link):
                #remove dir using shutil.rmtree
                logging.debug("removing directory %s" % link)
                shutil.rmtree(link)
            else:
                logging.debug("removing file %s" % link)
                os.remove(link)

        logging.debug("Linking %s to %s" % (target, link))
        os.symlink(target, link)

    except:
        logging.error(traceback.format_exc())
        raise

def getUsernameId(username):
    return pwd.getpwnam(username)[2]

def getGroupId(groupName):
    return grp.getgrnam(groupName)[2]

def findAndReplace(path, oldstr, newstr):
    regex = '(%s)'%(oldstr)
    p = re.compile(regex)
    try:
        # Read file content
        fd = file(path)
        fileText = fd.readlines()
        fd.close()

        # Change content
        fd = file(path, 'w')
        for line in fileText:
            line = p.sub(newstr, line)
            fd.write(line)
        fd.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FIND_AND_REPLACE%(path))

def byLength(word1, word2):
    """
    Compars two strings by their length
    Returns:
    Negative if word2 > word1
    Positive if word1 > word2
    Zero if word1 == word 2
    """
    return len(word1) - len(word2)

def nslookup(address):
    cmd = [
        basedefs.EXEC_NSLOOKUP, address,
    ]
    #since nslookup will return 0 no matter what, the RC is irrelevant
    output, rc = execCmd(cmdList=cmd)
    return output

def getConfiguredIps():
    try:
        iplist=set()
        cmd = [
            basedefs.EXEC_IP, "addr",
        ]
        output, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_GET_CFG_IPS_CODES)
        ipaddrPattern=re.compile('\s+inet (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).+')
        list=output.splitlines()
        for line in list:
            foundIp = ipaddrPattern.search(line)
            if foundIp:
                if foundIp.group(1) != "127.0.0.1":
                    ipAddr = foundIp.group(1)
                    logging.debug("Found IP Address: %s"%(ipAddr))
                    iplist.add(ipAddr)
        return iplist
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_GET_CFG_IPS)

def getCurrentDateTime(isUtc=None):
    now = None
    if (isUtc is not None):
        now = datetime.datetime.utcnow()
    else:
        now = datetime.datetime.now()
    return now.strftime("%Y_%m_%d_%H_%M_%S")

def getCurrentDateTimeHuman(isUtc=None):
    now = None
    if (isUtc is not None):
        now = datetime.datetime.utcnow()
    else:
        now = datetime.datetime.now()
    return now.strftime("%b %d %H:%M:%S")

def verifyStringFormat(str, matchRegex):
    '''
    Verify that the string given matches the matchRegex.
    for example:
    string: 111-222
    matchRegex: \d{3}-\d{3}
    this will return true since the string matches the regex
    '''
    pattern = re.compile(matchRegex)
    result = re.match(pattern, str)
    if result == None:
        return False
    else:
        return True


def find_mount_point(path):
    path = os.path.abspath(path)
    while not os.path.ismount(path):
        path = os.path.dirname(path)
    return path


def getAvailableSpace(path):
    logging.debug("Checking available space on %s" % (path))
    stat = os.statvfs(path)
    #block size * available blocks = available space in bytes, we devide by
    #1024 ^ 2 in order to get the size in megabytes
    availableSpace = (stat.f_bsize * stat.f_bavail) / pow(1024, 2)
    logging.debug("Available space on %s is %s" % (path, availableSpace))
    return int(availableSpace)

def transformUnits(size):
    """ Transform the number of size param (received in MB)
    into an appropriate units string (MB/GB)"""

    if size > 1024:
        return "%.02f" % (float(size) / 1024.0) + " Gb"
    else:
        return str(size) + " Mb"

def compareStrIgnoreCase(str1, str2):
    ''' compare 2 strings and ignore case
        if one of the input is not str (bool for e.g) - return normal comapre
    '''
    if type(str1) == types.StringType and type(str2) == types.StringType:
        return str1.lower() == str2.lower()
    else:
        return str1 == str2

def parseStrRegex(string, regex, errMsg):
    """
    Gets a text string and a regex pattern
    and returns the extracted sub-string
    captured.
    """
    rePattern = re.compile(regex)
    found = rePattern.search(string)
    if found:
        match = found.group(1)
        logging.debug("found new parsed string: %s"%(match))
        return match
    else:
        raise Exception(errMsg)

def copyFile(filename, destination, uid=-1, gid=-1, filemod=-1):
    """
    copy filename to
    the destDir path
    give the target file uid:gid ownership
    and file mod

    filename     - full path to src file (not directories!)
    destination  - full path to target dir or filename
    uid          - integer with user id (default -1 leaves the original uid)
    gid          - integer with group id (default -1 leaves the original gid)
    filemod      - integer with file mode (default -1 keeps original mode)
    """
    # If the source is a directory, throw an exception since this func handles only files
    if (os.path.isdir(filename)):
        raise Exception(output_messages.ERR_SOURCE_DIR_NOT_SUPPORTED)

    # In case the src file is a symbolic link, we'll get the origin filename
    fileSrc = os.path.realpath(filename)

    # In default, assume the destination is a file
    targetFile = destination

    # Copy file to destination
    shutil.copy2(fileSrc, destination)
    logging.debug("successfully copied file %s to target destination %s"%(fileSrc, destination))

    # Get the file basename, if the destination is a directory
    if (os.path.isdir(destination)):
        fileBasename = os.path.basename(fileSrc)
        targetFile = os.path.join(destination, fileBasename)

    # Set file mode, uid and gid to the file
    logging.debug("setting file %s uid/gid ownership"%(targetFile))
    os.chown(targetFile, uid, gid)

    logging.debug("setting file %s mode to %d"%(targetFile, filemod))
    os.chmod(targetFile, filemod)

def getDbAdminUser():
    """
    Retrieve Admin user from .pgpass file on the system.
    Use default settings if file is not found.
    """
    admin_user = getDbConfig("admin")
    if admin_user:
        return admin_user
    return basedefs.DB_ADMIN

def getDbUser():
    """
    Retrieve Admin user from .pgpass file on the system.
    Use default settings if file is not found.
    """
    db_user = getDbConfig("user")
    if db_user:
        return db_user
    return basedefs.DB_USER

def getDbHostName():
    """
    Retrieve DB Host name from .pgpass file on the system.
    Use default settings if file is not found, or '*' was used.
    """

    host = getDbConfig("host")
    if host and host != "*":
        return host
    return basedefs.DB_HOST

def getDbPort():
    """
    Retrieve DB port number from .pgpass file on the system.
    Use default settings if file is not found, or '*' was used.
    """
    port = getDbConfig("port")
    if port:
        return port
    return basedefs.DB_PORT

def getDbPassword(user):
    password = getDbConfig("password", user)
    if password:
        return password.rstrip("\n")

    return False

def getDbConfig(param, user=None):
    """
    Generic function to retrieve values from admin line in .pgpass
    """
    # 'user' and 'admin' are the same fields, just different lines
    # and for different cases
    field = {'password' : 4, 'user' : 3, 'admin' : 3, 'host' : 0, 'port' : 1}
    if param not in field.keys():
        return False

    inDbAdminSection = False
    inDbUserSection = False
    if os.path.exists(basedefs.DB_PASS_FILE):
        logging.debug("found existing pgpass file %s, fetching DB %s value", basedefs.DB_PASS_FILE, param)
        with open(basedefs.DB_PASS_FILE, 'r') as pgPassFile:
            for line in pgPassFile:

                # find the line with "DB ADMIN"
                if basedefs.PGPASS_FILE_ADMIN_LINE in line:
                    inDbAdminSection = True
                    continue

                if inDbAdminSection and param == "admin" and \
                    not line.startswith("#"):
                        # Means we're on DB ADMIN line, as it's for all DBs
                        dbcreds = line.split(":", 4)
                        return dbcreds[field[param]]

                # Fetch the password if needed
                if param == "password" \
                    and user \
                    and not line.startswith("#"):
                        dbcreds = line.split(":", 4)
                        if dbcreds[3] == user:
                            return dbcreds[field[param]]

                # find the line with "DB USER"
                if basedefs.PGPASS_FILE_USER_LINE in line:
                    inDbUserSection = True
                    continue

                # fetch the values
                if inDbUserSection:
                    # Means we're on DB USER line, as it's for all DBs
                    dbcreds = line.split(":", 4)
                    return dbcreds[field[param]]

    return False


def backupDB(db, backup_file, env, user, host="localhost", port="5432"):
    """
    Backup postgres db
    using backup.sh
    Args:  file - a target file to backup to
           db - db name to backup
           user - db user to use for backup
           host - db host where postgresql server runs
           port - db connection port

    backup.sh -u user -f backup_file -d db -s host -p port
    """
    logging.debug("%s DB Backup started", db)

    # Run backup
    cmd = [
        os.path.join(".", basedefs.FILE_DB_BACKUP_SCRIPT),
        "-u", user,
        "-s", host,
        "-p", port,
        "-d", db,
        "-f", backup_file,
    ]
    execCmd(cmdList=cmd, cwd=basedefs.DIR_DB_SCRIPTS, failOnError=True, msg=output_messages.ERR_DB_BACKUP, envDict=env)
    logging.debug("%s DB Backup completed successfully", db)


def restoreDB(user, host, port, backupFile):
    """
    Restore postgres db
    using pgrestore
    Args:  file - a db backup file to restore from
           user - db user to use for backup
           host - db host where postgresql server runs
           port - db connection port
    """

    # Restore
    logging.debug("DB Restore started")
    cmd = [
        basedefs.EXEC_PSQL,
        "-h", host,
        "-p", port,
        "-U", user,
        "-d", basedefs.DB_POSTGRES,
        "-f", backupFile,
    ]

    output, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_DB_RESTORE, envDict=getPgPassEnv())
    logging.debug("DB Restore completed successfully")

def renameDB(oldname, newname):
    """docstring for renameDb"""

    if oldname == newname:
        return

    logging.info("Renaming '%s' to '%s'..." % (oldname, newname))
    sqlQuery="ALTER DATABASE %s RENAME TO %s" % (oldname, newname)
    execRemoteSqlCommand(getDbUser(), getDbHostName(), getDbPort(),
                               basedefs.DB_POSTGRES, sqlQuery, True,
                               output_messages.ERR_DB_RENAME % (oldname, newname))

def getVDCOption(key, engineConfigBin=basedefs.FILE_ENGINE_CONFIG_BIN):
    """
    Query vdc_option in db and return its value.
    """

    cmd = [
        engineConfigBin,
        "-g", key,
    ]

    # Get the value and raise and Exception on failure
    out, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_FAILED_GET_VDC_OPTIONS % key)
    if out and 'version' in out:
        # The value is the second field in the output
        return out.split()[1]
    else:
        return None

def updateVDCOption(key, value, maskList=[], keyType='text', engineConfigBin=basedefs.FILE_ENGINE_CONFIG_BIN, engineConfigExtended=basedefs.FILE_ENGINE_EXTENDED_CONF):
    """
    Update vdc_option value in db
    using rhevm-config

    maskList is received to allow
    masking passwords in logging

    keyType can be 'text' or 'pass' for password
    """

    # Mask passwords
    logValue = _maskString(value, maskList)
    logging.debug("updating vdc option %s to: %s"%(key, logValue))
    msg = output_messages.ERR_EXP_UPD_VDC_OPTION%(key, logValue)

    # The first part of the command is really simple:
    cmd = [
        engineConfigBin,
    ]

    # For text options we just provide the name of the option and the value in
    # the command line, but for password options we have to put the password in
    # an external file and provide the name of that file:
    passFile = None
    if keyType == 'pass':
        passFile = mkTempPassFile(value)
        cmd.extend([
            '-s',
            key,
            '--admin-pass-file=%s' % passFile,
        ])
    else:
        cmd.extend([
            '-s',
            '%s=%s' % (key, value),
        ])

    # The rest of the arguments for engine-config are the same for all kind of
    # options:
    cmd.extend([
        '--cver=' + basedefs.VDC_OPTION_CVER,
        '-p',
        engineConfigExtended,
    ])

    # Execute the command, and always remember to remove the password file:
    try:
        output, rc = execCmd(cmdList=cmd, failOnError=True, msg=msg, maskList=maskList)
    finally:
        if passFile:
            os.remove(passFile)

def mkTempPassFile(value):
    t = tempfile.NamedTemporaryFile(delete=False)
    t.file.write(value)
    t.file.close()
    return t.name

def _maskString(string, maskList=[]):
    """
    private func to mask passwords
    in utils
    """
    maskedStr = string
    for maskItem in maskList:
        maskedStr = maskedStr.replace(maskItem, "*"*8)

    return maskedStr

def askYesNo(question=None):
    message = StringIO()
    askString = "%s? (yes|no): "%(question)
    logging.debug("asking user: %s"%askString)
    message.write(askString)
    message.seek(0)
    rawAnswer = raw_input(message.read())
    logging.debug("user answered: %s"%(rawAnswer))
    answer = rawAnswer.lower()
    if answer == "yes" or answer == "y":
        return True
    elif answer == "no" or answer == "n":
        return False
    else:
        return askYesNo(question)

def retry(func, tries=None, timeout=None, sleep=1):
    """
    Retry a function. Wraps the retry logic so you don't have to
    implement it each time you need it.

    :param func: The callable to run.
    :param tries: The number of time to try. None\0,-1 means infinite.
    :param timeout: The time you want to spend waiting. This **WILL NOT** stop the method.
                    It will just not run it if it ended after the timeout.
    :param sleep: Time to sleep between calls in seconds.
    """
    if tries in [0, None]:
        tries = -1

    if timeout in [0, None]:
        timeout = -1

    startTime = time.time()

    while True:
        tries -= 1
        try:
            return func()
        except RetryFailException as e:
            # That's the exception we do not want to ignore, so raise it
            raise e
        except Exception as e:

            if tries == 0:
                raise

            if (timeout > 0) and ((time.time() - startTime) > timeout):
                raise

            time.sleep(sleep)

def checkIfDbIsUp():
    """
    func to test is db is up

    will throw exception on error
    and not return a value
    """
    logging.debug("checking if db is already installed and running..")
    execRemoteSqlCommand(getDbUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, "select 1", True)

def localHost(hostname):
    # Create an ip set of possible IPs on the machine. Set has only unique values, so
    # there's no problem with union.
    # TODO: cache the list somehow? There's no poing quering the IP configuraion all the time.
    ipset = getConfiguredIps().union(set([ "localhost", "127.0.0.1"]))
    if hostname in ipset:
        return True
    return False

def clearDbConnections(dbName):
    """ Lock local DB and clear active connections """
    # Block new connections first
    logging.info("Closing DB '%s' for new connections" % dbName)
    query = "update pg_database set datallowconn = 'false' where datname = '%s';" % dbName
    cmd = [
        basedefs.EXEC_PSQL,
        "-U", getDbAdminUser(),
        "-c", query,
    ]
    execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_DB_CONNECTIONS_BLOCK, envDict=getPgPassEnv())

    # Disconnect active connections
    # First, check postgresql version, as this would work differently for 9.2
    logging.info("Checking PostgreSQL version")
    pid = "procpid"
    version_query = "SELECT version();"
    cmd = [
            basedefs.EXEC_PSQL,
            "-P", "tuples_only=on",
            "-P", "format=unaligned",
            "-h", getDbHostName(),
            "-p", getDbPort(),
            "-U", getDbAdminUser(),
            "-d", basedefs.DB_TEMPLATE,
            "-c", version_query,
        ]
    out, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_POSTGRESQL, envDict=getPgPassEnv())
    version = out.split()[1]
    if getVersionNumber(version) >= getVersionNumber("9.2"):
        logging.info("Detected PostgreSQL version >= 9.2")
        pid = "pid"

    logging.info("Disconnect active connections from DB '%s'" % dbName)
    query = "SELECT pg_terminate_backend(%s) FROM pg_stat_activity WHERE datname = '%s'" % (pid, dbName)
    cmd = [
        basedefs.EXEC_PSQL,
        "-U", getDbAdminUser(),
        "-c", query,
    ]
    execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_DB_CONNECTIONS_CLEAR, envDict=getPgPassEnv())

def listTempDbs():
    """ Create a list of temp DB's on the server with regex 'engine_*' """

    dbListRemove = [basedefs.DB_NAME]
    cmd = [
        basedefs.EXEC_PSQL,
        "-U", getDbAdminUser(),
        "-h", getDbHostName(),
        "-p", getDbPort(),
        "--list",
    ]
    output, rc = execCmd(cmdList=cmd, msg=output_messages.ERR_DB_TEMP_LIST, envDict=getPgPassEnv())
    if rc:
        logging.error(output_messages.ERR_DB_TEMP_LIST)
        raise Exception ("\n" + output_messages.ERR_DB_TEMP_LIST + "\n")

    # if there are temp DB that need to be removed, add them to DB list
    tempDbs = re.findall("^engine_\w*", output)
    if len(tempDbs) > 0:
        dbListRemove.extend(tempDbs)

    return dbListRemove

def getHostParams(webconf):
    """
    get hostname & secured port from /etc/ovirt-engine/web-conf.js
    """

    logging.debug("looking for configuration from %s", webconf)
    if not os.path.exists(webconf):
        raise Exception("Could not find %s" % webconf)

    handler = TextConfigFileHandler(webconf)
    handler.open()

    pattern = "\"(.+)\""
    values = {
                "fqdn" : handler.getParam("var host_fqdn"),
                "httpPort" : handler.getParam("var http_port"),
                "httpsPort" : handler.getParam("var https_port"),
             }

    for name, value in values.items():
        found = re.match(pattern, value)
        if found:
            values[name] = found.group(1)
            logging.debug("%s is: %s", name, value)
        else:
            logging.error("Could not find the %s value in %s", name, webconf)
            raise Exception(output_messages.ERR_EXP_PARSE_WEB_CONF % (name, webconf))

    return (values["fqdn"], values["httpPort"], values["httpsPort"])

def generateMacRange():
    ipSet = getConfiguredIps()
    if len(ipSet) > 0:
        ip = ipSet.pop()
        mac_parts = ip.split(".")[1:3]
        mac_base ="%s:%02X:%02X" %(basedefs.CONST_BASE_MAC_ADDR, int(mac_parts[0]), int(mac_parts[1]))
        return "%s:00-%s:FF"%(mac_base, mac_base)
    else:
        logging.error("Could not find a configured ip address, returning default MAC address range")
        return basedefs.CONST_DEFAULT_MAC_RANGE


def editEngineSysconfig(proxyEnabled, dbUrl, dbUser, fqdn, http, https, javaHome):
    # Load the file:
    logging.debug("Loading text file handler")
    handler = TextConfigFileHandler(basedefs.FILE_ENGINE_SYSCONFIG)
    handler.open()

    # Save the Java home:
    handler.editParam("JAVA_HOME", javaHome)

    handler.editParam("ENGINE_DB_DRIVER", "org.postgresql.Driver")
    handler.editParam("ENGINE_DB_URL", dbUrl)
    handler.editParam("ENGINE_DB_USER", dbUser)

    # Put FQDN for use by other components
    handler.editParam("ENGINE_FQDN", fqdn)

    # Save port numbers and enabled/disabled state:
    if proxyEnabled:
        handler.editParam("ENGINE_PROXY_ENABLED", "true")
        handler.editParam("ENGINE_PROXY_HTTP_PORT", http)
        handler.editParam("ENGINE_PROXY_HTTPS_PORT", https)
        handler.editParam("ENGINE_HTTP_ENABLED", "false")
        handler.editParam("ENGINE_HTTPS_ENABLED", "false")
        handler.editParam("ENGINE_AJP_ENABLED", "true")
        handler.editParam("ENGINE_AJP_PORT", basedefs.JBOSS_AJP_PORT)
    else:
        handler.editParam("ENGINE_PROXY_ENABLED", "false")
        handler.editParam("ENGINE_HTTP_ENABLED", "true")
        handler.editParam("ENGINE_HTTP_PORT", http)
        handler.editParam("ENGINE_HTTPS_ENABLED", "true")
        handler.editParam("ENGINE_HTTPS_PORT", https)
        handler.editParam("ENGINE_AJP_ENABLED", "false")

    # Save and close the file:
    logging.debug("Engine has been configured")
    handler.close()

def encryptEngineDBPass(password, maskList):
    """
    Encryptes the jboss postgres db password
    and store it in conf
    """
    #run encrypt tool on user give password
    if (os.path.exists(basedefs.EXEC_ENCRYPT_PASS)):
        cmd = [
            basedefs.EXEC_ENCRYPT_PASS, password,
        ]

        # The encrypt tool needs the jboss home env set
        # Since we cant use the bash way, we need to set it as environ
        os.environ["JBOSS_HOME"] = basedefs.DIR_ENGINE
        output, rc = execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_ENCRYPT_PASS, maskList=maskList)

        #parse the encrypted password from the tool
        return parseStrRegex(output, "Encoded password:\s*(.+)", output_messages.ERR_EXP_PARSING_ENCRYPT_PASS)
    else:
        raise Exception(output_messages.ERR_ENCRYPT_TOOL_NOT_FOUND)

def configEncryptedPass(password):
    """
    Push the encrypted password into the local configuration file.
    """
    logging.debug("Encrypting database password.")
    handler = TextConfigFileHandler(basedefs.FILE_ENGINE_SYSCONFIG)
    handler.open()
    handler.editParam("ENGINE_DB_USER", getDbUser())
    handler.editParam("ENGINE_DB_PASSWORD", password)
    handler.close()

# TODO: Support SystemD services
class Service():
    def __init__(self, name):
        self.wasStopped = False
        self.wasStarted = False
        self.name = name

    def isServiceAvailable(self):
        if os.path.exists("/etc/init.d/%s" % self.name):
            return True
        return False

    def start(self, raiseFailure = False):
        logging.debug("starting %s", self.name)
        (output, rc) = self._serviceFacility("start")
        if rc == 0:
            self.wasStarted = True
        elif raiseFailure:
            raise Exception(output_messages.ERR_FAILED_START_SERVICE % self.name)

        return (output, rc)

    def stop(self, raiseFailure = False):
        logging.debug("stopping %s", self.name)
        (output, rc) = self._serviceFacility("stop")
        if rc == 0:
            self.wasStopped = True
        elif raiseFailure:
                raise Exception(output_messages.ERR_FAILED_STOP_SERVICE % self.name)

        return (output, rc)

    def autoStart(self, start=True):
        mode = "on" if start else "off"
        cmd = [
            basedefs.EXEC_CHKCONFIG, self.name, mode,
        ]
        execCmd(cmdList=cmd, failOnError=True)

    def conditionalStart(self, raiseFailure = False):
        """
        Will only start if wasStopped is set to True
        """
        if self.wasStopped:
            logging.debug("Service %s was stopped. starting it again"%self.name)
            return self.start(raiseFailure)
        else:
            logging.debug("Service was not stopped. there for we're not starting it")
            return (False, False)

    def status(self):
        logging.debug("getting status for %s", self.name)
        (output, rc) = self._serviceFacility("status")
        return (output, rc)

    def _serviceFacility(self, action):
        """
        Execute the command "service NAME action"
        returns: output, rc
        """
        logging.debug("executing action %s on service %s", self.name, action)
        cmd = [
            basedefs.EXEC_SERVICE, self.name, action
        ]
        return execCmd(cmdList=cmd, usePipeFiles=True)

    def available(self):
        logging.debug("checking if %s service is available", self.name)

        # Checks if systemd service available
        cmd = [
            basedefs.EXEC_SYSTEMCTL,
            "show",
            "%s.service" % self.name
        ]
        if os.path.exists(basedefs.EXEC_SYSTEMCTL):
            out, rc = execCmd(cmdList=cmd)
            sysd = "LoadState=loaded" in out
        else:
            sysd = False

        # Checks if systemV service available
        sysv = os.path.exists("/etc/init.d/%s" % self.name)

        return (sysd or sysv)

def chown(target,uid, gid):
    logging.debug("chown %s to %s:%s" % (target, uid, gid))
    os.chown(target, uid, gid)

def chownToEngine(target):
    uid = getUsernameId(basedefs.ENGINE_USER_NAME)
    gid = getGroupId(basedefs.ENGINE_GROUP_NAME)
    chown(target, uid, gid)

def getRpmVersion(rpmName=basedefs.ENGINE_RPM_NAME):
    """
    extracts rpm version
    from a given rpm package name
    default rpm is 'rhevm'

    returns version (string)
    """
    # Update build number on welcome page
    logging.debug("retrieving version for %s rpm" % rpmName)

    # TODO
    # Do not create miniyum here, pass it via argument
    localminiyum = miniyum.MiniYum(sink=MiniYumSink())

    res = [
        p for p in localminiyum.queryPackages(patterns=[rpmName])
        if p['operation'] == 'installed'
    ]

    if len(res) != 1:
        raise Exception(output_messages.ERR_READ_RPM_VER % rpmName)

    return "%s-%s" % (res[0]['version'], res[0]['release'])

def getEngineVersion():
    # Just read the info from the file
    with open(basedefs.FILE_ENGINE_VERSION) as version:
        return version.readline().rstrip('\n')

def installed(rpm):
    try:
        getRpmVersion(rpmName=rpm)
        return True
    except:
        logging.debug("getRpmVersion failed", exc_info=True)
        return False

def lockRpmVersion(miniyum, patterns):
    """
    Enters rpm versions into yum version-lock
    """
    logging.debug("Locking rpms in yum-version-lock")

    pkgs = [
        p['display_name'] for p in
        miniyum.queryPackages(
            patterns=patterns
        )
        if p['operation'] == 'installed'
    ]

    # Create RPM lock list
    with open(basedefs.FILE_YUM_VERSION_LOCK, 'a') as yumlock:
        yumlock.write("\n".join(pkgs) + "\n")

def setHttpPortsToNonProxyDefault(controller):
    logging.debug("Changing HTTP_PORT & HTTPS_PORT to the default non-proxy values (8700 & 8701)")
    httpParam = controller.getParamByName("HTTP_PORT")
    httpParam.setKey("DEFAULT_VALUE", basedefs.JBOSS_HTTP_PORT)
    httpParam = controller.getParamByName("HTTPS_PORT")
    httpParam.setKey("DEFAULT_VALUE", basedefs.JBOSS_HTTPS_PORT)

def checkJavaVersion(version):
    # Check that the version is supported:
    if not version.startswith(basedefs.JAVA_VERSION):
        logging.debug("Java version \"%s\" is not supported, it should start with \"%s\"." % (version, basedefs.JAVA_VERSION))
        return False

    # If we are here it is an acceptable java version:
    return True

def checkJvm(jvmPath):
    # Check that it contains the Java launcher:
    javaLauncher = os.path.join(jvmPath, "bin", "java")
    if not os.path.exists(javaLauncher):
        logging.debug("JVM path \"%s\" doesn't contain the Java launcher." % jvmPath)
        return False

    # Check that Java launcher is executable:
    if not os.access(javaLauncher, os.X_OK):
        logging.debug("The Java launcher \"%s\" isn't executable." % javaLauncher)
        return False

    # Invoke the Java launcher to check what is the version number:
    javaCmd = [
        javaLauncher,
        "-version",
    ]
    javaOut, javaExit = execCmd(cmdList=javaCmd, failOnError=True, msg=output_messages.ERR_RC_CODE)

    # Extract version number:
    match = re.search(r'^java version "([^"]*)"$', str(javaOut), re.MULTILINE)
    if not match:
        logging.debug("The Java launcher \"%s\" doesn't provide the version number." % javaLauncher)
        return False
    javaVersion = match.group(1)

    # Check that the version is supported:
    if not checkJavaVersion(javaVersion):
        logging.debug("The java version \"%s\" is not supported." % javaVersion)
        return False

    # Check that it is an OpenJDK:
    match = re.search(r'^OpenJDK .*$', str(javaOut), re.MULTILINE)
    if not match:
        logging.debug("The Java launcher \"%s\" is not OpenJDK." % javaLauncher)
        return False

    # It passed all the checks, so it is valid JVM:
    return True

def checkJdk(jvmPath):
    # We assume that this JVM path has already been checked and that it
    # contains a valid JVM, so we only need to check that it also
    # contains a Java compiler:
    javaCompiler = os.path.join(jvmPath, "bin", "javac")
    if not os.path.exists(javaCompiler):
        logging.debug("JVM path \"%s\" doesn't contain the Java compiler." % jvmPath)
        return False

    # It passed all the checks, so it is a JDK:
    return True

def findJavaHome():
    # Find links in the search directories that point to real things,
    # not to other symlinks (this is to avoid links that point to things
    # that can be changed by the user, specially "alternatives" managed
    # links):
    jvmLinks = []
    for javaDir in basedefs.JAVA_DIRS:
        for fileName in os.listdir(javaDir):
            filePath = os.path.join(javaDir, fileName)
            if os.path.islink(filePath):
                targetName = os.readlink(filePath)
                if targetName.startswith("/"):
                    targetPath = targetName
                else:
                    targetPath = os.path.join(javaDir, targetName)
                if not os.path.islink(targetPath):
                    jvmLinks.append(filePath)

    # For each possible JVM path check that it really contain a JVM and
    # that the version is supported:
    jvmLinks = [x for x in jvmLinks if checkJvm(x)]

    # We prefer JRE over JDK, mainly because it is more stable, I mean,
    # a JRE will be always present if there is a JDK, but not the other
    # way around:
    jreLinks = [x for x in jvmLinks if not checkJdk(x)]
    if jreLinks:
        jvmLinks = jreLinks

    # Sort the list alphabetically (this is only to get a predictable
    # result):
    jvmLinks.sort()

    # Return the first link:
    javaHome = None
    if jvmLinks:
        javaHome = jvmLinks[0]

    # Return the result:
    return javaHome

def configureTasksTimeout(timeout,
                          engineConfigBin=basedefs.FILE_ENGINE_CONFIG_BIN,
                          engineConfigExtended=basedefs.FILE_ENGINE_EXTENDED_CONF):
    """
    Set AsyncTaskZombieTaskLifeInMinutes
    to a specified value
    """

    # First, get the originalTimeout value
    originalTimeout = getVDCOption("AsyncTaskZombieTaskLifeInMinutes", engineConfigBin)

    # Now, set the value to timeout, it raises an Exception if it fails
    updateVDCOption(key="AsyncTaskZombieTaskLifeInMinutes",
                    value=timeout,
                    keyType='text',
                    engineConfigBin=engineConfigBin,
                    engineConfigExtended=engineConfigExtended)

    # If everything went fine, return the original value
    return originalTimeout


def configureEngineForMaintenance():
    # Try to reconfigure the engine and raise an exception if it fails
    try:
        updateVDCOption(key="EngineMode", value="MAINTENANCE")
    except:
        logging.error(traceback.format_exc())
        restoreEngineFromMaintenance()
        raise


def restoreEngineFromMaintenance():
    try:
        # Try to restore the regular state.
        updateVDCOption(key="EngineMode", value="ACTIVE")
    except:
        logging.error(traceback.format_exc())
        raise

# Returns db size in MB
def getDbSize(dbname):
    sql = "SELECT pg_database_size(\'%s\')" % (dbname)
    out, rc = execRemoteSqlCommand(getDbAdminUser(),
                                    getDbHostName(),
                                    getDbPort(),
                                    basedefs.DB_POSTGRES,
                                    sql,
                                    True,
                                    output_messages.ERR_DB_GET_SPACE % (dbname))
    size = int(out.splitlines()[2].strip())
    size = size / 1024 / 1024 # Get size in MB
    return size


def checkAvailableSpace(required, dbName=None, dbFolder=None, msg=output_messages.MSG_ERROR_SPACE):

    mounts = {}
    engineDbSize = 0

    # Get DB Size
    if dbName:
        engineDbSize = getDbSize(dbName)

    # Loop over folders in the map of folders
    for folder, req_space in required.iteritems():
        # Find the mount point for the folder
        mount_point = find_mount_point(folder)
        logging.debug("Found mount point of '%s' at '%s'", folder, mount_point)
        # Find the free space on the folder/mount
        freeSpace = getAvailableSpace(folder)

        # Hack for the DB size, adding db size to the
        # required space value
        if folder == str(dbFolder):
            req_space += engineDbSize

        # IMPORTANT NOTICE
        # If the current mount point already holds
        # another folder, update its required space
        # by adding the new requirements to
        # the previously stored requirements.
        #
        # Otherwise, create a map between the mount_point
        # and its free space and required space.
        if mount_point in mounts.keys():
            mounts[mount_point]['required'] += req_space
        else:
            mounts[mount_point] = {'free': freeSpace,
                                   'required': req_space}

    logging.debug("Mount points are: %s", mounts)
    # Loop over each mount point and compare its free space
    # to the requirements.
    # If there is a folder with less space than required, issue
    # an error with the option to ignore the check.
    raise_space_error = False
    for mount, size in mounts.iteritems():
        logging.debug("Comparing free space %s MB with required %s MB",
                      size['free'],
                      size['required'])
        if size['free'] < size['required']:
            raise_space_error = True
            warn_msg = ("Warning: available disk space at %s (%s MB)"
                   " is lower than the minimum requirement"
                   " for the upgrade (%s MB)" % (mount,
                                                 size['free'],
                                                 size['required']))

            print warn_msg

    if raise_space_error:
        logging.debug(msg)
        print msg
        raise Exception(msg)

def generatePassword(length):
    chars = string.ascii_letters + string.digits + '!@#$%^&()'
    randomizer = random.SystemRandom()
    return ''.join(randomizer.choice(chars) for char in xrange(length))

def getVersionNumber(version_string):
    def _prefixNum(s):
        return int(re.match("\d*", s).group(0))
    version_num = version_string.split('.')
    return 1000*_prefixNum(version_num[0]) + _prefixNum(version_num[1])


def isApplicationModeGluster(conf):
    if "APPLICATION_MODE" in conf and conf["APPLICATION_MODE"] == "gluster":
        return True

    return False
