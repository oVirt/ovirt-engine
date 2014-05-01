#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2014 Red Hat, Inc.
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


"""Jboss plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup.engine_common \
    import constants as oengcommcons


@util.export
class Plugin(plugin.PluginBase):
    """JBoss plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_HOME,
            oengcommcons.FileLocations.JBOSS_HOME
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_NEEDED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_FIRST,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.JBOSS_NEEDED
        ],
    )
    def _jboss(self):
        """
        Check JBOSS_HOME after ovirt-engine upgrade since jboss may be
        upgraded as well and JBOSS_HOME may become invalid.
        This can't be done at package stage since yum transaction is committed
        as last action in that stage.
        """
        if not os.path.exists(
            self.environment[
                oengcommcons.ConfigEnv.JBOSS_HOME
            ]
        ):
            raise RuntimeError(
                _('Cannot find Jboss at {jbossHome}').format(
                    jbossHome=self.environment[
                        oengcommcons.ConfigEnv.JBOSS_HOME
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
