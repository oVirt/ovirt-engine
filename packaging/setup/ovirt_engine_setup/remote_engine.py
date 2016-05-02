#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


import gettext
import os
import tempfile
import time

from M2Crypto import EVP, RSA, X509
from otopi import constants as otopicons
from otopi import base, filetransaction, util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class RemoteEngine(base.Base):

    _instance = None

    def __init__(self, plugin):
        super(RemoteEngine, self).__init__()
        self._plugin = plugin
        self._style = None
        self._client = None

    @property
    def plugin(self):
        return self._plugin

    @property
    def dialog(self):
        return self._plugin.dialog

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def logger(self):
        return self._plugin.logger

    def style(self):
        if self._style is None:
            self.configure()
        return self._style

    def execute_on_engine(self, cmd, timeout=60, text=None):
        return self._style.execute_on_engine(
            cmd=cmd,
            timeout=timeout,
            text=text,
        )

    def copy_from_engine(self, file_name):
        return self._style.copy_from_engine(
            file_name=file_name,
        )

    def copy_to_engine(
        self,
        file_name,
        content,
        inp_env_key=None,
        uid=None,
        gid=None,
        mode=None,
    ):
        return self._style.copy_to_engine(
            file_name=file_name,
            content=content,
            inp_env_key=inp_env_key,
            uid=uid,
            gid=gid,
            mode=mode,
        )

    def cleanup(self):
        if self._style:
            return self._style.cleanup()

    def configure(self, fqdn):
        key = osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLE
        styles = dict(
            (
                str(i + 1),
                s
            )
            for i, s in enumerate(
                self.environment[
                    osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLES
                ]
            )
        )
        if self.environment[key] is None:
            choices = sorted(styles.keys())
            descs = ''.join(
                '{c} - {desc}\n'.format(
                    c=c,
                    desc=styles[c].desc(),
                )
                for c in choices
            )
            reply = self.dialog.queryString(
                name='REMOTE_ENGINE_SETUP_STYLE',
                note=_(
                    'Setup will need to do some actions on the remote engine '
                    'server. Either automatically, using ssh as root to '
                    'access it, or you will be prompted to manually '
                    'perform each such action.\n'
                    'Please choose one of the following:\n'
                    '{descs}'
                    '(@VALUES@) [@DEFAULT@]: '
                ).format(
                    descs=descs,
                ),
                prompt=True,
                validValues=choices,
                default=choices[0],
            )
            self.environment[key] = styles[reply].name
        if self._style is None:
            self._style = next(
                s for i, s in styles.items()
                if s.name == self.environment[key]
            )
            self._style.configure(fqdn=fqdn)


