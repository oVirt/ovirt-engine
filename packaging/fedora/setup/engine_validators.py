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
import uuid
import socket


def validateNFSMountPoint(param, options=[]):
    """ Validates the correct mount point for NFS local storage """

    if validateMountPoint(param) and validateDirSize(param, basedefs.CONST_MINIMUM_SPACE_ISODOMAIN):
            return True

    return False

def validateMountPoint(path):
    logging.info("validating %s as a valid mount point" % (path))
    if not utils.verifyStringFormat(path, "^\/[\w\_\-\s]+(\/[\w\_\-\s]+)*\/?$"):
        print output_messages.INFO_VAL_PATH_NAME_INVALID
        return False
    valid = True
    if os.path.exists(path) and len(os.listdir(path)):
        for entry in os.listdir(path):
            entry_path = os.path.join(path, entry)
            if not os.path.isdir(entry_path):
                valid = False
                break
            try:
                #check if the entry is a valid UUID
                if uuid.UUID(entry).version != 4:
                    valid = False
                    break
            except ValueError:
                valid = False
                break
    if not valid:
        print output_messages.INFO_VAR_PATH_NOT_EMPTY % path
        return False
    if not _isPathWriteable(_getBasePath(path)):
        print output_messages.INFO_VAL_PATH_NOT_WRITEABLE
        return False

    return True

def validateDirSize(path, size):
    availableSpace = utils.getAvailableSpace(_getBasePath(path))
    if availableSpace < size:
        print output_messages.INFO_VAL_PATH_SPACE % (path,
                                                     utils.transformUnits(availableSpace),
                                                     utils.transformUnits(size))
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


def validateRemotePort(param, options = []):
    #Validate that the port is an integer betweeen 1024 and 65535
    logging.debug("Validating %s as a valid TCP Port" % (param))
    if validateInteger(param, options):
        port = int(param)
        if (port > 0 and port < 65535):
            return True
        else:
            logging.warn("validatePort('%s') - failed" %(param))
            print output_messages.INFO_VAL_PORT_NOT_RANGE

    return False

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
    if "'" in param:
        logging.error(output_messages.ERR_NOT_ALLOWED_CHAR % "'")
        print output_messages.ERR_NOT_ALLOWED_CHAR % "'"
        return False
    if re.search("\s", param):
        logging.error(output_messages.ERR_SPACES_IN_PASS)
        print output_messages.ERR_SPACES_IN_PASS
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
    if "yes" in options and param.lower() == "y":
        return True
    if "no" in options and param.lower() == "n":
        return True
    if param.lower() in [option.lower() for option in options]:
        return True
    print output_messages.INFO_VAL_NOT_IN_OPTIONS % (", ".join(options))
    return False


def ensurePortPreCondition(param, options=[]):
    # check if selinux is enabled. exit if not enabled.
    logging.debug("Checking SELINUX status")
    cmd = [
        basedefs.EXEC_GETENFORCE,
    ]
    (out, rc) = utils.execCmd(cmdList=cmd, failOnError=True)
    if "Disabled" in out:
        logging.debug("SELINUX was found in disabled mode")
        print output_messages.MSG_ENABLE_SELINUX
        return False
    #Need httpd stopped for later port validation
    logging.debug("stopping httpd service")
    utils.Service(basedefs.HTTPD_SERVICE_NAME).stop()
    return True


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

        # DB Encoding check
        _checkDbEncoding(param["DB_ADMIN"], param["DB_HOST"], param["DB_PORT"])

        # Delete DB check
        _checkDropDbPrivilege(param["DB_ADMIN"], param["DB_HOST"], param["DB_PORT"])

        # Everything is fine, return True
        return True

    except Exception,e:
        # Something failed, print the error on screen and return False
        print e
        return False

    finally:
        # if the test DB was created, drop it
        sqlQuery = "DROP DATABASE IF EXISTS ovirt_engine_test;"
        utils.execRemoteSqlCommand(param["DB_ADMIN"],
                                   param["DB_HOST"],
                                   param["DB_PORT"],
                                   basedefs.DB_POSTGRES, sqlQuery, False)

        # restore the original pgpass file in all cases
        if os.path.exists(backupFile):
            os.rename(backupFile, basedefs.DB_PASS_FILE)


