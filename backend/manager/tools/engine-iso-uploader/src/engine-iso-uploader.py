#!/usr/bin/python

import sys
import os
from optparse import OptionParser, OptionGroup, SUPPRESS_HELP
import subprocess
import shlex
import logging
import locale
import gettext
import pprint
import urllib
import urllib2
import base64
import traceback
import tempfile
import shutil
from pwd import getpwnam
import getpass

from schemas import api


APP_NAME = "engine-iso-uploader"
VERSION = "1.0.0"
STREAM_LOG_FORMAT = '%(levelname)s: %(message)s'
FILE_LOG_FORMAT = '%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
FILE_LOG_DSTMP = '%Y-%m-%d %H:%M:%S'
DEFAULT_IMAGES_DIR = 'images/11111111-1111-1111-1111-111111111111'
NFS_MOUNT_OPTS = '-t nfs -o rw,sync,soft'
NFS_UMOUNT_OPTS = '-t nfs -f '
NFS_USER = 'vdsm'
NUMERIC_VDSM_ID = 36
SUDO='/usr/bin/sudo'
MOUNT='/bin/mount'
UMOUNT='/bin/umount'
SSH='/usr/bin/ssh'
SCP='/usr/bin/scp'
CP='/bin/cp'
RM='/bin/rm -fv'
MV='/bin/mv -fv'
CHOWN='/bin/chown'
CHMOD='/bin/chmod'
TEST='/usr/bin/test'
DEFAULT_CONFIGURATION_FILE='/etc/engine/isouploader.conf'
DEFAULT_LOG_FILE='/var/log/engine/engine-iso-uploader.log'
PERMS_MASK='640'
PYTHON='/usr/bin/python'

def multilog(logger, msg):
     for line in str(msg).splitlines():
         logger(line)

def get_from_prompt(msg, default=None, prompter=raw_input):
    try:
        return prompter(msg)
    except EOFError:
        print
        return default

class ExitCodes():
    """
    A simple psudo-enumeration class to hold the current and future exit codes
    """
    NOERR=0
    CRITICAL=1
    LIST_ISO_ERR=2
    UPLOAD_ERR=3
    CLEANUP_ERR=4
    exit_code=NOERR

class Commands():
    """
    A simple psudo-enumeration class to facilitate command checking.
    """
    LIST = 'list'
    UPLOAD = 'upload'
    #DELETE = 'delete'
    ARY = [LIST, UPLOAD]

class Caller(object):
    """
    Utility class for forking programs.
    """
    def __init__(self, configuration):
        self.configuration = configuration

    def prep(self, cmd):
        _cmd = cmd % self.configuration
        logging.debug(_cmd)
        return shlex.split(_cmd)

    def call(self, cmds):
        """Uses the configuration to fork a subprocess and run cmds"""
        _cmds = self.prep(cmds)
        logging.debug("_cmds(%s)" % _cmds)
        proc = subprocess.Popen(_cmds,
                   stdout=subprocess.PIPE,
                   stderr=subprocess.PIPE)
        stdout, stderr = proc.communicate()
        returncode = proc.returncode
        logging.debug("returncode(%s)" % returncode)
        logging.debug("STDOUT(%s)" % stdout)
        logging.debug("STDERR(%s)" % stderr)

        if returncode == 0:
            return (stdout,returncode)
        else:
            raise Exception(stderr)



