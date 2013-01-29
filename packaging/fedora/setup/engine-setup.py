#! /usr/bin/python

import sys
import logging
import os
import re
import signal
import traceback
import types
import socket
import ConfigParser
from StringIO import StringIO
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
from Cheetah.Template import Template
from miniyum import MiniYum

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

def generateIsoDomainPath():
    '''
    Generates path for iso domain
    '''

    if os.path.exists(basedefs.DEFAULT_ISO_EXPORT_PATH):
        return "%s_%s" % (basedefs.DEFAULT_ISO_EXPORT_PATH, utils.getCurrentDateTime())
    else:
        return basedefs.DEFAULT_ISO_EXPORT_PATH


def generateOrgName():
    '''
    Generates org name
    fqdn - recevies the fqdn of the host
    '''

    # Get the domain name, split only once
    fqdn = socket.getfqdn().split('.', 1)

    if fqdn:
        # Return domain name
        return fqdn[-1]
    else:
        # No domain name, return empty
        return ""

def initSequences():
    sequences_conf = [
                      { 'description'     : 'Initial Steps',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_CONFIG_OVIRT_ENGINE,
                                                'functions' : [setMaxSharedMemory] },
                                              { 'title'     : output_messages.INFO_FIND_JAVA,
                                                'functions' : [_findJavaHome]},
                                              { 'title'     : output_messages.INFO_CREATE_CA,
                                                'functions' : [_createCA]},
                                              { 'title'     : output_messages.INFO_UPD_ENGINE_CONF,
                                                'functions' : [_editSysconfig] },
                                              { 'title'     : output_messages.INFO_SET_DB_CONFIGURATION,
                                                'functions' : [_updatePgPassFile]}]
                       },
                      { 'description'     : 'Update DB',
                        'condition'       : [_isDbAlreadyInstalled],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_SET_DB_SECURITY,
                                                'functions' : [_encryptDBPass, _configEncryptedPass] },
                                              {  'title'     : output_messages.INFO_UPGRADE_DB,
                                                'functions' : [stopRhevmDbRelatedServices, _upgradeDB, startRhevmDbRelatedServices]} ]
                       },
                      { 'description'     : 'Create DB',
                        'condition'       : [_isDbAlreadyInstalled],
                        'condition_match' : [False],
                        'steps'           : [ { 'title'     : output_messages.INFO_SET_DB_SECURITY,
                                                'functions' : [_encryptDBPass, _configEncryptedPass]},
                                              { 'title'     : output_messages.INFO_CREATE_DB,
                                                'functions' : [_createDB,  _updateVDCOptions]},
                                              { 'title'     : output_messages.INFO_UPD_DC_TYPE,
                                                'functions' : [_updateDefaultDCType]} ]
                       },
                      { 'description'     : 'Edit Configuration',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_UPD_RHEVM_CONF,
                                                'functions' : [_editToolsConfFile] } ]
                       },
                      { 'description'     : 'Update local postgresql configuration',
                        'condition'       : [utils.compareStrIgnoreCase, controller.CONF["DB_REMOTE_INSTALL"], "local"],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_UPD_CONF % "Postgresql",
                                                'functions' : [editPostgresConf] } ]
                       },
                      { 'description'     : 'Config NFS',
                        'condition'       : [utils.compareStrIgnoreCase, controller.CONF["CONFIG_NFS"], "yes"],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_CFG_NFS,
                                                'functions' : [_configNfsShare, _loadFilesToIsoDomain] } ]
                       },
                      { 'description'     : 'Final Steps',
                        'condition'       : [],
                        'condition_match' : [],
                        'steps'           : [ { 'title'     : output_messages.INFO_CFG_IPTABLES,
                                                'functions' : [_configFirewall] },
                                              { 'title'     : output_messages.INFO_START_ENGINE,
                                                'functions' : [_startEngine] } ]
                       },
                      { 'description'     : 'Handling httpd',
                        'condition'       : [utils.compareStrIgnoreCase, controller.CONF["OVERRIDE_HTTPD_CONFIG"], "yes"],
                        'condition_match' : [True],
                        'steps'           : [ { 'title'     : output_messages.INFO_CONFIG_HTTPD,
                                                'functions' : [_configureSelinuxBoolean, _backupOldHttpdConfig, _configureHttpdSslKeys, _configureHttpdPort, _configureHttpdSslPort, _redirectUrl, _startHttpd]}]
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
            {   "CMD_OPTION"      :"override-firewall",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_IPTABLES_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_IPTABLES_PROMPT,
                "OPTION_LIST"     :getFirewalls(),
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   :"",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "OVERRIDE_FIREWALL",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False} ]
         ,
         "PORTS" : [
            {   "CMD_OPTION"      :"override-httpd-config",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_OVERRIDE_HTTPD_CONF_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_OVERRIDE_HTTPD_CONF_PROMPT,
                "OPTION_LIST"     :["yes","no"],
                "VALIDATION_FUNC" :validate.validateOverrideHttpdConfAndChangePortsAccordingly,
                "DEFAULT_VALUE"   :"yes",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "OVERRIDE_HTTPD_CONFIG",
                "USE_DEFAULT"     : True,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"http-port",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_HTTP_PORT_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_HTTP_PORT_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validatePort,
                "DEFAULT_VALUE"   :"80",
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
                "DEFAULT_VALUE"   :"443",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "HTTPS_PORT",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False}]
         ,
         "ALL_PARAMS" : [
             {  "CMD_OPTION"      :"random-passwords",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_RANDOM_PASSWORDS_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_RANDOM_PASSWORDS_PROMPT,
                "OPTION_LIST"     :["yes", "no"],
                "VALIDATION_FUNC" :validate.validateOptions,
                "DEFAULT_VALUE"   : "no",
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "RANDOM_PASSWORDS",
                "USE_DEFAULT"     : True,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

             {  "CMD_OPTION"      :"mac-range",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_MAC_RANGE_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_MAC_RANG_PROMPT,
                "OPTION_LIST"     :[],
                "VALIDATION_FUNC" :validate.validateStringNotEmpty,
                "DEFAULT_VALUE"   : utils.generateMacRange(),
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
                "DEFAULT_VALUE"   :generateOrgName(),
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "ORG_NAME",
                "USE_DEFAULT"     : False,
                "NEED_CONFIRM"    : False,
                "CONDITION"       : False},

            {   "CMD_OPTION"      :"default-dc-type",
                "USAGE"           :output_messages.INFO_CONF_PARAMS_DC_TYPE_USAGE,
                "PROMPT"          :output_messages.INFO_CONF_PARAMS_DC_TYPE_PROMPT,
                "OPTION_LIST"     :["NFS","FC","ISCSI","POSIXFS"],
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
                "VALIDATION_FUNC" : validate.validateRemotePort,
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
                "VALIDATION_FUNC" :validate.validateNFSMountPoint,
                "DEFAULT_VALUE"   : generateIsoDomainPath(),
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
                "DEFAULT_VALUE"   :basedefs.ISO_DISPLAY_NAME,
                "MASK_INPUT"      : False,
                "LOOSE_VALIDATION": False,
                "CONF_NAME"       : "ISO_DOMAIN_NAME",
                "USE_DEFAULT"     : True,
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
                    { "GROUP_NAME"            : "PORTS",
                      "DESCRIPTION"           : output_messages.INFO_GRP_PORTS,
                      "PRE_CONDITION"         : validate.validateIpaAndHttpdStatus,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
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
controller.CONF["DC_TYPE_ENUM"] = utils.Enum(NFS=1, FC=2, ISCSI=3, POSIXFS=6)

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
                    if "yes" in param.getKey("OPTION_LIST") and userInput.lower() == "y":
                        userInput = "yes"
                    if "no" in param.getKey("OPTION_LIST") and userInput.lower() == "n":
                        userInput = "no"
                    controller.CONF[param.getKey("CONF_NAME")] = userInput
                    loop = False
                # If validation failed but LOOSE_VALIDATION is true, ask user
                elif param.getKey("LOOSE_VALIDATION"):
                    answer = utils.askYesNo("User input failed validation, do you still wish to use it")
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

def _backupOldHttpdConfig():
    logging.debug("Backup old httpd configuration files")
    dateTimeSuffix = utils.getCurrentDateTime()
    #1. Backup httpd.conf file
    backupFile = "%s.%s.%s" % (basedefs.FILE_HTTPD_CONF, "BACKUP", dateTimeSuffix)
    logging.debug("Backing up %s into %s", basedefs.FILE_HTTPD_CONF, backupFile)
    utils.copyFile(basedefs.FILE_HTTPD_CONF, backupFile)

    #2. Backup ssl.conf file
    backupFile = "%s.%s.%s" % (basedefs.FILE_HTTPD_SSL_CONFIG, "BACKUP", dateTimeSuffix)
    logging.debug("Backing up %s into %s", basedefs.FILE_HTTPD_SSL_CONFIG, backupFile)
    utils.copyFile(basedefs.FILE_HTTPD_SSL_CONFIG, backupFile)


def _configureSelinuxBoolean():
    logging.debug("Enable httpd_can_network_connect boolean")
    cmd = [
        basedefs.EXEC_SEMANAGE,
        "boolean",
        "--modify",
        "--on",
        "httpd_can_network_connect",
    ]
    out, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_FAILED_UPDATING_SELINUX_BOOLEAN)

def _configureHttpdSslKeys():
    try:
        logging.debug("Update %s to use engine private key in mod_ssl directives"%(basedefs.FILE_HTTPD_SSL_CONFIG))
        # Use the apache key in mod_ssl directives
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_SSL_CONFIG, " ")
        handler.open()
        handler.editParam("SSLCertificateFile", basedefs.FILE_APACHE_CERT, uncomment=True)
        handler.editParam("SSLCertificateKeyFile", basedefs.FILE_APACHE_PRIVATE_KEY, uncomment=True)
        handler.editParam("SSLCertificateChainFile", basedefs.FILE_APACHE_CA_CRT_SRC, uncomment=True)
        handler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTPD_SSL_CONFIG%(basedefs.FILE_HTTPD_SSL_CONFIG))

def _redirectUrl():
    try:
        # Create the Apache configuration fragment from the template:
        confTemplate = Template(file=basedefs.FILE_OVIRT_HTTPD_CONF_TEMPLATE)
        confText = str(confTemplate)

        # Save the text produced by the template:
        fd = open(basedefs.FILE_OVIRT_HTTPD_CONF, 'w')
        fd.write(confText)
        fd.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_CREATE_OVIRT_HTTPD_CONF%(basedefs.FILE_OVIRT_HTTPD_CONF))

