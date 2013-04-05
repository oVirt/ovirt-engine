"""
All in one plugin.
Installs and configures VDSM on the same host as ovirt-engine
"""

import logging
import sys
import os
import os.path
import urllib2
import crypt
import time
import nfsutils
import traceback
import engine_validators as validate
import basedefs
import common_utils as utils
from ovirtsdk.api import API
from ovirtsdk.xml import params

# Override basedefs default so that status message are aligned
basedefs.SPACE_LEN = 80

# Product version
MAJOR = '3'
MINOR = '2'

# Controller object will be initialized from main flow
controller = None

# Plugin name
PLUGIN_NAME = "AIO"
PLUGIN_NAME_COLORED = utils.getColoredText(PLUGIN_NAME, basedefs.BLUE)

# INFO Messages
INFO_CONF_PARAMS_ALL_IN_ONE_USAGE = "Configure all in one"
INFO_CONF_PARAMS_ALL_IN_ONE_PROMPT = "Configure VDSM on this host?"
INFO_CONF_PARAMS_LOCAL_STORAGE = "Local storage domain path"
INFO_LIBVIRT_START = "libvirt service is started"
INFO_CREATE_HOST_WAITING_UP = "Waiting for the host to start"

# ERROR MESSAGES
SYSTEM_ERROR = "System Error"
ERROR_CREATE_API_OBJECT = "Error: could not create ovirtsdk API object"
ERROR_CREATE_LOCAL_DATACENTER = "Error: Could not create local datacenter"
ERROR_CREATE_LOCAL_CLUSTER = "Error: Could not create local cluster"
ERROR_CREATE_LOCAL_HOST = "Error: Could not install local host"
ERROR_CREATE_HOST_FAILED = "Error: Host was found in a 'Failed' state. Please check engine and bootstrap installation logs."
ERROR_CREATE_HOST_TIMEOUT = "Error: Timed out while waiting for host to start"
ERROR_ADD_LOCAL_DOMAIN = "Error: could not add local storage domain"
ERROR_CREATE_STORAGE_PATH = "Error: could not create local domain path"
ERROR_UNSUPPORTED_CPU_MODEL = "Error: This host does not have any of the supported CPU models"
ERROR_CAPABILITIES = "Error: couldn't obtain host CPU capabilities."
ERROR_LIBVIRT_START = "Error: Could not start libvirt service. Please try \
starting it manually and rerun the setup."
ERROR_LIBVIRT_STATUS = "Error: Could not get status of the libvirt service"
ERROR_JBOSS_STATUS = "Error: There's a problem with JBoss service.\
Check that it's up and rerun setup."
ERROR_SSHD_START = "Error: sshd service could not be started. Cannot continue"
# PARAMS
PAUSE = 10
SLEEP_PERIOD = 25 # period in seconds, this is waiting until JBoss is up
MAX_CYCLES = 33 # (5.5 (minutes) * 60 )/ 10, since we sleep 10 seconds after each iteration
LOCAL_STORAGE_MIN_SIZE = 10 # Size in Gb
API_OBJECT_PATH = "https://%s:%s/api"
JBOSS_HEALTH_URL = "http://%s:%s/OvirtEngineWeb/HealthStatus"
LOCAL_CLUSTER = "local_cluster"
LOCAL_DATA_CENTER = "local_datacenter"
LOCAL_HOST = "local_host"
LOCAL_STORAGE = "local_storage"
LOCAL_STORAGE_PATH = "/var/lib/images"

# PATH PARAMS
VDSM_PATH = "/usr/share/vdsm"
SHADOW_FILE = "/etc/shadow"

logging.debug("plugin %s loaded", __name__)


def _useDefaultConfigNfs(conf):
    # When gluster mode is selected then don't ask the questions related
    # to NFS setup, so update default value for CONFIG_NFS as "no" if gluster
    # is selected in application mode prompt(NFS_MP and ISO_DOMAIN_NAME).
    if utils.isApplicationModeGluster(conf):
        controller.getParamByName("CONFIG_NFS").setKey("DEFAULT_VALUE", "no")
    else:
        controller.getParamByName("CONFIG_NFS").setKey("DEFAULT_VALUE", "yes")
    return True


