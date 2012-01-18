#!/usr/bin/python
#
# Copyright 2008 Qumranet, Inc. All rights reserved.
# Use is subject to license terms.
#

import sys, getopt
import os
import subprocess
import logging, logging.config
import traceback
import string
import random
import re

SUPPORTED_PLATFORMS = [ "RedHatEnterpriseServer", "Fedora" ]
HYPERVISOR_PLATFORMS = [ "RedHatEnterpriseVirtualizationHypervisor", "RedHatEnterpriseHypervisor", "oVirtNodeHypervisor" ]
HYPERVISOR_RELEASE_FILE = '/etc/rhev-hypervisor-release'
REDHAT_RELEASE_FILE = '/etc/redhat-release'
vdsm_reg_conf_file = '/etc/vdsm-reg/vdsm-reg.conf'

def printNlog(s):
    print s
    logging.debug(s)

rnum = random.randint(100,1000000).__repr__()
log_filename = '/tmp/vds_installer.'+rnum+'.log'

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)-8s %(message)s',
                    datefmt='%a, %d %b %Y %H:%M:%S',
                    filename=log_filename,
                    filemode='w')

# "curl -o /tmp/vds_bootstrap_{GUID}.py {URL1}; chmod +x /tmp/vds_bootstrap_{GUID}.py; /tmp/vds_bootstrap_{GUID}.py {URL2} {vds-server} {GUID}";
# "curl -o /tmp/vds_bootstrap_complete_{GUID}.py {URL1}; chmod +x /tmp/vds_bootstrap_complete_{GUID}.py; /tmp/vds_bootstrap_complete_{GUID}.py {GUID}";

def isScriptValid(module):
    try:
        __import__(module)
    except:
        logging.error(traceback.format_exc())
        return False
    return True

def buildScriptName(random_num, vds_complete):
    if not vds_complete:
        script_name = 'vds_bootstrap_'+random_num+'.py'
    else:
        script_name = 'vds_bootstrap_complete_'+random_num+'.py'
    return script_name

def releaseFileExists():
    """ -- According to lsb_release:
           Read first line from HYPERVISOR_RELEASE_FILE, then try REDHAT_RELEASE_FILE and then return it.
    """
    if os.path.exists(HYPERVISOR_RELEASE_FILE):
        return True, HYPERVISOR_RELEASE_FILE
    elif os.path.exists(REDHAT_RELEASE_FILE):
        return True, REDHAT_RELEASE_FILE
    else:
        return False, HYPERVISOR_RELEASE_FILE + ", " + REDHAT_RELEASE_FILE

def get_id_line():
    line = ''
    RELEASE_FILE = None

    try:
        fileExists, releaseFile =  releaseFileExists()
        RELEASE_FILE = releaseFile
        if (fileExists):
            release = open(releaseFile, "r")
            line = release.readline()
            line = line.replace ("\n", "")
            release.close
            logging.debug("get_id_line: read line %s.", line)
        else:
            line = None
            message = "Failed to find the release file(s): " + releaseFile
            logging.error(message)
    except:
        line = None
        message = "Failed to read release file: " + str(RELEASE_FILE)
        logging.error(message + "\n" + traceback.format_exc())

    return line

def lsb_release():
    """ -- According to lsb_release:
         1. Remove 'Linux'
         2. Remove release data
         3. For short format, remove spaces.
    """
    res = get_id_line()
    logging.debug("lsb_release: input line %s.", res)

    if res is not None:
        res = re.sub(r' [Ll][Ii][Nn][Uu][Xx]', '', res)
        res = re.sub(r'relea.*', '', res)
        res = re.sub(r' ', '', res)

    logging.debug("lsb_release: return: %s.", res)
    return res

def testPlatform():
    ''' testPlatform evaluates the platform version and returns
        0 - platform is eligible for installation
        1 - platform is ovirt-node
        2 - platform is not eligible for installation
    '''
    fReturn = 0
    st = "OK"
    message = "Test platform succeeded"
    component = "INSTALLER"

    try:
        res = lsb_release()
        if res is None:
            fReturn = 2
            message = 'Unable to calculate platform ID'
            logging.error(message)
            st = "FAIL"
        elif res in HYPERVISOR_PLATFORMS:
            fReturn = 1
            component = "RHEV_INSTALL"
            message = "oVirt Node DETECTED"
            logging.debug(message)
            st = "OK"
        elif res not in SUPPORTED_PLATFORMS:
            fReturn = 2
            message = "Unsupported platform: %s" % res
            logging.error(message)
            st = "FAIL"
    except:
        fReturn = 2
        message = "Failed to test platform compatibility"
        logging.error(message + "\n" + traceback.format_exc())
        st = "FAIL"

    printNlog("<BSTRAP component='%s' status='%s' message='%s'/>" % (component,st, message))
    sys.stdout.flush()

    return fReturn