def _configureHttpdPort():
    try:
        logging.debug("Update %s to listen in the new HTTP port"%(basedefs.FILE_HTTPD_CONF))
        # Listen in the new http port
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_CONF, " ")
        handler.open()
        handler.editParam("Listen", controller.CONF["HTTP_PORT"])
        handler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTP_LISTEN_PORT%(basedefs.FILE_HTTPD_CONF))

def _configureHttpdSslPort():
    try:
        logging.debug("Update %s to listen in the new HTTPS port"%(basedefs.FILE_HTTPD_SSL_CONFIG))
        # Listen in the new https port
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_SSL_CONFIG, " ")
        handler.open()
        handler.editParam("Listen", controller.CONF["HTTPS_PORT"])
        handler.editLine("\s*<VirtualHost _default_:", "<VirtualHost _default_:%s>\n"%(controller.CONF["HTTPS_PORT"]),
                         True, output_messages.ERR_EXP_UPD_HTTPS_LISTEN_PORT%(basedefs.FILE_HTTPD_SSL_CONFIG))
        handler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTPS_LISTEN_PORT%(basedefs.FILE_HTTPD_SSL_CONFIG))

def _createCA():
    pubtemp = None

    try:
        # Create new CA only if none available
        if not os.path.exists(basedefs.FILE_CA_CRT_SRC):
            _updateCaCrtTemplate()

            # We create the CA with yesterday's starting date
            yesterday = datetime.datetime.utcnow() - datetime.timedelta(1)
            date = yesterday.strftime("%y%m%d%H%M%S+0000")
            logging.debug("Date string is %s", date)

            # Add random string to certificate CN field
            randInt = random.randint(10000,99999)

            # Truncating host fqdn to max allowed in certificate CN field
            truncatedFqdn = controller.CONF["HOST_FQDN"][0:basedefs.CONST_MAX_HOST_FQDN_LEN]
            logging.debug("truncated HOST_FQDN '%s' to '%s'. sized reduced to %d.."%(controller.CONF["HOST_FQDN"],truncatedFqdn,len(truncatedFqdn)))
            uniqueCN = truncatedFqdn + "." + str(randInt)
            logging.debug("using unique CN: '%s' for CA certificate"%uniqueCN)

            # Create the CA
            cmd = [
                os.path.join(basedefs.DIR_OVIRT_PKI, "installCA.sh"),
                controller.CONF["HOST_FQDN"],
                basedefs.CONST_CA_COUNTRY,
                controller.CONF["ORG_NAME"],
                basedefs.CONST_CA_ALIAS,
                basedefs.CONST_CA_PASS,
                date,
                basedefs.DIR_OVIRT_PKI,
                uniqueCN,
            ]

            out, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_RC_CODE, maskList=[basedefs.CONST_CA_PASS])

            # Extract non password key for log collector
            cmd = [
                basedefs.EXEC_OPENSSL,
                "pkcs12",
                "-in", basedefs.FILE_ENGINE_KEYSTORE,
                "-passin", "pass:" + basedefs.CONST_KEY_PASS,
                "-nodes",
                "-nocerts",
                "-out", basedefs.FILE_SSH_PRIVATE_KEY
            ]

            out, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_RC_CODE, maskList=[basedefs.CONST_KEY_PASS])
            os.chmod(basedefs.FILE_SSH_PRIVATE_KEY, 0600)

            # Extract non password key for apache
            cmd = [
                basedefs.EXEC_OPENSSL,
                "pkcs12",
                "-in", basedefs.FILE_APACHE_KEYSTORE,
                "-passin", "pass:" + basedefs.CONST_KEY_PASS,
                "-nodes",
                "-nocerts",
                "-out", basedefs.FILE_APACHE_PRIVATE_KEY
            ]

            out, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_RC_CODE, maskList=[basedefs.CONST_KEY_PASS])
            os.chmod(basedefs.FILE_APACHE_PRIVATE_KEY, 0600)

            os.symlink(
                os.path.basename(basedefs.FILE_CA_CRT_SRC),
                basedefs.FILE_APACHE_CA_CRT_SRC
            )

            # Extract CA fingerprint
            cmd = [
                basedefs.EXEC_OPENSSL,
                "x509",
                "-in", basedefs.FILE_CA_CRT_SRC,
                "-fingerprint",
                "-noout",
            ]

            finger, rc = utils.execCmd(cmdList=cmd, failOnError=True)
            msg = output_messages.INFO_CA_SSL_FINGERPRINT%(finger.rstrip().split("=")[1])
            controller.MESSAGES.append(msg)

            # ExtractSSH fingerprint
            cmd = [
                basedefs.EXEC_SSH_KEYGEN,
                "-yf", basedefs.FILE_SSH_PRIVATE_KEY
            ]
            pubkey, rc = utils.execCmd(cmdList=cmd, failOnError=True)
            pubtempfd, pubtemp = tempfile.mkstemp(suffix=".pub")
            os.close(pubtempfd)
            with open(pubtemp, "w") as f:
                f.write(pubkey)
            cmd = [
                basedefs.EXEC_SSH_KEYGEN,
                "-lf", pubtemp
            ]
            finger, rc = utils.execCmd(cmdList=cmd, failOnError=True)
            msg = output_messages.INFO_CA_SSH_FINGERPRINT%(finger.split()[1])
            controller.MESSAGES.append(msg)

            # Set right permissions
            _changeCaPermissions()
        else:
            msg = output_messages.INFO_CA_KEYSTORE_EXISTS
            logging.warn(msg)
            controller.MESSAGES.append(msg)

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_CREATE_CA)
    finally:
        if pubtemp != None:
            os.remove(pubtemp)