@util.export
class EnrollCert(base.Base):

    def __init__(
        self,
        remote_engine,
        engine_fqdn,
        base_name,
        base_touser,
        key_file,
        cert_file,
        csr_fname_envkey,
        engine_ca_cert_file,
        engine_pki_requests_dir,
        engine_pki_certs_dir,
        key_size,
        url,
    ):
        super(EnrollCert, self).__init__()
        self._need_key = False
        self._need_cert = False
        self._key = None
        self._pubkey = None
        self._csr = None
        self._cert = None
        self._csr_file = None
        self._engine_csr_file = None
        self._engine_cert_file = None
        self._remote_name = None
        self._enroll_command = None

        self._remote_engine = remote_engine
        self._engine_fqdn = engine_fqdn
        self._base_name = base_name
        self._base_touser = base_touser
        self._key_file = key_file
        self._cert_file = cert_file
        self._csr_fname_envkey = csr_fname_envkey
        self._engine_ca_cert_file = engine_ca_cert_file
        self._engine_pki_requests_dir = engine_pki_requests_dir
        self._engine_pki_certs_dir = engine_pki_certs_dir
        self._key_size = key_size
        self._url = url

        self._plugin = remote_engine.plugin

    @property
    def plugin(self):
        return self._plugin

    @property
    def dialog(self):
        return self._plugin.dialog

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def logger(self):
        return self._plugin.logger

    def _genCsr(self):
        rsa = RSA.gen_key(self._key_size, 65537)
        rsapem = rsa.as_pem(cipher=None)
        evp = EVP.PKey()
        evp.assign_rsa(rsa)
        rsa = None  # should not be freed here
        csr = X509.Request()
        csr.set_pubkey(evp)
        csr.sign(evp, 'sha1')
        return rsapem, csr.as_pem(), csr.get_pubkey().as_pem(cipher=None)

    def _enroll_cert_auto_ssh(self):
        cert = None
        self.logger.info(
            _(
                "Signing the {base_touser} certificate on the engine server"
            ).format(
                base_touser=self._base_touser,
            )
        )

        tries_left = 30
        goodcert = False
        while not goodcert and tries_left > 0:
            try:
                self._remote_engine.copy_to_engine(
                    file_name='{pkireqdir}/{remote_name}.req'.format(
                        pkireqdir=self._engine_pki_requests_dir,
                        remote_name=self._remote_name,
                    ),
                    content=self._csr,
                )
                self._remote_engine.execute_on_engine(cmd=self._enroll_command)
                cert = self._remote_engine.copy_from_engine(
                    file_name='{pkicertdir}/{remote_name}.cer'.format(
                        pkicertdir=self._engine_pki_certs_dir,
                        remote_name=self._remote_name,
                    ),
                )
                goodcert = self._pubkey == X509.load_cert_string(
                    cert
                ).get_pubkey().as_pem(cipher=None)
                if not goodcert:
                    self.logger.error(
                        _(
                            'Failed to sign {base_touser} certificate on '
                            'engine server'
                        ).format(
                            base_touser=self._base_touser,
                        )
                    )
            except:
                self.logger.error(
                    _(
                        'Error while trying to sign {base_touser} certificate'
                    ).format(
                        base_touser=self._base_touser,
                    )
                )
                self.logger.debug('Error signing cert', exc_info=True)
            tries_left -= 1
            if not goodcert and tries_left > 0:
                self.dialog.note(
                    text=_('Trying again...')
                )
                time.sleep(10)

        self.logger.info(
            _('{base_touser} certificate signed successfully').format(
                base_touser=self._base_touser,
            )
        )
        return cert

    def _enroll_cert_manual_files(self):
        cert = None
        csr_fname = self.environment[self._csr_fname_envkey]
        with (
            open(csr_fname, 'w') if csr_fname
            else tempfile.NamedTemporaryFile(mode='w', delete=False)
        ) as self._csr_file:
            self._csr_file.write(self._csr)
        self.dialog.note(
            text=_(
                "\n\nTo sign the {base_touser} certificate on the engine "
                "server, please:\n\n"
                "1. Copy {tmpcsr} from here to {enginecsr} on the engine "
                "server.\n\n"
                "2. Run on the engine server:\n\n"
                "{enroll_command}\n\n"
                "3. Copy {enginecert} from the engine server to some file "
                "here. Provide the file name below.\n\n"
                "See {url} for more details, including using an external "
                "certificate authority."
            ).format(
                base_touser=self._base_touser,
                tmpcsr=self._csr_file.name,
                enginecsr='{pkireqdir}/{remote_name}.req'.format(
                    pkireqdir=self._engine_pki_requests_dir,
                    remote_name=self._remote_name,
                ),
                enroll_command=self._enroll_command,
                enginecert='{pkicertdir}/{remote_name}.cer'.format(
                    pkicertdir=self._engine_pki_certs_dir,
                    remote_name=self._remote_name,
                ),
                url=self._url,
            ),
        )
        goodcert = False
        while not goodcert:
            filename = self.dialog.queryString(
                name='ENROLL_CERT_MANUAL_FILES_{base_name}'.format(
                    base_name=self._base_name,
                ),
                note=_(
                    '\nPlease input the location of the file where you '
                    'copied the signed certificate in step 3 above: '
                ),
                prompt=True,
            )
            try:
                with open(filename) as f:
                    cert = f.read()
                goodcert = self._pubkey == X509.load_cert_string(
                    cert
                ).get_pubkey().as_pem(cipher=None)
                if not goodcert:
                    self.logger.error(
                        _(
                            'The certificate in {cert} does not match '
                            'the request in {csr}. Please try again.'
                        ).format(
                            cert=filename,
                            csr=self._csr_file.name,
                        )
                    )
            except:
                self.logger.error(
                    _(
                        'Error while reading or parsing {cert}. '
                        'Please try again.'
                    ).format(
                        cert=filename,
                    )
                )
                self.logger.debug('Error reading cert', exc_info=True)
        self.logger.info(
            _('{base_touser} certificate read successfully').format(
                base_touser=self._base_touser,
            )
        )
        return cert

    def _enroll_cert_manual_inline(self):
        pass

    def enroll_cert(self):
        cert = None

        self.logger.debug('enroll_cert')
        self._need_cert = not os.path.exists(self._cert_file)
        self._need_key = not os.path.exists(self._key_file)

        if self._need_key:
            self._key, self._csr, self._pubkey = self._genCsr()
            self._need_cert = True

        if self._need_cert:
            self._remote_name = '{name}-{fqdn}'.format(
                name=self._base_name,
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
            )
            self._enroll_command = (
                " /usr/share/ovirt-engine/bin/pki-enroll-request.sh \\\n"
                "     --name={remote_name} \\\n"
                "     --subject=\""
                "$(openssl x509 -in {engine_ca_cert_file} -noout "
                "-subject | sed 's;subject= \(/C=[^/]*/O=[^/]*\)/.*;\\1;')"
                "/CN={fqdn}\""
            ).format(
                remote_name=self._remote_name,
                engine_ca_cert_file=self._engine_ca_cert_file,
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
            )
            self._remote_engine.configure(fqdn=self._engine_fqdn)
            # TODO
            # This is ugly - we rely on having these two plugins
            # and do not support others. A good fix will:
            # 1. Be completely pluggable
            # 2. Will not duplicate the code in this function
            # 3. Will be nice to the user in every style
            # 4. Have a clearly-defined interface where relevant
            # Perhaps we'll have to give up on some of these, not sure
            # Also, for the meantime, we might/should implement
            # manual_inline and have another function for that,
            # or perhaps make _enroll_cert_manual_files work with both.
            cert = {
                osetupcons.Const.REMOTE_ENGINE_SETUP_STYLE_AUTO_SSH: (
                    self._enroll_cert_auto_ssh
                ),
                osetupcons.Const.REMOTE_ENGINE_SETUP_STYLE_MANUAL_FILES: (
                    self._enroll_cert_manual_files
                ),
            }[self._remote_engine.style().name]()
        self._cert = cert

    def add_to_transaction(
        self,
        uninstall_group_name,
        uninstall_group_desc,
    ):
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group=uninstall_group_name,
            description=uninstall_group_desc,
            optional=True,
        ).addFiles(
            group=uninstall_group_name,
            fileList=uninstall_files,
        )
        if self._need_key:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=self._key_file,
                    mode=0o600,
                    owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                    enforcePermissions=True,
                    content=self._key,
                    modifiedList=uninstall_files,
                )
            )

        if self._need_cert:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=self._cert_file,
                    mode=0o600,
                    owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                    enforcePermissions=True,
                    content=self._cert,
                    modifiedList=uninstall_files,
                )
            )

    def cleanup(self):
        if self._csr_file is not None:
            try:
                os.unlink(self._csr_file.name)
            except OSError:
                self.logger.debug(
                    "Failed to delete '%s'",
                    self._csr_file.name,
                    exc_info=True,
                )


# vim: expandtab tabstop=4 shiftwidth=4
