#!/usr/bin/python

# Imports
import sys
import os
import signal
import shutil
import logging
import traceback
import tempfile
import types
import time
import pwd
from optparse import OptionParser, SUPPRESS_HELP
import common_utils as utils
import basedefs
import output_messages
from miniyum import MiniYum

# Consts

# The following constants are used in maintenance mode for
# running things in a loop
MAINTENANCE_TASKS_WAIT_PERIOD = 180
MAINTENANCE_TASKS_WAIT_PERIOD_MINUTES = MAINTENANCE_TASKS_WAIT_PERIOD / 60
MAINTENANCE_TASKS_CYCLES = 20

RPM_BACKEND = "ovirt-engine-backend"
RPM_DBSCRIPTS = "ovirt-engine-dbscripts"
RPM_SETUP = "ovirt-engine-setup"
RPM_UPGRADE = "engine-upgrade"

# DB default configuration
SERVER_NAME = basedefs.DB_HOST
SERVER_PORT = basedefs.DB_PORT
SERVER_ADMIN = basedefs.DB_ADMIN


# CONST
BACKUP_FILE = "ovirt-engine_db_backup"
LOG_PATH = "/var/log/ovirt-engine"

# ASYNC TASKS AND COMPLETIONS QUERIES
ASYNC_TASKS_QUERY = "select * from fn_db_get_async_tasks();"
COMPENSATIONS_QUERY = "select command_type, entity_type from business_entity_snapshot;"

ETL_SERVICE="/etc/init.d/ovirt-engine-etl"

# Versions
UNSUPPORTED_VERSION = "2.2"

#MSGS
MSG_TASKS_COMPENSATIONS = "\n\nSystem Tasks:\n%s\n%s\n\n"
MSG_STOP_RUNNING_TASKS = "\nInfo: The following tasks have been found running \
in the system: %s%sWould you like to proceed and try to stop tasks automatically?\
\n(Answering 'no' will stop the upgrade)"
MSG_RUNNING_TASKS = "Checking active system tasks"
MSG_TASKS_STILL_RUNNING = "\nThere are still running tasks: %sPlease make sure \
that there are no running system tasks before you continue. Stopping upgrade."
MSG_WAITING_RUNNING_TASKS = "Still waiting for system tasks to be cleared."
MSG_ERROR_USER_NOT_ROOT = "Error: insufficient permissions for user %s, you must run with user root."
MSG_NO_ROLLBACK = "Error: Current installation "
MSG_RC_ERROR = "Return Code is not zero"
MSG_INFO_NO_UPGRADE_AVAIL = "No updates available"
MSG_INFO_UPGRADE_AVAIL = "%d Updates available:"
MSG_ERROR_INCOMPATIBLE_UPGRADE = "\nError: a data center or cluster version %s were found on the system.\n\
Such upgrade flow is not supported. Upgrade all %s data centers and clusters and rerun the upgrade utility.\n" \
% (UNSUPPORTED_VERSION, UNSUPPORTED_VERSION)
MSG_ERROR_NO_ROLLBACK_AVAIL = "Error: Installed packages are missing from the yum repositories\n\
Please check your yum repositories or use --no-yum-rollback"
MSG_ERROR_NEW_SETUP_AVAIL="\nError: New %s rpm available via yum.\n\
Please execute `yum update %s`, then re-execute '%s'.\n\
To use the current %s rpm, execute '%s --force-current-setup-rpm'." % (RPM_SETUP, RPM_SETUP, RPM_UPGRADE, RPM_SETUP, RPM_UPGRADE)
MSG_ERROR_BACKUP_DB = "Error: Database backup failed"
MSG_ERROR_RESTORE_DB = "Error: Database restore failed"
MSG_ERROR_DROP_DB = "Error: Database drop failed"
MSG_ERROR_UPDATE_DB = "Error: Database update failed"
MSG_ERROR_RENAME_DB = "Error: Database rename failed. Check that there are no active connections to the DB and try again."
MSG_ERROR_RPM_QUERY = "Error: Unable to retrieve rpm versions"
MSG_ERROR_CHECK_LOG = "Error: Upgrade failed.\nplease check log at %s"
MSG_ERROR_CONNECT_DB = "Error: Failed to connect to database"
MSG_ERR_FAILED_START_ENGINE_SERVICE = "Error: Can't start ovirt-engine"
MSG_ERR_FAILED_ENGINE_SERVICE_STILL_RUN = "Error: Can't stop ovirt-engine service. Please shut it down manually."
MSG_ERR_FAILED_STOP_ENGINE_SERVICE = "Error: Can't stop ovirt-engine"
MSG_ERR_FAILED_STATUS_ENGINE_SERVICE = "Error: Can't get ovirt-engine service status"
MSG_ERR_FAILED_START_SERVICE = "Error: Can't start the %s service"
MSG_ERR_FAILED_STOP_SERVICE = "Error: Can't stop the %s service"
MSG_ERR_SQL_CODE = "Failed running sql query"
MSG_ERR_EXP_UPD_DC_TYPE="Failed updating default Data Center Storage Type in %s db"
MSG_ERROR_ENGINE_PID = "Error: ovirt-engine service is dead, but pid file exists"
MSG_ERROR_PGPASS = "Error: DB password file was not found on this system. Verify \
that this system was previously installed and that there's a password file at %s or %s" % \
(basedefs.DB_PASS_FILE, basedefs.ORIG_PASS_FILE)
MSG_ERROR_FAILED_CONVERT_ENGINE_KEY = "Error: Can't convert engine key to PKCS#12 format"
MSG_ERROR_FAILED_SYNTHESIS_ENGINE_KEY = "Error: Can't synthesis engine key to PKCS#12 format"
MSG_ERROR_SSH_KEY_SYMLINK = "Error: SSH key should not be symlink"
MSG_ERROR_UUID_VALIDATION_FAILED = (
    "Pre-upgade host UUID validation failed\n"
    "\n"
    "Please move the following hosts to maintenance mode before upgrade:\n"
    "%s"
    "\n"
)
MSG_ERROR_UUID_FAILED_HOST = "   - Host %s(%s), reason: %s\n"
MSG_ERROR_UUID_FAILED_REASON_EMPTYUUID = "empty UUID"
MSG_ERROR_UUID_FAILED_REASON_NOUUID = "no BIOS UUID"
MSG_ERROR_UUID_FAILED_REASON_DUPUUID = "duplicate BIOS UUID"

