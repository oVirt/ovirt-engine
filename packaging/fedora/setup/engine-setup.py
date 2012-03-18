#! /usr/bin/python

import sys
import logging
import os
import re
import traceback
import types
import socket
import ConfigParser
from StringIO import StringIO
import libxml2
import shutil
import pwd
import getpass
import copy
import time
import datetime
import nfsutils
import basedefs
import output_messages
import common_utils as utils
import engine_validators as validate
import random
import tempfile
from optparse import OptionParser, OptionGroup
from setup_controller import Controller

# Globals
controller = Controller()
logFile = os.path.join(basedefs.DIR_LOG,basedefs.FILE_INSTALLER_LOG)
commandLineValues = {}

# Required by stopRhevmDbRelatedServices & startRhevmDbRelatedServices
etlService = utils.Service("rhevm-etl")
notificationService = utils.Service("rhevm-notifierd")


# List to hold all values to be masked in logging (i.e. passwords and sensitive data)
#TODO: read default values from conf_param?
masked_value_set = set()

def initLogging():
    global logFile
    try:
        #in order to use UTC date for the log file, send True to getCurrentDateTime(True)
        logFilename = "engine-setup_%s.log" %(utils.getCurrentDateTime())
        logFile = os.path.join(basedefs.DIR_LOG,logFilename)
        if not os.path.isdir(os.path.dirname(logFile)):
            os.makedirs(os.path.dirname(logFile))
        level = logging.INFO
        level = logging.DEBUG
        hdlr = logging.FileHandler(filename = logFile, mode='w')
        fmts='%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
        dfmt='%Y-%m-%d %H:%M:%S'
        fmt = logging.Formatter(fmts, dfmt)
        hdlr.setFormatter(fmt)
        logging.root.addHandler(hdlr)
        logging.root.setLevel(level)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_INIT_LOGGER)

#defined as a list and not tuple because we might need to add ports
#on the run later on
NFS_IPTABLES_PORTS = [ {"port"     : "111",
                        "protocol" : ["udp","tcp"]},
                       {"port"     : "892",
                        "protocol" : ["udp","tcp"]},
                       {"port"     : "875",
                        "protocol" : ["udp","tcp"]},
                       {"port"     : "662",
                        "protocol" : ["udp","tcp"]},
                       {"port"     : "2049",
                        "protocol" : ["tcp"]},
                       {"port"     : "32803",
                        "protocol" : ["tcp"]},
                       {"port"     : "32769",
                        "protocol" : ["udp"]},
]

def generateMacRange():
    ipSet = utils.getConfiguredIps()
    if len(ipSet) > 0:
        ip = ipSet.pop()
        mac_parts = ip.split(".")[1:3]
        mac_base ="%s:%02X:%02X" %(basedefs.CONST_BASE_MAC_ADDR, int(mac_parts[0]), int(mac_parts[1]))
        return "%s:00-%s:FF"%(mac_base, mac_base)
    else:
        logging.error("Could not find a configured ip address, returning default MAC address range")
        return basedefs.CONST_DEFAULT_MAC_RANGE

def initSequences():
    sequences_conf = [
                      { 'description'     : 'Initial Steps',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_CONFIG_OVIRT_ENGINE,
                                                'functions' : [_createJbossProfile, setMaxSharedMemory] },
                                              { 'title'     : output_messages.INFO_CREATE_CA,
                                                'functions' : [_createCA]},
                                              { 'title'     : output_messages.INFO_UPD_JBOSS_CONF,
                                                'functions' : [configJbossXml, deployJbossModules, _editRootWar] },
                                              { 'title'     : output_messages.INFO_SET_DB_CONFIGURATION,
                                                'functions' : [_updatePgPassFile]}]
                       },
                      { 'description'     : 'Update DB',
                        'condition'       : [_isDbAlreadyInstalled],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_UPGRADE_DB,
                                                'functions' : [stopRhevmDbRelatedServices, _upgradeDB, startRhevmDbRelatedServices]} ]
                       },
                      { 'description'     : 'Create DB',
                        'condition'       : [_isDbAlreadyInstalled],
                        'condition_match' : [False],
                        'steps'           : [ { 'title'     : output_messages.INFO_SET_DB_SECURITY,
                                                'functions' : [_encryptDBPass, configEncryptedPass]},
                                              { 'title'     : output_messages.INFO_CREATE_DB,
                                                'functions' : [_createDB,  _updateVDCOptions]},
                                              { 'title'     : output_messages.INFO_UPD_DC_TYPE,
                                                'functions' : [_updateDefaultDCType]} ]
                       },
                      { 'description'     : 'Edit Configuration',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_UPD_RHEVM_CONF,
                                                'functions' : [_editToolsConfFile, editPostgresConf, updateFileDescriptors] } ]
                       },
                      { 'description'     : 'Config NFS',
                        'condition'       : [utils.compareStrIgnoreCase, controller.CONF["CONFIG_NFS"], "yes"],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_CFG_NFS,
                                                'functions' : [_configNfsShare] } ]
                       },
                      { 'description'     : 'Final Steps',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_CFG_IPTABLES,
                                                'functions' : [_configIptables] },
                                              { 'title'     : output_messages.INFO_START_JBOSS,
                                                'functions' : [_startJboss] } ]
                       },
                     ]

    for item in sequences_conf:
        controller.addSequence(item['description'], item['condition'], item['condition_match'], item['steps'])

