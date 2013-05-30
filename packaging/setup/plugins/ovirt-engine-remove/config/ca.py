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


"""CA plugin."""


import tarfile
import tempfile
import datetime
import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._bkpfile = None

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: 'ca_pki' in [
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UNINSTALL_ENABLED_FILE_GROUPS
            ].split(',')
            if x
        ],
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
            dir=osetupcons.FileLocations.OVIRT_ENGINE_DB_BACKUP_DIR,
        )
        os.fchown(
            fd,
            osetuputil.getUid(
                self.environment[osetupcons.SystemEnv.USER_ROOT]
            ),
            -1
        )
        os.fchmod(fd, 0o600)
        with os.fdopen(fd, 'wb') as fileobj:
            #fileobj is not closed, when TarFile is closed
            with tarfile.open(
                mode='w:gz',
                fileobj=fileobj
            ) as f:
                for n in (
                    osetupcons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_PKI,
                    osetupcons.FileLocations.OVIRT_ENGINE_PKIDIR,
                ):
                    if os.path.exists(n):
                        f.add(n)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._bkpfile is not None,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
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