MSG_INFO_DONE = "DONE"
MSG_INFO_ERROR = "ERROR"
MSG_INFO_REASON = " **Reason: %s**\n"
MSG_INFO_STOP_ENGINE = "Stopping %s service"
MSG_INFO_STOP_DB = "Stopping DB related services"
MSG_INFO_START_DB = "Starting DB related services"
MSG_INFO_PREUPGRADE = "Pre-upgrade validations"
MSG_INFO_BACKUP_DB = "Backing Up Database"
MSG_INFO_RENAME_DB = "Rename Database"
MSG_INFO_RESTORE_DB = "Restore Database name"
MSG_INFO_YUM_UPDATE = "Updating rpms"
MSG_INFO_DB_UPDATE = "Updating Database"
MSG_INFO_RUN_POST = "Running post install configuration"
MSG_ERROR_UPGRADE = "\n **Error: Upgrade failed, rolling back**"
MSG_INFO_DB_RESTORE = "Restoring Database"
MSG_INFO_YUM_ROLLBACK = "Rolling back rpms..."
MSG_INFO_START_ENGINE = "Starting %s service"
MSG_INFO_DB_BACKUP_FILE = "DB Backup available at "
MSG_INFO_LOG_FILE = "Upgrade log available at"
MSG_INFO_CHECK_UPDATE = "\nChecking for updates... (This may take several minutes)"
MSG_INFO_UPGRADE_OK = "%s upgrade completed successfully!" % basedefs.APP_NAME
MSG_INFO_STOP_INSTALL_EXIT="Upgrade stopped, Goodbye."
MSG_INFO_UPDATE_ENGINE_PROFILE="Updating ovirt-engine Profile"
MSG_INFO_PKI_PREPARE = "Preparing CA"
MSG_INFO_PKI_ROLLBACK = "Restoring CA"
MSG_INFO_UUID_VALIDATION_INFO = (
    "NOTICE:\n"
    "\n"
    "  The following hosts must be reinstalled. If reason is BIOS\n"
    "  duplicate UUID execute the following command on each host before\n"
    "  reinstallation:\n"
    "  # uuidgen > /etc/vdsm/vdsm.id\n"
    "  If you are running rhev-h or ovirt-node, restart vdsm-reg service:\n"
    "  # service vdsm-reg restart\n"
    "\n"
    "%s"
    "."
)

MSG_ALERT_STOP_ENGINE="\nDuring the upgrade process, %s  will not be accessible.\n\
All existing running virtual machines will continue but you will not be able to\n\
start or stop any new virtual machines during the process.\n" % basedefs.APP_NAME
INFO_Q_PROCEED="Would you like to proceed"
MSG_INFO_REPORTS="Perform the following steps to upgrade the history service \
or the reporting package:\n\
1. Execute: yum update ovirt-engine-reports*\n\
2. Execute: ovirt-engine-dwh-setup\n\
3. Execute: ovirt-engine-reports-setup"

# GLOBAL
logFile = "ovirt-engine-upgrade.log"
messages = []
hostids = {}

# Code
def getOptions():
    parser = OptionParser()
    parser.add_option("-r", "--no-yum-rollback",
                      action="store_false", dest="yum_rollback", default=True,
                      help="don't rollback yum transaction")

    parser.add_option("-u", "--unattended",
                      action="store_true", dest="unattended_upgrade", default=False,
                      help="unattended upgrade (this option will stop ovirt-engine service before upgrading)")

    parser.add_option("-s", "--force-current-setup-rpm",
                      action="store_true", dest="force_current_setup_rpm", default=False,
                      help="Ignore new %s rpm"%(RPM_SETUP))

    parser.add_option("-c", "--check-update",
                      action="store_true", dest="check_update", default=False,
                      help="Check for available package updates")

    parser.add_option("-f", "--upgrade-ignore-tasks",
                      action="store_true", dest="ignore_tasks", default=False,
                      help=SUPPRESS_HELP)

    parser.add_option("-n", "--no-space-check", help="Disable space check", action="store_true", default=False)


    (options, args) = parser.parse_args()
    return (options, args)

def checkEngine(service=basedefs.ENGINE_SERVICE_NAME):
    """
    Ask user to stop ovirt-engine service before
    upgrading ovirt-engine

    returns: true if user choose to stop ovirt-engine
    false otherwise
    """
    logging.debug("checking the status of ovirt-engine service")
    cmd = [
        basedefs.EXEC_SERVICE, service , "status",
    ]
    output, rc = utils.execCmd(cmdList=cmd, failOnError=False)

    if rc == 0:
        logging.debug("ovirt-engine service is up and running")

        print MSG_ALERT_STOP_ENGINE
        answer = utils.askYesNo(INFO_Q_PROCEED)

        # If user choose yes -> return true (stop ovirt-engine)
        if answer:
            return True
        else:
            logging.debug("User chose not to stop ovirt-engine")
            return False

    elif rc == 1:
        # Proc is dead, pid exists
        raise Exception(MSG_ERROR_ENGINE_PID)

    elif rc == 3:
        # If ovirt-engine is not running, we don't need to stop it
        return True

    else:
        raise Exception(MSG_ERR_FAILED_STATUS_ENGINE_SERVICE)

