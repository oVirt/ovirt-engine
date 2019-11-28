#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Simple plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Simple plugin."""

    def _safeDelete(self, filename):
        try:
            os.unlink(filename)
        except OSError as e:
            self.logger.debug(
                "Cannot delete '%s'",
                filename,
                exc_info=True,
            )
            self.logger.error(
                _("Cannot delete '{file}': {error}").format(
                    file=filename,
                    error=e,
                )
            )

    def _revertChanges(self, filename, changes):
        new_content = []
        with open(filename, 'r') as f:
            old_content = f.read().splitlines()
        just_remove = []
        just_add = []
        replace = {}

        for c in changes:
            if 'removed' not in c:
                just_remove.append(c['added'])
            elif 'added' not in c:
                just_add.append(c['removed'])
            else:
                replace[c['added']] = c['removed']
        # For checking if remove/replace lines were found, we work on copies,
        # because there might be duplicate lines in the file.
        remove_unremoved = just_remove[:]
        replace_unremoved = replace.copy()
        for line in old_content:
            if line in just_remove:
                if line in remove_unremoved:
                    remove_unremoved.remove(line)
            else:
                # should be updated or added
                if line in replace:
                    orig_line = line
                    line = replace[line]
                    if orig_line in replace_unremoved:
                        del replace_unremoved[orig_line]
                new_content.append(line)
        new_content.extend(just_add)
        if remove_unremoved or replace_unremoved:
            self.logger.warning(
                _(
                    'Some changes to {file} could not be reverted. More '
                    'details can be found in the log.'
                ).format(
                    file=filename,
                )
            )
        if remove_unremoved:
            self.logger.debug(
                (
                    'The following lines were not found in {file} and so '
                    'were not removed:\n{lines}'
                ).format(
                    file=filename,
                    lines='\n'.join(
                        [
                            '\t{line}'.format(line=newline)
                            for newline in remove_unremoved
                        ]
                    ),
                )
            )
        if replace_unremoved:
            self.logger.debug(
                (
                    'The following lines were not found in {file} and so '
                    'were not reverted to their old content:\n{lines}'
                ).format(
                    file=filename,
                    lines='\n'.join(
                        [
                            '\tnew:\t{new}\n\told:\t{old}\n'.format(
                                new=new,
                                old=old,
                            )
                            for new, old in replace_unremoved.items()
                        ]
                    ),
                )
            )

        if new_content != old_content:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=filename,
                    content=new_content
                )
            )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._files = {}
        self._toremove = None
        self._lines = {}
        self._descriptions = {}

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_GROUPS,
            ''
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.ASK_GROUPS,
            True
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.FILES_TO_REMOVE,
            []
        )

        # TODO: check if we need to allow to override this by answer file.
        # Using a list here won't allow you to override this
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST,
            []
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_CHANGED,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.REMOVE_CUSTOMIZATION_GROUPS,
        after=(
            osetupcons.Stages.REMOVE_CUSTOMIZATION_COMMON,
        ),
    )
    def _customization(self):
        interactive = self.environment[
            osetupcons.RemoveEnv.ASK_GROUPS
        ]
        for group, description in self.environment[
            osetupcons.CoreEnv.UNINSTALL_GROUPS_DESCRIPTIONS
        ].items():
            add_group = self.environment[
                osetupcons.RemoveEnv.REMOVE_ALL
            ]
            if not add_group:
                if group in self.environment[
                    osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST
                ]:
                    add_group = True

            if (
                not add_group and
                interactive
            ):
                if group not in self.environment[
                    osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST
                ]:
                    add_group = dialog.queryBoolean(
                        dialog=self.dialog,
                        name='OVESETUP_REMOVE_GROUP/' + group,
                        note=_(
                            'Do you want to remove {description}? '
                            '(@VALUES@) [@DEFAULT@]: '
                        ).format(
                            description=description,
                        ),
                        prompt=True,
                        true=_('Yes'),
                        false=_('No'),
                        default=False,
                    )
            if add_group:
                self.environment[
                    osetupcons.RemoveEnv.
                    REMOVE_GROUPS
                ] += ',' + group

        remove_groups_set = set([
            x.strip()
            for x in self.environment[
                osetupcons.RemoveEnv.REMOVE_GROUPS
            ].split(',')
            if x.strip()
        ])
        self._toremove = set([
            name
            for name, info in self.environment[
                osetupcons.CoreEnv.UNINSTALL_FILES_INFO
            ].items()
            if (
                'unremovable' not in info['groups']
            ) and True in [
                g in remove_groups_set
                for g in info['groups']
            ]
        ])
        changed = [
            name
            for name, info in self.environment[
                osetupcons.CoreEnv.UNINSTALL_FILES_INFO
            ].items()
            if info.get('changed')
        ]

        if changed:
            if self.environment[osetupcons.RemoveEnv.REMOVE_CHANGED] is None:
                self.environment[
                    osetupcons.RemoveEnv.REMOVE_CHANGED
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_REMOVE_CHANGED',
                    note=_(
                        'The following files were changed since setup:\n'
                        '{files}\n'
                        'Remove them anyway? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        files='\n'.join(changed),
                    ),
                    prompt=True,
                    true=_('Yes'),
                    false=_('No'),
                    default=True,
                )

        if not self.environment[osetupcons.RemoveEnv.REMOVE_CHANGED]:
            self._toremove -= set(changed)

        self.environment[
            osetupcons.RemoveEnv.FILES_TO_REMOVE
        ] = self._toremove

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
        self.logger.info(_('Removing files'))
        for f in self._toremove:
            if os.path.exists(f):
                self._safeDelete(f)

            elif os.path.islink(f):
                # dead link
                self._safeDelete(f)

        self.logger.info(_('Reverting changes to files'))
        for f, changes in self.environment[
            osetupcons.CoreEnv.UNINSTALL_LINES_INFO
        ].items():
            if os.path.exists(f):
                self._revertChanges(f, changes)

        for info in self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNINSTALL_FILES
        ]:
            self._safeDelete(info)

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
        all_groups = set(self.environment[
            osetupcons.CoreEnv.UNINSTALL_GROUPS_DESCRIPTIONS
        ].keys())
        uninstalled_groups = set([
            x.strip()
            for x in self.environment[
                osetupcons.RemoveEnv.REMOVE_GROUPS
            ].split(',')
            if x.strip()
        ])
        not_uninstalled = set(all_groups - uninstalled_groups)
        for group in not_uninstalled:
            self.dialog.note(
                text=_(
                    '{description} files not removed'
                ).format(
                    description=self.environment[
                        osetupcons.CoreEnv.UNINSTALL_GROUPS_DESCRIPTIONS
                    ][group],
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
