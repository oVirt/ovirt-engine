#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


from otopi import base, util


@util.export
class RemoteEngineBase(base.Base):

    def __init__(self, plugin):
        super(RemoteEngineBase, self).__init__()
        self._plugin = plugin

    @property
    def plugin(self):
        return self._plugin

    @property
    def dialog(self):
        return self._plugin.dialog

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def logger(self):
        return self._plugin.logger

    @property
    def name(self):
        raise RuntimeError('Unset')

    def desc(self):
        raise RuntimeError('Unset')

    def configure(self, fqdn):
        pass

    def execute_on_engine(self, cmd, timeout=60):
        pass

    def copy_from_engine(self, file_name):
        pass

    def copy_to_engine(
        self,
        file_name,
        content,
        inp_env_key=None,
        uid=None,
        gid=None,
        mode=None,
    ):
        pass

    def cleanup(self):
        pass


# vim: expandtab tabstop=4 shiftwidth=4
