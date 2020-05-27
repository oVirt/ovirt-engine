#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


from otopi import base
from otopi import util


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

    def copy_from_engine(self, file_name, dialog_name=None):
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
