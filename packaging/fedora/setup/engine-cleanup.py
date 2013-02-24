#!/usr/bin/python

# Imports
import sys
import os
import logging
import traceback
import tempfile
import shutil
import pwd
from optparse import OptionParser, OptionValueError
from StringIO import StringIO
import common_utils as utils
import types
import basedefs
import nfsutils

# Consts
BASE_NAME = "ovirt-engine"
PREFIX = "engine"
PROD_NAME = "oVirt Engine"

LOG_FILE = "%s-cleanup.log" % (PREFIX)

# PATH
LOG_PATH = "/var/log/%s" % (BASE_NAME)
PKI_DIR = "/etc/pki/%s" % (BASE_NAME)
PKI_BACKUP_DIR = "/etc/pki/%s-backups" % (BASE_NAME)

# Default DB Configuration
DB_HOST = basedefs.DB_HOST
DB_PORT = basedefs.DB_PORT
DB_ADMIN = basedefs.DB_ADMIN

#MSGS
MSG_ERROR_USER_NOT_ROOT = "Error: insufficient permissions for user %s, you must run with user root."
MSG_RC_ERROR = "Return Code is not zero"
MSG_ERROR_CONNECT_DB = "Error: Couldn't connect to the database server.\
Check that connection is working and rerun the cleanup utility"
MSG_ERROR_BACKUP_DB = "Error: Database backup failed"
MSG_ERROR_DROP_DB = "Error: DB drop operation failed. Check that there are no active connection to the '%s' DB"
MSG_ERROR_CHECK_LOG = "Error: Cleanup failed.\nplease check log at %s"
MSG_ERR_FAILED_ENGINE_SERVICE_STILL_RUN = "Error: Can't stop ovirt-engine service. Please shut it down manually."
MSG_ERR_FAILED_STP_ENGINE_SERVICE = "Error: Can't stop ovirt-engine"
MSG_ERR_FAILED_STATUS_ENGINE_SERVICE = "Error: Can't get ovirt-engine service status"
MSG_ERR_CANT_FIND_PGPASS_FILE="Could not find DB password file %s. Skipping DB cleanup" % basedefs.DB_PASS_FILE

MSG_INFO_DONE = "DONE"
MSG_INFO_ERROR = "ERROR"
MSG_INFO_STOP_ENGINE = "Stopping %s service" % basedefs.ENGINE_SERVICE_NAME
MSG_INFO_STOP_NOTIFIERD = "Stopping engine-notifierd service"
MSG_INFO_BACKUP_DB = "Backing Up Database"
MSG_INFO_REMOVE_DB = "Removing Database"
MSG_INFO_REMOVE_CA = "Removing CA"
MSG_INFO_UNLINK_EAR = "Removing EAR link"
MSG_INFO_DB_BACKUP_FILE = "DB Backup available at"
MSG_INFO_LOG_FILE = "Cleanup log available at"
MSG_INFO_CLEANUP_OK = "\nCleanup finished successfully!"
MSG_INFO_FINISHED_WITH_ERRORS = "\nCleanup finished with errors, please see log file"
MSG_INFO_STOP_INSTALL_EXIT = "Cleanup stopped, Goodbye."
MSG_INFO_KILL_DB_CONNS = "Stopping all connections to DB"
MSG_INFO_DELETE_SYSCTL_CONF = "Error deleting %s" % basedefs.FILE_ENGINE_SYSCTL

MSG_ALERT_CLEANUP = "WARNING: Executing %s cleanup utility.\n\
This utility will wipe all existing data including configuration settings, certificates and database.\n\
In addition, all existing DB connections will be closed." % (PROD_NAME)
MSG_PROCEED_QUESTION = "Would you like to proceed"

MSG_INFO_CLEANING_NFS="Cleaning NFS Exports"
MSG_CLEAN_NFS_EXPORTS_QUESTION="Would you like to remove %s configuration from \
{files}" % basedefs.APP_NAME
MSG_CLEAN_NFS_EXPORTED_DIRS_QUESTION="Would you like to remove the following \
directories:\n%s\n"
#global err msg list
err_messages = []


