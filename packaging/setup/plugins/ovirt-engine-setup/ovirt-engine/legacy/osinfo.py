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


"""Upgrade osinfo from legacy plugin."""


import filecmp
import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common \
    import constants as oengcommcons


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade osinfo from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._toremove = set()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ] and
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ]
        ),
        after=[
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ],
    )
    def _misc(self):
        content = []
        used = set()

        for vdco, sysprep, osinfo in (
            ('SysPrep2K3Path', 'sysprep.2k3', (
                'windows_2003', 'windows_2003x64'
            )),
            ('SysPrep2K8Path', 'sysprep.2k8', ('windows_2008',)),
            ('SysPrep2K8R2Path', 'sysprep.2k8', ('windows_2008',)),
            ('SysPrep2K8x64Path', 'sysprep.2k8x64', ('windows_2008x64',)),
            ('SysPrepWindows2012x64Path', 'sysprep.2k12x64', (
                'windows_2012x64',
            )),
            ('SysPrepWindows7Path', 'sysprep.w7', ('windows_7',)),
            ('SysPrepWindows7x64Path', 'sysprep.w7x64', ('windows_7x64',)),
            ('SysPrepWindows8Path', 'sysprep.w8', ('windows_8',)),
            ('SysPrepWindows8x64Path', 'sysprep.w8x64', ('windows_2008x64',)),
            ('SysPrepXPPath', 'sysprep.xp', ('windows_xp',)),
        ):
            val = vdcoption.VdcOption(
                statement=self.environment[
                    oenginecons.EngineDBEnv.STATEMENT
                ]
            ).getVdcOption(name=vdco)

            #
            # fix embarrassing typo in previous setup
            # so we can find files.
            #
            val = val.replace('/gsysprep', '/sysprep')

            #
            # reset so at next cycle we won't
            # consider vdcoptions any more
            #
            vdcoption.VdcOption(
                statement=self.environment[
                    oenginecons.EngineDBEnv.STATEMENT
                ]
            ).updateVdcOptions(
                options=(
                    {
                        'name': vdco,
                        'value': '',
                    },
                ),
            )

            if val and os.path.exists(val):
                self.logger.debug(
                    "Found legacy sysprep %s '%s'",
                    vdco,
                    val,
                )
                if filecmp.cmp(
                    val,
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_DATADIR,
                        'conf',
                        'sysprep',
                        sysprep,
                    )
                ):
                    self._toremove.add(val)
                else:
                    used.add(val)
                    self.logger.debug('legacy sysprep differ from %s', sysprep)
                    for name in osinfo:
                        content.append(
                            'os.%s.sysprepPath.value = %s' % (
                                name,
                                val,
                            )
                        )

        self._toremove -= used

        self.logger.debug('legacy sysprep fixup: %s', content)
        self.logger.debug('legacy sysprep remove: %s', self._toremove)

        if content:
            self.environment[
                otopicons.CoreEnv.MAIN_TRANSACTION
            ].append(
                filetransaction.FileTransaction(
                    name=osetupcons.FileLocations.OSINFO_LEGACY_SYSPREP,
                    content=content,
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _closeup(self):
        for candidate in self._toremove:
            for f in (
                '%s' % candidate,
                '%s.rpmnew' % candidate,
                '%s.rpmsave' % candidate,
            ):
                if os.path.exists(f):
                    os.unlink(f)
        try:
            os.rmdir(
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                    'sysprep',
                )
            )
        except OSError:
            pass


# vim: expandtab tabstop=4 shiftwidth=4
