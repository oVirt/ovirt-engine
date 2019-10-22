#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-provider-ovn plugin."""
from otopi import util

from . import macpools
from . import ovirtproviderovn


@util.export
def createPlugins(context):
    ovirtproviderovn.Plugin(context=context)
    macpools.Plugin(context=context)
