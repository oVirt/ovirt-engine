#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#


import contextlib
import datetime
import gettext
import logging
import logging.handlers
import optparse
import os
import resource
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time

import daemon


from dateutil import tz

from . import base
from . import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


@util.export
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

        def converter(self, timestamp):
            return datetime.datetime.fromtimestamp(
                timestamp,
                tz.tzlocal()
            )

        def formatTime(self, record, datefmt=None):
            ct = self.converter(record.created)
            if datefmt:
                s = ct.strftime(datefmt, ct)
            else:
                s = "%s,%03d%s" % (
                    ct.strftime('%Y-%m-%d %H:%M:%S'),
                    record.msecs,
                    ct.strftime('%z')
                )
            return s

    logger = logging.getLogger('ovirt')
    logger.propagate = False
    if os.environ.get('OVIRT_SERVICE_DEBUG', '0') != '0':
        logger.setLevel(logging.DEBUG)
    else:
        logger.setLevel(logging.INFO)

    try:
        h = logging.handlers.SysLogHandler(
            address='/dev/log',
            facility=logging.handlers.SysLogHandler.LOG_DAEMON,
        )
        h.setLevel(logging.DEBUG)
        h.setFormatter(
            _MyFormatter(
                fmt=(
                    '%(asctime)s '
                    '{process}: '
                    '%(levelname)s '
                    '%(funcName)s:%(lineno)d '
                    '%(message)s'
                ).format(
                    process=os.path.splitext(os.path.basename(sys.argv[0]))[0],
                ),
            ),
        )
        logger.addHandler(h)
    except IOError:
        logging.debug('Cannot open syslog logger', exc_info=True)


@util.export
class TempDir(base.Base):
    """
    Temporary directory scope management

    Usage:
        with TempDir(directory):
            pass
    """

    @property
    def directory(self):
        return self._dir

    def _clear(self):
        self.logger.debug("removing directory '%s'", self._dir)
        if os.path.exists(self._dir):
            shutil.rmtree(self._dir)

    def __init__(self, dir=None):
        super(TempDir, self).__init__()
        self._dir = dir
        if self._dir is None:
            self._dir = tempfile.mkdtemp()

    def create(self):
        self._clear()
        os.makedirs(self._dir, 0o700)

    def destroy(self):
        try:
            self._clear()
        except Exception as e:
            self.logger.warning(
                _("Cannot remove directory '{directory}': {error}").format(
                    directory=self._dir,
                    error=e,
                ),
            )
            self.logger.debug('exception', exc_info=True)

    def __enter__(self):
        self.create()
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.destroy()


@util.export
class PidFile(base.Base):
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
            self.logger.debug(
                "creating pidfile '%s' pid=%s",
                self._file,
                os.getpid()
            )
            with open(self._file, 'w') as f:
                f.write('%s\n' % os.getpid())

    def __exit__(self, exc_type, exc_value, traceback):
        if self._file is not None:
            self.logger.debug("removing pidfile '%s'", self._file)
            try:
                os.remove(self._file)
            except OSError:
                # we may not have permissions to delete pid
                # so just try to empty it
                try:
                    with open(self._file, 'w'):
                        pass
                except IOError as e:
                    self.logger.error(
                        _("Cannot remove pidfile '{file}': {error}").format(
                            file=self._file,
                            error=e,
                        ),
                    )
                    self.logger.debug('exception', exc_info=True)


