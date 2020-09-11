#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""ovirt-imageio constants."""

from otopi import util

from . import config as oipconfig


@util.export
class ImageIO(object):
    SERVICE_NAME = 'ovirt-imageio'
    DAEMON_PORT = 54322
    PROXY_PORT = 54323
    CONTROL_PORT = 54324
    ENGINE_CONFIG = oipconfig.OVIRT_IMAGEIO_ENGINE_CONFIG
    CONFIG_STAGE = "setup.config.imageio"
    CONFIG_TEMPLATE = """\
    # Configuration overrides for ovirt-engine.
    #
    # WARNING: This file is owned by ovirt-engine. If you modify this file your
    # changes will be overwritten in the next ovirt-engine upgrade.
    #
    # To change the configuration create a new drop-in file with higher prefix,
    # so your setting will override ovirt-engine configuration:
    #
    # $ cat /etc/ovirt-imageio/conf.d/99-local.conf
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
    # http://ovirt.github.io/ovirt-imageio/configuration.html#tls-configuration
    key_file = {key_file}
    cert_file = {cert_file}
    ca_file = {ca_file}

    [backend_http]
    # CA file used by HTTP backend client to verify imageio server certificate.
    # Do not change, oVirt host pki setup is based on this CA.
    ca_file = {engine_ca_file}

    [remote]
    # Port cannot be changed as it's currently hard-coded in engine code.
    port = {remote_port}

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
