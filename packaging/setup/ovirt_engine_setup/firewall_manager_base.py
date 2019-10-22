#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall manager base
"""


from otopi import base
from otopi import util


@util.export
class FirewallManagerBase(base.Base):

    def __init__(self, plugin):
        super(FirewallManagerBase, self).__init__()
        self._plugin = plugin

    @property
    def plugin(self):
        return self._plugin

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def name(self):
        raise RuntimeError('Unset')

    def __str__(self):
        return self.name

    def selectable(self):
        return True

    def detect(self):
        return False

    def active(self):
        return False

    def enable(self):
        pass

    def remove(self):
        pass

    def prepare_examples(self):
        pass

    def print_manual_configuration_instructions(self):
        pass

    def review_config(self):
        pass


# vim: expandtab tabstop=4 shiftwidth=4
