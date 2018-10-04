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

import gettext
import json
import os
import socket
import ssl
import struct
import sys
import urllib

import websockify

import config


from ovirt_engine import configfile
from ovirt_engine import service
from ovirt_engine import ticket


def websockify_has_plugins():
    try:
        import websockify.token_plugins as imported
        wstokens = imported
    except ImportError:
        wstokens = None
    return wstokens is not None


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


class VenCryptSocket(object):
    """
    Wrapper around a socket.socket. It takes over the negotiation of VNC
    encrypted with VeNCrypt/X509VNC. Its purpose is to allow noVNC to connect
    to a VM running on libvirtd with vnc_tls enabled. VNC normally does not
    support encrypted connections, so proxy has to handle TLS part, leaving
    password authentication to ncVNC.
    """
    X509VNC = 261
    PASSWORD_AUTHENTICATION = '\x01\x02'
    PASSWORD_AUTHENTICATION_RESPONSE = '\x02'
    VENCRYPT_AUTHENTICATION = '\x01\x13'
    VENCRYPT_AUTHENTICATION_RESPONSE = '\x13'
    VERSION_02 = '\x00\x02'
    VERSION_ACK = '\x00'
    CONNECTION_CLOSE = '\x00\x00'
    METHOD_UNSUPPORTED = '\x00'

    def __init__(self, sock, log):
        self.sock = sock
        self.log = log
        self.recv = self.recv_expect_sectype
        self.send = self.send_passthrough

    def __getattr__(self, attr):
        "Anything that is not defined is passed to the underlying socket."
        return self.sock.__getattribute__(attr)

    def send_passthrough(self, string, flags=0):
        "This version of send passes all to the underlying socket."
        return self.sock.send(string, flags)

    def send_fake_password_protected(self, string, flags=0):
        """
        This is a fake send that expects the client to pass only a password
        authentication byte, but swallows it and does not send it to the
        server.
        """
        # The client should only send a single byte with the selected
        # authentication method (02).
        if string != self.PASSWORD_AUTHENTICATION_RESPONSE:
            msg = "VNC negotitation failed. " \
                + "Expected the client to send \\x02, got %s instead" % string
            self.log.error(msg)
            raise Exception(msg)
        # After that just do the regular send
        self.send = self.send_passthrough
        return 1

    def recv_passthrough(self, bufsize, flags=0):
        "This version of recv uses the underlying socket."
        return self.sock.recv(bufsize, flags)

    def recv_expect_sectype(self, bufsize, flags=0):
        """
        VenCryptSocket waits for the VeNCrypt authentication type,
        passing through all communication that precedes it.
        """
        handshake = self.sock.recv(64, socket.MSG_PEEK)
        if handshake == self.VENCRYPT_AUTHENTICATION:
            self.do_vencrypt_handshake()
            self.recv = self.recv_passthrough
            self.send = self.send_fake_password_protected
            return self.PASSWORD_AUTHENTICATION
        else:
            self.log.debug(
                "Waiting for \\x01\\x13, got: [%s]. Passing through" %
                handshake)
            return self.sock.recv(bufsize, flags)

    def do_vencrypt_handshake(self):
        """
        do_vencrypt_handshake... how should I put it... performs VeNCrypt
        handshake... What did you expect?
        """
        self.log.info("Negotiating VenCrypt protocol")
        self.sock.recv(2)  # Consume \x01\x13, type VeNCrypt
        self.sock.send(self.VENCRYPT_AUTHENTICATION_RESPONSE)

        # Negotiate version
        version = self.sock.recv(2)
        if version != self.VERSION_02:
            self.log.error(
                "Failed to negotiate VeNCrypt: Only version 0.2 is "
                "supported; server requested version '%s'" % version
            )
            self.sock.send(self.CONNECTION_CLOSE)
            return
        self.log.debug("VeNCrypt version {}.{}".format(
            *struct.unpack("bb", version))
        )
        self.sock.send(self.VERSION_02)
        ack = self.sock.recv(1)
        if ack != self.VERSION_ACK:
            # WHA? Server does not support the version it has just sent?
            # Abort
            self.log.error("Failed to negotiate VeNCrypt: server does not "
                           "support proto version {}.{}".format(
                               *struct.unpack("bb", self.VERSION_02)
                           ))
            return

        # Negotiate subtype
        subtypes_number = self.sock.recv(1)
        subtypes_number = struct.unpack('b', subtypes_number)[0]
        if subtypes_number == 0:
            # Server does not support any subtype (silly but possible)
            self.log.error(
                "Failed to negotiate VeNCrypt: "
                "server does not support any subtype")
            return

        subtypes_str = self.sock.recv(subtypes_number * 4)
        subtypes = struct.unpack('>' + 'i'*subtypes_number, subtypes_str)
        self.log.debug("Server supports the following subtypes: %s" % subtypes)
        if self.X509VNC not in subtypes:
            self.log.debug("Server does not support X509VNC. "
                           "OvirtProxy only supports X509VNC")
            self.sock.send(self.METHOD_UNSUPPORTED)
            return
        else:
            self.sock.send(struct.pack('!i', self.X509VNC))

        # Server sends one more byte? What's in it??
        self.sock.recv(1)
        # The handshake confirmation is expected after the whole procedure is
        # done, ie. after TLS is set up, the passoword verification needs to
        # happen. That part is handed back to noNVC.
        #
        # I got this byte by debugging a live connection to a running libvirt.
        #
        # It might be the case that VNC treats X509VNC as two separate security
        # negotiation (1: TLS, 2: password), each acknowledged by that one
        # byte, but this is only my hypothesis.

        # Do a regular TLS negotiation
        self.log.info(
            "VeNCrypt negotiation succeeded. Setting up TLS connection")
        self.sock = ssl.wrap_socket(self.sock)
        self.log.info("VeNCrypt negotiation done")


