#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-rename vmconsole_proxy_helper plugin."""


from otopi import util

from . import config


@util.export
def createPlugins(context):
    config.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