def initLogging():
    global logFile
    try:
        if not os.path.isdir(LOG_PATH):
            os.makedirs(LOG_PATH)
        logFile = "%s/ovirt-engine-upgrade_%s.log"%(LOG_PATH, utils.getCurrentDateTime())
        level = logging.DEBUG
        # TODO: Move to mode="a"?
        hdlr = logging.FileHandler(filename=logFile, mode='w')
        fmts='%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
        dfmt='%Y-%m-%d %H:%M:%S'
        fmt = logging.Formatter(fmts, dfmt)
        hdlr.setFormatter(fmt)
        logging.root.addHandler(hdlr)
        logging.root.setLevel(level)
    except:
        logging.error(traceback.format_exc())
        raise Exception("Failed to initiate logger")

def _verifyUserPermissions():
    if (os.geteuid() != 0):
        username = pwd.getpwuid(os.getuid())[0]
        print MSG_ERROR_USER_NOT_ROOT%(username)
        sys.exit(1)

class MYum():

    class _transaction(object):
        def __init__(self, parent, transaction):
            self._parent = parent
            self._transaction = transaction

        def __enter__(self):
            self._parent._unlock()
            self._transaction.__enter__()

        def __exit__(self, exc_type, exc_value, traceback):
            if exc_type is not None and self._parent._rpmChange:
                print MSG_INFO_YUM_ROLLBACK
            self._transaction.__exit__(exc_type, exc_value, traceback)
            self._parent._lock()

    def __init__(self, miniyum):
        self._rpmChange = False
        self._miniyum = miniyum

    def _lock(self):
        utils.lockRpmVersion(self._miniyum, basedefs.RPM_LOCK_LIST.split())

    def _unlock(self):
        logging.debug("Yum unlock started")
        # Read file content
        fd = file(basedefs.FILE_YUM_VERSION_LOCK)
        fileText = fd.readlines()
        fd.close()

        # Change content:
        fd = file(basedefs.FILE_YUM_VERSION_LOCK, 'w')
        for line in fileText:
            if not basedefs.ENGINE_RPM_NAME in line:
                fd.write(line)
        fd.close()
        logging.debug("Yum unlock completed successfully")

    def clean(self):
        with self._miniyum.transaction():
            self._miniyum.clean(['expire-cache'])

    def transaction(self):
        return MYum._transaction(self, self._miniyum.transaction())

    def begin(self):
        useGroups = False
        for group in self._miniyum.queryGroups():
            if group['name'] == basedefs.ENGINE_YUM_GROUP:
                useGroups = True

        if useGroups:
            self._miniyum.updateGroup(group=basedefs.ENGINE_YUM_GROUP)
        else:
            self._miniyum.update(packages=(basedefs.ENGINE_RPM_NAME,))

        self.emptyTransaction = not self._miniyum.buildTransaction()

        logging.debug('Transaction Summary:')
        for p in self._miniyum.queryTransaction():
            logging.debug('    %s - %s' % (
                p['operation'],
                p['display_name']
            ))

    def update(self):
        self._rpmChange = True
        self._miniyum.processTransaction()

    def getPackages(self):
        return self._miniyum.queryTransaction()

    def rollbackAvailable(self, packages):
        logging.debug("Yum rollback-avail started")

        # Verify all installed packages available in yum
        for package in packages:
            for query in self._miniyum.queryPackages(patterns=[package['name']]):
                if query['operation'] == 'installed':
                    logging.debug("Checking package %s", query['display_name'])
                    if not self._miniyum.queryPackages(patterns=[query['display_name']], showdups=True):
                        logging.debug("package %s not available in cache" % query['display_name'])
                        return False

        logging.debug("Yum rollback-avail completed successfully")
        return True

class DB():
    def __init__(self):
        date = utils.getCurrentDateTime()
        self.sqlfile = "%s/%s_%s.sql" % (basedefs.DIR_DB_BACKUPS, BACKUP_FILE, date)
        self.updated = False
        self.dbrenamed = False
        self.name = basedefs.DB_NAME


    def __del__(self):
        if self.updated:
            logging.debug(MSG_INFO_DB_BACKUP_FILE + self.sqlfile)
            print "* %s %s" % (MSG_INFO_DB_BACKUP_FILE, self.sqlfile)

    def backup(self):
        logging.debug("DB Backup started")
        #cmd = "%s -C -E UTF8 --column-inserts --disable-dollar-quoting  --disable-triggers -U %s -h %s -p %s --format=p -f %s %s"\
            #%(basedefs.EXEC_PGDUMP, SERVER_ADMIN, SERVER_HOST, SERVER_PORT, self.sqlfile, basedefs.DB_NAME)
        cmd = [
            basedefs.EXEC_PGDUMP,
            "-C", "-E", "UTF8",
            "--disable-dollar-quoting",
            "--disable-triggers",
            "-U", SERVER_ADMIN,
            "-h", SERVER_NAME,
            "-p", SERVER_PORT,
            "--format=p",
            "-f", self.sqlfile,
            basedefs.DB_NAME,
        ]
        output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERROR_BACKUP_DB, envDict=utils.getPgPassEnv())
        logging.debug("DB Backup completed successfully")

    def restore(self):
        # run psql -U engine -h host -p port -d template1 -f <backup directory>/<backup_file>
        # If DB was renamed, restore it

        if self.updated or self.dbrenamed:
            logging.debug("DB Restore started")

            # If we're here, upgrade failed. Drop temp DB.
            cmd = [
                basedefs.EXEC_DROPDB,
                "-U", SERVER_ADMIN,
                "-h", SERVER_NAME,
                "-p", SERVER_PORT,
                self.name,
            ]
            output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERROR_DROP_DB, envDict=utils.getPgPassEnv())

            # Restore
            cmd = [
                basedefs.EXEC_PSQL,
                "-U", SERVER_ADMIN,
                "-h", SERVER_NAME,
                "-p", SERVER_PORT,
                "-d", basedefs.DB_TEMPLATE,
                "-f", self.sqlfile,
            ]
            output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERROR_RESTORE_DB, envDict=utils.getPgPassEnv())
            logging.debug("DB Restore completed successfully")
        else:
            logging.debug("No DB Restore needed")

    def update(self):
        cwd = os.getcwd()
        os.chdir(basedefs.DIR_DB_SCRIPTS)

        # Make sure we always returning to cwd
        try:
            self.updated = True
            logging.debug("DB Update started")

            # Perform the upgrade
            # ./upgrade.sh -s ${SERVERNAME} -p ${PORT} -u ${USERNAME} -d ${DATABASE};
            dbupgrade = os.path.join(basedefs.DIR_DB_SCRIPTS, basedefs.FILE_DB_UPGRADE_SCRIPT)
            cmd = [
                dbupgrade,
                "-s", SERVER_NAME,
                "-p", SERVER_PORT,
                "-u", SERVER_ADMIN,
                "-d", self.name,
            ]

            # If ignore_tasks supplied, use it:
            if options.ignore_tasks:
                cmd.extend(["-c"])

            output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERROR_UPDATE_DB)
            logging.debug("DB Update completed successfully")

        finally:
            os.chdir(cwd)

    def rename(self, newname):
        """ Rename DB from current name to a newname"""

        # Check that newname is different from current
        if self.name == newname:
            return

        # run the rename query and raise Exception on error
        query = "ALTER DATABASE %s RENAME TO %s" % (self.name, newname)
        try:
            utils.execRemoteSqlCommand(SERVER_ADMIN, SERVER_NAME, SERVER_PORT, basedefs.DB_TEMPLATE, query, True, MSG_ERROR_RENAME_DB)
            # set name to the newname
            self.name = newname
            # toggle dbrenamed value to TRUE
            self.dbrenamed = True
        except:
            # if this happened before DB update, remove DB backup file.
            if not self.updated and os.path.exists(self.sqlfile):
                os.remove(self.sqlfile)
            raise