def _changeCaPermissions():
    changeList = [os.path.join(basedefs.DIR_OVIRT_PKI, "private"),
                  basedefs.FILE_CA_CRT_SRC,
                  basedefs.FILE_ENGINE_KEYSTORE,
                  os.path.join(basedefs.DIR_OVIRT_PKI, "private", "ca.pem"),
                  os.path.join(basedefs.DIR_OVIRT_PKI, ".truststore")]
    for item in changeList:
        utils.chownToEngine(item)
        if os.path.isdir(item):
            logging.debug("changing directory permissions for %s to 0750" % item)
            os.chmod(item, 0750)
        else:
            logging.debug("changing file permissions for %s to 0640" % item)
            os.chmod(item, 0640)

    os.chown(basedefs.FILE_APACHE_KEYSTORE, utils.getUsernameId("apache"), utils.getGroupId("apache"))
    os.chmod(basedefs.FILE_APACHE_KEYSTORE, 0640)
    os.chown(basedefs.FILE_APACHE_PRIVATE_KEY, utils.getUsernameId("apache"), utils.getGroupId("apache"))
    os.chmod(basedefs.FILE_APACHE_PRIVATE_KEY, 0640)

def _updateCaCrtTemplate():
    for file in [basedefs.FILE_CA_CRT_TEMPLATE, basedefs.FILE_CERT_TEMPLATE]:
        logging.debug("updating %s" % (file))
        fileHandler = utils.TextConfigFileHandler(file)
        fileHandler.open()
        fileHandler.editParam("authorityInfoAccess", " caIssuers;URI:http://%s:%s/ca.crt" % (controller.CONF["HOST_FQDN"], controller.CONF["HTTP_PORT"]))
        fileHandler.close()

