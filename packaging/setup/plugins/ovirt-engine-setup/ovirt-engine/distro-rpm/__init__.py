#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
ovirt-host-setup distro-rpm plugin.
Includes code relevant for rpm-based distributions
"""


from otopi import util

from . import packages


@util.export
def createPlugins(context):
    packages.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
