#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-imageio plugin."""


import datetime
import gettext
import os
import textwrap

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.ovirt_imageio import constants as oipcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-imageio-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-imageio plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = None
        self._notifications = []

    def _file_modified(self, path, new_content):
        if os.path.exists(path):
            with open(path) as f:
                old_content = f.read()
            return old_content != new_content
        return False

    def _conffile_was_written_by_previous_setup(self, conffile):
        return self.environment[
            osetupcons.CoreEnv.UNINSTALL_FILES_INFO
        ].get(conffile) is not None

    def _write_new_config(self, file_name, content):
        timestamp = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        new_file_name =  "{}.new.{}".format(file_name, timestamp)

        # If we are writing a new file, write it immediately.
        # Note that this will not be rolled back by engine-setup
        # if there is some failure.
        local_transaction = transaction.Transaction()
        with local_transaction:
            local_transaction.append(
                filetransaction.FileTransaction(
                    name=new_file_name,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )
        self._notifications.append((file_name, new_file_name))

    def _write_config(self, file_name, content):
        # If updating the existing file (or writing it new), do this
        # in the main transaction. So will be rolled back if there is
        # a problem.
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=file_name,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    def _update_config(self, path, content):
        """
        If we wrote the config in the past and user modified the file,
        write a ".new.timestamp" file.
        Otherwise rewrite the file so we have new hash in the
        uninstall info.
        """
        if self._conffile_was_written_by_previous_setup(path):
            if self._file_modified(path, content):
                # Write a new file, do not touch existing one that user
                # changed.
                self._write_new_config(path, content)
            else:
                # We wrote the file in the past, the user changed it, but
                # changed it to have the same content we already want to write.
                # So output nothing to the user, but still re-write the file,
                # so that we have the new hash in uninstall info.
                self.logger.debug(
                    'Rewriting %s in order to update uninstall info',
                    path,
                )
                self._write_config(path, content)
        else:
            # We haven't written the file in the past. Write it now and if there
            # is any such file, overwrite it.
            self._write_config(path, content)

    def _get_configuration(self):
        return textwrap.dedent(oipcons.ImageIO.CONFIG_TEMPLATE).format(
            key_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
            cert_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
            ca_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT,
            host=self.environment[osetupcons.ConfigEnv.FQDN],
            data_port=oipcons.ImageIO.DATA_PORT,
            control_port=oipcons.ImageIO.CONTROL_PORT,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
                osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
                osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
        name=oipcons.ImageIO.CONFIG_STAGE,
    )
    def _customization_enable(self):
        self._enabled = self.environment[oenginecons.CoreEnv.ENABLE]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
                osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
                osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
                oipcons.ImageIO.CONFIG_STAGE,
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-imageio',
                'directory': 'ovirt-imageio'
            },
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc_config(self):
        self._update_config(
            oipcons.ImageIO.DAEMON_CONFIG,
            self._get_configuration()
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
                self.environment[
                    osetupcons.CoreEnv.DEVELOPER_MODE
                ] and self._enabled
        ),
    )
    def _misc_dev_logger_config(self):
        self._update_config(
            oipcons.ImageIO.LOGGER_CONFIG,
            textwrap.dedent(oipcons.ImageIO.LOGGER)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
                osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
                osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._notifications,
    )
    def _closeup_notify_new_config(self):
        for cur_conf, new_conf in self._notifications:
            self.dialog.note(
                _(
                    'Did not update {} because it was changed manually. You '
                    'might want to compare it with {} and edit as needed.'
                ).format(cur_conf, new_conf)
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
                not self.environment[
                    osetupcons.CoreEnv.DEVELOPER_MODE
                ] and self._enabled
        ),
    )
    def _closeup_restart_service(self):
        for state in (False, True):
            self.services.state(
                name=oipcons.ImageIO.SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=oipcons.ImageIO.SERVICE_NAME,
            state=True,
        )
