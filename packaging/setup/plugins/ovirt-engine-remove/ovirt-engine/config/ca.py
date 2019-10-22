#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""CA plugin."""


import datetime
import gettext
import os
import tarfile
import tempfile

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._bkpfile = None

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[
                oenginecons.RemoveEnv.REMOVE_ENGINE
            ] or
            'ca_pki' in [
                x.strip()
                for x in self.environment[
                    osetupcons.RemoveEnv.REMOVE_GROUPS
                ].split(',')
                if x
            ]
        ),
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _misc(self):
        self.logger.info(
            _('Backing up PKI configuration and keys')
        )
        fd, self._bkpfile = tempfile.mkstemp(
            prefix=(
                'engine-pki-%s' %
                datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            ),
            suffix='.tar.gz',
            dir=self.environment[
                oenginecons.ConfigEnv.OVIRT_ENGINE_DB_BACKUP_DIR
            ],
        )
        os.fchown(
            fd,
            osetuputil.getUid(
                self.environment[oengcommcons.SystemEnv.USER_ROOT]
            ),
            -1
        )
        os.fchmod(fd, 0o600)
        with os.fdopen(fd, 'wb') as fileobj:
            # fileobj is not closed, when TarFile is closed
            # cannot use with tarfile.open() <python-2.7
            tar = None
            try:
                tar = tarfile.open(
                    mode='w:gz',
                    fileobj=fileobj
                )
                for n in (
                    oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_PKI,
                    oenginecons.FileLocations.OVIRT_ENGINE_PKIDIR,
                ):
                    if os.path.exists(n):
                        tar.add(n)
            finally:
                if tar is not None:
                    tar.close()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._bkpfile is not None,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'A backup of PKI configuration and keys '
                'is available at {path}'
            ).format(
                path=self._bkpfile
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
