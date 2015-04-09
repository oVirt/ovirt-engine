#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2014 Red Hat, Inc.
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


import os
import re
import random
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from M2Crypto import X509


from otopi import util
from otopi import plugin
from otopi import transaction
from otopi import filetransaction
from otopi import constants as otopicons


from ovirt_engine import util as outil


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    class CATransaction(transaction.TransactionElement):
        """yum transaction element."""

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

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._ca_was_renewed = False

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
            oenginecons.PKIEnv.COUNTRY,
            oengcommcons.Defaults.DEFAULT_PKI_COUNTRY
        )
        self.environment.setdefault(
            oenginecons.PKIEnv.ORG,
            None
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
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            )
        ),
        before=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
    )
    def _miscUpgrade(self):
        self.logger.info(_('Upgrading CA'))

        #
        # LEGACY NOTE
        # Since 3.0 and maybe before the method of
        # allowing user to override AIA was to explict
        # edit files. Until we rewrite the entire PKI
        # we must preserve this approach.
        # The template may change over time, so regenerate.
        #
        aia = None
        template = oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE[
            :-len('.in')
        ]
        if os.path.exists(template):
            with open(template) as f:
                PREFIX = 'caIssuers;URI:'
                for l in f.readlines():
                    if l.startswith('authorityInfoAccess'):
                        aia = l[l.find(PREFIX)+len(PREFIX):]
                        break

        uninstall_files = []
        self._setupUninstall(uninstall_files)
        if aia is not None:
            localtransaction = transaction.Transaction()
            with localtransaction:
                for name in (
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_TEMPLATE,
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE,
                ):
                    localtransaction.append(
                        filetransaction.FileTransaction(
                            name=name[:-len('.in')],
                            content=outil.processTemplate(
                                name,
                                {
                                    '@AIA@': aia,
                                }
                            ),
                            modifiedList=uninstall_files,
                        ),
                    )
                    localtransaction.append(
                        filetransaction.FileTransaction(
                            name=name[:-len('.template.in')] + '.conf',
                            content=outil.processTemplate(
                                name,
                                {
                                    '@AIA@': aia,
                                }
                            ),
                            modifiedList=uninstall_files,
                        ),
                    )

        #
        # LEGACY NOTE
        # Since 3.0 and maybe before the CA certificate's
        # notBefore attribute was set using timezone offset
        # instead of Z
        # in this case we need to reissue CA certificate.
        #
        x509 = X509.load_cert(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
        )
        if x509.get_not_before().get_datetime().tzname() is None:
            self._ca_was_renewed = True
            self.logger.info(_('Renewing CA'))
            self.execute(
                args=(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_CREATE,
                    '--renew',
                    '--keystore-password=%s' % (
                        self.environment[oenginecons.PKIEnv.STORE_PASS],
                    ),
                ),
                envAppend={
                    'JAVA_HOME': self.environment[
                        oengcommcons.ConfigEnv.JAVA_HOME
                    ],
                },
            )

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
    def _misc(self):
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

        self.logger.info(_('Creating CA'))

        localtransaction = transaction.Transaction()
        with localtransaction:
            for name in (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_TEMPLATE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_TEMPLATE,
            ):
                localtransaction.append(
                    filetransaction.FileTransaction(
                        name=name[:-len('.in')],
                        content=outil.processTemplate(
                            name,
                            {
                                '@AIA@': 'http://%s:%s%s' % (
                                    self.environment[
                                        osetupcons.ConfigEnv.FQDN
                                    ],
                                    self.environment[
                                        oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
                                    ],
                                    oenginecons.Const.ENGINE_PKI_CA_URI,
                                )
                            }
                        ),
                        modifiedList=uninstall_files,
                    ),
                )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_CREATE,
                '--subject=/C=%s/O=%s/CN=%s.%s' % (
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.COUNTRY],
                    ),
                    self._subjectComponentEscape(
                        self.environment[oenginecons.PKIEnv.ORG],
                    ),
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
            ),
            envAppend={
                'JAVA_HOME': self.environment[
                    oengcommcons.ConfigEnv.JAVA_HOME
                ],
            },
        )

        for name in (
            'engine',
            'apache',
            'jboss',
            'websocket-proxy',
            'reports'
        ):
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
                ),
            )

        uninstall_files.extend(
            (
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_APACHE_STORE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_KEY,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CERT,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_JBOSS_STORE,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_JBOSS_CERT,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CA_CERT_CONF,
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_CERT_CONF,
                (
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_CERT
                ),
                (
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_STORE
                ),
            )
        )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=websocket-proxy',
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=%s' % (
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_KEY,
                ),
            ),
            logStreams=False,
        )
        uninstall_files.append(
            oenginecons.FileLocations.
            OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_KEY
        )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=reports',
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=%s' % (
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_PKI_REPORTS_KEY,
                ),
            ),
            logStreams=False,
        )
        uninstall_files.append(
            oenginecons.FileLocations.
            OVIRT_ENGINE_PKI_REPORTS_KEY
        )

        self.execute(
            args=(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=apache',
                '--passin=%s' % (
                    self.environment[oenginecons.PKIEnv.STORE_PASS],
                ),
                '--key=%s' % (
                    oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
                ),
            ),
            logStreams=False,
        )
        uninstall_files.append(
            oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY
        )

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

        for f in (
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE,
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_JBOSS_STORE,
            oenginecons.FileLocations.
            OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_KEY,
            oenginecons.FileLocations.
            OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_STORE,
        ):
            os.chown(
                f,
                osetuputil.getUid(
                    self.environment[osetupcons.SystemEnv.USER_ENGINE]
                ),
                -1,
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
        x509 = X509.load_cert(
            file=oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            format=X509.FORMAT_PEM,
        )
        self.dialog.note(
            text=_('Internal CA {fingerprint}').format(
                fingerprint=re.sub(
                    r'(..)',
                    r':\1',
                    x509.get_fingerprint(md='sha1'),
                )[1:],
            )
        )
        if self._ca_was_renewed:
            self.logger.warning(
                _(
                    'Internal CA was renewed, please refresh manually '
                    'distributed copies'
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
