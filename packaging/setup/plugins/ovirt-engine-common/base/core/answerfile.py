#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Answer file plugin."""


import datetime
import gettext
import io
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Answer file plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.ANSWER_FILE,
            None
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.GENERATE_STANDARD_ANSWERFILE,
            True
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
        after=(
            otopicons.Stages.ANSWER_FILE_GENERATED,
        ),
        priority=plugin.Stages.PRIORITY_LAST,
    )
    def _cleanup(self):
        answers = []
        if self.environment[osetupcons.CoreEnv.GENERATE_STANDARD_ANSWERFILE]:
            answers.append(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_SETUP_ANSWERS_DIR,
                    '%s-%s.conf' % (
                        datetime.datetime.now().strftime('%Y%m%d%H%M%S'),
                        self.environment[osetupcons.CoreEnv.ACTION],
                    ),
                )
            )
        if self.environment[osetupcons.CoreEnv.ANSWER_FILE] is not None:
            answers.append(
                self.environment[osetupcons.CoreEnv.ANSWER_FILE]
            )

        for answer in answers:
            self.logger.info(
                _("Generating answer file '{name}'").format(
                    name=answer,
                )
            )
            # Generate the answer file only if valid path is passed
            try:
                with io.open(
                    self.resolveFile(answer),
                    mode='w',
                    encoding='utf-8',
                ) as f:
                    os.fchmod(f.fileno(), 0o600)
                    f.write(
                        self.environment[
                            otopicons.DialogEnv.ANSWER_FILE_CONTENT
                        ]
                    )
            except IOError as e:
                self.logger.warning(
                    _(
                        'Cannot write to answer file: {answerfile} '
                        'Error: {error}'
                    ).format(
                        answerfile=answer,
                        error=e,
                    )
                )
                self.logger.debug(
                    'Exception while writing to answer file: %s',
                    answer,
                    exc_info=True,
                )

# vim: expandtab tabstop=4 shiftwidth=4
