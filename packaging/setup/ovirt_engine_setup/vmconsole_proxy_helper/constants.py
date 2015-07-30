#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
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


"""Constants."""


import gettext
import os

from otopi import util

from ovirt_engine_setup.constants import osetupattrs, osetupattrsclass

from . import config as vmpconfig


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Const(object):
    # TODO: if we add package management, add package names here

    VMCONSOLE_PROXY_PKI_NAME = 'vmconsole-proxy'

    VMCONSOLE_PROXY_HELPER_PKI_NAME = 'vmconsole-proxy-helper'

    OVIRT_VMCONSOLE_PROXY_EKU = '1.3.6.1.4.1.2312.13.1.2.1.1'

    OVIRT_VMCONSOLE_USER = 'ovirt-vmconsole'

    OVIRT_VMCONSOLE_GROUP = 'ovirt-vmconsole'

    # this is also used for SSH keys enrollment
    OVIRT_VMCONSOLE_PROXY_SERVICE_NAME = 'ovirt-vmconsole-proxy'

    MANUAL_INTERVENTION_TEXT = _(
        'Manual intervention is required, because '
        'setup was run in developer mode. '
        'Please run with root privileges:\n'
    )


@util.export
class FileLocations(object):

    ENGINE_VMCONSOLE_PROXY_DIR = \
        vmpconfig.ENGINE_VMCONSOLE_PROXY_DIR

    OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIG = \
        vmpconfig.ENGINE_VMCONSOLE_PROXY_CONFIG

    OVIRT_SETUP_DATADIR = vmpconfig.SETUP_DATADIR

    OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIGD = (
        '%s.d' % OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIG
    )
    OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIG_SETUP = os.path.join(
        OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIGD,
        '10-setup.conf',
    )

    OVIRT_VMCONSOLE_PROXY_CONFIG = os.path.join(
        OVIRT_SETUP_DATADIR,
        'conf',
        'ovirt-vmconsole-proxy.conf'
    )

    OVIRT_ENGINE_PKIDIR = vmpconfig.ENGINE_PKIDIR

    OVIRT_ENGINE_PKIKEYSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'keys',
    )
    OVIRT_ENGINE_PKICERTSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'certs',
    )
    OVIRT_ENGINE_PKIREQUESTSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'requests',
    )

    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_REQ = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        '%s.req' % Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
    )
    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        '%s.p12' % Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
    )
    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        '%s.key.nopass' % Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
    )
    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        '%s.cer' % Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
    )

    # integration with external package ovirt-vmconsole-proxy
    # we default to absolute path because this is an
    # external package.
    # sync with ovirt-vmconsole-proxy
    OVIRT_VMCONSOLE_PROXY_CONFIG_ENGINE_SETUP_FILE = os.path.join(
        '/',
        'etc',
        'ovirt-vmconsole',
        'ovirt-vmconsole-proxy',
        'conf.d',
        '50-ovirt-vmconsole-proxy.conf',
    )

    OVIRT_VMCONSOLE_PROXY_ENGINE_HELPER = \
        vmpconfig.VMCONSOLE_PROXY_HELPER_PATH

    OVIRT_VMCONSOLE_PROXY_PKIDIR = \
        '/etc/pki/ovirt-vmconsole/'

    OVIRT_VMCONSOLE_PKI_CA_PUB = os.path.join(
        OVIRT_VMCONSOLE_PROXY_PKIDIR,
        'ca.pub'
    )

    OVIRT_VMCONSOLE_PKI_PROXY_HOST_CERT = os.path.join(
        OVIRT_VMCONSOLE_PROXY_PKIDIR,
        'proxy-ssh_host_rsa-cert.pub'
    )

    OVIRT_VMCONSOLE_PKI_PROXY_HOST_KEY = os.path.join(
        OVIRT_VMCONSOLE_PROXY_PKIDIR,
        'proxy-ssh_host_rsa'
    )


@util.export
class Stages(object):

    # sync with engine
    CA_AVAILABLE = 'osetup.pki.ca.available'

    CONFIG_VMCONSOLE_ENGINE_CUSTOMIZATION = \
        'setup.config.vmconsole-engine.customization'

    CONFIG_VMCONSOLE_PKI_ENGINE = \
        'setup.config.vmconsole-engine.pki'

    CONFIG_VMCONSOLE_PROXY_CUSTOMIZATION = \
        'setup.config.vmconsole-proxy.customization'

    CONFIG_VMCONSOLE_PKI_PROXY = \
        'setup.config.vmconsole-proxy.pki'

    # sync with engine
    ENGINE_CORE_ENABLE = 'osetup.engine.core.enable'


@util.export
class Defaults(object):
    DEFAULT_VMCONSOLE_PROXY_PORT = 2222


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

    VMCONSOLE_PROXY_HOST = \
        'OVESETUP_VMCONSOLE_PROXY_CONFIG/vmconsoleProxyHost'

    VMCONSOLE_PROXY_PORT = \
        'OVESETUP_VMCONSOLE_PROXY_CONFIG/vmconsoleProxyPort'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure VMConsole Proxy'),
        postinstallfile=True,
    )
    def VMCONSOLE_PROXY_CONFIG(self):
        return 'OVESETUP_CONFIG/vmconsoleProxyConfig'


@util.export
@util.codegen
@osetupattrsclass
class EngineConfigEnv(object):
    """Sync with ovirt-engine"""

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine Host FQDN'),
        postinstallfile=True,
    )
    def ENGINE_FQDN(self):
        return 'OVESETUP_ENGINE_CONFIG/fqdn'


# vim: expandtab tabstop=4 shiftwidth=4
