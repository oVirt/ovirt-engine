#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup pki plugin."""


from otopi import util

from . import database
from . import postgres


@util.export
def createPlugins(context):
    database.Plugin(context=context)
    postgres.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
