#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


import re
import socket
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import base


from ovirt_engine_setup import constants as osetupcons


@util.export
class Hostname(base.Base):
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

    def __init__(self, plugin):
        super(Hostname, self).__init__()
        self._plugin = plugin
        self.command.detect('dig')

    @property
    def plugin(self):
        return self._plugin

    @property
    def command(self):
        return self._plugin.command

    @property
    def dialog(self):
        return self._plugin.dialog

    @property
    def execute(self):
        return self._plugin.execute

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def logger(self):
        return self._plugin.logger

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

    def getHostname(self, envkey, whichhost, supply_default, prompttext=None):
        interactive = self.environment[envkey] is None
        validFQDN = False
        if prompttext is None:
            prompttext = _(
                'Host fully qualified DNS name of {whichhost} server'
            ).format(
                whichhost=whichhost,
            )
        while not validFQDN:
            if interactive:
                fqdn = socket.getfqdn()
                self.environment[
                    envkey
                ] = self.dialog.queryString(
                    name='OVESETUP_NETWORK_FQDN_{whichhost}'.format(
                        whichhost=whichhost.replace(' ', '_'),
                    ),
                    note=_(
                        '{prompt} [@DEFAULT@]: '
                    ).format(
                        prompt=prompttext,
                    ),
                    prompt=True,
                    default=fqdn if supply_default else '',
                )
            try:
                self._validateFQDN(
                    self.environment[envkey]
                )
                self._validateFQDNresolvability(
                    self.environment[envkey]
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