def getFirewalls():
    firewalls = ["None"]
    iptables = utils.Service("iptables")
    fwd = utils.Service("firewalld")

    # Add available services to list
    if fwd.available():
        firewalls.append("Firewalld")
    if iptables.available():
        firewalls.append("IPTables")

    return firewalls

def _configFirewall():
    # Create Sample configuration files
    _createIptablesConfig()
    _createFirewalldConfig()

    # Configure chosen firewall
    if utils.compareStrIgnoreCase(controller.CONF["OVERRIDE_FIREWALL"], "firewalld"):
        _configureFirewalld()
    elif utils.compareStrIgnoreCase(controller.CONF["OVERRIDE_FIREWALL"], "iptables"):
        _configureIptables()
    else:
        # Inform user how he can configure firewall
        controller.MESSAGES.append(output_messages.INFO_IPTABLES_PORTS % (controller.CONF["HTTP_PORT"], controller.CONF["HTTPS_PORT"]))
        controller.MESSAGES.append(output_messages.INFO_IPTABLES_FILE % (basedefs.FILE_IPTABLES_EXAMPLE))
        controller.MESSAGES.append(output_messages.INFO_FIREWALLD_INSTRUCTIONS)

def _createFirewalldConfig():
    logging.debug("Creating firewalld configuration")

    # Open xml
    servicexml = utils.XMLConfigFileHandler(basedefs.FILE_FIREWALLD_SERVICE)
    servicexml.open()

    # Remove all port entries
    servicexml.removeNodes("/service/port")

    # Add ports to service xml
    ports = []
    for port in [controller.CONF["HTTP_PORT"], controller.CONF["HTTPS_PORT"]]:
        ports.append({
            'port': port,
            'protocol': ['tcp']
        })

    if utils.compareStrIgnoreCase(controller.CONF["CONFIG_NFS"], "yes"):
        ports += NFS_IPTABLES_PORTS

    for portCfg in ports:
        for protocol in portCfg["protocol"]:
             servicexml.addNodes("/service", "<port protocol=\"%s\" port=\"%s\"/>" % (protocol, portCfg["port"]))

    # Save firewalld service configuration
    servicexml.close()

def _configureFirewalld():
    logging.debug("configuring firewalld")

    # Load firewalld module only when needed.
    # This will fail if firewalld isn't available in the system.
    import engine_firewalld as firewalld

    # Always start firewalld, otherwise, we will get DBus exception
    service = utils.Service("firewalld")
    service.start(True)

    for zone in firewalld.getActiveZones():
        firewalld.addServiceToZone("ovirt", zone)

    # Restart firewalld
    service = utils.Service("firewalld")
    service.stop(True)
    service.start(True)

def _createIptablesConfig():
    logging.debug("creating iptables configuration")
    try:
        with open(basedefs.FILE_IPTABLES_DEFAULT, "r") as f:
            fileContent = f.read()

        ports = []
        lines = []

        for port in [controller.CONF["HTTP_PORT"], controller.CONF["HTTPS_PORT"]]:
            ports.append({
                'port': port,
                'protocol': ['tcp']
            })

        if utils.compareStrIgnoreCase(controller.CONF["CONFIG_NFS"], "yes"):
            ports += NFS_IPTABLES_PORTS

        for portCfg in ports:
            for protocol in portCfg["protocol"]:
                lines.append(
                    "-A INPUT -p %s -m state --state NEW -m %s --dport %s -j ACCEPT" % (
                        protocol,
                        protocol,
                        portCfg["port"]
                    )
                )

        if basedefs.CONST_CONFIG_EXTRA_IPTABLES_RULES in controller.CONF:
            lines += controller.CONF[basedefs.CONST_CONFIG_EXTRA_IPTABLES_RULES]

        outputText = fileContent.replace('@CUSTOM_RULES@', "\n".join(lines))
        logging.debug(outputText)

        with open(basedefs.FILE_IPTABLES_EXAMPLE, "w") as f:
            f.write(outputText)

    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_CFG_IPTABLES)

