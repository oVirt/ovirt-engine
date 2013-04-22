#!/usr/bin/python

# Copyright 2012 Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import glob
import logging
import logging.handlers
import optparse
import os
import re
import shutil
import signal
import subprocess
import sys
import time
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


import daemon
from Cheetah.Template import Template


import config


class Base(object):
    """
    Base class for logging.
    """
    def __init__(self):
        self._logger = logging.getLogger(
            'ovirt.engine.service.%s' % self.__class__.__name__
        )


class ConfigFile(Base):
    """
    Helper class to simplify getting values from the configuration, specially
    from the template used to generate the application server configuration
    file
    """

    # Compile regular expressions:
    COMMENT_EXPR = re.compile(r'\s*#.*$')
    BLANK_EXPR = re.compile(r'^\s*$')
    VALUE_EXPR = re.compile(r'^\s*(?P<key>\w+)\s*=\s*(?P<value>.*?)\s*$')
    REF_EXPR = re.compile(r'\$\{(?P<ref>\w+)\}')

    def __init__(self, files):
        super(ConfigFile, self).__init__()

        self._dir = dir
        # Save the list of files:
        self.files = files

        # Start with an empty set of values:
        self.values = {}

        # Merge all the given configuration files, in the same order
        # given, so that the values in one file are overriden by values
        # in files appearing later in the list:
        for file in self.files:
            self.loadFile(file)
            for filed in sorted(
                glob.glob(
                    os.path.join(
                        '%s.d' % file,
                        '*.conf',
                    )
                )
            ):
                self.loadFile(filed)

    def loadFile(self, file):
        if os.path.exists(file):
            self._logger.debug("loading config '%s'", file)
            with open(file, 'r') as f:
                for line in f:
                    self.loadLine(line)

    def loadLine(self, line):
        # Remove comments:
        commentMatch = self.COMMENT_EXPR.search(line)
        if commentMatch is not None:
            line = line[:commentMatch.start()] + line[commentMatch.end():]

        # Skip empty lines:
        emptyMatch = self.BLANK_EXPR.search(line)
        if emptyMatch is not None:
            return

        # Separate name from value:
        keyValueMatch = self.VALUE_EXPR.search(line)
        if keyValueMatch is None:
            return
        key = keyValueMatch.group('key')
        value = keyValueMatch.group('value')

        # Strip quotes from value:
        if len(value) >= 2 and value[0] == '"' and value[-1] == '"':
            value = value[1:-1]

        # Expand references to other parameters:
        while True:
            refMatch = self.REF_EXPR.search(value)
            if refMatch is None:
                break
            refKey = refMatch.group('ref')
            refValue = self.values.get(refKey)
            if refValue is None:
                break
            value = '%s%s%s' % (
                value[:refMatch.start()],
                refValue,
                value[refMatch.end():],
            )

        # Update the values:
        self.values[key] = value

    def getString(self, name):
        text = self.values.get(name)
        if text is None:
            raise RuntimeError(
                _("The parameter '{name}' does not have a value").format(
                    name=name,
                )
            )
        return text

    def getBoolean(self, name):
        return self.getString(name) in ('t', 'true', 'y', 'yes', '1')

    def getInteger(self, name):
        value = self.getString(name)
        try:
            return int(value)
        except ValueError:
            raise RuntimeError(
                _(
                    "The value '{value}' of parameter '{name}' "
                    "is not a valid integer"
                ).format(
                    name,
                    value,
                )
            )


class TempDir(Base):
    """
    Temporary directory scope management

    Usage:
        with TempDir(directory):
            pass
    """

    def _clear(self):
        self._logger.debug("removing directory '%s'", self._dir)
        if os.path.exists(self._dir):
            shutil.rmtree(self._dir)

    def __init__(self, dir):
        super(TempDir, self).__init__()
        self._dir = dir

    def __enter__(self):
        self._clear()
        os.mkdir(self._dir)

    def __exit__(self, exc_type, exc_value, traceback):
        try:
            self._clear()
        except Exception as e:
            self._logger.warning(
                _("Cannot remove directory '{directory}': {error}").format(
                    directory=self._dir,
                    error=e,
                ),
            )
            self._logger.debug('exception', exc_info=True)


