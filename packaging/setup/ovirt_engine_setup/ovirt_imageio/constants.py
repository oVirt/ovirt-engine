#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""ovirt-imageio constants."""


import gettext

from otopi import util

from . import config as oipconfig


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-imageio-setup')


@util.export
class ImageIO(object):
    SERVICE_NAME = 'ovirt-imageio'
    DATA_PORT = 54323
    CONTROL_PORT = 54324
    DAEMON_CONFIG = oipconfig.OVIRT_IMAGEIO_DAEMON_CONFIG
    LOGGER_CONFIG = oipconfig.OVIRT_IMAGEIO_LOGGER_CONFIG
    CONFIG_STAGE = "setup.config.imageio"
    CONFIG_TEMPLATE = """\
    # Configuration for ovirt-engine.
    # This file is installed and owned by ovirt-engine,
    # please do not modify.

    [tls]
    # TLS is always enabled.
    enable = true
    # By default uses httpd certificates.
    # If you want to change the certificates, you need to restart
    # ovirt-imageio service.
    # For more information about imageio certificates, please read
    # http://ovirt.github.io/ovirt-imageio/overview.html#ssl-keys-in-imageio
    key_file = {key_file}
    cert_file = {cert_file}
    ca_file = {ca_file}

    [remote]
    # Hostname has to be hostname of the engine and port cannot be changed
    # as it's currently hardcoded in engine code.
    host = {host}
    port = {data_port}

    [local]
    # Local service is used to access images and runs on hosts, which
    # do all the manipulation with images. On engine is should be
    # disabled.
    enable = false

    [control]
    # Engine currently support only communication over TCP.
    transport = tcp
    port = {control_port}
    """
    LOGGER = """\
    # Logging configuration for development setup.

    [loggers]
    keys=root

    [handlers]
    keys=logfile

    [formatters]
    keys=long

    [logger_root]
    level=DEBUG
    handlers=logfile
    propagate=0

    [handler_logfile]
    class=logging.StreamHandler
    level=DEBUG
    formatter=long

    [formatter_long]
    format=%(asctime)s %(levelname)-7s (%(threadName)s) [%(name)s] %(message)s
    """