class CA():

    JKSKEYSTORE = "/etc/pki/ovirt-engine/.keystore"
    TMPAPACHECONF = basedefs.FILE_HTTPD_SSL_CONFIG + ".tmp"

    def mayUpdateDB(self):
        """Returns True if we may update database.

        It can be false positive but will trigger database transaction,
        so database will be rolled back upon failure

        """
        return os.path.exists(self.JKSKEYSTORE)

    def prepare(self):
        if os.path.exists(self.JKSKEYSTORE):
            logging.debug("PKI: convert JKS to PKCS#12")

            tmpPKCS12 = None
            try:
                fd, tmpPKCS12 = tempfile.mkstemp()
                os.close(fd)
                os.unlink(tmpPKCS12)    # java does not like empty files as keystore

                mask = [basedefs.CONST_KEY_PASS]

                cmd = [
                    basedefs.EXEC_KEYTOOL,
                    "-importkeystore",
                    "-noprompt",
                    "-srckeystore", self.JKSKEYSTORE,
                    "-srcstoretype", "JKS",
                    "-srcstorepass", basedefs.CONST_KEY_PASS,
                    "-srcalias", "engine",
                    "-srckeypass", basedefs.CONST_KEY_PASS,
                    "-destkeystore", tmpPKCS12,
                    "-deststoretype", "PKCS12",
                    "-deststorepass", basedefs.CONST_KEY_PASS,
                    "-destalias", "1",
                    "-destkeypass", basedefs.CONST_KEY_PASS
                ]
                output, rc = utils.execCmd(cmdList=cmd, maskList=mask, failOnError=True, msg=MSG_ERROR_FAILED_CONVERT_ENGINE_KEY)

                # synthesis PKCS#12 see rhbz#961069
                cmd = [
                    (
                        "{openssl} pkcs12 -in {input} -passin pass:{password} -nodes | "
                        "{openssl} pkcs12 -export -out {output} -passout pass:{password}"
                    ).format(
                        openssl=basedefs.EXEC_OPENSSL,
                        input=tmpPKCS12,
                        output=basedefs.FILE_ENGINE_KEYSTORE,
                        password=basedefs.CONST_KEY_PASS,
                    )
                ]
                utils.execCmd(cmdList=cmd, maskList=mask, useShell=True, failOnError=True, msg=MSG_ERROR_FAILED_SYNTHESIS_ENGINE_KEY)
                utils.chownToEngine(basedefs.FILE_ENGINE_KEYSTORE)
                os.chmod(basedefs.FILE_ENGINE_KEYSTORE, 0640)
            finally:
                if tmpPKCS12 is not None and os.path.exists(tmpPKCS12):
                    os.unlink(tmpPKCS12)

        for src, dst in (
            (basedefs.FILE_ENGINE_KEYSTORE, basedefs.FILE_APACHE_KEYSTORE),
            (basedefs.FILE_ENGINE_CERT, basedefs.FILE_APACHE_CERT),
            (basedefs.FILE_SSH_PRIVATE_KEY, basedefs.FILE_APACHE_PRIVATE_KEY)
        ):
            logging.debug("PKI: dup cert for apache")
            try:
                utils.copyFile(
                    src,
                    dst,
                    utils.getUsernameId("apache"),
                    utils.getGroupId("apache"),
                    0640
                )
            except OSError:
                logging.error("PKI: Cannot dup cert for apache")
                raise

        if not os.path.exists(basedefs.FILE_APACHE_CA_CRT_SRC):
            logging.debug("PKI: dup ca for apache")
            try:
                os.symlink(
                    os.path.basename(basedefs.FILE_CA_CRT_SRC),
                    basedefs.FILE_APACHE_CA_CRT_SRC
                )
            except OSError:
                logging.error("PKI: Cannot dup ca for apache")
                raise

        shutil.copyfile(
            basedefs.FILE_HTTPD_SSL_CONFIG,
            self.TMPAPACHECONF
        )
        handler = utils.TextConfigFileHandler(self.TMPAPACHECONF, " ")
        handler.open()
        if handler.getParam("SSLCertificateFile") == '/etc/pki/ovirt-engine/certs/engine.cer':
            handler.editParam("SSLCertificateFile", basedefs.FILE_APACHE_CERT)
        if handler.getParam("SSLCertificateKeyFile") == '/etc/pki/ovirt-engine/keys/engine_id_rsa':
            handler.editParam("SSLCertificateKeyFile", basedefs.FILE_APACHE_PRIVATE_KEY)

        if handler.getParam("SSLCertificateChainFile") == '/etc/pki/ovirt-engine/ca.pem':
            handler.editParam("SSLCertificateChainFile", basedefs.FILE_APACHE_CA_CRT_SRC)
        handler.close()

        utils.updateVDCOption("keystoreUrl", basedefs.FILE_ENGINE_KEYSTORE, (), "text")
        utils.updateVDCOption("TruststoreUrl", basedefs.FILE_TRUSTSTORE, (), "text")
        utils.updateVDCOption("CertAlias", "1", (), "text")

    def commit(self):
        shutil.move(self.TMPAPACHECONF, basedefs.FILE_HTTPD_SSL_CONFIG)

        if os.path.exists(self.JKSKEYSTORE):
            logging.debug("PKI: removing JKS keystore")
            try:
                os.remove(self.JKSKEYSTORE)
            except OSError:
                logging.error("PKI: cannot remove JKS keystore '%s'" % self.JKSKEYSTORE)

    def rollback(self):
        if os.path.exists(self.JKSKEYSTORE):
            for f in (
                self.TMPAPACHECONF,
                basedefs.FILE_ENGINE_KEYSTORE,
                basedefs.FILE_APACHE_KEYSTORE,
                basedefs.FILE_APACHE_CA_CRT_SRC,
                basedefs.FILE_APACHE_CERT,
                basedefs.FILE_APACHE_PRIVATE_KEY
            ):
                try:
                    os.remove(f)
                except OSError:
                    logging.error("PKI: cannot remove '%s'" % f)