def _configureIptables():
    logging.debug("configuring iptables")
    try:
        if os.path.isfile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG)):
            backupFile = "%s.%s_%s"%(basedefs.FILE_IPTABLES_BACKUP, time.strftime("%H%M%S-%m%d%Y"), os.getpid())
            utils.copyFile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG), backupFile)
            controller.MESSAGES.append(output_messages.INFO_IPTABLES_BACKUP_FILE%(backupFile))

        utils.copyFile(basedefs.FILE_IPTABLES_EXAMPLE, "%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG))

        # stop the iptables explicitly, since we dont care about the status
        # of the current rules we will ignore the return code
        logging.debug("Restarting the iptables service")
        iptables = utils.Service("iptables")
        iptables.stop(True)
        iptables.start(True)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_CFG_IPTABLES)

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
    cmd = [
        scriptHome,
        "-l", dbLogFilename,
        "-w", controller.CONF["DB_PASS"],
        "-u", getDbAdminUser(),
        "-s", getDbHostName(),
        "-p", getDbPort(),
        "-r", controller.CONF["DB_REMOTE_INSTALL"],
    ]

    # Create db using shell command
    output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_DB_CREATE_FAILED, maskList=masked_value_set)
    logging.debug("Successfully installed %s db" % basedefs.DB_NAME)

def _upgradeDB():
    """
    run db upgrade scripts
    required installed db.
    won't change db security settings
    """

    # Set current DB name
    currentDbName = basedefs.DB_NAME

    # Before db upgrade we want to make a backup of existing db in case we fail
    # The backup is performed on local system, even for remote DB.
    dbBackupFile = tempfile.mkstemp(suffix=".sql", dir=basedefs.DIR_DB_BACKUPS)[1]
    logging.debug("backing up %s db to file %s"%(basedefs.DB_NAME, dbBackupFile))

    # Run db backup
    utils.backupDB(basedefs.DB_NAME, getDbUser(), dbBackupFile, getDbHostName(), getDbPort())

    # Rename DB first. If it fails - stop with "active connections" error.
    # if upgrade passes fine, rename the DB back.
    DB_NAME_TEMP = "%s_%s" % (basedefs.DB_NAME, utils.getCurrentDateTime())
    utils.renameDB(basedefs.DB_NAME, DB_NAME_TEMP)
    currentDbName = DB_NAME_TEMP

    # if we're here, DB was renamed.
    # upgrade script must run from dbscripts dir
    currentDir = os.getcwd()
    os.chdir(basedefs.DIR_DB_SCRIPTS)

    try:

        logging.debug("upgrading db schema")
        cmd = [
            os.path.join(basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_UPGRADE_SCRIPT),
            "-u", getDbUser(),
            "-d", DB_NAME_TEMP,
            "-s", getDbHostName(),
            "-p", getDbPort(),
        ]

        # Run upgrade.sh script to update existing db
        output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_DB_UPGRADE_FAILED)

        # Log the successful upgrade
        logging.debug('Successfully upgraded %s DB'%(basedefs.DB_NAME))
        controller.MESSAGES.append("DB was upgraded to latest version. previous DB backup can be found at %s"%(dbBackupFile))

        # Go back to previous dir
        os.chdir(currentDir)

        # Upgrade was successful, so rename the DB back.
        utils.renameDB(DB_NAME_TEMP, basedefs.DB_NAME)
        currentDbName = basedefs.DB_NAME

        # Update rpm version in vdc options
        utils.updateVDCOption("ProductRPMVersion", utils.getRpmVersion(basedefs.ENGINE_RPM_NAME))
    except:
        # Upgrade failed! we need to restore the old db
        logging.debug("DB upgrade failed, restoring it to a previous state. DB was backed up to %s", dbBackupFile)

        # Delete the original DB.
        # TODO: handle the case of failure - it should not stop the flow, but should write to the log
        sqlQuery="DROP DATABASE %s" % currentDbName
        utils.execRemoteSqlCommand(getDbUser(), \
                                   getDbHostName(), \
                                   getDbPort(), \
                                   basedefs.DB_POSTGRES, \
                                   sqlQuery, False, \
                                   output_messages.ERR_DB_DROP)

        # Restore the DB
        utils.restoreDB(getDbUser(), getDbHostName(), getDbPort(), dbBackupFile)

        raise Exception(output_messages.ERR_DB_UPGRADE_FAILED)

def _updateDefaultDCType():
    logging.debug("updating default data center storage type")
    newDcTypeNum = controller.CONF["DC_TYPE_ENUM"].parse(str.upper(controller.CONF["DC_TYPE"]))
    sqlQuery = "select inst_update_default_storage_pool_type (%s)" % newDcTypeNum
    utils.execRemoteSqlCommand(getDbUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_EXP_UPD_DC_TYPE%(basedefs.DB_NAME))

