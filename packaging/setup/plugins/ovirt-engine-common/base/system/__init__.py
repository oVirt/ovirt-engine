#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup system plugin."""


from otopi import util

from . import hostile_services
from . import mem


@util.export
def createPlugins(context):
    hostile_services.Plugin(context=context)
    mem.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