def generateLocalStorageDomainPath():
    '''
    Generates name for local storage domain
    '''
    if os.path.exists(LOCAL_STORAGE_PATH):
        return "%s_%s" % (LOCAL_STORAGE_PATH,
                          utils.getCurrentDateTime())
    else:
        return LOCAL_STORAGE_PATH


def initConfig(controllerObject):
    global controller
    controller = controllerObject
    logging.debug("Adding parameters for VDSM configuration")
    paramsList = [{
                   "CMD_OPTION"      : "config-allinone",
                   "USAGE"           : INFO_CONF_PARAMS_ALL_IN_ONE_USAGE,
                   "PROMPT"          : INFO_CONF_PARAMS_ALL_IN_ONE_PROMPT,
                   "OPTION_LIST"     : ["yes", "no"],
                   "VALIDATION_FUNC" : validate.validateOptions,
                   "DEFAULT_VALUE"   : "yes",
                   "MASK_INPUT"      : False,
                   "LOOSE_VALIDATION": False,
                   "CONF_NAME"       : "CONFIG_ALLINONE",
                   "USE_DEFAULT"     : False,
                   "NEED_CONFIRM"    : False,
                   "CONDITION"       : True },
                  {
                   "CMD_OPTION"      : "storage-path",
                   "USAGE"           : INFO_CONF_PARAMS_LOCAL_STORAGE,
                   "PROMPT"          : INFO_CONF_PARAMS_LOCAL_STORAGE,
                   "OPTION_LIST"     : [],
                   "VALIDATION_FUNC" : validateStoragePath,
                   "DEFAULT_VALUE"   : generateLocalStorageDomainPath(),
                   "MASK_INPUT"      : False,
                   "LOOSE_VALIDATION": True,
                   "CONF_NAME"       : "STORAGE_PATH",
                   "USE_DEFAULT"     : False,
                   "NEED_CONFIRM"    : False,
                   "CONDITION"       : False },
                  {
                   "CMD_OPTION"      : "superuser-pass",
                   "USAGE"           : "root password",
                   "PROMPT"          : "Confirm root password",
                   "OPTION_LIST"     : [],
                   "VALIDATION_FUNC" : validateSuperUserPasswd,
                   "DEFAULT_VALUE"   : "",
                   "MASK_INPUT"      : True,
                   "LOOSE_VALIDATION": False,
                   "CONF_NAME"       : "SUPERUSER_PASS",
                   "USE_DEFAULT"     : False,
                   "NEED_CONFIRM"    : False,
                   "CONDITION"       : False} ]

    groupDict = { "GROUP_NAME"            : "VDSM",
                  "DESCRIPTION"           : "VDSM Configuration",
                  "PRE_CONDITION"         : "CONFIG_ALLINONE",
                  "PRE_CONDITION_MATCH"   : "yes",
                  "POST_CONDITION"        : False,
                  "POST_CONDITION_MATCH"  : True}

    controller.addGroup(groupDict, paramsList)

    # Hack:
    # We disable the question regarding the NFS configuration (ISO domain) because we always want
    # it in All In One installation.
    controller.getParamByName("CONFIG_NFS").setKey("USE_DEFAULT", _useDefaultConfigNfs)
    controller.getParamByName("CONFIG_NFS").setKey("CONDITION", False)