def stopEngine(service=basedefs.ENGINE_SERVICE_NAME):
    logging.debug("stopping %s service.", service)
    cmd = [
        basedefs.EXEC_SERVICE, service, "stop",
    ]
    output, rc = utils. execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERR_FAILED_STOP_ENGINE_SERVICE)

def startEngine(service=basedefs.ENGINE_SERVICE_NAME):
    logging.debug("starting %s service.", service)
    cmd = [
        basedefs.EXEC_SERVICE, service, "start",
    ]
    output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERR_FAILED_START_ENGINE_SERVICE)

def runPost():
    logging.debug("Running post script")
    import post_upgrade as post
    post.run()
    logging.debug("Post script completed successfully")

def runFunc(funcList, dispString):
    sys.stdout.write("%s..." % dispString)
    sys.stdout.flush()
    spaceLen = basedefs.SPACE_LEN - len(dispString)
    try:
        for func in funcList:
            if type(func) is types.ListType:
                func[0](*func[1:])
            else:
                func()
        print ("[ " + utils.getColoredText(MSG_INFO_DONE, basedefs.GREEN) + " ]").rjust(spaceLen)
    except:
        print ("[ " + utils.getColoredText(MSG_INFO_ERROR, basedefs.RED) + " ]").rjust(spaceLen+3)
        raise

def printMessages():
    for msg in messages:
        logging.info(msg)
        print "* %s" % msg.strip()

def addAdditionalMessages(addReports=False):
    global messages
    messages.append(MSG_INFO_LOG_FILE + " " + logFile)

    if addReports:
        messages.append(MSG_INFO_REPORTS)


def stopDbRelatedServices(etlService, notificationService):
    """
    shut down etl and notifier services
    in order to disconnect any open sessions to the db
    """
    # If the ovirt-engine-etl service is installed, then try and stop it.
    if etlService.isServiceAvailable():
        try:
            etlService.stop(True)
        except:
            logging.warn("Failed to stop %s", etlService.name)
            logging.warn(traceback.format_exc())
            messages.append(MSG_ERR_FAILED_STOP_SERVICE % etlService.name)

    # If the ovirt-engine-notifierd service is up, then try and stop it.
    if notificationService.isServiceAvailable():
        try:
            (status, rc) = notificationService.status()
            if utils.verifyStringFormat(status, ".*running.*"):
                logging.debug("stopping %s service.", notificationService.name)
                notificationService.stop()
        except:
            logging.warn("Failed to stop %s service", notificationService.name)
            logging.warn(traceback.format_exc())
            messages.append(MSG_ERR_FAILED_STOP_SERVICE % notificationService.name)

def startDbRelatedServices(etlService, notificationService):
    """
    bring back any service we stopped
    we won't start services that are down
    but weren't stopped by us
    """
    if etlService.isServiceAvailable():
        (output, rc) = etlService.conditionalStart()
        if rc != 0:
            logging.warn("Failed to start %s", etlService.name)
            messages.append(MSG_ERR_FAILED_START_SERVICE % etlService.name)

    if notificationService.isServiceAvailable():
        (output, rc) = notificationService.conditionalStart()
        if rc != 0:
            logging.warn("Failed to start %s: exit code %d", notificationService.name, rc)
            messages.append(MSG_ERR_FAILED_START_SERVICE % notificationService.name)

