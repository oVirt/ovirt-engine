"""
contains all available validation functions
"""
import common_utils as utils
import re
import logging
import output_messages
import basedefs
import types
import traceback
import os
import os.path
import tempfile
import cracklib
from setup_controller import Controller

def validateMountPoint(param, options=[]):
    logging.info("validating %s as a valid mount point" % (param))
    if not utils.verifyStringFormat(param, "^\/[\w\_\-\s]+(\/[\w\_\-\s]+)*\/?$"):
        print output_messages.INFO_VAL_PATH_NAME_INVALID
        return False
    if _isPathInExportFs(param):
        print output_messages.INFO_VAL_PATH_NAME_IN_EXPORTS
        return False
    if os.path.exists(param) and len(os.listdir(param)):
        print output_messages.INFO_VAR_PATH_NOT_EMPTY % param
        return False
    basePath = _getBasePath(param)
    if not _isPathWriteable(basePath):
        print output_messages.INFO_VAL_PATH_NOT_WRITEABLE
        return False
    availableSpace = utils.getAvailableSpace(basePath)
    if availableSpace < basedefs.CONST_MINIMUM_SPACE_ISODOMAIN:
        print output_messages.INFO_VAL_PATH_SPACE % (str(availableSpace), str(basedefs.CONST_MINIMUM_SPACE_ISODOMAIN))
        return False
    return True

def validateInteger(param, options=[]):
    try:
        int(param)
        return True
    except:
        logging.warn("validateInteger('%s') - failed" %(param))
        print output_messages.INFO_VAL_NOT_INTEGER
        return False

def validatePort(param, options=[]):
    #TODO: add actual port check with socket open
    logging.debug("Validating %s as a valid TCP Port" % (param))
    minVal = 0
    controller = Controller()
    isProxyEnabled = utils.compareStrIgnoreCase(controller.CONF["OVERRIDE_HTTPD_CONFIG"], "yes")
    if not isProxyEnabled:
        minVal = 1024
    if not validateInteger(param, options):
        return False
    port = int(param)
    if not (port > minVal and port < 65535) :
        logging.warn(output_messages.INFO_VAL_PORT_NOT_RANGE %(minVal))
        print output_messages.INFO_VAL_PORT_NOT_RANGE %(minVal)
        return False
    if isProxyEnabled and param in[basedefs.JBOSS_HTTP_PORT, basedefs.JBOSS_HTTPS_PORT, basedefs.JBOSS_AJP_PORT]:
        logging.warn(output_messages.INFO_VAL_PORT_OCCUPIED_BY_JBOSS %(param))
        print output_messages.INFO_VAL_PORT_OCCUPIED_BY_JBOSS %(param)
        return False
    (portOpen, process, pid) = utils.isTcpPortOpen(param)
    if portOpen:
        logging.warn(output_messages.INFO_VAL_PORT_OCCUPIED % (param, process, pid))
        print output_messages.INFO_VAL_PORT_OCCUPIED % (param, process, pid)
        return False
    if isProxyEnabled and not checkAndSetHttpdPortPolicy(param):
        logging.warn(output_messages.INFO_VAL_FAILED_ADD_PORT_TO_HTTP_POLICY %(port))
        print output_messages.INFO_VAL_FAILED_ADD_PORT_TO_HTTP_POLICY %(port)
        return False
    return True

def checkAndSetHttpdPortPolicy(port):
    def parsePorts(portsStr):
        ports = []
        for part in portsStr.split(","):
            part = part.strip().split("-")
            if len(part) > 1:
                for port in range(int(part[0]),int(part[1])):
                    ports.append(port)
            else:
                ports.append(int(part[0]))
        return ports

    newPort = int(port)
    out, rc = utils.execCmd([basedefs.EXEC_SEMANAGE, "port", "-l"]) #, "-t", "http_port_t"])
    if rc:
        return False
    httpPortsList = []
    pattern = re.compile("^http_port_t\s*tcp\s*([0-9, \-]*)$")
    for line in out.splitlines():
        httpPortPolicy = re.match(pattern, line)
        if httpPortPolicy:
            httpPortsList = parsePorts(httpPortPolicy.groups()[0])
    logging.debug("http_port_t = %s"%(httpPortsList))
    if newPort in httpPortsList:
        return True
    else:
        out, rc = utils.execCmd([basedefs.EXEC_SEMANAGE, "port", "-a", "-t", "http_port_t", "-p", "tcp", "%d"%(newPort)], None, False, "", [], False, True)
        if rc:
            logging.error(out)
            return False
    return True



def validateStringNotEmpty(param, options=[]):
    if type(param) != types.StringType or len(param) == 0:
        logging.warn("validateStringNotEmpty('%s') - failed" %(param))
        print output_messages.INFO_VAL_STRING_EMPTY
        return False
    else:
        return True

