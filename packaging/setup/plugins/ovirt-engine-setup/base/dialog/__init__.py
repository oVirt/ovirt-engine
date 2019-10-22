#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup dialog plugin."""


from otopi import util

from . import preview


@util.export
def createPlugins(context):
    preview.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
