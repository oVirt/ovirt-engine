#!/usr/bin/python

import sys
import os
from optparse import OptionParser, OptionGroup, SUPPRESS_HELP
import subprocess
import shlex
import urllib
import urllib2
import base64
import pprint
import fnmatch
import traceback
import tempfile
import shutil
import logging
import getpass

from schemas import hypervisors

versionNum="1.0.0"
STREAM_LOG_FORMAT = '%(levelname)s: %(message)s'
FILE_LOG_FORMAT = '%(asctime)s::%(levelname)s::%(module)s::%(lineno)d::%(name)s:: %(message)s'
FILE_LOG_DSTMP = '%Y-%m-%d %H:%M:%S'
DEFAULT_SSH_KEY = "/etc/pki/engine/keys/engine_id_rsa"
DEFAULT_SSH_USER = 'root'
DEFAULT_CONFIGURATION_FILE = "/etc/engine/logcollector.conf"
DEFAULT_SCRATCH_DIR='/tmp/logcollector'
DEFAULT_LOG_FILE='/var/log/engine/engine-log-collector.log'
DEFAULT_TIME_SHIFT_FILE='time_diff.txt'


def multilog(logger, msg):
    for line in str(msg).splitlines():
        logger(line)

def get_from_prompt(msg, default=None, prompter=raw_input):
    try:
        value = prompter(msg)
        if value.strip():
            return value.strip()
        else:
            return default
    except EOFError:
        print
        return default


class ExitCodes():
    """
    A simple psudo-enumeration class to hold the current and future exit codes
    """
    NOERR=0
    CRITICAL=1
    WARN=2
    exit_code=NOERR

