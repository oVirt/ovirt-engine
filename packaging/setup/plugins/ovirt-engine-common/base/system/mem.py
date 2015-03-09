#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
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


import gettext
import re

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    _RE_MEMINFO_MEMTOTAL = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            MemTotal:
            \s+
            (?P<value>\d+)
            \s+
            (?P<unit>\w+)
        """
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self.environment.setdefault(
            osetupcons.ConfigEnv.TOTAL_MEMORY_MB,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.logger.debug('Checking total memory')
        with open('/proc/meminfo', 'r') as f:
            content = f.read()

        match = self._RE_MEMINFO_MEMTOTAL.match(content)
        if match is None:
            raise RuntimeError(_("Unable to parse /proc/meminfo"))

        if self.environment[osetupcons.ConfigEnv.TOTAL_MEMORY_MB] is None:
            self.environment[osetupcons.ConfigEnv.TOTAL_MEMORY_MB] = int(
                match.group('value')
            )
            if match.group('unit') == "kB":
                self.environment[osetupcons.ConfigEnv.TOTAL_MEMORY_MB] //= 1024


# vim: expandtab tabstop=4 shiftwidth=4
