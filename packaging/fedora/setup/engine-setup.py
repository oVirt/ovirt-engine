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

# Globals
logFile = os.path.join(basedefs.DIR_LOG,basedefs.FILE_INSTALLER_LOG)
conf_params = ()
conf_groups = ()
conf = {}
messages = []
commandLineValues = {}

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

def initConfig():
    """
    Initialization of configuration
    """
    global conf_params
    global conf_groups
    conf_params = (
        {   "CMD_OPTION"      :"override-iptables",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_IPTABLES_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_IPTABLES_PROMPT,
            "OPTION_LIST"     :["yes","no"],
            "VALIDATION_FUNC" :validate.validateOptions,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "OVERRIDE_IPTABLES",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "IPTABLES"},

        {   "CMD_OPTION"      :"http-port",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_HTTP_PORT_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_HTTP_PORT_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validatePort,
            "DEFAULT_VALUE"   :"8080",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "HTTP_PORT",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

        {   "CMD_OPTION"      :"https-port",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_HTTPS_PORT_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_HTTPS_PORT_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validatePort,
            "DEFAULT_VALUE"   :"8443",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "HTTPS_PORT",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

         {  "CMD_OPTION"      :"mac-range",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_MAC_RANGE_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_MAC_RANG_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validateStringNotEmpty,
            "DEFAULT_VALUE"   :generateMacRange(),
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "MAC_RANGE",
            "USE_DEFAULT"     : True,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

        {   "CMD_OPTION"      :"host-fqdn",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_FQDN_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_FQDN_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validateFQDN,
            "DEFAULT_VALUE"   :socket.getfqdn(),
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": True,
            "CONF_NAME"       : "HOST_FQDN",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

        {   "CMD_OPTION"      :"auth-pass",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_AUTH_PASS_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_AUTH_PASS_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validatePassword,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : True,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "AUTH_PASS",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : True,
            "GROUP"           : "ALL_PARAMS"},

        {   "CMD_OPTION"      :"db-pass",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_DB_PASSWD_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_DB_PASSWD_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validatePassword,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : True,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "DB_PASS",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : True,
            "GROUP"           : "ALL_PARAMS"},

         {  "CMD_OPTION"      :"org-name",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_ORG_NAME_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_ORG_NAME_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validateOrgName,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "ORG_NAME",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

        {   "CMD_OPTION"      :"default-dc-type",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_DC_TYPE_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_DC_TYPE_PROMPT,
            "OPTION_LIST"     :["NFS","FC","ISCSI"],
            "VALIDATION_FUNC" :validate.validateOptions,
            "DEFAULT_VALUE"   :"NFS",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "DC_TYPE",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "ALL_PARAMS"},

         {  "CMD_OPTION"      :"config-nfs",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_CONFIG_NFS_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_CONFIG_NFS_PROMPT,
            "OPTION_LIST"     :["yes","no"],
            "VALIDATION_FUNC" :validate.validateOptions,
            "DEFAULT_VALUE"   :"yes",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "CONFIG_NFS",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : ""},

         {  "CMD_OPTION"      :"nfs-mp",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_NFS_MP_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_NFS_MP_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validateMountPoint,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "NFS_MP",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "NFS"},

         {  "CMD_OPTION"      :"iso-domain-name",
            "USAGE"           :output_messages.INFO_CONF_PARAMS_NFS_DESC_USAGE,
            "PROMPT"          :output_messages.INFO_CONF_PARAMS_NFS_DESC_PROMPT,
            "OPTION_LIST"     :[],
            "VALIDATION_FUNC" :validate.validateIsoDomainName,
            "DEFAULT_VALUE"   :"",
            "REAPEAT"         : True,
            "MASK_INPUT"      : False,
            "LOOSE_VALIDATION": False,
            "CONF_NAME"       : "ISO_DOMAIN_NAME",
            "USE_DEFAULT"     : False,
            "NEED_CONFIRM"    : False,
            "GROUP"           : "NFS"},

    )

    """
    this implementation has come into place in order
    to verify a set of params based on user action, which mean
    that the param named in the CONDITION member will be executed
    and if the response equals to the CONDITION_RESULT member
    the rest of the params will be presented to the user
    in order to by pass the CONDITION check, set CONDITION to False
    and set CONDITION_RESULT to True
    EXAMPLE:
    conf_groups = ( { "GROUP_NAME"            : "ALL_PARAMS",
                      "PRE_CONDITION"         : False,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},
                   { "GROUP_NAME"            : "AD",
                      "PRE_CONDITION"         : False,
                      "PRE_CONDITION_MATCH"   : True,
                      "POST_CONDITION"        : validateAD,
                      "POST_CONDITION_MATCH"  : True},
                   { "GROUP_NAME"            : "NFS",
                      "PRE_CONDITION"         : "CONFIG_NFS",
                      "PRE_CONDITION_MATCH"   : "yes",
                      "POST_CONDITION"        : False,
                      "POST_CONDITION_MATCH"  : True},

    )
    the given example contains 3 groups,
    when the group NFS is checked, we get the input for the "PRE_CONDTION" member
    (i.e the "CONFIG_NFS" param is prompted to the user) and the value
    of the said param is compared with the "PRE_CONDITION_MATCH" member.
    if the compare returns True (i.e. the user answered "yes") all the params
    in which the "GROUP" members is "NFS" are then prompted to the user
    when the group ALL_PARAMS is checked, since the "CONDITION" member is False
    we do not check for validity and simply prompt the user with all params
    in which the "GROUP" member is "ALL_PARAMS"
    when the group AD is checked, since the "PRE_CONDITION" is False we act just
    like the ALL_PARAMS group, however, since the "POST_CONDITION" member is populated
    we run the given function(can be either a function name or a name of a conf_param)
    and compare it with the value of the "POST_CONDITION_MATCH" member, if the result differs
    we prompt the user to input the entire set of params which are members of the AD group
    all over again
    """
    conf_groups = ( { "GROUP_NAME"            : "ALL_PARAMS",
                      "DESCRIPTION"           : output_messages.INFO_GRP_ALL,
                      "PRE_CONDITION"         : False,
                      "PRE_CONDITION_MATCH"   : True,
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

#data center types enum
DC_TYPE = Enum(NFS=1, FC=2, ISCSI=3)

def _getColoredText (text, color):
    ''' gets text string and color
        and returns a colored text.
        the color values are RED/BLUE/GREEN/YELLOW
        everytime we color a text, we need to disable
        the color at the end of it, for that
        we use the NO_COLOR chars.
    '''
    return color + text + basedefs.NO_COLOR

def _getParamKeyValue(confName, keyName):
    for param in conf_params:
        if (param["CONF_NAME"] == confName):
            return param[keyName]

def _getParamsPerGroup(groupName):
    paramsList = []
    for param in conf_params:
        if param["GROUP"] == groupName:
            paramsList.append(param["CONF_NAME"])
    return paramsList

def _getParamPerName(paramName):
    for param in conf_params:
        if param["CONF_NAME"] == paramName:
            return param
    return False

def _getParamsPerKey(key, value):
    """
    returns a list of  params dictionary that has the supplied key which has the 
    value of the supplied value
    """
    paramsList = []
    for param in conf_params:
        if param.has_key(key) and param[key] == value:
            paramsList.append(param)
    return paramsList

def _getInputFromUser(param):
    """
    this private func reads the data from the user
    for the given param
    """
    global conf
    loop = True
    userInput = None

    try:
        if param["USE_DEFAULT"]:
            logging.debug("setting default value (%s) for key (%s)" % (mask(param["DEFAULT_VALUE"]), param["CONF_NAME"]))
            conf[param["CONF_NAME"]] = param["DEFAULT_VALUE"]
        else:
            while loop:
                # If the value was not supplied by the command line flags
                if not commandLineValues.has_key(param["CONF_NAME"]):
                    message = StringIO()
                    message.write(param["PROMPT"])

                    if type(param["OPTION_LIST"]) == types.ListType and len(param["OPTION_LIST"]) > 0:
                        message.write(" %s" % (str(param["OPTION_LIST"]).replace(',', '|')))

                    if param["DEFAULT_VALUE"]:
                        message.write("  [%s] " % (str(param["DEFAULT_VALUE"])))

                    message.write(": ")
                    message.seek(0)
                    #mask password or hidden fields

                    if (param["MASK_INPUT"]):
                        userInput = getpass.getpass("%s :" % (param["PROMPT"]))
                    else:
                        userInput = raw_input(message.read())
                else:
                    userInput = commandLineValues[param["CONF_NAME"]]
                # If DEFAULT_VALUE is set and user did not input anything
                if userInput == "" and len(param["DEFAULT_VALUE"]) > 0:
                    userInput = param["DEFAULT_VALUE"]

                # If param requires validation
                if param["VALIDATION_FUNC"](userInput, param["OPTION_LIST"]):
                    conf[param["CONF_NAME"]] = userInput
                    loop = False
                # If validation failed but LOOSE_VALIDATION is true, ask user
                elif param["LOOSE_VALIDATION"]:
                    answer = _askYesNo("User input failed validation, do you still wish to use it")
                    if answer:
                        loop = False
                        conf[param["CONF_NAME"]] = userInput
                    else:
                        if commandLineValues.has_key(param["CONF_NAME"]):
                            del commandLineValues[param["CONF_NAME"]]
                        loop = True                        
                else:
                    # Delete value from commandLineValues so that we will prompt the user for input
                    if commandLineValues.has_key(param["CONF_NAME"]):
                        del commandLineValues[param["CONF_NAME"]]
                    loop = True

    except KeyboardInterrupt:
        print "" # add the new line so messages wont be displayed in the same line as the question
        raise KeyboardInterrupt
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_READ_INPUT_PARAM % (param["CONF_NAME"]))

def input_param(param):
    """
    this func will read input from user
    and ask confirmation if needed
    """
    # We need to check if a param needs confirmation, (i.e. ask user twice)
    # Do not validate if it was given from the command line
    if (param["NEED_CONFIRM"]) and not commandLineValues.has_key(param["CONF_NAME"]):
        #create a copy of the param so we can call it twice
        confirmedParam = copy.deepcopy(param)
        confirmedParamName = param["CONF_NAME"] + "_CONFIRMED"
        confirmedParam["CONF_NAME"] = confirmedParamName
        confirmedParam["PROMPT"] = output_messages.INFO_CONF_PARAMS_PASSWD_CONFIRM_PROMPT
        confirmedParam["VALIDATION_FUNC"] = validate.validateStringNotEmpty
        # Now get both values from user (with existing validations
        while True:
            _getInputFromUser(param)
            _getInputFromUser(confirmedParam)
            if conf[param["CONF_NAME"]] == conf[confirmedParamName]:
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

        conf["HTTP_URL"] = "http://" + conf["HOST_FQDN"] + ":" + conf["HTTP_PORT"]
        conf["HTTPS_URL"] = "https://" + conf["HOST_FQDN"] + ":" + conf["HTTPS_PORT"]

        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var host_fqdn.*", 'var host_fqdn = "%s";'%(conf["HOST_FQDN"]))
        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var http_port.*", 'var http_port = "%s";'%(conf["HTTP_PORT"]))
        utils.findAndReplace(basedefs.FILE_JBOSS_HTTP_PARAMS, "var https_port.*", 'var https_port = "%s";'%(conf["HTTPS_PORT"]))

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
        node.setProp("value", conf["HTTPS_PORT"])
        
        exConfigHandler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_EXTERNAL_CFG%(basedefs.FILE_EXTERNAL_CONFIG))

def _editDefaultHtml():
    try:
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var httpPort.*", 'var httpPort = "%s";'%(conf["HTTP_PORT"]))
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var httpsPort.*", 'var httpsPort = "%s";'%(conf["HTTPS_PORT"]))
        utils.findAndReplace(basedefs.FILE_SERVER_PARAMS_JS, "var hostName.*", 'var hostName = "%s";'%(conf["HOST_FQDN"]))
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTML_FILE%(basedefs.FILE_DEFAULT_HTML))

def _createCA():
    global messages

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
	    truncatedFqdn = conf["HOST_FQDN"][0:basedefs.CONST_MAX_HOST_FQDN_LEN]
	    logging.debug("truncated HOST_FQDN '%s' to '%s'. sized reduced to %d.."%(conf["HOST_FQDN"],truncatedFqdn,len(truncatedFqdn)))
            uniqueCN = truncatedFqdn + "." + str(randInt)
	    logging.debug("using unique CN: '%s' for CA certificate"%uniqueCN)

            # Create the CA
            cmd = [os.path.join(basedefs.DIR_OVIRT_PKI, "installCA.sh"), conf["HOST_FQDN"],
                   basedefs.CONST_CA_COUNTRY, conf["ORG_NAME"], basedefs.CONST_CA_ALIAS, basedefs.CONST_CA_PASS, date,
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
            messages.append(msg)

            # ExtractSSH fingerprint
            cmd = [basedefs.EXEC_SSH_KEYGEN, "-lf", basedefs.FILE_PUBLIC_SSH_KEY]
            finger, rc = utils.execCmd(cmd, None, True)
            msg = output_messages.INFO_CA_SSH_FINGERPRINT%(finger.split()[1])
            messages.append(msg)

            # Set right permissions
            _changeCaPermissions(basedefs.DIR_OVIRT_PKI)
        else:
            msg = output_messages.INFO_CA_KEYSTORE_EXISTS
            logging.warn(msg)
            messages.append(msg)

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
        fileHandler.editParam("authorityInfoAccess", " caIssuers;URI:http://%s:%s/ca.crt" % (conf["HOST_FQDN"], conf["HTTP_PORT"]))
        fileHandler.close()

def _configIptables():
    global messages
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

        for port in [conf["HTTP_PORT"], conf["HTTPS_PORT"]]:
            lineToAdd = "-A RH-Firewall-1-INPUT -m state --state NEW -p tcp --dport %s -j ACCEPT" % port
            list.insert(insertLocation, lineToAdd)

        if utils.compareStrIgnoreCase(conf["CONFIG_NFS"], "yes"):
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

        if conf["OVERRIDE_IPTABLES"] == "yes":
            if os.path.isfile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG)):
                backupFile = "%s.%s_%s"%(basedefs.FILE_IPTABLES_BACKUP, time.strftime("%H%M%S-%m%d%Y"), os.getpid())
                utils.copyFile("%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG), backupFile)
                messages.append(output_messages.INFO_IPTABLES_BACKUP_FILE%(backupFile))

            utils.copyFile(basedefs.FILE_IPTABLES_EXAMPLE, "%s/iptables"%(basedefs.DIR_ETC_SYSCONFIG))

            #stop the iptables explicitly, since we dont care about the status
            #of the current rules we will ignore the return code
            logging.debug("Restarting the iptables service")
            iptables = utils.Service("iptables")
            iptables.stop(True)
            iptables.start(True)

        else:
            messages.append(output_messages.INFO_IPTABLES_PORTS%(conf["HTTP_PORT"], conf["HTTPS_PORT"]))
            messages.append(output_messages.INFO_IPTABLES_FILE%(basedefs.FILE_IPTABLES_EXAMPLE))

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
    scriptHome = os.path.join(basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_INSTALL_SCRIPT)
    cmd = [scriptHome, dbLogFilename, conf["DB_PASS"]]

    # Create db using shell command
    output, rc = utils.execCmd(cmd, None, True, output_messages.ERR_DB_CREATE_FAILED, masked_value_set)
    logging.debug('Successfully installed %s db'%(basedefs.DB_NAME))

def _upgradeDB():
    """
    run db upgrade scripts
    required installed db.
    won't change db security settings
    """
    logging.debug("upgrading rhevm db schema")
    dbScriptArgs = "-u %s -d %s"%(basedefs.DB_ADMIN, basedefs.DB_NAME)
    cmd = os.path.join("/bin/sh ", basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_UPGRADE_SCRIPT + " " + dbScriptArgs)

    # Before db upgrade we want to make a backup of existing db in case we fail
    dbBackupFile = tempfile.mkstemp(suffix=".sql", dir=basedefs.DIR_DB_BACKUPS)[1] 
    logging.debug("backing up %s db to file %s"%(basedefs.DB_NAME, dbBackupFile))

    # Run db backup
    utils.backupDB(basedefs.DB_NAME, basedefs.DB_ADMIN, dbBackupFile)

    # Upgrade script must run from dbscripts dir
    currentDir = os.getcwd()
    os.chdir(basedefs.DIR_DB_SCRIPTS)

    try:
        # Run upgrade.sh script to update existing db
        output, rc = utils.execExternalCmd(cmd, True, output_messages.ERR_DB_UPGRADE_FAILED)

        # Go back to previous dir
        os.chdir(currentDir)
        logging.debug('Successfully upgraded %s db'%(basedefs.DB_NAME))
        messages.append("DB was upgraded to latest version. previous DB backup can be found at %s"%(dbBackupFile))

        # Update rpm version in vdc options
        utils.updateVDCOption("ProductRPMVersion", utils.getRpmVersion(basedefs.ENGINE_RPM_NAME))
    except:
        # Upgrade failed! we need to restore the old db
        logging.debug("%s DB upgrade failed, restoring db to previous state. db was backed up to %s"%(basedefs.DB_NAME, dbBackupFile))
        utils.restoreDB(basedefs.DB_NAME, basedefs.DB_ADMIN, dbBackupFile)
        raise Exception(output_messages.ERR_DB_UPGRADE_FAILED)

def _updateDefaultDCType():
    logging.debug("updating default data center storage type")
    newDcTypeNum = DC_TYPE.parse(str.upper(conf["DC_TYPE"]))
    sqlQuery = "select inst_update_default_storage_pool_type (%s)"%(newDcTypeNum)
    utils.execSqlCommand(basedefs.DB_ADMIN, basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_EXP_UPD_DC_TYPE%(basedefs.DB_NAME)) 

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
            "LocalAdminPassword":conf["AUTH_PASS"],
            "SSLEnabled": "true",
            "UseSecureConnectionWithServers": "true",
            "ScriptsPath":"/usr/share/ovirt-engine",
            "VdcBootStrapUrl":"http://" + conf["HOST_FQDN"] + ":" + conf["HTTP_PORT"] + "/Components/vds/",
            "AsyncPollingCyclesBeforeCallbackCleanup":"30",
            "SysPrepXPPath":"/usr/share/ovirt-engine/sysprep/sysprep.xp",
            "SysPrep2K3Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k3",
            "SysPrep2K8Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8x86",
            "SysPrep2K8x64Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8",
            "SysPrep2K8R2Path":"/usr/share/ovirt-engine/sysprep/sysprep.2k8",
            "SysPrepWindows7Path":"/usr/share/ovirt-engine/sysprep/sysprep.w7",
            "SysPrepWindows7x64Path":"/usr/share/ovirt-engine/sysprep/sysprep.w7x64",
            "MacPoolRanges":conf["MAC_RANGE"],
            "InstallVds":"true",
            "ConfigDir":"/etc/ovirt-engine",
            "DataDir":"/usr/share/ovirt-engine",
            "SignScriptName":"SignReq.sh",
            "BootstrapInstallerFileName":"/usr/share/ovirt-engine/scripts/vds_installer.py",
            "PublicURLPort":conf["HTTP_PORT"],
            "VirtualMachineDomainName":conf["HOST_FQDN"],
            "OrganizationName":conf["ORG_NAME"],
            "ProductRPMVersion":utils.getRpmVersion(basedefs.ENGINE_RPM_NAME),
            "AdminPassword":conf["AUTH_PASS"]
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

        #insert line for postgres - db admin
        #(very important for maintance and upgrades in case user rhevm is not created yet).
        line = _updatePgPassLine(basedefs.DB_HOST, basedefs.DB_PORT, "*", basedefs.DB_ADMIN, conf["DB_PASS"])
        pgPassFile.write(line + "\n")

        #insert line for user rhevm
        line = _updatePgPassLine(basedefs.DB_HOST, basedefs.DB_PORT, basedefs.DB_NAME, basedefs.DB_USER, conf["DB_PASS"])
        pgPassFile.write(line + "\n")

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
        cmd = [basedefs.EXEC_ENCRYPT_PASS, conf["DB_PASS"]]

        # The encrypt tool needs the jboss home env set
        # Since we cant use the bash way, we need to set it as environ
        os.environ["JBOSS_HOME"] = basedefs.JBOSS_SHARE_FOLDER
        output, rc = utils.execCmd(cmd, None, True, output_messages.ERR_EXP_ENCRYPT_PASS, masked_value_set)

        #parse the encrypted password from the tool
        conf["ENCRYPTED_DB_PASS"] = utils.parseStrRegex(output, "Encoded password:\s*(.+)", output_messages.ERR_EXP_PARSING_ENCRYPT_PASS)
    else:
        raise Exception(output_messages.ERR_ENCRYPT_TOOL_NOT_FOUND)

def _verifyUserPermissions():
    username = pwd.getpwuid(os.getuid())[0]
    if os.geteuid() != 0:
        sys.exit(output_messages.ERR_EXP_INVALID_PERM%(username))

def runFunction(funcs, displayString):
    #keep relative space
    spaceLen = basedefs.SPACE_LEN - len(displayString)
    try:
        print "%s..."%(displayString),
        sys.stdout.flush()
        if type(funcs) is types.ListType:
            for funcName in funcs:
                logging.debug("running %s"%(funcName.func_name))
                funcName()
        elif type(funcs) is types.FunctionType:
            logging.debug("running %s"%(funcs.func_name))
            funcs()
        else:
            logging.error("%s is not a supported type"%(type(funcs)))
            raise Exception(output_messages.ERR_EXP_RUN_FUNCTION)
    except Exception, (instance):
        print ("[ " + _getColoredText(output_messages.INFO_ERROR, basedefs.RED) + " ]").rjust(spaceLen)
        raise Exception(instance)
    print ("[ " + _getColoredText(output_messages.INFO_DONE, basedefs.GREEN) + " ]").rjust(spaceLen)

def _addDefaultsToMaskedValueSet():
    """
    For every param in conf_params
    that has MASK_INPUT enabled keep the default value 
    in the 'masked_value_set'
    """
    global masked_value_set
    for param in conf_params:
        # Keep default password values masked, but ignore default empty values
        if ((param["MASK_INPUT"] == True) and param["DEFAULT_VALUE"] != ""):
            masked_value_set.add(param["DEFAULT_VALUE"])

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
    for confName in conf:
        # Add all needed values to masked_value_set
        if (_getParamKeyValue(confName, "MASK_INPUT") == True):
            masked_value_set.add(conf[confName])

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

def _validateParamValue(paramName, paramValue):
    validateFunc = _getParamKeyValue(paramName, "VALIDATION_FUNC")
    optionsList  = _getParamKeyValue(paramName, "OPTION_LIST")
    logging.debug("validating param %s in answer file."%(paramName))
    if (not validateFunc(paramValue, optionsList)):
        raise Exception(output_messages.ERR_EXP_VALIDATE_PARAM % (paramName))

def _handleGroupCondition(config, conditionName, conditionValue):
    """
    handle params group pre/post condition
    checks if a group has a pre/post condition
    and validates the params related to the group
    """
    global conf

    # If the post condtition is a function
    if type(conditionName) == types.FunctionType:
        # Call the function conditionName with conf as the arg
        conditionValue = conditionName(conf)

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
    global conf

    # Get paramName from answer file
    value = config.get(section, paramName)

    # Validate param value using its validation func
    _validateParamValue(paramName, value)

    # Keep param value in our never ending global conf
    conf[paramName] = value

    return value

def _handleAnswerFileParams(answerFile):
    """
    handle loading and validating
    params from answer file
    supports reading single or group params
    """
    global conf
    try:
        logging.debug("Starting to handle config file")

        # Read answer file
        fconf = ConfigParser.ConfigParser()
        fconf.read(answerFile)

        # Iterate all the groups and check the pre/post conditions
        for group in conf_groups:
            # Get all params per group

            # Handle pre conditions for group
            preConditionValue = True
            if group["PRE_CONDITION"]:
                preConditionValue = _handleGroupCondition(fconf, group["PRE_CONDITION"], preConditionValue)

            # Handle pre condition match with case insensitive values
            if utils.compareStrIgnoreCase(preConditionValue, group["PRE_CONDITION_MATCH"]):
                paramsList = _getParamsPerGroup(group["GROUP_NAME"])
                for paramName in paramsList:
                    _loadParamFromFile(fconf, "general", paramName)

                # Handle post conditions for group only if pre condition passed
                postConditionValue = True
                if group["POST_CONDITION"]:
                    postConditionValue = _handleGroupCondition(fconf, group["POST_CONDITION"], postConditionValue)

                    # Handle post condition match for group
                    if not utils.compareStrIgnoreCase(postConditionValue, group["POST_CONDITION_MATCH"]):
                        logging.error("The group condition (%s) returned: %s, which differs from the excpeted output: %s"%\
                                      (group["GROUP_NAME"], postConditionValue, group["POST_CONDITION_MATCH"]))
                        raise ValueError(output_messages.ERR_EXP_GROUP_VALIDATION_ANS_FILE%\
                                         (group["GROUP_NAME"], postConditionValue, group["POST_CONDITION_MATCH"]))
                    else:
                        logging.debug("condition (%s) passed"%(group["POST_CONDITION"]))
                else:
                    logging.debug("no post condition check for group %s"%(group["GROUP_NAME"]))
            else:
                logging.debug("skipping params group %s since value of group validation is %s"%(group["GROUP_NAME"], preConditionValue))

    except Exception as e:
        raise Exception(output_messages.ERR_EXP_HANDLE_ANSWER_FILE%(e))

def _handleInteractiveParams():
    global conf
    try:
        for group in conf_groups:
            preConditionValue = True
            logging.debug("going over group %s" % (group))

            # If pre_condition is set, get Value
            if group["PRE_CONDITION"]:
                preConditionValue = _getConditionValue(group["PRE_CONDITION"])

            inputLoop = True

            # If we have a match, i.e. condition returned True, go over all params in the group
            if utils.compareStrIgnoreCase(preConditionValue, group["PRE_CONDITION_MATCH"]):
                while inputLoop:
                    paramsList = _getParamsPerGroup(group["GROUP_NAME"])

                    for paramName in paramsList:
                        input_param(_getParamPerName(paramName))
                        #update password list, so we know to mask them
                        _updateMaskedValueSet()

                    postConditionValue = True

                    # If group has a post condition, we check it after we get the input from
                    # all the params in the group. if the condition returns False, we loop over the group again
                    if group["POST_CONDITION"]:
                        postConditionValue = _getConditionValue(group["POST_CONDITION"])

                        if postConditionValue == group["POST_CONDITION_MATCH"]:
                            inputLoop = False
                        else:
                            #we clear the value of all params in the group
                            #in order to re-input them by the user
                            for paramName in paramsList:
                                if conf.has_key(paramName):
                                    del conf[paramName]
                                if commandLineValues.has_key(paramName):
                                    del commandLineValues[paramName]
                    else:
                        inputLoop = False
            else:
                logging.debug("no post condition check for group %s" % group["GROUP_NAME"])

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
    global conf
    if type(matchMember) == types.FunctionType:
        returnValue = matchMember(conf)
    elif type(matchMember) == types.StringType:
        #we assume that if we get a string as a member it is the name
        #of a member of conf_params
        if not conf.has_key(matchMember):
            paramDict = _getParamPerName(matchMember)
            input_param(paramDict)
        returnValue = conf[matchMember]
    else:
        raise TypeError("%s type (%s) is not supported"%(matchMember, type(matchMember)))

    return returnValue

def _displaySummary():
    global conf

    print output_messages.INFO_DSPLY_PARAMS
    print  "=" * (len(output_messages.INFO_DSPLY_PARAMS) - 1)
    logging.info("*** User input summary ***")
    for group in conf_groups:
        paramsList = _getParamsPerGroup(group["GROUP_NAME"])
        for paramName in paramsList:
            useDefault = _getParamKeyValue(paramName, "USE_DEFAULT")
            if not useDefault and conf.has_key(paramName):
                cmdOption = _getParamKeyValue(paramName, "CMD_OPTION")
                l = 30 - len(cmdOption)
                maskParam = _getParamKeyValue(paramName, "MASK_INPUT")
                # Only call mask on a value if the param has MASK_INPUT set to True
                if maskParam:
                    logging.info("%s: %s" % (cmdOption, mask(conf[paramName])))
                    print "%s:" % (cmdOption) + " " * l + mask(conf[paramName])
                else:
                    # Otherwise, log & display it as it is
                    logging.info("%s: %s" % (cmdOption, conf[paramName]))
                    print "%s:" % (cmdOption) + " " * l + conf[paramName]
    logging.info("*** User input summary ***")
    answer = _askYesNo(output_messages.INFO_USE_PARAMS)
    if not answer:
        logging.debug("user chose to re-enter the user parameters")
        for group in conf_groups:
            paramsList = _getParamsPerGroup(group["GROUP_NAME"])
            preCond = group["PRE_CONDITION"]
            postCond = group["POST_CONDITION"]
            for item in [preCond, postCond]:
                if type(item)== types.StringType:
                    #item is a string, we assume a param name
                    paramsList.append(item)
            for paramName in paramsList:
                if conf.has_key(paramName):
                    param = _getParamPerName(paramName)
                    if not param["MASK_INPUT"]:
                        param["DEFAULT_VALUE"] = conf[paramName]
                    # Remove the string from mask_value_set in order
                    # to remove values that might be over overwritten. 
                    removeMaskString(conf[paramName])
                    del conf[paramName]
                if commandLineValues.has_key(paramName):
                    del commandLineValues[paramName]
            print ""
        logging.debug("calling handleParams in interactive mode")
        return _handleParams(None)
    else:
        logging.debug("user chose to accept user parameters")

def _startJboss():
    logging.debug("using chkconfig to enable jboss to load on system startup.")
    output, rc = utils.execExternalCmd("/sbin/chkconfig jboss-as on", True, output_messages.ERR_FAILED_CHKCFG_JBOSS)
    _handleJbossService('stop', output_messages.INFO_STOP_JBOSS, output_messages.ERR_FAILED_STP_JBOSS_SERVICE, False)
    _handleJbossService('start', output_messages.INFO_START_JBOSS, output_messages.ERR_FAILED_START_JBOSS_SERVICE, False)
    
def _configNfsShare():
    #ISO_DOMAIN_NAME, NFS_MP
    try:
        logging.debug("configuring NFS share")

        # If path does not exist, create it
        if not os.path.exists(conf["NFS_MP"]):
            logging.debug("creating directory %s " % (conf["NFS_MP"]))
            os.makedirs(conf["NFS_MP"])
        # Add export to exportfs
        nfsutils.addNfsExport(conf["NFS_MP"], (("0.0.0.0", "0.0.0.0", ("rw",)),), "rhev installer")

        # Add warning to user about nfs export permissions
        messages.append(output_messages.WARN_ISO_DOMAIN_SECURITY % (conf["NFS_MP"]))

        # Set selinux configuration
        nfsutils.setSELinuxContextForDir(conf["NFS_MP"], nfsutils.SELINUX_RW_LABEL)

        #set NFS/portmap ports by overriding /etc/sysconfig/nfs
        backupFile = "%s.%s_%s"%(basedefs.FILE_NFS_BACKUP, time.strftime("%H%M%S-%m%d%Y"), os.getpid())
        utils.copyFile("%s/nfs"%(basedefs.DIR_ETC_SYSCONFIG), backupFile)
        utils.copyFile(basedefs.FILE_NFS_SYSCONFIG, "%s/nfs"%(basedefs.DIR_ETC_SYSCONFIG))

        # Start services
        _startNfsServices()

        # Generate the UUID for the isodomain
        conf["sd_uuid"] = nfsutils.generateUUID()

        # Create ISO domain
        nfsutils.createISODomain(conf["NFS_MP"], conf["ISO_DOMAIN_NAME"], conf["sd_uuid"])

        # Add iso domain to DB
        _addIsoDomaintoDB(conf["sd_uuid"], conf["ISO_DOMAIN_NAME"])

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
    sqlQuery = "select inst_add_iso_storage_domain ('%s', '%s', '%s:%s', %s, %s)" % (uuid, description, conf["HOST_FQDN"], conf["NFS_MP"], 0, 0)
    utils.execSqlCommand(basedefs.DB_ADMIN, basedefs.DB_NAME, sqlQuery, True, output_messages.ERR_FAILED_INSERT_ISO_DOMAIN%(basedefs.DB_NAME)) 

def _startNfsServices():
    logging.debug("Enabling the rpcbind & nfs services")
    try:
        for service in ["rpcbind", "nfs-server"]:
            cmd = "/sbin/chkconfig %s on"%(service)
            utils.execExternalCmd(cmd, True, output_messages.ERR_FAILED_CHKCFG_NFS%(service))
            cmd = "/sbin/service %s stop"%(service)
            utils.execExternalCmd(cmd, False)
            cmd = "/sbin/service %s start"%(service)
            utils.execExternalCmd(cmd, True, output_messages.ERR_RESTARTING_NFS_SERVICE%(service))
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
    targetPath = os.path.join(conf["NFS_MP"], conf["sd_uuid"], "images", "11111111-1111-1111-1111-111111111111")

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
    for msg in messages:
        logging.info(output_messages.INFO_ADDTIONAL_MSG_BULLET%(msg))
        print output_messages.INFO_ADDTIONAL_MSG_BULLET%(msg)

def _addFinalInfoMsg():
    """
    add info msg to the user finalizing the 
    successfull install of rhemv
    """
    messages.append(output_messages.INFO_LOG_FILE_PATH%(logFile))
    messages.append(output_messages.INFO_LOGIN_USER)
    messages.append(output_messages.INFO_ADD_USERS)
    messages.append(output_messages.INFO_RHEVM_URL % conf["HTTP_URL"])

def _checkJbossService(configFile):
    logging.debug("checking the status of jboss")
    cmd = "%s jboss-as status" % (basedefs.EXEC_SERVICE)
    output, rc = utils.execExternalCmd(cmd)
    if 0 == rc:
        logging.debug("jboss-as is up and running")

        #if we don't use an answer file, we need to ask the user if to stop jboss
        if not configFile:
            print output_messages.INFO_NEED_STOP_JBOSS
            answer = _askYesNo(output_messages.INFO_Q_STOP_JBOSS)
            if answer:
                _handleJbossService('stop', output_messages.INFO_STOP_JBOSS, output_messages.ERR_FAILED_STP_JBOSS_SERVICE, True)
            else:
                logging.debug("User chose not to stop jboss")
                return False
        else:
            #we stop the jboss service on a silent install
            _handleJbossService('stop', output_messages.INFO_STOP_JBOSS, output_messages.ERR_FAILED_STP_JBOSS_SERVICE, True)
    return True

def _handleJbossService(action, infoMsg, errMsg, printToStdout=False):
    ''' stop or start the jbossas service
        according to action param
    '''
#    cmd = "%s jboss %s"  % (basedefs.EXEC_SERVICE, action)
    cmd = ["%s" % basedefs.EXEC_SERVICE, "jboss-as", "%s" % action]
    logging.debug(infoMsg)
    if printToStdout:
        print infoMsg
    utils.execCmd(cmdList=cmd,failOnError=True, msg=errMsg)

#def _lockRpmVersion():
#    """
#    Enters RHEVM rpm versions into yum version-lock
#    """
#    logging.debug("Locking rpms in yum-version-lock")
#    cmd = "%s -q %s >> %s"%(basedefs.EXEC_RPM, basedefs.RPM_LOCK_LIST, basedefs.FILE_YUM_VERSION_LOCK)
#    output, rc = utils.execExternalCmd(cmd, True, output_messages.ERR_YUM_LOCK)

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
            fileHandler.editParam("engine", "%s:%s" % (conf["HOST_FQDN"], conf["HTTPS_PORT"]))

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
    if len(conf) > 0:
        logging.debug("*** The following params were used as user input:")
        for param in conf_params:
            if conf.has_key(param["CONF_NAME"]):
                maskedValue = mask(conf[param["CONF_NAME"]])
                logging.debug("%s: %s" % (param["CMD_OPTION"], maskedValue ))

def restartPostgresql():
    """
    restart the postgresql service
    """

    logging.debug("Restarting the postgresql service")
    postgresql = utils.Service("postgresql")
    postgresql.stop(True)
    postgresql.start(True)

    # Now we want to make sure the postgres service is up
    # before we continue to the upgrade
    utils.retry(utils.checkIfRhevmDbIsUp, tries=10, timeout=30)

def _isDbAlreadyInstalled():
    logging.debug("checking if rhevm db is already installed..")
    (out, rc) = utils.execSqlCommand(basedefs.DB_ADMIN, basedefs.DB_NAME, "select 1")
    if (rc != 0):
        return False
    else:
        return True

def stopRhevmDbRelatedServices(etlService, notificationService):
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
            messages.append(output_messages.ERR_FAILED_STOP_SERVICE % "rhevm-etl")

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
            messages.append(output_messages.ERR_FAILED_STOP_SERVICE % "rhevm-notiferd")

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

    datasourceStr = '''
        <datasource jndi-name="java:/ENGINEDataSource" pool-name="ENGINEDataSource" enabled="true">
        <connection-url>
            jdbc:postgresql://localhost:5432/engine
        </connection-url>
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
'''
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
                    <module-option name="password" value="'''+ conf["ENCRYPTED_DB_PASS"] +'''"/>
                    <module-option name="managedConnectionFactoryName" value="jboss.jca:name=ENGINEDataSource,service=LocalTxCM"/>
                </login-module>
            </authentication>
        </security-domain>
'''
    xmlObj.addNodes("//security:subsystem/security:security-domains", securityPassStr)

#    node = xmlObj.xpathEval("//security:subsystem/security:security-domains/security:security-domain[@name='EncryptDBPassword']/security:authentication/security:login-module/security:module-option[@name='password']")[0]
#    node.setProp('value', conf["ENCRYPTED_DB_PASS"])

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
    httpNode.setProp("port", conf["HTTP_PORT"])

    httpsNode = xmlObj.xpathEval("//domain:server/domain:socket-binding-group[@name='standard-sockets']/domain:socket-binding[@name='https']")[0]
    httpsNode.setProp("port", conf["HTTPS_PORT"])

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

def startRhevmDbRelatedServices(etlService, notificationService):
    """
    bring back any service we stopped
    we won't start services that are down
    but weren't stopped by us
    """
    (output, rc) = etlService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-etl")
        messages.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-etl")

    (output, rc) = notificationService.conditionalStart()
    if rc != 0:
        logging.warn("Failed to start rhevm-notifierd")
        messages.append(output_messages.ERR_FAILED_START_SERVICE % "rhevm-notifierd")

def runMainFunctions(conf):
    # Create rhevm-slimmed jboss profile
    runFunction([_createJbossProfile, setMaxSharedMemory], output_messages.INFO_CONFIG_OVIRT_ENGINE)

    # Create CA
    runFunction(_createCA, output_messages.INFO_CREATE_CA)

    # Edit JBoss configuration
    runFunction([configJbossXml, deployJbossModules, _editRootWar], output_messages.INFO_UPD_JBOSS_CONF)

    # Install rhevm db if it's not installed and running already
    if _isDbAlreadyInstalled() == False:
        # Handle db authtication and password encryption
        runFunction([_updatePgPassFile, _encryptDBPass, configEncryptedPass], output_messages.INFO_SET_DB_SECURITY)

        # Create db, update vdc_options table and attach user to su role
        runFunction([_createDB,  _updateVDCOptions], output_messages.INFO_CREATE_DB)

        # Update default dc type
        runFunction(_updateDefaultDCType, output_messages.INFO_UPD_DC_TYPE)

    else:
        # Stop ETL and NOTIFIER if services are available and running
        etlService = utils.Service("rhevm-etl")
        notificationService = utils.Service("rhevm-notifierd")

        # Close any db connections that might intefere with db upgrade
        stopRhevmDbRelatedServices(etlService, notificationService)

        # If db already installed, we support db upgrade without deleting exiting data
        runFunction([restartPostgresql, _upgradeDB], output_messages.INFO_UPGRADE_DB)

        # Bring up any services we shut down before db upgrade
        startRhevmDbRelatedServices(etlService, notificationService)

    # Update default html welcome pages & conf files for isouploader & logcollector
    #runFunction([_editExternalConfig, _editDefaultHtml, _editToolsConfFile, editPostgresConf, updateFileDescriptors], output_messages.INFO_UPD_RHEVM_CONF)
    runFunction([_editToolsConfFile, editPostgresConf, updateFileDescriptors], output_messages.INFO_UPD_RHEVM_CONF)

    if utils.compareStrIgnoreCase(conf["CONFIG_NFS"], "yes"):
        # Config iso domain folder and load existing iso files
        runFunction([_configNfsShare], output_messages.INFO_CFG_NFS)

    # Config iptables
    runFunction(_configIptables, output_messages.INFO_CFG_IPTABLES)

    # Start jboss at the end
    runFunction([_startJboss], output_messages.INFO_START_JBOSS)

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

def main(configFile=None):
    global conf
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
        logging.debug(mask(conf))

        # Start configuration stage
        logging.debug("Entered Configuration stage")
        print "\n",output_messages.INFO_INSTALL

        # Run main setup logic
        runMainFunctions(conf)

        # Lock rhevm version
        #_lockRpmVersion()

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
    for param in conf_params:
        content.write("%s=%s%s"%(param["CONF_NAME"], param["DEFAULT_VALUE"],os.linesep))
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
        messages.append(output_messages.WARN_LOW_MEMORY)

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
    for group in conf_groups:
        groupParser = OptionGroup(parser, group["DESCRIPTION"])
        params = _getParamsPerGroup(group["GROUP_NAME"])
        # If the pre/post conditions are params, add them to the params list
        for condition in ["PRE_CONDITION", "POST_CONDITION"]:
            if group[condition] and type(group[condition]) == types.StringType:
                params.append(group[condition])

        # Add each param of this group into the group's parser
        for param in params:
            cmdOption = _getParamKeyValue(param, "CMD_OPTION")
            paramUsage = _getParamKeyValue(param, "USAGE")
            optionsList = _getParamKeyValue(param, "OPTION_LIST")
            useDefault = _getParamKeyValue(param, "USE_DEFAULT")
            # If the param has a populted options_list also provide the choices
            # directive
            if not useDefault:
                if optionsList:
                    groupParser.add_option("--%s" % cmdOption, metavar=optionsList, help=paramUsage, choices=optionsList)
                else:
                    groupParser.add_option("--%s" % cmdOption, help=paramUsage)

        # Add group parser to main parser
        parser.add_option_group(groupParser)

    return parser

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

def initMain():
    #verify that root is the user executing the script - only supported user for now.
    #TODO: check how we can change this to user rhevm
    _verifyUserPermissions()

    # Initialize logging
    initLogging() 

    # Initialize configuration
    initConfig()

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
                    list = _getParamsPerKey("CMD_OPTION", key.replace("_","-"))
                    for param in list:
                        if value:
                            commandLineValues[param["CONF_NAME"]] = value
            main(confFile)

    except SystemExit:
        raise

    except BaseException as e:
        logging.error(traceback.format_exc())
        print e
        print output_messages.ERR_CHECK_LOG_FILE_FOR_MORE_INFO%(logFile)
        sys.exit(1)