def initSequences(controller):
    logging.debug("Setting the Sequences for VDSM all in one installation")
    cpuSteps =  [{ 'title' : "%s: Validating CPU Compatibility" % PLUGIN_NAME_COLORED,
                    'functions' : [startLibvirt, getSupportedCpus, getCPUFamily] } ]
    logging.debug("Setting sequence to validate cpu")
    controller.insertSequenceBeforeSequence("Initial Steps",
                                            "Validate CPU",
                                            [controller.CONF["CONFIG_ALLINONE"]],
                                            ["yes"],
                                            cpuSteps)
    controller.insertSequenceBeforeSequence(
        "Initial Steps",
        "Add firewall rules",
        [controller.CONF["CONFIG_ALLINONE"]],
        ["yes"],
        [{
                'title' : "%s: Adding firewall rules" % PLUGIN_NAME_COLORED,
                'functions' : [addFirewallRules]
        }]
    )


    # Main AIO sequences
    aioSteps = [ { 'title'     : "%s: Creating storage directory" % PLUGIN_NAME_COLORED,
                   'functions' : [makeStorageDir] },
                 { 'title'     : "%s: Adding Local Datacenter and cluster" % PLUGIN_NAME_COLORED,
                   'functions' : [waitForJbossUp, initAPI, createDC, createCluster]},
                 { 'title'     : "%s: Adding Local host (This may take several minutes)" % PLUGIN_NAME_COLORED,
                   'functions' : [createHost, waitForHostUp]},
                 { 'title'     : "%s: Adding Local storage (This may take several minutes)" % PLUGIN_NAME_COLORED,
                   'functions' : [addStorageDomain]} ]
    logging.debug("Adding sequence to create host")
    controller.addSequence("Local host", [controller.CONF["CONFIG_ALLINONE"]], ["yes"], aioSteps)

def startLibvirt():
    """ Start service libvirt """
    libvirtService = utils.Service("libvirtd")

    # Check status and return if up
    output, rc = libvirtService.status()
    if rc == 0:
        logging.info(INFO_LIBVIRT_START)
        return

    # Otherwise, start the service
    output, rc = libvirtService.start(True)
    cycle = 1
    while cycle <= MAX_CYCLES:
        output, rc = libvirtService.status()
        if rc == 0:
            logging.info(INFO_LIBVIRT_START)
            return
        cycle += 1
        time.sleep(PAUSE)

    raise Exception(ERROR_LIBVIRT_START)

def addFirewallRules():
    global controller

    if basedefs.CONST_CONFIG_EXTRA_IPTABLES_RULES not in controller.CONF:
        controller.CONF[basedefs.CONST_CONFIG_EXTRA_IPTABLES_RULES] = []

    controller.CONF[basedefs.CONST_CONFIG_EXTRA_IPTABLES_RULES] += [
        '#guest consoles',
        '-A INPUT -p tcp -m state --state NEW -m multiport --dports 5634:6166  -j ACCEPT',
        '#migration',
        '-A INPUT -p tcp -m state --state NEW -m multiport --dports 49152:49216 -j ACCEPT'
    ]

    if basedefs.CONST_CONFIG_EXTRA_FIREWALLD_RULES not in controller.CONF:
        controller.CONF[basedefs.CONST_CONFIG_EXTRA_FIREWALLD_RULES] = []
    controller.CONF[basedefs.CONST_CONFIG_EXTRA_FIREWALLD_RULES].append('ovirt-aio')

def waitForJbossUp():
    """
    Wait for Jboss to start
    """
    utils.retry(isHealthPageUp, tries=25, timeout=15, sleep=5)

def initAPI():
    global controller
    logging.debug("Initiating the API object")

    URL = API_OBJECT_PATH % (controller.CONF["HOST_FQDN"], controller.CONF["HTTPS_PORT"])
    USERNAME = 'admin@internal'
    try:
        controller.CONF["API_OBJECT"] = API(url=URL,
                                        username=USERNAME,
                                        password=controller.CONF['AUTH_PASS'],
                                        ca_file=basedefs.FILE_CA_CRT_SRC,
                                    )
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CREATE_API_OBJECT)

def createDC():
    global controller
    logging.debug("Creating the local datacenter")
    try:
        controller.CONF["API_OBJECT"].datacenters.add(params.DataCenter(name=LOCAL_DATA_CENTER,
                                                                           storage_type='localfs',
                                                                           version=params.Version(major=MAJOR, minor=MINOR)))
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CREATE_LOCAL_DATACENTER)

