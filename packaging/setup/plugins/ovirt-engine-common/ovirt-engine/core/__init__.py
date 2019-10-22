#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-engine-common core plugin."""


from otopi import util

from . import engine
from . import fence_kdump_listener


@util.export
def createPlugins(context):
    engine.Plugin(context=context)
    fence_kdump_listener.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