class Caller(object):
    """
    Utility class for forking programs.
    """
    def __init__(self, configuration):
        self.configuration = configuration

    def prep(self, cmd):
        _cmd = cmd % self.configuration
        return shlex.split(_cmd)

    def call(self, cmds):
        """Uses the configuration to fork a subprocess and run cmds."""
        _cmds = self.prep(cmds)
        proc = subprocess.Popen(_cmds,
                   stdout=subprocess.PIPE,
                   stderr=subprocess.PIPE)
        stdout, stderr = proc.communicate()
        returncode = proc.returncode
        logging.debug("returncode(%s)" % returncode)
        logging.debug("STDOUT(%s)" % stdout)
        logging.debug("STDERR(%s)" % stderr)

        if returncode == 0:
            return stdout
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
        self.command = "list"
        self.parser = parser
        self.options = None
        self.args = None

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
            # Need to parse again to override conf file options
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

    def from_option_groups(self,options,parser):
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

        # we want the items from the LogCollector section only
        try:
            opts = ["--%s=%s" % (k,v)
                       for k,v in cp.items("LogCollector")]
            (new_options, args) = self.parser.parse_args(args=opts, values=self.options)
            self.from_option_groups(new_options, self.parser)
            self.from_options(new_options, self.parser)
        except ConfigParser.NoSectionError:
            pass

    def from_args(self, args):
        self.command = args[0]
        if self.command not in ('list', 'collect'):
            raise Exception("%s is not a valid command." % self.command)

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
        value = get_from_prompt(msg="Please provide the %s (CTRL+D to skip): " % msg,
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
                # Case: Batch and log file supplied.  Log to only file
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


class CollectorBase(object):
        def __init__(self,
                     hostname,
                     configuration=None,
                     **kwargs):
            self.hostname = hostname
            if configuration:
                self.configuration = configuration.copy()
            else:
                self.configuration = {}
            self.prep()
            self.caller = Caller(self.configuration)

        def prep(self):
            self.configuration['ssh_cmd'] = self.format_ssh_command()
            self.configuration['scp_cmd'] = self.format_ssh_command(cmd="scp")

        def get_key_file(self):
            return self.configuration.get("key_file")

        def get_ssh_user(self):
            return "%s@" % DEFAULT_SSH_USER


        def parse_sosreport_stdout(self, stdout):
            try:
                lines = stdout.splitlines()
                lines = filter(None, lines)
                path = lines[-3].strip()
                filename = os.path.basename(path)
                md5sum = lines[-2].partition(": ")[-1]
                self.configuration["filename"] = filename
                self.configuration["checksum"] = md5sum
                self.configuration["path"] = path
            except IndexError, e:
                logging.debug("message(%s)" % e)
                logging.debug("parse_sosreport_stdout: " + traceback.format_exc())
                raise Exception("Could not parse sosreport output to determine filename")

        def format_ssh_command(self, cmd="ssh"):
            cmd = "/usr/bin/%s " % cmd

            if "ssh_port" in self.configuration:
                port_flag = "-p" if cmd.startswith("/usr/bin/ssh") else "-P"
                cmd += port_flag + " %(ssh_port)s " % self.configuration

            if self.get_key_file():
                cmd += "-i %s " % self.get_key_file()

            # ignore host key checking
            cmd += "-oStrictHostKeyChecking=no "

            cmd += self.get_ssh_user()

            return cmd + "%s" % self.hostname


class HyperVisorData(CollectorBase):

    def __init__(self,
                 hostname,
                 configuration=None,
                 semaphore=None,
                 queue=None,
                 **kwargs):
        super(HyperVisorData, self).__init__(hostname,configuration)
        self.semaphore = semaphore
        self.queue = queue

    def prep(self):
        self.configuration["hostname"] = self.hostname
        self.configuration['ssh_cmd'] = self.format_ssh_command()
        self.configuration['scp_cmd'] = self.format_ssh_command(cmd="scp")
        self.configuration['reports'] = ",".join((
            "libvirt",
            "vdsm",
            "general",
            "networking",
            "hardware",
            "process",
            "yum",
            "filesys",
            "devicemapper",
            "selinux",
            "kernel",
            ))

        # these are the reports that will work with rhev2.2 hosts
        self.configuration['bc_reports'] = "vdsm,general,networking,hardware,process,yum,filesys"

    def get_time_diff(self, stdout):
        import datetime
        h_time = datetime.datetime.strptime(
                stdout.strip(), "%a, %d %b %Y %H:%M:%S +0000")
        l_time = datetime.datetime.utcnow()

        logging.debug("host <%s> time: %s" % (self.configuration["hostname"], h_time.isoformat()))
        logging.debug("local <%s> time: %s" % ("localhost", l_time.isoformat(),))

        time_diff = "%(hostname)s " % self.configuration
        if h_time > l_time:
            self.queue.append(time_diff + "+%s" % (h_time - l_time))
        else:
            self.queue.append(time_diff + "-%s" % (l_time - h_time))

    def sosreport(self):
        cmd = """%(ssh_cmd)s "
        VERSION=`/bin/rpm -q --qf '[%%{VERSION}]' sos | /bin/sed 's/\.//'`;
        if [ "$VERSION" -ge "22" ]; then
            /usr/sbin/sosreport --batch -o %(reports)s
        elif [ "$VERSION" -ge "17" ]; then
            /usr/sbin/sosreport --no-progressbar -o %(bc_reports)s
        else
            /bin/echo "No valid version of sosreport found." 1>&2
            exit 1
        fi
        "
        """
        return self.caller.call(cmd)

    def run(self):

        try:
            logging.info("collecting information from %(hostname)s" % self.configuration)
            stdout = self.sosreport()
            self.parse_sosreport_stdout(stdout)
            self.configuration["hypervisor_dir"] = os.path.join(self.configuration.get("local_scratch_dir"),self.configuration.get("hostname"))
            os.mkdir(self.configuration["hypervisor_dir"])
            self.configuration['archive_name'] = self.configuration.get("hostname") + "-" + os.path.basename(self.configuration.get("path"))
            self.caller.call('%(scp_cmd)s:%(path)s %(hypervisor_dir)s/%(archive_name)s')
            self.caller.call('%(ssh_cmd)s "/bin/rm %(path)s*"')

            # setting up a pipeline since passing stdin to communicate doesn't seem to work
            echo_cmd = self.caller.prep('/bin/echo "%(checksum)s  %(hypervisor_dir)s/%(archive_name)s"')
            md5sum_cmd = self.caller.prep("/usr/bin/md5sum -c -")
            result = None

            p1 = subprocess.Popen(echo_cmd, stdout=subprocess.PIPE)
            p2 = subprocess.Popen(md5sum_cmd, stdin=p1.stdout, stdout=subprocess.PIPE)
            result = p2.communicate()[0]

            stdout = self.caller.call('%(ssh_cmd)s "/bin/date -uR"')
            try:
                self.get_time_diff(stdout)
            except ValueError, e:
                logging.debug("get_time_diff: " + str(e))

            if result and "OK" not in result:
                logging.error("checksum test: " + result)
                raise Exception("%(local_scratch_dir)s/%(filename)s failed checksum test!" % self.configuration)

        except Exception, e:
            ExitCodes.exit_code=ExitCodes.WARN
            logging.error("Failed to collect logs from: %s; %s" % (self.configuration.get("hostname"), e))
            multilog(logging.debug,traceback.format_exc())
            logging.debug("Configuration for %(hostname)s:" % self.configuration)
            multilog(logging.debug,pprint.pformat(self.configuration))
        finally:
            if self.semaphore:
                self.semaphore.release()

        logging.info("finished collecting information from %(hostname)s" % self.configuration)


class ENGINEData(CollectorBase):

    def build_options(self):
        opts = ["-k rpm.rpmva=off",
                "-k engine.vdsmlogs=%s" % self.configuration.get("local_scratch_dir"),
                "-k engine.prefix=on"]
        for key, value in self.configuration.iteritems():
            if key.startswith("java") or key.startswith("jboss"):
                opts.append('-k %s="%s"' % (key,value))

        if self.configuration.get("ticket_number"):
            opts.append("--ticket-number=%s" % self.configuration.get("ticket_number"))

        if self.configuration.get("upload"):
            opts.append("--upload=%s" % self.configuration.get("upload"))

        return " ".join(opts)

    def sosreport(self):
        self.configuration["reports"] = ",".join((
            "jboss",
            "engine",
            "rpm",
            "libvirt",
            "general",
            "networking",
            "hardware",
            "process",
            "yum",
            "filesys",
            "devicemapper",
            "selinux",
            "kernel",
        ))
        self.configuration["sos_options"] = self.build_options()
        stdout = self.caller.call('/usr/sbin/sosreport --batch --report --tmp-dir=%(local_tmp_dir)s  -o %(reports)s %(sos_options)s')
        self.parse_sosreport_stdout(stdout)
        return """Log files have been collected and placed in %s.
      The MD5 for this file is %s and its size is %s""" % (
      self.configuration["path"] ,
      self.configuration["checksum"],
      '%.1fM' % (float(os.path.getsize(self.configuration["path"])) / (1 << 20)))


class PostgresData(CollectorBase):

    def get_key_file(self):
        """
        Override the base get_key_file method to return the SSH key for the
        PostgreSQL system if there is one.  Returns None if there isn't one.
        """
        return self.configuration.get("pg_host_key")

    def get_ssh_user(self):
        """
        Override the base get_ssh_user method to return the SSH user for the
        PostgreSQL system if there is one.
        """
        if self.configuration.get("pg_ssh_user"):
            return "%s@" % self.configuration.get("pg_ssh_user")
        else:
            return "%s@" % DEFAULT_SSH_USER


    def sosreport(self):
        if self.configuration.get("pg_pass"):
            opt = '-k postgresql.dbname=%(pg_dbname)s -k postgresql.username=%(pg_user)s -k postgresql.password=%(pg_pass)s'
        else:
            opt = ""

        if self.hostname == "localhost":
            stdout = self.caller.call('/usr/sbin/sosreport --batch --report -o postgresql '
                        '--tmp-dir=%(local_scratch_dir)s ' + opt)
            self.parse_sosreport_stdout(stdout)
            # Prepend postgresql- to the .md5 file that is produced by SOS
            # so that it is easy to distinguish from the other N reports
            # that are all related to hypervisors.  Note, that we
            # only do this in the case of a local PostgreSQL DB because
            # when the DB is remote the .md5 file is not copied.
            os.rename("%s.md5" % (self.configuration["path"]),
                      os.path.join(self.configuration["local_scratch_dir"],
                                   "postgresql-%s.md5" % self.configuration["filename"]))
        else:
            # The PG database is on a remote host
            cmd = '%(ssh_cmd)s "/usr/sbin/sosreport --batch --report -o postgresql ' + opt
            stdout = self.caller.call(cmd)
            self.parse_sosreport_stdout(stdout)
            self.caller.call('%(scp_cmd)s:%(path)s %(local_scratch_dir)s')
            self.caller.call('%(ssh_cmd)s "rm %(path)s*"')

        # Prepend postgresql- to the PostgreSQL SOS report
        # so that it is easy to distinguished from the other N reports
        # that are all related to hypervisors.
        os.rename(os.path.join(self.configuration["local_scratch_dir"], self.configuration["filename"]),
                  os.path.join(self.configuration["local_scratch_dir"], "postgresql-%s" % self.configuration["filename"]))


class LogCollector(object):

    def __init__(self, configuration):
        self.conf = configuration
        if self.conf.command is None:
            raise Exception("No command specified.")

    def write_time_diff(self, queue):
        local_scratch_dir = self.conf.get("local_scratch_dir")

        with open(os.path.join(local_scratch_dir, DEFAULT_TIME_SHIFT_FILE), "w") as fd:
            for record in queue:
                fd.write(record + "\n")

    def _get_hypervisors_from_api(self):
        if not self.conf:
            raise Exception("No configuration.")

        try:
            self.conf.prompt("engine", msg="hostname of oVirt Engine")
            self.conf.prompt("user", msg="username for oVirt Engine")
            self.conf.getpass("passwd", msg="password for oVirt Engine")
        except Configuration.SkipException:
            logging.info("Will not collect hypervisor list from oVirt Engine API.")
            raise

        try:
            return hypervisors.get_all(self.conf.get("engine"),
                                       self.conf.get("user"),
                                       self.conf.get("passwd"))
        except Exception, e:
            ExitCodes.exit_code=ExitCodes.WARN
            logging.error("_get_hypervisors_from_api: %s" % e)
            return set()

    @staticmethod
    def _sift_patterns(list_):
        """Returns two sets: patterns and others. A pattern is any string
           that contains the any of the following: * [ ] ?"""
        patterns = set()
        others = set()

        try:
            for candidate in list_:
                if any(c in candidate for c in ('*', '[', ']', '?')):
                    patterns.add(candidate)
                else:
                    others.add(candidate)
        except TypeError:
            pass

        return patterns, others

    def _filter_hosts(self, which, pattern):
        logging.debug("filtering host list with %s against %s name" % (pattern, which))

        if which == "host":
            return set([(dc, cl, h) for dc, cl, h in self.conf.get("hosts")
                    if fnmatch.fnmatch(h, pattern)])
        elif which == "cluster":
            return set([(dc, cl, h) for dc, cl, h in self.conf.get("hosts")
                    if fnmatch.fnmatch(cl, pattern)])
        elif which == "datacenter":
            return set([(dc, cl, h) for dc, cl, h in self.conf.get("hosts")
                    if fnmatch.fnmatch(dc, pattern)])

    def set_hosts(self):
        """Fetches the hostnames for the supplied cluster or datacenter.
           Filtering is applied if patterns are found in the --hosts, --cluster
           or --datacenters options. There can be multiple patterns in each
           option. Patterns found within the same option are inclusive and
           each each option set together is treated as an intersection.
        """

        self.conf["hosts"] = set()

        host_patterns, host_others = self._sift_patterns(self.conf.get("hosts_list"))
        datacenter_patterns = self.conf.get("datacenter", [])
        cluster_patterns = self.conf.get("cluster", [])

        if host_patterns:
            self.conf['host_pattern'] = host_patterns

        if any((host_patterns,
            datacenter_patterns,
            cluster_patterns)) or not host_others:
            self.conf["hosts"] = self._get_hypervisors_from_api()

        host_filtered = set()
        cluster_filtered = set()
        datacenter_filtered = set()

        if host_patterns:
            for pattern in host_patterns:
                host_filtered |= self._filter_hosts("host", pattern)
            self.conf['hosts'] &= host_filtered

        if datacenter_patterns:
            for pattern in datacenter_patterns:
                datacenter_filtered |= self._filter_hosts("datacenter", pattern)
            self.conf['hosts'] &= datacenter_filtered

        if cluster_patterns:
            for pattern in cluster_patterns:
                cluster_filtered |= self._filter_hosts("cluster", pattern)
            self.conf['hosts'] &= cluster_filtered

        # build a set of hostnames that are already in the target host list.
        # So that we can prevent duplication in the next step
        hostnames = set((t[2] for t in self.conf['hosts']))

        for hostname in host_others:
            if hostname not in hostnames:
                self.conf['hosts'].add(("", "", hostname))

        return bool(self.conf.get("hosts"))

    def list_hosts(self):

        def get_host(tuple_):
            return tuple_[2]

        host_list = list(self.conf.get("hosts"))
        host_list.sort(key=get_host)

        fmt = "%-20s | %-20s | %s"
        print "Host list (datacenter=%(datacenter)s, cluster=%(cluster)s, host=%(host_pattern)s):" % self.conf
        print fmt % ("Data Center", "Cluster", "Hostname/IP Address")
        print "\n".join(fmt % (dc, cluster, host) for dc, cluster, host in host_list)

    def get_hypervisor_data(self):
        hosts = self.conf.get("hosts")

        if hosts:
            if not self.conf.get("quiet"):
                continue_ = get_from_prompt(
                        msg="About to collect information from %d hypervisors. Continue? (Y/n): " % len(hosts),
                        default='y')

                if continue_ not in ('y', 'Y'):
                    logging.info("Aborting hypervisor collection...")
                    return

            logging.info("Gathering information from selected hypervisors...")

            max_connections = self.conf.get("max_connections", 10)

            import threading
            from collections import deque

            # max_connections may be defined as a string via a .rc file
            sem = threading.Semaphore(int(max_connections))
            time_diff_queue = deque()

            threads = []

            for datacenter, cluster, host in hosts:
                sem.acquire(True)
                collector = HyperVisorData(host.strip(),
                                           configuration=self.conf,
                                           semaphore=sem,
                                           queue=time_diff_queue)
                thread = threading.Thread(target=collector.run)
                thread.start()
                threads.append(thread)

            for thread in threads:
                thread.join()

            self.write_time_diff(time_diff_queue)

    def get_postgres_data(self):
        if self.conf.get("no_postgresql") == False:
            try:
                try:
                    self.conf.getpass("pg_pass", msg="password for the PostgreSQL user, %s, to dump the %s PostgreSQL database instance" %
                                          (self.conf.get('pg_user'),
                                           self.conf.get('pg_dbname')))
                    logging.info("Gathering PostgreSQL the oVirt Engine database and log files from %s..." % (self.conf.get("pg_dbhost")))
                except Configuration.SkipException:
                    logging.info("PostgreSQL oVirt Engine database will not be collected.")
                    logging.info("Gathering PostgreSQL log files from %s..." % (self.conf.get("pg_dbhost")))

                collector = PostgresData(self.conf.get("pg_dbhost"),
                                         configuration=self.conf)
                collector.sosreport()
            except Exception, e:
                ExitCodes.exit_code=ExitCodes.WARN
                logging.error("Could not collect PostgreSQL information: %s" % e)
        else:
            ExitCodes.exit_code=ExitCodes.NOERR
            logging.info("Skipping postgresql collection...")

    def get_engine_data(self):
        logging.info("Gathering oVirt Engine information...")
        if self.conf.get("enable_jmx"):
            try:
                self.conf.getpass("jboss.pass", msg="password for the JBoss JMX user")
            except Configuration.SkipException:
                logging.info("JBoss JMX information will not be collected because the JMX user's password was not supplied.")
        collector = ENGINEData("localhost",
                              configuration=self.conf)
        stdout = collector.sosreport()
        logging.info(stdout)

def parse_password(option, opt_str, value, parser):
    value = getpass.getpass("Please enter %s: " % (option.help))
    setattr(parser.values, option.dest, value)

if __name__ == '__main__':

    def comma_separated_list(option, opt_str, value, parser):
        setattr(parser.values, option.dest, [v.strip() for v in value.split(",")])

    usage_string = "\n".join(("Usage: %prog [options] list",
                              "       %prog [options] collect"))

    epilog_string = """\nReturn values:
    0: The program ran to completion with no errors.
    1: The program encountered a critical failure and stopped.
    2: The program encountered a problem gathering data but was able to continue.
"""
    OptionParser.format_epilog = lambda self, formatter: self.epilog
    parser = OptionParser(usage_string,
                          version="Version " + versionNum,
                          epilog=epilog_string)


    parser.add_option("", "--conf-file", dest="conf_file",
            help="path to configuration file (default=%s)" % DEFAULT_CONFIGURATION_FILE,
            metavar="PATH")

    parser.add_option("", "--local-tmp", dest="local_tmp_dir",
            help="directory to copy reports to locally (default=%s)" % DEFAULT_SCRATCH_DIR, metavar="PATH",
            default=DEFAULT_SCRATCH_DIR)

    parser.add_option("", "--ticket-number", dest="ticket_number",
            help="ticket number to pass with the sosreport",
            metavar="TICKET")

    parser.add_option("", "--upload", dest="upload",
            help="Upload the report to Red Hat (use exclusively if advised from a Red Hat support representative).",
            metavar="FTP_SERVER")

    parser.add_option("", "--quiet", dest="quiet",
            action="store_true", default=False,
            help="reduce console output (default=False)")

    parser.add_option("", "--log-file",
                      dest="log_file",
                      help="path to log file (default=%s)" % DEFAULT_LOG_FILE,
                      metavar="PATH",
                      default=DEFAULT_LOG_FILE)

    parser.add_option("-v", "--verbose", dest="verbose",
            action="store_true", default=False)

    engine_group = OptionGroup(parser,
                              "oVirt Engine Configuration",
"""The options in the oVirt Engine configuration group can be used to filter log collection from one or more RHEV-H.
If the --no-hypervisors option is specified, data is not collected from any RHEV-H.""")

    engine_group.add_option("", "--no-hypervisors",
            help="skip collection from hypervisors (default=False)",
            dest="no_hypervisor",
            action="store_true",
            default=False)

    engine_group.add_option("-u", "--user", dest="user",
            help="username to use with the REST API.  This should be in UPN format.",
            metavar="user@engine.example.com")

    engine_group.add_option("-p",
                           "--passwd",
                           dest="passwd",
                           help=SUPPRESS_HELP)

    engine_group.add_option("-r", "--engine", dest="engine", metavar="engine.example.com",
            help="hostname or IP address of the oVirt Engine (default=localhost:8443)",
            default="localhost:8443")

    engine_group.add_option("-c", "--cluster", dest="cluster",
            help="pattern, or comma separated list of patterns to filter the host list by cluster name (default=None)",
            action="callback",
            callback=comma_separated_list,
            type="string",
            default=None, metavar="CLUSTER")

    engine_group.add_option("-d", "--data-center", dest="datacenter",
            help="pattern, or comma separated list of patterns to filter the host list by data center name (default=None)",
            action="callback",
            callback=comma_separated_list,
            type="string",
            default=None, metavar="DATACENTER")

    engine_group.add_option("-H", "--hosts", dest="hosts_list", action="callback",
            callback=comma_separated_list,
            type="string",
            help="""comma separated list of hostnames, hostname patterns, FQDNs, FQDN patterns,
IP addresses, or IP address patterns from which the log collector should collect RHEV-H logs (default=None)""")

    ssh_group = OptionGroup(parser, "SSH Configuration",
"""The options in the SSH configuration group can be used to specify the maximum
number of concurrent SSH connections to RHEV-H(s) for log collection, the
SSH port, and a identity file to be used.""")

    ssh_group.add_option("", "--ssh-port", dest="ssh_port",
            help="the port to ssh and scp on", metavar="PORT",
            default=22)

    ssh_group.add_option("-k", "--key-file", dest="key_file",
            help="""the identity file (private key) to be used for accessing the RHEV-Hs (default=%s).
If a identity file is not supplied the program will prompt for a password.  It is strongly recommended to
use key based authentication with SSH because the program may make multiple SSH connections
resulting in multiple requests for the SSH password.""" % DEFAULT_SSH_KEY,
            metavar="KEYFILE",
            default=DEFAULT_SSH_KEY)

    ssh_group.add_option("", "--max-connections", dest="max_connections",
            help="max concurrent connections for fetching RHEV-H logs (default = 10)",
            default=10)

    db_group = OptionGroup(parser, "PostgreSQL Database Configuration",
"""The log collector will connect to the oVirt Engine PostgreSQL database and dump the data
for inclusion in the log report unless --no-postgresql is specified.  The PostgreSQL user ID and database
name can be specified if they are different from the defaults.  If the PostgreSQL database
is not on the localhost set pg-dbhost, provide a pg-ssh-user, and optionally supply pg-host-key and the log collector
will gather remote PostgreSQL logs.  The PostgreSQL SOS plug-in must be installed on pg-dbhost for
successful remote log collection.""")

    db_group.add_option("", "--no-postgresql", dest="no_postgresql",
            help="This option causes the tool to skip the postgresql collection (default=false)",
            action="store_true",
            default=False)

    db_group.add_option("", "--pg-user", dest="pg_user",
            help="PostgreSQL database user name (default=postgres)",
            metavar="postgres",
            default="postgres")

    db_group.add_option("",
                        "--pg-pass",
                        dest="pg_pass",
                        help=SUPPRESS_HELP)

    db_group.add_option("", "--pg-dbname", dest="pg_dbname",
            help="PostgreSQL database name (default=engine)",
            metavar="engine",
            default="engine")

    db_group.add_option("", "--pg-dbhost", dest="pg_dbhost",
            help="PostgreSQL database hostname or IP address (default=localhost)",
            metavar="localhost",
            default="localhost")

    db_group.add_option("", "--pg-ssh-user", dest="pg_ssh_user",
            help="""the SSH user that will be used to connect to the
server upon which the remote PostgreSQL database lives. (default=root)""",
            metavar="root",
            default='root')

    db_group.add_option("", "--pg-host-key", dest="pg_host_key",
            help="""the identity file (private key) to be used for accessing the host
upon which the PostgreSQL database lives (default=not needed if using localhost)""",
            metavar="none")

    jboss_group = OptionGroup(parser,
                              "SOSReport Options",
"""The JBoss SOS plug-in will always be executed.  To activate data collection
from JBoss's JMX console enable-jmx, java-home, jboss-user, and jboss-pass must
also be specified.  If no jboss-pass is supplied in the configuration file then
it will be asked for prior to collection time.""")

    jboss_group.add_option("", "--jboss-home", dest="jboss.home",
        help="JBoss's installation dir (default=/var/lib/jbossas)",
        metavar="/path/to/jbossas",
        default="/var/lib/jbossas")

    jboss_group.add_option("", "--java-home", dest="jboss.javahome",
        help="Java's installation dir (default=/usr/lib/jvm/java)",
        metavar="/path/to/java",
        default="/usr/lib/jvm/java")

    jboss_group.add_option("", "--jboss-profile",
        dest="jboss.profile",
        action="callback",
        type="string",
        help="comma separated list of server profiles to limit collection (default='engine-slimmed')",
        callback=comma_separated_list,
        metavar="PROFILE1, PROFILE2",
        default="engine-slimmed")

    jboss_group.add_option("", "--enable-jmx", dest="enable_jmx",
            help="Enable the collection of run-time metrics from the oVirt Engine JBoss JMX interface",
            action="store_true",
            default=False)

    jboss_group.add_option("", "--jboss-user", dest="jboss.user",
        help="JBoss JMX username (default=admin)",
        metavar="admin",
        default="admin")

    jboss_group.add_option("",
                           "--jboss-pass",
                           dest="jboss.pass",
                           help=SUPPRESS_HELP)

    jboss_group.add_option("", "--jboss-logsize", dest="jboss.logsize",
        help="max size (MiB) to collect per log file (default=15)",
        metavar="15",
        default=15)

    jboss_group.add_option("", "--jboss-stdjar", dest="jboss.stdjar",
        metavar="on or off",
        help="collect jar statistics for JBoss standard jars.(default=on)")

    jboss_group.add_option("", "--jboss-servjar", dest="jboss.servjar",
        metavar="on or off",
        help="collect jar statistics from any server configuration dirs (default=on)")

    jboss_group.add_option("", "--jboss-twiddle", dest="jboss.twiddle",
        metavar="on or off",
        help="collect twiddle data (default=on)")

    jboss_group.add_option("", "--jboss-appxml",
        dest="jboss.appxml",
        action="callback",
        type="string",
        callback=comma_separated_list,
        help="""comma separated list of application's whose XML descriptors you want (default=all)""",
        metavar="APP, APP2",
        default="all")

    parser.add_option_group(engine_group)
    parser.add_option_group(jboss_group)
    parser.add_option_group(ssh_group)
    parser.add_option_group(db_group)

    try:
        conf = Configuration(parser)
        collector = LogCollector(conf)

        # We must ensure that the working directory exits before
        # we start doing anything.
        if os.path.exists(conf["local_tmp_dir"]):
            if not os.path.isdir(conf["local_tmp_dir"]):
                raise Exception('%s is not a directory.' % (conf["local_tmp_dir"]))
        else:
            logging.info("%s does not exist.  It will be created." % (conf["local_tmp_dir"]))
            os.makedirs(conf["local_tmp_dir"])

        # We need to make a temporary scratch directory wherein
        # all of the output from VDSM and PostgreSQL SOS plug-ins
        # will be dumped.  The contents of this directory will be scooped
        # up by the oVirt Engine SOS plug-in via the engine.vdsmlogs option
        # and included in a single .xz file.
        conf["local_scratch_dir"] = os.path.join(conf["local_tmp_dir"], 'RHEVH-and-PostgreSQL-reports')
        if not os.path.exists(conf["local_scratch_dir"]):
            os.makedirs(conf["local_scratch_dir"])
        else:
            if len(os.listdir(conf["local_scratch_dir"])) != 0:
                raise Exception("""the scratch directory for temporary storage of RHEVH reports is not empty.
It should be empty so that reports from a prior invocation of the log collector are not collected again.
The directory is: %s'""" % (conf["local_scratch_dir"]))


        if conf.command == "collect":
            if not conf.get("no_hypervisor"):
                if collector.set_hosts():
                    collector.get_hypervisor_data()
                else:
                    logging.info("No hypervisors were selected, therefore no hypervisor data will be collected.")
            else:
                logging.info("Skipping hypervisor collection...")

            collector.get_postgres_data()
            collector.get_engine_data()

        elif conf.command == "list":
            if collector.set_hosts():
                collector.list_hosts()
            else:
                logging.info("No hypervisors were found, therefore no hypervisor data will be listed.")

        # Clean up the temp directory
        shutil.rmtree(conf["local_scratch_dir"])
    except  KeyboardInterrupt, k:
        print "Exiting on user cancel."
    except Exception, e:
        multilog(logging.error, e)
        print "Use the -h option to see usage."
        logging.debug("Configuration:")
        try:
            logging.debug("command: %s" % conf.command)
            #multilog(logging.debug, pprint.pformat(conf))
        except:
            pass
        multilog(logging.debug, traceback.format_exc())
        sys.exit(ExitCodes.CRITICAL)

    sys.exit(ExitCodes.exit_code)
