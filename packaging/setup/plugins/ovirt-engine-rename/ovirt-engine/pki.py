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


"""CA plugin."""


import gettext
import os

from M2Crypto import X509
from otopi import constants as otopicons
from otopi import filetransaction, plugin, transaction, util
from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

XN_FLAG_SEP_MULTILINE = 4 << 16


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self.uninstall_files = []

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            oenginecons.PKIEnv.STORE_PASS
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.PKIEnv.STORE_PASS,
            oengcommcons.Defaults.DEFAULT_PKI_STORE_PASS
        )
        self.environment.setdefault(
            osetupcons.RenameEnv.FORCE_IGNORE_AIA_IN_CA,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ].extend(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE[
                    :-len('.in')],
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_CONF,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
        condition=lambda self: os.path.exists(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
        )
    )
    def _late_setup(self):
        if (
            X509.load_cert(
                file=(
                    oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
                ),
                format=X509.FORMAT_PEM,
            ).get_pubkey().get_rsa().pub() != X509.load_cert(
                file=oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                format=X509.FORMAT_PEM,
            ).get_pubkey().get_rsa().pub()
        ):
            self.logger.warning(_('The CA certificate of Apache is changed'))
            self.dialog.note(
                text=_(
                    '{apache_ca} is different from {ca} .\n'
                    'It was probably replaced with a 3rd party certificate.\n'
                    'You might want to replace it again with a certificate\n'
                    'for the new host name.\n'
                ).format(
                    apache_ca=(
                        oengcommcons.FileLocations.
                        OVIRT_ENGINE_PKI_APACHE_CA_CERT
                    ),
                    ca=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                )
            )
        else:
            self._enabled = True

            self.environment[
                osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
            ].extend(
                (
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_APACHE_STORE,
                    oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
                    oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            self._enabled and
            not self.environment[
                osetupcons.RenameEnv.FORCE_IGNORE_AIA_IN_CA
            ]
        )
    )
    def _aia(self):
        x509 = X509.load_cert(
            file=oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            format=X509.FORMAT_PEM,
        )

        try:
            authorityInfoAccess = x509.get_ext(
                'authorityInfoAccess'
            ).get_value()

            self.logger.warning(_('AIA extension found in CA certificate'))
            self.dialog.note(
                text=_(
                    'Please note:\n'
                    'The certificate for the CA contains the\n'
                    '"Authority Information Access" extension pointing\n'
                    'to the old hostname:\n'
                    '{aia}'
                    'Currently this is harmless, but it might affect future\n'
                    'upgrades. In version 3.3 the default was changed to\n'
                    'create new CA certificate without this extension. If\n'
                    'possible, it might be better to not rely on this\n'
                    'program, and instead backup, cleanup and setup again\n'
                    'cleanly.\n'
                    '\n'
                    'More details can be found at the following address:\n'
                    'http://www.ovirt.org/Changing_Engine_Hostname\n'
                ).format(
                    aia=authorityInfoAccess,
                ),
            )
            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_RENAME_AIA_BYPASS',
                note=_('Do you want to continue? (@VALUES@) [@DEFAULT@]: '),
                prompt=True,
            ):
                raise RuntimeError(_('Aborted by user'))
        except LookupError:
            pass

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.RENAME_PKI_CONF_MISC,
    )
    def _misc_conffiles(self):
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki',
            description='PKI keys',
            optional=True,
        ).addFiles(
            group='ca_pki',
            fileList=self.uninstall_files,
        )

        localtransaction = transaction.Transaction()
        with localtransaction:
            for config in (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE[
                    :-len('.in')],
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_CONF
            ):
                with open(config, 'r') as f:
                    content = []
                    for line in f:
                        line = line.rstrip('\n')
                        if line.startswith('authorityInfoAccess'):
                            line = (
                                'authorityInfoAccess = '
                                'caIssuers;URI:http://%s:%s/ca.crt'
                            ) % (
                                self.environment[
                                    osetupcons.RenameEnv.FQDN
                                ],
                                self.environment[
                                    oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
                                ],
                            )
                        content.append(line)
                localtransaction.append(
                    filetransaction.FileTransaction(
                        name=config,
                        content=content,
                        modifiedList=self.uninstall_files,
                    ),
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.RENAME_PKI_CONF_MISC,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        # TODO
        # this implementation is not transactional
        # too many issues with legacy ca implementation
        # need to work this out to allow transactional
        rc, stdout, stderr = self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=%s' % 'apache',
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--cert=-',
            ),
        )
        x509 = X509.load_cert_string(
            string='\n'.join(stdout).encode('ascii'),
            format=X509.FORMAT_PEM,
        )
        subject = x509.get_subject()
        subject.get_entries_by_nid(
            X509.X509_Name.nid['CN']
        )[0].set_data(
            self.environment[
                osetupcons.RenameEnv.FQDN
            ]
        )

        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % 'apache',
                '--password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--subject=%s' % '/' + '/'.join(
                    outil.escape(s, '/\\')
                    for s in subject.as_text(
                        flags=XN_FLAG_SEP_MULTILINE,
                    ).splitlines()
                ),
            ),
        )

        self.uninstall_files.extend(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_APACHE_STORE,
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
            )
        )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=%s' % 'apache',
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=%s' % (
                    oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
                ),
            ),
        )

        self.uninstall_files.append(
            oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
        )

        self.environment[
            oengcommcons.ApacheEnv.NEED_RESTART
        ] = True


# vim: expandtab tabstop=4 shiftwidth=4