def createCluster():
    global controller
    logging.debug("Creating the local cluster")
    CPU_TYPE = controller.CONF['VDSM_CPU_FAMILY']
    try:
        controller.CONF["API_OBJECT"].clusters.add(params.Cluster(name=LOCAL_CLUSTER,
                                                                     cpu=params.CPU(id=CPU_TYPE),
                                                                     data_center=controller.CONF["API_OBJECT"].datacenters.get(LOCAL_DATA_CENTER),
                                                                     version=params.Version(major=MAJOR, minor=MINOR)))
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CREATE_LOCAL_CLUSTER)

def createHost():
    global controller
    logging.debug("Adding the local host")
    try:
        sshd = utils.Service("sshd")
        if sshd.available():
            sshd.autoStart()
            sshd.start()
        else:
            raise Exception(ERROR_SSHD_START)
        controller.CONF["API_OBJECT"].hosts.add(params.Host(name=LOCAL_HOST,
                                                               address=controller.CONF["HOST_FQDN"],
                                                               reboot_after_installation=False,
                                                               cluster=controller.CONF["API_OBJECT"].clusters.get(LOCAL_CLUSTER),
                                                               root_password=controller.CONF["SUPERUSER_PASS"]))
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CREATE_LOCAL_HOST)

def waitForHostUp():
    # We will wait for 10 minutes for host to start. We sample the status each 5 seconds.
    # Also, if host is in Failed mode, fail immediately, don't cycle.
    utils.retry(isHostUp, tries=120, timeout=600, sleep=5)

def isHostUp():
    logging.debug("Waiting for host to become operational")
    try:
        hostStatus = controller.CONF['API_OBJECT'].hosts.get(LOCAL_HOST).status.state
        logging.debug("current host status is: %s", hostStatus)
        if hostStatus == "up":
            logging.debug("The host is up.")
            return

        if "failed" in hostStatus:
            raise utils.RetryFailException(ERROR_CREATE_HOST_FAILED)

        raise Exception(INFO_CREATE_HOST_WAITING_UP)

    except Exception as e:
        logging.debug(traceback.format_exc())
        # Raise the exception again
        raise e

def addStorageDomain():
    global controller
    logging.debug("Adding local storage domain")

    # strip last '/' from path if it's given. Otherwise, adding storage will
    # fail.
    try:
        logging.info("Creating local storage")
        stParams = params.Storage(path=controller.CONF["STORAGE_PATH"].rstrip('/'))
        stParams.set_type('localfs')

        logging.info("Creating local storage domain")
        sdParams = params.StorageDomain(name=LOCAL_STORAGE,
                                    data_center=controller.CONF["API_OBJECT"].datacenters.get(LOCAL_DATA_CENTER),
                                    storage_format='v3',
                                    host=controller.CONF["API_OBJECT"].hosts.get(LOCAL_HOST),
                                    storage=stParams)
        sdParams.set_type('data')

        logging.info("Adding local storage domain")
        controller.CONF["API_OBJECT"].storagedomains.add(sdParams)
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_ADD_LOCAL_DOMAIN)

def validateSuperUserPasswd(param, options=[]):
    logging.debug("Validating superuser password")
    logging.debug("Reading encrypted password from %s", SHADOW_FILE)
    encryptedPasswd = None
    with open(SHADOW_FILE, "r") as f:
        rootString = f.readline()
        encryptedPasswd = rootString.split(':')[1]

    # If we encrypt the given string with the salt which is the
    # encrypted text password, the result should match the encrypted password.
    # Read more on crypt.crypt if this is giberish to you.
    if crypt.crypt(param, encryptedPasswd) == encryptedPasswd:
        logging.debug("Superuser password is validated")
        return True

    return False

def validateStoragePath(param, options = []):
    """
    Validate that a given path is a valid mount point and has at least LOCAL_STORAGE_MIN_SIZE GB.
    """
    logging.info("Validating provided storage path")
    if validate.validateMountPoint(param) and validate.validateDirSize(param, LOCAL_STORAGE_MIN_SIZE * 1024):
            return True

    return False

