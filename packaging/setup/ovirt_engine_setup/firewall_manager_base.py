#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""
Firewall manager base
"""


from otopi import base, util


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
