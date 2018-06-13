#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-rename core plugin."""


from otopi import util

from . import database
from . import engine
from . import misc
from . import ovn
from . import pki
from . import protocols
from . import sso
from . import tools
from . import uninstall


@util.export
def createPlugins(context):
    database.Plugin(context=context)
    engine.Plugin(context=context)
    misc.Plugin(context=context)
    ovn.Plugin(context=context)
    pki.Plugin(context=context)
    protocols.Plugin(context=context)
    sso.Plugin(context=context)
    tools.Plugin(context=context)
    uninstall.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
