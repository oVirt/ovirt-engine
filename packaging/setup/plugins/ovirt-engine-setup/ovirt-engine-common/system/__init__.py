#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup system plugin."""


from otopi import util

from . import sysctl


@util.export
def createPlugins(context):
    sysctl.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
