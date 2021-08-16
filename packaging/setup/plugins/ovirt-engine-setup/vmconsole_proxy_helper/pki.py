#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""vmconsole proxy plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


def _refresh_needed(cert_path):
    ca_cert_path = oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
    return (not os.path.exists(cert_path) or
            os.stat(ca_cert_path).st_mtime > os.stat(cert_path).st_mtime)


@util.export
class Plugin(plugin.PluginBase):
    """vmconsole proxy configuration plugin."""

    def _subjectComponentEscape(self, s):
        return outil.escape(s, '/\\')

    def _enrollSSHKeys(self, host_mode, uninstall_files):
        suffix = 'host' if host_mode else 'user'
        name = '%s-%s' % (
            ovmpcons.Const.VMCONSOLE_PROXY_PKI_NAME,
            suffix
        )

        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % name,
                '--password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--subject=/C=%s/O=%s/CN=%s' % (
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.COUNTRY],
                    ),
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.ORG],
                    ),
                    self._subjectComponentEscape(
                        name
                    ),
                ),
            ),
        )

        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_SSH_ENROLL,
                '--name=%s' % name,
                '--id=%s' % name,
                '--principals=%s' % (
                    self.environment[osetupcons.ConfigEnv.FQDN]
                    if host_mode
                    else ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_PRINCIPAL
                ),
            ) + (
                ('--host',) if host_mode else ()
            ),
        )

        cert = os.path.join(
            ovmpcons.FileLocations.OVIRT_ENGINE_PKICERTSDIR,
            '%s-cert.pub' % name
        )
        uninstall_files.append(cert)

        with open(cert) as f:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=os.path.join(
                        ovmpcons.FileLocations.VMCONSOLE_PKI_DIR,
                        'proxy-ssh_%s_rsa-cert.pub' % suffix,
                    ),
                    content=f.read(),
                    modifiedList=uninstall_files,
                )
            )

        rc, key, stderr = self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=%s-%s' % (
                    ovmpcons.Const.VMCONSOLE_PROXY_PKI_NAME,
                    suffix,
                ),
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=-',
            ),
        )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(
                    ovmpcons.FileLocations.VMCONSOLE_PKI_DIR,
                    'proxy-ssh_%s_rsa' % suffix,
                ),
                mode=0o600,
                owner=self.environment[ovmpcons.SystemEnv.USER_VMCONSOLE],
                group=self.environment[ovmpcons.SystemEnv.GROUP_VMCONSOLE],
                content=key,
                modifiedList=uninstall_files,
            )
        )

    def _expandSSHCAKey(self, uninstall_files):
        res = self.executePipe((
            {
                'args': (
                    self.command.get('openssl'),
                    'x509',
                    '-in', (
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                    '-noout',
                    '-pubkey',
                ),
            },
            {
                'args': (
                    self.command.get('ssh-keygen'),
                    '-i',
                    '-m', 'PKCS8',
                    '-f', '/proc/self/fd/0',
                ),
            },
        ))

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=os.path.join(
                    ovmpcons.FileLocations.VMCONSOLE_PKI_DIR,
                    'ca.pub',
                ),
                content=res['stdout'],
                modifiedList=uninstall_files,
            )
        )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_PROGRAMS,
    )
    def _setup(self):
        self.command.detect('openssl')
        self.command.detect('ssh-keygen')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ] and _refresh_needed(
                ovmpcons.FileLocations.
                OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_KEY
            )
        ),
    )
    def _miscPKIEngine(self):
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='vmconsole_proxy_helper_pki',
            description='VMConsole Helper PKI keys',
            optional=True,
        ).addFiles(
            group='vmconsole_proxy_helper_pki',
            fileList=uninstall_files,
        )

        self.logger.info(
            _(
                'Setting up ovirt-vmconsole proxy helper PKI artifacts'
            )
        )

        # vmconsole enrollment needs special care due to EKU
        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % ovmpcons.Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
                '--password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--subject=/C=%s/O=%s/CN=%s' % (
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.COUNTRY],
                    ),
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.ORG],
                    ),
                    self._subjectComponentEscape(
                        self.environment[osetupcons.ConfigEnv.FQDN],
                    ),
                ),
                '--ku=digitalSignature',
                '--eku=%s' % ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_EKU,
            ),
        )

        uninstall_files.extend(
            (
                (
                    ovmpcons.FileLocations.
                    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_CERT
                ),
                (
                    ovmpcons.FileLocations.
                    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_STORE
                ),
            )
        )

        rc, key_content, stderr = self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=%s' % ovmpcons.Const.VMCONSOLE_PROXY_HELPER_PKI_NAME,
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=-',
            ),
        )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    ovmpcons.FileLocations.
                    OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_KEY
                ),
                mode=0o600,
                owner=self.environment[ovmpcons.SystemEnv.USER_VMCONSOLE],
                group=self.environment[ovmpcons.SystemEnv.GROUP_VMCONSOLE],
                content=key_content,
                modifiedList=uninstall_files,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ] and _refresh_needed(
                os.path.join(
                    ovmpcons.FileLocations.VMCONSOLE_PKI_DIR,
                    'proxy-ssh_host_rsa',
                )
            )
        ),
    )
    def _miscPKIProxy(self):
        uninstall_files = []
        # files to copy in /etc/pki/ovirt-vmconsole
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='vmconsole_proxy_pki',
            description='VMConsole PKI Proxy keys',
            optional=True,
        ).addFiles(
            group='vmconsole_proxy_pki',
            fileList=uninstall_files,
        )

        self.logger.info(_('Setting up ovirt-vmconsole SSH PKI artifacts'))

        self._expandSSHCAKey(
            uninstall_files=uninstall_files,
        )

        for mode in (False, True):
            self._enrollSSHKeys(
                host_mode=mode,
                uninstall_files=uninstall_files,
            )


# vim: expandtab tabstop=4 shiftwidth=4