def downloadBootstrap(url_bs, random_num, vds_complete):
    """ -- Download vds bootstrap scripts
    """
    install_script = None
    install_lib = None
    st = 'FAIL'
    try:
        if not vds_complete:
            script_name = 'vds_bootstrap.py'
            install_lib = 'deployUtil.py'
        else:
            script_name = 'vds_bootstrap_complete.py'
        # check whether url ends with '/'
        if url_bs[-1] != '/':
            url_bs = url_bs + '/'
        src_url = url_bs + script_name
        tmp_script_name = buildScriptName(random_num, vds_complete)
        trg_script = "/tmp/%s"%(tmp_script_name)

        if install_lib is not None and not os.path.exists(install_lib):
            src_lib_url = url_bs + install_lib
            trg_lib = "/tmp/%s"%(install_lib)
            execfn = ["/usr/bin/curl","-s", "-k", "-w", "%{http_code}", "-o", trg_lib, src_lib_url]
            logging.debug("trying to fetch %s script cmd = '%s'",install_lib, string.join(execfn, " "))
            code = subprocess.Popen(execfn, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()[0]
            if code == '200':
                if isScriptValid(os.path.splitext(install_lib)[0]):
                    st = 'OK'
                    message = "%s download succeeded"%(install_lib)
                else:
                    st = 'FAIL'
                    message = "%s download failed. Pathname could not be resolved (verify component web site path)."%(install_lib)
            else:
                st = 'FAIL'
                message = "%s download failed. Pathname could not be resolved (verify computer/domain name)."%(install_lib)
        else:
            st = 'OK'
            message = "Install library already exists"

        printNlog("<BSTRAP component='INSTALLER LIB' status='%s' message='%s'/>"%(st, message))
        sys.stdout.flush()

        if st != 'OK':
            return install_script

        if not os.path.exists(trg_script):
            execfn = ["/usr/bin/curl","-s","-k", "-w", "%{http_code}", "-o", trg_script, src_url]
            logging.debug("trying to fetch %s script cmd = '%s'",script_name, string.join(execfn, " "))
            code = subprocess.Popen(execfn, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()[0]
            if code == '200':
                if isScriptValid(os.path.splitext(tmp_script_name)[0]):
                    st = 'OK'
                    message = "%s download succeeded"%(script_name)
                    install_script = trg_script
                else:
                    st = 'FAIL'
                    message = "%s download failed. Pathname could not be resolved (verify component web site path)."%(script_name)
                    install_script = None
            else:
                st = 'FAIL'
                message = "%s download failed. Pathname could not be resolved (verify computer/domain name)."%(script_name)
                install_script = None
        else:
            st = 'OK'
            message = "%s already exist"%(script_name)
            install_script = trg_script

        subprocess.Popen(["/bin/chmod","+x", trg_script], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()

        printNlog("<BSTRAP component='INSTALLER' status='%s' message='%s'/>" % (st, message))
        sys.stdout.flush()
    except:
        install_script = None
        logging.error(traceback.format_exc())
    return install_script

def runInstaller(remote_nfs, orgName, systime, ncport, usevdcrepo, vds_config_str, url_rpm, vds_server, random_num, script, vds_complete, firewall_rules_file):
    """ -- Run VDS bootstrap scripts
    """
    try:
        if os.path.exists(script):
            if not vds_complete:
                execfn = [script]
                if remote_nfs:
                    execfn += ["-m", remote_nfs]
                if orgName:
                    execfn += ["-O", orgName]
                if systime:
                    execfn += ["-t", systime]
                if ncport:
                    execfn += ["-n", ncport]
                if usevdcrepo:
                    execfn += ["-u", str(usevdcrepo)]
                if firewall_rules_file:
                    execfn += ["-f", firewall_rules_file]
                execfn += [url_rpm, vds_server, random_num]
            else:
                if vds_config_str:
                    execfn = [script, "-c", vds_config_str, random_num]
                else:
                    execfn = [script, random_num]
            logging.debug("trying to run %s script cmd = '%s'",script, string.join(execfn, " "))
            subprocess.Popen(execfn).communicate()
        else:
            logging.debug("script %s doen not exist",script)
    except:
        logging.error(traceback.format_exc())

def process_ovirt_platform(url_bs, engine_port, random_num, systime ):

    """ update vdsm-reg.conf and restart vdsm-reg service """
    import time
    import calendar

    downloadBootstrap(url_bs, random_num, False)

    return_value = False
    ticket = None

    try:
        time_struct = time.strptime(systime, '%Y-%m-%dT%H:%M:%S')
        ticket = calendar.timegm(time_struct)
    except ValueError, ex:
        logging.debug("setHostTime: Failed to parse ENGINE time. message= " + str(ex))
        return 1

    if ticket is not None:
        return_value = update_and_restart_vdsm_reg(url_bs, engine_port, ticket)

    return return_value

def update_and_restart_vdsm_reg(url_bs, engine_port, ticket):
    from urlparse import urlparse

    try:
        import deployUtil
    except:
        printNlog("<BSTRAP component='INIT' status='FAIL' message='Error trying to deploy library.'/>")
        logging.error(traceback.format_exc())
        return False

    return_value = False
    if not os.path.exists(vdsm_reg_conf_file):
        message = "Error trying to configure registration service."
        printNlog("<BSTRAP component='UPDATE_VDSM_REG_CONF' status='FAIL' message='%s'/>" % (message) )
        logging.debug("file %s does not exist", vdsm_reg_conf_file)
    else:
        vdc_url = urlparse(url_bs)
        if engine_port is None:
            if vdc_url.port is not None:
                engine_port = str(vdc_url.port)

        if engine_port is not None:
            deployUtil._updateFileLine(vdsm_reg_conf_file, "vdc_host_port", str(engine_port), True)

        deployUtil._updateFileLine(vdsm_reg_conf_file, "vdc_host_name", str(vdc_url.hostname), True)
        deployUtil._updateFileLine(vdsm_reg_conf_file, "ticket", str(ticket), True)
        deployUtil.ovirtfunctions.ovirt_store_config(vdsm_reg_conf_file)

        if handle_ssh_key(vdc_url.hostname, str(engine_port)):
            out, err, return_code = deployUtil.setService('vdsm-reg', 'restart')
        else:
            return_code = None

        if not return_code:
            return_value = True
    return return_value

def handle_ssh_key(host, port):
    import deployUtil

    ssh_result = False
    strKey = deployUtil.getAuthKeysFile(host, port)

    if strKey is not None:
        ssh_result = deployUtil.handleSSHKey(strKey)

    if ssh_result:
        printNlog("<BSTRAP component='RHEV_INSTALL' status='OK' message='RHEV-H ACCESSIBLE'/>")
    else:
        printNlog("<BSTRAP component='RHEV_INSTALL' status='FAIL' message='Host failed to download management server public-key.'/>")

    return ssh_result

def main():
    """Usage: vds_installer.py  [-c vds_config_str] [-m remote_nfs] [-r rev_num] [-O organizationName] [-t YYYY-MM-DDTHH:mm:SS_system_time] [-n netconsole_host:port] [-p engine_port] <url_bs> <url_rpm> <vds_server> <random_num> <vds_complete>
                    url_bs - components url
                    url_rpm - rpm download url
                    random_num - random number for temp. file names generation
                    vds_server - vds server for CSR usage
                    vds_complete - to run first vds_bootstrap script = false
                                   to run second vds_bootstrap_complete script = true
    """
    try:
        remote_nfs = None
        rev_num = None
        vds_config_str = None
        orgName = None
        systime = None
        ncport = None
        usevdcrepo = False
        engine_port = None
        firewall_rules_file = None
        opts, args = getopt.getopt(sys.argv[1:], "c:m:r:O:t:n:u:p:f:")
        for o,v in opts:
            if o == "-c":
                vds_config_str = v
            if o == "-m":
                remote_nfs = v
            if o == "-r":
                rev_num = v
            if o == "-O":
                orgName = v
            if o == "-t":
                systime = v
            if o == "-n":
                ncport = v
            if o == "-u":
                usevdcrepo = (v[0].upper() == 'T')
            if o == "-p":
                engine_port = v
            if o =="-f":
                firewall_rules_file = v

        url_bs = args[0]
        url_rpm = args[1]
        vds_server = args[2]
        random_num = args[3]
        vds_complete = args[4]
        if vds_complete.lower() == 'true':
            vds_complete = True
        elif vds_complete.lower() == 'false':
            vds_complete = False
        else:
            printNlog(main.__doc__)
            return 1
    except:
        printNlog(main.__doc__)
        return 1
    try:
        logging.debug('**** Start VDS Installation ****')
        res = testPlatform()

        if res == 0:
            vds_script = downloadBootstrap(url_bs, random_num, vds_complete)
            if vds_script:
                runInstaller(remote_nfs, orgName, systime, ncport, usevdcrepo, vds_config_str, url_rpm, vds_server, random_num, vds_script, vds_complete, firewall_rules_file)
                if firewall_rules_file is not None:
                    try:
                        os.unlink(firewall_rules_file)
                    except:
                        logging.warn("Failed to delete firewall conf file: %s" , firewall_rules_file)
            if vds_complete:
                file_name = '/tmp/vds_installer_'+random_num+'.py'
                os.unlink(file_name)
        elif res == 1:
            ret_value = process_ovirt_platform(url_bs, engine_port, random_num, systime)
            if ret_value is False:
                printNlog("<BSTRAP component='RHEV_INSTALL' status='FAIL'/>")
            return ret_value
        elif res == 2:
            logging.error("Failed platform test.")
            return 1

    except:
        logging.error(traceback.format_exc())
        return 1
    return 0

if __name__ == "__main__":
    sys.exit(main())