def validatePassword(param, options=[]):
    logging.debug("Validating password")
    if not validateStringNotEmpty(param, options):
        return False
    try:
        cracklib.FascistCheck(param)
    except:
        logging.warn("Password failed check")
        logging.warn(traceback.format_exc())
        print output_messages.WARN_WEAK_PASS

    return True

def validateOptions(param, options=[]):
    logging.info("Validating %s as part of %s"%(param, options))
    if not validateStringNotEmpty(param, options):
        return False
    if param.lower() in [option.lower() for option in options]:
        return True
    print output_messages.INFO_VAL_NOT_IN_OPTIONS % (", ".join(options))
    return False

def validateOverrideHttpdConfAndChangePortsAccordingly(param, options=[]):
    """
    This validation function is specific for the OVERRIDE_HTTPD_CONF param and it does more than validating the answer.
    It actually changes the default HTTP/S ports in case the user choose not to override the httpd configuration.
    """
    logging.info("validateOverrideHttpdConfAndChangePortsAccordingly %s as part of %s"%(param, options))
    retval = validateOptions(param, options)
    if retval and param.lower() == "no":
        logging.debug("Changing HTTP_PORT & HTTPS_PORT to the default jboss values (8080 & 8443)")
        controller = Controller()
        httpParam = controller.getParamByName("HTTP_PORT")
        httpParam.setKey("DEFAULT_VALUE", basedefs.JBOSS_HTTP_PORT)
        httpParam = controller.getParamByName("HTTPS_PORT")
        httpParam.setKey("DEFAULT_VALUE", basedefs.JBOSS_HTTPS_PORT)
    elif retval:
        #stopping httpd service (in case it's up) when the configuration can be overridden
        logging.debug("stopping httpd service")
        utils.Service(basedefs.HTTPD_SERVICE_NAME).stop()
    return retval


def validateDomain(param, options=[]):
    """
    Validate domain name
    """
    logging.info("validating %s as a valid domain string" % (param))
    (errMsg, rc) = _validateString(param, 1, 1024, "^[\w\-\_]+\.[\w\.\-\_]+\w+$")

    # Right now we print a generic error, might want to change it in the future
    if rc != 0:
        print output_messages.INFO_VAL_NOT_DOMAIN
        return False
    else:
        return True

def validateUser(param, options=[]):
    """
    Validate Auth Username
    Setting a logical max value of 256
    """
    logging.info("validating %s as a valid user name" % (param))
    (errMsg, rc) = _validateString(param, 1, 256, "^\w[\w\.\-\_\%\@]{2,}$")

    # Right now we print a generic error, might want to change it in the future
    if rc != 0:
        print output_messages.INFO_VAL_NOT_USER
        return False
    else:
        return True

def validateRemoteHost(param, options=[]):
    """ Validate that the we are working with remote DB host
    """
    # If we received localhost, use default flow.
    # If not local, REMOTE_DB group is run.
    # It means returning True if remote, and False if local

    if "DB_REMOTE_INSTALL" in param.keys() and param["DB_REMOTE_INSTALL"] == "remote":
        return True
    else:
        return False

def validateRemoteDB(param={}, options=[]):
    """ Ensure, that params provided for the remote DB are
     working, and if not, issue the correct error.
    """

    logging.info("Validating %s as a RemoteDb" % param["DB_HOST"])
    if utils.localHost(param["DB_HOST"]):
        logging.info("%s is a local host, no connection checks needed" % param["DB_HOST"])
        return True

    if "DB_ADMIN" not in param.keys():
        param["DB_ADMIN"] = basedefs.DB_ADMIN
        param["DB_PORT"] = basedefs.DB_PORT
        param["DB_PASS"] = param["DB_LOCAL_PASS"]
    else:
        param["DB_PASS"] = param["DB_REMOTE_PASS"]

    # Create a new pgpass, store previous in backupFile
    backupFile = _createTempPgPass(param["DB_ADMIN"], param["DB_HOST"],
                                   param["DB_PORT"], param["DB_PASS"])

    # Now, let's check credentials:
    try:
        # Connection check
        _checkDbConnection(param["DB_ADMIN"], param["DB_HOST"], param["DB_PORT"])

        # DB Create check
        _checkCreateDbPrivilege(param["DB_ADMIN"], param["DB_HOST"], param["DB_PORT"])

        # Delete DB check
        _checkDropDbPrivilege(param["DB_ADMIN"], param["DB_HOST"], param["DB_PORT"])

        # Everything is fine, return True
        return True

    except Exception,e:
        # Something failed, print the error on screen and return False
        print e
        return False

    finally:
        # restore the original pgpass file in all cases
        os.rename(backupFile, basedefs.DB_PASS_FILE)

