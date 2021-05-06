#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Titles plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Titles plugin."""

    def _title(self, text):
        self.dialog.note(
            text='\n--== %s ==--\n\n' % text,
        )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_PACKAGES,
        after=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        condition=lambda self: (
            osetuputil.is_ovirt_packaging_supported_distro()
        ),
    )
    def _title_s_packages(self):
        self._title(
            text=_('PACKAGES'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_PACKAGES,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PACKAGES,
        ),
    )
    def _title_e_packages(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        after=(
            osetupcons.Stages.DIALOG_TITLES_E_PACKAGES,
        ),
    )
    def _title_s_network(self):
        self._title(
            text=_('NETWORK CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.NETWORK_OWNERS_CONFIG_CUSTOMIZED,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
    )
    def _network_owners_config_customized(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
    )
    def _title_e_network(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        after=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
    )
    def _title_s_database(self):
        self._title(
            text=_('DATABASE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DB_OWNERS_CONNECTIONS_CUSTOMIZED,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _db_owners_connections_customized(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _title_e_database(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
    )
    def _title_s_engine(self):
        self._title(
            text=_('OVIRT ENGINE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
    )
    def _title_e_engine(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_S_STORAGE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
    )
    def _title_s_storage(self):
        self._title(
            text=_('STORAGE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_E_STORAGE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_STORAGE,
        ),
    )
    def _title_e_storage(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_E_STORAGE,
        ),
    )
    def _title_s_pki(self):
        self._title(
            text=_('PKI CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        ),
    )
    def _title_e_pki(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_S_APACHE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        ),
    )
    def _title_s_apache(self):
        self._title(
            text=_('APACHE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DIALOG_TITLES_E_APACHE,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_APACHE,
        ),
    )
    def _title_e_apache(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        after=(
            oengcommcons.Stages.DIALOG_TITLES_E_APACHE,
        ),
    )
    def _title_s_system(self):
        self._title(
            text=_('SYSTEM CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
    )
    def _title_e_system(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_MISC,
        after=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
    )
    def _title_s_misc(self):
        self._title(
            text=_('MISC CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_MISC,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_MISC,
        ),
    )
    def _title_e_misc(self):
        self._title(
            text=_('END OF CONFIGURATION'),
        )


# vim: expandtab tabstop=4 shiftwidth=4