def _updateVDCOptions():
    logging.debug("updating vdc options..")

    #some options must be set before others in order for the keystore passwords to be set encrypted
    #since python doesn't iterate on the dict in a sorted order, we must seperate them to different dicts
    #1st we update the keystore and CA related paths, only then we can set the passwords and the rest options
    options = (
        {
            "CABaseDirectory":[basedefs.DIR_OVIRT_PKI, 'text'],
            "keystoreUrl":[basedefs.FILE_ENGINE_KEYSTORE, 'text'],
            "CertificateFileName":[basedefs.FILE_ENGINE_CERT, 'text'],
            "TruststoreUrl":[basedefs.FILE_TRUSTSTORE, 'text'],
            "ENGINEEARLib":["%s/engine.ear" %(basedefs.DIR_ENGINE), 'text'],
            "CACertificatePath":[basedefs.FILE_CA_CRT_SRC, 'text'],
            "CertAlias":["1", 'text'],
            "keystorePass":[basedefs.CONST_KEY_PASS, 'text'],
        },
        {
            "TruststorePass":[basedefs.CONST_CA_PASS, 'text'],
            "LocalAdminPassword":[controller.CONF["AUTH_PASS"], 'pass'],
            "SSLEnabled":[ "true", 'text'],
            "UseSecureConnectionWithServers":[ "true", 'text'],
            "SysPrepXPPath":["/etc/ovirt-engine/sysprep/sysprep.xp", 'text'],
            "SysPrep2K3Path":["/etc/ovirt-engine/sysprep/sysprep.2k3", 'text'],
            "SysPrep2K8Path":["/etc/ovirt-engine/sysprep/sysprep.2k8x86", 'text'],
            "SysPrep2K8x64Path":["/etc/ovirt-engine/sysprep/sysprep.2k8", 'text'],
            "SysPrep2K8R2Path":["/etc/ovirt-engine/sysprep/sysprep.2k8", 'text'],
            "SysPrepWindows7Path":["/etc/ovirt-engine/sysprep/sysprep.w7", 'text'],
            "SysPrepWindows7x64Path":["/etc/ovirt-engine/sysprep/sysprep.w7x64", 'text'],
            "SysPrepWindows8Path":["/etc/ovirt-engine/sysprep/sysprep.w8", 'text'],
            "SysPrepWindows8x64Path":["/etc/ovirt-engine/sysprep/sysprep.w8x64", 'text'],
            "SysPrepWindows2012x64Path":["/etc/ovirt-engine/sysprep/sysprep.2k12x64", 'text'],
            "MacPoolRanges":[controller.CONF["MAC_RANGE"], 'text'],
            "InstallVds":["true", 'text'],
            "ConfigDir":["/etc/ovirt-engine", 'text'],
            "DataDir":["/usr/share/ovirt-engine", 'text'],
            "SignScriptName":["SignReq.sh", 'text'],
            "OrganizationName":[controller.CONF["ORG_NAME"], 'text'],
            "ProductRPMVersion":[utils.getRpmVersion(basedefs.ENGINE_RPM_NAME), 'text'],
            "AdminPassword":[controller.CONF["AUTH_PASS"], 'pass']
        }
    )

    try:
        if (os.path.exists(basedefs.FILE_ENGINE_CONFIG_BIN)):
            if (os.path.exists(basedefs.FILE_ENGINE_EXTENDED_CONF)):
                #1st iterate on the CA related options
                for subDict in options:
                    for key in subDict:
                        value, keyType = subDict[key]
                        utils.updateVDCOption(key, value, masked_value_set, keyType)

                logging.debug("finished updating vdc options")
            else:
                raise Exception(output_messages.ERR_CANT_FIND_VDC_OPTION_FILE%(basedefs.FILE_ENGINE_EXTENDED_CONF))
        else:
            raise Exception(output_messages.ERR_CANT_FIND_RHEVM_CONFIG_FILE%(basedefs.FILE_ENGINE_CONFIG_BIN))

    except:
        raise Exception(output_messages.ERR_FAILED_UPD_VDC_OPTIONS%(sys.exc_info()[1]))

def _getVDCOption(key):
    #running rhevm-config to get values per key
    logging.debug("getting vdc option %s" % key)
    msg = output_messages.ERR_EXP_GET_VDC_OPTION % key
    cmd = [
        basedefs.FILE_ENGINE_CONFIG_BIN,
        "-g", key,
        "--cver=" + basedefs.VDC_OPTION_CVER,
        "-p", basedefs.FILE_ENGINE_EXTENDED_CONF,
    ]
    output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=msg, maskList=masked_value_set)
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

def getDbUser():
    """
    Get user for db
    """
    if "DB_ADMIN" in controller.CONF.keys():
            return controller.CONF["DB_ADMIN"]

    return basedefs.DB_USER

