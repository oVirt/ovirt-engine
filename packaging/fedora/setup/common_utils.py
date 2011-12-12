"""
contains all common and re-usable code for rhevm-setup and sub packages
"""
import grp
import pwd
import logging
import subprocess
import re
import output_messages
import traceback
import os
import basedefs
import datetime
import libxml2
import types
import shutil
import time

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
    def __init__(self, filepath):
        ConfigFileHandler.__init__(self, filepath)
        self.data = []

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
            if not re.match("\s*#", line):
                found = re.match("\s*%s\s*\=\s*(.+)$" % param, line)
                if found:
                    value = found.group(1)
        return value

    def editParam(self, param, value):
        changed = False
        for i, line in enumerate(self.data[:]):
            if not re.match("\s*#", line):
                if re.match("\s*%s"%(param), line):
                    self.data[i] = "%s=%s\n"%(param, value)
                    changed = True
                    break
        if not changed:
            self.data.append("%s=%s\n"%(param, value))

    def delParams(self, paramsDict):
        pass

class XMLConfigFileHandler(ConfigFileHandler):
    def __init__(self, filepath):
        ConfigFileHandler.__init__(self, filepath)

    def open(self):
        libxml2.keepBlanksDefault(0)
        self.doc = libxml2.parseFile(self.filepath)
        self.ctxt = self.doc.xpathNewContext()

    def close(self):
        self.doc.saveFormatFile(self.filepath,1)
        self.doc.freeDoc()
        self.ctxt.xpathFreeContext()

    def xpathEval(self, xpath):
        return self.ctxt.xpathEval(xpath)

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

def getXmlNode(xml, xpath):
    nodes = xml.xpathEval(xpath)
    if len(nodes) != 1:
        raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(nodes)))
    return nodes[0]

def removeXmlNode(xml, xpath):
    nodes = xml.xpathEval(xpath)
    if len(nodes) != 1:
        raise Exception(output_messages.ERR_EXP_UPD_XML_CONTENT%(xpath, len(nodes)))

    #delete the node
    nodes[0].unlinkNode()
    nodes[0].freeNode()

def setXmlContent(xml,xpath,content):
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

def isTcpPortOpen(port):
    """
    checks using lsof that a given tcp port is not open
    and being listened upon.
    if port is open, returns the process name & pid that use it
    """
    cmd = "%s -i -n -P" % (basedefs.EXEC_LSOF)
    answer = False
    process = False
    pid = False
    logging.debug("Checking if TCP port %s is open by any process" % port)
    output, rc = execExternalCmd(cmd, True, output_messages.ERR_EXP_LSOF)
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

def execExternalCmd(command, failOnError=False, msg=output_messages.ERR_RC_CODE, maskList=[]):
    """
    Run External os command
    Receives maskList to allow passwords masking
    """
    logString = _maskString(command, maskList)
    logging.debug("cmd = %s" % (logString))
    p = subprocess.Popen(command, shell=True,
        stdin=subprocess.PIPE, stdout=subprocess.PIPE,
        stderr=subprocess.PIPE, close_fds=True)
    out, err = p.communicate()
    logging.debug("output = %s"%(out))
    logging.debug("stderr = %s"%(err))
    logging.debug("retcode = %s"%(p.returncode))
    output = out + err
    if failOnError and p.returncode != 0:
        raise Exception(msg)
    return ("".join(output.splitlines(True)), p.returncode)

def execCmd(cmdList, cwd=None, failOnError=False, msg=output_messages.ERR_RC_CODE, maskList=[], useShell=False):
    """
    Run external shell command with 'shell=false'
    receives a list of arguments for command line execution
    """
    # All items in the list needs to be strings, otherwise the subprocess will fail
    cmd = [str(item) for item in cmdList]

    # We need to join cmd list into one string so we can look for passwords in it and mask them
    logCmd = _maskString((' '.join(cmd)), maskList)
    logging.debug("Executing command --> '%s'"%(logCmd))
    
    # We use close_fds to close any file descriptors we have so it won't be copied to forked childs
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, stdin=subprocess.PIPE, cwd=cwd, shell=useShell, close_fds=True)

    out, err = proc.communicate()
    logging.debug("output = %s"%(out))
    logging.debug("stderr = %s"%(err))
    logging.debug("retcode = %s"%(proc.returncode))
    output = out + err
    if failOnError and proc.returncode != 0:
        raise Exception(msg)
    return ("".join(output.splitlines(True)), proc.returncode)

def execSqlCommand(userName, dbName, sqlQuery, failOnError=False, errMsg=output_messages.ERR_SQL_CODE):
    logging.debug("running sql query %s on db: \'%s\'."%(dbName, sqlQuery))
    cmd = "/usr/bin/psql -U %s -d %s -c \"%s\""%(userName, dbName, sqlQuery)
    return execExternalCmd(cmd, failOnError, errMsg)

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
    cmd = "%s %s"%(basedefs.EXEC_NSLOOKUP, address)
    #since nslookup will return 0 no matter what, the RC is irrelevant
    output, rc = execExternalCmd(cmd)
    return output

def getConfiguredIps():
    try:
        iplist=set()
        cmd = "%s addr"%(basedefs.EXEC_IP)
        output, rc = execExternalCmd(cmd, True, output_messages.ERR_EXP_GET_CFG_IPS_CODES)
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

def getAvailableSpace(path):
    logging.debug("Checking available space on %s" % (path))
    stat = os.statvfs(path)
    #block size * available blocks = available space in bytes, we devide by
    #1024 ^ 2 in order to get the size in megabytes
    availableSpace = (stat.f_bsize * stat.f_bavail) / pow(1024, 2)
    logging.debug("Available space on %s is %s" % (path, availableSpace))
    return int(availableSpace)

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

