#! /usr/bin/python

import common_utils as utils
import basedefs
import output_messages
import random
import shutil
import logging
import os
import traceback

# Globals
HTTP_OLD_PORT="8700"
HTTPS_OLD_PORT="8701"
HTTP_NEW_PORT="80"
HTTPS_NEW_PORT="443"

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

def _checkPreRequisites():
    # Verify httpd and mod_ssl are installed in the system
    logging.debug("Checking httpd and mod_ssl are installed as prerequisites")
    cmd = ["rpm","-q","httpd"]
    out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_HTTPD_NOT_INSTALLED)
    cmd = ["rpm","-q","mod_ssl"]
    out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_MOD_SSL_NOT_INSTALLED)

def _configureSelinuxBoolean():
    logging.debug("Enable httpd_can_network_connect boolean")
    cmd = ["setsebool","-P","httpd_can_network_connect","1"]
    out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_FAILED_UPDATING_SELINUX_BOOLEAN)

def _exportPrivateKey():
    try:
        logging.debug("Update %s to use engine_id_rsa private key in mod_ssl directives"%(basedefs.FILE_HTTPD_SSL_CONFIG))
        # Use the engine_id_rsa key in mod_ssl directives
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_SSL_CONFIG, " ")
        handler.open()
        handler.editParam("SSLCertificateFile", "/etc/pki/ovirt-engine/certs/engine.cer")
        handler.editParam("SSLCertificateKeyFile", "/etc/pki/ovirt-engine/keys/engine_id_rsa")
        handler.editParam("SSLCertificateChainFile", "/etc/pki/ovirt-engine/ca.pem")
        handler.close()
        #utils.findAndReplace(basedefs.FILE_HTTPD_SSL_CONFIG,"^SSLCertificateFile.+$",SSL_CERT)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTPD_SSL_CONFIG%(basedefs.FILE_HTTPD_SSL_CONFIG))

def _configureJbossXml():
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
        logging.debug("loading xml file handler to configure Jboss configuration file")
        xmlObj = utils.XMLConfigFileHandler(editFile)
        xmlObj.open()

        logging.debug("Configuring ajp connector")
	xmlObj.registerNs('web', 'urn:jboss:domain:web:1.1')
        ajpConnectorStr='<connector name="ajp" protocol="AJP/1.3" scheme="http" socket-binding="ajp"/>'
        xmlObj.removeNodes("//web:subsystem/web:connector[@name='ajp']")
        xmlObj.addNodes("//web:subsystem", ajpConnectorStr)

        logging.debug("Configuring ajp socket")
        xmlObj.registerNs('domain', 'urn:jboss:domain:1.1')
        ajpSocketStr='<socket-binding name="ajp" port="8702"/>'
        xmlObj.removeNodes("//domain:socket-binding-group/domain:socket-binding[@name='ajp']")
        xmlObj.addNodes("//domain:socket-binding-group", ajpSocketStr)

        xmlObj.close()

        shutil.move(editFile, basedefs.FILE_JBOSS_STANDALONE)
        os.chown(basedefs.FILE_JBOSS_STANDALONE, utils.getUsernameId("jboss-as"), utils.getGroupId("jboss-as"))
        logging.debug("Jboss configuration has been saved")

    except:
        logging.error("ERROR Editing jboss's configuration file")
        logging.error(traceback.format_exc())
        raise output_messages.ERR_EXP_FAILED_CONFIG_JBOSS

def _redirectUrl():
    try:
        # Redirect oVirt specific URLs to the application server using the AJP protocol
        logging.debug("Redirect oVirt URLs using AJP protocol")
        redirectStr="ProxyPass / ajp://localhost:8702/"

        fd = open(basedefs.FILE_OVIRT_HTTPD_CONF, 'w')
        fd.write(redirectStr)
        fd.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_CREATE_OVIRT_HTTPD_CONF%(basedefs.FILE_OVIRT_HTTPD_CONF))

def _listenHttpPort():
    try:
        logging.debug("Update %s to listen in the new HTTP port"%(basedefs.FILE_HTTPD_CONF))
        # Listen in the new http port
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_CONF, " ")
        handler.open()
        handler.editParam("Listen", "%s"%(HTTP_NEW_PORT))
        handler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTP_LISTEN_PORT%(basedefs.FILE_HTTPD_CONF))

def _listenHttpsPort():
    try:
        logging.debug("Update %s to listen in the new HTTPS port"%(basedefs.FILE_HTTPD_SSL_CONFIG))
        # Listen in the new https port
        handler = utils.TextConfigFileHandler(basedefs.FILE_HTTPD_SSL_CONFIG, " ")
        handler.open()
        handler.editParam("Listen", "%s"%(HTTPS_NEW_PORT))
        handler.close()
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_EXP_UPD_HTTPS_LISTEN_PORT%(basedefs.FILE_HTTPD_SSL_CONFIG))

def _startHttpdService():
    logging.debug("Enabling the httpd service")
    try:
        cmd = ["/sbin/chkconfig","httpd","on"]
        out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_FAILED_CHKCFG_HTTPD)
        cmd = ["/sbin/service","httpd","stop"]
        out, rc = utils.execCmd(cmd, None, False)
        cmd = ["/sbin/service","httpd","start"]
        utils.execCmd(cmd, None, True, output_messages.ERR_RESTARTING_HTTPD_SERVICE)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_FAILED_TO_START_HTTPD_SERVICE)

def _startJbossService():
    logging.debug("Enabling the JBoss service")
    try:
        cmd = ["/sbin/service","jboss-as","stop"]
        out, rc = utils.execCmd(cmd, None, True, output_messages.ERR_FAILED_STP_JBOSS_SERVICE)
        cmd = ["/sbin/service","jboss-as","start"]
        utils.execCmd(cmd, None, True, output_messages.ERR_FAILED_START_JBOSS_SERVICE)
    except:
        logging.error(traceback.format_exc())
        raise Exception(output_messages.ERR_FAILED_TO_RESTART_JBOSS_SERVICE)


if __name__ == "__main__":
    initLogging()
    _checkPreRequisites()
    _configureSelinuxBoolean()
    _configureJbossXml()
    _redirectUrl()
    _exportPrivateKey()
    #_configureIpTables()
    _listenHttpPort()
    _listenHttpsPort()
    _startHttpdService()
    _startJbossService()