def initConfig():
    """
    Initialization of configuration
    """

    """
    Param Fields:
    CMD_OPTION       - the command line flag to use for this option
    USAGE            - usage to display to the user
    PROMPT           - text to prompt the user with when querying this param
    OPTION_LIST      - if set, let the user only choose from this list as answer
    VALIDATION_FUNC  - Validation function for this param
    DEFAULT_VALUE    - the default value of this param
    MASK_INPUT       - should we mask the value of this param in the logs?
    LOOSE_VALIDATION - (True/False) if True, and validation failed, let the user use the failed value
    CONF_NAME        - Name of param, must be unique, used as key
    USE_DEFAULT      - (True/False) Should we use the default value instead of querying the user?
    NEED_CONFIRM     - (True/False) Do we require the user to confirm the input(used in password fields)
    CONDITION        - (True/False) is this a condition for a group?
    """
    conf_params = {
         "IPTABLES" : [
            {   "CMD_OPTION"      :"override-iptables",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_IPTABLES_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_IPTABLES_PROMPT,
                "OPTION_LIST"     :["yes","no"],
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "OVERRIDE_IPTABLES",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False} ]
         ,
         "ALL_PARAMS" : [
            {   "CMD_OPTION"      :"http-port",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_HTTP_PORT_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_HTTP_PORT_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validatePort,
                "DEFAULT_VALUE"   :"8080",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "HTTP_PORT",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"https-port",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_HTTPS_PORT_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_HTTPS_PORT_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validatePort,
                "DEFAULT_VALUE"   :"8443",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "HTTPS_PORT",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

             {  "CMD_OPTION"      :"mac-range",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_MAC_RANGE_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_MAC_RANG_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateStringNotEmpty,
                "DEFAULT_VALUE"   :generateMacRange(),
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "MAC_RANGE",
                "USE_DEFAULT"     : True,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"host-fqdn",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_FQDN_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_FQDN_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateFQDN,
                "DEFAULT_VALUE"   :socket.getfqdn(),
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": True,
                "CONF_NAME"       : "HOST_FQDN",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"auth-pass",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_AUTH_PASS_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_AUTH_PASS_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validatePassword,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : True,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "AUTH_PASS",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : True,
                "CONDITION"       : False},

             {  "CMD_OPTION"      :"org-name",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_ORG_NAME_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_ORG_NAME_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateOrgName,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "ORG_NAME",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"default-dc-type",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_DC_TYPE_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_DC_TYPE_PROMPT,
                "OPTION_LIST"     :["NFS","FC","ISCSI"],
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   :"NFS",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DC_TYPE",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      : "db-remote-install",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_REMOTE_DB_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_REMOTE_DB_PROMPT,
                "OPTION_LIST"     : ["remote", "local"],
                "VALIDATION_FUNC" : validate.validateOptions,
                "DEFAULT_VALUE"   : "local",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_REMOTE_INSTALL",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False}]
         ,
         "LOCAL_DB": [
            {   "CMD_OPTION"      :"db-local-pass",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_DB_PASSWD_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_DB_PASSWD_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validatePassword,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : True,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_LOCAL_PASS",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : True,
                "CONDITION"       : False}]
         ,
         "REMOTE_DB" : [

            {   "CMD_OPTION"      : "db-host",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_USE_DB_HOST_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_USE_DB_HOST_PROMPT,
                "OPTION_LIST"     : [],
                "VALIDATION_FUNC" : validate.validatePing,
                "DEFAULT_VALUE"   : "",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": True,
                "CONF_NAME"       : "DB_HOST",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      : "db-port",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_USE_DB_PORT_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_USE_DB_PORT_PROMPT,
                "OPTION_LIST"     : [],
                "VALIDATION_FUNC" : validate.validateInteger,
                "DEFAULT_VALUE"   : basedefs.DB_PORT,
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_PORT",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      : "db-admin",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_DB_ADMIN_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_DB_ADMIN_PROMPT,
                "OPTION_LIST"     : [],
                "VALIDATION_FUNC" : validate.validateUser,
                "DEFAULT_VALUE"   : basedefs.DB_ADMIN,
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_ADMIN",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      : "db-remote-pass",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_REMOTE_DB_PASSWD_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_REMOTE_DB_PASSWD_PROMPT,
                "OPTION_LIST"     : [],
                "VALIDATION_FUNC" : validate.validatePassword,
                "DEFAULT_VALUE"   : "",
                "MASK_INPUT"      : True,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_REMOTE_PASS",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : True,
                "CONDITION"       : False},

            {   "CMD_OPTION"      : "db-secure-connection",
                "USAGE"           : output_messages.INFO_CONF_PARAMS_DB_SECURE_CONNECTION_USAGE,
                "PROMPT"          : output_messages.INFO_CONF_PARAMS_DB_SECURE_CONNECTION_PROMPT,
                "OPTION_LIST"     : ["yes", "no"],
                "VALIDATION_FUNC" : validate.validateOptions,
                "DEFAULT_VALUE"   : "no",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "DB_SECURE_CONNECTION",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},]
          ,
          "NFS": [
             {  "CMD_OPTION"      :"nfs-mp",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_NFS_MP_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_NFS_MP_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateMountPoint,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "NFS_MP",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

             {  "CMD_OPTION"      :"iso-domain-name",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_NFS_DESC_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_NFS_DESC_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateIsoDomainName,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "ISO_DOMAIN_NAME",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

             {  "CMD_OPTION"      :"config-nfs",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_CONFIG_NFS_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_CONFIG_NFS_PROMPT,
                "OPTION_LIST"     :["yes","no"],
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   :"yes",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "CONFIG_NFS",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : True} ]
    }
    """
    Group fields:
    GROUP_NAME           - Name of group, used as key
    DESCRIPTION          - Used to prompt the user when showing the command line options
    PRE_CONDITION        - Condition to match before going over all params in the group, if fails, will not go into group
    PRE_CONDITION_MATCH  - Value to match condition with
    POST_CONDITION       - Condition to match after all params in the groups has been queried. if fails, will re-query all parameters
    POST_CONDITION_MATCH - Value to match condition with
    """
    conf_groups = (
                    { "GROUP_NAME"            : "ALL_PARAMS",
                      "DESCRIPTION"           : output_messages.INFO_GRP_ALL,
                      "PRE_CONDITION"         : False,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
                    { "GROUP_NAME"            : "REMOTE_DB",
                      "DESCRIPTION"           : output_messages.INFO_GRP_REMOTE_DB,
                      "PRE_CONDITION"         : validate.validateRemoteHost,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : validate.validateRemoteDB,
                      "POST_CONDITION_MATCH"  : True},
                    { "GROUP_NAME"            : "LOCAL_DB",
                      "DESCRIPTION"           : output_messages.INFO_GRP_LOCAL_DB,
                      "PRE_CONDITION"         : validate.validateRemoteHost,
                      "PRE_CONDITION_MATCH"   : False,
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
                    { "GROUP_NAME"            : "NFS",
                      "DESCRIPTION"           : output_messages.INFO_GRP_ISO,
                      "PRE_CONDITION"         : "CONFIG_NFS",
                      "PRE_CONDITION_MATCH"   : "yes",
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
                    { "GROUP_NAME"            : "IPTABLES",
                      "DESCRIPTION"           : output_messages.INFO_GRP_IPTABLES,
                      "PRE_CONDITION"         : False,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
    )
    for group in conf_groups:
        paramList = conf_params[group["GROUP_NAME"]]
        controller.addGroup(group, paramList)

#data center types enum
controller.CONF["DC_TYPE_ENUM"] = utils.Enum(NFS=1, FC=2, ISCSI=3)

def _getColoredText (text, color):
    ''' gets text string and color
        and returns a colored text.
        the color values are RED/BLUE/GREEN/YELLOW
        everytime we color a text, we need to disable
        the color at the end of it, for that
        we use the NO_COLOR chars.
    '''
    return color + text + basedefs.NO_COLOR

def _getInputFromUser(param):
    """
    this private func reads the data from the user
    for the given param
    """
    loop = True
    userInput = None

    try:
        if param.getKey("USE_DEFAULT"):
            logging.debug("setting default value (%s) for key (%s)" % (mask(param.getKey("DEFAULT_VALUE")), param.getKey("CONF_NAME")))
            controller.CONF[param.getKey("CONF_NAME")] = param.getKey("DEFAULT_VALUE")
        else:
            while loop:
                # If the value was not supplied by the command line flags
                if not commandLineValues.has_key(param.getKey("CONF_NAME")):
                    message = StringIO()
                    message.write(param.getKey("PROMPT"))

                    if type(param.getKey("OPTION_LIST")) == types.ListType and len(param.getKey("OPTION_LIST")) > 0:
                        message.write(" %s" % (str(param.getKey("OPTION_LIST")).replace(',', '|')))

                    if param.getKey("DEFAULT_VALUE"):
                        message.write("  [%s] " % (str(param.getKey("DEFAULT_VALUE"))))

                    message.write(": ")
                    message.seek(0)
                    #mask password or hidden fields

                    if (param.getKey("MASK_INPUT")):
                        userInput = getpass.getpass("%s :" % (param.getKey("PROMPT")))
                    else:
                        userInput = raw_input(message.read())
                else:
                    userInput = commandLineValues[param.getKey("CONF_NAME")]
                # If DEFAULT_VALUE is set and user did not input anything
                if userInput == "" and len(param.getKey("DEFAULT_VALUE")) > 0:
                    userInput = param.getKey("DEFAULT_VALUE")

                # If param requires validation
                if param.getKey("VALIDATION_FUNC")(userInput, param.getKey("OPTION_LIST")):
                    controller.CONF[param.getKey("CONF_NAME")] = userInput
                    loop = False
                # If validation failed but LOOSE_VALIDATION is true, ask user
                elif param.getKey("LOOSE_VALIDATION"):
                    answer = _askYesNo("User input failed validation, do you still wish to use it")
                    if answer:
                        loop = False
                        controller.CONF[param.getKey("CONF_NAME")] = userInput
                    else:
                        if commandLineValues.has_key(param.getKey("CONF_NAME")):
                            del commandLineValues[param.getKey("CONF_NAME")]
                        loop = True
                else:
                    # Delete value from commandLineValues so that we will prompt the user for input
                    if commandLineValues.has_key(param.getKey("CONF_NAME")):
                        del commandLineValues[param.getKey("CONF_NAME")]
                    loop = True

    except KeyboardInterrupt:
        print "" # add the new line so messages wont be displayed in the same line as the question
        raise KeyboardInterrupt
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_READ_INPUT_PARAM % (param.getKey("CONF_NAME")))

def input_param(param):
    """
    this func will read input from user
    and ask confirmation if needed
    """
    # We need to check if a param needs confirmation, (i.e. ask user twice)
    # Do not validate if it was given from the command line
    if (param.getKey("NEED_CONFIRM") and not commandLineValues.has_key(param.getKey("CONF_NAME"))):
        #create a copy of the param so we can call it twice
        confirmedParam = copy.deepcopy(param)
        confirmedParamName = param.getKey("CONF_NAME") + "_CONFIRMED"
        confirmedParam.setKey("CONF_NAME", confirmedParamName)
        confirmedParam.setKey("PROMPT", output_messages.INFO_CONF_PARAMS_PASSWD_CONFIRM_PROMPT)
        confirmedParam.setKey("VALIDATION_FUNC", validate.validateStringNotEmpty)
        # Now get both values from user (with existing validations
        while True:
            _getInputFromUser(param)
            _getInputFromUser(confirmedParam)
            if controller.CONF[param.getKey("CONF_NAME")] == controller.CONF[confirmedParamName]:
                logging.debug("Param confirmation passed, value for both questions is identical")
                break
            else:
                print output_messages.INFO_VAL_PASSWORD_DONT_MATCH
    else:
        _getInputFromUser(param)

    return param

def _askYesNo(question=None):
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
        return _askYesNo(question)

def copyAndLinkConfig(config):
    """
    Moves file to oVirt config directory and create sym-link
    from the original location to the new one
    """

    basename = os.path.basename(config)
    new_config_path = os.path.join(basedefs.DIR_CONFIG, basename)

    # Verify destination dir exists, create it if necessary
    if not os.path.isdir(basedefs.DIR_CONFIG):
        try:
            logging.debug("Creating ovirt-engine config directory")
            os.makedirs(basedefs.DIR_CONFIG)
        except:
            logging.error(traceback.format_exc())
            raise Exception(output_messages.ERR_EXP_FAILED_CREATE_RHEVM_CONFIG_DIR % basedefs.DIR_CONFIG)

    # Verify original config is not already linked
    if os.path.islink(config):
        if (os.readlink(config) == new_config_path):
            logging.debug("%s is already linked to %s"%(config, new_config_path))
            return(os.path.join(basedefs.DIR_CONFIG, basename))
        else:
            raise Exception(output_messages.ERR_EXP_LINK_EXISTS%(config, new_config_path))

    # Verify original config is a normal file, and copy it to the new location
    elif os.path.isfile(config):
        try:
            utils.copyFile(config, basedefs.DIR_CONFIG)

            # Remove old file
            logging.debug("Removing %s" %(config))
            os.remove(config)

            # Linking
            logging.debug("Linking  %s to %s/%s" %(config, basedefs.DIR_CONFIG, config))
            os.symlink(new_config_path, config)
        except:
            logging.error(traceback.format_exc())
            raise Exception(output_messages.ERR_EXP_CPY_RHEVM_CFG % (config, "%s/%s" % (basedefs.DIR_CONFIG, config)))
        # return new path
        return new_config_path

def _editJbossasConf():
    try:
        jbossasHandler = utils.TextConfigFileHandler(basedefs.FILE_JBOSSAS_CONF)
        jbossasHandler.open()

        jbossasHandler.editParam("JBOSS_IP", "0.0.0.0")
        jbossasHandler.editParam("JBOSSCONF", basedefs.JBOSS_PROFILE_NAME)

        jbossasHandler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_JBOSS_CONF%(basedefs.FILE_JBOSSAS_CONF))

def _linkHttpParams():
    #copy context.xml to ROOT.war/WEB-INF
    try:
        #create links for files in /usr/share/rhevm/reosurces/jboss
        targetsList = [ basedefs.FILE_JBOSS_HTTP_PARAMS ]

        for target in targetsList:
            #first, remove existing destination
            link = "%s/%s" % (basedefs.DIR_JBOSS_ROOT_WAR, os.path.basename(target))
            if os.path.exists(link):
                if os.path.islink(link):
                    logging.debug("removing link %s" % link)
                    os.unlink(link)
                #logging.debug("Removing %s" % link)
                elif os.path.isdir(link):
                    #remove dir using shutil.rmtree
                    logging.debug("removing directory %s" % link)
                    shutil.rmtree(link)
                else:
                    logging.debug("removing file %s" % link)
                    os.remove(link)

            logging.debug("Linking %s to %s" % (target, link))
            os.link(target, link)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_ROOT_WAR)

def _changeDefaultWelcomePage():
    logging.debug("editing jboss conf file %s" % (basedefs.FILE_JBOSS_WEB_XML_SRC))
    webXmlHandler = utils.XMLConfigFileHandler(basedefs.FILE_JBOSS_WEB_XML_SRC)
    webXmlHandler.open()

    #check if the node already exists
    welcomeFileListNode = webXmlHandler.xpathEval("/web-app/welcome-file-list")
    if len(welcomeFileListNode) == 0: #get the child to insert the node after
        servletMapNode = utils.getXmlNode(webXmlHandler, "/web-app/mime-mapping")

        #add new node - welcome-file-list
        welcomeFileListNode = libxml2.newNode('welcome-file-list')

        #add the new node as a next sibling to the servletMapNode
        servletMapNode.addNextSibling(welcomeFileListNode)

        #add child and set content to the new child node
        welcomeFileNode = libxml2.newNode('welcome-file')
        welcomeFileListNode.addChild(welcomeFileNode)
    else:
        logging.debug("jboss file %s already updated, updating tag content only" % (basedefs.FILE_JBOSS_WEB_XML_SRC))

    logging.debug("setting value of default root.war html-file to %s" % (basedefs.FILE_JBOSS_ROOT_WAR_HTML))
    utils.setXmlContent(webXmlHandler, "/web-app/welcome-file-list/welcome-file", basedefs.FILE_JBOSS_ROOT_WAR_HTML)
    webXmlHandler.close()

def _addMimeMapNode(webXmlHandler):
    """
    adds a new mime-mapping node after description
    """
    descMapNode = utils.getXmlNode(webXmlHandler, "/web-app/description")
    mimeMapListNode = libxml2.newNode('mime-mapping')

    #add the new node as a next sibling to the servletMapNode
    descMapNode.addNextSibling(mimeMapListNode)

    #add the extention node
    extFileNode = libxml2.newNode('extension')
    mimeMapListNode.addChild(extFileNode)

    #add the mime-type node
    typeFileNode = libxml2.newNode('mime-type')
    mimeMapListNode.addChild(typeFileNode)

    #set content to extension node
    logging.debug("setting value of extention to crt")
    utils.setXmlContent(webXmlHandler, "/web-app/mime-mapping/extension", "crt")

    #set content to mime type node
    logging.debug("setting value of mime-type to application/x-x509-ca-cert")
    utils.setXmlContent(webXmlHandler, "/web-app/mime-mapping/mime-type", "application/x-x509-ca-cert")

def _handleJbossCertFile():
    """
    copy ca.crt to ROOT.WAR dir
    change permissions to jboss
    update mime type to application/x-x509-ca-cert
    """
    destCaFile = os.path.join(basedefs.DIR_JBOSS_ROOT_WAR, "ca.crt")
    logging.debug("copying %s to %s" % (basedefs.FILE_CA_CRT_SRC, destCaFile))
    if os.path.exists(basedefs.FILE_CA_CRT_SRC):
        utils.copyFile(basedefs.FILE_CA_CRT_SRC, destCaFile)
    else:
        raise Exception(output_messages.ERR_EXP_CANT_FIND_CA_FILE % (basedefs.FILE_CA_CRT_SRC))

    # change ownership to jboss
    os.chown(destCaFile, utils.getUsernameId("jboss-as"), utils.getGroupId("jboss-as"))

    # update mime type in web.xml
    logging.debug("editing jboss conf file %s" % (basedefs.FILE_JBOSS_WEB_XML_SRC))
    webXmlHandler = utils.XMLConfigFileHandler(basedefs.FILE_JBOSS_WEB_XML_SRC)
    webXmlHandler.open()

    #check if the node already exists
    mimeMapListNode = webXmlHandler.xpathEval("/web-app/mime-mapping")

    #add new xml node in case we don't have one
    if len(mimeMapListNode) == 0:
        _addMimeMapNode(webXmlHandler)
    else:
        #if we already have a mapping node, check if it doesn't include the 'crt' map
        mapNodes = webXmlHandler.xpathEval("/web-app/mime-mapping/extension")
        nodeExists = False
        for node in mapNodes:
            if node.content == "crt":
                nodeExists = True
                break
        if not nodeExists:
            _addMimeMapNode(webXmlHandler)

    webXmlHandler.close()

def _editRootWar():
    try:
        # Copy new files and images to rhevm-slimmed profile
        _linkHttpParams()

        # Update rhevm_index.html file with consts
        logging.debug("update %s with http & ssl urls"%(basedefs.FILE_JBOSS_HTTP_PARAMS))

        controller.CONF["HTTP_URL"] = "http://" + controller.CONF["HOST_FQDN"] + ":" + controller.CONF["HTTP_PORT"]
        controller.CONF["HTTPS_URL"] = "https://" + controller.CONF["HOST_FQDN"] + ":" + controller.CONF["HTTPS_PORT"]

        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var host_fqdn.*", 'var host_fqdn = "%s";'%(controller.CONF["HOST_FQDN"]))
        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var http_port.*", 'var http_port = "%s";'%(controller.CONF["HTTP_PORT"]))
        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var https_port.*", 'var https_port = "%s";'%(controller.CONF["HTTPS_PORT"]))

        # Handle ca.crt
        _handleJbossCertFile()

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_ROOT_WAR)

def _editExternalConfig():
    try:
        exConfigHandler = utils.XMLConfigFileHandler(basedefs.FILE_EXTERNAL_CONFIG)
        exConfigHandler.open()

        node = utils.getXmlNode(exConfigHandler, "//add[@key='BackendPort']")
        node.setProp("value", controller.CONF["HTTPS_PORT"])
        exConfigHandler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_EXTERNAL_CFG%(basedefs.FILE_EXTERNAL_CONFIG))

def _editDefaultHtml():
    try:
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var httpPort.*", 'var httpPort = "%s";'%(controller.CONF["HTTP_PORT"]))
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var httpsPort.*", 'var httpsPort = "%s";'%(controller.CONF["HTTPS_PORT"]))
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var hostName.*", 'var hostName = "%s";'%(controller.CONF["HOST_FQDN"]))
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTML_FILE%(basedefs.FILE_DEFAULT_HTML))

def _createCA():

    try:
        # Create new CA only if none available
        ksPath = os.path.join(basedefs.DIR_OVIRT_PKI, ".keystore")
        if not os.path.exists(ksPath):
            _updateCaCrtTemplate()

            # time.timezone is in seconds
            tzOffset = time.timezone / 3600
            logging.debug("current timezone offset is %i", tzOffset)
            if abs(tzOffset) > 12:
                logging.debug("Timezone offset is bigger then 12, resizing to 12")
                tzOffset = 12

            # Add "+" infront of the string
            if tzOffset >= 0:
                tzOffsetStr = "+%.2i00" % tzOffset
            else:
                tzOffsetStr = "%.2i00" % tzOffset

            # We create the CA with yesterday's starting date
            yesterday = datetime.datetime.now() + datetime.timedelta(-1)
            date = "%s%s" % (yesterday.strftime("%y%m%d%H%M%S"), tzOffsetStr)
            logging.debug("Date string is %s", date)

            # Add random string to certificate CN field
            randInt = random.randint(10000,99999)

            # Truncating host fqdn to max allowed in certificate CN field
            truncatedFqdn = controller.CONF["HOST_FQDN"][0:basedefs.CONST_MAX_HOST_FQDN_LEN]
            logging.debug("truncated HOST_FQDN '%s' to '%s'. sized reduced to %d.."%(controller.CONF["HOST_FQDN"],truncatedFqdn,len(truncatedFqdn)))
            uniqueCN = truncatedFqdn + "." + str(randInt)
            logging.debug("using unique CN: '%s' for CA certificate"%uniqueCN)

            # Create the CA
            cmd = [os.path.join(basedefs.DIR_OVIRT_PKI, "installCA.sh"), controller.CONF["HOST_FQDN"],
                   basedefs.CONST_CA_COUNTRY, controller.CONF["ORG_NAME"], basedefs.CONST_CA_ALIAS, basedefs.CONST_CA_PASS, date,
                   basedefs.DIR_OVIRT_PKI, uniqueCN]

            out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_RC_CODE, [basedefs.CONST_CA_PASS])

            # Generate the ssh key
            cmd = [os.path.join(basedefs.DIR_OVIRT_PKI, "generate-ssh-keys"), "-s", ksPath, "-p",
                   basedefs.CONST_CA_PASS, "-a", "engine", "-k",
                   os.path.join(basedefs.DIR_OVIRT_PKI, "keys", "engine_id_rsa")]

            out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_RC_CODE, [basedefs.CONST_CA_PASS])

            # Copy publich ssh key to ROOT site
            utils.copyFile(basedefs.FILE_PUBLIC_SSH_KEY, basedefs.DIR_JBOSS_ROOT_WAR)

            # Extract CA fingerprint
            cmd = [basedefs.EXEC_OPENSSL, "x509", "-in", basedefs.FILE_CA_CRT_SRC, "-fingerprint", "-noout"]

            finger, rc = utils.execCmd(cmd, None, True)
            msg = output_messages.INFO_CA_SSL_FINGERPRINT%(finger.rstrip().split("=")[1])
            controller.MESSAGES.append(msg)

            # ExtractSSH fingerprint
            cmd = [basedefs.EXEC_SSH_KEYGEN, "-lf", basedefs.FILE_PUBLIC_SSH_KEY]
            finger, rc = utils.execCmd(cmd, None, True)
            msg = output_messages.INFO_CA_SSH_FINGERPRINT%(finger.split()[1])
            controller.MESSAGES.append(msg)

            # Set right permissions
            _changeCaPermissions(basedefs.DIR_OVIRT_PKI)
        else:
            msg = output_messages.INFO_CA_KEYSTORE_EXISTS
            logging.warn(msg)
            controller.MESSAGES.append(msg)

        # Always Copy publich ssh key to ROOT site
        utils.copyFile(basedefs.FILE_PUBLIC_SSH_KEY, basedefs.DIR_JBOSS_ROOT_WAR)

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_CREATE_CA)

