#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Simple plugin."""


import gettext
import glob
import os

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Simple plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RenameEnv.FORCE_OVERWRITE,
            False
        )
        self.environment.setdefault(
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED,
            []
        )
        self._infos = sorted(
            glob.glob(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
                    '*.conf',
                )
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        all_modified_files = self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ]

        externally_modified_files = [
            f
            for f, info in self.environment[
                osetupcons.CoreEnv.UNINSTALL_FILES_INFO
            ].items()
            if info.get('changed')
        ]

        self.logger.info(_('The following files will be updated:'))
        self.dialog.note(
            text=_(
                '\n'
                '{files}\n'
                '\n'
            ).format(
                files='\n'.join(sorted(all_modified_files))
            )
        )

        if (
            externally_modified_files and
            not self.environment[osetupcons.RenameEnv.FORCE_OVERWRITE]
        ):
            self.logger.warn(_("Files modified externally"))
            self.dialog.note(
                text=_(
                    'The following files were externally modified - outside\n'
                    'of package management and/or engine-setup - perhaps by\n'
                    'the system administrator:\n'
                    '\n'
                    '{files}\n'
                    '\n'
                ).format(
                    files='\n'.join(sorted(externally_modified_files))
                )
            )

            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='FORCE_OVERWRITE_FILES',
                note=_(
                    'Do you want to overwrite all of the above files '
                    'with newly-generated content using the new host name? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            ):
                raise RuntimeError(_('Aborted by user'))


# vim: expandtab tabstop=4 shiftwidth=4
