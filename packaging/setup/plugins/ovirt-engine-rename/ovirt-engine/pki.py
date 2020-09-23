#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""CA plugin."""


import gettext
import os

from M2Crypto import X509

from otopi import filetransaction
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog

XN_FLAG_SEP_MULTILINE = 4 << 16


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self.uninstall_files = []

    def _subjectComponentEscape(self, s):
        return outil.escape(s, '/\\')

    def _apache_extra_action(self):
        self.environment[
            oengcommcons.ApacheEnv.NEED_RESTART
        ] = True

    _COMMON_ENTITIES = (
        # This is currently unrelated to engine-setup's _COMMON_ENTITIES.
        # TODO: Consider uniting them.
        {
            'name': 'apache',
            'display_name': 'Apache',
            'ca_cert': (
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            ),
            'extract_key': True,
            'extra_action': _apache_extra_action,
        },
    )

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[osetupcons.RenameEnv.PKI_ENTITIES] = []
        self.environment[
            osetupcons.RenameEnv.PKI_ENTITIES
        ].extend(
            self._COMMON_ENTITIES
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
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_QEMU_CERT_TEMPLATE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_CONF,
            )
        )

    def _check_ca_cert(self, entity):
        if (
            entity['ca_cert'] and
            X509.load_cert(
                file=entity['ca_cert'],
                format=X509.FORMAT_PEM,
            ).get_pubkey().get_rsa().pub() != X509.load_cert(
                file=oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                format=X509.FORMAT_PEM,
            ).get_pubkey().get_rsa().pub()
        ):
            self.logger.warning(
                _(
                    'The CA certificate of {display_name} is changed'
                ).format(
                    display_name=entity['display_name'],
                )
            )
            self.dialog.note(
                text=_(
                    '{ca_cert} is different from {engine_ca} .\n'
                    'It was probably replaced with a 3rd party certificate.\n'
                    'You might want to replace it again with a certificate\n'
                    'for the new host name.\n'
                ).format(
                    ca_cert=entity['ca_cert'],
                    engine_ca=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                )
            )
        else:
            entity['enabled'] = True
            self.environment[
                osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
            ].extend(
                (
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                        '{name}.p12'.format(name=entity['name'])
                    ),
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_PKICERTSDIR,
                        '{name}.cer'.format(name=entity['name'])
                    )
                )
            )
            if entity['extract_key']:
                self.environment[
                    osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
                ].append(
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                        '{name}.key.nopass'.format(name=entity['name'])
                    )
                )

    def _handle_rename(self, entity):
        rc, stdout, stderr = self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=%s' % entity['name'],
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
            ].encode('utf8')
        )

        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % entity['name'],
                '--password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--subject=%s' % '/' + '/'.join(
                    outil.escape(s, '/\\')
                    for s in subject.as_text(
                        flags=XN_FLAG_SEP_MULTILINE,
                    ).splitlines()
                ),
                '--san=DNS:%s' % (
                    self._subjectComponentEscape(
                        self.environment[osetupcons.RenameEnv.FQDN],
                    ),
                ),
            ),
        )

        self.uninstall_files.extend(
            (
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                    '{name}.p12'.format(name=entity['name'])
                ),
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKICERTSDIR,
                    '{name}.cer'.format(name=entity['name'])
                )
            )
        )
        if entity['extract_key']:
            self.execute(
                args=(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                    '--name=%s' % entity['name'],
                    '--passin=%s' % (
                        self.environment[oenginecons.PKIEnv.STORE_PASS],
                    ),
                    '--key=%s' % (
                        os.path.join(
                            oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                            '{name}.key.nopass'.format(name=entity['name'])
                        )
                    ),
                ),
            )
            self.uninstall_files.append(
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                    '{name}.key.nopass'.format(name=entity['name'])
                )
            )
        if entity['extra_action']:
            entity['extra_action'](self)

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
        condition=lambda self: os.path.exists(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
        )
    )
    def _late_setup(self):
        for entity in self.environment[
            osetupcons.RenameEnv.PKI_ENTITIES
        ]:
            self._check_ca_cert(entity)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
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
                    'http://www.ovirt.org/documentation/how-to/networking'
                    '/changing-engine-hostname/\n'
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
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_QEMU_CERT_TEMPLATE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_CONF
            ):
                with open(config, 'r') as f:
                    content = []
                    for line in f:
                        line = line.rstrip('\n')
                        if line.startswith('authorityInfoAccess'):
                            line = (
                                'authorityInfoAccess = '
                                'caIssuers;URI:http://%s:%s%s'
                            ) % (
                                self.environment[
                                    osetupcons.RenameEnv.FQDN
                                ],
                                self.environment[
                                    oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
                                ],
                                oenginecons.Const.ENGINE_PKI_CA_URI,
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
    )
    def _misc(self):
        # TODO
        # this implementation is not transactional
        # too many issues with legacy ca implementation
        # need to work this out to allow transactional
        for entity in self.environment[
            osetupcons.RenameEnv.PKI_ENTITIES
        ]:
            if entity.get('enabled'):
                self._handle_rename(entity)


# vim: expandtab tabstop=4 shiftwidth=4
