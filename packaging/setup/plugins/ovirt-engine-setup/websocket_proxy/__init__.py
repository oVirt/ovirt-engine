#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup websocket_proxy plugin."""


from otopi import util

from . import config
from . import packages
from . import pki


@util.export
def createPlugins(context):
    config.Plugin(context=context)
    packages.Plugin(context=context)
    pki.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