class OvirtProxyRequestHandler(websockify.ProxyRequestHandler):
    RFB_HANDSHAKE = 'RFB 003.008\n'

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

    def _veNCrypt_socket(self, host, port=None, prefer_ipv6=False,
                         use_ssl=False, tcp_keepalive=True, tcp_keepcnt=None,
                         tcp_keepidle=None, tcp_keepintvl=None):
        """
        Wrap the socket with custom VenCryptSocket class to perform VeNCrypt
        negotiation.
        """
        addrs = socket.getaddrinfo(host, port, 0, socket.SOCK_STREAM,
                                   socket.IPPROTO_TCP, 2)

        if not addrs:
            raise Exception("Could not resolve host '%s'" % host)
        addrs.sort(key=lambda x: x[0])
        if prefer_ipv6:
            addrs.reverse()
        sock = socket.socket(addrs[0][0], addrs[0][1])

        if tcp_keepalive:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
            if tcp_keepcnt:
                sock.setsockopt(socket.SOL_TCP, socket.TCP_KEEPCNT,
                                tcp_keepcnt)
            if tcp_keepidle:
                sock.setsockopt(socket.SOL_TCP, socket.TCP_KEEPIDLE,
                                tcp_keepidle)
            if tcp_keepintvl:
                sock.setsockopt(socket.SOL_TCP, socket.TCP_KEEPINTVL,
                                tcp_keepintvl)

        sock.connect(addrs[0][4])

        handshake = sock.recv(1024, socket.MSG_PEEK)
        if handshake == self.RFB_HANDSHAKE:
            self.log_message("Wrapping RFB protocol in VenCryptSocket")
            sock = VenCryptSocket(sock, self.logger)

        return sock

    def new_websocket_client(self):
        """
        Called after a new WebSocket connection has been established.
        Partially copied from websockify.ProxyRequestHandler to be able to
        take over RFB protocol negotiation.
        """
        if not self.server.ssl_target:
            # Non-SSL connections can be handled the old way
            self.log_message(
                "Not a SSL connection, falling back to standard Websockify"
                " connection handling")
            websockify.ProxyRequestHandler.new_websocket_client(self)

        # Connect to the target
        if self.server.wrap_cmd:
            msg = "connecting to command: '{command}' (port {port})".format(
                command=" ".join(self.server.wrap_cmd),
                port=self.server.target_port
            )
        elif self.server.unix_target:
            msg = "connecting to unix socket: {socket}".format(
                socket=self.server.unix_target
            )
        else:
            msg = "connecting to: {host}:{port}".format(
                host=self.server.target_host,
                port=self.server.target_port
            )

        if self.server.ssl_target:
            msg += " (using SSL)"
        self.log_message(msg)

        tsock = self._veNCrypt_socket(self.server.target_host,
                                      self.server.target_port,
                                      use_ssl=self.server.ssl_target)

        self.print_traffic(self.traffic_legend)

        # Start proxying
        try:
            self.do_proxy(tsock)
        except:
            if tsock:
                tsock.shutdown(socket.SHUT_RDWR)
                tsock.close()
                if self.verbose:
                    self.log_message(
                        "%s:%s: Closed target",
                        self.server.target_host, self.server.target_port)
            raise


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
