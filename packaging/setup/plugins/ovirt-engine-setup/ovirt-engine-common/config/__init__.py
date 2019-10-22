#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup config plugin."""


from otopi import util

from . import firewall
from . import jboss


@util.export
def createPlugins(context):
    jboss.Plugin(context=context)
    firewall.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
