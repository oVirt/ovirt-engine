#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt_engine_setup scram rotation plugin."""


from otopi import util

from . import scram_rotation


@util.export
def createPlugins(context):
    scram_rotation.Plugin(context=context)
