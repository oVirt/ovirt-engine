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


"""Protocols plugin."""


import re
import socket
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Protocols plugin."""
    _IPADDR_RE = re.compile(r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}')

    _DOMAIN_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            [\w\-\_]+
            \.
            [\w\.\-\_]+
            \w+
            $
        """
    )

    _INTERFACE_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            \d+
            :
            \s+
            (?P<interface>\w+)
            :
            \s+
            <(?P<options>[^>]+)
            .*
        """
    )

    _ADDRESS_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            \s+
            inet
            \s
            (?P<address>\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})
            .+
            \s+
            (?P<interface>\w+)
            $
    """
    )

    _DIG_LOOKUP_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            [\w.-]+
            \s+
            \d+
            \s+
            IN
            \s+
            (A|CNAME)
            \s+
            [\w.-]+
        """
    )

    _DIG_REVLOOKUP_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            (?P<query>\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).in-addr.arpa.
            \s+
            \d+
            \s+
            IN
            \s+
            PTR
            \s+
            (?P<answer>[\w.-]+)
            \.
            $
        """
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _getConfiguredIps(self):
        interfaces = {}
        addresses = {}
        rc, stdout, stderr = self.execute(
            args=(
                self.command.get('ip'),
                'addr',
            ),
        )
        for line in stdout:
            interfacematch = self._INTERFACE_RE.match(line)
            addressmatch = self._ADDRESS_RE.match(line)
            if interfacematch is not None:
                interfaces[
                    interfacematch.group('interface')
                ] = 'LOOPBACK' in interfacematch.group('options')
            elif addressmatch is not None:
                addresses.setdefault(
                    addressmatch.group('interface'),
                    []
                ).append(
                    addressmatch.group('address')
                )
        iplist = []
        for interface, loopback in interfaces.iteritems():
            if not loopback:
                iplist.extend(addresses.get(interface, []))
        if len(iplist) < 1:
            raise RuntimeError(
                _('Could not find any configured IP address on the host')
            )
        return set(iplist)

    def _resolvedByDNS(self, fqdn):
        args = [
            self.command.get('dig'),
            fqdn
        ]
        rc, stdout, stderr = self.execute(
            args=args,
            raiseOnError=False
        )
        resolved = False
        if rc == 0:
            for line in stdout:
                if self._DIG_LOOKUP_RE.search(line):
                    resolved = True
        return resolved

    def _dig_reverse_lookup(self, addr):
        names = set()
        args = [
            self.command.get('dig'),
            '-x',
            addr,
        ]
        rc, stdout, stderr = self.execute(
            args=args,
            raiseOnError=False
        )
        if rc == 0:
            for line in stdout:
                found = self._DIG_REVLOOKUP_RE.search(line)
                if found:
                    names.add(found.group('answer'))
        return names

    def _validateFQDNresolvability(self, fqdn):

        #get set of IPs
        ipAddresses = self._getConfiguredIps()
        #resolve fqdn
        try:
            resolvedAddresses = set([
                address[0] for __, __, __, __, address in
                socket.getaddrinfo(
                    fqdn,
                    None
                )
            ])
        except socket.error:
            #can't be resolved
            raise RuntimeError(
                _('{fqdn} did not resolve into an IP address').format(
                    fqdn=fqdn,
                )
            )

        prettyString = ' '.join(resolvedAddresses)
        self.logger.debug(
            '{fqdn} resolves to: {addresses}'.format(
                fqdn=fqdn,
                addresses=prettyString
            )
        )
        resolvedByDNS = self._resolvedByDNS(fqdn)
        source = ('DNS' if resolvedByDNS else _('host file'))
        #compare found IP with list of local IPs and match.
        if not resolvedAddresses.issubset(ipAddresses):
            raise RuntimeError(
                _(
                    'The following addresses resolved from {source}: '
                    "{addresses} can't be mapped "
                    'to non loopback devices on this host'
                ).format(
                    source=source,
                    addresses=prettyString
                )
            )

        if not resolvedByDNS:
            self.logger.warning(
                _(
                    'Failed to resolve {fqdn} using DNS, '
                    'it can be resolved only locally'
                ).format(
                    fqdn=fqdn,
                )
            )
        elif self.environment[osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION]:
            #reverse resolved IP and compare with given fqdn
            revResolved = False
            for address in resolvedAddresses:
                reverseNames = self._dig_reverse_lookup(address)
                for name in reverseNames:
                    revResolved = name.lower() == fqdn.lower()
                    if revResolved:
                        break
                if revResolved:
                    break
            if not revResolved:
                raise RuntimeError(
                    _(
                        'The following addresses: {addresses} did not reverse'
                        'resolve into {fqdn}'
                    ).format(
                        addresses=prettyString,
                        fqdn=fqdn
                    )
                )

    def _validateFQDN(self, fqdn):
        if self._IPADDR_RE.match(fqdn):
            raise RuntimeError(
                _(
                    '{fqdn} is an IP address and not a FQDN. '
                    'A FQDN is needed to be able to generate '
                    'certificates correctly.'
                ).format(
                    fqdn=fqdn,
                )
            )
        if (
            len(fqdn) < 1 or
            len(fqdn) > 1000 or
            not self._DOMAIN_RE.match(fqdn)
        ):
            raise RuntimeError(
                _('{fqdn} has not a valid domain name').format(
                    fqdn=fqdn,
                )
            )
        self._validateFQDNresolvability(fqdn)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION,
            False
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.HTTP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_HTTP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.HTTPS_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_HTTPS_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_HTTP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_HTTPS_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTPS_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_AJP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_AJP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('ip')
        self.command.detect('dig')
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[
                osetupcons.ConfigEnv.HTTP_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                osetupcons.ConfigEnv.HTTPS_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.CONFIG_PROTOCOLS_CUSTOMIZATION,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ],
    )
    def _customization(self):
        interactive = self.environment[osetupcons.ConfigEnv.FQDN] is None
        validFQDN = False
        while not validFQDN:
            if interactive:
                fqdn = socket.getfqdn()
                self.environment[
                    osetupcons.ConfigEnv.FQDN
                ] = self.dialog.queryString(
                    name='OVESETUP_NETWORK_FQDN',
                    note=_(
                        'Host fully qualified DNS name of '
                        'this server [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=fqdn,
                )
            try:
                self._validateFQDN(
                    self.environment[osetupcons.ConfigEnv.FQDN]
                )
                validFQDN = True
            except RuntimeError as e:
                if interactive:
                    self.logger.error(
                        _('FQDN is not valid: {error}').format(
                            error=e
                        )
                    )
                else:
                    raise

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        #
        # TODO
        # Defaults of engine should be HTTP[s]_ENABLED=false
        #
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            content = (
                'ENGINE_FQDN={fqdn}\n'
                'ENGINE_PROXY_ENABLED=false\n'
                'ENGINE_HTTP_ENABLED=true\n'
                'ENGINE_HTTPS_ENABLED=true\n'
                'ENGINE_HTTP_PORT={httpPort}\n'
                'ENGINE_HTTPS_PORT={httpsPort}\n'
                'ENGINE_AJP_ENABLED=false\n'
                'ENGINE_DEBUG_ADDRESS={debugAddress}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                debugAddress=self.environment[
                    osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS
                ],
            )
        else:
            content = (
                'ENGINE_FQDN={fqdn}\n'
                'ENGINE_PROXY_ENABLED=true\n'
                'ENGINE_PROXY_HTTP_PORT={httpPort}\n'
                'ENGINE_PROXY_HTTPS_PORT={httpsPort}\n'
                'ENGINE_HTTP_ENABLED=false\n'
                'ENGINE_HTTPS_ENABLED=false\n'
                'ENGINE_AJP_ENABLED=true\n'
                'ENGINE_AJP_PORT={ajpPort}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                ajpPort=self.environment[
                    osetupcons.ConfigEnv.JBOSS_AJP_PORT
                ],
            )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS
                ),
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Web access is enabled at:\n'
                '    http://{fqdn}:{httpPort}{engineURI}\n'
                '    https://{fqdn}:{httpsPort}{engineURI}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                engineURI=osetupcons.Const.ENGINE_URI,
            )
        )

        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.dialog.note(
                text=_(
                    'JBoss is listending for debug connection at: {address}'
                ).format(
                    address=self.environment[
                        osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
