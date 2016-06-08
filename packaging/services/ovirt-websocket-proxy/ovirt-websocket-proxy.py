#!/usr/bin/python

# Copyright (C) 2013-2015 Red Hat, Inc.
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

import config
import gettext
import json
import os
import sys
import urllib

import websockify

from ovirt_engine import configfile, service, ticket


def websockify_has_plugins():
    try:
        import websockify.token_plugins as imported
        wstokens = imported
    except ImportError:
        wstokens = None
    return wstokens is not None


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


class OvirtProxyRequestHandler(websockify.ProxyRequestHandler):
    def __init__(self, retsock, address, proxy, *args, **kwargs):
        self._proxy = proxy
        websockify.ProxyRequestHandler.__init__(self, retsock, address, proxy,
                                                *args, **kwargs)

    def get_target(self, target_cfg, path):
        """
        Parses the path, extracts a token, and looks for a valid
        target for that token in the configuration file(s). Returns
        target_host and target_port if successful and sets an ssl_target
        flag.
        """
        connection_data = json.loads(urllib.unquote(
            self._proxy._ticketDecoder.decode(path[1:])))
        target_host = connection_data['host'].encode('utf8')
        target_port = connection_data['port'].encode('utf8')
        self.server.ssl_target = connection_data['ssl_target']
        return (target_host, target_port)


class OvirtWebSocketProxy(websockify.WebSocketProxy):
    """"
    Websocket proxy for usage with oVirt engine.
    Leverages websocket.py by Joel Martin
    """

    def __init__(self, *args, **kwargs):
        self._ticketDecoder = kwargs.pop('ticketDecoder')
        self._logger = kwargs.pop('logger')
        super(OvirtWebSocketProxy, self).__init__(*args, **kwargs)

    def get_logger(self):
        return self._logger


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

    def daemonContext(self):
        with open(
            self._config.get(
                'CERT_FOR_DATA_VERIFICATION'
            )
        ) as f:
            peer = f.read()

        if websockify_has_plugins():
            kwargs = {'token_plugin': 'TokenFile'}
        else:
            kwargs = {'target_cfg': '/dummy'}

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
            logger=self._logger,
            cert=self._config.get('SSL_CERTIFICATE'),
            key=self._config.get('SSL_KEY'),
            ssl_only=self._config.getboolean('SSL_ONLY'),
            daemon=False,
            record=(
                None if not self._config.getboolean('TRACE_ENABLE')
                else self._config.get('TRACE_FILE')
            ),
            web=None,
            target_host=None,
            target_port=None,
            wrap_mode='exit',
            wrap_cmd=None,
            RequestHandlerClass=OvirtProxyRequestHandler,
            **kwargs
        ).start_server()


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