# Code

def getOptions():
    parser = OptionParser()

    parser.add_option("-u", "--unattended",
                      action="store_true", dest="unattended_clean", default=False,
                      help="unattended cleanup")

    parser.add_option("-d", "--dont-drop-db",
                      action="store_false", dest="drop_db", default=True,
                      help="Don't drop database")

    parser.add_option("-c", "--dont-remove-ca",
                      action="store_false", dest="remove_ca", default=True,
                      help="Don't remove CA")

    parser.add_option("-e", "--remove-nfs-exports",
                      action="store_true", dest="remove_nfs_exports",
                      default=False, help="Remove NFS exports")

    parser.add_option("-n", "--remove-exported-content",
                      action="store_true", dest="remove_nfs_content",
                      default=False, help="Remove NFS exported content")

    #parser.add_option("-s", "--dont-remove-profile",
    #                  action="store_false", dest="remove_slimmed", default=True,
    #                  help="Don't remove slimmed JBoss profile")

    (options, args) = parser.parse_args()
    if options.remove_nfs_content and not options.remove_nfs_exports:
        raise OptionValueError("-n can't be used without -e option")
    return (options, args)


def askYesNo(question=None):
    """
    service func to ask yes/no
    input from user
    """
    message = StringIO()
    userQuestion = "%s? (yes|no): " % (question)
    logging.debug("asking user: %s", userQuestion)
    message.write(userQuestion)
    message.seek(0)
    answer = raw_input(message.read())
    logging.debug("user answered: %s", answer)
    answer = answer.lower()
    if answer == "yes" or answer == "y":
        return True
    elif answer == "no" or answer == "n":
        return False
    else:
        return askYesNo(question)


def _getColoredText(text, color):
    """
        gets text string and color
        and returns a colored text.
        the color values are RED/BLUE/GREEN/YELLOW
        everytime we color a text, we need to disable
        the color at the end of it, for that
        we use the NO_COLOR chars.
    """
    return color + text + basedefs.NO_COLOR


def askForUserApproval():
    """
    Ask user to proceed with cleanup
    """

    logging.debug("Asking user for approval")
    print MSG_ALERT_CLEANUP
    answer = askYesNo(MSG_PROCEED_QUESTION)

    if answer:
        logging.debug("User chose to proceed")
        return True
    else:
        logging.debug("User chose to exit")
        return False


def cleanNFSExports():
    """
    If the user choose to clean NFS exports by command line or interactive
    prompt, removes any exported directory configured by the setup script
    from /etc/exports.
    For any line removed, if the user choose to clean the exported directories
    by command line or interactive prompt, removes the directories.
    """
    search_path = [basedefs.FILE_ETC_EXPORTS]
    msg_files = basedefs.FILE_ETC_EXPORTS
    exportFilePath = os.path.join(basedefs.DIR_ETC_EXPORTSD,
        "%s-iso-domain.exports" % basedefs.ENGINE_SERVICE_NAME)
    if os.path.exists(exportFilePath):
        search_path.append(exportFilePath)
        msg_files = "%s and %s" % (basedefs.FILE_ETC_EXPORTS, exportFilePath)

    if not options.unattended_clean:
        msg = MSG_CLEAN_NFS_EXPORTS_QUESTION.format(files=msg_files)
        options.remove_nfs_exports = askYesNo(msg)
    if not options.remove_nfs_exports:
        logging.debug("User chose to not clean NFS exports")
        return
    logging.debug("User chose to clean NFS exports")

    removed = []
    for path in search_path:
        removed += nfsutils.cleanNfsExports(" %s installer" % basedefs.APP_NAME,
                                            path)
    if len(removed) == 0:
        return
    nfsutils.refreshNfsExports()
    path_list = ""
    for p in removed:
        path_list += "- %s\n" % p
    if not options.unattended_clean:
        options.remove_nfs_content = askYesNo(
            MSG_CLEAN_NFS_EXPORTED_DIRS_QUESTION % path_list)
    if not options.remove_nfs_content:
        logging.debug("User chose to not clean NFS exported directories")
        return
    logging.debug("User chose to clean NFS exported directories")
    for p in removed:
        logging.debug("Removing directory %s" % p)
        shutil.rmtree(p)


