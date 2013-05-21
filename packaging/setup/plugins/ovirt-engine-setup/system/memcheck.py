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
Available memory checking plugin.
"""


import re
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """
    Available memory checking plugin.
    """
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
        self._total_memory = 0

    def _check_requirements(self):
        satisfied = False
        if self._total_memory < self.environment[
            osetupcons.SystemEnv.MEMCHECK_MINIMUM_MB
        ] * 0.95:
            self.logger.warn(
                _(
                    'Warning: Not enough available memory on the Host, '
                    'the minimum requirement is {minimum}MB, and the '
                    'recommended is {recommended}MB).'
                ).format(
                    minimum=self.environment[
                        osetupcons.SystemEnv.MEMCHECK_MINIMUM_MB
                    ],
                    recommended=self.environment[
                        osetupcons.SystemEnv.MEMCHECK_RECOMMENDED_MB
                    ],
                )
            )
        else:
            satisfied = True
            if self._total_memory < self.environment[
                osetupcons.SystemEnv.MEMCHECK_RECOMMENDED_MB
            ] * 0.95:
                self.logger.warn(
                    _(
                        'There is less than {recommended}MB available memory'
                    ).format(
                        recommended=self.environment[
                            osetupcons.SystemEnv.MEMCHECK_RECOMMENDED_MB
                        ],
                    )
                )
        return satisfied

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.SystemEnv.MEMCHECK_ENABLED,
            None
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.MEMCHECK_MINIMUM_MB,
            osetupcons.Defaults.DEFAULT_SYSTEM_MEMCHECK_MINIMUM_MB
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.MEMCHECK_RECOMMENDED_MB,
            osetupcons.Defaults.DEFAULT_SYSTEM_MEMCHECK_RECOMMENDED_MB
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        """
        Check if the system met the memory requirements.
        """
        self.logger.debug('Checking total memory')
        with open('/proc/meminfo', 'r') as f:
            content = f.read()

        match = self._RE_MEMINFO_MEMTOTAL.match(content)
        if match is None:
            raise RuntimeError(_("Unable to parse /proc/meminfo"))

        self._total_memory = int(
            match.group('value')
        )
        if match.group('unit') == "kB":
            self._total_memory //= 1024

        satisfied = self._check_requirements()
        interactive = self.environment[
            osetupcons.SystemEnv.MEMCHECK_ENABLED
        ] is None
        if not satisfied and interactive:
            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_MEMORY_CHECK',
                note=_(
                    'Do you want to install with memory less than '
                    'recommended? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            ):
                raise RuntimeError(_('Aborted by user'))

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
    )
    def _closeup(self):
        self._check_requirements()


# vim: expandtab tabstop=4 shiftwidth=4
