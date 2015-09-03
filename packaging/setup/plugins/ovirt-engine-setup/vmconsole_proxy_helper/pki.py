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


"""vmconsole proxy plugin."""


import collections
import gettext
import os

from otopi import filetransaction, plugin, util
from otopi import constants as otopicons
from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


# owner OR group = None: no need to change it.
Artifact = collections.namedtuple(
    'Artifact',
    (
        'path', 'mode', 'owner', 'group', 'destination'
    ),
)


def get_install_command(artifact, destination_path=None):
    if destination_path is None:
        if artifact.owner is not None:
            chown_cmd = 'chown {owner}:{group} {path}\n'.format(
                owner=artifact.owner,
                group=artifact.group,
                path=artifact.path,
            )
        else:
            chown_cmd = ''

        chmod_cmd = 'chmod {mode:o} {path}\n'.format(
            mode=artifact.mode,
            path=artifact.path,
        )

        return chown_cmd + chmod_cmd
    else:
        destination = (
            destination_path if artifact.destination is None
            else os.path.join(destination_path, artifact.destination)
        )
        return (
            'install{owner}{group} -m {mode:o} {path} {destination}\n'.format(
                owner=(
                    ' -o %s' % artifact.owner
                    if artifact.owner is not None
                    else ''
                ),
                group=(
                    ' -g %s' % artifact.group
                    if artifact.group is not None
                    else ''
                ),
                mode=artifact.mode,
                path=artifact.path,
                destination=destination,
            )
        )


def CopyFileTransaction(
        src_name,
        dst_name,
        binary=False,
        mode=0o644,
        dmode=0o755,
        owner=None,
        group=None,
        downer=None,
        dgroup=None,
        enforcePermissions=False,
        visibleButUnsafe=False,
        modifiedList=None):
    with open(src_name) as src_file:
        return filetransaction.FileTransaction(
            name=dst_name,
            content=src_file.read(),
            binary=binary,
            mode=mode,
            dmode=dmode,
            owner=owner,
            group=group,
            downer=downer,
            dgroup=dgroup,
            enforcePermissions=enforcePermissions,
            visibleButUnsafe=visibleButUnsafe,
            modifiedList=modifiedList,
        )


def CopyFileInDirTransaction(
        src_name,
        dst_dir,
        dst_name=None,
        binary=False,
        mode=0o644,
        dmode=0o755,
        owner=None,
        group=None,
        downer=None,
        dgroup=None,
        enforcePermissions=False,
        visibleButUnsafe=False,
        modifiedList=None):
    dst_path = os.path.join(
        dst_dir,
        os.path.basename(src_name) if dst_name is None else dst_name,
    )
    return CopyFileTransaction(
        src_name=src_name,
        dst_name=dst_path,
        binary=binary,
        mode=mode,
        dmode=dmode,
        owner=owner,
        group=group,
        downer=downer,
        dgroup=dgroup,
        enforcePermissions=enforcePermissions,
        visibleButUnsafe=visibleButUnsafe,
        modifiedList=modifiedList,
    )


