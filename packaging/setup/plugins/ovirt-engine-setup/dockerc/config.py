#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015-2016 Red Hat, Inc.
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


"""ovirt-engine-setup dockerc plugin."""


import base64
import gettext
import json
import uuid

from M2Crypto import RSA, X509

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import hostname as osetuphostname
from ovirt_setup_lib import dialog
from ovirt_engine_setup.dockerc import constants as odockerccons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-engine-setup dockerc plugin."""

    def _encrypt_pwd(self, value):
        x509 = X509.load_cert(
            file=(
                oenginecons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CERT
            ),
            format=X509.FORMAT_PEM,
        )
        evalue = base64.b64encode(
            x509.get_pubkey().get_rsa().public_encrypt(
                data=value,
                padding=RSA.pkcs1_padding,
            ),
        )
        return evalue

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._dimages = []
        self._already_deployed_by_me = []
        self._dcli = None

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            odockerccons.ConfigEnv.DOCKERC_CINDER,
            None
        )
        self.environment.setdefault(
            odockerccons.ConfigEnv.DOCKERC_GLANCE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=odockerccons.Stages.DOCKERC_CUSTOMIZE,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_CINDER
        ] is None:
            self.environment[
                odockerccons.ConfigEnv.DOCKERC_CINDER
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_DOCKERC_CINDER',
                note=_(
                    'Deploy Cinder container on this host '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=True,
            )
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_GLANCE
        ] is None:
            self.environment[
                odockerccons.ConfigEnv.DOCKERC_GLANCE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_DOCKERC_GLANCE',
                note=_(
                    'Deploy Glance container on this host '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=True,
            )
        self._enabled = self.environment[
            odockerccons.ConfigEnv.DOCKERC_CINDER
        ] or self.environment[
            odockerccons.ConfigEnv.DOCKERC_GLANCE
        ]

        tag = ':' + self.environment[
            odockerccons.ConfigEnv.DOCKERC_CTAG
        ]

        self._dimages = [
            {
                'image': odockerccons.Const.C_IMAGE_RABBITMQ + tag,
                'name': odockerccons.Const.C_NAME_RABBITMQ
            },
            {
                'image': odockerccons.Const.C_IMAGE_MARIADBDATA + tag,
                'name': odockerccons.Const.C_NAME_MARIADBDATA
            },
            {
                'image': odockerccons.Const.C_IMAGE_MARIADBAPP + tag,
                'name': odockerccons.Const.C_NAME_MARIADBAPP
            },
            {
                'image': odockerccons.Const.C_IMAGE_KEYSTONE + tag,
                'name': odockerccons.Const.C_NAME_KEYSTONE
            }
        ]

        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_CINDER
        ]:
            self._dimages.append(
                {
                    'image': odockerccons.Const.C_IMAGE_CINDER + tag,
                    'name': odockerccons.Const.C_NAME_CINDER
                }
            )

        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_GLANCE
        ]:
            self._dimages.extend(
                [
                    {
                        'image': odockerccons.Const.C_IMAGE_GLANCE_REG + tag,
                        'name': odockerccons.Const.C_NAME_GLANCE_REG
                    },
                    {
                        'image': odockerccons.Const.C_IMAGE_GLANCE_API + tag,
                        'name': odockerccons.Const.C_NAME_GLANCE_API
                    },
                ]
            )
        if self._enabled:
            self.environment[
                odockerccons.ConfigEnv.DOCKERC_NEEDED
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            odockerccons.Stages.DOCKERC_CUSTOMIZE,
        ),
    )
    def _customization_firewall(self):
        # TODO: ket the user customize service ports
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_CINDER
        ]:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-local-cinder',
                    'directory': 'dockerc'
                },
            ])
            self.environment[
                osetupcons.NetEnv.FIREWALLD_SUBST
            ].update({
                '@OVIRT_CINDER_PORT@': odockerccons.Const.CINDER_SERVICE_PORT
            })
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_GLANCE
        ]:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-local-glance',
                    'directory': 'dockerc'
                },
            ])
            self.environment[
                osetupcons.NetEnv.FIREWALLD_SUBST
            ].update({
                '@OVIRT_GLANCE_PORT@': odockerccons.Const.GLANCE_SERVICE_PORT
            })

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        import docker
        self._dcli = docker.Client(base_url='unix://var/run/docker.sock')

        already_existing = set(
            [
                d['name']
                for d in self._dimages
            ]
        ).intersection(
            set(
                [
                    str(name).lstrip('/')
                    for d in self._dcli.containers(all=True)
                    for name in d['Names']
                ]
            )
        )

        if self.environment[
            odockerccons.RemoveEnv.REMOVE_DCLIST
        ]:
            self._already_deployed_by_me = [
                x.strip()
                for x in self.environment[
                    odockerccons.RemoveEnv.REMOVE_DCLIST
                ].split(',')
                if x
            ]

        already_existing_not_by_me = already_existing.difference(
            self._already_deployed_by_me
        )
        # TODO: evaluate if we prefer to deploy adding a random/time-based
        # suffix to the container name.
        if already_existing_not_by_me:
            self.dialog.note(
                text=_(
                    'The following containers were found:\n'
                    '    {found}\n'
                    'Please remove or rename and '
                    'then execute Setup again\n'
                ).format(
                    found=', '.join(already_existing),
                ),
            )
            raise RuntimeError(_('Please check existing containers'))

        self._dimages = [
            c
            for c in self._dimages
            if c['name'] not in self._already_deployed_by_me
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=odockerccons.Stages.DOCKERC_DEPLOY,
        condition=lambda self: self._enabled,
    )
    def _misc_deploy(self):
        import docker
        fqdn = self.environment[osetupcons.ConfigEnv.FQDN]

        envdict = {
            'ADMIN_USER_PASSWORD': odockerccons.Const.ADMIN_USER_PASSWORD,
            'ADMIN_TENANT_NAME': odockerccons.Const.ADMIN_TENANT_NAME,
            # TODO: use randomly generated passwords
            'DB_ROOT_PASSWORD': odockerccons.Const.MARIADB_ROOT_PASSWORD,
            'CINDER_DB_NAME': odockerccons.Const.CINDER_DB_NAME,
            'CINDER_DB_USER': odockerccons.Const.CINDER_DB_USER,
            'CINDER_DB_PASSWORD': odockerccons.Const.CINDER_DB_PASSWORD,
            'CINDER_KEYSTONE_USER': odockerccons.Const.CINDER_KEYSTONE_USER,
            'CINDER_ADMIN_PASSWORD': odockerccons.Const.CINDER_ADMIN_PASSWORD,
            'GLANCE_API_SERVICE_HOST': fqdn,
            'GLANCE_DB_NAME': odockerccons.Const.GLANCE_DB_NAME,
            'GLANCE_DB_PASSWORD': odockerccons.Const.GLANCE_DB_PASSWORD,
            'GLANCE_DB_USER': odockerccons.Const.GLANCE_DB_USER,
            'GLANCE_KEYSTONE_PASSWORD': odockerccons.Const.
            GLANCE_KEYSTONE_PASSWORD,
            'GLANCE_KEYSTONE_USER': odockerccons.Const.GLANCE_KEYSTONE_USER,
            'GLANCE_REGISTRY_SERVICE_HOST': fqdn,
            'KEYSTONE_ADMIN_PASSWORD': odockerccons.Const.
            KEYSTONE_ADMIN_PASSWORD,
            'KEYSTONE_ADMIN_SERVICE_HOST': fqdn,
            'KEYSTONE_ADMIN_SERVICE_PORT': odockerccons.Const.
            KEYSTONE_ADMIN_SERVICE_PORT,
            'KEYSTONE_ADMIN_TOKEN': odockerccons.Const.KEYSTONE_ADMIN_TOKEN,
            'KEYSTONE_AUTH_PROTOCOL': odockerccons.Const.
            KEYSTONE_AUTH_PROTOCOL,
            'KEYSTONE_DB_PASSWORD': odockerccons.Const.KEYSTONE_DB_PASSWORD,
            'KEYSTONE_PUBLIC_SERVICE_HOST': fqdn,
            'MARIADB_SERVICE_HOST': fqdn,
            'MARIADB_ROOT_PASSWORD': odockerccons.Const.MARIADB_ROOT_PASSWORD,
            'RABBITMQ_PASS': odockerccons.Const.RABBIT_PASSWORD,
            'RABBITMQ_SERVICE_HOST': fqdn,
            'RABBITMQ_USER': odockerccons.Const.RABBIT_USER,
            'RABBIT_PASSWORD': odockerccons.Const.RABBIT_PASSWORD,
            'RABBIT_USERID': odockerccons.Const.RABBIT_USER,
        }

        hostname = osetuphostname.Hostname(plugin=self)
        dnsresolved = hostname.isResolvedByDNS(fqdn)
        # TODO: check if we also need to force container DNS
        for cont in self._dimages:
            self.logger.info(_('Pulling {cname}').format(cname=cont['name']))
            for line in self._dcli.pull(cont['image'], stream=True):
                jline = json.loads(line)
                self.logger.debug(json.dumps(jline, indent=4))
                if 'error' in jline:
                    raise RuntimeError(
                        _("Unable to pull image {cname}: {message}").format(
                            cname=cont['image'],
                            message=jline['errorDetail']['message'],
                        )
                    )
        for cont in self._dimages:
            self.logger.info(_('Creating {cname}').format(cname=cont['name']))
            try:
                container = self._dcli.create_container(
                    image=cont['image'],
                    name=cont['name'],
                    environment=envdict,
                )
            except docker.errors.APIError as ex:
                if ex.response.status_code == 404:
                    raise RuntimeError(
                        _(
                            'Unable to find image {image}: {explanation}'
                        ).format(
                            image=cont['image'],
                            explanation=ex.explanation
                        )
                    )
                elif ex.response.status_code == 409:
                    raise RuntimeError(
                        _(
                            'Name conflict creating container {cname}: '
                            '{explanation}'
                        ).format(
                            cname=cont['name'],
                            explanation=ex.explanation
                        )
                    )
                elif ex.response.status_code == 500:
                    raise RuntimeError(
                        _(
                            'docker server error creating container {cname}: '
                            '{explanation}'
                        ).format(
                            cname=cont['name'],
                            explanation=ex.explanation
                        )
                    )
                else:
                    raise ex

            self.logger.info(_('Starting {cname}').format(cname=cont['name']))
            cid = container.get('Id')
            self.logger.debug(
                'Container {cname}: {cid}'.format(
                    cname=cont['name'],
                    cid=cid,
                )
            )
            try:
                self._dcli.start(
                    container=cid,
                    restart_policy={
                        "Name": "always"
                    },
                    volumes_from=(
                        odockerccons.Const.C_NAME_MARIADBDATA
                        if cont['name'] == odockerccons.Const.C_NAME_MARIADBAPP
                        else None
                    ),
                    network_mode='host',
                    extra_hosts=(
                        {fqdn: hostname.getResolvedAddresses(fqdn).pop()}
                        if not dnsresolved
                        else None
                    )
                )
            except docker.errors.APIError as ex:
                if ex.response.status_code == 404:
                    raise RuntimeError(
                        _(
                            'Unable to start container {cname}: {explanation}'
                        ).format(
                            cname=cont['name'],
                            explanation=ex.explanation
                        )
                    )
                else:
                    raise ex

        dlist = [
            d['name']
            for d in self._dimages
        ]
        dlist.extend(self._already_deployed_by_me)
        self.environment[
            odockerccons.RemoveEnv.REMOVE_DCLIST
        ] = ', '.join(set(dlist))

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oenginecons.Stages.CA_AVAILABLE,
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
            odockerccons.Stages.DOCKERC_DEPLOY
        ),
        condition=lambda self: self._enabled,
    )
    def _misc_configure(self):
        self.logger.debug('Generating a new uuid for glance provider')
        gpUUID = str(uuid.uuid4())
        self.logger.debug('Generating a new uuid for glance storage domain')
        gsdUUID = str(uuid.uuid4())

        # TODO: setup also for remote engine
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_GLANCE
        ]:
            self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
                statement="""
                    select inst_add_glance_provider(
                        %(provider_id)s,
                        %(provider_name)s,
                        %(provider_description)s,
                        %(provider_url)s,
                        %(storage_domain_id)s,
                        %(auth_required)s,
                        %(auth_username)s,
                        %(auth_password)s,
                        %(auth_url)s,
                        %(tenant_name)s
                    )
                """,
                args=dict(
                    provider_id=gpUUID,
                    provider_name='local-glance-image-repository',
                    provider_description=_(
                        'Local Glance repository for oVirt'
                    ),
                    provider_url=(
                        'http://' +
                        self.environment[osetupcons.ConfigEnv.FQDN] +
                        ':' +
                        odockerccons.Const.GLANCE_SERVICE_PORT
                    ),
                    storage_domain_id=gsdUUID,
                    auth_required="true",
                    auth_username=odockerccons.Const.GLANCE_KEYSTONE_USER,
                    auth_password=self._encrypt_pwd(
                        odockerccons.Const.GLANCE_KEYSTONE_PASSWORD
                    ),
                    auth_url=(
                        odockerccons.Const.KEYSTONE_AUTH_PROTOCOL +
                        '://' +
                        self.environment[osetupcons.ConfigEnv.FQDN] +
                        ':' +
                        odockerccons.Const.KEYSTONE_PUBLIC_SERVICE_PORT +
                        '/v2.0'
                    ),
                    tenant_name=odockerccons.Const.ADMIN_TENANT_NAME,
                ),
            )
        if self.environment[
            odockerccons.ConfigEnv.DOCKERC_CINDER
        ]:
            # TODO: add also cinder as an external provider when
            # configurable in the engine
            pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        self.logger.info(_('Enabling Docker'))
        self.services.startup(
            name=odockerccons.Const.DOCKER_SERVICE_NANE,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
