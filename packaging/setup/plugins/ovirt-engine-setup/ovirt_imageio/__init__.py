#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-imageio setup plugin."""


from otopi import util

from . import config


@util.export
def createPlugins(context):
    config.Plugin(context=context)
