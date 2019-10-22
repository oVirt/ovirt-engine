#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Various database plugins."""


from otopi import util

from . import versions


@util.export
def createPlugins(context):
    versions.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
