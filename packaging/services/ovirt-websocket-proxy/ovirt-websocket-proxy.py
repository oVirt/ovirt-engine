#!/usr/bin/python

# Copyright 2013 Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import sys
import signal
import gettext
import json
import urllib
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


import websockify


import config


from ovirt_engine import configfile
from ovirt_engine import service
from ovirt_engine import ticket


class OvirtWebSocketProxy(websockify.WebSocketProxy):
    """"
    Websocket proxy for usage with oVirt engine.
    Leverages websocket.py by Joel Martin
    """

    def __init__(self, *args, **kwargs):
        self._ticketDecoder = kwargs.pop('ticketDecoder')
        super(OvirtWebSocketProxy, self).__init__(*args, **kwargs)

    def get_target(self, target_cfg, path):
        """
        Parses the path, extracts a token, and looks for a valid
        target for that token in the configuration file(s). Returns
        target_host and target_port if successful and sets an ssl_target
        flag.
        """
        connection_data = json.loads(
            urllib.unquote(self._ticketDecoder.decode(path[1:]))
        )
        target_host = connection_data['host'].encode('utf8')
        target_port = connection_data['port'].encode('utf8')
        self.ssl_target = connection_data['ssl_target']
        return (target_host, target_port)


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()
        self._defaults = os.path.abspath(
            os.path.join(
                os.path.dirname(sys.argv[0]),
                'ovirt-websocket-proxy.conf',
            )
        )

    def _checkInstallation(
        self,
        pidfile,
    ):
        # Check the required engine directories and files:
        self.check(
            os.path.join(
                self._config.get('ENGINE_USR'),
                'services',
            ),
            directory=True,
        )

        if pidfile is not None:
            self.check(
                name=pidfile,
                writable=True,
                mustExist=False,
            )

        if (
            self._config.getboolean('SSL_ONLY') and
            (
                not os.path.exists(self._config.get('SSL_KEY')) or
                not os.path.exists(self._config.get('SSL_CERTIFICATE'))
            )
        ):
            raise RuntimeError(
                _(
                    "SSL_ONLY is set but SSL_CERTIFICATE "
                    "or SSL_KEY file not found."
                )
            )

    def daemonSetup(self):

        if not os.path.exists(self._defaults):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=self._defaults,
                )
            )

        self._config = configfile.ConfigFile(
            (
                self._defaults,
                config.ENGINE_WSPROXY_VARS,
            ),
        )

        self._checkInstallation(
            pidfile=self.pidfile,
        )

    def daemonStdHandles(self):
        consoleLog = open(os.devnull, 'w+')
        return (consoleLog, consoleLog)

    def daemonContext(self):
        #
        # WORKAROUND-BEGIN
        # set terminate exception as
        # the websockify library assumes interactive
        # mode only (SIGINT).
        # it also expect exit at the middle of processing.
        # so we comply.
        def myterm(signo, frame):
            sys.exit(0)
        oldterm = signal.getsignal(signal.SIGTERM)
        signal.signal(signal.SIGTERM, myterm)
        # WORKAROUND-END

        try:
            with open(
                self._config.get(
                    'CERT_FOR_DATA_VERIFICATION'
                )
            ) as f:
                peer = f.read()

            OvirtWebSocketProxy(
                listen_host=self._config.get('PROXY_HOST'),
                listen_port=self._config.get('PROXY_PORT'),
                source_is_ipv6=self._config.getboolean('SOURCE_IS_IPV6'),
                verbose=self.debug,
                ticketDecoder=ticket.TicketDecoder(
                    ca=None,
                    eku=None,
                    peer=peer,
                ),
                cert=self._config.get('SSL_CERTIFICATE'),
                key=self._config.get('SSL_KEY'),
                ssl_only=self._config.getboolean('SSL_ONLY'),
                daemon=False,
                record=(
                    None if not self._config.getboolean('TRACE_ENABLE')
                    else self._config.get('TRACE_FILE')
                ),
                web=None,
                target_cfg='/dummy',
                target_host=None,
                target_port=None,
                wrap_mode='exit',
                wrap_cmd=None
            ).start_server()
        # WORKAROUND-BEGIN
        # websockify exit because of signals.
        # redirect it to expected termination sequence.
        except SystemExit:
            self.logger.debug('SystemExit', exc_info=True)
        finally:
            signal.signal(signal.SIGTERM, oldterm)
        # WORKAROUND-END


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
