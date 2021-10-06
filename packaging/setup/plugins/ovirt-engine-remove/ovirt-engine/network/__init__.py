#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup network plugin."""


from otopi import util

from . import ovirtproviderovn


@util.export
def createPlugins(context):
    ovirtproviderovn.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
