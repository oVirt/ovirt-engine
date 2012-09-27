#!/usr/bin/python
#
# Copyright 2012 Red Hat, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
#
# Refer to the README and COPYING files for full details of the license
#

import os
import sys
import logging
import time
import traceback

import yum


from yum.rpmtrans import RPMBaseCallback
from yum.callbacks import PT_MESSAGES, PT_DOWNLOAD_PKGS
from yum.Errors import YumBaseError
from yum.callbacks import DownloadBaseCallback


class MiniYum(object):

    class _loghandler(logging.Handler):
        """Required for extracting yum log output."""

        def __init__(self, sink):
            logging.Handler.__init__(self)
            self._sink = sink

        def emit(self, record):
            if self._sink is not None:
                self._sink.verbose(record.getMessage())

    class _yumlogger(logging.Logger):
        """Required for hacking yum log."""

        _sink = None

        def __init__(self, name, level=logging.NOTSET):
            logging.Logger.__init__(self, name, level)

        def addHandler(self, hdlr):
            if self.name.startswith('yum') or self.name.startswith('rhsm'):
                self.handlers = []
                logging.Logger.addHandler(
                    self,
                    MiniYum._loghandler(
                        MiniYum._yumlogger._sink
                    )
                )
            else:
                logging.Logger.addHandler(self, hdlr)

    class _yumlistener(object):
        """Required for extracting yum events."""

        def __init__(self, sink):
            self._sink = sink

        def event(self, event, *args, **kwargs):
            msg = "Status: "
            if event in PT_MESSAGES:
                msg += "%s" % PT_MESSAGES[event]
            else:
                msg += "Unknown(%d)" % event

            if event == PT_DOWNLOAD_PKGS:
                msg += " packages:"
                for po in args[0]:
                    msg += " " + MiniYum._get_package_name(po)

                self._sink.verbose(msg)
            else:
                self._sink.info(msg)

    class _rpmcallback(RPMBaseCallback):
        """Required for extracting rpm events."""

        def __init__(self, sink):
            RPMBaseCallback.__init__(self)
            self._sink = sink
            self._lastaction = None
            self._lastpackage = None

        def event(
            self, package, action, te_current, te_total,
            ts_current, ts_total
        ):
            if self._lastaction != action or package != self._lastpackage:
                self._lastaction = action
                self._lastpackage = package
                self._sink.info("%s: %u/%u: %s" % (
                    self.action[action], ts_current,
                    ts_total, package))

        def scriptout(self, package, msgs):
            if msgs:
                self._sink.verbose("Script sink: " + msgs)

            self._sink.verbose("Done: %s" % (package))

        def errorlog(self, msg):
            self._sink.error(msg)

        def filelog(self, package, action):
            RPMBaseCallback.filelog(self, package, action)

        def verify_txmbr(self, base, txmbr, count):
            self._sink.info(
                "Verify: %u/%u: %s" % (
                    count,
                    len(base.tsInfo),
                    txmbr
                )
            )

    class _downloadcallback(DownloadBaseCallback):
        """Required for extracting progress messages."""

        def __init__(self, sink):
            DownloadBaseCallback.__init__(self)
            self._sink = sink

        def updateProgress(self, name, frac, fread, ftime):
            msg = "Downloading: %s %s(%d%%)" % (
                name,
                fread,
                int(float(frac) * 100)
            )
            self._sink.verbose(msg)
            self._sink.keepAlive(msg)
            DownloadBaseCallback.updateProgress(self, name, frac, fread, ftime)

    class _voidsink(object):
        def verbose(self, msg):
            """verbose log.

            Keyword arguments:
            msg -- message to print

            """
            pass

        def info(self, msg):
            """info log.

            Keyword arguments:
            msg -- message to print

            """
            pass

        def error(self, msg):
            """error log.

            Keyword arguments:
            msg -- message to print

            """
            pass

        def keepAlive(self, msg):
            """keepAlive log.

            Keyword arguments:
            msg -- message to print

            """
            pass

        def askForGPGKeyImport(self, userid, hexkeyid):
            """Ask for GPG Key import.

            Keyword arguments:
            userid -- user
            hexkeyid - key id

            return True to accept.

            """
            return False

    class _disable_stdhandles(object):
        """Disable stdin/stdout/stderr

        Even after handling all logs, there are
        some tools that writes to stderr/stdout!!!
        these are not important messages, so we just
        ignore for now

        """

        def __init__(self, rfile=None):
            self._refcount = 0
            self._rstdin = os.open(os.devnull, os.O_RDONLY)
            if rfile is None:
                self._rstdout = os.open(os.devnull, os.O_WRONLY)
                self._should_close_rstdout = True
            else:
                self._rstdout = rfile.fileno()
                self._should_close_rstdout = False

        def __del__(self):
            os.close(self._rstdin)
            if self._should_close_rstdout:
                os.close(self._rstdout)

        def __enter__(self):
            self._refcount += 1
            if self._refcount == 1:
                self._oldfds = []

                for i in range(3):
                    self._oldfds.append(os.dup(i))
                    if i == 0:
                        os.dup2(self._rstdin, i)
                    else:
                        os.dup2(self._rstdout, i)

        def __exit__(self, exc_type, exc_value, traceback):
            self._refcount -= 1
            if self._refcount == 0:
                # first flush any python buffers
                sys.stdout.flush()
                sys.stderr.flush()

                for i in range(len(self._oldfds)):
                    os.dup2(self._oldfds[i], i)
                    os.close(self._oldfds[i])

    class _YumBase(yum.YumBase):
        """Require to overrde base functions."""

        def __init__(self, sink):
            yum.YumBase.__init__(self)

            self._sink = sink
            self._lastpkg = None

        def _askForGPGKeyImport(self, po, userid, hexkeyid):
            return self._sink.askForGPGKeyImport(userid, hexkeyid)

        def verifyPkg(self, fo, po, raiseError):
            if self._lastpkg != po:
                self._lastpkg = po
                self._sink.info(
                    "Download/Verify: %s" % MiniYum._get_package_name(po)
                )
            yum.YumBase.verifyPkg(self, fo, po, raiseError)

    class _MiniYumTransaction(object):
        def __init__(self, managed):
            self._managed = managed

        def __enter__(self):
            self._managed._beginTransaction()

        def __exit__(self, exc_type, exc_value, traceback):
            self._managed._endTransaction(exc_type is not None)

    @staticmethod
    def _get_package_name(po):
        return "%s-%s%s-%s.%s" % (
            po.name,
            "%s:" % po.epoch if po.epoch == 0 else "",
            po.version,
            po.release,
            po.arch
        )

    @staticmethod
    def _get_package_info(po):
        info = {}
        info['display_name'] = MiniYum._get_package_name(po)
        for f in (
            'name',
            'version',
            'release',
            'epoch',
            'arch'
        ):
            info[f] = getattr(po, f)
        return info

    @staticmethod
    def setup_log_hook(sink=None):
        """logging hack for yum.

        Keyword arguments:
        sink -- callback sink (default None)

        Yum packages uses logging package
        intensively, but we have no clue which
        log is used.
        What we have done in constructor should have
        redirect all output to us.
        However, its lazy initialization of the
        log handlers, diverse some output to its own
        handlers.
        So we set our own class to remove the hostile
        handlers for the yum loggers.

        Maybe someday this code can be removed.

        Tested: rhel-6.3

        """
        MiniYum._yumlogger._sink = sink
        logging.setLoggerClass(MiniYum._yumlogger)

    def _beginTransaction(self):
        """Lock (begin of transaction)

        Need to disbale output as:
            Freeing read locks for locker 0x84: 1316/139849637029632
            Freeing read locks for locker 0x86: 1316/139849637029632

        """
        with self._disableOutput:
            self._transactionBase = self._yb.history.last()
            self._yb.doLock()

    def _endTransaction(self, rollback=False):
        """Unlock (end of transaction)."""
        with self._disableOutput:
            try:
                if rollback:
                    self._sink.verbose("Performing rollback")
                    transactionCurrent = self._yb.history.last(
                        complete_transactions_only=False
                    )
                    if (
                        transactionCurrent is not None and
                        self._transactionBase.tid < transactionCurrent.tid
                    ):
                        if (
                            transactionCurrent.altered_lt_rpmdb or
                            transactionCurrent.altered_gt_rpmdb
                        ):
                            # @ALON:
                            # copied from yum processing, don't fully
                            # understand the statement, looks some kind
                            # of safe guard.
                            pass
                        else:
                            try:
                                self._yb.repos.populateSack(
                                    mdtype='all',
                                    cacheonly=1
                                )
                                del self._yb.tsInfo
                                if self._yb.history_undo(transactionCurrent):
                                    if self.buildTransaction():
                                        self.processTransaction()
                            finally:
                                self._yb.repos.populateSack(
                                    mdtype='all',
                                    cacheonly=0
                                )

            except:
                self._sink.error('Transaction end failed: %s' % (
                    traceback.format_exc()
                ))
            finally:
                self._transactionBase = None

                # forget current transaction
                del self._yb.tsInfo
                self._yb.doUnlock()

    def _queue(self, action, call, packages, ignoreErrors=False):
        ret = True

        with self._disableOutput:
            for package in packages:
                try:
                    self._sink.verbose(
                        "queue package %s for %s" % (package, action)
                    )
                    call(name=package)
                    self._sink.verbose("package %s queued" % package)
                except YumBaseError as e:
                    ret = False
                    self._sink.error(
                        "cannot queue package %s: %s" % (package, e)
                    )

                    if not ignoreErrors:
                        raise

                except Exception as e:
                    self._sink.error(
                        "cannot queue package %s: %s" % (package, e)
                    )
                    raise

        return ret

    def __init__(self, sink=None, extraLog=None):
        """Constructor.

        Keyword arguments:
        sink -- sink to use for interaction.
        extraLog -- a File object for stdout/stderr redirection.

        Notes:
        extraLog is required in order to collect noise output
        of yum going into stdout/stderr directly.

        """
        try:
            if sink is None:
                self._sink = MiniYum._voidsink()
            else:
                self._sink = sink

            self._disableOutput = MiniYum._disable_stdhandles(rfile=extraLog)

            with self._disableOutput:
                self._yb = MiniYum._YumBase(self._sink)

                self._yb.repos.setProgressBar(
                    MiniYum._downloadcallback(self._sink)
                )

            for l in ('yum', 'rhsm'):
                log = logging.getLogger(l)
                log.propagate = False
                log.handlers = []
                log.addHandler(
                    MiniYum._loghandler(self._sink)
                )

        except Exception as e:
            self._sink.error(e)

    def selinux_role(self):
        """Setup proper selinux role.

        this must be called at beginning of process
        to adjust proper roles for selinux.
        it will re-execute the process with same arguments.

        This has similar effect of:
        # chcon -t rpm_exec_t executable.py

        We must do this dynamic as this class is to be
        used at bootstrap stage, so we cannot put any
        persistent selinux policy changes, and have no clue
        if filesystem where we put scripts supports extended
        attributes, or if we have proper role for chcon.

        """

        try:
            selinux = __import__('selinux', globals(), locals(), [], -1)
        except ImportError:
            with self.transaction():
                self.install(['libselinux-python'])
                if self.buildTransaction():
                    self.processTransaction()

        if not 'selinux' in globals():
            selinux = __import__('selinux', globals(), locals(), [], -1)
        if selinux.is_selinux_enabled() and "MINIYUM_2ND" not in os.environ:
            env = os.environ.copy()
            env["MINIYUM_2ND"] = "1"
            rc, ctx = selinux.getcon()
            if rc != 0:
                raise Exception("Cannot get selinux context")
            ctx1 = selinux.context_new(ctx)
            if not ctx1:
                raise Exception("Cannot create selinux context")
            if selinux.context_type_set(ctx1, 'rpm_t') != 0:
                raise Exception("Cannot set type within selinux context")
            if selinux.context_role_set(ctx1, 'system_r') != 0:
                raise Exception("Cannot set role within selinux context")
            if selinux.context_user_set(ctx1, 'unconfined_u') != 0:
                raise Exception("Cannot set user within selinux context")
            if selinux.setexeccon(selinux.context_str(ctx1)) != 0:
                raise Exception("Cannot set selinux exec context")
            os.execve(sys.executable, [sys.executable] + sys.argv, env)
            os._exit(1)

    def transaction(self):
        """Manage transaction.

        Usage:
            with miniyum.transaction():
                do anything
        """
        return MiniYum._MiniYumTransaction(self)

    def clean(self, what):
        """Clean yum data."""

        try:
            doall = 'all' in what

            with self._disableOutput:
                for w, f in (
                    ('metadata', self._yb.cleanMetadata),
                    ('packages', self._yb.cleanPackages),
                    ('sqlite', self._yb.cleanSqlite),
                    ('expire-cache', self._yb.cleanExpireCache),
                    ('rpmdb', self._yb.cleanRpmDB),
                    ('headers', self._yb.cleanHeaders)
                ):
                    if doall or w in what:
                        f()

        except Exception as e:
            self._sink.error(e)
            raise

    def install(self, packages, **kwargs):
        """Install packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue("install", self._yb.install, packages, **kwargs)

    def update(self, packages, **kwargs):
        """Update packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue("update", self._yb.update, packages, **kwargs)

    def installUpdate(self, packages, **kwargs):
        """Install or update packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return (
            self.install(packages, **kwargs) or
            self.update(packages, **kwargs)
        )

    def remove(self, packages, **kwargs):
        """Remove packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue("remove", self._yb.remove, packages, **kwargs)

    def buildTransaction(self):
        """Build transaction.

        returns False if empty.

        """
        try:
            with self._disableOutput:
                ret = False
                self._sink.verbose("Building transaction")
                rc, msg = self._yb.buildTransaction()
                if rc == 0:
                    self._sink.verbose("Empty transaction")
                elif rc == 2:
                    ret = True
                    self._sink.verbose("Transaction built")
                else:
                    raise YumBaseError(msg)

                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def queryTransaction(self):
        try:
            with self._disableOutput:
                ret = []
                self._yb.tsInfo.makelists()
                for op, l in (
                    ('install', self._yb.tsInfo.installed),
                    ('update', self._yb.tsInfo.updated),
                    ('install', self._yb.tsInfo.depinstalled),
                    ('update', self._yb.tsInfo.depupdated),
                ):
                    for p in l:
                        info = MiniYum._get_package_info(p)
                        info['operation'] = op
                        ret.append(info)
                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def queryPackages(self, pkgnarrow='all', patterns=None):
        try:
            with self._disableOutput:
                ret = []
                holder = self._yb.doPackageLists(
                    pkgnarrow=pkgnarrow,
                    patterns=patterns
                )
                for op, l in (
                    ('available', holder.available),
                    ('installed', holder.installed),
                    ('updates', holder.updates),
                    ('extras', holder.extras),
                    ('obsoletes', holder.obsoletes),
                    ('recent', holder.recent)
                ):
                    for entry in l:
                        if isinstance(entry, tuple):
                            info = MiniYum._get_package_info(entry[0])
                            info['operation'] = op
                            ret.append(info)
                            info = MiniYum._get_package_info(entry[1])
                            info['operation'] = 'installed'
                            ret.append(info)
                        else:
                            info = MiniYum._get_package_info(entry)
                            info['operation'] = op
                            ret.append(info)

                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def queryLocalCachePackages(self, patterns=None):
        try:
            with self._disableOutput:
                return [
                    MiniYum._get_package_info(p)
                    for p in self._yb.pkgSack.returnPackages(patterns=patterns)
                ]

        except Exception as e:
            self._sink.error(e)
            raise

    def processTransaction(self):
        """Process built transaction."""

        try:
            with self._disableOutput:
                self._sink.verbose("Processing transaction")
                self._yb.processTransaction(
                    callback=MiniYum._yumlistener(sink=self._sink),
                    rpmTestDisplay=MiniYum._rpmcallback(sink=self._sink),
                    rpmDisplay=MiniYum._rpmcallback(sink=self._sink)
                )
                self._sink.verbose("Transaction processed")

        except Exception as e:
            self._sink.error(e)
            raise


