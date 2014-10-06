#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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

import os
import platform
import datetime
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin
from otopi import transaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


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

        def _filterVersionLock(self):
            modified = False
            content = []

            if os.path.exists(
                osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK
            ):
                with open(
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                ) as f:
                    for line in f.read().splitlines():
                        found = False
                        for pattern in self.environment[
                            osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
                        ]:
                            if line.find(pattern) != -1:
                                found = True
                                break
                        if not found:
                            content.append(line)
                        else:
                            modified = True
            return (modified, content)

        @property
        def environment(self):
            return self._parent.environment

        def __init__(self, parent):
            self._parent = parent
            self._backup = None

        def __str__(self):
            return _("Version Lock Transaction")

        def prepare(self):
            if not self._parent._enabled:
                return

            modified, content = self._filterVersionLock()

            if modified:
                self._backup = '%s.%s' % (
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                    datetime.datetime.now().strftime('%Y%m%d%H%M%S'),
                )
                os.rename(
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                    self._backup,
                )
                with open(
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                    'w'
                ) as f:
                    f.write('\n'.join(content) + '\n')

        def abort(self):
            if (
                self._backup is not None and
                os.path.exists(self._backup)
            ):
                os.rename(
                    self._backup,
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
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
                self.environment[
                    osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
                ].append(osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK)

                self.environment[
                    osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
                ].createGroup(
                    group='versionlock',
                    description='YUM version locking configuration',
                    optional=False
                ).addChanges(
                    'versionlock',
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                    changes,
                )

                modified, content = self._filterVersionLock()
                content.extend(out)
                with open(
                    osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                    'w',
                ) as f:
                    f.write('\n'.join(content) + '\n')

    def _getSink(self):
        class MyMiniYumSink(self._miniyum.MiniYumSinkBase):
            def __init__(self, log):
                super(MyMiniYumSink, self).__init__()
                self._log = log

            def verbose(self, msg):
                super(MyMiniYumSink, self).verbose(msg)
                self._log.debug('Yum: %s', msg)

            def info(self, msg):
                super(MyMiniYumSink, self).info(msg)
                self._log.info('Yum: %s', msg)

            def error(self, msg):
                super(MyMiniYumSink, self).error(msg)
                self._log.error('Yum: %s', msg)
        return MyMiniYumSink(self.logger)

    def _checkForPackagesUpdate(self, packages):
        update = []
        myum = self._miniyum.MiniYum(
            sink=self._getSink(),
            disabledPlugins=('versionlock',),
        )
        for package in packages:
            with myum.transaction():
                myum.update(packages=(package,))
                if myum.buildTransaction():
                    if myum.queryTransaction():
                        update.append(package)

        return update

    def _checkForProductUpdate(self):
        haveRollback = True
        upgradeAvailable = False
        myum = self._miniyum.MiniYum(
            sink=self._getSink(),
            disabledPlugins=('versionlock',),
        )
        with myum.transaction():
            groups = [group['name'] for group in myum.queryGroups()]
            for entry in self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ]:
                if 'group' in entry and entry['group'] in groups:
                    myum.updateGroup(group=entry['group'])
                else:
                    myum.install(packages=entry['packages'])
                    myum.update(packages=entry['packages'])

            if myum.buildTransaction():
                upgradeAvailable = True

                # Some debug
                for p in myum.queryTransaction():
                    self.logger.debug('PACKAGE: [%s] %s' % (
                        p['operation'],
                        p['display_name']
                    ))

                # Verify all installed packages available in yum
                for package in myum.queryTransaction():
                    installed = False
                    reinstall_available = False
                    for query in myum.queryPackages(
                        patterns=(package['name'],),
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
                        haveRollback = False
                        break
        return (upgradeAvailable, haveRollback)

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
            not self.environment[
                osetupcons.CoreEnv.OFFLINE_PACKAGER
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

        from otopi import miniyum
        self._miniyum = miniyum
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
        haveRollback = None

        if self.environment[osetupcons.RPMDistroEnv.ENABLE_UPGRADE] is None:
            self.logger.info(_('Checking for product updates...'))
            (
                upgradeAvailable,
                haveRollback,
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
                        'Setup has found updates for some packages, '
                        'do you wish to update them now? '
                        '(@VALUES@) [@DEFAULT@]: '
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
                        '"yum update {packages}"\nand then execute Setup '
                        'again.'
                    ).format(
                        packages=' '.join(update),
                    ),
                )
                raise RuntimeError(_('Please update the Setup packages'))

            if upgradeAvailable is None:
                (
                    upgradeAvailable,
                    haveRollback,
                ) = self._checkForProductUpdate()

            if not upgradeAvailable:
                self.dialog.note(text=_('No update for Setup found'))
            else:
                if not haveRollback:
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
                                'installed ones were not found in enabled '
                                'repositories.\n'
                                'Do you want to abort Setup? '
                                '(@VALUES@) [@DEFAULT@]: '
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