def _changeCaPermissions(pkiDir):
    changeList = [os.path.join(pkiDir, "ca.pem"),
                    os.path.join(pkiDir,".keystore"),
                    os.path.join(pkiDir, "private"),
                    os.path.join(pkiDir, "private", "ca.pem"),
                    os.path.join(pkiDir,".truststore")
                    ]
    jbossUid = utils.getUsernameId("jboss-as")
    jbossGid = utils.getGroupId("jboss-as")

    for item in changeList:
        logging.debug("changing ownership of %s to %s/%s (uid/gid)" % (item, jbossUid, jbossGid))
        os.chown(item, int(jbossUid), int(jbossGid))
        logging.debug("changing file permissions for %s to 0750" % (item))
        os.chmod(item, 0750)

def _updateCaCrtTemplate():
    for file in [basedefs.FILE_CA_CRT_TEMPLATE, basedefs.FILE_CERT_TEMPLATE]:
        logging.debug("updating %s" % (file))
        fileHandler = utils.TextConfigFileHandler(file)
        fileHandler.open()
        fileHandler.editParam("authorityInfoAccess", " caIssuers;URI:http://%s:%s/ca.crt" % (controller.CONF["HOST_FQDN"], controller.CONF["HTTP_PORT"]))
        fileHandler.close()

def _configIptables():
    logging.debug("configuring iptables")
    try:
        file = open(basedefs.FILE_IPTABLES_DEFAULT, "r")
        fileContent = file.read()
        file.close()
        outputText = fileContent

        PORTS_LIST=[]
        #get the location of the drop all rule comment
        list = outputText.split("\n")
        location = None
        counter = 0
        for line in list:
            if line == "#drop all rule":
                location = counter
            counter += 1
        if not location:
            logging.error(output_messages.ERR_EXP_FAILED_IPTABLES_RULES)
            raise Exception(output_messages.ERR_EXP_FAILED_IPTABLES_RULES)

        insertLocation = location - len(list)

        for port in [controller.CONF["HTTP_PORT"], controller.CONF["HTTPS_PORT"]]:
            lineToAdd = "-A RH-Firewall-1-INPUT -m state --state NEW -p tcp --dport %s -j ACCEPT" % port
            list.insert(insertLocation, lineToAdd)

        if utils.compareStrIgnoreCase(controller.CONF["CONFIG_NFS"], "yes"):
            PORTS_LIST = PORTS_LIST + NFS_IPTABLES_PORTS

        for portCfg in PORTS_LIST:
            portNumber = portCfg["port"]
            for protocol in portCfg["protocol"]:
                lineToAdd = "-A RH-Firewall-1-INPUT -m state --state NEW -p %s --dport %s -j ACCEPT"%(protocol, portNumber)
                list.insert(insertLocation, lineToAdd)

        outputText = "\n".join(list)
        logging.debug(outputText)
        exampleFile = open(basedefs.FILE_IPTABLES_EXAMPLE, "w")
        exampleFile.write(outputText)
        exampleFile.close()

        if controller.CONF["OVERRIDE_IPTABLES"] == "yes":
            if os.path.isfile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG)):
                backupFile = "%s.%s_%s"%(basedefs.FILE_IPTABLES_BACKUP, time.strftime("%H%M%S-%m%d%Y"), os.getpid())
                utils.copyFile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG), backupFile)
                controller.MESSAGES.append(output_messages.INFO_IPTABLES_BACKUP_FILE%(backupFile))

            utils.copyFile(basedefs.FILE_IPTABLES_EXAMPLE, "%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG))

            #stop the iptables explicitly, since we dont care about the status
            #of the current rules we will ignore the return code
            logging.debug("Restarting the iptables service")
            iptables = utils.Service("iptables")
            iptables.stop(True)
            iptables.start(True)

        else:
            controller.MESSAGES.append(output_messages.INFO_IPTABLES_PORTS%(controller.CONF["HTTP_PORT"], controller.CONF["HTTPS_PORT"]))
            controller.MESSAGES.append(output_messages.INFO_IPTABLES_FILE%(basedefs.FILE_IPTABLES_EXAMPLE))

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_CFG_IPTABLES)

def _createJbossProfile():
    logging.debug("creating jboss profile")
    try:
        dirs = [
                   {'src'  : basedefs.DIR_ENGINE_EAR_SRC,
                    'dest' : os.path.join(basedefs.DIR_JBOSS, "standalone", "deployments", "engine.ear")},
                   {'src'  : basedefs.DIR_ROOT_WAR_SRC,
                    'dest' : os.path.join(basedefs.DIR_JBOSS, "standalone", "deployments", "ROOT.war")}
                   ]

        for item in dirs:
            if not os.path.exists(item['dest']):
                if os.path.islink(item['dest']) and os.readlink(item['dest']) != item['src']:
                    os.remove(item['dest'])
                os.symlink(item['src'], item['dest'])

                logging.debug("Successfully created jboss profile %s"%(basedefs.JBOSS_PROFILE_NAME))
            else:
                logging.debug("%s profile already exists, doing nothing"%(basedefs.JBOSS_PROFILE_NAME))

            logging.debug("touching .dodeploy file for %s" % item['dest'])
            open("%s.dodeploy" % item['dest'], 'w').close()

    except:
        logging.error(traceback.format_exc())
        raise Exception("Failed to create JBoss profile")

def _createDB():
    """
    create fresh engine db
    set db security settings
    """
    logging.debug("installing postgres db")
    # TODO: do we need to add user permissions validation? (runs only with root access now)
    dbLogFilename = "engine-db-install-%s.log" %(utils.getCurrentDateTime())
    logging.debug("engine db creation is logged at %s/%s" % (basedefs.DIR_LOG, dbLogFilename))

    # Set rhevm-db-install.sh args - logfile and db password
    # To handle remote DB installation, we need to pass host/port/user values, and the script needs to handle them.
    scriptHome = os.path.join(basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_INSTALL_SCRIPT)
    cmd = [scriptHome, "-l", dbLogFilename,
                       "-w", controller.CONF["DB_PASS"],
                       "-u", getDbAdminUser(),
                       "-s", getDbHostName(),
                       "-p", getDbPort()]

    # Create db using shell command
    output, rc = utils.execCmd(cmd, None, True, output_messages.ERR_DB_CREATE_FAILED, masked_value_set)
    logging.debug("Successfully installed %s db" % basedefs.DB_NAME)