def makeStorageDir():
    logging.debug("Creating/Verifying local domain path")
    try:
        if not os.path.exists(controller.CONF["STORAGE_PATH"]):
            logging.debug("Creating directory %s ", controller.CONF["STORAGE_PATH"])
            os.makedirs(controller.CONF["STORAGE_PATH"])

        logging.debug("Setting selinux context")
        nfsutils.setSELinuxContextForDir(controller.CONF["STORAGE_PATH"], nfsutils.SELINUX_RW_LABEL)
        os.chown(controller.CONF["STORAGE_PATH"], basedefs.CONST_VDSM_UID, basedefs.CONST_KVM_GID)
        os.chmod(controller.CONF["STORAGE_PATH"], 0755)
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CREATE_STORAGE_PATH)

def getSupportedCpus():
    global controller

    try:
        # Load and import the module
        logging.debug("Attempting to load the caps vdsm module")
        sys.path.append(VDSM_PATH)
        moduleobj = __import__('caps', globals())
        moduleobj.__file__ = "%s/caps.py" % VDSM_PATH
    except:
        raise Exception("Could not find the VDSM caps module in %s" % VDSM_PATH)

    # Get the list
    try:
        controller.CONF['VDSM_SUPPORTED_MODELS'] = moduleobj._getCompatibleCpuModels()
        logging.debug("Supported CPU models are:")
        for item in controller.CONF['VDSM_SUPPORTED_MODELS']:
            logging.debug(item)
    except:
        logging.error(traceback.format_exc())
        raise Exception(ERROR_CAPABILITIES)

def getCPUFamily():
    global controller
    controller.CONF['VDSM_CPU_FAMILY'] = None

    logging.debug("Determening the CPU family supported by the host")
    families = {'model_Westmere'   : 'Intel Westmere Family',
                'model_Nehalem'    : 'Intel Nehalem Family',
                'model_Penryn'     : 'Intel Penryn Family',
                'model_Conroe'     : 'Intel Conroe Family',
                'model_Opteron_G3' : 'AMD Opteron G3',
                'model_Opteron_G2' : 'AMD Opteron G2',
                'model_Opteron_G1' : 'AMD Opteron G1'}

    # We loop over the list and not the dictionaries because we want to sort it
    # per our own priorities. (when looping over a dict, we cannot force a certain order)
    sortedList = ('model_Westmere',
                  'model_Nehalem',
                  'model_Penryn',
                  'model_Conroe',
                  'model_Opteron_G3',
                  'model_Opteron_G2',
                  'model_Opteron_G1')

    for model in sortedList:
        logging.debug("comparing %s with supported models", model)
        if model in controller.CONF['VDSM_SUPPORTED_MODELS']:
            logging.debug("Supported model family is: %s", families[model])
            controller.CONF['VDSM_CPU_FAMILY'] = families[model]
            return

    # we're still here, it means we don't have supported CPU model
    logging.error(ERROR_UNSUPPORTED_CPU_MODEL)
    raise Exception(ERROR_UNSUPPORTED_CPU_MODEL)

def isHealthPageUp():
    """
    check if project health page is and accesible
    will throw exception on error
    and not return a value
    """
    health_url = JBOSS_HEALTH_URL % (controller.CONF["HOST_FQDN"], controller.CONF["HTTP_PORT"])
    logging.debug("Checking JBoss status.")
    content = getUrlContent(health_url)
    if content and utils.verifyStringFormat(content, ".*DB Up.*"):
        logging.info("JBoss is up and running.")
        return True
    else:
        logging.error(ERROR_JBOSS_STATUS)
        raise Exception(ERROR_JBOSS_STATUS)

def getUrlContent(url):
    try:
        urlObj = urllib2.urlopen(url)
        urlContent = urlObj.read()
    except:
        return None

    return urlContent
