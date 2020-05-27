#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import os
import tempfile

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import remote_engine_base


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    class _ManualFiles(remote_engine_base.RemoteEngineBase):

        def __init__(self, plugin):
            super(Plugin._ManualFiles, self).__init__(plugin=plugin)
            self._plugin = plugin

        @property
        def plugin(self):
            return self._plugin

        @property
        def dialog(self):
            return self._plugin.dialog

        @property
        def environment(self):
            return self._plugin.environment

        @property
        def logger(self):
            return self._plugin.logger

        @property
        def name(self):
            return osetupcons.Const.REMOTE_ENGINE_SETUP_STYLE_MANUAL_FILES

        def desc(self):
            return _(
                'Perform each action manually, use files to copy content '
                'around'
            )

        def configure(self, fqdn):
            self._fqdn = fqdn

        def execute_on_engine(self, cmd, timeout=60, text=None):
            self.dialog.note(
                text=text if text else _(
                    'Please run on the engine server:\n\n'
                    '{cmd}\n\n'
                ).format(
                    cmd=cmd
                )
            )

        def copy_from_engine(self, file_name, dialog_name=None):
            if dialog_name is None:
                dialog_name = 'REMOTE_ENGINE_MANUAL_COPY_FILES_FROM_ENGINE'
            resfilename = self.dialog.queryString(
                name=dialog_name,
                note=_(
                    'Please copy {file_name} from the engine server to some '
                    'file here.\n'
                    'Please input the location of the local file where you '
                    'copied {file_name} from the engine server: '
                ),
                prompt=True,
            )
            with open(resfilename) as f:
                res = f.read()
            return res

        def copy_to_engine(
                self,
                file_name,
                content,
                inp_env_key,
                uid=None,
                gid=None,
                mode=None,
        ):
            fname = self.environment.get(inp_env_key)
            with (
                open(fname, 'w') if fname
                else tempfile.NamedTemporaryFile(mode='w', delete=False)
            ) as inpfile:
                inpfile.write(content)
            self.dialog.note(
                text=_(
                    'Please copy {inpfile} from here to {file_name} on the '
                    'engine server.\n'
                ).format(
                    inpfile=inpfile.name,
                    file_name=file_name,
                )
            )
            if uid and gid:
                self.dialog.note(
                    text=_(
                        'Please make {file_name} on the engine server owned '
                        'by uid:gid {uid}:{gid}.\n'
                        'You can do this by running there:\n'
                        '# chown {uid}:{gid} {file_name}\n'
                    ).format(
                        file_name=file_name,
                        uid=uid,
                        gid=gid,
                    )
                )
            if mode:
                self.dialog.note(
                    text=_(
                        'Please set the mode bits of {file_name} on the '
                        'engine server to {mode}.\n'
                        'You can do this by running there:\n'
                        '# chmod {mode} {file_name}\n'
                    ).format(
                        file_name=file_name,
                        mode=oct(mode),
                    )
                )
            self.dialog.queryString(
                name='PROMPT_REMOTE_ENGINE_MANUAL_COPY_FILES',
                note="Please press Enter to continue: ",
                prompt=True,
                default='y'  # Allow enter without any value
            )
            if not fname:
                # Remove temporary file
                os.unlink(inpfile.name)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLES
        ].append(
            self._ManualFiles(
                plugin=self,
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
