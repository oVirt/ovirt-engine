#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup config plugin."""


from otopi import util

from . import java


@util.export
def createPlugins(context):
    java.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
