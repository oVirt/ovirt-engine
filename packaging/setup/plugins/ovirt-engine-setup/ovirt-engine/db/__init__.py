#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Various database plugins."""


from otopi import util

from . import configuration
from . import connection
from . import dbmsupgrade
from . import schema
from . import vacuum


@util.export
def createPlugins(context):
    connection.Plugin(context=context)
    configuration.Plugin(context=context)
    dbmsupgrade.Plugin(context=context)
    schema.Plugin(context=context)
    vacuum.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
