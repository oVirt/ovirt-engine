#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-engine system plugin."""


from otopi import util

from . import he


@util.export
def createPlugins(context):
    he.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
