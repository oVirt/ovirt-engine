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
NFS exports configuration plugin.
"""

import re
import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction

from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    NFS exports configuration plugin.
    """

    _RE_EXPORTS_LINE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            (?P<path>\S+)
            \s+
            (?P<clients>[^\#]*[^\#\s])
            (?P<comment>\s*\#.*)?
            $
        """,
    )

    def _read_and_find_path(self, conf, path):
        index = None
        content = None
        clients = None
        if os.path.exists(conf):
            with open(conf, 'r') as f:
                content = f.read().splitlines()
            for i, line in enumerate(content):
                matcher = self._RE_EXPORTS_LINE.match(line)
                if matcher and matcher.group('path') == path:
                    index = i
                    clients = matcher.group('clients')
                    break
        return content, index, clients

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._conf = None

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]
        self.command.detect('exportfs')

        if os.path.exists(osetupcons.FileLocations.NFS_EXPORT_DIR):
            self._conf = osetupcons.FileLocations.OVIRT_NFS_EXPORT_FILE
        else:
            self._conf = osetupcons.FileLocations.NFS_EXPORT_FILE

        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(osetupcons.FileLocations.NFS_EXPORT_FILE)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        self._enabled = self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ] is not None

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        after=(
            osetupcons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        ),
    )
    def _misc(self):
        """
        Assume that if /etc/exports.d exists we have exports.d support.
        Always create single exports.d entry for our engine, no matter
        what we had before.
        In /etc/exports make sure we have our own path.
        """
        uninstall_files = []
        exports_uninstall_group = self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='exportfs',
            description='NFS exports configuration',
            optional=True
        )
        exports_uninstall_group.addFiles(
            group='exportfs',
            fileList=uninstall_files,
        )
        path = self.environment[
            osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
        ]
        default_clients = '0.0.0.0/0.0.0.0(rw)'
        new_line = '{path}\t{clients}'.format(
            path=path,
            clients=default_clients,
        )
        exports_content, exports_index, exports_clients = (
            self._read_and_find_path(
                osetupcons.FileLocations.NFS_EXPORT_FILE,
                path
            )
        )
        if self._conf == osetupcons.FileLocations.NFS_EXPORT_FILE:
            if exports_index is None:
                exports_content.append(new_line)
                exports_uninstall_group.addChanges(
                    group='exportfs',
                    filename=self._conf,
                    changes=[
                        {
                            'added': new_line,
                        }
                    ],
                )
        else:
            if exports_index is not None:
                new_line = exports_content.pop(exports_index)
                if exports_clients != default_clients:
                    self.environment[
                        osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
                    ].append(self._conf)
            exports_d_content, exports_d_index, exports_d_clients = (
                self._read_and_find_path(
                    self._conf,
                    path
                )
            )
            if exports_d_index is not None:
                self.logger.debug(
                    '{path} already in {conf}, not changing it'.format(
                        path=path,
                        conf=self._conf,
                    )
                )
            else:
                self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                    filetransaction.FileTransaction(
                        name=self._conf,
                        content=[new_line],
                        modifiedList=uninstall_files,
                    )
                )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.NFS_EXPORT_FILE,
                content=exports_content,
                modifiedList=uninstall_files,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        rc, stdout, stderr = self.execute(
            (
                self.command.get('exportfs'),
                '-r',
                '-a',
            ),
            raiseOnError=False,
        )
        if rc != 0:
            self.logger.error(
                _('Could not refresh NFS exports ({code}: {error})').format(
                    code=rc,
                    error=stderr,
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._enabled,
    )
    def _closeupMessage(self):
        self.dialog.note(
            text=_(
                'An ISO NFS share has been created on this host.\n'
                '    If IP based access restrictions are required, edit:\n'
                '    entry {entry} in {file}'
            ).format(
                entry=self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ],
                file=self._conf,
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
