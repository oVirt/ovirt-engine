#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-engine-setup system plugin."""


from otopi import util

from . import environment


@util.export
def createPlugins(context):
    environment.Plugin(context=context)

# vim: expandtab tabstop=4 shiftwidth=4