def _upgradeDB():
    """
    run db upgrade scripts
    required installed db.
    won't change db security settings
    """

    # Before db upgrade we want to make a backup of existing db in case we fail
    # The backup is performed on local system, even for remote DB.
    dbBackupFile = tempfile.mkstemp(suffix=".sql", dir=basedefs.DIR_DB_BACKUPS)[1]
    logging.debug("backing up %s db to file %s"%(basedefs.DB_NAME, dbBackupFile))

    # Run db backup
    utils.backupDB(basedefs.DB_NAME, getDbAdminUser(), dbBackupFile, getDbHostName(), getDbPort())

    # Rename DB first. If it fails - stop with "active connections" error.
    # if upgrade passes fine, rename the DB back.
    DB_NAME_TEMP = "%s_%s" % (basedefs.DB_NAME, utils.getCurrentDateTime())
    utils.renameDB(basedefs.DB_NAME, DB_NAME_TEMP)

    # if we're here, DB was renamed.
    logging.debug("upgrading db schema")
    dbScriptArgs = "-u %s -d %s -h %s --port=%s" %(getDbAdminUser(), DB_NAME_TEMP, getDbHostName(), getDbPort())
    cmd = os.path.join("/bin/sh ", basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_UPGRADE_SCRIPT + " " + dbScriptArgs)

    # Upgrade script must run from dbscripts dir
    currentDir = os.getcwd()
    os.chdir(basedefs.DIR_DB_SCRIPTS)

    try:

        # Run upgrade.sh script to update existing db
        output, rc = utils.execExternalCmd(cmd, True, output_messages.ERR_DB_UPGRADE_FAILED)

        # Log the successful upgrade
        logging.debug('Successfully upgraded %s DB'%(basedefs.DB_NAME))
        controller.MESSAGES.append("DB was upgraded to latest version. previous DB backup can be found at %s"%(dbBackupFile))

        # Go back to previous dir
        os.chdir(currentDir)

        # Upgrade was successful, so rename the DB back.
        utils.renameDB(DB_NAME_TEMP, basedefs.DB_NAME)

        # Update rpm version in vdc options
        utils.updateVDCOption("ProductRPMVersion", utils.getRpmVersion(basedefs.ENGINE_RPM_NAME))
    except:
        # Upgrade failed! we need to restore the old db
        logging.debug("DB upgrade failed, restoring it to a previous state. DB was backed up to %s", dbBackupFile)
        utils.restoreDB(getDbAdminUser(), getDbHostName(), getDbPort(), dbBackupFile)

        # Delete the original DB.
        # TODO: handle the case of failure - it should not stop the flow, but should write to the log
        sqlQuery="DROP DATABASE %s" % DB_NAME_TEMP
        utils.execRemoteSqlCommand(getDbAdminUser(), \
                                   getDbHostName(), \
                                   getDbPort(), \
                                   basedefs.DB_POSTGRES, \
                                   sqlQuery, False, \
                                   output_messages.ERR_DB_DROP % DB_NAME_TEMP)

        raise Exception(output_messages.ERR_DB_UPGRADE_FAILED)

def _updateDefaultDCType():
    logging.debug("updating default data center storage type")
    newDcTypeNum = controller.CONF["DC_TYPE_ENUM"].parse(str.upper(controller.CONF["DC_TYPE"]))
    sqlQuery = "select inst_update_default_storage_pool_type (%s)" % newDcTypeNum
    utils.execRemoteSqlCommand(getDbAdminUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_EXP_UPD_DC_TYPE%(basedefs.DB_NAME))

def _updateVDCOptions():
    logging.debug("updating vdc options..")

    #some options must be set before others in order for the keystore passwords to be set encrypted
    #since python doesn't iterate on the dict in a sorted order, we must seperate them to different dicts
    #1st we update the keystore and CA related paths, only then we can set the passwords and the rest options
    options = (
        {
            "CABaseDirectory":"/etc/pki/ovirt-engine",
            "keystoreUrl":"/etc/pki/ovirt-engine/.keystore",
            "CertificateFileName":"/etc/pki/ovirt-engine/certs/engine.cer",
            "CAEngineKey":"/etc/pki/ovirt-engine/private/ca.pem",
            "TruststoreUrl":"/etc/pki/ovirt-engine/.keystore",
            "ENGINEEARLib":"/usr/share/jboss-as/standalone/deployments/engine.ear",
            "CACertificatePath":"/etc/pki/ovirt-engine/ca.pem",
            "CertAlias":"engine",
        },
        {
            "TruststorePass":basedefs.CONST_CA_PASS,
            "keystorePass":basedefs.CONST_CA_PASS,
            "CertificatePassword":basedefs.CONST_CA_PASS,
            "LocalAdminPassword":controller.CONF["AUTH_PASS"],
            "SSLEnabled": "true",
            "UseSecureConnectionWithServers": "true",
            "ScriptsPath":"/usr/share/ovirt-engine",
            "VdcBootStrapUrl":"http://" + controller.CONF["HOST_FQDN"] + ":" + controller.CONF["HTTP_PORT"] + "/Components/vds/",
            "AsyncPollingCyclesBeforeCallbackCleanup":"30",
            "SysPrepXPPath":"/usr/share/ovirt-engine/sysprep/sysprep.xp",
            "SysPrep2K3Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k3",
            "SysPrep2K8Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8x86",
            "SysPrep2K8x64Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8",
            "SysPrep2K8R2Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8",
            "SysPrepWindows7Path":"/usr/share/ovirt-engine/sysprep/sysprep.w7",
            "SysPrepWindows7x64Path":"/usr/share/ovirt-engine/sysprep/sysprep.w7x64",
            "MacPoolRanges":controller.CONF["MAC_RANGE"],
            "InstallVds":"true",
            "ConfigDir":"/etc/ovirt-engine",
            "DataDir":"/usr/share/ovirt-engine",
            "SignScriptName":"SignReq.sh",
            "BootstrapInstallerFileName":"/usr/share/ovirt-engine/scripts/vds_installer.py",
            "PublicURLPort":controller.CONF["HTTP_PORT"],
            "VirtualMachineDomainName":controller.CONF["HOST_FQDN"],
            "OrganizationName":controller.CONF["ORG_NAME"],
            "ProductRPMVersion":utils.getRpmVersion(basedefs.ENGINE_RPM_NAME),
            "AdminPassword":controller.CONF["AUTH_PASS"]
        }
    )

    try:
        if (os.path.exists(basedefs.FILE_RHEVM_CONFIG_BIN)):
            if (os.path.exists(basedefs.FILE_RHEVM_EXTENDED_CONF)):
                #1st iterate on the CA related options
                for subDict in options:
                    for key in subDict:
                        utils.updateVDCOption(key, subDict[key], masked_value_set)

                logging.debug("finished updating vdc options")
            else:
                raise Exception(output_messages.ERR_CANT_FIND_VDC_OPTION_FILE%(basedefs.FILE_RHEVM_EXTENDED_CONF))
        else:
            raise Exception(output_messages.ERR_CANT_FIND_RHEVM_CONFIG_FILE%(basedefs.FILE_RHEVM_CONFIG_BIN))

    except:
        raise Exception(output_messages.ERR_FAILED_UPD_VDC_OPTIONS%(sys.exc_info()[1]))

def _getVDCOption(key):
    #running rhevm-config to get values per key
    cmd = "/bin/sh %s -g %s --cver=%s -p %s" % (basedefs.FILE_RHEVM_CONFIG_BIN, key, basedefs.VDC_OPTION_CVER, basedefs.FILE_RHEVM_EXTENDED_CONF)
    logging.debug("getting vdc option %s" % key)
    msg = output_messages.ERR_EXP_GET_VDC_OPTION % key
    output, rc = utils.execExternalCmd(cmd, True, msg, masked_value_set)
    logging.debug("Value of %s is %s" % (key, output))
    return output

def getDbAdminUser():
    """
    Check whether db admin user was provided during interactive setup.
    If it was - use the provided value, if not, use default (basedefs.DB_ADMIN)
    """
    if "DB_ADMIN" in controller.CONF.keys():
            return controller.CONF["DB_ADMIN"]

    return basedefs.DB_ADMIN

def getDbHostName():
    """
    Get the host name for the DB.
    """

    if "DB_HOST" in controller.CONF.keys():
            return controller.CONF["DB_HOST"]

    return basedefs.DB_HOST

def getDbPort():
    """
    Get the db port
    """

    if "DB_PORT" in controller.CONF.keys():
            return controller.CONF["DB_PORT"]

    return basedefs.DB_PORT

def _updatePgPassFile():
    """
    Create ~/.pgpass file with
    all relevant info to connect via md5

    file syntax:
    hostname:port:database:username:password
    to allow access to any db - write '*' instead of database
    """
    try:
        #backup existing .pgpass
        if (os.path.exists(basedefs.DB_PASS_FILE)):
            backupFile = "%s.%s" % (basedefs.DB_PASS_FILE, utils.getCurrentDateTime())
            logging.debug("found existing pgpass file, backing current to %s" % (backupFile))
            os.rename(basedefs.DB_PASS_FILE, backupFile)

        pgPassFile = open (basedefs.DB_PASS_FILE, "w")

        pgPassFile.write("#####  oVirt-engine DB ADMIN settings section. Do not change!!"+"\n")
        #insert line for postgres - db admin
        #(very important for maintance and upgrades in case user rhevm is not created yet).
        # Use parameters received from the user and skip if the install is local
        if "DB_ADMIN" in controller.CONF.keys():
            logging.info("Using db credentials provided by the user")
            line_admin = _updatePgPassLine(controller.CONF["DB_HOST"], controller.CONF["DB_PORT"],"*",
                                          controller.CONF["DB_ADMIN"], controller.CONF["DB_PASS"])

            #insert line for user  ('engine' by default)
            line_user = _updatePgPassLine(controller.CONF["DB_HOST"], controller.CONF["DB_PORT"],
                                          basedefs.DB_NAME, basedefs.DB_USER, controller.CONF["DB_PASS"])
        else:
            logging.info("Using default db credentials")
            line_admin = _updatePgPassLine(controller.CONF["DB_HOST"], basedefs.DB_PORT, "*", basedefs.DB_ADMIN, controller.CONF["DB_PASS"])
            line_user = _updatePgPassLine(controller.CONF["DB_HOST"], basedefs.DB_PORT, basedefs.DB_NAME, basedefs.DB_USER, controller.CONF["DB_PASS"])

        pgPassFile.write(line_admin + "\n")
        pgPassFile.write(line_user + "\n")
        pgPassFile.write("#####  End of oVirt-engine DB ADMIN settings section."+"\n")
        pgPassFile.close()

        #make sure the file has still 0600 mod
        os.chmod(basedefs.DB_PASS_FILE, 0600)

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_UPD_DB_PASS)


def _updatePgPassLine(host, port, db, user, password):
    pgData = basedefs.PGPASS_FILE_TEMPLATE
    pgData = pgData.replace("hostname", host)
    pgData = pgData.replace("port",     port)
    pgData = pgData.replace("database", db)
    pgData = pgData.replace("username", user)
    pgData = pgData.replace("password", password)

    return pgData

def _encryptDBPass():
    """
    Encryptes the jboss postgres db password
    and store it in conf
    """
    #run encrypt tool on user give password
    if (os.path.exists(basedefs.EXEC_ENCRYPT_PASS)):
        cmd = [basedefs.EXEC_ENCRYPT_PASS, controller.CONF["DB_PASS"]]

        # The encrypt tool needs the jboss home env set
        # Since we cant use the bash way, we need to set it as environ
        os.environ["JBOSS_HOME"] = basedefs.DIR_JBOSS
        output, rc = utils.execCmd(cmd, None, True, output_messages.ERR_EXP_ENCRYPT_PASS, masked_value_set)

        #parse the encrypted password from the tool
        controller.CONF["ENCRYPTED_DB_PASS"] = utils.parseStrRegex(output, "Encoded password:\s*(.+)", output_messages.ERR_EXP_PARSING_ENCRYPT_PASS)
    else:
        raise Exception(output_messages.ERR_ENCRYPT_TOOL_NOT_FOUND)

def _verifyUserPermissions():
    username = pwd.getpwuid(os.getuid())[0]
    if os.geteuid() != 0:
        sys.exit(output_messages.ERR_EXP_INVALID_PERM%(username))

