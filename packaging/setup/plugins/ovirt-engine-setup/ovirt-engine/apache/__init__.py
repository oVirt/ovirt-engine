#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup apache plugin."""


from otopi import util

from . import engine
from . import root
from . import runner
from . import selinux


@util.export
def createPlugins(context):
    engine.Plugin(context=context)
    root.Plugin(context=context)
    runner.Plugin(context=context)
    selinux.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
