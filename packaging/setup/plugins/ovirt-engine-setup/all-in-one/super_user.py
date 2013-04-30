#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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
AIO super user password plugin.
"""

import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    AIO super user password plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _validateUserPasswd(self, host, user, password):
        valid = False
        import paramiko
        try:
            cli = paramiko.SSHClient()
            cli.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            cli.connect(
                hostname=host,
                username=user,
                password=password
            )
            valid = True
        except paramiko.AuthenticationException:
            pass
        finally:
            cli.close()
        return valid

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.AIOEnv.ROOT_PASSWORD,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ],
        name=osetupcons.Stages.AIO_CONFIG_ROOT_PASSWORD
    )
    def _customization(self):
        interactive = (
            self.environment[osetupcons.AIOEnv.ROOT_PASSWORD] is None
        )
        while self.environment[osetupcons.AIOEnv.ROOT_PASSWORD] is None:
            password = self.dialog.queryString(
                name='AIO_ROOT_PASSWORD',
                note=_("Enter 'root' user password: "),
                prompt=True,
                hidden=True,
            )
            if self._validateUserPasswd(
                host='localhost',
                user='root',
                password=password
            ):
                self.environment[osetupcons.AIOEnv.ROOT_PASSWORD] = password
            else:
                if interactive:
                    self.logger.error(_('Wrong root password, try again'))
                else:
                    raise RuntimeError(_('Wrong root password'))

        self.environment[otopicons.CoreEnv.LOG_FILTER].append(
            self.environment[osetupcons.AIOEnv.ROOT_PASSWORD]
        )


# vim: expandtab tabstop=4 shiftwidth=4
