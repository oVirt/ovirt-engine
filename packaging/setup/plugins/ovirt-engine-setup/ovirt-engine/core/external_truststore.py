#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import os
import tempfile

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    class ExternalTruststoreRemoveTransaction(transaction.TransactionElement):
        """external_truststore remove transaction element."""

        def __init__(self):
            self.external_truststore = \
                oenginecons.FileLocations.EXTERNAL_TRUSTSTORE
            self.tmp_external_truststore = tempfile.mkstemp(
                dir=os.path.dirname(self.external_truststore)
            )

        def __str__(self):
            return _("External_truststore Remove Transaction")

        def prepare(self):
            """Renaming external_truststore"""

            if os.path.exists(self.external_truststore):
                os.rename(
                    self.external_truststore,
                    self.tmp_external_truststore[1]
                )

        def abort(self):
            "Renaming external_truststore back"

            if os.path.exists(self.tmp_external_truststore[1]):
                os.rename(
                    self.tmp_external_truststore[1],
                    self.external_truststore
                )

        def commit(self):
            """Remove old external_truststore"""

            if os.path.exists(self.tmp_external_truststore[1]):
                os.unlink(self.tmp_external_truststore[1])

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: (
            self.environment[oenginecons.EngineDBEnv.NEW_DATABASE] and
            os.path.exists(oenginecons.FileLocations.EXTERNAL_TRUSTSTORE)
        )
    )
    def remove_external_truststore(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.ExternalTruststoreRemoveTransaction()
        )

# vim: expandtab tabstop=4 shiftwidth=4
