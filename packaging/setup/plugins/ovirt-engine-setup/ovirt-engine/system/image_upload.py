#
# image_upload plugin -- ovirt engine setup
#
# Copyright (C) 2014-2015 Red Hat, Inc.
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
ISO image uploader plugin.
"""


import datetime
import gettext
import os
import shutil

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    ISO image uploader plugin.
    """
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.ISO_PATHS_TO_UPLOAD,
            []
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[osetupcons.ConfigEnv.ISO_PATHS_TO_UPLOAD] = [
            os.path.join(
                osetupcons.FileLocations.VIRTIO_WIN_DIR,
                'virtio-win_x86.vfd',
            ),
            os.path.join(
                osetupcons.FileLocations.VIRTIO_WIN_DIR,
                'virtio-win_amd64.vfd',
            ),
            os.path.join(
                osetupcons.FileLocations.VIRTIO_WIN_DIR,
                'virtio-win.iso',
            ),
            os.path.join(
                osetupcons.FileLocations.OVIRT_GUEST_TOOLS_DIR,
                'ovirt-tools-setup.iso',
            ),
            os.path.join(
                osetupcons.FileLocations.RHEV_GUEST_TOOLS_DIR,
                'rhev-tools-setup.iso',
            ),
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ] and
            self.environment[
                oenginecons.SystemEnv.NFS_CONFIG_ENABLED
            ]
        ),
    )
    def _validation(self):
        for filename in self.environment[
            osetupcons.ConfigEnv.ISO_PATHS_TO_UPLOAD
        ]:
            if os.path.exists(filename):
                self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        after=(
            oenginecons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        ),
    )
    def _misc(self):
        """
        Load files (iso, vfd) from existing rpms to the NFS ISO domain
        TODO: use engine-iso-uploader when it will support local destinations
        """
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='iso_images',
            description='Uploaded ISO images',
            optional=True
        ).addFiles(
            group='iso_images',
            fileList=uninstall_files,
        )

        targetDir = self.environment[
            oenginecons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
        ]

        # Iterate the list and copy all the files.
        for filename in self.environment[
            osetupcons.ConfigEnv.ISO_PATHS_TO_UPLOAD
        ]:
            if os.path.exists(filename):
                try:
                    targetFile = os.path.join(
                        targetDir,
                        os.path.basename(filename)
                    )
                    if os.path.exists(targetFile):
                        shutil.move(
                            targetFile,
                            '%s.%s' % (
                                targetFile,
                                datetime.datetime.now().strftime(
                                    '%Y%m%d%H%M%S'
                                )
                            )
                        )
                    shutil.copyfile(filename, targetFile)
                    uninstall_files.append(targetFile)
                    os.chmod(targetFile, 0o644)
                    os.chown(
                        targetFile,
                        osetuputil.getUid(
                            self.environment[oengcommcons.SystemEnv.USER_VDSM]
                        ),
                        osetuputil.getGid(
                            self.environment[oengcommcons.SystemEnv.GROUP_KVM]
                        )
                    )
                except (OSError, shutil.Error) as e:
                    self.logger.warning(
                        _(
                            "Cannot copy '{filename}' to iso domain "
                            "'{directory}', error: {error}"
                        ).format(
                            filename=filename,
                            directory=targetDir,
                            error=e,
                        )
                    )


# vim: expandtab tabstop=4 shiftwidth=4
