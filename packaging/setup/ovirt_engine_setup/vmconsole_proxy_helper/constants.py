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
    VMCONSOLE_PROXY_SERVICE_NAME = 'ovirt-vmconsole-proxy-sshd'

    VMCONSOLE_PROXY_PKI_NAME = 'vmconsole-proxy'

    VMCONSOLE_PROXY_HELPER_PKI_NAME = 'vmconsole-proxy-helper'

    OVIRT_VMCONSOLE_PROXY_EKU = '1.3.6.1.4.1.2312.13.1.2.1.1'

    OVIRT_VMCONSOLE_PROXY_SERVICE_NAME = 'ovirt-vmconsole-proxy-sshd'
    OVIRT_VMCONSOLE_PROXY_PRINCIPAL = 'ovirt-vmconsole-proxy'
    OVIRT_VMCONSOLE_PROXY_PACKAGE = 'ovirt-vmconsole-proxy'


@util.export
class FileLocations(object):

    VMCONSOLE_SYSCONF_DIR = vmpconfig.VMCONSOLE_SYSCONF_DIR
    VMCONSOLE_PKI_DIR = vmpconfig.VMCONSOLE_PKI_DIR

    VMCONSOLE_PROXY_HELPER_VARS = \
        vmpconfig.VMCONSOLE_PROXY_HELPER_VARS

    OVIRT_SETUP_DATADIR = vmpconfig.SETUP_DATADIR

    VMCONSOLE_PROXY_HELPER_VARSD = (
        '%s.d' % VMCONSOLE_PROXY_HELPER_VARS
    )
    VMCONSOLE_PROXY_HELPER_VARS_SETUP = os.path.join(
        VMCONSOLE_PROXY_HELPER_VARSD,
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

    OVIRT_VMCONSOLE_PROXY_ENGINE_HELPER = \
        vmpconfig.VMCONSOLE_PROXY_HELPER_PATH

    VMCONSOLE_CONFIG = os.path.join(
        VMCONSOLE_SYSCONF_DIR,
        'ovirt-vmconsole-proxy',
        'conf.d',
        '20-ovirt-vmconsole-proxy-helper.conf',
    )


@util.export
class Stages(object):

    # sync with engine
    CA_AVAILABLE = 'osetup.pki.ca.available'
    # sync with engine
    ENGINE_CORE_ENABLE = 'osetup.engine.core.enable'


@util.export
class Defaults(object):
    DEFAULT_VMCONSOLE_PROXY_PORT = 2222
    DEFAULT_SYSTEM_USER_VMCONSOLE = 'ovirt-vmconsole'
    DEFAULT_SYSTEM_GROUP_VMCONSOLE = 'ovirt-vmconsole'


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
        return 'OVESETUP_VMCONSOLE_PROXY_CONFIG/vmconsoleProxyConfig'


@util.export
@util.codegen
@osetupattrsclass
class SystemEnv(object):
    USER_VMCONSOLE = 'OVESETUP_SYSTEM/userVmConsole'
    GROUP_VMCONSOLE = 'OVESETUP_SYSTEM/groupVmConsole'


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
