#!/usr/bin/python
#
# otopi -- plugable installer
# Copyright (C) 2012-2013 Red Hat, Inc.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
#


"""Minimalist yum API interaction."""


import os
import sys
import logging
import time
import traceback
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='otopi')


import yum
import yum.rpmtrans
import yum.callbacks
import yum.Errors
import yum.callbacks
import yum.constants


class MiniYumSinkBase(object):
    """Sink base."""

    @property
    def failed(self):
        return self._failed

    def clearError(self):
        self._failed = False

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
        self._failed = True

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

    def reexec(self):
        """Last chance before reexec."""
        pass


class MiniYum(object):
    """Minimalist yum API interaction."""

    TRANSACTION_STATE = {
        yum.constants.TS_UPDATE: _('update'),
        yum.constants.TS_INSTALL: _('install'),
        yum.constants.TS_TRUEINSTALL: _('trueinstall'),
        yum.constants.TS_ERASE: _('erase'),
        yum.constants.TS_OBSOLETED: _('obsoleted'),
        yum.constants.TS_OBSOLETING: _('obsoleting'),
        yum.constants.TS_AVAILABLE: _('available'),
        yum.constants.TS_UPDATED: _('updated'),
        'repackaging': _('repackaging'),
    }

    class _LogHandler(logging.Handler):
        """Required for extracting yum log output."""

        def __init__(self, sink):
            logging.Handler.__init__(self)
            self._sink = sink

        def emit(self, record):
            if self._sink is not None:
                self._sink.verbose(record.getMessage())

    class _YumLogger(logging.Logger):
        """Required for hacking yum log."""

        _sink = None

        def __init__(self, name, level=logging.NOTSET):
            logging.Logger.__init__(self, name, level)

        def addHandler(self, hdlr):
            if self.name.startswith('yum') or self.name.startswith('rhsm'):
                self.handlers = []
                logging.Logger.addHandler(
                    self,
                    MiniYum._LogHandler(
                        MiniYum._YumLogger._sink
                    )
                )
            else:
                logging.Logger.addHandler(self, hdlr)

    class _YumListener(object):
        """Required for extracting yum events."""

        def __init__(self, sink):
            self._sink = sink

        def event(self, event, *args, **kwargs):
            if event in yum.callbacks.PT_MESSAGES:
                msg = '%s' % yum.callbacks.PT_MESSAGES[event]
            elif event == yum.callbacks.PT_DOWNLOAD_PKGS:
                msg = _('Download packages')
            else:
                msg = _('Unknown({event})').format(event=event)
            entry = _('Status: {message}').format(message=msg)

            if event == yum.callbacks.PT_DOWNLOAD_PKGS:
                entry += _(' packages:')
                for po in args[0]:
                    entry += ' ' + MiniYum._get_package_name(po)

                self._sink.verbose(entry)
            else:
                self._sink.info(entry)

    class _RPMCallback(yum.rpmtrans.RPMBaseCallback):
        """Required for extracting rpm events."""

        def __init__(self, sink):
            yum.rpmtrans.RPMBaseCallback.__init__(self)
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

                #
                # NOTE:
                # do not use self.action as it is encoded
                # using invalid encoding, some unicode proprietary
                # for yum.
                # test using LC_ALL=cs_CZ.utf8, LANG=cs_CZ
                #
                self._sink.info(
                    _('{action}: {count}/{total}: {package}').format(
                        action=MiniYum.TRANSACTION_STATE.get(action, action),
                        count=ts_current,
                        total=ts_total,
                        package=package.name,
                    )
                )

        def scriptout(self, package, msgs):
            if msgs:
                self._sink.verbose('Script sink: ' + msgs)

            self._sink.verbose('Done: %s' % (package))

        def errorlog(self, msg):
            self._sink.error(msg)

        def filelog(self, package, action):
            yum.rpmtrans.RPMBaseCallback.filelog(self, package, action)

        def verify_txmbr(self, base, txmbr, count):
            self._sink.info(
                _('Verify: {count}/{total}: {member}').format(
                    count=count,
                    total=len(base.tsInfo),
                    member=txmbr,
                )
            )

    class _DownloadCallback(yum.callbacks.DownloadBaseCallback):
        """Required for extracting progress messages."""

        def __init__(self, sink):
            yum.callbacks.DownloadBaseCallback.__init__(self)
            self._sink = sink

        def updateProgress(self, name, frac, fread, ftime):
            msg = _('Downloading: {package} {count}({percent}%)').format(
                package=name,
                count=fread,
                percent=int(float(frac) * 100)
            )
            self._sink.verbose(msg)
            self._sink.keepAlive(msg)
            yum.callbacks.DownloadBaseCallback.updateProgress(
                self,
                name,
                frac,
                fread,
                ftime
            )

    class _VoidSink(MiniYumSinkBase):
        def __init__(self):
            super(MiniYum._VoidSink, self).__init__()

    class _HandleStdHandlesBase(object):
        def __init__(self):
            pass

        def __enter__(self):
            pass

        def __exit__(self, exc_type, exc_value, traceback):
            pass

    class _HandleStdHandles(_HandleStdHandlesBase):
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
                for stream in (sys.stdout, sys.stderr):
                    # tty [at least] gets errors
                    stream.flush()
                    try:
                        os.fsync(stream.fileno())
                    except OSError:
                        pass

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
                    _('Download/Verify: {package}').format(
                        package=MiniYum._get_package_name(po),
                    )
                )
            yum.YumBase.verifyPkg(self, fo, po, raiseError)

    class _MiniYumTransaction(object):
        def __init__(self, managed):
            self._managed = managed

        def __enter__(self):
            self._managed.beginTransaction()

        def __exit__(self, exc_type, exc_value, traceback):
            self._managed.endTransaction(rollback=exc_type is not None)

    @classmethod
    def _get_package_name(clz, po):
        return '%s-%s%s-%s.%s' % (
            po.name,
            '%s:' % po.epoch if po.epoch == 0 else '',
            po.version,
            po.release,
            po.arch
        )

    @classmethod
    def _get_package_info(clz, po):
        info = {}
        info['display_name'] = clz._get_package_name(po)
        for f in (
            'name',
            'version',
            'release',
            'epoch',
            'arch'
        ):
            info[f] = getattr(po, f)
        return info

    @classmethod
    def setup_log_hook(clz, sink=None):
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
        clz._YumLogger._sink = sink
        logging.setLoggerClass(clz._YumLogger)

    def _queueGroup(self, action, call, group, ignoreErrors=False):
        ret = True

        with self._disableOutput:
            try:
                self._sink.verbose(
                    'queue group %s for %s' % (group, action)
                )
                call(grpid=group)
                self._sink.verbose('group %s queued' % group)
            except yum.Errors.YumBaseError as e:
                ret = False
                self._sink.error(
                    _('Cannot queue group {group}: {error}').format(
                        group=group,
                        error=e
                    )
                )

                if not ignoreErrors:
                    raise

            except Exception as e:
                self._sink.error(
                    _('Cannot queue group {group}: {error}').format(
                        group=group,
                        error=e
                    )
                )
                raise

        return ret

    def _queue(self, action, call, packages, ignoreErrors=False):
        ret = True

        with self._disableOutput:
            for package in packages:
                try:
                    self._sink.verbose(
                        'queue package %s for %s' % (package, action)
                    )
                    call(name=package)
                    self._sink.verbose('package %s queued' % package)
                except yum.Errors.YumBaseError as e:
                    ret = False
                    self._sink.error(
                        _('Cannot queue package {package}: {error}').format(
                            package=package,
                            error=e
                        )
                    )

                    if not ignoreErrors:
                        raise

                except Exception as e:
                    self._sink.error(
                        _('Cannot queue package {package}: {error}').format(
                            package=package,
                            error=e
                        )
                    )
                    raise

        return ret

    @property
    def sink(self):
        return self._sink

    def __init__(
        self,
        sink=None,
        blockStdHandles=True,
        extraLog=None,
        disabledPlugins=None,
        enabledPlugins=None,
    ):
        """Constructor.

        Keyword arguments:
        sink -- sink to use for interaction.
        extraLog -- a File object for stdout/stderr redirection.

        Notes:
        extraLog is required in order to collect noise output
        of yum going into stdout/stderr directly.

        """
        try:
            self._yb = None

            if sink is None:
                self._sink = self._VoidSink()
            else:
                self._sink = sink

            if blockStdHandles:
                self._disableOutput = self._HandleStdHandles(rfile=extraLog)
            else:
                self._disableOutput = self._HandleStdHandlesBase()

            with self._disableOutput:
                self._yb = self._YumBase(self._sink)
                if disabledPlugins is not None:
                    self._yb.preconf.disabled_plugins = disabledPlugins
                if enabledPlugins is not None:
                    self._yb.preconf.enabled_plugins = enabledPlugins

                #
                # DO NOT use async which is the
                # hardcoded default as we will not
                # be able to monitor progress
                #
                from urlgrabber import grabber
                if hasattr(grabber, 'parallel_wait'):
                    for repo in self._yb.repos.listEnabled():
                        repo._async = False

                #
                # Set progress bar hook, useless if
                # async/parallel is enabled.
                #
                self._yb.repos.setProgressBar(
                    self._DownloadCallback(self._sink)
                )

            for l in ('yum', 'rhsm'):
                log = logging.getLogger(l)
                log.propagate = False
                log.handlers = []
                log.addHandler(
                    self._LogHandler(self._sink)
                )

        except Exception as e:
            self._sink.error(e)

    def __del__(self):
        """Destructor"""
        if self._yb is not None:
            del self._yb
            self._yb = None

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
            import selinux
        except ImportError:
            with self.transaction():
                self.install(['libselinux-python'])
                if self.buildTransaction():
                    self.processTransaction()
            #
            # on fedora-18 for example
            # the selinux core is updated
            # so we fail resolving symbols
            # solution is re-execute the process
            # after installation.
            #
            self._sink.reexec()
            os.execv(sys.executable, [sys.executable] + sys.argv)
            os._exit(1)

        if selinux.is_selinux_enabled():
            rc, ctx = selinux.getcon()
            if rc != 0:
                raise Exception(_('Cannot get selinux context'))
            ctx1 = selinux.context_new(ctx)
            if not ctx1:
                raise Exception(_('Cannot create selinux context'))
            if selinux.context_role_get(ctx1) != 'system_r':
                if selinux.context_role_set(ctx1, 'system_r') != 0:
                    raise Exception(
                        _('Cannot set role within selinux context')
                    )
                if selinux.setexeccon(selinux.context_str(ctx1)) != 0:
                    raise Exception(
                        _('Cannot set selinux exec context')
                    )
                self._sink.reexec()
                os.execv(sys.executable, [sys.executable] + sys.argv)
                os._exit(1)

    def transaction(self):
        """Manage transaction.

        Usage:
            with miniyum.transaction():
                do anything
        """
        return self._MiniYumTransaction(self)

    def clean(self, what):
        """Clean yum data."""

        self._sink.verbose(
            _('Cleaning caches: {what}.').format(
                what=what,
            )
        )

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

    def beginTransaction(self):
        """Lock (begin of transaction)

        Need to disbale output as:
            Freeing read locks for locker 0x84: 1316/139849637029632
            Freeing read locks for locker 0x86: 1316/139849637029632

        """
        with self._disableOutput:
            self._transactionBase = self._yb.history.last()
            self._yb.doLock()

    def endTransaction(self, rollback=False):
        """Unlock (end of transaction)."""
        with self._disableOutput:
            try:
                if rollback:
                    self._sink.info('Performing yum transaction rollback')
                    transactionCurrent = self._yb.history.last(
                        complete_transactions_only=False
                    )
                    if (
                        transactionCurrent is not None and
                        self._transactionBase is not None and
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
                                    cacheonly=1,
                                )
                            except Exception as e:
                                self._sink.verbose(
                                    _(
                                        'Cannot switch to offline: {error}'
                                    ).format(
                                        error=e,
                                    )
                                )
                            try:
                                del self._yb.tsInfo
                                del self._yb.ts
                                if self._yb.history_undo(transactionCurrent):
                                    if self.buildTransaction():
                                        self.processTransaction()
                            finally:
                                try:
                                    self._yb.repos.populateSack(
                                        mdtype='all',
                                        cacheonly=0,
                                    )
                                except Exception as e:
                                    self._sink.verbose(
                                        _(
                                            'Cannot switch to online: {error}'
                                        ).format(
                                            error=e,
                                        )
                                    )

            except:
                self._sink.error(
                    _('Transaction close failed: {error}').format(
                        error=traceback.format_exc()
                    )
                )
            finally:
                self._transactionBase = None

                # forget current transaction
                del self._yb.tsInfo
                self._yb.doUnlock()

    def installGroup(self, group, **kwargs):
        """Install group.

        group -- group name
        ignoreErrors - to ignore errors, will return False

        """
        return self._queueGroup(
            'install',
            self._yb.selectGroup,
            group,
            **kwargs
        )

    def updateGroup(self, group, **kwargs):
        """Update group.

        group -- group name
        ignoreErrors - to ignore errors, will return False

        rhel-6.3 does not have the upgrade keyword parameter,
        so we just install.
        """
        return self._queueGroup(
            'update',
            self._yb.selectGroup,
            group,
            **kwargs
        )

    def removeGroup(self, group, **kwargs):
        """Update group.

        group -- group name
        ignoreErrors - to ignore errors, will return False

        rhel-6.3 does not have the upgrade keyword parameter,
        so we just install.
        """
        return self._queueGroup(
            'remove',
            self._yb.groupRemove,
            group,
            **kwargs
        )

    def install(self, packages, **kwargs):
        """Install packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue('install', self._yb.install, packages, **kwargs)

    def update(self, packages, **kwargs):
        """Update packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue('update', self._yb.update, packages, **kwargs)

    def remove(self, packages, **kwargs):
        """Remove packages.

        Keyword arguments:
        packages -- packages to install.
        ignoreErrors - to ignore errors, will return False

        """
        return self._queue('remove', self._yb.remove, packages, **kwargs)

    def buildTransaction(self):
        """Build transaction.

        returns False if empty.

        """
        try:
            with self._disableOutput:
                ret = False
                self._sink.verbose('Building transaction')
                rc, msg = self._yb.buildTransaction()
                if rc == 0:
                    self._sink.verbose('Empty transaction')
                elif rc == 2:
                    ret = True
                    self._sink.verbose('Transaction built')
                else:
                    raise yum.Errors.YumBaseError(msg)

                self._sink.verbose('Transaction Summary:')
                for p in self.queryTransaction():
                    self._sink.verbose('    %-10s - %s' % (
                        p['operation'],
                        p['display_name']
                    ))

                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def queryTransaction(self):
        try:
            with self._disableOutput:
                ret = []
                for txmbr in sorted(self._yb.tsInfo):
                    info = self._get_package_info(txmbr)
                    info['operation'] = self.TRANSACTION_STATE.get(
                        txmbr.output_state,
                        txmbr.output_state
                    )
                    ret.append(info)
                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def queryGroups(self):
        ret = []

        try:
            with self._disableOutput:
                installed, available = self._yb.doGroupLists()

                for grp in installed:
                    ret.append({
                        'operation': 'installed',
                        'name': grp.name,
                        'uservisible': grp.user_visible
                    })
                for grp in available:
                    ret.append({
                        'operation': 'available',
                        'name': grp.name,
                        'uservisible': grp.user_visible
                    })
        except yum.Errors.GroupsError as e:
            # rhbz#973383 empty groups raises an exception
            self._sink.verbose('Ignoring group error: %s' % e)
        except Exception as e:
            self._sink.error(e)
            raise

        return ret

    def queryPackages(self, pkgnarrow='all', patterns=None, showdups=None):
        try:
            with self._disableOutput:
                ret = []
                holder = self._yb.doPackageLists(
                    pkgnarrow=pkgnarrow,
                    patterns=patterns,
                    showdups=showdups,
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
                            info = self._get_package_info(entry[0])
                            info['operation'] = op
                            ret.append(info)
                            info = self._get_package_info(entry[1])
                            info['operation'] = 'installed'
                            ret.append(info)
                        else:
                            info = self._get_package_info(entry)
                            info['operation'] = op
                            ret.append(info)

                return ret

        except Exception as e:
            self._sink.error(e)
            raise

    def processTransaction(self):
        """Process built transaction."""

        self._sink.clearError()

        try:
            with self._disableOutput:
                self._sink.verbose('Processing transaction')
                self._yb.processTransaction(
                    callback=self._YumListener(sink=self._sink),
                    rpmTestDisplay=self._RPMCallback(sink=self._sink),
                    rpmDisplay=self._RPMCallback(sink=self._sink)
                )
                self._sink.verbose('Transaction processed')

        except Exception as e:
            self._sink.error(e)
            raise

        if self._sink.failed:
            raise RuntimeError(
                _('One or more elements within Yum transaction failed')
            )


class Example(object):
    """Example of miniyum usage."""

    class MyMiniYumSink(MiniYumSinkBase):
        """Events."""

        KEEPALIVE_INTERVAL = 60

        def __init__(self):
            """dup the stdout as during yum operation so we redirect it."""
            super(Example.MyMiniYumSink, self).__init__()
            self._stream = os.dup(sys.stdout.fileno())
            self._touch()

        def __del__(self):
            os.close(self._stream)

        def _touch(self):
            self._last = time.time()

        def verbose(self, msg):
            super(Example.MyMiniYumSink, self).verbose(msg)
            os.write(self._stream, ('VERB: -->%s<--\n' % msg).encode('utf-8'))

        def info(self, msg):
            super(Example.MyMiniYumSink, self).info(msg)
            self._touch()
            os.write(self._stream, ('OK:   -->%s<--\n' % msg).encode('utf-8'))

        def error(self, msg):
            super(Example.MyMiniYumSink, self).error(msg)
            self._touch()
            os.write(self._stream, ('FAIL: -->%s<--\n' % msg).encode('utf-8'))

        def keepAlive(self, msg):
            super(Example.MyMiniYumSink, self).keepAlive(msg)
            if time.time() - self._last >= \
                    self.KEEPALIVE_INTERVAL:
                self.info(msg)

        def askForGPGKeyImport(self, userid, hexkeyid):
            os.write(
                self._stream,
                (
                    'APPROVE-GPG: -->%s-%s<--\n' % (userid, hexkeyid)
                ).encode('utf-8')
            )
            return True

    @staticmethod
    def main():
        # BEGIN: PROCESS-INITIALIZATION
        miniyumsink = Example.MyMiniYumSink()
        MiniYum.setup_log_hook(sink=miniyumsink)
        extraLog = open('/tmp/miniyum.log', 'a')
        miniyum = MiniYum(sink=miniyumsink, extraLog=extraLog)
        miniyum.selinux_role()
        # END: PROCESS-INITIALIZATION

        with miniyum.transaction():
            miniyum.clean(['expire-cache'])

        miniyumsink.info('Search Summary:')
        for p in miniyum.queryPackages(patterns=['vdsm']):
            miniyumsink.info(
                _('    {operation} - {package}').format(
                    operation=p['operation'],
                    package=p['display_name'],
                )
            )

        with miniyum.transaction():
            miniyum.remove(('cman',), ignoreErrors=True)
            miniyum.install(('qemu-kvm-tools',))
            miniyum.install(('vdsm', 'vdsm-cli'))
            miniyum.update(('vdsm', 'vdsm-cli'))
            if miniyum.buildTransaction():
                miniyumsink.info('Transaction Summary:')
                for p in miniyum.queryTransaction():
                    miniyumsink.info('    %s - %s' % (
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

if __name__ == '__main__':
    Example.main()

__all__ = ['MiniYum', 'MiniYumSinkBase']


# vim: expandtab tabstop=4 shiftwidth=4