def validateFQDN(param, options=[]):
    logging.info("Validating %s as a FQDN on non loopback devices" % (param))
    # Ensure that it isn't an IP address.
    if re.match("\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}", param):
        logging.error(output_messages.ERR_CANT_USE_IP_AS_FQDN % (param))
        print output_messages.ERR_CANT_USE_IP_AS_FQDN % (param)
        return False

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
        resolvedFromDNS = False
        if len(resolvedAddresses) < 1:
            logging.error("Failed to resolve %s using DNS."%(param))
            try:
                resolvedAddresses = set(socket.gethostbyname_ex(param)[2])
            except socket.error:
                #can't be resolved by /etc/hosts
                print output_messages.ERR_DIDNT_RESOLVED_IP%(param)
                return False
            logging.warning('%s can be resolved only locally!' % param)
        else:
            resolvedFromDNS = True
        #string is generated here since we use it in all latter error messages
        prettyString = " ".join(["%s"%string for string in resolvedAddresses])

        #compare found IP with list of local IPs and match.
        if not resolvedAddresses.issubset(ipAddresses):
            logging.error(
                (
                    "the following address(es): %s can't be mapped to "
                    "non loopback devices on this host"
                ) %(prettyString)
            )
            #different grammar for plural and single
            if len(resolvedAddresses) > 1:
                print output_messages.ERR_IPS_NOT_CONFIGED%(prettyString, param)
            else:
                print output_messages.ERR_IPS_NOT_CONFIGED_ON_INT%(prettyString, param)
            return False

        #reverse resolved IP and compare with given fqdn
        if resolvedFromDNS:
            counter = 0
            pattern = '[\w\.-]+\s+name\s\=\s([\w\.\-]+)\.'
            for address in resolvedAddresses:
                addressSet = _getPatternFromNslookup(address, pattern)
                reResolvedAddress = None
                revResolved = False
                if len(addressSet) > 0:
                    reResolvedAddress = addressSet.pop()
                    if reResolvedAddress.lower() == param.lower():
                        counter += 1
                        revResolved = True
                if not revResolved:
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
    except:
        logging.error(traceback.format_exc())
        raise

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
        cmd = [
            "/bin/ping",
            "-c", "1",
            "%s" % param,
        ]
        out, rc = utils.execCmd(cmdList=cmd)
        if rc == 0:
            return True

    print "\n" + output_messages.ERR_PING + ".\n"
    return False

def validateWebSocketProxy(param, options=[]):
    param['CONFIG_WEBSOCKET_PROXY'] = "no"
    ret = utils.Service('ovirt-websocket-proxy').available()
    logging.debug("validateWebSocketProxy %s", ret)
    return ret

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
        logging.error(output_messages.ERR_DB_CREATE_PRIV, dbHost)
        raise Exception("\n" + output_messages.ERR_DB_CREATE_PRIV % dbHost + "\n")
    else:
        logging.info("Successfully created temp database on server %s." % dbHost)

def _checkDbEncoding(dbAdminUser, dbHost, dbPort):
    """ _checkDbEncoding checks DB default encoding on DB server"""

    logging.info("Checking encoding of the database 'ovirt_engine_test' on remote server.")
    try:
        utils.checkDbEncoding(
            dbAdminUser=dbAdminUser,
            dbHost=dbHost,
            dbPort=dbPort,
            dbName="ovirt_engine_test",
        )
    except Exception, e:
        raise e


def _checkDropDbPrivilege(dbAdminUser, dbHost, dbPort):
    """ _checkDropDbPrivilege checks DROP DB privilege on DB server"""

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
        if os.path.exists(backupFile):
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


def _getBasePath(path):
    if os.path.exists(path):
        return path

    # Iterate up in the tree structure until we get an
    # existing path
    return _getBasePath(os.path.dirname(path.rstrip("/")))

def _isPathWriteable(path):
    try:
        logging.debug("attempting to write temp file to %s" % (path))
        tempfile.TemporaryFile(dir=path)
        return True
    except:
        logging.warning(traceback.format_exc())
        logging.warning("%s is not writeable" % path)
        return False
