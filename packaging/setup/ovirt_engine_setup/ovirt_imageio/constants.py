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
    ENGINE_CONFIG = oipconfig.OVIRT_IMAGEIO_ENGINE_CONFIG
    CONFIG_STAGE = "setup.config.imageio"
    CONFIG_TEMPLATE = """\
    # Configuration overrides for ovirt-engine.
    #
    # WARNING: This file owned by ovirt-engine. If you modify this file your
    # changes will be overwritten in the next ovirt-engine upgrade.
    #
    # To change the configuration create a new drop-in file with higher prefix, so
    # your setting will override ovirt-engine configuration:
    #
    # $ cat /etc/ovirt-imageio/conf.d/99-locl.conf
    # [tls]
    # ca_file =
    #
    # This example overrides ca_file to be empty string. This can be useful if
    # the host certificates are signed by a trusted CA.


    # Daemon configuration.

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
    # Port cannot be changed as it's currently hardcoded in engine code.
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


    # Logging configuration.

    [handlers]
    keys = {logger_handler}

    [logger_root]
    handlers = {logger_handler}
    level = {logger_level}
    """
