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
AIO local storage domain plugin.
"""

import datetime
import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import domains as osetupdomains
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    AIO local storage plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._checker = osetupdomains.DomainChecker()

    def _validateDomain(self, path):
        self._checker.check_valid_path(path)
        self._checker.check_base_writable(path)
        self._checker.check_available_space(
            path,
            oenginecons.AIOConst.MINIMUM_SPACE_STORAGEDOMAIN_MB
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.AIOEnv.STORAGE_DOMAIN_DIR,
            None
        )
        self.environment.setdefault(
            oenginecons.AIOEnv.STORAGE_DOMAIN_NAME,
            None
        )
        self.environment.setdefault(
            oenginecons.AIOEnv.STORAGE_DOMAIN_DEFAULT_DIR,
            oenginecons.FileLocations.AIO_STORAGE_DOMAIN_DEFAULT_DIR
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            oenginecons.AIOEnv.CONFIGURE
        ],
        name=oenginecons.Stages.AIO_CONFIG_STORAGE,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ALLINONE,
        ),
        after=(
            oenginecons.Stages.AIO_CONFIG_AVAILABLE,
        ),
    )
    def _customization(self):
        """
        If the user want to use NFS for ISO domain ask how to configure it.
        """
        interactive = self.environment[
            oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
        ] is None

        validDomain = False
        while not validDomain:
            try:
                default_dir = self.environment[
                    oenginecons.AIOEnv.STORAGE_DOMAIN_DEFAULT_DIR
                ]
                if os.path.exists(default_dir):
                    default_dir += '-%s' % (
                        datetime.datetime.utcnow().strftime('%Y%m%d%H%M%S')
                    )
                if interactive:
                    self.environment[
                        oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
                    ] = self.dialog.queryString(
                        name='OVESETUP_AIO_STORAGE_DOMAIN_DIR',
                        note=_('Local storage domain path [@DEFAULT@]: '),
                        prompt=True,
                        caseSensitive=True,
                        default=default_dir,
                    )

                self._validateDomain(
                    path=self.environment[
                        oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
                    ]
                )

                validDomain = True

            except (ValueError, RuntimeError) as e:
                if interactive:
                    self.logger.debug('exception', exc_info=True)
                    self.logger.error(
                        _(
                            'Cannot access storage directory '
                            '{directory}: {error}'
                        ).format(
                            directory=self.environment[
                                oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
                            ],
                            error=e,
                        )
                    )
                else:
                    raise

        path = self.environment[
            oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
        ].rstrip('/')
        self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS].append({
            'type': 'public_content_rw_t',
            'pattern': '%s(/.*)?' % path,
        })
        self.environment[
            osetupcons.SystemEnv.SELINUX_RESTORE_PATHS
        ].append(path)

        if self.environment[
            oenginecons.AIOEnv.STORAGE_DOMAIN_NAME
        ] is None:
            self.environment[
                oenginecons.AIOEnv.STORAGE_DOMAIN_NAME
            ] = self.dialog.queryString(
                name='OVESETUP_AIO_STORAGE_DOMAIN_NAME',
                note=_('Local storage domain name [@DEFAULT@]: '),
                prompt=True,
                caseSensitive=True,
                default=oenginecons.AIODefaults.DEFAULT_STORAGE_DOMAIN_NAME,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oenginecons.AIOEnv.CONFIGURE
        ],
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(
                    self.environment[
                        oenginecons.AIOEnv.STORAGE_DOMAIN_DIR
                    ].rstrip('/'),
                    '.keep',
                ),
                content='',
                mode=0o644,
                dmode=0o755,
                owner=self.environment[oengcommcons.SystemEnv.USER_VDSM],
                group=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                downer=self.environment[
                    oengcommcons.SystemEnv.USER_VDSM
                ],
                dgroup=self.environment[oengcommcons.SystemEnv.GROUP_KVM],
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