def unsupportedVersionsPresent(oldversion=UNSUPPORTED_VERSION, dbName=basedefs.DB_NAME):
    """ Check whether there are UNSUPPORTED_VERSION
    objects present. If yes, throw an Exception

    dbName is provided for flexibility of quering different possible DBs.
    """
    queryCheckDCVersions="SELECT compatibility_version FROM storage_pool;"
    dcVersions, rc = utils.execRemoteSqlCommand(
        userName=SERVER_ADMIN,
        dbHost=SERVER_NAME,
        dbPort=SERVER_PORT,
        dbName=dbName,
        sqlQuery=queryCheckDCVersions,
        failOnError=True,
        errMsg=MSG_ERROR_CONNECT_DB,
    )
    queryCheckClusterVersions="SELECT compatibility_version FROM vds_groups;"
    clusterVersions, rc = utils.execRemoteSqlCommand(
        userName=SERVER_ADMIN,
        dbHost=SERVER_NAME,
        dbPort=SERVER_PORT,
        dbName=dbName,
        sqlQuery=queryCheckClusterVersions,
        failOnError=True,
        errMsg=MSG_ERROR_CONNECT_DB,
    )

    for versions in dcVersions, clusterVersions:
        if oldversion in versions:
            return True

    return False

def preupgradeUUIDCheck():
    VDSStatus_Maintenance = 2

    query="select vds_id, vds_name, host_name, vds_unique_id, status from vds;"
    out, rc = utils.parseRemoteSqlCommand(
        SERVER_ADMIN,
        SERVER_NAME,
        SERVER_PORT,
        basedefs.DB_NAME,
        query,
        True,
        MSG_ERROR_CONNECT_DB
    )

    def get_dups(l):
        """python-2.6 has no collections.Counter"""

        _seen = set()
        return [x for x in l if x in _seen or _seen.add(x)]

    dups = get_dups([i['vds_unique_id'].split('_')[0] for i in out])
    ok = True
    msgs = ""
    global hostids
    for r in out:
        warn = False
        if r['vds_unique_id'] == "":
            warn = True
            r['message'] = MSG_ERROR_UUID_FAILED_REASON_EMPTYUUID
        elif r['vds_unique_id'].startswith('_') or r['vds_unique_id'].startswith('Not_'):
            warn = True
            r['message'] = MSG_ERROR_UUID_FAILED_REASON_NOUUID
        elif r['vds_unique_id'].split('_')[0] in dups:
            warn = True
            r['message'] = MSG_ERROR_UUID_FAILED_REASON_DUPUUID

        if warn:
            msgs += MSG_ERROR_UUID_FAILED_HOST % (
                r['vds_name'],
                r['host_name'],
                r['message']
            )
            if int(r['status']) != VDSStatus_Maintenance:
                ok = False
        else:
            if '_' in r['vds_unique_id']:
                hostids[r['vds_id']] = r['vds_unique_id'].split('_')[0]

    if not ok:
        raise Exception(MSG_ERROR_UUID_VALIDATION_FAILED % msgs)

    if msgs != "":
        global messages
        messages.append(MSG_INFO_UUID_VALIDATION_INFO % msgs)

def modifyUUIDs():
    for vds_id, vds_unique_id in hostids.items():
        query="UPDATE vds_static SET vds_unique_id='%s' where vds_id='%s';" % (
            vds_unique_id,
            vds_id
        )
        utils.execRemoteSqlCommand(
            SERVER_ADMIN,
            SERVER_NAME,
            SERVER_PORT,
            basedefs.DB_NAME,
            query,
            True,
            MSG_ERROR_CONNECT_DB
        )

def deployDbAsyncTasks(dbName=basedefs.DB_NAME):
    # Deploy DB functionality first
    cmd = [
        basedefs.EXEC_PSQL,
        "-U", SERVER_ADMIN,
        "-f", basedefs.FILE_DB_ASYNC_TASKS,
        "-d", dbName,
    ]

    out, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg="Error updating DB for getting async_tasks", envDict=utils.getPgPassEnv())

def getRunningTasks(dbName=basedefs.DB_NAME):
    # Get async tasks:
    runningTasks, rc = utils.execRemoteSqlCommand(
                                       userName=SERVER_ADMIN,
                                       dbHost=SERVER_NAME,
                                       dbPort=SERVER_PORT,
                                       dbName=dbName,
                                       sqlQuery=ASYNC_TASKS_QUERY,
                                       failOnError=True,
                                       errMsg="Can't get async tasks list",
                                   )

    # We only want to return anything if there are really async tasks records
    if runningTasks and "RECORD" in runningTasks:
        return runningTasks
    else:
        return ""

def getCompensations(dbName=basedefs.DB_NAME):
    # Get compensations
    compensations, rc = utils.execRemoteSqlCommand(
                                       userName=SERVER_ADMIN,
                                       dbHost=SERVER_NAME,
                                       dbPort=SERVER_PORT,
                                       dbName=dbName,
                                       sqlQuery=COMPENSATIONS_QUERY,
                                       failOnError=True,
                                       errMsg="Can't get compensations list",
                                    )

    # We only want to return anything if there are really compensations records
    if not compensations or \
       (compensations and "0 rows" in compensations):
        return ""
    else:
        return compensations

