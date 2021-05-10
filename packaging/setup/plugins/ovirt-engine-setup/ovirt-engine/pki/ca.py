#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""CA plugin."""


import binascii
import gettext
import os
import random
import re

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import pki_utils

from ovirt_setup_lib import dialog

# Shorter names...
_fl = oenginecons.FileLocations
_CA_TEMPLATE_IN = _fl.OVIRT_ENGINE_PKI_CA_TEMPLATE_IN
_CERT_TEMPLATE_IN = _fl.OVIRT_ENGINE_PKI_CERT_TEMPLATE_IN
_CA_TEMPLATE = _fl.OVIRT_ENGINE_PKI_CA_TEMPLATE
_CERT_TEMPLATE = _fl.OVIRT_ENGINE_PKI_CERT_TEMPLATE
_QEMU_CA_TEMPLATE = _fl.OVIRT_ENGINE_PKI_QEMU_CA_TEMPLATE
_QEMU_CERT_TEMPLATE = _fl.OVIRT_ENGINE_PKI_QEMU_CERT_TEMPLATE
_CA_CERT_CONF = _fl.OVIRT_ENGINE_PKI_CA_CERT_CONF
_CERT_CONF = _fl.OVIRT_ENGINE_PKI_CERT_CONF
_QEMU_CA_CERT_CONF = _fl.OVIRT_ENGINE_PKI_QEMU_CA_CERT_CONF
_QEMU_CERT_CONF = _fl.OVIRT_ENGINE_PKI_QEMU_CERT_CONF

