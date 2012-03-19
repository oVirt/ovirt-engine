#!/usr/bin/python

# Imports
import sys
import os
import logging
import traceback
import tempfile
import shutil
import pwd
from optparse import OptionParser
from StringIO import StringIO
import common_utils as utils
import types
import basedefs

# Consts
BASE_NAME = "ovirt-engine"
PREFIX = "engine"
PROD_NAME = "oVirt Engine"

LOG_FILE = "%s-cleanup.log" % (PREFIX)

# PATH
LOG_PATH = "/var/log/%s" % (BASE_NAME)
PKI_DIR = "/etc/pki/%s" % (BASE_NAME)
TRUSTORE = os.path.join(PKI_DIR, ".truststore")
KEYSTORE = os.path.join(PKI_DIR, ".keystore")
JBOSS_SERVER_DIR = "/var/lib/jbossas/server"
VAR_SLIMMED_DIR = os.path.join(JBOSS_SERVER_DIR, basedefs.JBOSS_PROFILE_NAME)
ETC_JBOSS_DIR = "/etc/jbossas"
ETC_SLIMMED_DIR = os.path.join(ETC_JBOSS_DIR, basedefs.JBOSS_PROFILE_NAME)
PKI_BACKUP_DIR = "/etc/pki/%s-backups" % (BASE_NAME)

# Default DB Configuration
DB_HOST = basedefs.DB_HOST
DB_PORT = basedefs.DB_PORT
DB_ADMIN = basedefs.DB_ADMIN

#MSGS
MSG_ERROR_USER_NOT_ROOT = "Error: insufficient permissions for user %s, you must run with user root."
MSG_RC_ERROR = "Return Code is not zero"
MSG_ERROR_BACKUP_DB = "Error: Database backup failed"
MSG_ERROR_DROP_DB = "Error: Database drop failed"
MSG_ERROR_CHECK_LOG = "Error: Cleanup failed.\nplease check log at %s"
MSG_ERR_FAILED_JBOSS_SERVICE_STILL_RUN = "Error: Can't stop jboss service. Please shut it down manually."
MSG_ERR_FAILED_STP_JBOSS_SERVICE = "Error: Can't stop JBoss"
MSG_ERR_FAILED_STATUS_JBOSS_SERVICE = "Error: Can't get JBoss service status"

MSG_INFO_DONE = "DONE"
MSG_INFO_ERROR = "ERROR"
MSG_INFO_STOP_JBOSS = "Stopping JBoss Service"
MSG_INFO_BACKUP_DB = "Backing Up Database"
MSG_INFO_REMOVE_DB = "Removing Database"
MSG_INFO_REMOVE_SLIMMED = "Removing %s JBoss profile" % (PROD_NAME)
MSG_INFO_REMOVE_CA = "Removing CA"
MSG_INFO_UNLINK_EAR = "Removing EAR link"
MSG_INFO_DB_BACKUP_FILE = "DB Backup available at"
MSG_INFO_LOG_FILE = "Cleanup log available at"
MSG_INFO_CLEANUP_OK = "\nCleanup finished successfully!"
MSG_INFO_FINISHED_WITH_ERRORS = "\nCleanup finished with errors, please see log file"
MSG_INFO_STOP_INSTALL_EXIT = "Cleanup stopped, Goodbye."
MSG_INFO_KILL_DB_CONNS="Stopping all connections to DB"

MSG_ALERT_CLEANUP = "WARNING: Executing %s cleanup utility.\n\
This utility will wipe all existing data including configuration settings, certificates and database.\n\
In addition, all existing DB connections will be closed." % (PROD_NAME)
MSG_PROCEED_QUESTION = "Would you like to proceed"

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

    parser.add_option("-l", "--dont-unlink-ear",
                      action="store_false", dest="unlink_ear", default=True,
                      help="Don't unlink ear")

    #parser.add_option("-s", "--dont-remove-profile",
    #                  action="store_false", dest="remove_slimmed", default=True,
    #                  help="Don't remove slimmed JBoss profile")

    (options, args) = parser.parse_args()
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
    username = pwd.getpwuid(os.getuid())[0]
    if (username != 'root'):
        print MSG_ERROR_USER_NOT_ROOT % (username)
        sys.exit(1)

class CA():
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

        # Remove trustore
        if os.path.exists(TRUSTORE):
            logging.debug("Removing %s", TRUSTORE)
            os.remove(TRUSTORE)
        else:
            logging.debug("%s doesn't exists", TRUSTORE)

        # Remove keystore
        if os.path.exists(KEYSTORE):
            logging.debug("Removing %s", KEYSTORE)
            os.remove(KEYSTORE)
        else:
            logging.debug("%s doesn't exists", KEYSTORE)

        logging.debug("CA Remove completed successfully")

