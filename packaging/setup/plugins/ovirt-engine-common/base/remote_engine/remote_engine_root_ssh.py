#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import socket
import time

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import remote_engine_base


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    class _RootSshManager(remote_engine_base.RemoteEngineBase):

        def __init__(self, plugin):
            super(Plugin._RootSshManager, self).__init__(plugin=plugin)
            self._plugin = plugin
            self._client = None
            self._fqdn = None

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
            return osetupcons.Const.REMOTE_ENGINE_SETUP_STYLE_AUTO_SSH

        def desc(self):
            return _('Access remote engine server using ssh as root')

        def _ssh_get_port(self):
            import paramiko
            port_valid = False
            key = osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_SSH_PORT
            port = self.environment[key]
            interactive = False
            while not port_valid:
                try:
                    if port is None:
                        interactive = True
                        port = int(
                            self.dialog.queryString(
                                name='SSH_ACCESS_REMOTE_ENGINE_PORT',
                                note=_(
                                    'ssh port on remote engine server '
                                    '[@DEFAULT@]: '
                                ),
                                prompt=True,
                                default=22,
                            )
                        )
                        self.environment[key] = port
                    paramiko.Transport((self._fqdn, port))
                    port_valid = True
                except ValueError as e:
                    self.logger.debug('exception', exc_info=True)
                    msg = _(
                        'Invalid port number: {error}'
                    ).format(
                        error=e,
                    )
                    if interactive:
                        self.logger.error(msg)
                    else:
                        raise RuntimeError(msg)
                except (paramiko.SSHException, socket.gaierror) as e:
                    self.logger.debug('exception', exc_info=True)
                    msg = _(
                        'Unable to connect to {fqdn}:{port}: {error}'
                    ).format(
                        fqdn=self._fqdn,
                        port=port,
                        error=e,
                    )
                    if interactive:
                        self.logger.error(msg)
                    else:
                        raise RuntimeError(msg)

        def _ssh_connect(self):
            import paramiko
            connected = False
            interactive = False
            password = self.environment[
                osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_ROOT_PASSWORD
            ]
            bad_password = False
            while not connected:
                try:
                    if password is None or bad_password:
                        interactive = True
                        password = self.dialog.queryString(
                            name='SSH_ACCESS_REMOTE_ENGINE_PASSWORD',
                            note=_(
                                'root password on remote engine server '
                                '{fqdn}: '
                            ).format(
                                fqdn=self._fqdn,
                            ),
                            prompt=True,
                            hidden=True,
                            default='',
                        )
                    client = paramiko.SSHClient()
                    client.set_missing_host_key_policy(
                        paramiko.WarningPolicy()
                    )
                    # TODO Currently the warning goes only to the log file.
                    # We should probably write our own policy with a custom
                    # exception so that we can catch it below and verify with
                    # the user that it's ok.
                    client.load_system_host_keys(
                        self.environment[
                            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_KNOWN_HOSTS
                        ]
                    )
                    client.connect(
                        hostname=self._fqdn,
                        port=self.environment[
                            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_SSH_PORT
                        ],
                        username='root',
                        password=password,
                        key_filename=self.environment[
                            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_CLIENT_KEY
                        ],
                    )
                    self._client = client
                    connected = True
                except (
                    paramiko.SSHException,
                    paramiko.AuthenticationException,
                    socket.gaierror,
                ) as e:
                    self.logger.debug('exception', exc_info=True)
                    msg = _('Error: {error}').format(error=e)
                    if interactive:
                        self.logger.error(msg)
                    else:
                        raise RuntimeError(msg)
                    bad_password = True
            self.environment[
                osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_ROOT_PASSWORD
            ] = password

        def configure(self, fqdn):
            self._fqdn = fqdn
            self._ssh_get_port()
            self._ssh_connect()

        def execute_on_engine(self, cmd, timeout=60, text=None):
            # Currently do not allow writing to stdin, only "batch mode"
            # TODO consider something more complex/general, e.g. writing
            # to stdin/reading from stdout interactively
            self.logger.debug(
                'Executing on remote engine %s: %s' %
                (
                    self._fqdn,
                    cmd,
                )
            )
            stdin, stdout, stderr = self._client.exec_command(cmd)
            # exec_command creates a single new channel - stdin.channel,
            # stdout.channel, stderr.channel point to the same object.
            channel = stdin.channel
            # We are not going to write to its stdin
            channel.shutdown_write()
            stdin.close()

            outbuf = []
            errbuf = []
            exited = False
            rc = None
            outbuf.append(channel.recv(len(channel.in_buffer)))
            errbuf.append(channel.recv_stderr(len(channel.in_stderr_buffer)))
            while (
                not channel.closed or
                channel.recv_ready() or
                channel.recv_stderr_ready()
            ) and (timeout > 0):
                time.sleep(1)
                timeout -= 1
                got_data = False
                if channel.recv_ready():
                    got_data = True
                    outbuf.append(channel.recv(len(channel.in_buffer)))
                if channel.recv_stderr_ready():
                    got_data = True
                    errbuf.append(
                        channel.recv_stderr(len(channel.in_stderr_buffer))
                    )
                if (
                    not got_data and
                    channel.exit_status_ready() and
                    not channel.recv_ready()
                ):
                    channel.shutdown_read()
                    channel.close()
                    break
            stdout.close()
            stderr.close()

            if channel.exit_status_ready():
                exited = True
                rc = channel.recv_exit_status()

            res = {
                'stdout': outbuf,
                'stderr': errbuf,
                'exited': exited,
                'rc': rc,
            }
            self.logger.debug('Executing on remote engine result: %s', res)
            return res

        def copy_from_engine(self, file_name, dialog_name=None):
            self.logger.debug(
                'Copying data from remote engine %s:%s' %
                (
                    self._fqdn,
                    file_name,
                )
            )
            sf = self._client.open_sftp()
            res = None
            f = None
            try:
                f = sf.open(file_name, 'r')
                res = f.read()
            finally:
                if f:
                    f.close()
            return res

        def copy_to_engine(
            self,
            file_name,
            content,
            inp_env_key=None,
            uid=None,
            gid=None,
            mode=None,
        ):
            self.logger.debug(
                'Copying data to remote engine %s:%s' %
                (
                    self._fqdn,
                    file_name,
                )
            )
            sf = self._client.open_sftp()
            f = None
            try:
                f = sf.open(file_name, 'w')
                f.write(content)
            finally:
                if f:
                    f.close()
            if uid and gid:
                sf.chown(file_name, uid, gid)
            if mode:
                sf.chmod(file_name, mode)

        def cleanup(self):
            if self._client:
                self._client.close()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        # We want to be the default, so add early
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _setup(self):
        if not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            self.environment[
                osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLES
            ].append(
                self._RootSshManager(
                    plugin=self,
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
