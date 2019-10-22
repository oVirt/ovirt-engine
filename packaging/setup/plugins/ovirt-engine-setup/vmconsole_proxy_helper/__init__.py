#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup vmconsole_proxy plugin."""


from otopi import util

from . import config
from . import pki
from . import system


@util.export
def createPlugins(context):
    config.Plugin(context=context)
    pki.Plugin(context=context)
    system.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
