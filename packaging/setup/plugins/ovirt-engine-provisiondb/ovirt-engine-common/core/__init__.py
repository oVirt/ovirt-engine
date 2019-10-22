#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup core plugin."""


from otopi import util

from . import misc
from . import offlinepackager


@util.export
def createPlugins(context):
    misc.Plugin(context=context)
    offlinepackager.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