def validateFQDN(param, options=[]):
    logging.info("Validating %s as a FQDN"%(param))
    if not validateDomain(param,options):
        return False
    try:
        #get set of IPs
        ipAddresses = utils.getConfiguredIps()
        if len(ipAddresses) < 1:
            logging.error("Could not find any configured IP address on the host")
            raise Exception(output_messages.ERR_EXP_CANT_FIND_IP)

        #resolve fqdn
        pattern = 'Address: (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})'
        resolvedAddresses = _getPatternFromNslookup(param, pattern)
        if len(resolvedAddresses) < 1:
            logging.error("Failed to resolve %s"%(param))
            print output_messages.ERR_DIDNT_RESOLVED_IP%(param)
            return False

        #string is generated here since we use it in all latter error messages
        prettyString = " ".join(["%s"%string for string in resolvedAddresses])

        #compare found IP with list of local IPs and match.
        if not resolvedAddresses.issubset(ipAddresses):
            logging.error("the following address(es): %s are not configured on this host"%(prettyString))
            #different grammar for plural and single
            if len(resolvedAddresses) > 1:
                print output_messages.ERR_IPS_NOT_CONFIGED%(prettyString, param)
            else:
                print output_messages.ERR_IPS_NOT_CONFIGED_ON_INT%(prettyString, param)
            return False

        #reverse resolved IP and compare with given fqdn
        counter = 0
        pattern = '[\w\.-]+\s+name\s\=\s([\w\.\-]+)\.'
        for address in resolvedAddresses:
            addressSet = _getPatternFromNslookup(address, pattern)
            reResolvedAddress = None
            if len(addressSet) > 0:
                reResolvedAddress = addressSet.pop()
            if reResolvedAddress == param:
                counter += 1
            else:
                logging.warn("%s did not reverse-resolve into %s"%(address,param))
        if counter < 1:
            logging.error("The following addresses: %s did not reverse resolve into %s"%(prettyString, param))
            #different grammar for plural and single
            if len(resolvedAddresses) > 1:
                print output_messages.ERR_IPS_HAS_NO_PTR%(prettyString, param)
            else:
                print output_messages.ERR_IP_HAS_NO_PTR%(prettyString, param)
            return False

        #conditions passed
        return True
    except Exception, (instance):
        logging.error(traceback.format_exc())
        raise Exception(instance)

def validateIsoDomainName(param, options=[]):
    """
    Validate ISO domain name against
    the required schema (allowed chars)
    and max allowed length
    """
    logging.info("validating iso domain name")
    (errMsg, rc) = _validateString(param, 1, basedefs.CONST_STORAGE_DOMAIN_NAME_SIZE_LIMIT, "^[a-zA-Z0-9_-]+$")
    if rc == 3:
        # We want to print a specific error
        print output_messages.INFO_VAL_ISO_DOMAIN_ILLEGAL_CHARS
        return False
    elif rc != 0:
        print errMsg
        return False
    else:
        return True

def validateOrgName(param, options=[]):
    """
    Organization name length must be limited
    otherwise CA creation fails
    """
    logging.info("validating organization name")

    # Filter out special chars: "," "%" "$" "@" "#", "&" "*" "!"
    (errMsg, rc) = _validateString(param, 1, basedefs.CONST_ORG_NAME_SIZE_LIMIT, "^[^,\+\%\$\@\#&\*\!]+$")

    if rc == 3:
        # We want to print a specific error
        print output_messages.INFO_VAL_ORG_NAME_ILLEGAL_CHARS
        return False
    elif rc != 0:
        print errMsg
        return False
    else:
        return True

def validatePing(param, options=[]):
    """
    Check that provided host answers to ping
    """
    if validateStringNotEmpty(param):
        out, rc = utils.execCmd(["/bin/ping", "-c 1", "%s" % param])
        if rc == 0:
            return True

    print "\n" + output_messages.ERR_PING + ".\n"
    return False

def _checkDbConnection(dbAdminUser, dbHost, dbPort):
    """ _checkDbConnection checks connection to the DB"""

    # Connection check
    logging.info("Trying to connect to the remote database with provided credentials.")
    out, rc = utils.execRemoteSqlCommand(dbAdminUser, dbHost, dbPort,
                                           basedefs.DB_POSTGRES, "select 1")

    # It error is in "SELECT 1" it means that we have a problem with simple DB connection.
    if rc:
        logging.error(output_messages.ERR_DB_CONNECTION % dbHost)
        raise Exception("\n" + output_messages.ERR_DB_CONNECTION % dbHost + "\n")
    else:
        logging.info("Successfully connected to the DB host %s." % dbHost)

