#!/usr/bin/python

import sys
import os
import glob
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
import fnmatch
import uuid
import re
from pwd import getpwnam
import getpass
import tarfile
from lxml import etree
from schemas import api
from ovf import ovfenvelope
from ovf.ovfenvelope import *



APP_NAME = "engine-image-uploader"
STREAM_LOG_FORMAT = '%(levelname)s: %(message)s'
FILE_LOG_FORMAT = '%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
FILE_LOG_DSTMP = '%Y-%m-%d %H:%M:%S'
NFS_MOUNT_OPTS = '-t nfs -o rw,sync,soft'
NFS_UMOUNT_OPTS = '-t nfs -f '
NFS_USER = 'vdsm'
NUMERIC_VDSM_ID = 36
MOUNT='/bin/mount'
UMOUNT='/bin/umount'
DEFAULT_CONFIGURATION_FILE='/etc/engine/imageuploader.conf'
DEFAULT_LOG_FILE='/var/log/engine/engine-image-uploader.log'

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
    LIST_IMAGE_ERR=2
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

        # we want the items from the ImageUploader section only
        try:
            opts = ["--%s=%s" % (k,v)
                       for k,v in cp.items("ImageUploader")]
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

class ImageUploader(object):

    def __init__(self, conf):
        self.configuration = conf
        self.caller = Caller(self.configuration)
        if self.configuration.command == Commands.LIST:
            self.list_all_export_storage_domains()
        elif self.configuration.command == Commands.UPLOAD:
            self.upload_to_storage_domain()
        else:
            raise Exception(_("A valid command was not specified."))



    def _fetch_from_api(self, method):
        """
        Make a RESTful request to the supplied oVirt method.
        """
        if not self.configuration:
            raise Exception("No configuration.")

        try:
            self.configuration.prompt("engine", msg=_("hostname of oVirt Engine"))
            self.configuration.prompt("user", msg=_("REST API username for oVirt Engine"))
            self.configuration.getpass("passwd", msg=_("REST API password for the %s oVirt Engine user") % self.configuration.get("user"))
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

    def list_all_export_storage_domains(self):
        """
        List only the Export storage domains in sorted format.
        """
        def get_name(ary):
            return ary[0]

        dcXML = self._fetch_from_api("/datacenters")
        logging.debug("Returned XML is\n%s" % dcXML)
        dc = api.parseString(dcXML)
        dcAry = dc.get_data_center()
        if dcAry is not None:
             imageAry = [ ]
             for dc in dcAry:
                 dcName = dc.get_name()
                 domainXml = self._fetch_from_api("/datacenters/%s/storagedomains" %
                                                  dc.get_id())
                 logging.debug("Returned XML is\n%s" % domainXml)
                 sdom = api.parseString(domainXml)
                 domainAry = sdom.get_storage_domain()
                 if domainAry is not None:
                     for domain in domainAry:
                         if domain.get_type() == 'export':
                             status = domain.get_status()
                             if status is not None:
                                 imageAry.append([domain.get_name(),
                                                dcName,
                                                status.get_state()])
                             else:
                                 logging.debug("the storage domain didn't have a status element.")
                 else:
                     logging.debug(_("DC(%s) does not have a storage domain.") % dcName)

             if len(imageAry) > 0:
                imageAry.sort(key=get_name)
                fmt = "%-30s | %-25s | %s"
                print fmt % (_("Export Storage Domain Name"), _("Datacenter"), _("Export Domain Status"))
                print "\n".join(fmt % (name, dcName, status)
                                for name, dcName, status in imageAry)
             else:
                ExitCodes.exit_code=ExitCodes.LIST_IMAGE_ERR
                logging.error(_("There are no export storage domains."))
        else:
            ExitCodes.exit_code=ExitCodes.LIST_IMAGE_ERR
            logging.error(_("There are no datacenters with Export storage domains."))

    def get_host_and_path_from_export_domain(self, exportdomain):
        """
        Given a valid export storage domain, this method will return the
        hostname/IP, UUID, and path to the domain in a 3 tuple.
        Returns:
          (host, id, path)
        """
        query = urllib.quote("name=%s" % exportdomain)
        domainXml = self._fetch_from_api("/storagedomains?search=%s" % query)
        logging.debug("Returned XML is\n%s" % domainXml)
        sdom = api.parseString(domainXml)
        #sdom = api.parse('exportdomain.xml')
        domainAry = sdom.get_storage_domain()
        if domainAry is not None and len(domainAry) == 1:
            if domainAry[0].get_type() != 'export':
                raise Exception(_("The %s storage domain supplied is not of type 'export'" % (exportdomain)))
            address = None
            path = None
            id = domainAry[0].get_id()
            storage = domainAry[0].get_storage()
            if storage is not None:
                address = storage.get_address()
                path = storage.get_path()
            else:
                raise Exception(_("A storage element was not found for the %s export domain." % exportdomain))

            logging.debug('id=%s address=%s path=%s' % (id, address, path))
            return (id, address, path)
        else:
            raise Exception(_("An export storage domain with a name of %s was not found." %
                              exportdomain))

    def unpack_ovf(self, ovf_file, dest_dir):
        '''Given a path to an OVF .tgz this function will unpack it into dest_dir. '''
        retVal = True
        try:
            tar = tarfile.open(ovf_file, "r:gz")
            tar.extractall(dest_dir)
        except Exception, e:
            retVal = False
            logging.error(_("Problem unpacking %s.  Message %s"
                    % (ovf_file,
                       str(e).strip())))
        finally:
            tar.close()
        return retVal

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

    def space_test_ovf(self, ovf_file, dest_dir):
        '''Checks to see if there is enough room to decompress the tgz into dest_dir'''
        tar = tarfile.open(ovf_file, "r:gz")
        size_in_bytes = 0
        try:
            for tarinfo in tar:
                if tarinfo.isreg():
                    size_in_bytes += tarinfo.size
        except:
            logging.error(_("Unable to calculate the decompressed size of %s.") % ovf_file)
            return False
        finally:
            tar.close()

        dest_dir_stat = os.statvfs(dest_dir)
        dest_dir_size = (dest_dir_stat.f_bavail * dest_dir_stat.f_frsize)
        logging.debug("Size of %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB" %
                      (ovf_file, size_in_bytes, size_in_bytes/1024, (size_in_bytes/1024)/1024))
        logging.debug("Available space in %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB"  %
                      (dest_dir, dest_dir_size, dest_dir_size/1024, (dest_dir_size/1024)/1024))

        if  dest_dir_size >  size_in_bytes:
            return (True,size_in_bytes)
        else:
            return (False,size_in_bytes)

    def space_test_nfs(self, remote_dir, desired_size, uid, gid):
        """
        Checks to see if there is enough space in remote_dir for desired_size.
        """
        try:
            os.setegid(gid)
            os.seteuid(uid)
            dir_stat = os.statvfs(remote_dir)
        except Exception, e:
            raise Exception("unable to test the available space on %s" % remote_dir)
        finally:
            os.seteuid(0)
            os.setegid(0)

        dir_size = (dir_stat.f_bavail * dir_stat.f_frsize)
        logging.debug("Desired size:\t%s bytes\t%.1f 1K-blocks\t%.1f MB" %
                      (desired_size, desired_size/1024, (desired_size/1024)/1024))
        logging.debug("Available space in %s:\t%s bytes\t%.1f 1K-blocks\t%.1f MB"  %
                      (remote_dir, dir_size, dir_size/1024, (dir_size/1024)/1024))

        if dir_size > desired_size:
            return (True,dir_size)
        else:
            return (False,dir_size)

    def copy_file_nfs(self, src_file_name,dest_file_name, uid, gid):
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

    def make_dir_nfs(self, dest_dir, uid, gid, mode):
        """
        Make a directory via NFS
        """
        retVal = True
        logging.debug("euid(%s) egid(%s)" % (os.geteuid(), os.getegid()))
        try:
            os.setegid(gid)
            os.seteuid(uid)
            os.makedirs(dest_dir, mode)
        except Exception, e:
            retVal = False
            logging.error(_("Problem making %s.  Message: %s" %
                          (dest_dir, e)))
        finally:
            os.seteuid(0)
            os.setegid(0)
        return retVal

    def find_file(self, source_dir, file_name):
        for root, dirs, files in os.walk(source_dir, topdown=True):
            for name in fnmatch.filter(files, file_name):
                logging.debug("File is %s" % os.path.join(root,name))
                rel_dir = root.split(source_dir).pop()
                return os.path.join(rel_dir.lstrip('/'),name)
        return None

    def update_ovf_id(self, ovf_file, source_dir, tree):
        '''
        This function will rename the OVF XML file in the archive and
        rename the associated ID in the OVF XML.
        Returns:
            true if successful false otherwise
        '''
        retVal = True
        try:
            ovf_uuid = str(uuid.uuid4())
            logging.debug("new ovf file UUID (%s)" % ovf_uuid)

            iterator = tree.findall('Content/TemplateId')
            elem_ary = list(iterator)
            if len(elem_ary) != 1:
                logging.error("There should only be one TemplateId element in the OVF XML's Content section")
                return False
            else:
                logging.debug("tag(%s) text(%s) attr(%s)" % (elem_ary[0].tag, elem_ary[0].text,elem_ary[0].attrib))
                if (elem_ary[0].text != '00000000-0000-0000-0000-000000000000'):
                    elem_ary[0].text = ovf_uuid

            # find the ID in the file and change it
            iterator = tree.findall('Content/Section')
            for sec in iterator:
                id_attr = None
                os_attr = None
                logging.debug("tag(%s) text(%s) attr(%s) class(%s)" % (sec.tag, sec.text,sec.attrib, sec))
                for attr in sec.attrib:
                    if str(sec.attrib[attr]).endswith('OperatingSystemSection_Type'):
                        os_attr = attr
                    if str(attr).endswith('id'):
                        id_attr = attr
                if id_attr and os_attr:
                    logging.debug("Setting ovf:id in OperatingSystemSection_Type to %s" % (ovf_uuid))
                    sec.attrib[id_attr] = ovf_uuid
                    if not self.write_ovf_file(ovf_file, tree):
                        return False
                    break

            # Time to rename the file.
            new_name = os.path.join(os.path.dirname(ovf_file),
                                    '%s%s' % (ovf_uuid, '.ovf'))
            os.rename(ovf_file, new_name)
            # Rename the directory as required
            ovf_dir = os.path.dirname(ovf_file)
            if os.path.samefile(source_dir, ovf_dir):
                logging.error('OVF XML file does not exist in a named subdirectory in the archive')
                retVal = False
            else:
                logging.debug("Old dirname (%s)" % os.path.dirname(ovf_file))
                new_dir = os.path.join(os.path.dirname(ovf_dir),ovf_uuid)
                logging.debug("New dir (%s) " %  new_dir)
                os.rename(ovf_dir, new_dir)
        except Exception, e:
            logging.error("Unable to rename the OVF XML file. Message: %s" % e)
            retVal = False

        return retVal

    def update_meta_file(self, source_dir, old_image_id, new_image_id, image_group_id):
        ''' Update the IMAGE attribute in the meta file with
        the the given disk group ID and rename the META file
        with the new disk ID
        '''
        meta_file_name = "%s.meta" % old_image_id
        meta_file = self.find_file(source_dir, meta_file_name)
        if not meta_file:
            logging.error('The meta file %s was not found in the archive.' % meta_file_name)
            return False
        meta_file = os.path.join(source_dir,meta_file)
        logging.debug('Meta file is %s' % meta_file)
        try:
            fp = open(meta_file, "r")
            text = fp.read()
            fp.close()
            text = re.sub(r'IMAGE=.*', "IMAGE=%s" % image_group_id, text)
            logging.debug('Writing meta file\n%s' % text)
            fp = open(meta_file, "w")
            fp.write(text)
            fp.close()
        except Exception, e:
             logging.error("Unable rewrite metafile. Message: %s" % e)
             return False

        old_image_dir = os.path.dirname(meta_file)
        new_meta_file = os.path.join(old_image_dir, '%s.meta' % new_image_id)
        logging.debug('old meta file(%s) new meta file(%s)' % (meta_file, new_meta_file))
        os.rename(meta_file, new_meta_file)

        return True

    def update_meta_file_puuid(self, source_dir, image_id_dict):
        '''
        This method will open all of the .meta files in and edit the PUUID with the
        correct replacement for images with snapshots.
        '''
        def recursive_find(directory, pattern):
            for root, dirs, files in os.walk(directory):
                for basename in files:
                    if fnmatch.fnmatch(basename, pattern):
                        filename = os.path.join(root, basename)
                        yield filename


        for meta_file in recursive_find(source_dir, '*.meta'):
            logging.debug("Meta file is %s" % meta_file)
            try:
                fp = open(meta_file, "r")
                text = fp.read()
                fp.close()
                ary = re.findall(r'PUUID=(.*)', text)
                logging.debug("PUUID(%s)" % ary)
                if ary is not None and len(ary) == 1:
                    logging.debug("Image dictionary %s" % image_id_dict)
                    if image_id_dict.has_key(ary[0]):
                        logging.debug("Substituting old PUUID(%s) with new PUUID(%s)" % (ary[0],image_id_dict[ary[0]]))
                        text = re.sub(r'PUUID=.*', "PUUID=%s" % image_id_dict[ary[0]], text)
                        logging.debug('Writing meta file\n%s' % text)
                        fp = open(meta_file, "w")
                        fp.write(text)
                        fp.close()
            except Exception, e:
                 logging.error("Unable rewrite metafile. Message: %s" % e)
                 return False
        return True

    def update_xml_item_puuid(self, ovf_file, tree, source_dir, image_id_dict):
        '''
        This method will update the Content/Section/Item/Parent element
        UUIDs with correct replacement for images with snapshots.
        '''
        try:
            iterator = tree.findall('Content/Section')
            for sec in iterator:
                for attr in sec.attrib:
                    if str(sec.attrib[attr]).endswith('VirtualHardwareSection_Type'):
                        logging.debug("tag(%s) text(%s) attr(%s) class(%s)" % (sec.tag, sec.text,sec.attrib, sec))
                        itemElement = sec.findall('Item')
                        for item in itemElement:
                            logging.debug("item tag(%s) item text(%s) item attr(%s) class(%s)" % (item.tag, item.text,item.attrib, item))
                            instance_id_tag = None
                            host_resource_tag = None
                            resource_type = None
                            parent_tag = None
                            for elem in item:
                                # Iterate through the child elements of an info and ensure
                                # that it has all of the requisite elements that describe
                                # a disk.
                                if str(elem.tag).endswith('ResourceType') and elem.text == '17':
                                    resource_type = elem.text
                                elif str(elem.tag).endswith('HostResource'):
                                    host_resource_tag = elem.tag
                                elif str(elem.tag).endswith('InstanceId'):
                                    instance_id_tag = elem.tag
                                elif str(elem.tag).endswith('Parent'):
                                    parent_tag = elem.tag
                            if instance_id_tag and host_resource_tag and resource_type:
                                # Update the PUUID from old to new.
                                tmp = item.find(parent_tag)
                                if image_id_dict.has_key(tmp.text):
                                    logging.debug("old puuid id(%s) new puuid (%s)" % (tmp.text, image_id_dict[tmp.text]))
                                    tmp.text = image_id_dict[tmp.text]
            return self.write_ovf_file(ovf_file, tree)
        except Exception, e:
                   logging.error("Content/Section/Item/Parent element. Message: %s" % e)
                   return False

    def update_xml_disk_parentref(self, ovf_file, tree, source_dir, parent_combined_id):
        '''
        The Section/Disk/parentRef elements must be updated with the newly generated disk
        groupID/puuid combination.
        '''
        try:
            iterator = tree.findall('Section')
            for sec in iterator:
                for attr in sec.attrib:
                    if str(sec.attrib[attr]).endswith('DiskSection_Type'):
                        for elem in sec:
                            logging.debug("tag(%s) text(%s) attr(%s) class(%s)" %
                                          (elem.tag, elem.text,elem.attrib, elem))
                            for attr in elem.attrib:
                                if str(attr).endswith('parentRef') and str(elem.attrib[attr]).strip() != '':
                                    logging.debug("old parentRef(%s) new parentRef(%s)" %(elem.attrib[attr], parent_combined_id))
                                    elem.attrib[attr] = parent_combined_id
            return self.write_ovf_file(ovf_file, tree)
        except Exception, e:
                   logging.error("Section/Disk/parentRef element. Message: %s" % e)
                   return False

    def update_disk_id(self, ovf_file, source_dir, tree):
        '''
        Search the Content element in the OVF XML and look for disks.
        Then update all references to the disk throughout the XML with
        freshly generated UUIDs.
        '''
        retVal = True
        image_id_dict = {}
        image_group_id_dict = {}
        parent_combined_id = None
        try:
            iterator = tree.findall('Content/Section')
            for sec in iterator:
                for attr in sec.attrib:
                    if str(sec.attrib[attr]).endswith('VirtualHardwareSection_Type'):
                        logging.debug("tag(%s) text(%s) attr(%s) class(%s)" % (sec.tag, sec.text,sec.attrib, sec))
                        itemElement = sec.findall('Item')
                        for item in itemElement:
                            logging.debug("item tag(%s) item text(%s) item attr(%s) class(%s)" % (item.tag, item.text,item.attrib, item))
                            instance_id_tag = None
                            host_resource_tag = None
                            resource_type = None
                            for elem in item:
                                # Iterate through the child elements of an info and ensure
                                # that it has all of the requisite elements that describe
                                # a disk.
                                if str(elem.tag).endswith('ResourceType') and elem.text == '17':
                                    resource_type = elem.text
                                elif str(elem.tag).endswith('HostResource'):
                                    host_resource_tag = elem.tag
                                elif str(elem.tag).endswith('InstanceId'):
                                    instance_id_tag = elem.tag

                            if instance_id_tag and host_resource_tag and resource_type:
                                # At this point we know that the Content element has a
                                # "disk" Item.  We need go generate new UUIDs for it and
                                # reset them everywhere else they're located in the doc. Ugh.
                                old_image_id = None
                                old_combined_group_id = None
                                new_image_id = str(uuid.uuid4())
                                new_image_group_id = str(uuid.uuid4())
                                combined_ids = "%s/%s" % (new_image_group_id, new_image_id)
                                logging.debug("New image id(%s) new image group id(%s)" % (new_image_id, combined_ids))

                                tmp = item.find(instance_id_tag)
                                old_image_id = tmp.text
                                tmp.text = new_image_id
                                tmp = item.find(host_resource_tag)
                                old_combined_group_id = tmp.text
                                # Safety
                                if old_image_id is None or old_combined_group_id is None:
                                    logging.error("The Content/Section:VirtualHardwareSection_Type element contains a null InstanceId or HostResource")
                                    return False

                                old_image_group_id = os.path.dirname(old_combined_group_id)
                                logging.debug("old group id (%s) proposed new group id (%s)" % (old_image_group_id,new_image_group_id))
                                if image_group_id_dict.has_key(old_image_group_id):
                                    logging.debug("the old image group id (%s) has already been given a new image group id(%s)" %
                                                  (old_image_group_id, image_group_id_dict[old_image_group_id]))
                                    new_image_group_id = image_group_id_dict[old_image_group_id]
                                    combined_ids = "%s/%s" % (new_image_group_id, new_image_id)
                                else:
                                    # Save the new image group ID in a dict so that we can check other disks to see if
                                    # they're members of this *old* image group.
                                    image_group_id_dict[old_image_group_id] = new_image_group_id
                                # Set the image group
                                tmp.text = combined_ids
                                logging.debug("old image id(%s) old image group id(%s)" % (old_image_id, old_combined_group_id))

                                # We need to store a mapping of the old image ID to new image ID so that
                                # we can update the .meta file.
                                image_id_dict[old_image_id] = new_image_id




                                # Update the References section to point to the new disk UUID
                                ref_iterator = tree.findall('References')
                                for reference in ref_iterator:
                                    logging.debug("References tag(%s) References text(%s) References attr(%s) class(%s)" %
                                                  (reference.tag, reference.text,reference.attrib, reference))
                                    for file in reference:
                                        id_attr = None
                                        href_attr = None
                                        logging.debug("File tag(%s) File text(%s) File attr(%s) class(%s)" %
                                                      (file.tag, file.text,file.attrib, file))
                                        for attr in file.attrib:
                                            if str(attr).endswith('id') and file.attrib[attr] == old_image_id:
                                                id_attr = attr
                                            elif str(attr).endswith('href'):
                                                href_attr = attr
                                        if id_attr and href_attr:
                                            logging.debug("Setting %s and %s to %s and %s" %
                                                          (id_attr,href_attr, new_image_id, combined_ids))
                                            file.attrib[id_attr] = new_image_id
                                            file.attrib[href_attr] = combined_ids

                                # Update the Section xsi:type="ovf:DiskSection_Type"
                                iterator = tree.findall('Section')
                                for sec in iterator:
                                    for attr in sec.attrib:
                                        if str(sec.attrib[attr]).endswith('DiskSection_Type'):
                                            for elem in sec:
                                                disk_id = None
                                                file_ref = None
                                                parent_ref = None
                                                logging.debug("tag(%s) text(%s) attr(%s) class(%s)" %
                                                              (elem.tag, elem.text,elem.attrib, elem))
                                                for attr in elem.attrib:
                                                    if str(attr).endswith('diskId') and elem.attrib[attr] == old_image_id:
                                                        disk_id = attr
                                                    if str(attr).endswith('fileRef'):
                                                        file_ref = attr
                                                    if str(attr).endswith('parentRef') and str(elem.attrib[attr]).strip() != '':
                                                        parent_ref = attr

                                                # Update the disk ID and fileRef if we found a match
                                                logging.debug("tag(%s) text(%s) attr(%s) class(%s)" %
                                                              (elem.tag, elem.text,elem.attrib, elem))
                                                if disk_id and file_ref:
                                                    if parent_ref and (elem.attrib[file_ref] == elem.attrib[parent_ref]):
                                                        # It is odd that the parent_ref is the same for N-1 disks.  Should be
                                                        # tree-ish, IMHO.
                                                        logging.debug("Found the parent snap. file_ref(%s) parent_ref(%s)" %
                                                                      (elem.attrib[file_ref], elem.attrib[parent_ref]))
                                                        parent_combined_id = combined_ids
                                                    elem.attrib[disk_id] = new_image_id
                                                    elem.attrib[file_ref] = combined_ids



                                # Write the updated XML back out and update meta file
                                if self.write_ovf_file(ovf_file, tree):
                                    # Rename the image
                                    old_image_file = self.find_file(source_dir, old_image_id)
                                    old_image_file = os.path.join(source_dir,old_image_file)
                                    old_image_dir = os.path.dirname(old_image_file)
                                    logging.debug("Image file(%s) Image dir(%s)" % (old_image_file,old_image_dir))
                                    new_image_name = os.path.join(old_image_dir, new_image_id)
                                    logging.debug('old file(%s) new file(%s)' % (old_image_file, new_image_name))
                                    os.rename(old_image_file, new_image_name)

                                    # Update the meta file
                                    if not self.update_meta_file(source_dir, old_image_id, new_image_id, new_image_group_id):
                                        return False

                                    # Rename the image's dir (i.e. group ID dir)
                                    new_dir_name = os.path.join(os.path.dirname(old_image_dir), os.path.dirname(combined_ids))
                                    logging.debug('old dir(%s) new dir(%s)' % (old_image_dir, new_dir_name))
                                    os.rename(old_image_dir, new_dir_name)
                                else:
                                    return False

            # At this point we should have a mapping of old image_ids to new
            # ids.  We need to do a few things...
            # 1. Loop through all of the .meta files and update their PUUIDs
            # with a dictionary of old PUUIDs to new PUUIDs
            if not self.update_meta_file_puuid(source_dir, image_id_dict):
                return False
            # 2. Go back through the XML and update Item/Parent elements
            if not self.update_xml_item_puuid(ovf_file, tree, source_dir, image_id_dict):
                return False
            # 3. Go back trough the XMl and update Section/Disk/parentRef elements with
            # the updated one.  Again it's odd that they're not daisy chained.
            if not self.update_xml_disk_parentref(ovf_file, tree, source_dir, parent_combined_id):
                return False
        except Exception, e:
            logging.error("Unable to update the disk ID in the OVF XML. Message: %s" % e)
            retVal = False


        return retVal

    def update_ovf_name(self,ovf_file, tree):
        '''
        Update the Name element in the Content section of the
        OVF XML and write it back out to ovf_file
        '''
        retVal = True
        try:
            iterator = tree.findall('Content/Name')
            elem_ary = list(iterator)
            if len(elem_ary) != 1:
                logging.error("There should only be one Name element in the OVF XML's Content section")
                return False
            else:
                logging.debug("tag(%s) text(%s) attr(%s)" % (elem_ary[0].tag, elem_ary[0].text,elem_ary[0].attrib))
                elem_ary[0].text = self.configuration.get('new_image_name')
                if not self.write_ovf_file(ovf_file, tree):
                    retVal = False
        except Exception, e:
            logging.error("Unable to update the Name element of the Content section in the OVF XML. Message: %s" % e)
            retVal = False
        return retVal

    def remove_nics(self, ovf_file, tree):
        '''
        Remove all NICs within the OVF XML to prevent MAC address conflicts
        '''
        retVal = True
        try:
            write = False
            iterator = tree.findall('Content/Section')
            for sec in iterator:
                for attr in sec.attrib:
                    if str(sec.attrib[attr]).endswith('VirtualHardwareSection_Type'):
                        logging.debug("tag(%s) text(%s) attr(%s) class(%s)" % (sec.tag, sec.text,sec.attrib, sec))
                        itemElement = sec.findall('Item')
                        for item in itemElement:
                            logging.debug("item tag(%s) item text(%s) item attr(%s) class(%s)" % (item.tag, item.text,item.attrib, item))
                            resource_type = None
                            for elem in item:
                                logging.debug("tag(%s) value(%s)" % (elem.tag, elem.text))
                                if str(elem.tag).endswith('ResourceType') and elem.text == '10':
                                    resource_type = elem.text
                                    item.getparent().remove(item)
                                    write = True

            if write:
                 retVal = self.write_ovf_file(ovf_file, tree)
        except Exception, e:
            logging.error("Unable to update the Name element of the Content section in the OVF XML. Message: %s" % e)
            retVal = False
        return retVal

    def write_ovf_file(self, file_name, tree):
        retVal = True
        try:
            f = open(file_name, 'w')
            f.write("<?xml version='1.0' encoding='UTF-8'?>\n")
            doc = etree.tostring(tree.getroot(), pretty_print=True, encoding="UTF-8")
            f.write(doc)
            f.close()
        except Exception, e:
            logging.error("Unable to update the OVF XML file. Message: %s" % e)
            retVal = False
        return retVal

    def update_ovf_xml(self,source_dir):
        ''' Check to see if the user supplied template-name, rename_ovf, or
        instance_id and update the XML accordingly.  Will also rename files
        and directories as necessary.'''

        ovf_file = self.find_file(source_dir,'*.ovf')
        if ovf_file is None:
            logging.error("This archive does not contain an OVF XML file.")
            return False

        ovf_file = os.path.join(source_dir,ovf_file)
        try:
            tree = etree.parse(ovf_file)
        except Exception, e:
            logging.error("Unable to parse the OVF XML file. Message: %s" % e)
            return False

        if self.configuration.get('mac_address'):
            if not self.remove_nics(ovf_file, tree):
                return False

        if self.configuration.get('new_image_name'):
            if not self.update_ovf_name(ovf_file, tree):
                return False

        if self.configuration.get('instance_id'):
            if not self.update_disk_id(ovf_file, source_dir, tree):
                return False

        # Do this last as this will actually rename the XML as
        # required.
        if self.configuration.get('rename_ovf'):
            if not self.update_ovf_id(ovf_file, source_dir, tree):
                return False

        return True


    def get_files_to_copy(self, source_dir):
        '''Search the ovf unpack directory for a .ovf.  Open it and look
           for those files that need to be copied.'''
        retVal = []

        def href_finder(attr):
            if str(attr).endswith('href'):
                return attr
            else:
                return None

        ovf_file = self.find_file(source_dir,'*.ovf')
        if ovf_file is None:
            logging.error(_("This OVF archive does not have a required OVF XML file."))
            return retVal
        if str(ovf_file).startswith("master"):
            retVal.append(ovf_file)
        else:
            logging.error("The OVF XML file does not exist in the expected named directory within the archive. File (%s) " % ovf_file)
            return []

        xmlDoc = ovfenvelope.parse(os.path.join(source_dir,ovf_file))
        ref_type = xmlDoc.get_References()

        file_ary = ref_type.get_File()
        for file_type in file_ary:
            any_attrs = file_type.get_anyAttributes_()
            keys = any_attrs.keys()
            href_ary = filter(href_finder, keys)
            for href in href_ary:
                file_to_copy = any_attrs.get(href)
                logging.debug("File to copy: %s" % file_to_copy)
                retVal.append(os.path.join('images',file_to_copy))
                retVal.append(os.path.join('images','%s.meta' % file_to_copy))

        return retVal


    def copy_files_nfs(self, source_dir, remote_dir, address, ovf_size, ovf_file_name):
        ''' Copies all of the files in source_dir to remote_dir.'''
        files_to_copy = self.get_files_to_copy(source_dir)
        if len(files_to_copy) < 1:
            logging.error("The internal directory structure of the OVF file is invalid")
            return

        # Check for pre-existing files.  We can't just overwrite what is already there.
        existing_files = False
        for root, dirs, files in os.walk(source_dir, topdown=True):
            for name in files:
                for paths in files_to_copy:
                    if str(paths).endswith(name):
                        remote_file = os.path.join(remote_dir, paths)
                        if self.exists_nfs(remote_file,NUMERIC_VDSM_ID, NUMERIC_VDSM_ID):
                            if not conf.get('force'):
                                logging.error(_('%s exists on %s.  Either remove it or supply the --force option to overwrite it.')
                                              % (remote_file,address))
                                return
                            else:
                                # Remove the file.
                                self.remove_file_nfs(remote_file,NUMERIC_VDSM_ID, NUMERIC_VDSM_ID)

        # Is there enough room for what we want to copy now?
        retVal, remote_dir_size = self.space_test_nfs(remote_dir, ovf_size, NUMERIC_VDSM_ID, NUMERIC_VDSM_ID)
        if not retVal:
            logging.error(_('There is not enough space in %s (%s bytes) for the contents of %s (%s bytes)') %
                          (address, remote_dir_size, ovf_file_name, ovf_size))
            return

        # Make the remote directories
        for valid_files in files_to_copy:
            tmp_dir = os.path.join(remote_dir,os.path.dirname(valid_files))
            if not self.exists_nfs(tmp_dir,NUMERIC_VDSM_ID, NUMERIC_VDSM_ID):
                 self.make_dir_nfs(tmp_dir, NUMERIC_VDSM_ID, NUMERIC_VDSM_ID, 0770)

        # Copy the files with the .ovf being last because we don't want oVirt to find anything until
        # it is all there.
        local_ovf_file = None
        remote_ovf_file = None
        for root, dirs, files in os.walk(source_dir, topdown=True):
            for name in files:
                for paths in files_to_copy:
                    if str(paths).endswith(name):
                        remote_file = os.path.join(remote_dir, paths)
                        if name.endswith('.ovf'):
                            ovf_file = os.path.join(root,name)
                            remote_ovf_file = remote_file
                        else:
                            if not self.copy_file_nfs(os.path.join(root,name),
                                                      remote_file,
                                                      NUMERIC_VDSM_ID,
                                                      NUMERIC_VDSM_ID):
                                return

        # Copy the .ovf *last*
        self.copy_file_nfs(ovf_file, remote_ovf_file, NUMERIC_VDSM_ID,NUMERIC_VDSM_ID)

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

    def upload_to_storage_domain(self):
        """
        Method to upload a designated file to an export storage domain.
        """
        remote_path = ''
        # Did the user give us enough info to do our work?
        if self.configuration.get('export_domain') and self.configuration.get('nfs_server'):
            raise Exception(_("export-domain and nfs-server are mutually exclusive options"))
        if self.configuration.get('ssh_user') and self.configuration.get('nfs_server'):
            raise Exception(_("ssh-user and nfs-server are mutually exclusive options"))
        elif self.configuration.get('export_domain'):
            # Discover the hostname and path from the export domain.
            (id, address, path) = self.get_host_and_path_from_export_domain(self.configuration.get('export_domain'))
            remote_path = id
        elif self.configuration.get('nfs_server'):
            mnt = self.configuration.get('nfs_server')
            (address, sep, path) = mnt.partition(':')
        else:
            raise Exception(_("either export-domain or nfs-server must be provided"))

        # NFS support.
        mount_dir = tempfile.mkdtemp()
        logging.debug('local NFS mount point is %s' % mount_dir)
        cmd = self.format_nfs_command(address, path, mount_dir)
        try:
            self.caller.call(cmd)
            passwd = getpwnam(NFS_USER)
            dest_dir = os.path.join(mount_dir,remote_path)
            for ovf_file in self.configuration.files:
                try:
                    ovf_extract_dir = tempfile.mkdtemp()
                    logging.debug('local extract directory for OVF is %s' % ovf_extract_dir)
                    retVal, ovf_file_size = self.space_test_ovf(ovf_file, ovf_extract_dir)
                    if retVal:
                        if self.unpack_ovf(ovf_file, ovf_extract_dir):
                            if (self.update_ovf_xml(ovf_extract_dir)):
                                self.copy_files_nfs(ovf_extract_dir, dest_dir, address, ovf_file_size, ovf_file)
                finally:
                    try:
                        logging.debug("Cleaning up OVF extract directory %s" % ovf_extract_dir)
                        shutil.rmtree(ovf_extract_dir)
                    except Exception, e:
                        ExitCodes.exit_code=ExitCodes.CLEANUP_ERR
                        logging.debug(e)

        except KeyError, k:
            ExitCodes.exit_code=ExitCodes.CRITICAL
            logging.error(_("A user named %s with a UID and GID of %d must be defined on the system to mount the export storage domain on %s as Read/Write"
                            % (NFS_USER,
                               NUMERIC_VDSM_ID,
                               self.configuration.get('export_domain'))))
        except Exception, e:
            ExitCodes.exit_code=ExitCodes.CRITICAL
            logging.error(e)
        finally:
            try:
                cmd = '%s %s %s' % (UMOUNT, NFS_UMOUNT_OPTS, mount_dir)
                logging.debug(cmd)
                self.caller.call(cmd)
                shutil.rmtree(mount_dir)
            except Exception, e:
                ExitCodes.exit_code=ExitCodes.CLEANUP_ERR
                logging.debug(e)


