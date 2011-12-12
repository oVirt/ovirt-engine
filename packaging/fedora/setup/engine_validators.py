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
    if validateInteger(param, options):
        port = int(param)
        if (port > 1024 and port < 65535):
            (portOpen, process, pid) = utils.isTcpPortOpen(param)
            if portOpen:
                print output_messages.INFO_VAL_PORT_OCCUPIED % (param, process, pid)
                return False
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
