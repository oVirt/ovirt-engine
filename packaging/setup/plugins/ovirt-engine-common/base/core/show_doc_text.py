#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""show doc text plugin."""


import gettext
import inspect
import sys

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """show doc text plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(osetupcons.DocsEnv.SHOW_DOC_TEXT, False)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        condition=lambda self: self.environment[
            osetupcons.DocsEnv.SHOW_DOC_TEXT
        ],
    )
    def _show_doc_text(self):
        self.dialog.note(_(
            '\nThis tool uses the following environment keys.\n\n'
            'Some of them can be set via an answer file or '
            '--otopi-environment, for affecting its behavior.\n\n'
            'Others are internal-only, and setting them might cause '
            'problems.\n\n'
            'See also:\n\n'
            '- https://www.ovirt.org/develop/developer-guide/'
            'engine/engine-setup.html\n'
            '- https://www.ovirt.org/develop/developer-guide/'
            'engine/otopi.html\n\n'
            'Attributes:\n\n'
            '- answerfile, answerfile_condition: Unused now. In the past, '
            ' used for answer file generation.\n'
            '- summary, summary_condition: Whether the key should appear in '
            'the configuration preview in engine-setup.\n'
            '- description: For the configuration preview in engine-setup.\n'
            '- postinstallfile: Whether it is written to the postinstall '
            'file.\n'
            '- reconfigurable: Can it be set using '
            '--reconfigure-optional-components.\n'
            '- is_secret: Should it be filtered out in the log.\n'
            '- asked_on: If key is secret: Question names setting it.\n'
            '- doc_text: Documentation for this key.\n\n'
        ))
        attrs_defaults = {
            k: v.default
            for k, v in inspect.signature(
                osetupcons.osetupattrs
            ).parameters.items()
        }
        for constobj in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            self.dialog.note(f'From file {constobj.__file__}:')
            for cls in constobj.__dict__['__osetup_attrs__']:
                self.dialog.note(f'  From class {cls.__name__}:')
                for keyname, key in cls.__dict__.items():
                    if not keyname.startswith('_') and isinstance(key, str):
                        self.dialog.note(f'    * {key}')
                    elif hasattr(key, '__osetup_attrs__'):
                        self.dialog.note(f'    * {key.fget(None)}')
                        for attr in attrs_defaults.keys():
                            value = key.__osetup_attrs__[attr]
                            if value != attrs_defaults[attr]:
                                self.dialog.note(
                                    f'      - {attr} : {value}'
                                )
        # TODO: Is it safe to use sys.exit? I think so, as STAGE_INIT
        # is very early. If so, remove this comment. Other options:
        # 1. Do not exit. That's not very nice, but just as safe...
        # 2. Use addExitCode and raise an exception. otopi's "Standard" way
        # 3. Add and use some nicer means in otopi itself
        sys.exit(0)


# vim: expandtab tabstop=4 shiftwidth=4
