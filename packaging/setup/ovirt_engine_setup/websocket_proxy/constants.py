#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Constants."""


import gettext
import os

from otopi import util

from ovirt_engine_setup.constants import osetupattrs
from ovirt_engine_setup.constants import osetupattrsclass

from . import config as wspconfig


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Const(object):
    WEBSOCKET_PROXY_SERVICE_NAME = 'ovirt-websocket-proxy'
    WEBSOCKET_PROXY_PACKAGE_NAME = 'ovirt-engine-websocket-proxy'
    WEBSOCKET_PROXY_SETUP_PACKAGE_NAME = \
        'ovirt-engine-setup-plugin-websocket-proxy'
    WEBSOCKET_PROXY_CERT_NAME = 'websocket-proxy'


@util.export
class FileLocations(object):

    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG = \
        wspconfig.ENGINE_WEBSOCKET_PROXY_CONFIG

    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIGD = (
        '%s.d' % OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG
    )
    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG_SETUP = os.path.join(
        OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIGD,
        '10-setup.conf',
    )

    OVIRT_ENGINE_PKIDIR = wspconfig.ENGINE_PKIDIR

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

    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        '%s.key.nopass' % Const.WEBSOCKET_PROXY_CERT_NAME,
    )
    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        '%s.cer' % Const.WEBSOCKET_PROXY_CERT_NAME,
    )

    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_REQ = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        '%s.req' % Const.WEBSOCKET_PROXY_CERT_NAME,
    )
    OVIRT_ENGINE_PKI_ENGINE_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'engine.cer',
    )


@util.export
class Stages(object):

    CONFIG_WEBSOCKET_PROXY_CUSTOMIZATION = \
        'setup.config.websocket-proxy.customization'

    REMOTE_VDC = 'setup.config.websocket-proxy.remote_vdc'


@util.export
class Defaults(object):
    # pki-enroll-pkcs12.sh has it hard-coded to 2048 No need to add "sync with"
    DEFAULT_KEY_SIZE = 2048


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

    WEBSOCKET_PROXY_HOST = 'OVESETUP_CONFIG/websocketProxyHost'

    WEBSOCKET_PROXY_PORT = 'OVESETUP_CONFIG/websocketProxyPort'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure WebSocket Proxy'),
        postinstallfile=True,
    )
    def WEBSOCKET_PROXY_CONFIG(self):
        return 'OVESETUP_CONFIG/websocketProxyConfig'

    WSP_CERTIFICATE_CHAIN = 'OVESETUP_CONFIG/wspCertificateChain'

    PKI_WSP_CSR_FILENAME = 'OVESETUP_CONFIG/pkiWSPCSRFilename'

    WEBSOCKET_PROXY_STOP_NEEDED = 'OVESETUP_CONFIG/websocketProxyStopNeeded'


@util.export
@util.codegen
@osetupattrsclass
class RemoveEnv(object):
    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_WSP(self):
        return 'OVESETUP_REMOVE/removeWsp'


@util.export
@util.codegen
@osetupattrsclass
class RPMDistroEnv(object):
    PACKAGES = 'OVESETUP_WSP_RPMDISTRO_PACKAGES'
    PACKAGES_SETUP = 'OVESETUP_WSP_RPMDISTRO_PACKAGES_SETUP'


@util.export
@util.codegen
class Displays(object):
    CERTIFICATE_REQUEST = 'WSP_CERTIFICATE_REQUEST'

# vim: expandtab tabstop=4 shiftwidth=4