def initLogging():
    global LOG_FILE
    try:
        if not os.path.isdir(LOG_PATH):
            os.makedirs(LOG_PATH)
        LOG_FILE = "%s/%s-cleanup_%s.log" % (LOG_PATH, PREFIX,  utils.getCurrentDateTime())
        level = logging.DEBUG
        # TODO: Move to mode="a"?
        hdlr = logging.FileHandler(filename = LOG_FILE, mode='w')
        fmts = '%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
        dfmt = '%Y-%m-%d %H:%M:%S'
        fmt = logging.Formatter(fmts, dfmt)
        hdlr.setFormatter(fmt)
        logging.root.addHandler(hdlr)
        logging.root.setLevel(level)
    except:
        logging.error(traceback.format_exc())
        raise Exception("Failed to initiate logger")

def _verifyUserPermissions():
    if (os.geteuid() != 0):
        username = pwd.getpwuid(os.geteuid())[0]
        print MSG_ERROR_USER_NOT_ROOT % (username)
        sys.exit(1)

def cleanPgpass():
    '''
    This function cleans engine entries from pgpass file
    '''

    # Nothing to do if the .pgpass file doesn't exist:
    if not os.path.exists(basedefs.DB_PASS_FILE):
        logging.debug("The %s file doesn't exist." % basedefs.DB_PASS_FILE)
        return

    backupFile = None
    # Cleaning .pgpass
    try:
        backupFile = "%s.%s" % (basedefs.DB_PASS_FILE, utils.getCurrentDateTime())
        logging.debug("Found %s file, backing current to %s" % (basedefs.DB_PASS_FILE, backupFile))
        # Remove file as not needed after cleanup
        # It is our file, so we can just rename it.
        shutil.move(basedefs.DB_PASS_FILE, backupFile)

    except:
        logging.error("Failed to clean %s" % basedefs.DB_PASS_FILE)
        logging.debug("Restoring original %s file from backup %s" % (basedefs.DB_PASS_FILE, backupFile))
        shutil.copyfile(backupFile, basedefs.DB_PASS_FILE)
        raise Exception("Failed to clean %s" % basedefs.DB_PASS_FILE)

    # if cleaning ok, remove backup
    logging.debug("Removing %s" % backupFile)
    os.remove(backupFile)
    logging.debug("Cleaning %s completed successfully" % basedefs.DB_PASS_FILE)


class CA():

    def exists(self):
        if not os.path.exists(PKI_DIR):
            logging.info("PKI folder %s was not found. Skipping CA cleanup", PKI_DIR)
            return False

        return True

    def backup(self):
        logging.debug("CA backup started")

        if not os.path.isdir(PKI_BACKUP_DIR):
            os.mkdir(PKI_BACKUP_DIR)

        # Do backup
        now = utils.getCurrentDateTime()
        backupDir = os.path.join(PKI_BACKUP_DIR, "%s-%s" % (BASE_NAME, now))
        logging.debug("Copy %s to %s", PKI_DIR, backupDir)
        shutil.copytree(PKI_DIR, backupDir, True)

        logging.debug("CA backup completed successfully")

    def remove(self):
        logging.debug("CA Remove started")

        for f in (
            basedefs.FILE_CA_CRT_SRC,
            basedefs.FILE_APACHE_CA_CRT_SRC,
            basedefs.FILE_TRUSTSTORE,
            basedefs.FILE_ENGINE_KEYSTORE,
            basedefs.FILE_APACHE_KEYSTORE,
            basedefs.FILE_APACHE_PRIVATE_KEY,
            basedefs.FILE_SSH_PRIVATE_KEY
        ):
            try:
                logging.debug("Removing %s", f)
                os.remove(f)
            except OSError:
                logging.debug("%s doesn't exists", f)

        logging.debug("CA Remove completed successfully")