if __name__ == '__main__':

    # i18n setup
    gettext.bindtextdomain(APP_NAME)
    gettext.textdomain(APP_NAME)
    _ = gettext.gettext

    usage_string = "\n".join(("%prog [options] list ",
                              "       %prog [options] upload [file]"))

    desc = _("""The image uploader can be used to list export storage domains and upload OVF files to
export storage domains. This tool only supports OVF files created by oVirt engine.  OVF archives should have the
following characteristics:
1. The OVF archive must be created with gzip compression.
2. The archive must have the following internal layout:
    |-- images
    |   |-- <Image Group UUID>
    |        |--- <Image UUID (this is the disk image)>
    |        |--- <Image UUID (this is the disk image)>.meta
    |-- master
    |   |---vms
    |       |--- <UUID>
    |             |--- <UUID>.ovf

""")

    epilog_string = """\nReturn values:
    0: The program ran to completion with no errors.
    1: The program encountered a critical failure and stopped.
    2: The program did not discover any export domains.
    3: The program encountered a problem uploading to an export domain.
    4: The program encountered a problem un-mounting and removing the temporary directory.
"""

    OptionParser.format_epilog = lambda self, formatter: self.epilog
    OptionParser.format_description = lambda self, formatter: self.description

    parser = OptionParser(usage_string,
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

    export_group = OptionGroup(parser,
                              _("Export Storage Domain Configuration"),
_("""The options in the upload configuration group should be provided to specify the export storage domain to
which OVF files should be uploaded."""))

    export_group.add_option("-e", "--export-domain", dest="export_domain",
            help=_("the export storage domain to which the file(s) should be uploaded"),
            metavar=_("EXPORT_STORAGE_DOMAIN"))

    export_group.add_option("-n", "--nfs-server", dest="nfs_server",
            help=_("""the NFS server to which the file(s) should be uploaded.
This option is an alternative to export-domain and should not be combined with
export-domain.  Use this when you want to upload files to a specific
NFS server (e.g.--nfs-server=example.com:/path/to/some/dir)"""),
            metavar=_("NFSSERVER"))

    export_group.add_option("-i", "--ovf-id", dest="rename_ovf", action="store_false", default=True,
            help=_("use this option if you do not want to update the UUID of the image.  By default, \
the tool will generate a new UUID for the image.  This ensures that there is no conflict between \
the id of the incoming image and those already in oVirt engine."))

    export_group.add_option("-d", "--disk-instance-id", dest="instance_id", action="store_false", default=True,
            help=_("use this option if do not you want to rename the instance ID for each disk (i.e. InstanceId) in the image. \
By default, this tool will generate new UUIDs for disks within the image to be imported.  This ensures that there \
are no conflicts between the disks on the imported image and those within oVirt engine."))

    export_group.add_option("-m", "--mac-address", dest="mac_address", action="store_false", default=True,
            help=_("use this option if do not you want to remove the network components from the image \
that will be imported.  By default, this tool will remove any network interface cards from the image \
to prevent conflicts with NICs on other VMs within the oVirt engine.  Once the image has been imported, simply \
use the oVirt engine to add NICs back and the oVirt engine will ensure that there are no MAC address conflicts."))

    export_group.add_option("-N", "--name", dest="new_image_name",
            help=_("supply this option if you want to rename the image"),
            metavar=_("NEW_IMAGE_NAME"))

#    ssh_group = OptionGroup(parser,
#                              _("Connection Configuration"),
#_("""By default the program uses NFS to copy files to the ISO storage domain.
#To use SSH file transfer, instead of NFS, provide a ssh-user."""))
#
#    ssh_group.add_option("", "--ssh-user",
#                         dest="ssh_user",
#                         help=_("""the SSH user that the program will use
#for SSH file transfers.  This user must either be root or a user with a
#UID and GID of 36 on the target file server."""),
#                         metavar="root")
#
#    ssh_group.add_option("", "--ssh-port", dest="ssh_port",
#            help=_("the SSH port to connect on"), metavar="PORT",
#            default=22)
#
#    ssh_group.add_option("-k", "--key-file", dest="key_file",
#            help=_("""the identity file (private key) to be used for accessing the file server.
#If a identity file is not supplied the program will prompt for a password.  It is strongly recommended
#to use key based authentication with SSH because the program may make multiple SSH connections
#resulting in multiple requests for the SSH password."""),
#            metavar="KEYFILE")

    parser.add_option_group(engine_group)
    parser.add_option_group(export_group)
#    parser.add_option_group(ssh_group)

    try:
        # Define configuration so that we don't get a NameError when there is an exception in Configuration
        conf = None
        conf = Configuration(parser)

        imageup = ImageUploader(conf)
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
