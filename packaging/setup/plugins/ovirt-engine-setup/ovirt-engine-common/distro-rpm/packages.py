#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""
Package upgrade plugin.
"""

import datetime
import gettext
import os
import platform

from otopi import constants as otopicons
from otopi import plugin, transaction, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup import util as osetuputil


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
        # TODO: otopi is now providing minidnf too
        missingRollback = []
        upgradeAvailable = False
        mpm = self._MiniPM(
            sink=self._getSink(),
            disabledPlugins=('versionlock',),
        )
        plist = []
        with mpm.transaction():
            groups = [group['name'] for group in mpm.queryGroups()]
            for entry in self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ]:
                if 'group' in entry and entry['group'] in groups:
                    mpm.updateGroup(group=entry['group'])
                else:
                    mpm.installUpdate(packages=entry['packages'])

            if mpm.buildTransaction():
                upgradeAvailable = True

                for p in mpm.queryTransaction():
                    self.logger.debug('PACKAGE: [%s] %s' % (
                        p['operation'],
                        p['display_name']
                    ))
                    plist.append(
                        _(
                            'PACKAGE: [{operation}] {display_name}'
                        ).format(
                            operation=p['operation'],
                            display_name=p['display_name']
                        )
                    )

                # Verify all installed packages available in yum
                for package in mpm.queryTransaction():
                    installed = False
                    reinstall_available = False
                    for query in mpm.queryPackages(
                        patterns=(package['display_name'],),
                        showdups=True,
                    ):
                        self.logger.debug(
                            'dupes: operation [%s] package %s' % (
                                query['operation'],
                                query['display_name'],
                            )
                        )
                        if query['operation'] == 'installed':
                            installed = True
                        if query['operation'] == 'reinstall_available':
                            reinstall_available = True
                    if installed and not reinstall_available:
                        missingRollback.append(package['display_name'])
        return (upgradeAvailable, set(missingRollback), plist)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._shouldResultVersionLock = False
        self._enabled = False
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]

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

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self._distribution in ('redhat', 'fedora', 'centos')
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
                plist,
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
                        'Setup has found updates for some packages:\n'
                        '{plist}\n'
                        'do you wish to update them now? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        plist='\n'.join(plist)
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
            if update:
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
                (
                    upgradeAvailable,
                    missingRollback,
                    plist,
                ) = self._checkForProductUpdate()

            if not upgradeAvailable:
                self.dialog.note(text=_('No update for Setup found'))
            else:
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
                _('Aborted, packages must be updated')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_PACKAGES,
        condition=lambda self: self._enabled,
    )
    def packages(self):
        groups = [group['name'] for group in self.packager.queryGroups()]
        for entry in self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ]:
            if 'group' in entry and entry['group'] in groups:
                self.packager.updateGroup(group=entry['group'])
            else:
                self.packager.installUpdate(packages=entry['packages'])


# vim: expandtab tabstop=4 shiftwidth=4