def checkRunningTasks(dbName=basedefs.DB_NAME, service=basedefs.ENGINE_SERVICE_NAME):
    # Find running tasks first
    logging.debug(MSG_RUNNING_TASKS)
    deployDbAsyncTasks(dbName)
    runningTasks = getRunningTasks(dbName)
    compensations = getCompensations(dbName)
    engineConfigBinary = basedefs.FILE_ENGINE_CONFIG_BIN
    engineConfigExtended = basedefs.FILE_ENGINE_EXTENDED_CONF
    origTimeout = 0

    # Define state
    in_maintenance = False

    if runningTasks or compensations:

        # TODO: update runningTasks names/presentation and compensations
        timestamp = "\n[ " + utils.getCurrentDateTimeHuman() + " ] "
        stopTasksQuestion = MSG_STOP_RUNNING_TASKS % (
                                MSG_TASKS_COMPENSATIONS % (runningTasks, compensations),
                                timestamp,
                            )
        answerYes = utils.askYesNo(stopTasksQuestion)
        if not answerYes:
            raise Exception(output_messages.INFO_STOP_WITH_RUNNING_TASKS)


        timestamp = "\n[ " + utils.getCurrentDateTimeHuman() + " ] "
        print timestamp + output_messages.INFO_STOPPING_TASKS % MAINTENANCE_TASKS_WAIT_PERIOD_MINUTES

        # restart jboss/engine in maintenace mode (i.e different port):
        # Change AsyncTaskZombieTaskLifeInMinutes first
        origTimeout = utils.configureTasksTimeout(timeout=0,
                                                  engineConfigBin=engineConfigBinary,
                                                  engineConfigExtended=engineConfigExtended)


        try:

            # Enter maintenance mode
            utils.configureEngineForMaintenance()

            # Update engine state
            in_maintenance = True

            # Start the engine in maintenance mode
            startEngine(service)

            # Pull tasks in a loop for some time
            # MAINTENANCE_TASKS_WAIT_PERIOD  = 180 (seconds, between trials)
            # MAINTENANCE_TASKS_CYCLES = 20 (how many times to try)
            while runningTasks or compensations:
                time.sleep(MAINTENANCE_TASKS_WAIT_PERIOD)
                runningTasks = getRunningTasks(dbName)
                compensations = getCompensations(dbName)
                logging.debug(MSG_WAITING_RUNNING_TASKS)

                # Show the list of tasks to the user, ask what to do
                if runningTasks or compensations:
                    timestamp = "\n[ " + utils.getCurrentDateTimeHuman() + " ] "
                    stopTasksQuestion = MSG_STOP_RUNNING_TASKS % (
                                         MSG_TASKS_COMPENSATIONS % (runningTasks, compensations),
                                         timestamp,
                                        )
                    answerYes = utils.askYesNo(stopTasksQuestion)

                    # Should we continue? If yes, go for another iteration
                    # If not, break the loop
                    if not answerYes:
                        # There are still tasks running, so exit and tell to resolve
                        # before user continues.
                        RUNNING_TASKS_MSG = MSG_TASKS_COMPENSATIONS % (runningTasks, compensations)
                        raise Exception(MSG_TASKS_STILL_RUNNING % RUNNING_TASKS_MSG)

                    logging.debug(output_messages.INFO_RETRYING + output_messages.INFO_STOPPING_TASKS, MAINTENANCE_TASKS_WAIT_PERIOD_MINUTES)
                    timestamp = "\n[ " + utils.getCurrentDateTimeHuman() + " ] "
                    print timestamp + output_messages.INFO_RETRYING + output_messages.INFO_STOPPING_TASKS % MAINTENANCE_TASKS_WAIT_PERIOD_MINUTES

        except:
            logging.error(traceback.format_exc())
            raise

        finally:
            # Stop the engine first
            logging.debug(output_messages.INFO_STOP_ENGINE)
            stopEngine(service)
            # Restore previous engine configuration
            logging.debug(output_messages.INFO_RESTORING_NORMAL_CONFIGURATION)
            if in_maintenance:
                utils.restoreEngineFromMaintenance()
            # Restore previous zombie timeout
            utils.configureTasksTimeout(timeout=origTimeout,
                                        engineConfigBin=engineConfigBinary,
                                        engineConfigExtended=engineConfigExtended)