def _addDefaultsToMaskedValueSet():
    """
    For every param in conf_params
    that has MASK_INPUT enabled keep the default value
    in the 'masked_value_set'
    """
    global masked_value_set
    for group in controller.getAllGroups():
        for param in group.getAllParams():
            # Keep default password values masked, but ignore default empty values
            if ((param.getKey("MASK_INPUT") == True) and param.getKey("DEFAULT_VALUE") != ""):
                masked_value_set.add(param.getKey("DEFAULT_VALUE"))

    # Add deault consts we want to mask
    # TODO: add future consts to mask here
    masked_value_set.add(basedefs.CONST_CA_PASS)

def _updateMaskedValueSet():
    """
    For every param in conf
    has MASK_INPUT enabled keep the user input
    in the 'masked_value_set'
    """
    global masked_value_set
    for confName in controller.CONF:
        # Add all needed values to masked_value_set
        if (controller.getParamKeyValue(confName, "MASK_INPUT") == True):
            masked_value_set.add(controller.CONF[confName])

def mask(input):
    """
    Gets a dict/list/str and search maksked values in them.
    The list of masked values in is masked_value_set and is updated
    via the user input
    If it finds, it replaces them with '********'
    """
    output = copy.deepcopy(input)
    if type(input) == types.DictType:
        for key in input:
            if type(input[key]) == types.StringType:
                output[key] = maskString(input[key])
    if type(input) == types.ListType:
        for item in input:
            org = item
            orgIndex = input.index(org)
            if type(item) == types.StringType:
                item = maskString(item)
            if item != org:
                output.remove(org)
                output.insert(orgIndex, item)
    if type(input) == types.StringType:
            output = maskString(input)

    return output

def removeMaskString(maskedString):
    """
    remove an element from masked_value_set
    we need to itterate over the set since
    calling set.remove() on an string that does not exit
    will raise an exception
    """
    global masked_value_set
    # Since we cannot remove an item from a set during itteration over
    # the said set, we only mark a flag and if the flag is set to True
    # we remove the string from the set.
    found = False
    for item in masked_value_set:
        if item == maskedString:
            found = True
    if found:
        masked_value_set.remove(maskedString)

def maskString(str):
    # Iterate sorted list, so we won't mask only part of a password
    for password in sorted(masked_value_set, utils.byLength, None, True):
        str = str.replace(password, '*'*8)
    return str

def _validateParamValue(param, paramValue):
    validateFunc = param.getKey("VALIDATION_FUNC")
    optionsList  = param.getKey("OPTION_LIST")
    logging.debug("validating param %s in answer file." % param.getKey("CONF_NAME"))
    if not validateFunc(paramValue, optionsList):
        raise Exception(output_messages.ERR_EXP_VALIDATE_PARAM % param.getKey("CONF_NAME"))

def _handleGroupCondition(config, conditionName, conditionValue):
    """
    handle params group pre/post condition
    checks if a group has a pre/post condition
    and validates the params related to the group
    """

    # If the post condition is a function
    if type(conditionName) == types.FunctionType:
        # Call the function conditionName with conf as the arg
        conditionValue = conditionName(controller.CONF)

    # If the condition is a string - just read it to global conf
    # We assume that if we get a string as a member it is the name of a member of conf_params
    elif type(conditionName) == types.StringType:
        conditionValue = _loadParamFromFile(config, "general", conditionName)
    else:
        # Any other type is invalid
        raise TypeError("%s type (%s) is not supported" % (conditionName, type(conditionName)))

    return conditionValue

def _loadParamFromFile(config, section, paramName):
    """
    read param from file
    validate it
    and load to to global conf dict
    """

    # Get paramName from answer file
    value = config.get(section, paramName)

    # Validate param value using its validation func
    param = controller.getParamByName(paramName)
    _validateParamValue(param, value)

    # Keep param value in our never ending global conf
    controller.CONF[param.getKey("CONF_NAME")] = value

    return value

def _handleAnswerFileParams(answerFile):
    """
    handle loading and validating
    params from answer file
    supports reading single or group params
    """
    try:
        logging.debug("Starting to handle config file")

        # Read answer file
        fconf = ConfigParser.ConfigParser()
        fconf.read(answerFile)

        # Iterate all the groups and check the pre/post conditions
        for group in controller.getAllGroups():
            # Get all params per group

            # Handle pre conditions for group
            preConditionValue = True
            if group.getKey("PRE_CONDITION"):
                preConditionValue = _handleGroupCondition(fconf, group.getKey("PRE_CONDITION"), preConditionValue)

            # Handle pre condition match with case insensitive values
            logging.info("Comparing pre- conditions, value: '%s', and match: '%s'" % (preConditionValue, group.getKey("PRE_CONDITION_MATCH")))
            if utils.compareStrIgnoreCase(preConditionValue, group.getKey("PRE_CONDITION_MATCH")):
                for param in group.getAllParams():
                    _loadParamFromFile(fconf, "general", param.getKey("CONF_NAME"))

                # Handle post conditions for group only if pre condition passed
                postConditionValue = True
                if group.getKey("POST_CONDITION"):
                    postConditionValue = _handleGroupCondition(fconf, group.getKey("POST_CONDITION"), postConditionValue)

                    # Handle post condition match for group
                    if not utils.compareStrIgnoreCase(postConditionValue, group.getKey("POST_CONDITION_MATCH")):
                        logging.error("The group condition (%s) returned: %s, which differs from the excpeted output: %s"%\
                                      (group.getKey("GROUP_NAME"), postConditionValue, group.getKey("POST_CONDITION_MATCH")))
                        raise ValueError(output_messages.ERR_EXP_GROUP_VALIDATION_ANS_FILE%\
                                         (group.getKey("GROUP_NAME"), postConditionValue, group("POST_CONDITION_MATCH")))
                    else:
                        logging.debug("condition (%s) passed" % group.getKey("POST_CONDITION"))
                else:
                    logging.debug("no post condition check for group %s" % group.getKey("GROUP_NAME"))
            else:
                logging.debug("skipping params group %s since value of group validation is %s" % (group.getKey("GROUP_NAME"), preConditionValue))

    except Exception as e:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_HANDLE_ANSWER_FILE%(e))

def _handleInteractiveParams():
    try:
        for group in controller.getAllGroups():
            preConditionValue = True
            logging.debug("going over group %s" % group.getKey("GROUP_NAME"))

            # If pre_condition is set, get Value
            if group.getKey("PRE_CONDITION"):
                preConditionValue = _getConditionValue(group.getKey("PRE_CONDITION"))

            inputLoop = True

            # If we have a match, i.e. condition returned True, go over all params in the group
            logging.info("Comparing pre-conditions; condition: '%s', and match: '%s'" % (preConditionValue, group.getKey("PRE_CONDITION_MATCH")))
            if utils.compareStrIgnoreCase(preConditionValue, group.getKey("PRE_CONDITION_MATCH")):
                while inputLoop:
                    for param in group.getAllParams():
                        if not param.getKey("CONDITION"):
                            input_param(param)
                            #update password list, so we know to mask them
                            _updateMaskedValueSet()

                    postConditionValue = True

                    # If group has a post condition, we check it after we get the input from
                    # all the params in the group. if the condition returns False, we loop over the group again
                    if group.getKey("POST_CONDITION"):
                        postConditionValue = _getConditionValue(group.getKey("POST_CONDITION"))

                        if postConditionValue == group.getKey("POST_CONDITION_MATCH"):
                            inputLoop = False
                        else:
                            #we clear the value of all params in the group
                            #in order to re-input them by the user
                            for param in group.getAllParams():
                                if controller.CONF.has_key(param.getKey("CONF_NAME")):
                                    del controller.CONF[param.getKey("CONF_NAME")]
                                if commandLineValues.has_key(param.getKey("CONF_NAME")):
                                    del commandLineValues[param.getKey("CONF_NAME")]
                    else:
                        inputLoop = False
            else:
                logging.debug("no post condition check for group %s" % group.getKey("GROUP_NAME"))

        _displaySummary()
    except KeyboardInterrupt:
        logging.error("keyboard interrupt caught")
        raise Exception(output_messages.ERR_EXP_KEYBOARD_INTERRUPT)
    except Exception:
        logging.error(traceback.format_exc())
        raise
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_HANDLE_PARAMS)


def _handleParams(configFile):
    _addDefaultsToMaskedValueSet()
    if configFile:
        _handleAnswerFileParams(configFile)
    else:
        _handleInteractiveParams()

def _getConditionValue(matchMember):
    returnValue = False
    if type(matchMember) == types.FunctionType:
        returnValue = matchMember(controller.CONF)
    elif type(matchMember) == types.StringType:
        #we assume that if we get a string as a member it is the name
        #of a member of conf_params
        if not controller.CONF.has_key(matchMember):
            param = controller.getParamByName(matchMember)
            input_param(param)
        returnValue = controller.CONF[matchMember]
    else:
        raise TypeError("%s type (%s) is not supported"%(matchMember, type(matchMember)))

    return returnValue

def _displaySummary():

    print output_messages.INFO_DSPLY_PARAMS
    print  "=" * (len(output_messages.INFO_DSPLY_PARAMS) - 1)
    logging.info("*** User input summary ***")
    for group in controller.getAllGroups():
        for param in group.getAllParams():
            if not param.getKey("USE_DEFAULT") and controller.CONF.has_key(param.getKey("CONF_NAME")):
                cmdOption = param.getKey("CMD_OPTION")
                l = 30 - len(cmdOption)
                maskParam = param.getKey("MASK_INPUT")
                # Only call mask on a value if the param has MASK_INPUT set to True
                if maskParam:
                    logging.info("%s: %s" % (cmdOption, mask(controller.CONF[param.getKey("CONF_NAME")])))
                    print "%s:" % (cmdOption) + " " * l + mask(controller.CONF[param.getKey("CONF_NAME")])
                else:
                    # Otherwise, log & display it as it is
                    logging.info("%s: %s" % (cmdOption, controller.CONF[param.getKey("CONF_NAME")]))
                    print "%s:" % (cmdOption) + " " * l + controller.CONF[param.getKey("CONF_NAME")]
    logging.info("*** User input summary ***")
    answer = _askYesNo(output_messages.INFO_USE_PARAMS)
    if not answer:
        logging.debug("user chose to re-enter the user parameters")
        for group in controller.getAllGroups():
            for param in group.getAllParams():
                if controller.CONF.has_key(param.getKey("CONF_NAME")):
                    if not param.getKey("MASK_INPUT"):
                        param.setKey("DEFAULT_VALUE", controller.CONF[param.getKey("CONF_NAME")])
                    # Remove the string from mask_value_set in order
                    # to remove values that might be over overwritten.
                    removeMaskString(controller.CONF[param.getKey("CONF_NAME")])
                    del controller.CONF[param.getKey("CONF_NAME")]
                if commandLineValues.has_key(param.getKey("CONF_NAME")):
                    del commandLineValues[param.getKey("CONF_NAME")]
            print ""
        logging.debug("calling handleParams in interactive mode")
        return _handleParams(None)
    else:
        logging.debug("user chose to accept user parameters")

def _startJboss():
    logging.debug("using chkconfig to enable jboss to load on system startup.")
    srv = utils.Service(basedefs.JBOSS_SERVICE_NAME)
    srv.autoStart(True)
    srv.stop(True)
    srv.start(True)

def _configNfsShare():
    #ISO_DOMAIN_NAME, NFS_MP
    try:
        logging.debug("configuring NFS share")

        # If path does not exist, create it
        if not os.path.exists(controller.CONF["NFS_MP"]):
            logging.debug("creating directory %s " % (controller.CONF["NFS_MP"]))
            os.makedirs(controller.CONF["NFS_MP"])
        # Add export to exportfs
        nfsutils.addNfsExport(controller.CONF["NFS_MP"], (("0.0.0.0", "0.0.0.0", ("rw",)),), "rhev installer")

        # Add warning to user about nfs export permissions
        controller.MESSAGES.append(output_messages.WARN_ISO_DOMAIN_SECURITY % (controller.CONF["NFS_MP"]))

        # Set selinux configuration
        nfsutils.setSELinuxContextForDir(controller.CONF["NFS_MP"], nfsutils.SELINUX_RW_LABEL)

        #set NFS/portmap ports by overriding /etc/sysconfig/nfs
        backupFile = "%s.%s_%s"%(basedefs.FILE_NFS_BACKUP, time.strftime("%H%M%S-%m%d%Y"), os.getpid())
        utils.copyFile("%s/nfs"%(basedefs.DIR_ETC_SYSCONFIG), backupFile)
        utils.copyFile(basedefs.FILE_NFS_SYSCONFIG, "%s/nfs"%(basedefs.DIR_ETC_SYSCONFIG))

        # Start services
        _startNfsServices()

        # Generate the UUID for the isodomain
        controller.CONF["sd_uuid"] = nfsutils.generateUUID()

        # Create ISO domain
        nfsutils.createISODomain(controller.CONF["NFS_MP"], controller.CONF["ISO_DOMAIN_NAME"], controller.CONF["sd_uuid"])

        # Add iso domain to DB
        _addIsoDomaintoDB(controller.CONF["sd_uuid"], controller.CONF["ISO_DOMAIN_NAME"])

        # Refresh nfs exports
        nfsutils.refreshNfsExports()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_FAILED_CFG_NFS_SHARE)