class PidFile(Base):
    """
    pidfile scope management

    Usage:
        with PidFile(pidfile):
            pass
    """

    def __init__(self, file):
        super(PidFile, self).__init__()
        self._file = file

    def __enter__(self):
        if self._file is not None:
            self._logger.debug(
                "creating pidfile '%s' pid=%s",
                self._file,
                os.getpid()
            )
            with open(self._file, 'w') as f:
                f.write('%s\n' % os.getpid())

    def __exit__(self, exc_type, exc_value, traceback):
        if self._file is not None:
            self._logger.debug("removing pidfile '%s'", self._file)
            try:
                os.remove(self._file)
            except OSError:
                # we may not have permissions to delete pid
                # so just try to empty it
                try:
                    with open(self._file, 'w'):
                        pass
                except IOError as e:
                    self._logger.error(
                        _("Cannot remove pidfile '{file}': {error}").format(
                            file=self._file,
                            error=e,
                        ),
                    )
                    self._logger.debug('exception', exc_info=True)


class EngineDaemon(Base):
    """
    The engine daemon
    """

    def __init__(self):
        super(EngineDaemon, self).__init__()

    def _loadConfig(self):
        if not os.path.exists(config.ENGINE_DEFAULT_FILE):
            raise RuntimeError(
                _(
                    "The engine configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=config.ENGINE_DEFAULT_FILE,
                )
            )

        self._config = ConfigFile(
            (
                config.ENGINE_DEFAULT_FILE,
                config.ENGINE_VARS,
            ),
        )

    def _processTemplate(self, template):
        out = os.path.join(
            self._config.getString('ENGINE_TMP'),
            re.sub('\.in$', '', os.path.basename(template)),
        )
        with open(out, 'w') as f:
            f.write(str(Template(file=template, searchList=[self._config])))
        return out

    def _linkModules(self, modulesDir):
        """Link all the JBoss modules into a temporary directory"""

        modulesTmpDir = os.path.join(
            self._config.getString('ENGINE_TMP'),
            'modules',
        )

        # For each directory in the modules directory create the same in the
        # temporary directory and populate with symlinks pointing to the
        # original files (excluding indexes):
        for parentDir, childrenDirs, childrenFiles in os.walk(modulesDir):
            parentTmpDir = parentDir.replace(modulesDir, modulesTmpDir, 1)
            if not os.path.exists(parentTmpDir):
                os.makedirs(parentTmpDir)
            for childFile in childrenFiles:
                if childFile.endswith('.index'):
                    continue
                childPath = os.path.join(parentDir, childFile)
                childTmpPath = os.path.join(parentTmpDir, childFile)
                os.symlink(childPath, childTmpPath)

        return modulesTmpDir

    def _check(
        self,
        name,
        mustExist=True,
        readable=True,
        writable=False,
        executable=False,
        directory=False,
    ):
        artifact = _('Directory') if directory else _('File')

        if directory:
            readable = True
            executable = True

        if os.path.exists(name):
            if directory and not os.path.isdir(name):
                raise RuntimeError(
                    _("{artifact} '{name}' is required but missing").format(
                        artifact=artifact,
                        name=name,
                    )
                )
            if readable and not os.access(name, os.R_OK):
                raise RuntimeError(
                    _(
                        "{artifact} '{name}' cannot be accessed "
                        "for reading"
                    ).format(
                        artifact=artifact,
                        name=name,
                    )
                )
            if writable and not os.access(name, os.W_OK):
                raise RuntimeError(
                    _(
                        "{artifact} '{name}' cannot be accessed "
                        "for writing"
                    ).format(
                        artifact=artifact,
                        name=name,
                    )
                )
            if executable and not os.access(name, os.X_OK):
                raise RuntimeError(
                    _(
                        "{artifact} '{name}' cannot be accessed "
                        "for execution"
                    ).format(
                        artifact=artifact,
                        name=name,
                    )
                )
        else:
            if mustExist:
                raise RuntimeError(
                    _("{artifact} '{name}' is required but missing").format(
                        artifact=artifact,
                        name=name,
                    )
                )

            if not os.path.exists(os.path.dirname(name)):
                raise RuntimeError(
                    _(
                        "{artifact} '{name}' is to be created but "
                        "parent directory is missing"
                    ).format(
                        artifact=artifact,
                        name=name,
                    )
                )

            if not os.access(os.path.dirname(name), os.W_OK):
                raise RuntimeError(
                    _(
                        "{artifact} '{name}' is to be created but "
                        "parent directory is not writable"
                    ).format(
                        artifact=artifact,
                        name=name,
                    )
                )

    def _checkInstallation(
        self,
        pidfile,
        jbossModulesJar,
        java,
    ):
        # Check that the Java home directory exists and that it contais at
        # least the java executable:
        self._check(
            name=self._config.getString('JAVA_HOME'),
            directory=True,
        )
        self._check(
            name=java,
            executable=True,
        )

        # Check the required JBoss directories and files:
        self._check(
            name=self._config.getString('JBOSS_HOME'),
            directory=True,
        )
        self._check(
            name=jbossModulesJar,
        )

        # Check the required engine directories and files:
        self._check(
            os.path.join(
                self._config.getString('ENGINE_USR'),
                'services',
            ),
            directory=True,
        )
        self._check(
            self._config.getString('ENGINE_CACHE'),
            directory=True,
            writable=True,
        )
        self._check(
            self._config.getString('ENGINE_TMP'),
            directory=True,
            writable=True,
            mustExist=False,
        )
        for dir in ('.', 'content', 'deployments'):
            self._check(
                os.path.join(
                    self._config.getString('ENGINE_VAR'),
                    dir
                ),
                directory=True,
                writable=True,
            )
        self._check(
            self._config.getString('ENGINE_LOG'),
            directory=True,
            writable=True,
        )
        self._check(
            name=os.path.join(
                self._config.getString("ENGINE_LOG"),
                'host-deploy',
            ),
            directory=True,
            writable=True,
        )
        for log in ('engine.log', 'console.log', 'server.log'):
            self._check(
                name=os.path.join(self._config.getString("ENGINE_LOG"), log),
                mustExist=False,
                writable=True,
            )
        if pidfile is not None:
            self._check(
                name=pidfile,
                writable=True,
            )

    def _setupEngineApps(self):

        # The list of applications to be deployed:
        for engineApp in self._config.getString('ENGINE_APPS').split():
            # Do nothing if the application is not available:
            engineAppDir = os.path.join(
                self._config.getString('ENGINE_USR'),
                engineApp,
            )
            if not os.path.exists(engineAppDir):
                self._logger.warning(
                    _(
                        "Application '{application}' directory '{directory}' "
                        "does not exist, it will be ignored"
                    ).format(
                        application=engineApp,
                        directory=engineAppDir,
                    ),
                )
                continue

            # Make sure the application is linked in the deployments
            # directory, if not link it now:
            engineAppLink = os.path.join(
                self._config.getString('ENGINE_VAR'),
                'deployments',
                engineApp,
            )
            if not os.path.islink(engineAppLink):
                try:
                    os.symlink(engineAppDir, engineAppLink)
                except OSError as e:
                    self._logger.debug('exception', exc_info=True)
                    raise RuntimeError(
                        _(
                            "Cannot create symbolic link '{file}': "
                            "{error}"
                        ).format(
                            file=engineAppLink,
                            error=e,
                        ),
                    )

            # Remove all existing deployment markers:
            for markerFile in glob.glob('%s.*' % engineAppLink):
                try:
                    os.remove(markerFile)
                except OSError as e:
                    self._logger.debug('exception', exc_info=True)
                    raise RuntimeError(
                        _(
                            "Cannot remove deployment marker file '{file}': "
                            "{error}"
                        ).format(
                            file=markerFile,
                            error=e,
                        ),
                    )

            # Create the new marker file to trigger deployment
            # of the application:
            markerFile = "%s.dodeploy" % engineAppLink
            try:
                with open(markerFile, "w"):
                    pass
            except IOError as e:
                self._logger.debug('exception', exc_info=True)
                raise RuntimeError(
                    _(
                        "Cannot create deployment marker file '{file}': "
                        "{error}"
                    ).format(
                        file=markerFile,
                        error=e,
                    )
                )

    def _daemon(self, args, executable, env):

        self._logger.debug(
            'executing daemon: exe=%s, args=%s, env=%s',
            executable,
            args,
            env,
        )
        self._logger.debug('background=%s', self._options.background)

        class _TerminateException(Exception):
            pass

        def _myterm(signum, frame):
            raise _TerminateException()

        engineConsoleLog = open(
            os.path.join(
                self._config.getString('ENGINE_LOG'),
                'console.log'
            ),
            'w+',
        )

        with daemon.DaemonContext(
            detach_process=self._options.background,
            signal_map={
                signal.SIGTERM: _myterm,
                signal.SIGINT: _myterm,
                signal.SIGHUP: None,
            },
            stdout=engineConsoleLog,
            stderr=engineConsoleLog,
        ):
            self._logger.debug('I am a daemon %s', os.getpid())

            with PidFile(self._options.pidfile):
                try:
                    self._logger.debug('creating process')
                    p = subprocess.Popen(
                        args=args,
                        executable=executable,
                        env=env,
                        close_fds=True,
                    )

                    self._logger.debug(
                        'waiting for termination of pid=%s',
                        p.pid,
                    )
                    p.wait()
                    self._logger.debug(
                        'terminated pid=%s rc=%s',
                        p.pid,
                        p.returncode,
                    )

                    if p.returncode != 0:
                        raise RuntimeError(
                            _(
                                'Engine terminated with status '
                                'code {code}'
                            ).format(
                                code=p.returncode,
                            )
                        )

                except _TerminateException:
                    self._logger.debug('got stop signal')

                    stopTime = self._config.getInteger(
                        'ENGINE_STOP_TIME'
                    )
                    stopInterval = self._config.getInteger(
                        'ENGINE_STOP_INTERVAL'
                    )

                    # avoid recursive signals
                    for sig in (signal.SIGTERM, signal.SIGINT):
                        signal.signal(sig, signal.SIG_IGN)

                    try:
                        self._logger.debug('terminating pid=%s', p.pid)
                        p.terminate()
                        for i in range(stopTime // stopInterval):
                            if p.poll() is not None:
                                self._logger.debug('terminated pid=%s', p.pid)
                                break
                            self._logger.debug(
                                'waiting for termination of pid=%s',
                                p.pid,
                            )
                            time.sleep(stopInterval)
                    except OSError as e:
                        self._logger.warning(
                            _('Cannot terminate pid {pid}: {error}').format(
                                pid=p.pid,
                                error=e,
                            )
                        )
                        self._logger.debug('exception', exc_info=True)

                    try:
                        if p.poll() is None:
                            self._logger.debug('killing pid=%s', p.pid)
                            p.kill()
                            raise RuntimeError(
                                _('Had to kill engine process {pid}').format(
                                    pid=p.pid
                                )
                            )
                    except OSError as e:
                        self._logger.warning(
                            _('Cannot kill pid {pid}: {error}').format(
                                pid=p.pid,
                                error=e
                            )
                        )
                        self._logger.debug('exception', exc_info=True)
                        raise

    def _start(self):

        self._logger.debug('start entry pid=%s', os.getpid())

        if os.geteuid() == 0:
            raise RuntimeError(
                _('This script cannot be run as root')
            )

        self._loadConfig()

        jbossModulesJar = os.path.join(
            self._config.getString('JBOSS_HOME'),
            'jboss-modules.jar',
        )
        java = os.path.join(
            self._config.getString('JAVA_HOME'),
            'bin',
            'java',
        )

        self._checkInstallation(
            pidfile=self._options.pidfile,
            jbossModulesJar=jbossModulesJar,
            java=java,
        )

        with TempDir(self._config.getString('ENGINE_TMP')):
            self._setupEngineApps()

            jbossBootLoggingFile = self._processTemplate(
                os.path.join(
                    self._config.getString('ENGINE_USR'),
                    'services',
                    'engine-service-logging.properties.in'
                ),
            )

            jbossConfigFile = self._processTemplate(
                os.path.join(
                    self._config.getString('ENGINE_USR'),
                    'services',
                    'engine-service.xml.in',
                ),
            )

            jbossModulesTmpDir = self._linkModules(
                os.path.join(
                    self._config.getString('JBOSS_HOME'),
                    'modules',
                ),
            )

            # We start with an empty list of arguments:
            engineArgs = []

            # Add arguments for the java virtual machine:
            engineArgs.extend([
                # The name or the process, as displayed by ps:
                'engine-service',

                # Virtual machine options:
                '-server',
                '-XX:+TieredCompilation',
                '-Xms%s' % self._config.getString('ENGINE_HEAP_MIN'),
                '-Xmx%s' % self._config.getString('ENGINE_HEAP_MAX'),
                '-XX:PermSize=%s' % self._config.getString('ENGINE_PERM_MIN'),
                '-XX:MaxPermSize=%s' % self._config.getString(
                    'ENGINE_PERM_MAX'
                ),
                '-Djava.net.preferIPv4Stack=true',
                '-Dsun.rmi.dgc.client.gcInterval=3600000',
                '-Dsun.rmi.dgc.server.gcInterval=3600000',
                '-Djava.awt.headless=true',
            ])

            # Add extra system properties provided in the configuration:
            engineProperties = self._config.getString('ENGINE_PROPERTIES')
            for engineProperty in engineProperties.split():
                if not engineProperty.startswith('-D'):
                    engineProperty = '-D' + engineProperty
                engineArgs.append(engineProperty)

            # Add arguments for remote debugging of the java virtual machine:
            engineDebugAddress = self._config.getString('ENGINE_DEBUG_ADDRESS')
            if engineDebugAddress:
                engineArgs.append(
                    (
                        '-Xrunjdwp:transport=dt_socket,address=%s,'
                        'server=y,suspend=n'
                    ) % (
                        engineDebugAddress
                    )
                )

            # Enable verbose garbage collection if required:
            if self._config.getBoolean('ENGINE_VERBOSE_GC'):
                engineArgs.extend([
                    '-verbose:gc',
                    '-XX:+PrintGCTimeStamps',
                    '-XX:+PrintGCDetails',
                ])

            # Add arguments for JBoss:
            engineArgs.extend([
                '-Djava.util.logging.manager=org.jboss.logmanager',
                '-Dlogging.configuration=file://%s' % jbossBootLoggingFile,
                '-Dorg.jboss.resolver.warning=true',
                '-Djboss.modules.system.pkgs=org.jboss.byteman',
                '-Djboss.modules.write-indexes=false',
                '-Djboss.server.default.config=engine-service',
                '-Djboss.home.dir=%s' % self._config.getString(
                    'JBOSS_HOME'
                ),
                '-Djboss.server.base.dir=%s' % self._config.getString(
                    'ENGINE_USR'
                ),
                '-Djboss.server.config.dir=%s' % self._config.getString(
                    'ENGINE_TMP'
                ),
                '-Djboss.server.data.dir=%s' % self._config.getString(
                    'ENGINE_VAR'
                ),
                '-Djboss.server.log.dir=%s' % self._config.getString(
                    'ENGINE_LOG'
                ),
                '-Djboss.server.temp.dir=%s' % self._config.getString(
                    'ENGINE_TMP'
                ),
                '-Djboss.controller.temp.dir=%s' % self._config.getString(
                    'ENGINE_TMP'
                ),
                '-jar', jbossModulesJar,

                # Module path should include first the engine modules
                # so that they can override those provided by the
                # application server if needed:
                '-mp', "%s:%s" % (
                    os.path.join(
                        self._config.getString('ENGINE_USR'),
                        'modules',
                    ),
                    jbossModulesTmpDir,
                ),

                '-jaxpmodule', 'javax.xml.jaxp-provider',
                'org.jboss.as.standalone',
                '-c', os.path.basename(jbossConfigFile),
            ])

            engineEnv = os.environ.copy()
            engineEnv.update({
                'PATH': '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin',
                'LANG': 'en_US.UTF-8',
                'LC_ALL': 'en_US.UTF-8',
                'ENGINE_DEFAULTS': config.ENGINE_DEFAULT_FILE,
                'ENGINE_VARS': config.ENGINE_VARS,
                'ENGINE_ETC': self._config.getString('ENGINE_ETC'),
                'ENGINE_LOG': self._config.getString('ENGINE_LOG'),
                'ENGINE_TMP': self._config.getString('ENGINE_TMP'),
                'ENGINE_USR': self._config.getString('ENGINE_USR'),
                'ENGINE_VAR': self._config.getString('ENGINE_VAR'),
                'ENGINE_CACHE': self._config.getString('ENGINE_CACHE'),
            })

            self._daemon(
                args=engineArgs,
                executable=java,
                env=engineEnv,
            )

            self._logger.debug('start return')

    def run(self):
        self._logger.debug('startup args=%s', sys.argv)

        parser = optparse.OptionParser(
            usage=_('usage: %prog [options] start'),
        )
        parser.add_option(
            '-d', '--debug',
            dest='debug',
            action='store_true',
            default=False,
            help=_('debug mode'),
        )
        parser.add_option(
            '--pidfile',
            dest='pidfile',
            default=None,
            metavar=_('FILE'),
            help=_('pid file to use'),
        )
        parser.add_option(
            '--background',
            dest='background',
            action='store_true',
            default=False,
            help=_('Go into the background'),
        )
        (self._options, args) = parser.parse_args()

        if self._options.debug:
            logging.getLogger('ovirt').setLevel(logging.DEBUG)

        if len(args) != 1:
            parser.error(_('Action is missing'))
        action = args[0]
        if not action in ('start'):
            parser.error(
                _("Invalid action '{action}'").format(
                    action=action
                )
            )

        try:
            self._start()
        except Exception as e:
            self._logger.error(
                _('Error: {error}').format(
                    error=e,
                )
            )
            self._logger.debug('exception', exc_info=True)
            sys.exit(1)
        else:
            sys.exit(0)


def _setupLogger():
    class _MyFormatter(logging.Formatter):
        """Needed as syslog will truncate any lines after first."""

        def __init__(
            self,
            fmt=None,
            datefmt=None,
        ):
            logging.Formatter.__init__(self, fmt=fmt, datefmt=datefmt)

        def format(self, record):
            return logging.Formatter.format(self, record).replace('\n', ' | ')

    logger = logging.getLogger('ovirt')
    logger.propagate = False
    if os.environ.get('OVIRT_ENGINE_SERVICE_DEBUG', '0') != '0':
        logger.setLevel(logging.DEBUG)
    else:
        logger.setLevel(logging.INFO)
    h = logging.handlers.SysLogHandler(
        address='/dev/log',
        facility=logging.handlers.SysLogHandler.LOG_DAEMON,
    )
    h.setLevel(logging.DEBUG)
    h.setFormatter(
        _MyFormatter(
            fmt=(
                os.path.splitext(os.path.basename(sys.argv[0]))[0] +
                '[%(process)s] '
                '%(levelname)s '
                '%(funcName)s:%(lineno)d '
                '%(message)s'
            ),
        ),
    )
    logger.addHandler(h)


if __name__ == '__main__':
    _setupLogger()
    d = EngineDaemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