# Each of these is a dictionary, where the key is the template
# and the value is a list of files generated from it
_ENGINE_TEMPLATES_MAP = {
    _CA_TEMPLATE_IN: (
        _CA_TEMPLATE,
        _CA_CERT_CONF,
    ),
    _CERT_TEMPLATE_IN: (
        _CERT_TEMPLATE,
        _CERT_CONF,
    ),
}
_QEMU_TEMPLATES_MAP = {
    _CA_TEMPLATE_IN: (
        _QEMU_CA_TEMPLATE,
        _QEMU_CA_CERT_CONF,
    ),
    _CERT_TEMPLATE_IN: (
        _QEMU_CERT_TEMPLATE,
        _QEMU_CERT_CONF,
    ),
}


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    _CA_FILES = (
        oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
        oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_CERT,
    )

    def _ca_file_name(self, ca_file):
        basename = os.path.basename(ca_file)
        name = os.path.splitext(basename)[0]
        return name

    class CATransaction(transaction.TransactionElement):
        """CA transaction element."""

        def __init__(self, parent, uninstall_files):
            self._parent = parent
            self._uninstall_files = uninstall_files

        def __str__(self):
            return _("CA Transaction")

        def prepare(self):
            pass

        def abort(self):
            for f in self._uninstall_files:
                if os.path.exists(f):
                    os.unlink(f)

        def commit(self):
            pass

    def _subjectComponentEscape(self, s):
        return outil.escape(s, '/\\')

    def _setupUninstall(self, files):
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki',
            description='PKI keys',
            optional=True,
        ).addFiles(
            group='ca_pki',
            fileList=files,
        )

    def _extractPKCS12CertificateString(self, pkcs12):
        # input: pkcs12: A PKCS#12 file name
        # return: a string, pem-formatted x509 certificate
        res = False
        rc, stdout, stderr = self.execute(
            args=(
                self.command.get('openssl'),
                'pkcs12',
                '-in', pkcs12,
                '-passin', 'pass:%s' % self.environment[
                    oenginecons.PKIEnv.STORE_PASS
                ],
                '-nokeys',
            ),
            raiseOnError=False,
        )
        if rc == 0 and stdout:
            res = '\n'.join(stdout)
        else:
            self.logger.warn(
                _(
                    "Failed to read or parse '{pkcs12}'"
                ).format(
                    pkcs12=pkcs12,
                )
            )
            self.dialog.note(
                text='\n'.join(
                    [
                        _(
                            "Perhaps it was changed since last Setup."
                        ),
                        _(
                            "Error was:"
                        ),
                    ] + stderr
                )+'\n\n')
        return res

    def _extractPKCS12Certificate(self, pkcs12):
        # input: pkcs12: A PKCS#12 file name
        # return: cryptography.x509.Certificate object
        res = False
        cert = self._extractPKCS12CertificateString(pkcs12)
        if cert:
            res = x509.load_pem_x509_certificate(
                data=cert.encode(),
                backend=default_backend(),
            )
        return res

    def _expandPKCS12(self, pkcs12, name, owner, uninstall_files):
        rc, key, stderr = self.execute(
            args=(
                self.command.get('openssl'),
                'pkcs12',
                '-in', pkcs12,
                '-passin', 'pass:%s' % self.environment[
                    oenginecons.PKIEnv.STORE_PASS
                ],
                '-nodes',
                '-nocerts',
            ),
            logStreams=False,
        )

        localtransaction = transaction.Transaction()
        with localtransaction:
            localtransaction.append(
                filetransaction.FileTransaction(
                    name=os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_PKICERTSDIR,
                        '%s.cer' % name,
                    ),
                    content=self._extractPKCS12CertificateString(pkcs12),
                    mode=0o644,
                    modifiedList=uninstall_files,
                )
            )
            localtransaction.append(
                filetransaction.FileTransaction(
                    name=os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                        '%s.key.nopass' % name,
                    ),
                    content=key,
                    mode=0o600,
                    owner=owner,
                    modifiedList=uninstall_files,
                )
            )

    def _enrollCertificate(self, name, uninstall_files, keepKey=False):
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
                        self.environment[osetupcons.ConfigEnv.FQDN],
                    ),
                ),
                '--san=DNS:%s' % (
                    self._subjectComponentEscape(
                        self.environment[osetupcons.ConfigEnv.FQDN],
                    ),
                ),
            ) + (('--keep-key',) if keepKey else ())
        )
        uninstall_files.extend(
            (
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                    '{}.p12'.format(name),
                ),
            )
        )

    _COMMON_ENTITIES = (
        # TODO split these up to their own plugins and add there to ENTITIES
        {
            'name': 'engine',
            'extract': False,
            'user': osetupcons.SystemEnv.USER_ENGINE,
            'keepKey': True,
        },
        {
            'name': 'jboss',
            'extract': False,
            'user': osetupcons.SystemEnv.USER_ENGINE,
            'keepKey': False,
        },
        {
            'name': 'websocket-proxy',
            'extract': True,
            'user': osetupcons.SystemEnv.USER_ENGINE,
            'keepKey': False,
        },
        {
            'name': 'apache',
            'extract': True,
            'user': oengcommcons.SystemEnv.USER_ROOT,
            'keepKey': False,
        },
        {
            'name': 'reports',
            'extract': True,
            'user': oengcommcons.SystemEnv.USER_ROOT,
            'keepKey': False,
        },
    )

    def _ok_to_renew_cert(self, pkcs12, name, extract):
        # input:
        # - pkcs12: A PKCS#12 file name
        # - name: A base name (--name param of pki-* scripts)
        # - extract: bool. If True, we need to check the extracted cert
        # return: bool
        self.logger.debug("processing: '%s'", name)
        return os.path.exists(pkcs12) and pki_utils.ok_to_renew_cert(
            self.logger,
            self._extractPKCS12Certificate(pkcs12),
            pki_utils.x509_load_cert(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            ),
            name,
            extract,
        )

    def _enrollCertificates(self, renew, uninstall_files):
        for entry in self.environment[oenginecons.PKIEnv.ENTITIES]:
            self.logger.debug(
                "processing: '%s'[renew=%s]",
                entry['name'],
                renew,
            )

            pkcs12 = os.path.join(
                oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                '%s.p12' % entry['name'],
            )

            enroll = False

            if not os.path.exists(pkcs12):
                enroll = True
                self.logger.debug(
                    "'%s' does not exist, enrolling",
                    pkcs12,
                )
            elif renew:
                enroll = self._ok_to_renew_cert(
                    pkcs12,
                    entry['name'],
                    entry['extract']
                )

                if enroll:
                    self.logger.info(
                        _('Renewing {name} certificate').format(
                            name=entry['name'],
                        )
                    )

            if enroll:
                self._enrollCertificate(
                    entry['name'],
                    uninstall_files,
                    keepKey=entry['keepKey'] and renew,
                )
                os.chown(
                    pkcs12,
                    osetuputil.getUid(self.environment[entry['user']]),
                    -1,
                )
                if entry['extract']:
                    self._expandPKCS12(
                        pkcs12,
                        entry['name'],
                        self.environment[entry['user']],
                        uninstall_files,
                    )

    # Loop over the data in one of *_TEMPLATES_MAP - read the template,
    # replace aia, write to outputs
    def _update_templates(self, aia, templates_map, uninstall_files):
        localtransaction = transaction.Transaction()
        with localtransaction:
            for in_template, outputs in templates_map.items():
                if aia is not None:
                    for output_file in outputs:
                        localtransaction.append(
                            filetransaction.FileTransaction(
                                name=output_file,
                                content=outil.processTemplate(
                                    in_template,
                                    {
                                        '@AIA@': aia,
                                    }
                                ),
                                modifiedList=uninstall_files,
                            ),
                        )

    def _calculated_aia(self, ca_uri):
        return 'http://{fqdn}:{port}{ca_uri}'.format(
            fqdn=self.environment[
                osetupcons.ConfigEnv.FQDN
            ],
            port=self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
            ],
            ca_uri=ca_uri,
        )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._renewed_ca_files = set()

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.PKIEnv.STORE_PASS,
            oengcommcons.Defaults.DEFAULT_PKI_STORE_PASS
        )
        self.environment.setdefault(
            oenginecons.PKIEnv.COUNTRY,
            oengcommcons.Defaults.DEFAULT_PKI_COUNTRY
        )
        self.environment.setdefault(
            oenginecons.PKIEnv.ORG,
            None
        )
        self.environment.setdefault(
            oenginecons.PKIEnv.RENEW,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.PKI_RENEWAL_DOC_URL,
            oenginecons.Defaults.DEFAULT_PKI_RENEWAL_DOC_URL
        )
        self.environment[oenginecons.PKIEnv.ENTITIES] = []
        self.environment[
            oenginecons.PKIEnv.ENTITIES
        ].extend(
            self._COMMON_ENTITIES
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('openssl')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        ),
        after=(
            osetupcons.Stages.CONFIG_PROTOCOLS_CUSTOMIZATION,
            oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            ) and
            self.environment[oenginecons.PKIEnv.ORG] is None
        ),
    )
    def _customization(self):
        org = 'Test'
        if '.' in self.environment[osetupcons.ConfigEnv.FQDN]:
            org = self.environment[
                osetupcons.ConfigEnv.FQDN
            ].split('.', 1)[1]

        self.environment[
            oenginecons.PKIEnv.ORG
        ] = self.dialog.queryString(
            name='OVESETUP_PKI_ORG',
            note=_(
                'Organization name for certificate [@DEFAULT@]: '
            ),
            prompt=True,
            default=org,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        ),
        after=(
            osetupcons.Stages.CONFIG_PROTOCOLS_CUSTOMIZATION,
            oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
        ),
    )
    def _customization_upgrade(self):
        if True in [
            pki_utils.cert_expires(pki_utils.x509_load_cert(cert))
            for cert in self._CA_FILES
            if os.path.exists(cert)
        ] + [
            self._ok_to_renew_cert(
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
                    '%s.p12' % entry['name']
                ),
                entry['name'],
                entry['extract']
            )
            for entry in self.environment[oenginecons.PKIEnv.ENTITIES]
        ]:
            if self.environment[oenginecons.PKIEnv.RENEW] is None:
                self.environment[
                    oenginecons.PKIEnv.RENEW
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_RENEW_PKI',
                    note=_(
                        'One or more of the certificates should be renewed, '
                        'because they expire soon, or include an invalid '
                        'expiry date, or they were created with validity '
                        'period longer than 398 days, or do not include the '
                        'subjectAltName extension, which can cause them to be '
                        'rejected by recent browsers and up to date hosts.\n'
                        'See {url} for more details.\n'
                        'Renew certificates? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        url=self.environment[
                            oenginecons.ConfigEnv.PKI_RENEWAL_DOC_URL
                        ],
                    ),
                    prompt=True,
                    default=None,
                )

                if not self.environment[oenginecons.PKIEnv.RENEW]:
                    skip_renewal = dialog.queryBoolean(
                        dialog=self.dialog,
                        name='OVESETUP_SKIP_RENEW_PKI_CONFIRM',
                        note=_(
                            'Are you really sure that you want to skip the '
                            'PKI renewal process?\n'
                            'Please notice that recent openssl and gnutls '
                            'upgrades can lead hosts refusing this CA cert '
                            'making them unusable.\n'
                            'If you choose "Yes", setup will continue and you '
                            'will be asked again the next '
                            'time you run this Setup. Otherwise, this process '
                            'will abort and you will be expected to plan a '
                            'proper upgrade according to {url}.\n'
                            'Skip PKI renewal process? '
                            '(@VALUES@) [@DEFAULT@]: '
                        ).format(
                            url=self.environment[
                                oenginecons.ConfigEnv.PKI_RENEWAL_DOC_URL
                            ],
                        ),
                        prompt=True,
                        default=False,
                    )
                    if not skip_renewal:
                        raise RuntimeError('Aborted by user')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
        ),
    )
    def _check_existing_pki(self):
        pki_files_to_check = (
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE,
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE,
        )
        if True in [os.path.exists(f) for f in pki_files_to_check]:
            ans = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_PKI_VERIFY_MISSING_CA_PEM',
                note=_(
                    'Found existing PKI files, but {capem} is missing. If '
                    'you continue, Setup will overwrite existing PKI files '
                    'with new ones, including {capem}. After Setup completes '
                    'you must reinstall or re-enroll certificates for all '
                    'your hosts.\n\n'
                    'If {capem} was accidentally deleted, stop Setup, restore '
                    '{capem} from backup ({certs}/ca.der), and then run '
                    'Setup again.\n\n'
                    'Continue with Setup and overwrite existing PKI files? '
                    '(@VALUES@) [@DEFAULT@]: '
                ).format(
                    capem=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                    certs=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKICERTSDIR
                    ),
                ),
                prompt=True,
                default=False,
            )
            if not ans:
                raise RuntimeError('Aborted by user')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oenginecons.Stages.CA_AVAILABLE,
            oenginecons.Stages.QEMU_CA_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _miscUpgrade(self):
        #
        # In <3.6 setup did not store the organization and
        # country in post install file. Load it from CA certificate.
        #
        if self.environment[oenginecons.PKIEnv.ORG] is None:
            ca = pki_utils.x509_load_cert(
                oenginecons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
            self.environment[
                oenginecons.PKIEnv.ORG
            ] = ca.subject.get_attributes_for_oid(
                x509.oid.NameOID.ORGANIZATION_NAME
            )[0].value
            self.environment[
                oenginecons.PKIEnv.COUNTRY
            ] = ca.subject.get_attributes_for_oid(
                x509.oid.NameOID.COUNTRY_NAME
            )[0].value

        self.logger.info(_('Upgrading CA'))

        #
        # LEGACY NOTE
        # Since 3.0 and maybe before the method of
        # allowing user to override AIA was to explict
        # edit files. Until we rewrite the entire PKI
        # we must preserve this approach.
        # The template may change over time, so regenerate.
        #
        def _template_aia(template):
            aia = None
            if os.path.exists(template):
                with open(template) as f:
                    PREFIX = 'caIssuers;URI:'
                    for line in f.read().splitlines():
                        if line.startswith('authorityInfoAccess'):
                            aia = line[line.find(PREFIX)+len(PREFIX):]
                            break
            return aia

        engine_aia = _template_aia(_CERT_TEMPLATE)
        qemu_aia = _template_aia(_QEMU_CERT_TEMPLATE)
        if qemu_aia is None:
            qemu_aia = self._calculated_aia(
                oenginecons.Const.ENGINE_PKI_QEMU_CA_URI
            )

        if engine_aia and 'resource=qemu-ca-certificate' in engine_aia:
            # In the past, we had a single template for both engine and qemu
            # CAs, and it pointed at qemu cert.
            uninstall_info = self.environment[
                osetupcons.CoreEnv.UNINSTALL_FILES_INFO
            ].get(_CERT_TEMPLATE)
            if uninstall_info and not uninstall_info.get("changed"):
                # It was written by engine-setup and not changed since.
                # It should be safe to replace it.
                engine_aia = self._calculated_aia(
                    oenginecons.Const.ENGINE_PKI_CA_URI
                )
                self.logger.info(_('Fixing {}'.format(_CERT_TEMPLATE)))
                self.dialog.note(_(
                    'This does not fix existing certificates.'
                ))
            else:
                self.logger.warn(
                    _(
                        '{template} has wrong data, but was manually changed '
                        'after previous engine-setup'
                    ).format(
                        template=_CERT_TEMPLATE,
                    )
                )
                self.dialog.note(_('Not fixing it.'))
            self.dialog.note(_(
                'Please see also: https://bugzilla.redhat.com/1875386'
            ))

        uninstall_files = []
        self._setupUninstall(uninstall_files)
        self._update_templates(
            engine_aia,
            _ENGINE_TEMPLATES_MAP,
            uninstall_files,
        )
        self._update_templates(
            qemu_aia,
            _QEMU_TEMPLATES_MAP,
            uninstall_files,
        )

        if self.environment[oenginecons.PKIEnv.RENEW]:
            for ca_file in self._CA_FILES:
                if (
                    os.path.exists(ca_file) and
                    pki_utils.cert_expires(pki_utils.x509_load_cert(ca_file))
                ):
                    self._renewed_ca_files.add(ca_file)
                    self.logger.info(_('Renewing CA: %s'), ca_file)
                    args = (
                        oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_CREATE,
                        '--renew',
                        '--keystore-password=%s' % (
                            self.environment[oenginecons.PKIEnv.STORE_PASS],
                        ),
                        '--ca-file=%s' % (self._ca_file_name(ca_file),),
                    )
                    self.execute(
                        args=args,
                        envAppend={
                            'JAVA_HOME': self.environment[
                                oengcommcons.ConfigEnv.JAVA_HOME
                            ],
                        },
                    )

            self._enrollCertificates(True, uninstall_files)

        # Also enroll missing parts on upgrade
        # We check just Engine CA, QEMU certificates are only on hosts
        if os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
        ):
            self._enrollCertificates(False, uninstall_files)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.CA_AVAILABLE,
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
        ),
    )
    def _create_primary_ca(self):
        self._create_ca(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_KEY,
            oenginecons.Const.ENGINE_PKI_CA_URI,
            _ENGINE_TEMPLATES_MAP,
        )

        uninstall_files = []
        self._setupUninstall(uninstall_files)

        if not os.path.exists(
            oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
        ):
            os.symlink(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            )
            uninstall_files.append(
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            )

        self._enrollCertificates(False, uninstall_files)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.QEMU_CA_AVAILABLE,
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_CERT
            )
        ),
    )
    def _create_qemu_ca(self):
        self._create_ca(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_CERT,
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_KEY,
            oenginecons.Const.ENGINE_PKI_QEMU_CA_URI,
            _QEMU_TEMPLATES_MAP,
            'qemu'
        )

    def _create_ca(self, ca_file, key_file, ca_uri, templates_map, ou=None):
        self._enabled = True

        # TODO
        # this implementaiton is not transactional
        # too many issues with legacy ca implementation
        # need to work this out to allow transactional
        # for now just delete files if we fail
        uninstall_files = []
        self._setupUninstall(uninstall_files)
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.CATransaction(
                parent=self,
                uninstall_files=uninstall_files,
            )
        )

        # LEGACY NOTE
        # This is needed for avoiding error in create_ca when supporting
        # max cn length of 64.
        # please DON'T increase this size, any value over 55 will fail the
        # setup. the truncated host-fqdn is concatenated with a random string
        # to create a unique CN value.
        MAX_HOST_FQDN_LEN = 55

        self.logger.info(_('Creating CA: {}').format(ca_file))

        self._update_templates(
            self._calculated_aia(ca_uri),
            templates_map,
            uninstall_files,
        )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_CREATE,
                '--subject=/C=%s/O=%s%s/CN=%s.%s' % (
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.COUNTRY],
                    ),
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.ORG],
                    ),
                    ('' if ou is None else '/OU=%s' % (ou,)),
                    self._subjectComponentEscape(
                        self.environment[
                            osetupcons.ConfigEnv.FQDN
                        ][:MAX_HOST_FQDN_LEN],
                    ),
                    random.randint(10000, 99999),
                ),
                '--keystore-password=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--ca-file=%s' % (self._ca_file_name(ca_file),),
            ),
            envAppend={
                'JAVA_HOME': self.environment[
                    oengcommcons.ConfigEnv.JAVA_HOME
                ],
            },
        )

        uninstall_files.extend(
            (
                ca_file,
                key_file,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def miscOptions(self):
        vdcoption.VdcOption(
            statement=self.environment[oenginecons.EngineDBEnv.STATEMENT]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'OrganizationName',
                    'value': self.environment[
                        oenginecons.PKIEnv.ORG
                    ],
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _closeup(self):
        x509cert = pki_utils.x509_load_cert(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
        )
        self.dialog.note(
            text=_('Internal CA {fingerprint}').format(
                fingerprint=re.sub(
                    r'(..)',
                    r':\1',
                    binascii.b2a_hex(
                        x509cert.fingerprint(
                            algorithm=hashes.SHA1()
                        )
                    ).decode().upper()
                )[1:],
            )
        )
        for ca_file in self._renewed_ca_files:
            self.logger.warning(
                _(
                    'CA %s was renewed, please refresh manually '
                    'distributed copies'
                ),
                ca_file
            )


# vim: expandtab tabstop=4 shiftwidth=4