class Configuration(dict):
    """This class is a dictionary subclass that knows how to read and """
    """handle our configuration. Resolution order is defaults -> """
    """configuration file -> command line options."""

    class SkipException(Exception):
        "This exception is raised when the user aborts a prompt"
        pass

    def __init__(self,
                 parser=None):
        self.command = None
        self.parser = parser
        self.options = None
        self.args = None
        self.files = []

        # Immediately, initialize the logger to the INFO log level and our
        # logging format which is <LEVEL>: <MSG> and not the default of
        # <LEVEL>:<UID: <MSG>
        self.__initLogger(logging.INFO)

        if not parser:
            raise Exception("Configuration requires a parser")

        self.options, self.args = self.parser.parse_args()

        if os.geteuid() != 0:
            raise Exception("This tool requires root permissions to run.")

        # At this point we know enough about the command line options
        # to test for verbose and if it is set we should re-initialize
        # the logger to DEBUG.  This will have the effect of printing
        # stack traces if there are any exceptions in this class.
        if getattr(self.options, "verbose"):
            self.__initLogger(logging.DEBUG)

        self.load_config_file()

        if self.options:
            # Need to parse again to override configuration file options
            self.options, self.args = self.parser.parse_args(values=self.options)
            self.from_options(self.options, self.parser)
            # Need to parse out options from the option groups.
            self.from_option_groups(self.options, self.parser)

        if self.args:
            self.from_args(self.args)

        # Finally, all options from the command line and possibly a configuration
        # file have been processed.  We need to re-initialize the logger if
        # the user has supplied either --quiet processing or supplied a --log-file.
        # This will ensure that any further log messages throughout the lifecycle
        # of this program go to the log handlers that the user has specified.
        if self.options.log_file or self.options.quiet:
            level = logging.INFO
            if self.options.verbose:
                level = logging.DEBUG
            self.__initLogger(level, self.options.quiet, self.options.log_file)

    def __missing__(self, key):
        return None

    def load_config_file(self):
        """Loads the user-supplied config file or the system default.
           If the user supplies a bad filename we will stop."""

        if self.options and getattr(self.options, "conf_file"):
            if os.path.isfile(self.options.conf_file):
                self.from_file(self.options.conf_file)
            else:
                raise Exception("The specified configuration file does not exist.  File=(%s)" %
                                self.options.conf_file)

        elif os.path.isfile(DEFAULT_CONFIGURATION_FILE):
            self.from_file(DEFAULT_CONFIGURATION_FILE)

    def from_option_groups(self, options, parser):
        for optGrp in parser.option_groups:
            for optGrpOpts in optGrp.option_list:
                opt_value = getattr(options, optGrpOpts.dest)
                if opt_value is not None:
                    self[optGrpOpts.dest] = opt_value

    def from_options(self, options, parser):
        for option in parser.option_list:
            if option.dest:
                opt_value = getattr(options, option.dest)
                if opt_value is not None:
                    self[option.dest] = opt_value

    def from_file(self, filename):
        import ConfigParser
        cp = ConfigParser.ConfigParser()
        cp.read(filename)

        # we want the items from the ISOUploader section only
        try:
            opts = ["--%s=%s" % (k,v)
                       for k,v in cp.items("ISOUploader")]
            (new_options, args) = self.parser.parse_args(args=opts, values=self.options)
            self.from_option_groups(new_options, self.parser)
            self.from_options(new_options, self.parser)
        except ConfigParser.NoSectionError:
            pass

    def from_args(self, args):
        self.command = args[0]
        if self.command not in Commands.ARY:
            raise Exception(_("%s is not a valid command.  Valid commands are '%s' or '%s'.") %
                            (self.command,
                            Commands.LIST,
                            Commands.UPLOAD))

        if self.command == Commands.UPLOAD:
            if len(args) <= 1:
                raise Exception(_("Files must be supplied for %s commands" %
                                  (Commands.UPLOAD)))
            for file in args[1:]:
                self.files.append(file)

    def prompt(self, key, msg):
        if key not in self:
            self._prompt(raw_input, key, msg)

    def getpass(self, key, msg):
        if key not in self:
            self._prompt(getpass.getpass, key, msg)

    # This doesn't ask for CTRL+C to abort because KeyboardInterrupts don't
    # seem to behave the same way every time. Take a look at the link:
    # http://stackoverflow.com/questions/4606942/why-cant-i-handle-a-keyboardinterrupt-in-python
    def _prompt(self, prompt_function, key, msg=None):
        value = get_from_prompt(msg="Please provide the %s (CTRL+D to abort): " % msg,
                prompter=prompt_function)
        if value:
            self[key] = value
        else:
            raise self.SkipException

    def ensure(self, key, default=""):
        if key not in self:
            self[key] = default

    def has_all(self, *keys):
        return all(self.get(key) for key in keys)

    def has_any(self, *keys):
        return any(self.get(key) for key in keys)

    def __ensure_path_to_file(self, file_):
        dir_ = os.path.dirname(file_)
        if not os.path.exists(dir_):
            logging.info("%s does not exists. It will be created." % dir_)
            os.makedirs(dir_, 0755)

    def __log_to_file(self, file_, level):
        try:
            self.__ensure_path_to_file(file_)
            hdlr = logging.FileHandler(filename=file_, mode='w')
            fmt = logging.Formatter(FILE_LOG_FORMAT, FILE_LOG_DSTMP)
            hdlr.setFormatter(fmt)
            logging.root.addHandler(hdlr)
            logging.root.setLevel(level)
        except Exception, e:
            logging.error("Could not configure file logging: %s" % e)

    def __log_to_stream(self, level):
        sh = logging.StreamHandler()
        fmt = logging.Formatter(STREAM_LOG_FORMAT)
        sh.setLevel(level)
        sh.setFormatter(fmt)
        logging.root.addHandler(sh)

    def __initLogger(self, logLevel=logging.INFO, quiet=None, logFile=None):
        """
        Initialize the logger based on information supplied from the
        command line or configuration file.
        """

        # If you call basicConfig more than once without removing handlers
        # it is effectively a noop. In this program it is possible to call
        # __initLogger more than once as we learn information about what
        # options the user has supplied in either the config file or
        # command line; hence, we will need to load and unload the handlers
        # to ensure consistently fomatted output.
        log = logging.getLogger()
        for h in log.handlers:
            log.removeHandler(h)

        if quiet:
            if logFile:
                # Case: Quiet and log file supplied.  Log to only file
                self.__log_to_file(logFile, logLevel)
            else:
                # If the user elected quiet mode *and* did not supply
                # a file.  We will be *mostly* quiet but not completely.
                # If there is an exception/error/critical we will print
                # to stdout/stderr.
                logging.basicConfig(level=logging.ERROR, format=STREAM_LOG_FORMAT)
        else:
            if logFile:
                # Case: Not quiet and log file supplied.  Log to both file and
                # stdout/stderr
                self.__log_to_file(logFile, logLevel)
                self.__log_to_stream(logLevel)
            else:
                # Case: Not quiet and no log file supplied.  Log to only stdout/stderr
                logging.basicConfig(level=logLevel, format=STREAM_LOG_FORMAT)