@util.export
class Plugin(plugin.PluginBase):
    """vmconsole proxy configuration plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    def _subjectComponentEscape(self, s):
        return outil.escape(s, '/\\')

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            oenginecons.PKIEnv.STORE_PASS
        )

    def _requireManualIntervention(
        self,
        artifact,
        destination_path=ovmpcons.FileLocations.OVIRT_VMCONSOLE_PROXY_PKIDIR
    ):
        self.dialog.note(
            '{text}'
            '\n'
            '{install_cmd}'
            '\n'.format(
                text=ovmpcons.Const.MANUAL_INTERVENTION_TEXT,
                install_cmd=get_install_command(
                    artifact=artifact,
                    destination_path=destination_path
                ),
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=ovmpcons.Stages.CONFIG_VMCONSOLE_PKI_ENGINE,
        after=(
            ovmpcons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]
        ),
    )
    def _miscPKIEngine(self):
        self._enabled = True
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki_vmp_engine',
            description='VMP PKI Engine keys',
            optional=True,
        ).addFiles(
            group='ca_pki_vmp_engine',
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

        key_file = Artifact(
            path=(
                ovmpcons.FileLocations.
                OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_KEY
            ),
            mode=0o600,
            owner=ovmpcons.Const.OVIRT_VMCONSOLE_USER,
            group=ovmpcons.Const.OVIRT_VMCONSOLE_GROUP,
            destination=None,
        )

        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=key_file.path,
                    mode=key_file.mode,
                    content=key_content,
                    owner=key_file.owner,
                    group=key_file.group,
                    modifiedList=uninstall_files,
                )
            )
        else:
            # store incomplete file, let admin fix later
            # on developer mode we cannot change owner/group!
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=key_file.path,
                    content=key_content,
                    modifiedList=uninstall_files,
                )
            )

            self._requireManualIntervention(key_file, destination_path=None)

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
                    self._subjectComponentEscape(
                        self.environment[osetupcons.ConfigEnv.FQDN]
                        if host_mode else
                        ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_SERVICE_NAME,
                    ),
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

        # prepare final path in the engine pki directory.
        # copy in the vmconsole pki directory later
        # this does the copy because execute() above directly
        # writes the file, we don't use a FileTransaction
        # unlike the other PKI artifacts.
        cert_file = Artifact(
            path=cert,
            mode=0o644,
            owner=ovmpcons.Const.OVIRT_VMCONSOLE_USER,
            group=ovmpcons.Const.OVIRT_VMCONSOLE_GROUP,
            destination='proxy-ssh_%s_rsa-cert.pub' % suffix,
        )
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                CopyFileInDirTransaction(
                    src_name=cert_file.path,
                    dst_dir=(
                        ovmpcons.FileLocations.
                        OVIRT_VMCONSOLE_PROXY_PKIDIR
                    ),
                    dst_name=cert_file.destination,
                    mode=cert_file.mode,
                    owner=cert_file.owner,
                    group=cert_file.group,
                )
            )
        else:
            self._requireManualIntervention(cert_file)

    def _getEtcPkiDir(self):
        return (
            ovmpcons.FileLocations.OVIRT_VMCONSOLE_PROXY_PKIDIR
            if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
            else oenginecons.FileLocations.OVIRT_ENGINE_PKIDIR
        )

    def _expandPKCS12SSHKey(self, host_mode, uninstall_files):
        suffix = 'host' if host_mode else 'user'

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

        key_file = Artifact(
            path=os.path.join(
                self._getEtcPkiDir(),
                'proxy-ssh_%s_rsa' % suffix,
            ),
            mode=0o600,
            owner=ovmpcons.Const.OVIRT_VMCONSOLE_USER,
            group=ovmpcons.Const.OVIRT_VMCONSOLE_GROUP,
            destination=None,
        )

        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=key_file.path,
                    content=key,
                    mode=key_file.mode,
                    owner=key_file.owner,
                    group=key_file.group,
                    modifiedList=uninstall_files,
                )
            )
        else:
            # store incomplete file, let admin fix later
            # on developer mode we cannot change owner/group!
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=key_file.path,
                    content=key,
                    mode=key_file.mode,
                    modifiedList=uninstall_files,
                )
            )

            self._requireManualIntervention(key_file)

    def _expandSSHCAKey(self, uninstall_files):
        ca_file = Artifact(
            path=os.path.join(
                self._getEtcPkiDir(),
                'ca.pub',
            ),
            mode=0o644,
            owner=None,
            group=None,
            destination=None,
        )

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
                name=ca_file.path,
                content=res['stdout'],
                mode=ca_file.mode,
                modifiedList=uninstall_files,
            )
        )

        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self._requireManualIntervention(ca_file)

    @plugin.event(
        stage=plugin.Stages.STAGE_PROGRAMS,
    )
    def _setup(self):
        self.command.detect('openssl')
        self.command.detect('ssh-keygen')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=ovmpcons.Stages.CONFIG_VMCONSOLE_PKI_PROXY,
        after=(
            ovmpcons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ] and os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
        ),
    )
    def _miscPKIProxy(self):
        self._enabled = True
        uninstall_files = []
        # files to copy in /etc/pki/ovirt-vmconsole
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki_vmp_proxy',
            description='VMP PKI Proxy keys',
            optional=True,
        ).addFiles(
            group='ca_pki_vmp_proxy',
            fileList=uninstall_files,
        )

        self.logger.info(_('Setting up ovirt-vmconsole SSH PKI artifacts'))

        self._expandSSHCAKey(
            uninstall_files=uninstall_files,
        )

        self._enrollSSHKeys(
            host_mode=True,
            uninstall_files=uninstall_files,
        )
        self._expandPKCS12SSHKey(
            host_mode=True,
            uninstall_files=uninstall_files,
        )

        # user PKI artifacts
        self._enrollSSHKeys(
            host_mode=False,
            uninstall_files=uninstall_files,
        )
        self._expandPKCS12SSHKey(
            host_mode=False,
            uninstall_files=uninstall_files,
        )


# vim: expandtab tabstop=4 shiftwidth=4
