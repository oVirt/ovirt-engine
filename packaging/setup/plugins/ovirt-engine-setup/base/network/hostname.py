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


"""Hostname plugin."""


import re
import socket
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
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
            [A-Za-z0-9\.\-]+
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
            (?P<interface>\w+(\.\w+)?)(@\w+)?
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
            (?P<interface>\w+(\.\w+(@\w+)?)?)
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
            [\w/.-]+\.in-addr\.arpa\.
            \s+
            \d+
            \s+
            IN
            \s+
            PTR
            \s+
            (?P<answer>[\w/.-]+)
            \.
            $
        """
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _getNonLoopbackAddresses(self):
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

        self.logger.debug('addresses: %s' % iplist)
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

        try:
            resolvedAddresses = set([
                address[0] for __, __, __, __, address in
                socket.getaddrinfo(
                    fqdn,
                    None,
                    # Currently we need an IPv4 address and ignore the rest.
                    socket.AF_INET,
                )
            ])
            self.logger.debug(
                '{fqdn} resolves to: {addresses}'.format(
                    fqdn=fqdn,
                    addresses=resolvedAddresses,
                )
            )
            resolvedAddressesAsString = ' '.join(resolvedAddresses)
        except socket.error:
            raise RuntimeError(
                _('{fqdn} did not resolve into an IP address').format(
                    fqdn=fqdn,
                )
            )

        resolvedByDNS = self._resolvedByDNS(fqdn)
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
            revResolved = False
            for address in resolvedAddresses:
                for name in self._dig_reverse_lookup(address):
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
                        addresses=resolvedAddressesAsString,
                        fqdn=fqdn
                    )
                )

        if self.environment[
            osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
        ]:
            if not resolvedAddresses.issubset(self._getNonLoopbackAddresses()):
                raise RuntimeError(
                    _(
                        'The following addreses: '
                        "{addresses} can't be mapped "
                        'to non loopback devices on this host'
                    ).format(
                        addresses=resolvedAddressesAsString
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

        if not fqdn:
            raise RuntimeError(
                _('Please specify host FQDN')
            )

        if len(fqdn) > 1000:
            raise RuntimeError(
                _('FQDN has invalid length')
            )

        components = fqdn.split('.', 1)
        if len(components) == 1 or not components[0]:
            self.logger.warning(
                _('Host name {fqdn} has no domain suffix').format(
                    fqdn=fqdn,
                )
            )
        else:
            if not self._DOMAIN_RE.match(components[1]):
                raise RuntimeError(
                    _('Host name {fqdn} has invalid domain name').format(
                        fqdn=fqdn,
                    )
                )

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
            osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('ip')
        self.command.detect('dig')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.CONFIG_PROTOCOLS_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
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
                self._validateFQDNresolvability(
                    self.environment[osetupcons.ConfigEnv.FQDN]
                )
                validFQDN = True
            except RuntimeError as e:
                self.logger.error(
                    _('Host name is not valid: {error}').format(
                        error=e,
                    ),
                )
                self.logger.debug('exception', exc_info=True)
                if not interactive:
                    break


# vim: expandtab tabstop=4 shiftwidth=4
