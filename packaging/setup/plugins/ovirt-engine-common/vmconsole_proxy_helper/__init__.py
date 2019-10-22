#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup vmconsole_proxy plugin."""


from otopi import util

from . import core


@util.export
def createPlugins(context):
    core.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
