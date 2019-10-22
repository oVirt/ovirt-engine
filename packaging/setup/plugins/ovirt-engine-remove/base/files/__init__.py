#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup core plugin."""


from otopi import util

from . import simple


@util.export
def createPlugins(context):
    simple.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