def main(options):
    # BEGIN: PROCESS-INITIALIZATION
    miniyumsink = utils.MiniYumSink()
    MiniYum.setup_log_hook(sink=miniyumsink)
    extraLog = open(logFile, "a")
    miniyum = MiniYum(sink=miniyumsink, extraLog=extraLog)
    miniyum.selinux_role()
    # END: PROCESS-INITIALIZATION

    # we do not wish to be interrupted
    signal.signal(signal.SIGINT, signal.SIG_IGN)

    rhyum = MYum(miniyum)
    db = DB()
    ca = CA()
    DB_NAME_TEMP = "%s_%s" % (basedefs.DB_NAME, utils.getCurrentDateTime())

    # Handle pgpass
    if not os.path.exists(basedefs.DB_PASS_FILE):
        if not os.path.exists(basedefs.ORIG_PASS_FILE):
            logging.error(MSG_ERROR_PGPASS)
            print MSG_ERROR_PGPASS
            sys.exit(1)
        else:
            logging.info("Info: Found .pgpass file at old location. Moving it to a new location.")

            # Create directory if needed
            dbPassFileDirectory = os.path.dirname(basedefs.DB_PASS_FILE)
            if not os.path.exists(dbPassFileDirectory):
                os.makedirs(dbPassFileDirectory)
            shutil.copy(basedefs.ORIG_PASS_FILE, basedefs.DB_PASS_FILE)

            # File is copied/created by root, so no need to verify the owner.
            os.chmod(basedefs.DB_PASS_FILE, 0600)
    else:
        logging.info("Info: %s file found. Continue.", basedefs.DB_PASS_FILE)

    # Functions/parameters definitions
    currentDbName = basedefs.DB_NAME
    stopEngineService = [stopEngine]
    startEngineService = [startEngine]
    preupgradeFunc = [preupgradeUUIDCheck]
    upgradeFunc = [rhyum.update]
    postFunc = [modifyUUIDs, ca.commit, runPost]
    engineService = basedefs.ENGINE_SERVICE_NAME
    # define db connections services
    etlService = utils.Service(basedefs.ETL_SERVICE_NAME)
    notificationService = utils.Service(basedefs.NOTIFIER_SERVICE_NAME)

    # Check for the available free space in required locations:
    # 1. DB backups location
    # 2. Yum cache location (for downloading packages)
    # 3. /usr/share location (for installing jboss and engine)
    if not options.no_space_check:
        # The second part of the map is the space required in each location for
        # the successfull upgrade.
        required_folders_map = {
            basedefs.DIR_DB_BACKUPS: basedefs.CONST_DB_SIZE,
            basedefs.DIR_YUM_CACHE: basedefs.CONST_DOWNLOAD_SIZE_MB,
            basedefs.DIR_PKGS_INSTALL: basedefs.CONST_INSTALL_SIZE_MB,
        }
        utils.checkAvailableSpace(required=required_folders_map,
                                  dbName=currentDbName,
                                  dbFolder=basedefs.DIR_DB_BACKUPS,
                                  msg=output_messages.MSG_STOP_UPGRADE_SPACE,
                                  )

    # Check minimal supported versions of installed components
    if unsupportedVersionsPresent(dbName=currentDbName):
        print MSG_ERROR_INCOMPATIBLE_UPGRADE
        raise Exception(MSG_ERROR_INCOMPATIBLE_UPGRADE)

    rhyum.clean()

    with rhyum.transaction():
        # Check for upgrade, else exit
        runFunc([rhyum.begin], MSG_INFO_CHECK_UPDATE)
        if rhyum.emptyTransaction:
            logging.debug(MSG_INFO_NO_UPGRADE_AVAIL)
            print MSG_INFO_NO_UPGRADE_AVAIL
            sys.exit(0)

        packages = rhyum.getPackages()
        name_packages = [p['name'] for p in packages]

        print MSG_INFO_UPGRADE_AVAIL % (len(packages))
        for p in packages:
            print " * %s" % p['display_name']
        if options.check_update:
            sys.exit(100)

        if RPM_SETUP in name_packages and not options.force_current_setup_rpm:
            logging.debug(MSG_ERROR_NEW_SETUP_AVAIL)
            print MSG_ERROR_NEW_SETUP_AVAIL
            sys.exit(3)

        # Make sure we will be able to rollback
        if not rhyum.rollbackAvailable(packages) and options.yum_rollback:
            logging.debug(MSG_ERROR_NO_ROLLBACK_AVAIL)
            print MSG_ERROR_NO_ROLLBACK_AVAIL
            print MSG_ERROR_CHECK_LOG % logFile
            sys.exit(2)

        # Update is related to database if:
        # 1. database related package is updated
        # 2. CA upgrade is going to alter parameters within database
        # 3. UUID update will take place
        updateRelatedToDB = False
        for package in RPM_BACKEND, RPM_DBSCRIPTS:
            if package in name_packages:
                updateRelatedToDB = True
                logging.debug("related to database package %s" % package)
        updateRelatedToDB = updateRelatedToDB or ca.mayUpdateDB()
        updateRelatedToDB = updateRelatedToDB or hostids

        # No rollback in this case
        try:
            # We ask the user before stoping ovirt-engine or take command line option
            if options.unattended_upgrade or checkEngine(engineService):
                # Stopping engine
                runFunc(stopEngineService, MSG_INFO_STOP_ENGINE % engineService)
                if updateRelatedToDB:
                    runFunc([[stopDbRelatedServices, etlService, notificationService]], MSG_INFO_STOP_DB)

                if not options.ignore_tasks:
                    # Check that there are no running tasks/compensations
                    try:
                        checkRunningTasks()
                    # If something went wrong, restart DB services and the engine
                    except:
                        runFunc([[startDbRelatedServices, etlService, notificationService]], MSG_INFO_START_DB)
                        runFunc(startEngineService, MSG_INFO_START_ENGINE % engineService)
                        raise
            else:
                # This means that user chose not to stop ovirt-engine
                logging.debug("exiting gracefully")
                print MSG_INFO_STOP_INSTALL_EXIT
                sys.exit(0)

            # Preupgrade checks
            runFunc(preupgradeFunc, MSG_INFO_PREUPGRADE)

            # Backup DB
            if updateRelatedToDB:
                runFunc([db.backup], MSG_INFO_BACKUP_DB)
                runFunc([[db.rename, DB_NAME_TEMP]], MSG_INFO_RENAME_DB)

        except Exception as e:
            print e
            raise

        # In case of failure, do rollback
        try:
            # yum update
            runFunc(upgradeFunc, MSG_INFO_YUM_UPDATE)

            # check if update is relevant to db update
            if updateRelatedToDB:

                # Update the db and restore its name back
                runFunc([db.update], MSG_INFO_DB_UPDATE)
                runFunc([[db.rename, basedefs.DB_NAME]], MSG_INFO_RESTORE_DB)

                # Bring up any services we shut down before db upgrade
                startDbRelatedServices(etlService, notificationService)

            # CA restore
            runFunc([ca.prepare], MSG_INFO_PKI_PREPARE)

            # post install conf
            runFunc(postFunc, MSG_INFO_RUN_POST)

        except:
            logging.error(traceback.format_exc())
            logging.error("Rolling back update")

            print MSG_ERROR_UPGRADE
            print MSG_INFO_REASON%(sys.exc_info()[1])

            # CA restore
            runFunc([ca.rollback], MSG_INFO_PKI_ROLLBACK)

            # allow db restore
            if updateRelatedToDB:
                try:
                    runFunc([db.restore], MSG_INFO_DB_RESTORE)
                except:
                    # This Exception have already been logged, so just pass along
                    pass

            raise

        finally:
            # start engine
            runFunc([startEngine], MSG_INFO_START_ENGINE % engineService)

        # Print log location on success
        addAdditionalMessages(etlService.isServiceAvailable())
        print "\n%s\n" % MSG_INFO_UPGRADE_OK
        printMessages()


if __name__ == '__main__':
    try:
        # Must run as root
        _verifyUserPermissions()

        # Init logging facility
        initLogging()

        # DB Configuration
        SERVER_NAME = utils.getDbHostName()
        SERVER_PORT = utils.getDbPort()
        SERVER_ADMIN = utils.getDbUser()

        # get user arguments
        (options, args) = getOptions()

        main(options)

    except SystemExit:
        raise

    except:
        print MSG_ERROR_CHECK_LOG % logFile
        logging.error(traceback.format_exc())
        sys.exit(1)
