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
import gettext
import socket
import base64
import json
import datetime
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


from M2Crypto import X509


import service
import config


import websockify


class OvirtWebSocketProxy(websockify.WebSocketProxy):
    """"
    Websocket proxy for usage with oVirt engine.
    Leverages websocket.py by Joel Martin
    """

    def __init__(self, *args, **kwargs):
        self._ticketDecoder = kwargs.pop('ticketDecoder')
        super(OvirtWebSocketProxy, self).__init__(*args, **kwargs)

    def new_client(self):
        """
        Called after a new WebSocket connection has been established.
        """
        connection_data = self._ticketDecoder.decode(self.path[1:]).split(':')
        target_host = connection_data[0].encode('utf8')
        target_port = connection_data[1].encode('utf8')

        # Connect to the target
        self.msg("connecting to: %s:%s" % (
                 target_host, target_port))
        tsock = self.socket(target_host, target_port,
                            connect=True)

        # Start proxying
        try:
            self.do_proxy(tsock)
        except:
            if tsock:
                tsock.shutdown(socket.SHUT_RDWR)
                tsock.close()
                self.vmsg("%s:%s: Target closed" % (target_host, target_port))
            raise


class TicketDecoder(object):

    def __init__(
            self,
            insecure,
            certificate
    ):
        self._insecure = insecure
        if not insecure:
            self._key = X509.load_cert(
                certificate,
                X509.FORMAT_PEM,
            ).get_pubkey()

    def decode(self, ticket):
        decoded = json.loads(base64.b64decode(ticket))
        if not self._insecure:
            self._key.verify_init()
            for field in decoded['signedFields'].split(','):
                self._key.verify_update(decoded[field].encode('utf8'))
            if self._key.verify_final(
                base64.b64decode(decoded['signature'])
            ) != 1:
                raise ValueError('Invalid ticket signature')
        if (
            datetime.datetime.utcnow() -
            datetime.datetime.strptime(decoded['validFrom'], '%Y%m%d%H%M%S')
        ) < datetime.timedelta():
            raise ValueError('Ticket life time expired')
        if (
            datetime.datetime.strptime(decoded['validTo'], '%Y%m%d%H%M%S') -
            datetime.datetime.utcnow()
        ) < datetime.timedelta():
            raise ValueError('Ticket life time expired')
        return decoded['data']


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()

    def _checkInstallation(
        self,
        pidfile,
    ):
        # Check the required engine directories and files:
        self.check(
            os.path.join(
                self._config.getString('ENGINE_USR'),
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

    def daemonSetup(self):

        if not os.path.exists(config.ENGINE_WSPROXY_DEFAULT_FILE):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=config.ENGINE_WSPROXY_DEFAULT_FILE,
                )
            )

        self._config = service.ConfigFile(
            (
                config.ENGINE_WSPROXY_DEFAULT_FILE,
                config.ENGINE_WSPROXY_VARS,
            ),
        )

        self._checkInstallation(
            pidfile=self.pidfile,
        )

    def daemonContext(self):
        record = self._config.getString('LOG_FILE')
        if record == 'False':
            record = False # translate to boolean

        OvirtWebSocketProxy(
            listen_host=self._config.getString('PROXY_HOST'),
            listen_port=self._config.getString('PROXY_PORT'),
            source_is_ipv6=self._config.getBoolean('SOURCE_IS_IPV6'),
            verbose=self._config.getBoolean('LOG_VERBOSE'),
            ticketDecoder=TicketDecoder(
                insecure=not self._config.getBoolean(
                    'FORCE_DATA_VERIFICATION'
                ),
                certificate=self._config.getString(
                    'CERT_FOR_DATA_VERIFICATION'
                )
            ),
            cert=self._config.getString('SSL_CERTIFICATE'),
            key=self._config.getString('SSL_KEY'),
            ssl_only=self._config.getBoolean('SSL_ONLY'),
            daemon=False,
            record=record,
            web=None,
            target_host='ignore',
            target_port='ignore',
            wrap_mode='exit',
            wrap_cmd=None
        ).start_server()


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