def setMaxSharedMemory():
    """
    Check and verify that the kernel.shmmax kernel parameter is above 35mb
    """
    # First verify that kernel.shmmax is not set and is below the requested value.
    logging.debug("loading %s", basedefs.FILE_SYSCTL)
    txtHandler = utils.TextConfigFileHandler(basedefs.FILE_SYSCTL)
    txtHandler.open()

    # Compare to basedefs.CONST_SHMMAX
    currentShmmax = txtHandler.getParam("kernel.shmmax")
    if currentShmmax and (int(currentShmmax) >= basedefs.CONST_SHMMAX):
        logging.debug("current shared memory max in kernel is %s, there is no need to update the kernel parameters", currentShmmax)
        return

    # If we got here, it means we need to update kernel.shmmax in sysctl.conf
    logging.debug("setting SHARED MEMORY MAX to: %s", basedefs.CONST_SHMMAX)
    txtHandler.editParam("kernel.shmmax", basedefs.CONST_SHMMAX)
    txtHandler.close()

    # Execute sysctl -a
    utils.execExternalCmd("%s -e -p" % basedefs.EXEC_SYSCTL, True, output_messages.ERR_EXP_FAILED_KERNEL_PARAMS)

def _addIsoDomaintoDB(uuid, description):
    logging.debug("Adding iso domain into DB")
    sqlQuery = "select inst_add_iso_storage_domain ('%s', '%s', '%s:%s', %s, %s)" % (uuid, description, controller.CONF["HOST_FQDN"], controller.CONF["NFS_MP"], 0, 0)
    utils.execRemoteSqlCommand(getDbAdminUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_FAILED_INSERT_ISO_DOMAIN%(basedefs.DB_NAME))

def _startNfsServices():
    logging.debug("Enabling the rpcbind & nfs services")
    try:
        for service in ["rpcbind", basedefs.NFS_SERVICE_NAME]:
            srv = utils.Service(service)
            srv.autoStart(True)
            srv.stop(False)
            srv.start(True)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_FAILED_TO_START_NFS_SERVICE)

def _loadFilesToIsoDomain():
    """
    Load files (iso,vfd) from existing rpms
    to the configured nfs shared domain
    """
    #TODO: add more iso files in the future
    fileList = [basedefs.FILE_VIRTIO_WIN_VFD, basedefs.FILE_VIRTIO_WIN_ISO, basedefs.FILE_RHEV_GUEST_TOOLS_ISO]

    # Prepare the full path for the iso files
    targetPath = os.path.join(controller.CONF["NFS_MP"], controller.CONF["sd_uuid"], "images", "11111111-1111-1111-1111-111111111111")

    try:
        # Iterate the list and copy all the files
        for filename in fileList:
            utils.copyFile(filename, targetPath, basedefs.CONST_VDSM_UID, basedefs.CONST_KVM_GID)
    except:
        # We don't want to fail the setup, just log the error
        logging.error(traceback.format_exc())
        logging.error(output_messages.ERR_FAILED_TO_COPY_FILE_TO_ISO_DOMAIN)

def _printAdditionalMessages():
    print "\n",output_messages.INFO_ADDTIONAL_MSG
    for msg in controller.MESSAGES:
        logging.info(output_messages.INFO_ADDTIONAL_MSG_BULLET%(msg))
        print output_messages.INFO_ADDTIONAL_MSG_BULLET%(msg)

def _addFinalInfoMsg():
    """
    add info msg to the user finalizing the
    successfull install of rhemv
    """
    controller.MESSAGES.append(output_messages.INFO_LOG_FILE_PATH%(logFile))
    controller.MESSAGES.append(output_messages.INFO_LOGIN_USER)
    controller.MESSAGES.append(output_messages.INFO_ADD_USERS)
    controller.MESSAGES.append(output_messages.INFO_RHEVM_URL % controller.CONF["HTTP_URL"])

def _checkJbossService(configFile):
    logging.debug("checking the status of jboss")
    jservice = utils.Service(basedefs.JBOSS_SERVICE_NAME)
    output, rc = jservice.status()
    if 0 == rc:
        logging.debug("jboss is up and running")

        #if we don't use an answer file, we need to ask the user if to stop jboss
        if not configFile:
            print output_messages.INFO_NEED_STOP_JBOSS
            answer = _askYesNo(output_messages.INFO_Q_STOP_JBOSS)
            if answer:
                print output_messages.INFO_STOP_JBOSS,
                jservice.stop(True)
            else:
                logging.debug("User chose not to stop jboss")
                return False
        else:
            #we stop the jboss service on a silent install
            print output_messages.INFO_STOP_JBOSS,
            jservice.stop(True)
    return True

def _lockRpmVersion():
    """
    Enters rpm versions into yum version-lock
    """
    logging.debug("Locking rpms in yum-version-lock")
    cmd = [basedefs.EXEC_RPM, "-q"] + basedefs.RPM_LOCK_LIST.split()
    output, rc = utils.execCmd(cmd, None, True, output_messages.ERR_YUM_LOCK)

    with open(basedefs.FILE_YUM_VERSION_LOCK, "a") as f:
        for rpm in output.splitlines():
            f.write(rpm + "\n")

def editPostgresConf():
    """
    edit /var/lib/pgsql/data/postgresql.conf and change max_connections to 150
    """
    try:
        tempFile = tempfile.mktemp(dir="/tmp")

        logging.debug("copying %s over %s", basedefs.FILE_PSQL_CONF, tempFile)
        shutil.copy2(basedefs.FILE_PSQL_CONF, tempFile)

        handler = utils.TextConfigFileHandler(tempFile)
        handler.open()
        handler.editParam("max_connections", basedefs.CONST_MAX_PSQL_CONNS)
        handler.close()

        logging.debug("copying temp file over original file")
        shutil.copy2(tempFile, basedefs.FILE_PSQL_CONF)

        logging.debug("setting permissions & file ownership")
        os.chown(basedefs.FILE_PSQL_CONF, utils.getUsernameId("postgres"), utils.getGroupId("postgres"))
        os.chmod(basedefs.FILE_PSQL_CONF, 0600)

        logging.debug("removing tempoarary file")
        os.remove(tempFile)

    except:
        logging.error("Failed editing %s" % basedefs.FILE_PSQL_CONF)
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_EDIT_PSQL_CONF)

def _editToolsConfFile():
    """
    add the user & host:secrue_port values to logcollector.conf and isouploader.conf
    """
    for confFile in [basedefs.FILE_LOGCOLLECTOR_CONF, basedefs.FILE_ISOUPLOADER_CONF]:
        if os.path.isfile(confFile):
            logging.debug("Editing %s" % confFile)
            fileHandler = utils.TextConfigFileHandler(confFile)
            fileHandler.open()

            logging.debug("Adding host & secure port")
            fileHandler.editParam("engine", "%s:%s" % (controller.CONF["HOST_FQDN"], controller.CONF["HTTPS_PORT"]))

            logging.debug("Adding username")
            fileHandler.editParam("user", "%s@%s" % (basedefs.INTERNAL_ADMIN, basedefs.INTERNAL_DOMAIN))

            fileHandler.close()
        else:
            logging.debug("Could not find %s" % confFile)

def updateFileDescriptors():
    """
    edit /etc/security/limit.conf and make sure that the File descriptors for jboss
    are 65k
    """
    try:
        logging.debug("opening %s", basedefs.FILE_LIMITS_CONF)
        tempFile = tempfile.mktemp(dir="/tmp")
        shutil.copy2(basedefs.FILE_LIMITS_CONF, tempFile)

        with open(tempFile,"r") as confFile:
            confFileContent = confFile.readlines()

        limitsDict = { 'soft' : False,
                      'hard' : False
                    }

        with open(tempFile, "w") as outFile:
            logging.debug("itterating over file content")
            for line in confFileContent:
                line = line.strip()
                lineToWrite = line

                # If the lines contains nofile (i.e. the directive that specified the amount
                # of allowed open file descriptors and is not a comment
                if re.search("\s+nofile\s+", line) and not re.search("^#", line):
                    # Look for both hard & soft limits.
                    for key in limitsDict:
                        found = re.search("jboss\s+%s\s+nofile\s+(\d+)" % key, line)
                        if found:
                            logging.debug("Current %s limit of file descriptors for jboss is %s", key, found.group(1))
                            limitsDict[key] = found.group(1)

                            if limitsDict[key] != basedefs.CONST_FD_OPEN:
                                logging.debug("Changing %s limit of nofiles to %s", key, str(basedefs.CONST_FD_OPEN))
                                lineToWrite = basedefs.CONST_FD_LINE % (key, basedefs.CONST_FD_OPEN)
                            else:
                                logging.debug("Leaving %s line of nofiles as it is", key)

                # Write Line to file
                outFile.write("%s%s" % (lineToWrite, os.linesep))

            # If we did not find any line matching the nofiles soft/hard limit for jboss, add them now
            for key in limitsDict:
                if not limitsDict[key]:
                    logging.debug("Adding %s limit to file", key)
                    outFile.write("%s%s" % (basedefs.CONST_FD_LINE % (key, basedefs.CONST_FD_OPEN), os.linesep))

        logging.debug("copying %s over %s", tempFile, basedefs.FILE_LIMITS_CONF)
        shutil.copy2(tempFile, basedefs.FILE_LIMITS_CONF)

    except:
        logging.error("Failed to edit %s", basedefs.FILE_LIMITS_CONF)
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_LIMITS)

    finally:
        logging.debug("removing %s", tempFile)
        os.remove(tempFile)

def _summaryParamsToLog():
    if len(controller.CONF) > 0:
        logging.debug("*** The following params were used as user input:")
        for group in controller.getAllGroups():
            for param in group.getAllParams():
                if controller.CONF.has_key(param.getKey("CONF_NAME")):
                    maskedValue = mask(controller.CONF[param.getKey("CONF_NAME")])
                    logging.debug("%s: %s" % (param.getKey("CMD_OPTION"), maskedValue ))

def _isDbAlreadyInstalled():
    logging.debug("Checking if db is already installed..")
    logging.debug("Checking the presence of .pgpass file")
    if not os.path.exists(basedefs.DB_PASS_FILE):
        logging.debug(".pgpass file was not found. Considering this a first installation")
        return False

    # Else, let's check the DB itself
    (out, rc) = utils.execRemoteSqlCommand(getDbAdminUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, "select 1")
    if (rc != 0):
        return False
    else:
        return True

def stopRhevmDbRelatedServices():
    """
    shut down etl and notifier services
    in order to disconnect any open sessions to the db
    """
    # If the rhevm-etl service is installed, then try and stop it.

    if etlService.isServiceAvailable():
        try:
            etlService.stop(True)
        except:
            logging.warn("Failed to stop rhevm-etl")
            logging.warn(traceback.format_exc())
            controller.MESSAGES.append(output_messages.ERR_FAILED_STOP_SERVICE % "rhevm-etl")

    # If the rhevm-notifierd service is up, then try and stop it.
    if notificationService.isServiceAvailable():
        try:
            (status, rc) = notificationService.status()
            if utils.verifyStringFormat(status, ".*running.*"):
                logging.debug("stopping rhevm-notifierd service..")
                notificationService.stop()
        except:
            logging.warn("Failed to stop rhevm-notifierd service")
            logging.warn(traceback.format_exc())
            controller.MESSAGES.append(output_messages.ERR_FAILED_STOP_SERVICE % "rhevm-notiferd")

    (output, rc) = etlService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-etl")
        controller.MESSAGES.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-etl")

    (output, rc) = notificationService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-notifierd")
        controller.MESSAGES.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-notifierd")


