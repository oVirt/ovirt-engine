#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup system plugin."""


from otopi import util

from . import engine
from . import memcheck
from . import selinux


@util.export
def createPlugins(context):
    engine.Plugin(context=context)
    memcheck.Plugin(context=context)
    selinux.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
