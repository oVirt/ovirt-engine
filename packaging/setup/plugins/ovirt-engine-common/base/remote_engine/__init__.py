#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-remove core plugin."""


from otopi import util

from . import remote_engine
from . import remote_engine_manual_files
from . import remote_engine_root_ssh


@util.export
def createPlugins(context):
    remote_engine.Plugin(context=context)
    remote_engine_manual_files.Plugin(context=context)
    remote_engine_root_ssh.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