def deployJbossModules():
    """
    deploy the postgres module and edit the xml for the jdk module
    """
    try:
        # Copy module(s) from /usr/share/ovirt-engine/resources/jboss/modules
        logging.debug("Adding modules to jboss's modules")
        modules = [ { 'target' : "%s/org/postgresql" % basedefs.DIR_MODULES_SRC,
                      'link'   : "%s/org/postgresql" % basedefs.DIR_MODULES_DEST}
                  ]
        for module in modules:
            utils.replaceWithLink(module['target'], module['link'])

        # edit module.xml for the jdk module
        backupFile = "%s.%i" % (basedefs.FILE_JDK_MODULE_XML, random.randint(1000000,9999999))
        editFile = "%s.%s.%i" % (basedefs.FILE_JDK_MODULE_XML, "EDIT", random.randint(1000000,9999999))
        logging.debug("Backing up %s into %s", basedefs.FILE_JDK_MODULE_XML, backupFile)
        utils.copyFile(basedefs.FILE_JDK_MODULE_XML, backupFile)
        utils.copyFile(basedefs.FILE_JDK_MODULE_XML, editFile)

        logging.debug("loading xml file handler")
        xmlObj = utils.XMLConfigFileHandler(editFile)
        xmlObj.open()

        logging.debug("registering name space")
        xmlObj.registerNs('module','urn:jboss:module:1.1')

        paths = ['''<path name="sun/security"/>''', '''<path name="sun/security/krb5"/>''', '''<path name="com/sun/jndi/url"/>''', '''<path name="com/sun/jndi/url/dns"/>''' ]

        for path in paths:
            logging.debug("adding %s as node", path)
            xmlObj.addNodes("//module:module/module:dependencies/module:system/module:paths", path)

        xmlObj.close()

        shutil.move(editFile, basedefs.FILE_JDK_MODULE_XML)
        logging.debug("JDK module configuration has been saved")

    except:
        logging.error("Failed to deploy modules into jboss")
        logging.error(traceback.format_exc())
        raise output_messages.ERR_EXP_FAILED_DEPLOY_MODULES

def configEncryptedPass():
    """
    push the encrypted password into standalone.xml
    """
    editFile = None
    backupFile = None
    try:
        #1. Backup standalone xml file
        editFile = "%s.%s.%i" % (basedefs.FILE_JBOSS_STANDALONE, "EDIT", random.randint(1000000,9999999))
        logging.debug("Backing up %s into %s", basedefs.FILE_JBOSS_STANDALONE, backupFile)
        utils.copyFile(basedefs.FILE_JBOSS_STANDALONE, editFile)

        #2. Configure the xml file
        logging.debug("loading xml file handler")
        xmlObj = utils.XMLConfigFileHandler(editFile)
        xmlObj.open()

        xmlObj.registerNs('security', 'urn:jboss:domain:security:1.1')

        configJbossSecurity(xmlObj)

        xmlObj.close()

        shutil.move(editFile, basedefs.FILE_JBOSS_STANDALONE)
        os.chown(basedefs.FILE_JBOSS_STANDALONE, utils.getUsernameId("jboss-as"), utils.getGroupId("jboss-as"))
        logging.debug("Jboss configuration has been saved")

    except:
        logging.error("ERROR Editing jboss's configuration file")
        logging.error(traceback.format_exc())
        raise output_messages.ERR_EXP_FAILED_CONFIG_JBOSS

def configJbossXml():
    """
    configure JBoss-AS's stadnalone.xml
    """
    editFile = None
    backupFile = None
    try:
        #1. Backup standalone xml file
        backupFile = "%s.%s.%i" % (basedefs.FILE_JBOSS_STANDALONE, "BACKUP", random.randint(1000000,9999999))
        editFile = "%s.%s.%i" % (basedefs.FILE_JBOSS_STANDALONE, "EDIT", random.randint(1000000,9999999))
        logging.debug("Backing up %s into %s", basedefs.FILE_JBOSS_STANDALONE, backupFile)
        utils.copyFile(basedefs.FILE_JBOSS_STANDALONE, backupFile)
        utils.copyFile(basedefs.FILE_JBOSS_STANDALONE, editFile)

        #2. Configure the xml file
        logging.debug("loading xml file handler")
        xmlObj = utils.XMLConfigFileHandler(editFile)
        xmlObj.open()

        #2a. Register the main domain Namespace
        xmlObj.registerNs('domain','urn:jboss:domain:1.1')

        logging.debug("Configuring Jboss")
        configJbossLogging(xmlObj)
        configJbossDatasource(xmlObj)
        configJbossNetwork(xmlObj)
        configJbossSSL(xmlObj)
        logging.debug("Jboss has been configured")

        xmlObj.close()

        shutil.move(editFile, basedefs.FILE_JBOSS_STANDALONE)
        os.chown(basedefs.FILE_JBOSS_STANDALONE, utils.getUsernameId("jboss-as"), utils.getGroupId("jboss-as"))
        logging.debug("Jboss configuration has been saved")

    except:
        logging.error("ERROR Editing jboss's configuration file")
        logging.error(traceback.format_exc())
        raise output_messages.ERR_EXP_FAILED_CONFIG_JBOSS

def configJbossLogging(xmlObj):
    """
    Configure the Logging for jboss
    """
    logging.debug("Configuring logging for jboss")

    logging.debug("Registering logging namespace")
    xmlObj.registerNs('logging', 'urn:jboss:domain:logging:1.1')

    logging.debug("setting attributes")
    nodes = xmlObj.xpathEval("//logging:subsystem/logging:console-handler[@name='CONSOLE']")
    nodes[0].setProp("autoflush", "true")

    logging.debug("Adding level node with attribute: name, value: INFO")
    nodes = xmlObj.xpathEval("//logging:subsystem/logging:periodic-rotating-file-handler[@name='FILE']")
    nodes[0].setProp("autoflush", "true")


    xmlObj.removeNodes("//logging:subsystem/logging:periodic-rotating-file-handler[@name='FILE']/logging:level")
    levelStr = '''<level name="INFO" />'''
    xmlObj.addNodes("//logging:subsystem/logging:periodic-rotating-file-handler[@name='FILE']", levelStr)

    xmlObj.removeNodes("//logging:subsystem/logging:size-rotating-file-handler[@name='ENGINE_LOG']")
    logging.debug("Adding file handler for ENGINE_LOG")
    fileHandlerStr = '''
    <size-rotating-file-handler name="ENGINE_LOG" autoflush="true">
        <level name="INFO"/>
        <formatter>
            <pattern-formatter pattern="%d %-5p [%c] (%t) %s%E%n"/>
        </formatter>
        <file path="/var/log/ovirt-engine/engine.log"/>
        <rotate-size value="1M"/>
        <max-backup-index value="30"/>
        <append value="true"/>
    </size-rotating-file-handler>
'''
    xmlObj.addNodes("//logging:subsystem", fileHandlerStr)

    logging.debug("Adding Loggers for ovirt-engine")
    loggerCats = ["org.ovirt", "org.ovirt.engine.core.bll", "org.ovirt.engine.core.dal.dbbroker.PostgresDbEngineDialect$PostgresJdbcTemplate","org.springframework.ldap"]
    for loggerCat in loggerCats:
        xmlObj.removeNodes("//logging:subsystem/logging:logger[@category='%s']" % loggerCat)

    loggers = ['''
    <logger category="org.ovirt">
        <level name="INFO"/>
        <handlers>
            <handler name="ENGINE_LOG"/>
        </handlers>
    </logger>
    ''','''
    <logger category="org.ovirt.engine.core.bll">
        <level name="INFO"/>
    </logger>
    ''','''
    <logger category="org.ovirt.engine.core.dal.dbbroker.PostgresDbEngineDialect$PostgresJdbcTemplate">
        <level name="WARN"/>
    </logger>
    ''','''
    <logger category="org.springframework.ldap">
        <level name="ERROR"/>
    </logger>
    ''']

    for logger in loggers:
        xmlObj.addNodes("//logging:subsystem", logger)

    logging.debug("Logging is enabled and configured in jboss's configuration")

def configJbossDatasource(xmlObj):
    """
    configure the datasource for jboss
    """
    logging.debug("Configuring logging for jboss")

    logging.debug("Registering datasource namespaces")
    xmlObj.registerNs('datasource', 'urn:jboss:domain:datasources:1.0')
    xmlObj.registerNs('deployment-scanner', 'urn:jboss:domain:deployment-scanner:1.0')

    logging.debug("looking for ENGINEDatasource datasource")

    # removeNodes will remove the node if it exists and will do nothing if it does not exist
    xmlObj.removeNodes("//datasource:subsystem/datasource:datasources/datasource:datasource[@jndi-name='java:/ENGINEDataSource']")

    secure_conn = ''
    if "DB_SECURE_CONNECTION" in controller.CONF.keys() and controller.CONF["DB_SECURE_CONNECTION"] == "yes":
        secure_conn = '''
            <connection-property name="ssl">
                 true
            </connection-property>
            <connection-property name="sslfactory">
                 org.postgresql.ssl.NonValidatingFactory
            </connection-property>
       '''

    datasourceStr = '''
        <datasource jndi-name="java:/ENGINEDataSource" pool-name="ENGINEDataSource" enabled="true">
        <connection-url>
            jdbc:postgresql://%s:%s/engine
        </connection-url>
        %s
        <driver>
            postgresql
        </driver>
        <transaction-isolation>
            TRANSACTION_READ_COMMITTED
        </transaction-isolation>
        <pool>
            <min-pool-size>
                1
            </min-pool-size>
            <max-pool-size>
                100
            </max-pool-size>
            <prefill>
                true
            </prefill>
        </pool>
        <security>
            <user-name>
                engine
            </user-name>
            <security-domain>
                EncryptDBPassword
            </security-domain>
        </security>
        <statement>
            <prepared-statement-cache-size>
                100
            </prepared-statement-cache-size>
        </statement>
    </datasource>
''' % (getDbHostName(), getDbPort(), secure_conn)
    logging.debug("Adding ENGINE datasource")
    xmlObj.addNodes("//datasource:subsystem/datasource:datasources", datasourceStr)

    logging.debug("Adding drivers to datasource")
    xmlObj.removeNodes("//datasource:subsystem/datasource:datasources/datasource:drivers/datasource:driver[@name='postgresql']")
    driversStr='''
    <drivers>
        <driver name="postgresql" module="org.postgresql">
            <xa-datasource-class>
                org.postgresql.xa.PGXADataSource
            </xa-datasource-class>
        </driver>
    </drivers>
'''
    xmlObj.addNodes("//datasource:subsystem/datasource:datasources", driversStr)

    logging.debug("configuring deployment-scanner")
    node = xmlObj.xpathEval("//deployment-scanner:subsystem/deployment-scanner:deployment-scanner")[0]
    node.setProp("name","default")
    node.setProp("path","deployments")
    node.setProp("scan-enabled","true")
    node.setProp("deployment-timeout","60")
    logging.debug("Datasource has been added into jboss's configuration")

def configJbossSecurity(xmlObj):
    """
    configure security for jboss
    """
    logging.debug("Configuring security for jboss")

    logging.debug("Registering security namespaces")
    xmlObj.registerNs('security', 'urn:jboss:domain:security:1.1')

    xmlObj.removeNodes("//security:subsystem/security:security-domains/security:security-domain[@name='EngineKerberosAuth']")
    securityKerbStr='''
        <security-domain name="EngineKerberosAuth">
            <authentication>
                <login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="required"/>
            </authentication>
        </security-domain>
'''
    xmlObj.addNodes("//security:subsystem/security:security-domains", securityKerbStr)

    xmlObj.removeNodes("//security:subsystem/security:security-domains/security:security-domain[@name='EncryptDBPassword']")
    securityPassStr='''
        <security-domain name="EncryptDBPassword">
            <authentication>
                <login-module code="org.picketbox.datasource.security.SecureIdentityLoginModule" flag="required">
                    <module-option name="username" value="engine"/>
                    <module-option name="password" value="'''+ controller.CONF["ENCRYPTED_DB_PASS"] +'''"/>
                    <module-option name="managedConnectionFactoryName" value="jboss.jca:name=ENGINEDataSource,service=LocalTxCM"/>
                </login-module>
            </authentication>
        </security-domain>
'''
    xmlObj.addNodes("//security:subsystem/security:security-domains", securityPassStr)