class ISOUploader(object):

    def __init__(self, conf):
        self.configuration = conf
        self.caller = Caller(self.configuration)
        if self.configuration.command == Commands.LIST:
            self.list_all_ISO_storage_domains()
        elif self.configuration.command == Commands.UPLOAD:
            self.upload_to_storage_domain()
        else:
            raise Exception(_("A valid command was not specified."))



    def _fetch_from_api(self, method):
        """
        Make a RESTful request to the supplied oVirt Engine method.
        """
        if not self.configuration:
            raise Exception("No configuration.")

        try:
            self.configuration.prompt("engine", msg=_("hostname of oVirt Engine"))
            self.configuration.prompt("user", msg=_("REST API username for oVirt Engine"))
            self.configuration.getpass("passwd", msg=_("REST API password for oVirt Engine"))
        except Configuration.SkipException:
            raise Exception("Insufficient information provided to communicate with the oVirt Engine REST API.")

        url = "https://" + self.configuration.get("engine") + "/api" + method
        req = urllib2.Request(url)
        logging.debug("URL is %s" % req.get_full_url())

        # Not using the AuthHandlers because they actually make two requests
        auth = "%s:%s" % (self.configuration.get("user"), self.configuration.get("passwd"))
        #logging.debug("HTTP auth is = %s" % auth)

        auth = base64.encodestring(auth).strip()
        req.add_header("Authorization", "Basic %s" % auth)

        fp = urllib2.urlopen(req)
        return fp.read()

    def list_all_ISO_storage_domains(self):
        """
        List only the ISO storage domains in sorted format.
        """
        def get_name(ary):
            return ary[0]

        dcXML = self._fetch_from_api("/datacenters")
        logging.debug("Returned XML is\n%s" % dcXML)
        dc = api.parseString(dcXML)
        dcAry = dc.get_data_center()
        if dcAry is not None:
             isoAry = [ ]
             for dc in dcAry:
                 dcName = dc.get_name()
                 domainXml = self._fetch_from_api("/datacenters/%s/storagedomains" %
                                                  dc.get_id())
                 logging.debug("Returned XML is\n%s" % domainXml)
                 sdom = api.parseString(domainXml)
                 domainAry = sdom.get_storage_domain()
                 if domainAry is not None:
                     for domain in domainAry:
                         if domain.get_type() == 'iso':
                             status = domain.get_status()
                             if status is not None:
                                 isoAry.append([domain.get_name(),
                                                dcName,
                                                status.get_state()])
                             else:
                                 logging.debug("the storage domain didn't have a satus element.")
                 else:
                     logging.debug(_("DC(%s) does not have a storage domain.") % dcName)

             if len(isoAry) > 0:
                isoAry.sort(key=get_name)
                fmt = "%-25s | %-25s | %s"
                print fmt % (_("ISO Storage Domain Name"), _("Datacenter"), _("ISO Domain Status"))
                print "\n".join(fmt % (name, dcName, status)
                                for name, dcName, status in isoAry)
             else:
                ExitCodes.exit_code=ExitCodes.LIST_ISO_ERR
                logging.error(_("There are no ISO storage domains."))
        else:
            ExitCodes.exit_code=ExitCodes.LIST_ISO_ERR
            logging.error(_("There are no datacenters with ISO storage domains."))

    def get_host_and_path_from_ISO_domain(self, isodomain):
        """
        Given a valid ISO storage domain, this method will return the
        hostname/IP, UUID, and path to the domain in a 3 tuple.
        Returns:
          (host, id, path)
        """
        query = urllib.quote("name=%s" % isodomain)
        domainXml = self._fetch_from_api("/storagedomains?search=%s" % query)
        logging.debug("Returned XML is\n%s" % domainXml)
        sdom = api.parseString(domainXml)
        #sdom = api.parse('isodomain.xml')
        domainAry = sdom.get_storage_domain()
        if domainAry is not None and len(domainAry) == 1:
            if domainAry[0].get_type() != 'iso':
                raise Exception(_("The %s storage domain supplied is not of type ISO" % (isodomain)))
            address = None
            path = None
            id = domainAry[0].get_id()
            storage = domainAry[0].get_storage()
            if storage is not None:
                address = storage.get_address()
                path = storage.get_path()
            else:
                raise Exception(_("A storage element was not found for the %s ISO domain." % isodomain))

            logging.debug('id=%s address=%s path=%s' % (id, address, path))
            return (id, address, path)
        else:
            raise Exception(_("An ISO storage domain with a name of %s was not found." %
                              isodomain))


    def format_ssh_user(self, ssh_user):
        if ssh_user and not ssh_user.endswith("@"):
            return "%s@" % ssh_user
        else:
            return ssh_user or ""

    def format_ssh_command(self, cmd=SSH):
        cmd = "%s " % cmd
        port_flag = "-p" if cmd.startswith(SSH) else "-P"
        if "ssh_port" in self.configuration:
            cmd += port_flag + " %(ssh_port)s " % self.configuration
        if "key_file" in self.configuration:
            cmd += "-i %s " % self.configuration
        return cmd

    def format_nfs_command(self, address, export, dir):
        cmd = '%s %s %s:%s %s' % (MOUNT, NFS_MOUNT_OPTS, address, export, dir)
        logging.debug('NFS mount command (%s)' % cmd)
        return cmd

    def exists_nfs(self, file, uid, gid):
        """
        Check for file existence.  The file will be tested as the
        UID and GID provided which is important for NFS.
        """
        try:
            os.setegid(gid)
            os.seteuid(uid)
            return os.path.exists(file)
        except Exception, e:
            raise Exception("unable to test the available space on %s" % dir)
        finally:
            os.seteuid(0)
            os.setegid(0)


    def exists_ssh(self, user, address, file):
        """
        Given a ssh user, ssh server, and full path to a file on the
        SSH server this command will test to see if it exists on the
        target file server and return true if it does.  False otherwise.
        """

        cmd = self.format_ssh_command()
        cmd += ' %s%s "%s -e %s"'  % (user, address, TEST, file)
        logging.debug(cmd)
        returncode = 1
        try:
            stdout,returncode = self.caller.call(cmd)
        except:
            pass

        if returncode == 0:
            logging.debug("exists returning true")
            return True
        else:
            logging.debug("exists returning false")
            return False

    def space_test_ssh(self, user, address,dir, file):
        """
        Function to test if the given file will fit on the given
        remote directory.  This function will return the available
        space in bytes of dir and the size of file.
        """
        dir_size = None
        returncode = 1
        cmd = self.format_ssh_command()
        cmd += """ %s%s "%s -c 'import os; dir_stat = os.statvfs(\\"%s\\"); print (dir_stat.f_bavail * dir_stat.f_frsize)'" """ % (user, address, PYTHON, dir)
        logging.debug('Mount point size test command is (%s)' % cmd)
        try:
            dir_size,returncode = self.caller.call(cmd)
        except Exception,e:
            pass

        if returncode == 0 and dir_size is not None:
            # This simply means that the SSH command was successful.
            dir_size = dir_size.strip()
            file_size = os.path.getsize(file)
            logging.debug("Size of %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB" %
                          (file, file_size, file_size/1024, (file_size/1024)/1024))
            logging.debug("Available space in %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB"  %
                          (dir, dir_size, float(dir_size)/1024, (float(dir_size)/1024)/1024))
            return (dir_size, file_size)
        else:
            raise Exception("unable to test the available space on %s" % dir)

    def space_test_nfs(self, dir, file, uid, gid):
        """
        Checks to see if there is enough space in dir for file.
        This function will return the available
        space in bytes of dir and the size of file.
        """
        try:
            os.setegid(gid)
            os.seteuid(uid)
            dir_stat = os.statvfs(dir)
        except Exception, e:
            raise Exception("unable to test the available space on %s" % dir)
        finally:
            os.seteuid(0)
            os.setegid(0)

        dir_size = (dir_stat.f_bavail * dir_stat.f_frsize)
        file_size = os.path.getsize(file)
        logging.debug("Size of %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB" %
                      (file, file_size, file_size/1024, (file_size/1024)/1024))
        logging.debug("Available space in %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB"  %
                      (dir, dir_size, dir_size/1024, (dir_size/1024)/1024))
        return (dir_size, file_size)

    def copy_file(self, src_file_name,dest_file_name, uid, gid):
        """
        Copy a file from source to dest via file handles.  The destination
        file will be opened and written to as the UID and GID provided.
        This odd copy operation is important when copying files over NFS.
        Read the NFS spec if you want to figure out *why* you need to do this.
        Returns: True if successful and false otherwise.
        """
        retVal = True
        logging.debug("euid(%s) egid(%s)" % (os.geteuid(), os.getegid()))
        umask_save = os.umask(0137) # Set to 660
        try:
            src = open(src_file_name, 'r')
            os.setegid(gid)
            os.seteuid(uid)
            dest = open(dest_file_name, 'w')
            shutil.copyfileobj(src, dest)
        except Exception, e:
            retVal = False
            logging.error(_("Problem copying %s to %s.  Message: %s" %
                          (src_file_name, dest_file_name, e)))
        finally:
            os.umask(umask_save)
            os.seteuid(0)
            os.setegid(0)
            src.close()
            dest.close()
        return retVal

    def rename_file_nfs(self, src_file_name,dest_file_name, uid, gid):
        """
        Rename a file from source to dest as the UID and GID provided.
        This method will set the euid and egid to that which is provided
        and then perform the rename.  This is can be important on an
        NFS mount.
        """
        logging.debug("euid(%s) egid(%s)" % (os.geteuid(), os.getegid()))
        try:
            os.setegid(gid)
            os.seteuid(uid)
            os.rename(src_file_name, dest_file_name)
        except Exception, e:
            logging.error(_("Problem renaming %s to %s.  Message: %s" %
                          (src_file_name, dest_file_name, e)))
        finally:
            os.seteuid(0)
            os.setegid(0)

    def rename_file_ssh(self, user, address, src_file_name, dest_file_name):
        """
        This method will remove a file via SSH.
        """
        cmd = self.format_ssh_command()
        cmd += """ %s%s "%s %s %s" """ % (user, address, MV, src_file_name, dest_file_name)
        logging.debug('Rename file command is (%s)' % cmd)
        try:
            stdout,returncode = self.caller.call(cmd)
        except Exception,e:
            raise Exception("unable to move file from %s to %s" % (src_file_name, dest_file_name))

    def remove_file_nfs(self, file_name, uid, gid):
        """
        Remove a file as the UID and GID provided.
        This method will set the euid and egid to that which is provided
        and then perform the remove.  This is can be important on an
        NFS mount.
        """
        logging.debug("euid(%s) egid(%s)" % (os.geteuid(), os.getegid()))
        try:
            os.setegid(gid)
            os.seteuid(uid)
            os.remove(file_name)
        except Exception, e:
            logging.error(_("Problem removing %s.  Message: %s" %
                          (file_name, e)))
        finally:
            os.seteuid(0)
            os.setegid(0)

    def remove_file_ssh(self,user, address, file):
        """
        This method will remove a file via SSH.
        """

        cmd = self.format_ssh_command()
        cmd += """ %s%s "%s %s" """ % (user, address, RM, file)
        logging.debug('Remove file command is (%s)' % cmd)
        try:
            stdout,returncode = self.caller.call(cmd)
        except Exception,e:
            raise Exception("unable to remove %s" % file)

    def refresh_iso_domain(self, id):
        """
        oVirt Engine scans and caches the list of files in each ISO domain.  It
        does this on a predefined interval.  Poking the /storagedomains/<id>/files
        RESTful method will cause it to refresh that list.
        """
        try:
            isoFiles = self._fetch_from_api("/storagedomains/%s/files" % id)
            logging.debug("Returned XML is\n%s" % isoFiles)
        except Exception,e:
            logging.warn(_("unable to force the oVirt Engine refresh its file list for the %s ISO storage domain" %
                           self.configuration.get('iso_domain')))

    def upload_to_storage_domain(self):
        """
        Method to upload a designated file to an ISO storage domain.
        """
        remote_path = ''
        id = None
        # Did the user give us enough info to do our work?
        if self.configuration.get('iso_domain') and self.configuration.get('nfs_server'):
            raise Exception(_("iso-domain and nfs-server are mutually exclusive options"))
        if self.configuration.get('ssh_user') and self.configuration.get('nfs_server'):
            raise Exception(_("ssh-user and nfs-server are mutually exclusive options"))
        elif self.configuration.get('iso_domain'):
            # Discover the hostname and path from the ISO domain.
            (id, address, path) = self.get_host_and_path_from_ISO_domain(self.configuration.get('iso_domain'))
            remote_path = os.path.join(id,DEFAULT_IMAGES_DIR)
        elif self.configuration.get('nfs_server'):
            mnt = self.configuration.get('nfs_server')
            (address, sep, path) = mnt.partition(':')
        else:
            raise Exception(_("either iso-domain or nfs-server must be provided"))

        # We need to create the full path to the images directory
        if conf.get('ssh_user'):
            for file in self.configuration.files:
                try:
                    logging.debug('file (%s)' % file)
                    dest_dir = os.path.join(path, remote_path)
                    dest_file = os.path.join(dest_dir,
                                             os.path.basename(file))
                    user = self.format_ssh_user(self.configuration["ssh_user"])
                    retVal = self.exists_ssh(user, address, dest_file)
                    if conf.get('force') or not retVal:
                        temp_dest_file = os.path.join(dest_dir,
                                                      '.%s' % os.path.basename(file))
                        if retVal:
                            self.remove_file_ssh(user, address, dest_file)
                        (dir_size, file_size) = self.space_test_ssh(user, address, path, file)
                        if (long(dir_size) > long(file_size)):
                            cmd = self.format_ssh_command(SCP)
                            cmd += ' %s %s%s:%s' % (file,
                                                    user,
                                                    address,
                                                    temp_dest_file)
                            logging.debug('SCP command is (%s)' % cmd)
                            self.caller.call(cmd)
                            if self.format_ssh_user(self.configuration["ssh_user"]) == 'root@':
                                cmd = self.format_ssh_command()
                                cmd += ' %s%s "%s %s:%s %s"' % (user,
                                                                address,
                                                                CHOWN,
                                                                NUMERIC_VDSM_ID,
                                                                NUMERIC_VDSM_ID,
                                                                temp_dest_file)
                                logging.debug('CHOWN command is (%s)' % cmd)
                                self.caller.call(cmd)
                            # chmod the file to 640.  Do this for every user (i.e. root and otherwise)
                            cmd = self.format_ssh_command()
                            cmd += ' %s%s "%s %s %s"' % (user,
                                                         address,
                                                         CHMOD,
                                                         PERMS_MASK,
                                                         temp_dest_file)
                            logging.debug('CHMOD command is (%s)' % cmd)
                            self.caller.call(cmd)
                            self.rename_file_ssh(user, address, temp_dest_file, dest_file)
                            # Force oVirt Engine to refresh the list of files in the ISO domain
                            self.refresh_iso_domain(id)
                        else:
                            logging.error(_('There is not enough space in %s (%s bytes) for %s (%s bytes)' %
                                            (path, dir_size, file, file_size)))
                    else:
                        ExitCodes.exit_code=ExitCodes.UPLOAD_ERR
                        logging.error(_('%s exists on %s.  Either remove it or supply the --force option to overwrite it.')
                                      % (file,address))
                except Exception, e:
                        ExitCodes.exit_code=ExitCodes.UPLOAD_ERR
                        logging.error(_('Unable to copy %s to ISO storage domain on %s.'
                                        % (file,
                                           self.configuration.get('iso_domain'))))
                        logging.error(_('Error message is "%s"') % str(e).strip())
        else:
            # NFS support.
            tmpDir = tempfile.mkdtemp()
            logging.debug('local NFS mount point is %s' % tmpDir)
            cmd = self.format_nfs_command(address, path, tmpDir)
            try:
                self.caller.call(cmd)
                passwd = getpwnam(NFS_USER)
                for file in self.configuration.files:
                    dest_dir = os.path.join(tmpDir,remote_path)
                    dest_file = os.path.join(dest_dir, os.path.basename(file))
                    retVal = self.exists_nfs(dest_file,NUMERIC_VDSM_ID, NUMERIC_VDSM_ID)
                    if conf.get('force') or not retVal:
                        try:
                            # Remove the file if it exists before checking space.
                            if retVal:
                                self.remove_file_nfs(dest_file, NUMERIC_VDSM_ID, NUMERIC_VDSM_ID)
                            (dir_size, file_size) = self.space_test_nfs(dest_dir,file, NUMERIC_VDSM_ID, NUMERIC_VDSM_ID)
                            if (dir_size > file_size):
                                temp_dest_file = os.path.join(dest_dir, '.%s' % os.path.basename(file))
                                if self.copy_file(file,
                                                  temp_dest_file,
                                                  NUMERIC_VDSM_ID,
                                                  NUMERIC_VDSM_ID):
                                    self.rename_file_nfs(temp_dest_file,
                                                     dest_file,
                                                     NUMERIC_VDSM_ID,
                                                     NUMERIC_VDSM_ID)
                                    # Force oVirt Engine to refresh the list of files in the ISO domain
                                    if id is not None:
                                        self.refresh_iso_domain(id)
                            else:
                                logging.error(_('There is not enough space in %s (%s bytes) for %s (%s bytes)' %
                                              (path, dir_size, file, file_size)))
                        except Exception, e:
                            ExitCodes.exit_code=ExitCodes.UPLOAD_ERR
                            logging.error(_('Unable to copy %s to ISO storage domain on %s.'
                                            % (file,
                                               self.configuration.get('iso_domain') if
                                                self.configuration.get('iso_domain') is not None else
                                                 self.configuration.get('nfs_server'))))
                            logging.error(_('Error message is "%s"') % str(e).strip())
                    else:
                        ExitCodes.exit_code=ExitCodes.UPLOAD_ERR
                        logging.error(_('%s exists on %s.  Either remove it or supply the --force option to overwrite it.')
                                      % (file,address))

            except KeyError, k:
                ExitCodes.exit_code=ExitCodes.CRITICAL
                logging.error(_("A user named %s with a UID and GID of %d must be defined on the system to mount the ISO storage domain on %s as Read/Write"
                                % (NFS_USER,
                                   NUMERIC_VDSM_ID,
                                   self.configuration.get('iso_domain'))))
            except Exception, e:
                ExitCodes.exit_code=ExitCodes.CRITICAL
                logging.error(e)
            finally:
                try:
                    cmd = '%s %s %s' % (UMOUNT, NFS_UMOUNT_OPTS, tmpDir)
                    logging.debug(cmd)
                    self.caller.call(cmd)
                    shutil.rmtree(tmpDir)
                except Exception, e:
                    ExitCodes.exit_code=ExitCodes.CLEANUP_ERR
                    logging.debug(e)

