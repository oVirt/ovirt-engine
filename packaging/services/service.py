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


import re
import glob
import logging
import logging.handlers
import optparse
import os
import shutil
import signal
import subprocess
import sys
import time
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


import daemon


def setupLogger():
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


class Base(object):
    """
    Base class for logging.
    """
    def __init__(self):
        self._logger = logging.getLogger(
            'ovirt.service.%s' % self.__class__.__name__
        )


class ConfigFile(Base):
    """
    Parsing of shell style config file.
    Follow closly the java LocalConfig implementaiton.
    """

    _EMPTY_LINE = re.compile(r'^\s*(#.*|)$')
    _KEY_VALUE_EXPRESSION = re.compile(r'^\s*(?P<key>\w+)=(?P<value>.*)$')

    def _loadLine(self, line):
        emptyMatch = self._EMPTY_LINE.search(line)
        if emptyMatch is None:
            keyValueMatch = self._KEY_VALUE_EXPRESSION.search(line)
            if keyValueMatch is None:
                raise RuntimeError(_('Invalid sytax'))
            self._values[keyValueMatch.group('key')] = self.expandString(
                keyValueMatch.group('value')
            )

    def __init__(self, files=[]):
        super(ConfigFile, self).__init__()

        self._values = {}

        for file in files:
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
            index = 0
            try:
                with open(file, 'r') as f:
                    for line in f:
                        index += 1
                        self._loadLine(line)
            except Exception as e:
                self._logger(
                    "File '%s' index %d error" % (file, index),
                    exc_info=True,
                )
                raise RuntimeError(
                    _(
                        "Cannot parse configuration file "
                        "'{file}' line {line}: {error}"
                    ).format(
                        file=file,
                        line=index,
                        error=e
                    )
                )

    def expandString(self, value):
        ret = ""

        escape = False
        inQuotes = False
        index = 0
        while (index < len(value)):
            c = value[index]
            index += 1
            if escape:
                escape = False
                ret += c
            else:
                if c == '\\':
                    escape = True
                elif c == '$':
                    if value[index] != '{':
                        raise RuntimeError('Malformed variable assignment')
                    index += 1
                    i = value.find('}', index)
                    if i == -1:
                        raise RuntimeError('Malformed variable assignment')
                    name = value[index:i]
                    index = i + 1
                    ret += self._values.get(name, "")
                elif c == '"':
                    inQuotes = not inQuotes
                elif c in (' ', '#'):
                    if inQuotes:
                        ret += c
                    else:
                        index = len(value)
                else:
                    ret += c

        return ret

    def getstring(self, name, default=None):
        "alias to get as cheetah.template cannot call get"
        return self.get(name, default)

    def get(self, name, default=None):
        return self._values.get(name, default)

    def getboolean(self, name, default=None):
        text = self.get(name)
        if text is None:
            return default
        else:
            return text.lower() in ('t', 'true', 'y', 'yes', '1')

    def getinteger(self, name, default=None):
        value = self.get(name)
        if value is None:
            return default
        else:
            return int(value)


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

    def create(self):
        self._clear()
        os.makedirs(self._dir)

    def destroy(self):
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

    def __enter__(self):
        self.create()

    def __exit__(self, exc_type, exc_value, traceback):
        self.destroy()


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


class Daemon(Base):

    class TerminateException(Exception):
        pass

    @property
    def pidfile(self):
        return self._options.pidfile

    @property
    def debug(self):
        return self._options.debug

    def __init__(self):
        super(Daemon, self).__init__()
        self._umask = 0o022

    def check(
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

    def daemonAsExternalProcess(
        self,
        executable,
        args,
        env,
        stopTime=30,
        stopInterval=1,
    ):
        self._logger.debug(
            'executing daemon: exe=%s, args=%s, env=%s',
            executable,
            args,
            env,
        )

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
                        'process terminated with status '
                        'code {code}'
                    ).format(
                        code=p.returncode,
                    )
                )

        except self.TerminateException:
            self._logger.debug('got stop signal')

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
                        _('Had to kill process {pid}').format(
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

            raise

    def _daemon(self):

        self._logger.debug('daemon entry pid=%s', os.getpid())
        self._logger.debug('background=%s', self._options.background)

        os.umask(self._umask)

        self.daemonSetup()

        stdout, stderr = (sys.stdout, sys.stderr)
        if self._options.redirectOutput:
            stdout, stderr = self.daemonStdHandles()

        def _myterm(signum, frame):
            raise self.TerminateException()

        with daemon.DaemonContext(
            detach_process=self._options.background,
            signal_map={
                signal.SIGTERM: _myterm,
                signal.SIGINT: _myterm,
                signal.SIGHUP: None,
            },
            stdout=stdout,
            stderr=stderr,
            umask=0o022,
        ):
            self._logger.debug('I am a daemon %s', os.getpid())

            try:
                with PidFile(self._options.pidfile):
                    self.daemonContext()
            except self.TerminateException:
                self._logger.debug('Terminated normally %s', os.getpid())
            finally:
                self.daemonCleanup()

        self._logger.debug('daemon return')

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
        parser.add_option(
            '--redirect-output',
            dest='redirectOutput',
            action='store_true',
            default=False,
            help=_('Redirect output of daemon'),
        )
        (self._options, args) = parser.parse_args()

        if self._options.debug:
            logging.getLogger('ovirt').setLevel(logging.DEBUG)

        if not self._options.redirectOutput:
            h = logging.StreamHandler()
            h.setLevel(logging.DEBUG)
            h.setFormatter(
                logging.Formatter(
                    fmt=(
                        os.path.splitext(os.path.basename(sys.argv[0]))[0] +
                        '[%(process)s] '
                        '%(levelname)s '
                        '%(funcName)s:%(lineno)d '
                        '%(message)s'
                    ),
                ),
            )
            logging.getLogger('ovirt').addHandler(h)

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
            self._daemon()
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

    def daemonSetup(self):
        """Setup environment
        Called before daemon context
        """
        pass

    def daemonStdHandles(self):
        """Return handles for daemon context"""
        return (sys.stdout, sys.stderr)

    def daemonContext(self):
        """Daemon logic
        Called within daemon context
        """
        pass

    def daemonCleanup(self):
        """Cleanup"""
        pass


# vim: expandtab tabstop=4 shiftwidth=4