@util.export
class Daemon(base.Base):

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
        self.logger.debug(
            'executing daemon: exe=%s, args=%s, env=%s',
            executable,
            args,
            env,
        )

        try:
            self.logger.debug('creating process')
            p = subprocess.Popen(
                args=args,
                executable=executable,
                env=env,
                close_fds=True,
            )

            self.logger.debug(
                'waiting for termination of pid=%s',
                p.pid,
            )
            p.wait()
            self.logger.debug(
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
            self.logger.debug('got stop signal')

            # avoid recursive signals
            for sig in (signal.SIGTERM, signal.SIGINT):
                signal.signal(sig, signal.SIG_IGN)

            try:
                self.logger.debug('terminating pid=%s', p.pid)
                p.terminate()
                for i in range(stopTime // stopInterval):
                    if p.poll() is not None:
                        self.logger.debug('terminated pid=%s', p.pid)
                        break
                    self.logger.debug(
                        'waiting for termination of pid=%s',
                        p.pid,
                    )
                    time.sleep(stopInterval)
            except OSError as e:
                self.logger.warning(
                    _('Cannot terminate pid {pid}: {error}').format(
                        pid=p.pid,
                        error=e,
                    )
                )
                self.logger.debug('exception', exc_info=True)

            try:
                if p.poll() is None:
                    self.logger.debug('killing pid=%s', p.pid)
                    p.kill()
                    raise RuntimeError(
                        _('Had to kill process {pid}').format(
                            pid=p.pid
                        )
                    )
            except OSError as e:
                self.logger.warning(
                    _('Cannot kill pid {pid}: {error}').format(
                        pid=p.pid,
                        error=e
                    )
                )
                self.logger.debug('exception', exc_info=True)
                raise

            raise

    def _setLimits(self):
        self.logger.debug('Setting rlimits')
        for limit in (resource.RLIMIT_NPROC, resource.RLIMIT_NOFILE):
            soft, hard = resource.getrlimit(resource.RLIMIT_NPROC)
            resource.setrlimit(resource.RLIMIT_NPROC, (hard, hard))

    def _sd_notify_ready(self):
        """
        NOTICE: systemd-notify is not working!
        SEE: rhbz#820448
        """
        with contextlib.closing(
            socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)
        ) as s:
            e = os.getenv('NOTIFY_SOCKET')
            if e.startswith('@'):
                # abstract namespace socket
                e = '\0%s' % e[1:]
            s.connect(e)
            s.sendall(b'READY=1')

    def _daemon(self):

        self.logger.debug('daemon entry pid=%s', os.getpid())
        self.logger.debug('background=%s', self._options.background)

        os.umask(0o022)

        if self._options.systemd == 'notify':
            self._sd_notify_ready()

        self.daemonSetup()

        stdout, stderr = (sys.stdout, sys.stderr)
        if self._options.redirectOutput:
            stdout, stderr = self.daemonStdHandles()

        def _myterm(signum, frame):
            raise self.TerminateException()

        #
        # preserve log handler.
        # bit undocumented.
        #
        handles = []
        for handler in logging.getLogger('ovirt').handlers:
            if hasattr(handler, 'socket'):
                handles.append(handler.socket)

        with daemon.DaemonContext(
            detach_process=self._options.background,
            signal_map={
                signal.SIGTERM: _myterm,
                signal.SIGINT: _myterm,
                signal.SIGHUP: None,
            },
            stdout=stdout,
            stderr=stderr,
            files_preserve=handles,
            umask=0o022,
        ):
            self.logger.debug('I am a daemon %s', os.getpid())

            self._setLimits()

            try:
                with PidFile(self._options.pidfile):
                    self.daemonContext()
                self.logger.debug('Returned normally %s', os.getpid())
            except self.TerminateException:
                self.logger.debug('Terminated normally %s', os.getpid())
            finally:
                self.daemonCleanup()

        self.logger.debug('daemon return')

    def run(self):
        self.logger.debug('startup args=%s', sys.argv)

        parser = optparse.OptionParser(
            usage=_('Usage: %prog [options] start'),
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
            '--systemd',
            dest='systemd',
            default='simple',
            choices=['simple', 'notify'],
            help=_('Systemd type simple|notify'),
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
        if action not in ('start'):
            parser.error(
                _("Invalid action '{action}'").format(
                    action=action
                )
            )

        try:
            self._daemon()
        except Exception as e:
            self.logger.error(
                _('Error: {error}').format(
                    error=e,
                )
            )
            self.logger.debug('exception', exc_info=True)
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
        null = open(os.devnull, 'w')
        return (null, null)

    def daemonContext(self):
        """Daemon logic
        Called within daemon context
        """
        pass

    def daemonCleanup(self):
        """Cleanup"""
        pass


# vim: expandtab tabstop=4 shiftwidth=4
