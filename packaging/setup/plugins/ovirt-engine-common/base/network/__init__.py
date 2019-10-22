#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup network plugin."""

from otopi import util

from . import firewall_manager
from . import firewall_manager_firewalld
from . import firewall_manager_human
from . import firewall_manager_iptables
from . import hostname


@util.export
def createPlugins(context):
    firewall_manager.Plugin(context=context)
    firewall_manager_firewalld.Plugin(context=context)
    firewall_manager_human.Plugin(context=context)
    firewall_manager_iptables.Plugin(context=context)
    hostname.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
