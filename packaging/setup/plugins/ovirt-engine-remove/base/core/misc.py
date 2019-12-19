#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Engine plugin."""


import distutils.version
import gettext
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _preinit(self):
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_DIR,
            osetupcons.FileLocations.OVIRT_SETUP_LOGDIR
        )
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_REMOVE_LOG_PREFIX
        )
        self.environment[
            osetupcons.CoreEnv.ACTION
        ] = osetupcons.Const.ACTION_REMOVE

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.REMOVE,
            None
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_ALL,
            True
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_OPTIONS,
            []
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[osetupcons.CoreEnv.GENERATE_POSTINSTALL] = False
        if not os.path.exists(
            osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG
        ):
            self.logger.error(_('Could not detect a completed product setup'))
            self.dialog.note(
                text=_(
                    'Please use the cleanup utility only after a setup '
                    'or after an upgrade from an older installation.'
                )
            )
            osetuputil.addExitCode(
                environment=self.environment,
                code=osetupcons.Const.EXIT_CODE_REMOVE_WITHOUT_SETUP
            )
            raise RuntimeError(
                _('Could not detect product setup')
            )

        rpm_v = distutils.version.LooseVersion(
            osetupcons.Const.RPM_VERSION
        ).version[:3]
        inst_v = distutils.version.LooseVersion(
            self.environment[
                osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION
            ]
        ).version[:3]

        if (rpm_v[:2] != inst_v[:2]) or (rpm_v < inst_v):
            self.logger.error(
                _('Cleanup utility and installed version mismatch')
            )
            self.dialog.note(
                text=_(
                    'Please use a version of cleanup utility '
                    'that matches the engine installed version '
                    '(now engine-cleanup {r_version}, engine {i_version})'
                ).format(
                    r_version=osetupcons.Const.RPM_VERSION,
                    i_version=self.environment[
                        osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION
                    ],
                )
            )
            raise RuntimeError(
                _('Cleanup utility version mismatch')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.REMOVE_CUSTOMIZATION_COMMON,
        priority=plugin.Stages.PRIORITY_HIGH
    )
    def _customization(self):
        if self.environment[
            osetupcons.RemoveEnv.REMOVE_ALL
        ] is None:
            self.logger.warn(_('Partial cleanup is not supported'))
            self.dialog.note(_(
                'Removing only some components is not supported. If you reply '
                'No, you will be prompted for components to be removed, '
                'but regardless of which ones you choose, the engine will '
                'not be functional. Some parts of the configuration are '
                'removed unconditionally. If unsure, stop this utility, e.g. '
                'by pressing Ctrl-C.'
            ))
            self.environment[
                osetupcons.RemoveEnv.REMOVE_ALL
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_REMOVE_ALL',
                note=_(
                    'Do you want to remove all components? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=True,
            )
        if not self.environment[osetupcons.RemoveEnv.REMOVE_ALL]:
            self.logger.warn(_('Partial cleanup is not supported'))
            self.dialog.note(_(
                'You will now be prompted for components to be removed, '
                'but regardless of which ones you choose, the engine will '
                'not be functional. Some parts of the configuration are '
                'removed unconditionally. If unsure, stop this utility, e.g. '
                'by pressing Ctrl-C.'
            ))

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        if self.environment[
            osetupcons.CoreEnv.REMOVE
        ] is None:
            if self.environment[osetupcons.RemoveEnv.REMOVE_ALL]:
                cnote = _(
                    'All the installed ovirt components are about to be '
                    'removed, data will be lost (@VALUES@) [@DEFAULT@]: '
                )
            elif (
                self.environment[osetupcons.RemoveEnv.REMOVE_OPTIONS] or
                self.environment[osetupcons.RemoveEnv.REMOVE_GROUPS]
            ):
                cnote = _(
                    'The following components are about to be removed, data '
                    'will be lost:\n'
                    '{clist}'
                    '(@VALUES@) [@DEFAULT@]: '
                ).format(
                    clist=', '.join(
                        self.environment[
                            osetupcons.RemoveEnv.REMOVE_OPTIONS
                        ] + [
                            x.strip()
                            for x in self.environment[
                                osetupcons.RemoveEnv.REMOVE_GROUPS
                            ].split(',')
                            if x
                        ]
                    )
                )
            else:
                raise RuntimeError(
                    _('Nothing to remove')
                )

            self.environment[
                osetupcons.CoreEnv.REMOVE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CORE_REMOVE',
                note=cnote,
                prompt=True,
                true=_('OK'),
                false=_('Cancel'),
                default=False,
            )

        if not self.environment[osetupcons.CoreEnv.REMOVE]:
            raise RuntimeError(
                _('Aborted by user')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Engine setup successfully cleaned up'
            ),
        )

# vim: expandtab tabstop=4 shiftwidth=4
