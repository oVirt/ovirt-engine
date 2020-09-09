#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
NFS exports configuration plugin.
"""

import gettext
import os
import re

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


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
            .*
            $
        """,
    )

    def _getContentRemovePath(self, conf, path):
        old_line = None
        new_content = []
        if os.path.exists(conf):
            with open(conf, 'r') as f:
                content = f.read().splitlines()
            for line in content:
                matcher = self._RE_EXPORTS_LINE.match(line)
                if matcher and matcher.group('path') == path:
                    old_line = line
                else:
                    new_content.append(line)
        return new_content, old_line

    def _getContentAppendLine(self, conf, new):
        path = self._RE_EXPORTS_LINE.match(new).group('path')
        new_content = []
        found = False
        if os.path.exists(conf):
            with open(conf, 'r') as f:
                content = f.read().splitlines()
            for line in content:
                matcher = self._RE_EXPORTS_LINE.match(line)
                if matcher and matcher.group('path') == path:
                    new_content.append(new)
                    found = True
                else:
                    new_content.append(line)
        if not found:
            new_content.append(new)
        return new_content

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._source = None
        self._destination = None
        self._move = False
        self._generate = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.ConfigEnv.ISO_DOMAIN_NFS_ACL,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]
        self.command.detect('exportfs')

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: not self.environment[
            oenginecons.CoreEnv.ENABLE
        ],
        priority=plugin.Stages.PRIORITY_HIGH
    )
    def _validation_enable(self):
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        if os.path.exists(oenginecons.FileLocations.OVIRT_NFS_EXPORT_FILE):
            self._source = self._destination = (
                oenginecons.FileLocations.OVIRT_NFS_EXPORT_FILE
            )
        elif os.path.exists(oenginecons.FileLocations.NFS_EXPORT_DIR):
            self._source = oenginecons.FileLocations.NFS_EXPORT_FILE
            self._destination = (
                oenginecons.FileLocations.
                OVIRT_NFS_EXPORT_FILE
            )

            content, old_line = self._getContentRemovePath(
                self._source,
                self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ],
            )
            self._move = old_line is not None
        elif os.path.exists(oenginecons.FileLocations.NFS_EXPORT_FILE):
            self.source = self._destination = (
                oenginecons.FileLocations.NFS_EXPORT_FILE
            )

        self._generate = (
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
            ] is not None and
            self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_ACL
            ] is not None
        )

        self.logger.debug(
            'move=%s, generate=%s',
            self._move,
            self._generate
        )

        self._enabled = self._move or self._generate

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        after=(
            oenginecons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        ),
    )
    def _misc(self):
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(oenginecons.FileLocations.NFS_EXPORT_FILE)
        new_line = '{path}\t{acl}'.format(
            path=self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
            ],
            acl=self.environment[
                oenginecons.ConfigEnv.ISO_DOMAIN_NFS_ACL
            ],
        )
        do_generate = self._generate

        if self._move:
            content, old_line = self._getContentRemovePath(
                self._source,
                self.environment[
                    oenginecons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ],
            )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=self._source,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

            if not self._generate:
                self.environment[
                    osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
                ].append(self._destination)
                do_generate = True
                new_line = old_line

        self.logger.debug('generate=%s, line=%s', self._generate, new_line)

        if do_generate:
            uninstall_files = []

            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=self._destination,
                    content=self._getContentAppendLine(
                        self._destination,
                        new_line,
                    ),
                    modifiedList=uninstall_files,
                )
            )

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

        if self._generate:
            exports_uninstall_group.addChanges(
                group='exportfs',
                filename=self._destination,
                changes=[{'added': new_line}],
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


# vim: expandtab tabstop=4 shiftwidth=4
