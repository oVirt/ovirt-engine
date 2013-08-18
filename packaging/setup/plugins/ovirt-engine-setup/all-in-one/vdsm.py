#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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


"""
VDSM configuration plugin.
"""

import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')
import contextlib
import urllib2
import time
import re
import distutils.version


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import config as osetupconfig


@util.export
class Plugin(plugin.PluginBase):
    """
    VDSM configuration plugin.
    """

    ENGINE_RETRIES = 20
    ENGINE_DELAY = 3
    VDSM_RETRIES = 600
    VDSM_DELAY = 1
    DB_UP_RE = re.compile('.*DB Up.*')

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    def _waitVDSMHostUp(self, engine_api, host):
        self.logger.info(_(
            'Waiting for VDSM host to become operational.'
            'This may take several minutes...'
        ))
        tries = self.VDSM_RETRIES
        isUp = False
        while not isUp and tries > 0:
            tries -= 1
            try:
                state = engine_api.hosts.get(host).status.state
            except Exception as exc:
                #sadly all ovirtsdk errors inherit only from Exception
                self.logger.debug(
                    'Error fetching host state: {error}'.format(
                        error=str(exc),
                    )
                )
                state = ''
            if 'failed' in state:
                self.logger.error(_(
                    'The VDSM host was found in a failed state. '
                    'Please check engine and bootstrap installation logs.'
                ))
                tries = -1  # Error state
            elif state == 'up':
                isUp = True
                self.logger.info(_('The VDSM Host is now operational'))
            else:
                self.logger.debug(
                    'VDSM host in {state} state'.format(
                        state=state,
                    )
                )
                if tries % 30 == 0:
                    self.logger.info(_(
                        'Still waiting for VDSM host to become operational...'
                    ))
                time.sleep(self.VDSM_DELAY)
        if not isUp and tries == 0:
            self.logger.error(_(
                'Timed out while waiting for host to start. '
                'Please check the logs.'
            ))
        return isUp

    def _waitEngineUp(self):
        self.logger.debug('Waiting for Engine health status')
        health_url = 'http://{fqdn}:{port}/OvirtEngineWeb/HealthStatus'.format(
            fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
            port=self.environment[osetupcons.ConfigEnv.PUBLIC_HTTP_PORT],
        )
        tries = self.ENGINE_RETRIES
        isUp = False
        while not isUp and tries > 0:
            tries -= 1
            try:
                with contextlib.closing(urllib2.urlopen(health_url)) as urlObj:
                    content = urlObj.read()
                    if content:
                        if self.DB_UP_RE.match(content) is None:
                            raise RuntimeError(
                                _('Engine status: {status}').format(
                                    status=content
                                )
                            )
                        isUp = True
            except urllib2.URLError:
                time.sleep(self.ENGINE_DELAY)
        if not isUp:
            raise RuntimeError(_('Engine unreachable'))

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.AIOEnv.LOCAL_DATA_CENTER,
            osetupcons.AIODefaults.DEFAULT_LOCAL_DATA_CENTER
        )
        self.environment.setdefault(
            osetupcons.AIOEnv.LOCAL_CLUSTER,
            osetupcons.AIODefaults.DEFAULT_LOCAL_CLUSTER
        )
        self.environment.setdefault(
            osetupcons.AIOEnv.LOCAL_HOST,
            osetupcons.AIODefaults.DEFAULT_LOCAL_HOST
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ],
    )
    def _validation(self):
        import ovirtsdk.api
        import ovirtsdk.xml
        self._ovirtsdk_api = ovirtsdk.api
        self._ovirtsdk_xml = ovirtsdk.xml

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ],
    )
    def _misc(self):
        self.environment[osetupcons.ApacheEnv.NEED_RESTART] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.AIO_CONFIG_VDSM,
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ],
        after=(
            osetupcons.Stages.AIO_CONFIG_STORAGE,
            osetupcons.Stages.CORE_ENGINE_START,
            osetupcons.Stages.APACHE_RESTART,
        ),
    )
    def _closeup(self):
        self._waitEngineUp()
        self.logger.debug('Connecting to the Engine')
        engine_api = self._ovirtsdk_api.API(
            url='https://{fqdn}:{port}/api'.format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                port=self.environment[osetupcons.ConfigEnv.PUBLIC_HTTPS_PORT],
            ),
            username='{user}@{domain}'.format(
                user=osetupcons.Const.USER_ADMIN,
                domain=osetupcons.Const.DOMAIN_INTERNAL,
            ),
            password=self.environment[osetupcons.ConfigEnv.ADMIN_PASSWORD],
            ca_file=osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
        )

        SupportedClusterLevels = self.environment[
            osetupcons.DBEnv.STATEMENT
        ].getVdcOption(name='SupportedClusterLevels')
        self.logger.debug(
            'SupportedClusterLevels [{levels}], '
            'PACKAGE_VERSION [{pv}],'.format(
                levels=SupportedClusterLevels,
                pv=osetupconfig.PACKAGE_VERSION,
            )
        )
        v = max(
            distutils.version.LooseVersion(vs).version
            for vs in SupportedClusterLevels.split(',')
        )
        engine_version = self._ovirtsdk_xml.params.Version(
            major=v[0],
            minor=v[1],
        )

        self.logger.debug('Creating the local data center')
        engine_api.datacenters.add(
            self._ovirtsdk_xml.params.DataCenter(
                name=self.environment[osetupcons.AIOEnv.LOCAL_DATA_CENTER],
                storage_type='localfs',
                version=engine_version,
            )
        )

        self.logger.debug(
            'Creating the local cluster into the local data center'
        )
        engine_api.clusters.add(
            self._ovirtsdk_xml.params.Cluster(
                name=self.environment[osetupcons.AIOEnv.LOCAL_CLUSTER],
                cpu=self._ovirtsdk_xml.params.CPU(
                    id=self.environment[osetupcons.AIOEnv.VDSM_CPU]
                ),
                data_center=engine_api.datacenters.get(
                    self.environment[osetupcons.AIOEnv.LOCAL_DATA_CENTER]
                ),
                version=engine_version
            )
        )

        self.logger.debug('Adding the local host to the local cluster')
        #At this stage sshd is already running
        engine_api.hosts.add(
            self._ovirtsdk_xml.params.Host(
                name=self.environment[osetupcons.AIOEnv.LOCAL_HOST],
                address=self.environment[osetupcons.ConfigEnv.FQDN],
                reboot_after_installation=False,
                override_iptables=False,
                cluster=engine_api.clusters.get(
                    self.environment[osetupcons.AIOEnv.LOCAL_CLUSTER]
                ),
                ssh=self._ovirtsdk_xml.params.SSH(
                    authentication_method='publickey',
                ),
            )
        )
        if not self._waitVDSMHostUp(
            engine_api=engine_api,
            host=self.environment[osetupcons.AIOEnv.LOCAL_HOST],
        ):
            self.logger.warning(_(
                'Local storage domain not added because '
                'the VDSM host was not up. Please add it manually.'
            ))
        else:
            self.logger.debug('Adding local storage domain')
            storage = self._ovirtsdk_xml.params.Storage(
                path=self.environment[
                    osetupcons.AIOEnv.STORAGE_DOMAIN_DIR
                ].rstrip('/'),
            )
            storage.set_type('localfs')

            storage_domain = self._ovirtsdk_xml.params.StorageDomain(
                name=self.environment[osetupcons.AIOEnv.STORAGE_DOMAIN_NAME],
                data_center=engine_api.datacenters.get(
                    self.environment[osetupcons.AIOEnv.LOCAL_DATA_CENTER]
                ),
                storage_format='v3',
                host=engine_api.hosts.get(
                    self.environment[osetupcons.AIOEnv.LOCAL_HOST]
                ),
                storage=storage
            )
            storage_domain.set_type('data')
            engine_api.storagedomains.add(storage_domain)


# vim: expandtab tabstop=4 shiftwidth=4
