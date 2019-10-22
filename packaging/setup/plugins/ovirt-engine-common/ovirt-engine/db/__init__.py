#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup datebase plugin."""


from otopi import util

from . import config
from . import connection
from . import pgpass


@util.export
def createPlugins(context):
    connection.Plugin(context=context)
    config.Plugin(context=context)
    pgpass.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