def _checkCreateDbPrivilege(dbAdminUser, dbHost, dbPort):
    """ _checkCreateDbPrivilege checks CREATE DB privilege on DB server"""

    logging.info("Creating database 'ovirt_engine_test' on remote server.")
    out, rc = utils.execRemoteSqlCommand(dbAdminUser, dbHost, dbPort,
                                           basedefs.DB_POSTGRES, "CREATE DATABASE ovirt_engine_test")

    # Error in "CREATE DATABASE", meaning we don't have enough privileges to create database.
    if rc:
        logging.error(output_messages.ERR_DB_CREATE_FAILED % dbHost)
        raise Exception("\n" + output_messages.ERR_DB_CREATE_FAILED % dbHost + ".\n")
    else:
        logging.info("Successfully created temp database on server %s." % dbHost)

def _checkDropDbPrivilege(dbAdminUser, dbHost, dbPort):
    """ _checkCreateDbPrivilege checks CREATE DB privilege on DB server"""

    logging.info("Deleting the test database from the remote server")
    out, rc = utils.execRemoteSqlCommand(dbAdminUser, dbHost, dbPort,
                                           basedefs.DB_POSTGRES, "DROP DATABASE ovirt_engine_test")

    # Error in "DROP DATABASE", meaning we don't have enough privileges to drop database.
    if rc:
        logging.error(output_messages.ERR_DB_DROP_PRIV % dbHost)
        raise Exception("\n" + output_messages.ERR_DB_DROP_PRIV % dbHost + ".\n")
    else:
        logging.info("Successfully deleted database on server %s." % dbHost)

def _createTempPgPass(dbAdminUser, dbHost, dbPort, dbPass):
    """docstring for _createTempPgPass"""

    #backup existing .pgpass
    backupFile = "%s.%s" % (basedefs.DB_PASS_FILE, utils.getCurrentDateTime())
    try:
        if (os.path.exists(basedefs.DB_PASS_FILE)):
            logging.debug("found existing pgpass file, backing current to %s for validation" % (backupFile))
            os.rename(basedefs.DB_PASS_FILE, backupFile)

        with open(basedefs.DB_PASS_FILE, "w") as pgPassFile:
            pgPassFile.write("%s:%s:*:%s:%s" %
                            (dbHost, dbPort, dbAdminUser, dbPass))
        #make sure the file has still 0600 mod
        os.chmod(basedefs.DB_PASS_FILE, 0600)
        return backupFile
    except:
        # Restore original file
        os.rename(backupFile, basedefs.DB_PASS_FILE)
        raise Exception(output_messages.ERR_BACKUP_PGPASS % backupFile)

def _validateString(string, minLen, maxLen, regex=".*"):
    """
    Generic func to verify a string
    match its min/max length
    and doesn't contain illegal chars

    The func returns various return codes according to the error
    plus a default error message
    the calling func can decide if to use to default error msg
    or to use a more specific one according the RC.
    Return codes:
    1 - string length is less than min
    2 - string length is more tham max
    3 - string contain illegal chars
    0 - success
    """
    # String length is less than minimum allowed
    if len(string) < minLen:
        msg = output_messages.INFO_STRING_LEN_LESS_THAN_MIN % (minLen)
        return(msg, 1)
    # String length is more than max allowed
    elif len(string) > maxLen:
        msg = output_messages.INFO_STRING_EXCEEDS_MAX_LENGTH % (maxLen)
        return(msg, 2)
    # String contains illegal chars
    elif not utils.verifyStringFormat(string, regex):
        return(output_messages.INFO_STRING_CONTAINS_ILLEGAL_CHARS, 3)
    else:
        # Success
        return (None, 0)

def _getPatternFromNslookup(address, pattern):
    rePattern = re.compile(pattern)
    addresses = set()
    output = utils.nslookup(address)
    list = output.splitlines()
    #do not go over the first 2 lines in nslookup output
    for line in list[2:]:
        found = rePattern.search(line)
        if found:
            foundAddress = found.group(1)
            logging.debug("%s resolved into %s"%(address, foundAddress))
            addresses.add(foundAddress)
    return addresses

def _isPathInExportFs(path):
    if not os.path.exists(basedefs.FILE_ETC_EXPORTS):
        return False
    file = open(basedefs.FILE_ETC_EXPORTS)
    fileContent = file.readlines()
    file.close()

    for line in fileContent:
        if utils.verifyStringFormat(line, "^%s\s+.+" % (path)):
            return True
    return False

def _getBasePath(path):
    if os.path.exists(path):
        return path
    dirList = path.split("/")
    dirList.pop()
    if len(dirList) > 1:
        path = "/".join(dirList)
    else:
        path = "/"
    return _getBasePath(path)

def _isPathWriteable(path):
    try:
        logging.debug("attempting to write temp file to %s" % (path))
        tempfile.TemporaryFile(dir=path)
        return True
    except:
        logging.warning(traceback.format_exc())
        logging.warning("%s is not writeable" % path)
        return False

