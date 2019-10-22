#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup system plugin."""


from otopi import util

from . import engine
from . import exportfs
from . import image_upload
from . import memcheck
from . import nfs


@util.export
def createPlugins(context):
    engine.Plugin(context=context)
    memcheck.Plugin(context=context)
    nfs.Plugin(context=context)
    exportfs.Plugin(context=context)
    image_upload.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