class example(object):
    class myminiyumsink(object):

        KEEPALIVE_INTERVAL = 60

        def __init__(self):
            """dup the stdout as during yum operation so we redirect it."""
            self._stream = os.dup(sys.stdout.fileno())
            self._touch()

        def __del__(self):
            os.close(self._stream)

        def _touch(self):
            self._last = time.time()

        def verbose(self, msg):
            os.write(self._stream, ("VERB: -->%s<--\n" % msg).encode('utf-8'))

        def info(self, msg):
            self._touch()
            os.write(self._stream, ("OK:   -->%s<--\n" % msg).encode('utf-8'))

        def error(self, msg):
            self._touch()
            os.write(self._stream, ("FAIL: -->%s<--\n" % msg).encode('utf-8'))

        def keepAlive(self, msg):
            if time.time() - self._last >= \
                    example.myminiyumsink.KEEPALIVE_INTERVAL:
                self.info(msg)

        def askForGPGKeyImport(self, userid, hexkeyid):
            os.write(
                self._stream,
                (
                    "APPROVE-GPG: -->%s-%s<--\n" % (userid, hexkeyid)
                ).encode('utf-8')
            )
            return True

    @staticmethod
    def main():
        # BEGIN: PROCESS-INITIALIZATION
        miniyumsink = example.myminiyumsink()
        MiniYum.setup_log_hook(sink=miniyumsink)
        extraLog = open("/tmp/miniyum.log", "a")
        miniyum = MiniYum(sink=miniyumsink, extraLog=extraLog)
        miniyum.selinux_role()
        # END: PROCESS-INITIALIZATION

        with miniyum.transaction():
            miniyum.clean(['expire-cache'])

        miniyumsink.info("Search Summary:")
        for p in miniyum.queryPackages(patterns=['vdsm']):
            miniyumsink.info("    %s - %s" % (
                p['operation'],
                p['display_name']
            ))

        with miniyum.transaction():
            miniyum.remove(('cman',), ignoreErrors=True)
            miniyum.install(('qemu-kvm-tools',))
            miniyum.installUpdate(('vdsm', 'vdsm-cli'))
            if miniyum.buildTransaction():
                miniyumsink.info("Transaction Summary:")
                for p in miniyum.queryTransaction():
                    miniyumsink.info("    %s - %s" % (
                        p['operation'],
                        p['display_name']
                    ))
                miniyum.processTransaction()

        class IgnoreMe(Exception):
            pass
        try:
            with miniyum.transaction():
                miniyum.install(('pcsc-lite',))
                if miniyum.buildTransaction():
                    miniyum.processTransaction()
                raise IgnoreMe()
        except IgnoreMe:
            pass

if __name__ == "__main__":
    example.main()
