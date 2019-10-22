#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-engine-setup core plugin."""


from otopi import util

from . import advertise_dwh
from . import external_truststore
from . import misc


@util.export
def createPlugins(context):
    misc.Plugin(context=context)
    advertise_dwh.Plugin(context=context)
    external_truststore.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