class DB():
    def __init__(self):
        self.sqlfile = tempfile.mkstemp(suffix=".sql", dir=basedefs.DIR_DB_BACKUPS)[1]
        self.dropped = False
        self.env = utils.getPgEnv()

    def __del__(self):
        if self.dropped:
            logging.debug(MSG_INFO_DB_BACKUP_FILE + " " + self.sqlfile)
            print MSG_INFO_DB_BACKUP_FILE,
            print self.sqlfile
        else:
            os.remove(self.sqlfile)

    def backup(self):
        utils.backupDB(
            db=basedefs.DB_NAME,
            backup_file=self.sqlfile,
            env=self.env,
            user=DB_ADMIN,
            host=DB_HOST,
            port=DB_PORT,
        )

    def drop(self):
        """
        Drops db using dropdb
        """
        logging.debug("DB Drop started")

        # Block New connections and disconnect active ones - only on local installation.
        if utils.localHost(DB_HOST):
            utils.clearDbConnections(basedefs.DB_NAME)

        # Drop DBs - including the tempoarary ones created during upgrade operations
        # go over all dbs in the list of temp DBs and remove them
        for dbname in utils.listTempDbs():
            cmd = [
                basedefs.EXEC_DROPDB,
                "-w",
                "-U", DB_ADMIN,
                "-h", DB_HOST,
                "-p", DB_PORT,
                dbname,
            ]
            output, rc = utils.execCmd(cmdList=cmd, failOnError=False, msg=MSG_ERROR_DROP_DB, envDict=self.env)
            if rc:
                logging.error(MSG_ERROR_DROP_DB % dbname)
                raise Exception(MSG_ERROR_DROP_DB % dbname)
        self.dropped = True
        logging.debug("DB Drop completed successfully")


    def exists(self):
        """
        check that db exists
        """
        logging.debug("verifying that db '%s' exists" % (basedefs.DB_NAME))

        # Checking whether pgpass file exists. If not,
        # we cannot cleanup DB, so return False.
        if not os.path.exists(basedefs.DB_PASS_FILE):
            logging.info(MSG_ERR_CANT_FIND_PGPASS_FILE)
            return False

        # Making sure postgresql service is up - only on local installation
        if utils.localHost(DB_HOST):
            postgresql = utils.Service("postgresql")
            postgresql.conditionalStart()

        try:
            # We want to make check that we can connect to basedefs.DB_NAME DB
            logging.debug("Checking that DB '%s' exists", basedefs.DB_NAME)
            utils.retry(utils.checkIfDbIsUp, tries=5, timeout=15, sleep=3)
        except:
            # If we're here, it means something is wrong, either there is a real db error
            # or the db is not installed, let's check
            logging.debug("checking if db is already installed..")
            out, rc = utils.execRemoteSqlCommand(
                userName=DB_ADMIN,
                dbHost=DB_HOST,
                dbPort=DB_PORT,
                dbName=basedefs.DB_NAME,
                sqlQuery="select 1",
            )
            if rc != 0:
                if utils.verifyStringFormat(out,".*FATAL:\s*database\s*\"%s\"\s*does not exist" % (PREFIX)):
                    # This means that the db is not installed, so we return false
                    return False
                else:
                    raise Exception(MSG_ERROR_CONNECT_DB)

        return True

def deleteSysctlConf():
    try :
        os.remove(basedefs.FILE_ENGINE_SYSCTL)
    except:
        raise Exception(MSG_INFO_DELETE_SYSCTL_CONF)