def _updatePgPassFile():
    """
    Create ~/.pgpass file with
    all relevant info to connect via md5

    file syntax:
    hostname:port:database:username:password
    to allow access to any db - write '*' instead of database

    The .pgpass file for our application is created with a specific structure.
    This structure is very important, and it is imperative not to modifiy it
    manually.
    The structure of the file is as follows:
    # The following section was created during oVirt Engine setup.
    # DO NOT CHANGE IT MANUALLY - OTHER UTILITIES AND TOOLS DEPEND ON ITS STRUCTURE.
    # Beginning of the oVirt Engine DB settings section
    # DB ADMIN credentials
    <DB ADMIN PGPASS LINE>
    # DB USER credentials
    <DB USER PGPASS LINE>
    #####  End of oVirt Engine DB settings section
    """
    try:
        #backup existing .pgpass
        if os.path.exists(basedefs.DB_PASS_FILE):
            backupFile = "%s.%s" % (basedefs.DB_PASS_FILE, utils.getCurrentDateTime())
            logging.debug("found existing pgpass file, backing current to %s" % (backupFile))
            os.rename(basedefs.DB_PASS_FILE, backupFile)

        with open(basedefs.DB_PASS_FILE, "w") as pgPassFile:

            # Add header and opening lines
            pgPassFile.write(basedefs.PGPASS_FILE_HEADER_LINE + "\n")
            pgPassFile.write(basedefs.PGPASS_FILE_OPENING_LINE + "\n")

            # Create credentials lines
            adminLine = "# %s." % basedefs.PGPASS_FILE_ADMIN_LINE
            userLine = "# %s." % basedefs.PGPASS_FILE_USER_LINE

            pgPassFile.write(adminLine + "\n")

            # Use parameters received from the user and skip if the install is local
            if "DB_ADMIN" in controller.CONF.keys():
                logging.info("Using db credentials provided by the user")

                # Create user lines
                pgPassFile.write(userLine + "\n")
                pglines = _updatePgPassLine(controller.CONF["DB_HOST"], controller.CONF["DB_PORT"],"*",
                                            controller.CONF["DB_ADMIN"], controller.CONF["DB_PASS"])
            else:
                logging.info("Using default db credentials")

                # Create an admin user line
                pglines = _updatePgPassLine(controller.CONF["DB_HOST"], basedefs.DB_PORT, "*", basedefs.DB_ADMIN, controller.CONF["DB_PASS"])

                # Add users
                pglines = pglines + "\n" + userLine + "\n"
                pglines = pglines + _updatePgPassLine(controller.CONF["DB_HOST"], basedefs.DB_PORT, "*", basedefs.DB_USER, controller.CONF["DB_PASS"])

            pgPassFile.write(pglines + "\n")
            pgPassFile.write(basedefs.PGPASS_FILE_CLOSING_LINE + "\n")

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
    Encryptes the postgres db password
    and store it in conf
    """
    #run encrypt tool on user given password
    controller.CONF["ENCRYPTED_DB_PASS"] = utils.encryptEngineDBPass(password=controller.CONF["DB_PASS"],
                                               maskList=masked_value_set)

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
                                         (group.getKey("GROUP_NAME"), postConditionValue, group.getKey("POST_CONDITION_MATCH")))
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
    answer = utils.askYesNo(output_messages.INFO_USE_PARAMS)
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

def _startHttpd():
    logging.debug("Handling the %s service"%(basedefs.HTTPD_SERVICE_NAME))
    srv = utils.Service(basedefs.HTTPD_SERVICE_NAME)
    srv.autoStart(True)
    srv.stop(False)
    srv.start(True)



def _startEngine():
    logging.debug("using chkconfig to enable engine to load on system startup.")
    srv = utils.Service(basedefs.ENGINE_SERVICE_NAME)
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
        nfsutils.addNfsExport(controller.CONF["NFS_MP"],
                              (("0.0.0.0", "0.0.0.0", ("rw",)),),
                              " %s installer" % basedefs.APP_NAME)

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
    # Compare to basedefs.CONST_SHMMAX
    cmd = [
        basedefs.EXEC_SYSCTL, "-b", "kernel.shmmax",
    ]
    currentShmmax, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_FAILED_KERNEL_PARAMS)
    if currentShmmax and (int(currentShmmax) >= basedefs.CONST_SHMMAX):
        logging.debug("current shared memory max in kernel is %s, there is no need to update the kernel parameters", currentShmmax)
        return

    # Decide what is the file that we are going to modify according to the
    # existence or not of sysctl.conf, it exists in older distributions but
    # has been replaced by a sysctl.d directory in newer distributions using
    # systemd
    sysctlFile = basedefs.FILE_SYSCTL
    if not os.path.exists(sysctlFile):
        sysctlFile = basedefs.FILE_ENGINE_SYSCTL

    # Create the chosen file if it doesn't exist.
    if not os.path.exists(sysctlFile):
        open(sysctlFile, "w").close()

    # If we got here, it means we need to update kernel.shmmax in sysctl.conf
    txtHandler = utils.TextConfigFileHandler(sysctlFile)
    txtHandler.open()
    logging.debug("setting SHARED MEMORY MAX to: %s", basedefs.CONST_SHMMAX)
    txtHandler.editParam("kernel.shmmax", basedefs.CONST_SHMMAX)
    txtHandler.close()

    # Restart sysctl service for systemd based systems or reload parameters for
    # sysVinit
    if os.path.exists(basedefs.EXEC_SYSTEMCTL):
        cmd = [
             basedefs.EXEC_SYSTEMCTL, "restart", "systemd-sysctl.service",
        ]
    else :
        cmd = [
            basedefs.EXEC_SYSCTL, "-e", "-p",
        ]
    utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_FAILED_KERNEL_PARAMS)

def _addIsoDomaintoDB(uuid, description):
    logging.debug("Adding iso domain into DB")
    connectionId = nfsutils.generateUUID()
    sqlQuery = "select inst_add_iso_storage_domain ('%s', '%s', '%s', '%s:%s', %s, %s)" % (uuid, description, connectionId, controller.CONF["HOST_FQDN"], controller.CONF["NFS_MP"], 0, 0)
    utils.execRemoteSqlCommand(getDbUser(), getDbHostName(), getDbPort(), basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_FAILED_INSERT_ISO_DOMAIN%(basedefs.DB_NAME))

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

def _stopEngine(configFile):
    logging.debug("stopping %s service" % basedefs.ENGINE_SERVICE_NAME)
    jservice = utils.Service(basedefs.ENGINE_SERVICE_NAME)
    (status, rc) = jservice.status()

    #if we don't use an answer file, we need to ask the user if to stop engine
    #if the engine is not already stopped (3, service not running)
    if not configFile:
        if rc != 3:
            logging.debug("engine is in status %d: %s" % (rc, status))
            print output_messages.INFO_NEED_STOP_ENGINE
            answer = utils.askYesNo(output_messages.INFO_Q_STOP_ENGINE)
            if answer:
                print output_messages.INFO_STOP_ENGINE,
                jservice.stop(True)
            else:
                logging.debug("User chose not to stop engine")
                return False
    else:
        #we stop the ovirt-engine service on a silent install
        print output_messages.INFO_STOP_ENGINE,
        jservice.stop(True)

    return True

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
    for confFile in [basedefs.FILE_LOGCOLLECTOR_CONF, basedefs.FILE_ISOUPLOADER_CONF, basedefs.FILE_IMAGE_UPLOADER_CONF]:
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

def _configEncryptedPass():
    """
    Push the encrypted password into the local configuration file.
    """
    try:
        utils.configEncryptedPass(controller.CONF["ENCRYPTED_DB_PASS"])
    except:
        logging.error("ERROR Editing engine local configuration file.")
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_FAILED_CONFIG_ENGINE)

def _editSysconfig():
    """
    Update the local configuration file.
    """
    dbUrl = "jdbc:postgresql://" + getDbHostName() + ":" + getDbPort() + "/" + basedefs.DB_NAME
    if "DB_SECURE_CONNECTION" in controller.CONF.keys() and controller.CONF["DB_SECURE_CONNECTION"] == "yes":
        dbUrl = dbUrl + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"

    proxyEnabled = utils.compareStrIgnoreCase(controller.CONF["OVERRIDE_HTTPD_CONFIG"], "yes")
    utils.editEngineSysconfig(proxyEnabled=proxyEnabled,
                              dbUrl=dbUrl,
                              dbUser=utils.getDbUser(),
                              fqdn=controller.CONF["HOST_FQDN"],
                              http=controller.CONF["HTTP_PORT"],
                              https=controller.CONF["HTTPS_PORT"],
                              javaHome=controller.CONF["JAVA_HOME"])

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

def _findJavaHome():
    # Find it:
    javaHome = utils.findJavaHome()
    if not javaHome:
        logging.error("Can't find any supported JVM.")
        raise Exception(output_messages.ERR_EXP_CANT_FIND_SUPPORTED_JAVA)

    # Save the result:
    controller.CONF["JAVA_HOME"] = javaHome

def isSecondRun():
    if os.path.exists(basedefs.FILE_ENGINE_KEYSTORE):
        logging.debug("%s exists, second run detected", basedefs.FILE_ENGINE_KEYSTORE)
        return True
    else:
        return False

def runSequences():
    controller.runAllSequences()

def main(configFile=None):

    # BEGIN: PROCESS-INITIALIZATION
    miniyumsink = utils.MiniYumSink()
    MiniYum.setup_log_hook(sink=miniyumsink)
    extraLog = open(logFile, "a")
    miniyum = MiniYum(sink=miniyumsink, extraLog=extraLog)
    miniyum.selinux_role()
    # END: PROCESS-INITIALIZATION

    try:
        logging.debug("Entered main(configFile='%s')"%(configFile))
        print output_messages.INFO_HEADER

        # Handle second excecution warning
        if isSecondRun():
            print output_messages.WARN_SECOND_RUN

            # Ask for user input only on interactive run
            if not configFile and not utils.askYesNo(output_messages.INFO_PROCEED):
                logging.debug("exiting gracefully")
                print output_messages.INFO_STOP_INSTALL_EXIT
                return 0

        # Stop engine
        if not _stopEngine(configFile):
            logging.debug("exiting gracefully")
            print output_messages.INFO_STOP_INSTALL_EXIT
            return 0

        # Get parameters
        _handleParams(configFile)

        # we do not wish to be interrupted from this point on
        signal.signal(signal.SIGINT, signal.SIG_IGN)

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

        # Override passwords with random if needed
        if controller.CONF["RANDOM_PASSWORDS"] == "yes":
            logging.debug("Overriding given passwords with random")
            controller.CONF["AUTH_PASS"] = utils.generatePassword(basedefs.RANDOM_PASS_LENGTH)
            if controller.CONF["DB_LOCAL_PASS"]: # Override db password only if db is local
                controller.CONF["DB_PASS"] = utils.generatePassword(basedefs.RANDOM_PASS_LENGTH)

            # Add random password to masked passwords
            masked_value_set.add(controller.CONF["AUTH_PASS"])
            masked_value_set.add(controller.CONF["DB_PASS"])


        # Run main setup logic
        runSequences()

        # Lock rhevm version
        utils.lockRpmVersion(miniyum, basedefs.RPM_LOCK_LIST.split())

        # Print info
        _addFinalInfoMsg()
        print output_messages.INFO_INSTALL_SUCCESS
        print output_messages.INFO_RHEVM_URL % (
            "http://%s:%s" % (controller.CONF["HOST_FQDN"],
            controller.CONF["HTTP_PORT"]))
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
    #execute free -m to get output in MB
    logging.debug("checking total memory")
    cmd = [
        basedefs.EXEC_FREE, "-m"
    ]
    output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=output_messages.ERR_EXP_FREE_MEM)

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
    #multiplying CONST_MIN_MEMORY by 0.95 to have tolerance of 5%
    if availableMemory < (basedefs.CONST_MIN_MEMORY_MB * 0.95):
        logging.error("Availble memory (%s) is lower then the minimum requirments (%s)" % (availableMemory, basedefs.CONST_MIN_MEMORY_MB))
        raise Exception(output_messages.ERR_EXP_NOT_EMOUGH_MEMORY)

    if availableMemory < basedefs.CONST_WARN_MEMORY_MB:
        logging.warn("There is less then %s available memory " % basedefs.CONST_WARN_MEMORY_MB)
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
    parser.add_option("--answer-file", help="Runs the configuration in non-interactive mode, extracting all information from the \
                                            configuration file. using this option excludes all other options")
    parser.add_option("--no-mem-check", help="Disable minimum memory check", action="store_true", default=False)

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

            # Validate host has enough memory
            if not options.no_mem_check:
                _checkAvailableMemory()

            main(confFile)

    except SystemExit:
        raise

    except BaseException as e:
        logging.error(traceback.format_exc())
        print e
        print output_messages.ERR_CHECK_LOG_FILE_FOR_MORE_INFO%(logFile)
        sys.exit(1)