if __name__ == '__main__':

    # i18n setup
    gettext.bindtextdomain(APP_NAME)
    gettext.textdomain(APP_NAME)
    _ = gettext.gettext

    usage_string = "\n".join(("%prog [options] list ",
                              "       %prog [options] upload [file].[file]...[file]"))

    desc = _("""The ISO uploader can be used to list ISO storage domains and upload files to
storage domains.  The upload operation supports multiple files (separated by spaces) and
wildcarding.""")

    epilog_string = """\nReturn values:
    0: The program ran to completion with no errors.
    1: The program encountered a critical failure and stopped.
    2: The program did not discover any ISO domains.
    3: The program encountered a problem uploading to an ISO domain.
    4: The program encountered a problem un-mounting and removing the temporary directory.
"""
    OptionParser.format_epilog = lambda self, formatter: self.epilog

    parser = OptionParser(usage_string,
                          version=_("Version ") + VERSION,
                          description=desc,
                          epilog=epilog_string)

    parser.add_option("",
                      "--quiet",
                      dest="quiet",
                      action="store_true",
                      help="intended to be used with \"upload\" operations to reduce console output. (default=False)",
                      default=False)

    parser.add_option("", "--log-file",
                      dest="log_file",
                      help=_("path to log file (default=%s)" % DEFAULT_LOG_FILE),
                      metavar=_("PATH"),
                      default=DEFAULT_LOG_FILE)

    parser.add_option("", "--conf-file",
                      dest="conf_file",
                      help=_("path to configuration file (default=%s)" % DEFAULT_CONFIGURATION_FILE),
                      metavar=_("PATH"))

    parser.add_option("-v", "--verbose", dest="verbose",
            action="store_true", default=False)

    parser.add_option("-f",
                      "--force",
                      dest="force",
                      help=_("replace like named files on the target file server (default=off)"),
                      action="store_true",
                      default=False)

    engine_group = OptionGroup(parser,
                              _("oVirt Engine Configuration"),
_("""The options in the oVirt Engine group are used by the tool to gain authorization to the oVirt Engine REST API. The options in this group are available for both list and upload commands."""))

    engine_group.add_option("-u", "--user", dest="user",
                           help=_("username to use with the oVirt Engine REST API.  This should be in UPN format."),
                           metavar=_("user@engine.example.com"))

    engine_group.add_option("-p",
                           "--passwd",
                           dest="passwd",
                           help=SUPPRESS_HELP)

    engine_group.add_option("-r", "--engine", dest="engine", metavar="engine.example.com",
            help=_("""hostname or IP address of the oVirt Engine (default=localhost:8443)."""),
            default="localhost:8443")

    iso_group = OptionGroup(parser,
                              _("ISO Storage Domain Configuration"),
_("""The options in the upload configuration group should be provided to specify the ISO storage domain to
which files should be uploaded."""))

    iso_group.add_option("-i", "--iso-domain", dest="iso_domain",
            help=_("the ISO domain to which the file(s) should be uploaded"),
            metavar=_("ISODOMAIN"))

    iso_group.add_option("-n", "--nfs-server", dest="nfs_server",
            help=_("""the NFS server to which the file(s) should be uploaded.
This option is an alternative to iso-domain and should not be combined with
iso-domain.  Use this when you want to upload files to a specific
NFS server (e.g.--nfs-server=example.com:/path/to/some/dir)"""),
            metavar=_("NFSSERVER"))

    ssh_group = OptionGroup(parser,
                              _("Connection Configuration"),
_("""By default the program uses NFS to copy files to the ISO storage domain.
To use SSH file transfer, instead of NFS, provide a ssh-user."""))

    ssh_group.add_option("", "--ssh-user",
                         dest="ssh_user",
                         help=_("""the SSH user that the program will use
for SSH file transfers.  This user must either be root or a user with a
UID and GID of 36 on the target file server."""),
                         metavar="root")

    ssh_group.add_option("", "--ssh-port", dest="ssh_port",
            help=_("the SSH port to connect on"), metavar="PORT",
            default=22)

    ssh_group.add_option("-k", "--key-file", dest="key_file",
            help=_("""the identity file (private key) to be used for accessing the file server.
If a identity file is not supplied the program will prompt for a password.  It is strongly recommended
to use key based authentication with SSH because the program may make multiple SSH connections
resulting in multiple requests for the SSH password."""),
            metavar="KEYFILE")

    parser.add_option_group(engine_group)
    parser.add_option_group(iso_group)
    parser.add_option_group(ssh_group)

    try:
        # Define configuration so that we don't get a NameError when there is an exception in Configuration
        conf = None
        conf = Configuration(parser)

        isoup = ISOUploader(conf)
    except  KeyboardInterrupt, k:
        print _("Exiting on user cancel.")
    except Exception, e:
        logging.error("%s" % e)
        logging.info(_("Use the -h option to see usage."))
        parser.print_help()
        if conf and (conf.get("verbose")):
            logging.debug(_("Configuration:"))
            logging.debug(_("command: %s") % conf.command)
            #multilog(logging.debug, pprint.pformat(conf))
            multilog(logging.debug, traceback.format_exc())
        sys.exit(ExitCodes.CRITICAL)

    sys.exit(ExitCodes.exit_code)