def stopEngine():
    logging.debug("stoping %s service." % basedefs.ENGINE_SERVICE_NAME)

    cmd = [
        basedefs.EXEC_SERVICE, basedefs.ENGINE_SERVICE_NAME, "stop",
    ]
    output, rc = utils.execCmd(cmdList=cmd, failOnError=True, msg=MSG_ERR_FAILED_STP_ENGINE_SERVICE)

    # JBoss service sometimes return zero rc even if service is still up
    if "[FAILED]" in output and "Timeout: Shutdown command was sent, but process is still running" in output:
        raise OSError(MSG_ERR_FAILED_ENGINE_SERVICE_STILL_RUN)

def stopNotifier():
    logging.debug("stoping engine-notifierd service.")

    notifier = utils.Service(basedefs.NOTIFIER_SERVICE_NAME)
    if notifier.isServiceAvailable():
        notifier.stop(True)

def runFunc(funcs, dispString):
    sys.stdout.write("%s..." % dispString)
    sys.stdout.flush()
    spaceLen = basedefs.SPACE_LEN - len(dispString)
    try:
        if type(funcs) is types.ListType:
            for funcName in funcs:
                funcName()
        elif type(funcs) is types.FunctionType:
            funcs()
        print ("[ " + _getColoredText(MSG_INFO_DONE, basedefs.GREEN) + " ]").rjust(spaceLen)
    except:
        print ("[ " + _getColoredText(MSG_INFO_ERROR, basedefs.RED) + " ]").rjust(spaceLen + 2)
        logging.error(traceback.format_exc())
        err_messages.append(sys.exc_info()[1])

def _printErrlMessages():
    print MSG_INFO_FINISHED_WITH_ERRORS
    for msg in err_messages:
        logging.info('%s'%(msg))
        print ('%s'%(msg))

def main(options):
    db = DB()
    ca = CA()

    if not options.unattended_clean:
        # Ask user to proceed
        if not askForUserApproval():
            print MSG_INFO_STOP_INSTALL_EXIT
            sys.exit(0)

    print

    # Stop Engine
    runFunc(stopEngine, MSG_INFO_STOP_ENGINE)

    # Backup DB, drop DB and clean .pgpass file (only if 'basedefs.DB_NAME' db exists)
    # If --dont-drop-db option was supplied, will skip checking the DB
    if options.drop_db and db.exists():
        runFunc([db.backup, db.drop, cleanPgpass], MSG_INFO_REMOVE_DB)

    # Remove 00-ovirt.conf
    if os.path.exists(basedefs.FILE_ENGINE_SYSCTL):
        deleteSysctlConf()

    # Remove CA
    if ca.exists() and options.remove_ca:
        runFunc([ca.backup, ca.remove], MSG_INFO_REMOVE_CA)

    # Stop notifierd service
    runFunc(stopNotifier, MSG_INFO_STOP_NOTIFIERD)

    # Clean NFS exports
    runFunc(cleanNFSExports, MSG_INFO_CLEANING_NFS)

    if len(err_messages) == 0:
        print MSG_INFO_CLEANUP_OK
    else:
        _printErrlMessages()

    print MSG_INFO_LOG_FILE,
    print LOG_FILE

if __name__ == '__main__':
    try:
        # Change to the root directory to avoid problems if our current
        # working directory is deleted:
        os.chdir("/")

        # Must run as root
        _verifyUserPermissions()

        # Init logging facility
        initLogging()

        # Initialise DB settings
        DB_HOST = utils.getDbHostName()
        DB_PORT = utils.getDbPort()
        DB_ADMIN = utils.getDbUser()

        # get iso and domain from user arguments
        (options, args) = getOptions()

        main(options)
    except SystemExit:
        raise

    except BaseException as e:
        logging.error(traceback.format_exc())
        print
        print e
        print MSG_ERROR_CHECK_LOG % (LOG_FILE)
        sys.exit(1)
