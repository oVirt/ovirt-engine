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

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.x509.extensions import ExtensionNotFound
from cryptography.x509.oid import NameOID

from otopi import filetransaction
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


# Copied from py-cryptography x509/name.py master (3.4).
# In el8 we have 2.3, which does not have it.
#: Short attribute names from RFC 4514:
#: https://tools.ietf.org/html/rfc4514#page-7
_NAMEOID_TO_NAME = {
    NameOID.COMMON_NAME: "CN",
    NameOID.LOCALITY_NAME: "L",
    NameOID.STATE_OR_PROVINCE_NAME: "ST",
    NameOID.ORGANIZATION_NAME: "O",
    NameOID.ORGANIZATIONAL_UNIT_NAME: "OU",
    NameOID.COUNTRY_NAME: "C",
    NameOID.STREET_ADDRESS: "STREET",
    NameOID.DOMAIN_COMPONENT: "DC",
    NameOID.USER_ID: "UID",
}


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

    def _pubkey_from_certfile(self, cert_file_name):
        with open(cert_file_name, 'rb') as f:
            cert_pubkey = x509.load_pem_x509_certificate(
                f.read(),
                backend=default_backend(),
            ).public_key(
            ).public_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PublicFormat.SubjectPublicKeyInfo,
            )
        return cert_pubkey

    def _check_ca_cert(self, entity):
        if (
            entity['ca_cert'] and
            self._pubkey_from_certfile(entity['ca_cert']) !=
            self._pubkey_from_certfile(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
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
        cert = x509.load_pem_x509_certificate(
            '\n'.join(stdout).encode('ascii'),
            backend=default_backend(),
        )
        new_subject = ''
        for rdn in cert.subject.rdns:
            for name_attribute in rdn:
                type_text = _NAMEOID_TO_NAME[name_attribute.oid]
                value_text = name_attribute.value
                if name_attribute.oid == x509.oid.NameOID.COMMON_NAME:
                    value_text = self.environment[
                        osetupcons.RenameEnv.FQDN
                    ]
                new_subject += '/{typ}={val}'.format(
                    typ=type_text,
                    val=outil.escape(value_text, '/\\'),
                )

        self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % entity['name'],
                '--password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--subject=%s' % new_subject,
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
        with open(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            'rb'
        ) as f:
            ca_cert = x509.load_pem_x509_certificate(
                f.read(),
                backend=default_backend(),
            )

        try:
            access_description = ca_cert.extensions.get_extension_for_oid(
                x509.oid.ExtensionOID.AUTHORITY_INFORMATION_ACCESS
            ).value[0]
        except ExtensionNotFound:
            # AIA was not included in the CA Cert, no need to warn/prompt
            return

        if (
            (
                access_description.access_method ==
                x509.oid.AuthorityInformationAccessOID.CA_ISSUERS
            ) and (
                type(access_description.access_location) ==
                x509.UniformResourceIdentifier
            )
        ):
            # This is the common/expected case
            # py cryptography, at least 2.3 in el8, does not expose the access
            # method name, and even internally (in
            # access_description.access_method._name), it's "caIssuers", a bit
            # different from m2crypto (which seems to use openssl's texts).
            # So if we are going to format this ourselves anyway, might as well
            # keep it identical with m2crypto/openssl, for this case
            aiatext = 'CA Issuers - URI:{uri}'.format(
                uri=access_description.access_location.value,
            )
        else:
            # If we didn't do the 'if' part above, but only this str() part,
            # the common case above would have looked like this:
            # "<AccessDescription(access_method=<ObjectIdentifier(
            #  oid=1.3.6.1.5.5.7.48.2, name=caIssuers)>, access_location=
            #  <UniformResourceIdentifier(value='http://{fqdn}:80/ovirt-engine/
            #  services/pki-resource?resource=ca-certificate&format=X509-PEM-CA'
            #  )>)>"
            aiatext = str(access_description)

        self.logger.warning(_('AIA extension found in CA certificate'))
        self.dialog.note(
            text=_(
                '\nPlease note:\n'
                'The certificate for the CA contains the '
                '"Authority Information Access" extension pointing '
                'to the old hostname:\n\n'
                '{aia}\n\n'
                'Currently this is harmless, but it might affect future '
                'upgrades. In version 3.3 the default was changed to '
                'create new CA certificate without this extension. If '
                'possible, it might be better to not rely on this '
                'program, and instead backup, cleanup and setup again '
                'cleanly.\n'
                '\n'
                'More details can be found at the following address:\n\n'
                'http://www.ovirt.org/documentation/how-to/networking'
                '/changing-engine-hostname/\n\n'
            ).format(
                aia=aiatext,
            ),
        )
        if not dialog.queryBoolean(
            dialog=self.dialog,
            name='OVESETUP_RENAME_AIA_BYPASS',
            note=_('Do you want to continue? (@VALUES@) [@DEFAULT@]: '),
            prompt=True,
        ):
            raise RuntimeError(_('Aborted by user'))

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
