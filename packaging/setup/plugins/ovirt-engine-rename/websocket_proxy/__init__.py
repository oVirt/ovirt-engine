#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-rename core plugin."""


from otopi import util

from . import engine_db
from . import pki
from . import service


@util.export
def createPlugins(context):
    engine_db.Plugin(context=context)
    pki.Plugin(context=context)
    service.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