def backupDB(db, user, backupFile):
    """
    Backup postgres db
    using pgdump
    Args:  file - a target file to backup to
           db - db name to backup
           user - db user to use for backup
    """
    logging.debug("%s DB Backup started"%(db))
    cmd = "%s -C -E UTF8 --column-inserts --disable-dollar-quoting  --disable-triggers -U %s --format=p -f %s %s"\
        %(basedefs.EXEC_PGDUMP, user, backupFile, db)
    output, rc = execExternalCmd(cmd, True, output_messages.ERR_DB_BACKUP)
    logging.debug("%s DB Backup completed successfully"%(db))

def restoreDB(db, user, backupFile):
    """
    Restore postgres db
    using pgrestore
    WARNING! - DROPS EXISTING DB
    Args:  file - a db backup file to restore from
           db - db name to backup
           user - db user to use for backup
    """
    # Drop
    #TODO: do we want to backup existing db before drop?
    logging.debug("dropping %s DB"%(db))
    cmd = "%s -U %s %s"%(basedefs.EXEC_DROPDB, user, db)
    output, rc = execExternalCmd(cmd, True, output_messages.ERR_DB_DROP)

    # Restore
    logging.debug("%s DB Restore started"%(db))
    cmd = "%s -U %s -f %s"%(basedefs.EXEC_PSQL, user, backupFile)
    output, rc = execExternalCmd(cmd, True, output_messages.ERR_DB_RESTORE)
    logging.debug("%s DB Restore completed successfully"%(db))

def updateVDCOption(key, value, maskList=[]):
    """
    Update vdc_option value in db
    using rhevm-config

    maskList is received to allow
    masking passwords in logging
    """
    # Running rhevm-config to update values
    cmd = [basedefs.FILE_RHEVM_CONFIG_BIN, '-s', key + '=' + value, '--cver=' + basedefs.VDC_OPTION_CVER, '-p', basedefs.FILE_RHEVM_EXTENDED_CONF]

    # Mask passwords
    logValue = _maskString(value, maskList)
    logging.debug("updating vdc option %s to: %s"%(key, logValue))
    msg = output_messages.ERR_EXP_UPD_VDC_OPTION%(key, logValue)
    output, rc = execCmd(cmd, None, True, msg, maskList)

def _maskString(string, maskList=[]):
    """
    private func to mask passwords
    in utils
    """
    maskedStr = string
    for maskItem in maskList:
        maskedStr = maskedStr.replace(maskItem, "*"*8)

    return maskedStr

def getRpmVersion(rpmName=basedefs.ENGINE_RPM_NAME):
    """
    extracts rpm version
    from a given rpm package name
    default rpm is 'rhevm'

    returns version (string)
    """
    # Update build number on welcome page
    logging.debug("retrieving build number for %s rpm"%(rpmName))
    cmd = "rpm -q --queryformat '%%{VERSION}-%%{RELEASE}' %s"%(rpmName)
    rpmVersion, rc = execExternalCmd(cmd, True, msg=output_messages.ERR_READ_RPM_VER%(rpmName))

    # Return rpm version
    return rpmVersion

def retry(func, expectedException=Exception, tries=None, timeout=None, sleep=1):
    """
    Retry a function. Wraps the retry logic so you don't have to
    implement it each time you need it.

    :param func: The callable to run.
    :param expectedException: The exception you expect to receive when the function fails.
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
        except expectedException:
            if tries == 0:
                raise

            if (timeout > 0) and ((time.time() - startTime) > timeout):
                raise

            time.sleep(sleep)

def checkIfRhevmDbIsUp():
    """
    func to test is rhevm is up
    
    will throw exception on error
    and not return a value
    """
    logging.debug("checking if rhevm db is already installed and running..")
    (out, rc) = execSqlCommand(basedefs.DB_ADMIN, basedefs.DB_NAME, "select 1", True)

class Service():
    def __init__(self, serviceName):
        self.wasStopped = False
        self.wasStarted = False
        self.serviceName = serviceName

    def isServiceAvailable(self):
        if os.path.exists("/etc/init.d/%s" % self.serviceName):
            return True
        return False

    def start(self, raiseFailure = False):
        logging.debug("starting %s", self.serviceName)
        (output, rc) = self._serviceFacility(self.serviceName, "start")
        if rc == 0:
            self.wasStarted = True
        elif raiseFailure:
            raise Exception(output_messages.ERR_FAILED_START_SERVICE % self.serviceName)

        return (output, rc)

    def stop(self, raiseFailure = False):
        logging.debug("stopping %s", self.serviceName)
        (output, rc) = self._serviceFacility(self.serviceName, "stop")
        if rc == 0:
            self.wasStopped = True
        elif raiseFailure:
                raise Exception(output_messages.ERR_FAILED_STOP_SERVICE % self.serviceName)

        return (output, rc)

    def conditionalStart(self, raiseFailure = False):
        """
        Will only start if wasStopped is set to True
        """
        if self.wasStopped:
            logging.debug("Service %s was stopped. starting it again"%self.serviceName)
            return self.start(raiseFailure)
        else:
            logging.debug("Service was not stopped. there for we're not starting it")
            return (False, False)

    def status(self):
        logging.debug("getting status for %s", self.serviceName)
        (output, rc) = self._serviceFacility(self.serviceName, "status")
        return (output, rc)

    def _serviceFacility(self, serviceName, action): 
        """
        Execute the command "service serviceName action"
        returns: output, rc
        """
        logging.debug("executing action %s on service %s", serviceName, action)
        cmd = [basedefs.EXEC_SERVICE, serviceName, action]
        return execCmd(cmd)
