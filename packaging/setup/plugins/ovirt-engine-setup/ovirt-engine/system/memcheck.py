#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Available memory checking plugin.
"""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Available memory checking plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _check_requirements(self):
        satisfied = False
        if self.environment[
            osetupcons.ConfigEnv.TOTAL_MEMORY_MB
        ] < self.environment[
            oenginecons.SystemEnv.MEMCHECK_MINIMUM_MB
        ] * self.environment[
            oenginecons.SystemEnv.MEMCHECK_THRESHOLD
        ] / 100:
            self.logger.warn(
                _(
                    'Warning: Not enough memory is available on the host. '
                    'Minimum requirement is {minimum}MB, and {recommended}MB '
                    'is recommended.'
                ).format(
                    minimum=self.environment[
                        oenginecons.SystemEnv.MEMCHECK_MINIMUM_MB
                    ],
                    recommended=self.environment[
                        oenginecons.SystemEnv.MEMCHECK_RECOMMENDED_MB
                    ],
                )
            )
        else:
            satisfied = True
            if self.environment[
                osetupcons.ConfigEnv.TOTAL_MEMORY_MB
            ] < self.environment[
                oenginecons.SystemEnv.MEMCHECK_RECOMMENDED_MB
            ] * self.environment[
                oenginecons.SystemEnv.MEMCHECK_THRESHOLD
            ] / 100:
                self.logger.warn(
                    _(
                        'Less than {recommended}MB of memory is available'
                    ).format(
                        recommended=self.environment[
                            oenginecons.SystemEnv.MEMCHECK_RECOMMENDED_MB
                        ],
                    )
                )
        return satisfied

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.SystemEnv.MEMCHECK_ENABLED,
            True
        )
        self.environment.setdefault(
            oenginecons.SystemEnv.MEMCHECK_MINIMUM_MB,
            oenginecons.Defaults.DEFAULT_SYSTEM_MEMCHECK_MINIMUM_MB
        )
        self.environment.setdefault(
            oenginecons.SystemEnv.MEMCHECK_RECOMMENDED_MB,
            oenginecons.Defaults.DEFAULT_SYSTEM_MEMCHECK_RECOMMENDED_MB
        )
        self.environment.setdefault(
            oenginecons.SystemEnv.MEMCHECK_THRESHOLD,
            oenginecons.Defaults.DEFAULT_SYSTEM_MEMCHECK_THRESHOLD
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        name=oenginecons.Stages.MEMORY_CHECK,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _validateMemory(self):
        """
        Check if the system met the memory requirements.
        """
        self._satisfied = self._check_requirements()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        after=(
            oenginecons.Stages.MEMORY_CHECK,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.SystemEnv.MEMCHECK_ENABLED]
        ),
    )
    def _validateContinueLowMemory(self):
        if (
            self.environment[
                oenginecons.SystemEnv.MEMCHECK_ENABLED
            ] and
            not self._satisfied
        ):
            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_MEMCHECK_ENABLED',
                note=_(
                    'Do you want Setup to continue, with amount of memory '
                    'less than recommended? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            ):
                raise RuntimeError(_('Aborted by user'))

            self.environment[
                oenginecons.SystemEnv.MEMCHECK_ENABLED
            ] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _closeup(self):
        self._check_requirements()


# vim: expandtab tabstop=4 shiftwidth=4
