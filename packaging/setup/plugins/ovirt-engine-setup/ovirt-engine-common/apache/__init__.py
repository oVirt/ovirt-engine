#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup apache plugin."""


from otopi import util

from . import core
from . import misc
from . import selinux
from . import ssl


@util.export
def createPlugins(context):
    core.Plugin(context=context)
    misc.Plugin(context=context)
    selinux.Plugin(context=context)
    ssl.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
