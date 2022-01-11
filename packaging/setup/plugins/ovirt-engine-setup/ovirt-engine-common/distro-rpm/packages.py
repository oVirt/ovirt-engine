#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Package upgrade plugin.
"""

import datetime
import gettext
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Package upgrade plugin.
    """

    class VersionLockTransaction(transaction.TransactionElement):
        """
        version lock transaction element.
        Not that this is real transaction, but we need to
        rollback/commit same as packager.
        We cannot actually prepare the transaction at preparation
        because new packages are not installed.
        But we must restore file as we do not know what packages
        were locked at previous version.
        """

        _VERSIONLOCK_LIST_FILES = (
            osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
            osetupcons.FileLocations.OVIRT_ENGINE_DNF_VERSIONLOCK,
        )

        def _filterVersionLock(self):
            modified = {}
            content = {}
            for versionlock_list_file in self._VERSIONLOCK_LIST_FILES:
                modified[versionlock_list_file] = False
                content[versionlock_list_file] = []
                if os.path.exists(versionlock_list_file):
                    with open(versionlock_list_file, 'r') as f:
                        for line in f.read().splitlines():
                            found = False
                            for pattern in self.environment[
                                osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
                            ]:
                                if line.find(pattern) != -1:
                                    found = True
                                    break
                            if not found:
                                content[versionlock_list_file].append(line)
                            else:
                                modified[versionlock_list_file] = True
            return (modified, content)

        @property
        def environment(self):
            return self._parent.environment

        def __init__(self, parent):
            self._parent = parent
            self._backup = {}

        def __str__(self):
            return _("Version Lock Transaction")

        def prepare(self):
            if not self._parent._enabled:
                return

            modified, content = self._filterVersionLock()
            for versionlock_list_file in self._VERSIONLOCK_LIST_FILES:
                if modified[versionlock_list_file]:
                    self._backup[versionlock_list_file] = '%s.%s' % (
                        versionlock_list_file,
                        datetime.datetime.now().strftime('%Y%m%d%H%M%S'),
                    )
                    os.rename(
                        versionlock_list_file,
                        self._backup[versionlock_list_file],
                    )
                    with open(
                        versionlock_list_file,
                        'w'
                    ) as f:
                        f.write(
                            '\n'.join(content[versionlock_list_file]) + '\n'
                        )

        def abort(self):
            for versionlock_list_file in self._VERSIONLOCK_LIST_FILES:
                if (
                    versionlock_list_file in self._backup and
                    os.path.exists(self._backup[versionlock_list_file])
                ):
                    os.rename(
                        self._backup[versionlock_list_file],
                        versionlock_list_file,
                    )

        def commit(self):
            # This must be always execucted so we be sure we
            # are locked

            # execute rpm directly
            # yum is not good in offline usage
            if self.environment[osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY]:
                rc, out, err = self._parent.execute(
                    args=(
                        self._parent.command.get('rpm'),
                        '-q',
                    ) + tuple(
                        set(
                            self.environment[
                                osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY
                            ]
                        )
                    ),
                )
                changes = []
                for line in out:
                    changes.append(
                        {
                            'added': line,
                        }
                    )

                versionlock_uninstall_group = self.environment[
                    osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
                ].createGroup(
                    group='versionlock',
                    description='YUM version locking configuration',
                    optional=False
                )

                modified, content = self._filterVersionLock()
                for versionlock_list_file in self._VERSIONLOCK_LIST_FILES:
                    self.environment[
                        osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
                    ].append(versionlock_list_file)
                    if os.path.exists(versionlock_list_file):
                        versionlock_uninstall_group.addChanges(
                            'versionlock',
                            versionlock_list_file,
                            changes,
                        )
                        content[versionlock_list_file].extend(out)
                        content[versionlock_list_file] = [
                            v for v in content[versionlock_list_file] if v
                        ]
                        with open(
                            versionlock_list_file,
                            'w',
                        ) as f:
                            f.write(
                                '\n'.join(
                                    content[versionlock_list_file]
                                ) + '\n'
                            )

    def _getSink(self):
        pm = self._PM

        class MyPMSink(self._MiniPMSinkBase):

            def __init__(self, log):
                super(MyPMSink, self).__init__()
                self._log = log

            def verbose(self, msg):
                super(MyPMSink, self).verbose(msg)
                self._log.debug('%s %s', pm, msg)

            def info(self, msg):
                super(MyPMSink, self).info(msg)
                self._log.info('%s %s', pm, msg)

            def error(self, msg):
                super(MyPMSink, self).error(msg)
                self._log.error('%s %s', pm, msg)
        return MyPMSink(self.logger)

    def _checkForPackagesUpdate(self, packages):
        update = []
        mpm = self._MiniPM(
            sink=self._getSink(),
            disabledPlugins=('versionlock',),
        )
        for package in packages:
            with mpm.transaction():
                mpm.update(packages=(package,))
                if mpm.buildTransaction():
                    if mpm.queryTransaction():
                        update.append(package)

        return update

    def _checkForProductUpdate(self):
        mpm = self._MiniPM(
            sink=self._getSink(),
            disabledPlugins=('versionlock',),
        )
        packages = [
            package
            for entry in self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ]
            for package in entry['packages']
        ]
        res = mpm.checkForSafeUpdate(packages)
        plist = res['packageOperations']
        upgradeAvailable = res['upgradeAvailable']
        missingRollback = res['missingRollback']
        return (upgradeAvailable, missingRollback, plist)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._shouldResultVersionLock = False
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RPMDistroEnv.ENABLE_UPGRADE,
            None
        )
        self.environment.setdefault(
            osetupcons.RPMDistroEnv.REQUIRE_ROLLBACK,
            None
        )
        self.environment.setdefault(
            osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY,
            []
        )
        self.environment.setdefault(
            osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER,
            []
        )
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ] = []
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_SETUP
        ] = []
        self._plist = None

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            osetuputil.is_ovirt_packaging_supported_distro()
        ),
    )
    def _setup(self):
        self.command.detect('rpm')

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.VersionLockTransaction(
                parent=self,
            )
        )

        if not self.environment[
            osetupcons.CoreEnv.OFFLINE_PACKAGER
        ]:
            self._PM, self._MiniPM, self._MiniPMSinkBase = (
                osetuputil.getPackageManager(self.logger)
            )
            self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PACKAGES,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PACKAGES,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        # assume we have nothing to do
        self._enabled = False

        upgradeAvailable = None
        missingRollback = None

        if self.environment[osetupcons.RPMDistroEnv.ENABLE_UPGRADE] is None:
            self.logger.info(_('Checking for product updates...'))
            (
                upgradeAvailable,
                missingRollback,
                self._plist,
            ) = self._checkForProductUpdate()

            if not upgradeAvailable:
                self.logger.info(_('No product updates found'))
            else:
                self.environment[
                    osetupcons.RPMDistroEnv.ENABLE_UPGRADE
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_RPMDISTRO_PACKAGE_UPGRADE',
                    note=_(
                        'Setup needs to install or update the following '
                        'packages:\n'
                        '{plist}\n'
                        'Replying "No" will abort Setup. You can pass the '
                        'option "--offline" to prevent installing or updating '
                        'packages.\n'
                        'Do you wish to update them now? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        plist='\n'.join(self._arrangedPackageList(self._plist))
                    ),
                    prompt=True,
                    true=_('Yes'),
                    false=_('No'),
                    default=True,
                )

        if self.environment[osetupcons.RPMDistroEnv.ENABLE_UPGRADE]:
            self.logger.info(_('Checking for an update for Setup...'))
            update = self._checkForPackagesUpdate(
                packages=self.environment[
                    osetupcons.RPMDistroEnv.PACKAGES_SETUP
                ],
            )
            if not update:
                self.dialog.note(text=_('No update for Setup found'))
            else:
                self.dialog.note(
                    text=_(
                        'An update for the Setup packages {packages} was '
                        'found. Please update that package by running:\n'
                        '"{pm} update {packages}"\nand then execute Setup '
                        'again.'
                    ).format(
                        pm=self._PM.lower(),
                        packages=' '.join(update),
                    ),
                )
                raise RuntimeError(_('Please update the Setup packages'))

            if upgradeAvailable is None:
                self.logger.info(_('Checking for product updates...'))
                (
                    upgradeAvailable,
                    missingRollback,
                    self._plist,
                ) = self._checkForProductUpdate()

            if upgradeAvailable:
                if missingRollback:
                    if self.environment[
                        osetupcons.RPMDistroEnv.REQUIRE_ROLLBACK
                    ] is None:
                        self.environment[
                            osetupcons.RPMDistroEnv.REQUIRE_ROLLBACK
                        ] = dialog.queryBoolean(
                            dialog=self.dialog,
                            name='OVESETUP_RPMDISTRO_REQUIRE_ROLLBACK',
                            note=_(
                                'Setup will not be able to rollback new '
                                'packages in case of a failure, because '
                                'the following installed packages were not '
                                'found in enabled repositories:\n\n'
                                '{missingRollback}\n'
                                'Do you want to abort Setup? '
                                '(@VALUES@) [@DEFAULT@]: '
                            ).format(
                                missingRollback='\n'.join(
                                    list(missingRollback)
                                ),
                            ),
                            prompt=True,
                            true=_('Yes'),
                            false=_('No'),
                            default=True,
                        )

                    if self.environment[
                        osetupcons.RPMDistroEnv.REQUIRE_ROLLBACK
                    ]:
                        raise RuntimeError(
                            _('Package rollback information is unavailable')
                        )

                    #
                    # Disable yum rollback on transaction failure
                    # as rhel yum will remove packages that were updated
                    # without installing previous ones.
                    #
                    self.environment[
                        otopicons.PackEnv.YUM_ROLLBACK
                    ] = False

                self._enabled = self.environment[
                    osetupcons.RPMDistroEnv.ENABLE_UPGRADE
                ]

        if not self._enabled and upgradeAvailable:
            raise RuntimeError(
                _(
                    'Aborted, packages must be updated. You can pass '
                    '"--offline" to prevent checking for updates.'
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            osetuputil.is_ovirt_packaging_supported_distro()
        ),
    )
    def _validation(self):
        engineVersion = None
        if self._plist is not None:
            for p in self._plist:
                if (
                    p['name'] == oenginecons.Const.ENGINE_PACKAGE_NAME and
                    p['operation'] == 'install'
                ):
                    engineVersion = f'{p["version"]}-{p["release"]}'
        if engineVersion is None:
            # Engine is not updated, or we run offline. I still want to make
            # sure we use a matching engine-setup. Check installed engine.
            rc, stdout, stderr = self.execute(
                args=(
                    self.command.get('rpm'),
                    '-q',
                    '--queryformat=%{version}-%{release}',
                    oenginecons.Const.ENGINE_PACKAGE_NAME,
                ),
            )
            engineVersion = stdout[0]
        if (
            engineVersion is not None and
            osetupcons.Const.DISPLAY_VERSION != engineVersion
        ):
            self.dialog.note(
                f'Setup version: {osetupcons.Const.DISPLAY_VERSION}\n'
                f'Engine version: {engineVersion}'
            )
            raise RuntimeError(_(
                'Setup and (updated) Engine versions must match'
            ))

    @plugin.event(
        stage=plugin.Stages.STAGE_PACKAGES,
        condition=lambda self: self._enabled,
    )
    def packages(self):
        for entry in self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ]:
            self.packager.installUpdate(packages=entry['packages'])

    def _arrangedPackageList(self, plist):
        verbs = {
            "update": "is an update",
            "updated": "will be updated",
            "obsoleted": "will be removed",
            "obsoleting": "will be installed",
            "install": "will be installed"
        }

        res = []

        for pname in plist:
            res.append(
                "[{operation}] {package} {verb}".format(
                    operation=pname['operation'],
                    package=pname['display_name'],
                    verb=verbs.get(pname['operation'], '')
                )
            )

            self.logger.debug(res[-1])

        res.sort(key=lambda s: s.split()[1])

        return res

# vim: expandtab tabstop=4 shiftwidth=4