#    node = xmlObj.xpathEval("//security:subsystem/security:security-domains/security:security-domain[@name='EncryptDBPassword']/security:authentication/security:login-module/security:module-option[@name='password']")[0]
#    node.setProp('value', controller.CONF["ENCRYPTED_DB_PASS"])

    logging.debug("Security has been configured for jboss")

def configJbossNetwork(xmlObj):
    """
    configure access for the public interface on jboss
    and set the ports for HTTP/S
    """
    logging.debug("Configuring Jboss's network")

    logging.debug("Removing all interfaces from the public interface")
    xmlObj.removeNodes("//domain:server/domain:interfaces/domain:interface[@name='public']/*")

    logging.debug("Adding access to public interface")
    xmlObj.addNodes("//domain:server/domain:interfaces/domain:interface[@name='public']", "<any-address/>")

    logging.debug("Setting ports")
    httpNode = xmlObj.xpathEval("//domain:server/domain:socket-binding-group[@name='standard-sockets']/domain:socket-binding[@name='http']")[0]
    httpNode.setProp("port", controller.CONF["HTTP_PORT"])

    httpsNode = xmlObj.xpathEval("//domain:server/domain:socket-binding-group[@name='standard-sockets']/domain:socket-binding[@name='https']")[0]
    httpsNode.setProp("port", controller.CONF["HTTPS_PORT"])

    logging.debug("Network has been configured for jboss")

def configJbossSSL(xmlObj):
    """
    configure SSL for jboss
    """
    logging.debug("Configuring SSL for jboss")

    logging.debug("Registering web namespaces")
    xmlObj.registerNs('web', 'urn:jboss:domain:web:1.1')
    sslConnectorStr='''
    <connector name="https" protocol="HTTP/1.1" socket-binding="https" scheme="https" enable-lookups="false" secure="true">
        <ssl name="ssl" password="mypass" certificate-key-file="/etc/pki/ovirt-engine/.keystore" protocol="TLSv1" verify-client="false"/>
    </connector>
'''
    xmlObj.removeNodes("//web:subsystem/web:connector[@name='https']")
    xmlObj.addNodes("//web:subsystem", sslConnectorStr)

    logging.debug("Disabling default welcome-content")
    node = xmlObj.xpathEval("//web:subsystem/web:virtual-server[@name='default-host']")[0]
    node.setProp("enable-welcome-root", "false")

    logging.debug("SSL has been configured for jboss")

def startRhevmDbRelatedServices():
    """
    bring back any service we stopped
    we won't start services that are down
    but weren't stopped by us
    """
    (output, rc) = etlService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-etl")
        controller.MESSAGES.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-etl")

    (output, rc) = notificationService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-notifierd")
        controller.MESSAGES.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-notifierd")

def isSecondRun():
    keystore = os.path.join(basedefs.DIR_OVIRT_PKI, ".keystore")
    engineLink = os.path.join(basedefs.DIR_JBOSS, "server", basedefs.JBOSS_PROFILE_NAME,"deploy","engine.ear")

    if os.path.exists(keystore):
        logging.debug("%s exists, second run detected", keystore)
        return True
    elif os.path.exists(engineLink):
        logging.debug("%s exists, second run detected", engineLink)
        return True
    else:
        return False

def runSequences():
    controller.runAllSequences()

def main(configFile=None):
    try:
        logging.debug("Entered main(configFile='%s')"%(configFile))
        print output_messages.INFO_HEADER

        # Handle second excecution warning
        if isSecondRun():
            print output_messages.WARN_SECOND_RUN

            # Ask for user input only on interactive run
            if not configFile and not _askYesNo(output_messages.INFO_PROCEED):
                logging.debug("exiting gracefully")
                print output_messages.INFO_STOP_INSTALL_EXIT
                return 0

        #TODO: add validation to answer file before stopping jboss
        if not _checkJbossService(configFile):
            logging.debug("exiting gracefully")
            print output_messages.INFO_STOP_INSTALL_EXIT
            return 0

        # Get parameters
        _handleParams(configFile)

        # Update masked_value_list with user input values
        _updateMaskedValueSet()

        # Print masked conf
        logging.debug(mask(controller.CONF))

        # Start configuration stage
        logging.debug("Entered Configuration stage")
        print "\n",output_messages.INFO_INSTALL

        # Initialize Sequences
        initSequences()

        initPluginsSequences()

        # We now have all params - update password and host hack.
        # This is a hack as there's no properly defined param named DB_PASS.
        # But we consider it OK, as it happens after handleParams call,
        # which would already have handled our interactive params.
        if "DB_HOST" not in controller.CONF.keys():
            controller.CONF["DB_HOST"] = basedefs.DB_HOST

        for passkey in ("DB_LOCAL_PASS", "DB_REMOTE_PASS"):
            if passkey in controller.CONF.keys():
                controller.CONF["DB_PASS"] = controller.CONF[passkey]
                break

        # Run main setup logic
        runSequences()

        # Lock rhevm version
        _lockRpmVersion()

        # Print info
        _addFinalInfoMsg()
        print output_messages.INFO_INSTALL_SUCCESS
        _printAdditionalMessages()

    finally:
        # Always print user params to log
        _summaryParamsToLog()

def generateAnswerFile(outputFile):
    content = StringIO()
    fd = open(outputFile,"w")
    content.write("[general]%s"%(os.linesep))
    for group in controller.getAllGroups():
        for param in group.getAllParams():
            content.write("%s=%s%s" % (param.getKey("CONF_NAME"), param.getKey("DEFAULT_VALUE"), os.linesep))
    content.seek(0)
    fd.write(content.read())
    os.chmod(outputFile, 0600)

def _checkAvailableMemory():
    """
    checks for memory using the "free" command
    """
    #execute free -g to get output in GB
    logging.debug("checking total memory")
    cmd = "%s -g" % basedefs.EXEC_FREE
    output, rc = utils.execExternalCmd(cmd, True, output_messages.ERR_EXP_FREE_MEM)

    #itterate over output and look for the line: "Mem: 1 something"
    #and extract 1 from it (1 is an example to the free memory)
    availableMemory = 0
    for line in output.split("\n"):
        result = re.match("Mem:\s+(\d+)\s+.+", line)
        if result:
            logging.debug("Found a match, amount of memory: %s" % result.group(1))
            availableMemory = result.group(1)

    #compare found memory to restrictions
    availableMemory = int(availableMemory)
    if availableMemory < basedefs.CONST_MIN_MEMORY_GB:
        logging.error("Availble memory (%s) is lower then the minimum requirments (%s)" % (availableMemory, basedefs.CONST_MIN_MEMORY_GB))
        raise Exception(output_messages.ERR_EXP_NOT_EMOUGH_MEMORY)

    if availableMemory < basedefs.CONST_WARN_MEMORY_GB:
        logging.warn("There is less then %s available memory " % basedefs.CONST_WARN_MEMORY_GB)
        controller.MESSAGES.append(output_messages.WARN_LOW_MEMORY)

def initCmdLineParser():
    """
    Initiate the optparse object, add all the groups and general command line flags
    and returns the optparse object
    """

    # Init parser and all general flags
    logging.debug("initiating command line option parser")
    usage = "usage: %prog [options]"
    parser = OptionParser(usage)
    parser.add_option("--gen-answer-file", help="Generate a template of an answer file, using this option excludes all other option")
    parser.add_option("--answer-file", help="Runs the configuration in none-interactive mode, extracting all information from the \
                                            configuration file. using this option excludes all other option")

    # For each group, create a group option
    for group in controller.getAllGroups():
        groupParser = OptionGroup(parser, group.getKey("DESCRIPTION"))

        for param in group.getAllParams():
            cmdOption = param.getKey("CMD_OPTION")
            paramUsage = param.getKey("USAGE")
            optionsList = param.getKey("OPTION_LIST")
            useDefault = param.getKey("USE_DEFAULT")
            if not useDefault:
                if optionsList:
                    groupParser.add_option("--%s" % cmdOption, metavar=optionsList, help=paramUsage, choices=optionsList)
                else:
                    groupParser.add_option("--%s" % cmdOption, help=paramUsage)

        # Add group parser to main parser
        parser.add_option_group(groupParser)

    return parser

def plugin_compare(x, y):
    """
    Used to sort the plugin file list
    according to the number at the end of the plugin module
    """
    x_match = re.search(".+\_(\d\d\d)", x)
    x_cmp = x_match.group(1)
    y_match = re.search(".+\_(\d\d\d)", y)
    y_cmp = y_match.group(1)
    return int(x_cmp) - int(y_cmp)

def loadPlugins():
    """
    Load All plugins from ./plugins
    """
    sys.path.append(basedefs.DIR_PLUGINS)
    fileList = sorted(os.listdir(basedefs.DIR_PLUGINS), cmp=plugin_compare)
    for item in fileList:
        # Looking for files that end with ###.py, example: a_plugin_100.py
        match = re.search("^(.+\_\d\d\d)\.py$", item)
        if match:
            try:
                moduleToLoad = match.group(1)
                logging.debug("importing module %s, from file %s", moduleToLoad, item)
                moduleobj = __import__(moduleToLoad)
                moduleobj.__file__ = os.path.join(basedefs.DIR_PLUGINS, item)
                globals()[moduleToLoad] = moduleobj
                checkPlugin(moduleobj)
                controller.addPlugin(moduleobj)
            except:
                 logging.error("Failed to load plugin from file %s", item)
                 logging.error(traceback.format_exc())
                 raise Exception("Failed to load plugin from file %s" % item)

def checkPlugin(plugin):
    for funcName in ['initConfig','initSequences']:
        if not hasattr(plugin, funcName):
            raise ImportError("Plugin %s does not contain the %s function" % (plugin.__class__, funcName))

def countCmdLineFlags(options, flag):
    """
    counts all command line flags that were supplied, excluding the supplied flag name
    """
    counter = 0
    # make sure only flag was supplied
    for key, value  in options.__dict__.items():
        if key == flag:
            next
        # If anything but flag was called, increment
        elif value:
            counter += 1

    return counter

def validateSingleFlag(options, flag):
    counter = countCmdLineFlags(options, flag)
    if counter > 0:
        optParser.print_help()
        print
        #replace _ with - for printing's sake
        raise Exception(output_messages.ERR_ONLY_1_FLAG % "--%s" % flag.replace("_","-"))

def initPluginsConfig():
    for plugin in controller.getAllPlugins():
        plugin.initConfig(controller)

def initPluginsSequences():
    for plugin in controller.getAllPlugins():
        plugin.initSequences(controller)

def initMain():
    #verify that root is the user executing the script - only supported user for now.
    #TODO: check how we can change this to user rhevm
    _verifyUserPermissions()

    # Initialize logging
    initLogging()

    # Load Plugins
    loadPlugins()

    # Initialize configuration
    initConfig()

    initPluginsConfig()

    # Validate host has enough memory
    _checkAvailableMemory()

if __name__ == "__main__":
    try:
        initMain()

        runConfiguration = True
        confFile = None

        optParser = initCmdLineParser()

        # Do the actual command line parsing
        # Try/Except are here to catch the silly sys.exit(0) when calling rhevm-setup --help
        (options, args) = optParser.parse_args()
        # If --gen-answer-file was supplied, do not run main
        if options.gen_answer_file:
            # Make sure only --gen-answer-file was supplied
            validateSingleFlag(options, "gen_answer_file")
            generateAnswerFile(options.gen_answer_file)
        # Otherwise, run main()
        else:
            # Make sure only --answer-file was supplied
            if options.answer_file:
                validateSingleFlag(options, "answer_file")
                confFile = options.answer_file
                if not os.path.exists(confFile):
                    raise Exception(output_messages.ERR_NO_ANSWER_FILE % confFile)
            else:
                for key, value in options.__dict__.items():
                    # Replace the _ with - in the string since optparse replace _ with -
                    for group in controller.getAllGroups():
                        param = group.getParams("CMD_OPTION", key.replace("_","-"))
                        if len(param) > 0 and value:
                            commandLineValues[param[0].getKey("CONF_NAME")] = value

            main(confFile)

    except SystemExit:
        raise

    except BaseException as e:
        logging.error(traceback.format_exc())
        print e
        print output_messages.ERR_CHECK_LOG_FILE_FOR_MORE_INFO%(logFile)
        sys.exit(1)