class DB():
    def __init__(self):
        self.sqlfile = tempfile.mkstemp(suffix=".sql", dir=basedefs.DIR_DB_BACKUPS)[1]
        self.dropped = False

    def __del__(self):
        if self.dropped:
            logging.debug(MSG_INFO_DB_BACKUP_FILE + " " + self.sqlfile)
            print MSG_INFO_DB_BACKUP_FILE,
            print self.sqlfile
        else:
            os.remove(self.sqlfile)

    def backup(self):
        """
        Backup db using pg_dump
        """
        # pg_dump -C -E UTF8  --column-inserts --disable-dollar-quoting  --disable-triggers -U postgres --format=p -f $dir/$file  dbname
        logging.debug("DB Backup started")
        cmd = [basedefs.EXEC_PGDUMP,
                        "-C", "-E", "UTF8",
                        "--column-inserts", "--disable-dollar-quoting",  "--disable-triggers",
                        "-U", DB_ADMIN,
                        "-h", DB_HOST,
                        "-p", DB_PORT,
                        "--format=p",
                        "-f", self.sqlfile,
                        basedefs.DB_NAME]
        utils.execCmd(cmd, None, True, MSG_ERROR_BACKUP_DB, [])
        logging.debug("DB Backup completed successfully")

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
            cmd = [basedefs.EXEC_DROPDB, "-w", "-U", DB_ADMIN, "-h", DB_HOST, "-p", DB_PORT, dbname]
            output, rc = utils.execCmd(cmd, None, False, MSG_ERROR_DROP_DB)
            if rc:
                logging.error("DB drop operation failed. Check that there are no active connection to the '%s' DB." % dbname)
                raise Exception(MSG_ERROR_DROP_DB)
        self.dropped = True
        logging.debug("DB Drop completed successfully")

    def exists(self):
        """
        check that db exists
        """
        logging.debug("verifying that db '%s' exists" % (basedefs.DB_NAME))

        # Making sure postgresql service is up - only on local installation
        if utils.localHost(DB_HOST):
            postgresql = utils.Service("postgresql")
            postgresql.conditionalStart()

        try:
            # We want to make check that we can connect to basedefs.DB_NAME DB
            logging.debug("making sure postgresql service is up")
            utils.retry(utils.checkIfDbIsUp, tries=5, timeout=15, sleep=3)
        except:
            # If we're here, it means something is wrong, either there is a real db error
            # or the db is not installed, let's check
            logging.debug("checking if db is already installed..")
            out, rc = utils.execRemoteSqlCommand(DB_ADMIN, DB_HOST, DB_PORT, basedefs.DB_NAME, "select 1")
            if rc != 0:
                if utils.verifyStringFormat(out,".*FATAL:\s*database\s*\"%s\"\s*does not exist" % (PREFIX)):
                    # This means that the db is not installed, so we return false
                    return False

        return True

def stopJboss():
    logging.debug("stoping jboss service.")

    cmd = [basedefs.EXEC_SERVICE, basedefs.JBOSS_SERVICE_NAME, "stop"]
    output, rc = utils.execCmd(cmd, None, True, MSG_ERR_FAILED_STP_JBOSS_SERVICE, [])

    # JBoss service sometimes return zero rc even if service is still up
    if "[FAILED]" in output and "Timeout: Shutdown command was sent, but process is still running" in output:
        raise OSError(MSG_ERR_FAILED_JBOSS_SERVICE_STILL_RUN)


def runFunc(funcs, dispString):
    print "%s..." % (dispString),
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

def unlinkEar():
    links = [os.path.join(basedefs.DIR_JBOSS, "standalone", "deployments", "engine.ear"),
             os.path.join(basedefs.DIR_JBOSS, "standalone", "deployments", "ROOT.war")]

    for link in links:
        if os.path.exists(link):
            os.unlink(link)

def main(options):
    db = DB()
    ca = CA()

    if not options.unattended_clean:
        # Ask user to proceed
        if not askForUserApproval():
            print MSG_INFO_STOP_INSTALL_EXIT
            sys.exit(0)

    print

    # Stop JBoss
    runFunc(stopJboss, MSG_INFO_STOP_JBOSS)

    # Backup and drop DB (only if 'basedefs.DB_NAME' db exists)
    if db.exists() and options.drop_db:
        runFunc([db.backup, db.drop], MSG_INFO_REMOVE_DB)

    # Remove CA
    if options.remove_ca:
        runFunc([ca.backup, ca.remove], MSG_INFO_REMOVE_CA)

    # Unlink ear link in JBoss
    if options.unlink_ear:
        runFunc([unlinkEar], MSG_INFO_UNLINK_EAR)


    if len(err_messages) == 0:
        print MSG_INFO_CLEANUP_OK
    else:
        _printErrlMessages()

    print MSG_INFO_LOG_FILE,
    print LOG_FILE

if __name__ == '__main__':
    try:
        # Must run as root
        _verifyUserPermissions()

        # Init logging facility
        initLogging()

        # Initialise DB settings
        DB_HOST = utils.getDbHostName()
        DB_PORT = utils.getDbPort()
        DB_ADMIN = utils.getDbAdminUser()

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
