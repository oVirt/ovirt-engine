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


"""Constants."""


import gettext

from otopi import util

from ovirt_engine_setup.constants import osetupattrs, osetupattrsclass


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Const(object):
    C_IMAGE_RABBITMQ = 'kollaglue/centos-rdo-rabbitmq'
    C_NAME_RABBITMQ = 'rabbitmq'
    C_IMAGE_MARIADBDATA = 'kollaglue/centos-rdo-mariadb-data'
    C_NAME_MARIADBDATA = 'mariadbdata'
    C_IMAGE_MARIADBAPP = 'kollaglue/centos-rdo-mariadb-app'
    C_NAME_MARIADBAPP = 'mariadbapp'
    C_IMAGE_KEYSTONE = 'kollaglue/centos-rdo-keystone'
    C_NAME_KEYSTONE = 'keystone'

    C_IMAGE_CINDER = 'kollaglue/centos-rdo-cinder'
    C_NAME_CINDER = 'cinder'

    C_IMAGE_GLANCE_REG = 'kollaglue/centos-rdo-glance-registry'
    C_NAME_GLANCE_REG = 'glance-registry'
    C_IMAGE_GLANCE_API = 'kollaglue/centos-rdo-glance-api'
    C_NAME_GLANCE_API = 'glance-api'

    DOCKER_SERVICE_NANE = 'docker'

    DEFAULT_CTAG = 'kilo'

    # Admin user password
    ADMIN_USER_PASSWORD = 'steakfordinner'
    # Database
    MARIADB_ROOT_PASSWORD = 'kolla'
    PASSWORD = '12345'
    # Host
    ADMIN_TENANT_NAME = 'admin'
    # RabbitMQ
    RABBIT_USER = 'guest'
    RABBIT_PASSWORD = 'guest'
    # Keystone
    KEYSTONE_ADMIN_TOKEN = PASSWORD
    KEYSTONE_DB_PASSWORD = 'kolla'
    KEYSTONE_ADMIN_PASSWORD = PASSWORD
    KEYSTONE_AUTH_PROTOCOL = 'http'
    KEYSTONE_ADMIN_SERVICE_PORT = '35357'
    KEYSTONE_PUBLIC_SERVICE_PORT = '5000'
    # Glance
    GLANCE_DB_NAME = 'glance'
    GLANCE_DB_USER = 'glance'
    GLANCE_DB_PASSWORD = 'kolla'
    GLANCE_KEYSTONE_USER = 'glance'
    GLANCE_KEYSTONE_PASSWORD = 'glance'
    GLANCE_SERVICE_PORT = '9292'
    # Cinder
    CINDER_ADMIN_PASSWORD = 'cinder'
    CINDER_DB_NAME = 'cinder'
    CINDER_DB_USER = 'cinder'
    CINDER_DB_PASSWORD = 'kolla'
    CINDER_KEYSTONE_USER = 'cinder'
    CINDER_ADMIN_PASSWORD = 'cinder'
    CINDER_SERVICE_PORT = '8776'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):
    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Deploy Cinder container'),
        postinstallfile=True,
        reconfigurable=True,
    )
    def DOCKERC_CINDER(self):
        return 'OVESETUP_CONFIG/cinderCDeploy'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Deploy Glance container'),
        postinstallfile=True,
        reconfigurable=True,
    )
    def DOCKERC_GLANCE(self):
        return 'OVESETUP_CONFIG/glanceCDeploy'

    @osetupattrs(
        answerfile=True,
    )
    def DOCKERC_DAEMON(self):
        return 'OVESETUP_CONFIG/dockerDaemon'

    DOCKERC_NEEDED = 'OVESETUP_CONFIG/dockercNeeded'

    @osetupattrs(
        answerfile=True,
        postinstallfile=True,
    )
    def DOCKERC_CTAG(self):
        return 'OVESETUP_CONFIG/dockercTag'


@util.export
class Stages(object):
    DOCKERC_CUSTOMIZE = 'osetup.dockerc.customize'
    DOCKERC_DEPLOY = 'osetup.dockerc.deploy'
    REMOVE_CUSTOMIZATION_DOCKERC = 'osetup.dockerc.remove.customization'


@util.export
@util.codegen
@osetupattrsclass
class RemoveEnv(object):
    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_DOCKERC(self):
        return 'OVESETUP_REMOVE/removeDockerc'

    @osetupattrs(
        postinstallfile=True,
    )
    def REMOVE_DCLIST(self):
        return 'OVESETUP_REMOVE/deployedDCList'


# vim: expandtab tabstop=4 shiftwidth=4
