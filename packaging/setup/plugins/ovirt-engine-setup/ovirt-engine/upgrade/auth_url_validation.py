#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


""" DB validations plugin."""


import gettext
import re


from collections import namedtuple

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

Provider = namedtuple(
    'Provider',
    ['id', 'name', 'invalid_auth_url', 'valid_auth_url']
)


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """auth_url validation plugin."""

    _VALID_AUTH_URL = '^http(s)?://[^/]*:[\\d]+/(v3|v2\\.0)/?$'
    _RE_VALID_AUTH_URL = re.compile(_VALID_AUTH_URL)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._providers_to_update = []

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        ),
        condition=lambda self: (
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _customization(self):
        for provider in self._get_invalid_providers():
            self._providers_to_update.append(
                Provider(
                    id=provider['id'],
                    name=provider['name'],
                    invalid_auth_url=provider['auth_url'],
                    valid_auth_url=self._query_updated_auth_url(provider)
                )
            )

    def _get_invalid_providers(self):
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        statement = (
            'SELECT id,name,auth_url '
            'FROM providers WHERE '
            'auth_url !~ \'{valid_url}\''.format(
                valid_url=self._VALID_AUTH_URL,
                ownConnection=True,
            )
        )
        return dbstatement.execute(
            statement=statement,
            ownConnection=True,
            transaction=False,
        )

    def _query_updated_auth_url(self, provider):
        while True:
            updated_auth_url = self.dialog.queryString(
                name='PROVIDER_AUTH_URL_{provider_id}'.format(
                    provider_id=provider['id'],
                ),
                note=_(
                    'Provider {} has an invalid authentication URL \'{}\'.\n'
                    'The authentication URL must have the following format: '
                    '"https://hostname:port/v3" or '
                    '"http://hostname:port/v3"\n'
                    'Enter new authentication URL or [Ctrl][c] to abort: '
                    .format(
                        provider['name'],
                        provider['auth_url']
                    )
                ),
                prompt=True,
            )
            if self._RE_VALID_AUTH_URL.match(updated_auth_url):
                return updated_auth_url
            self.dialog.note(text='The entered url is not valid, try again.')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
    )
    def _update_providers(self):
        for provider in self._providers_to_update:
            self._ensure_expected_auth_url(provider)
            self._update_provider(provider)

    def _update_provider(self, provider):
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement='UPDATE providers ' +
                      'SET auth_url = %(auth_url)s ' +
                      'WHERE id = %(id)s',
            args=dict(
                auth_url=provider.valid_auth_url,
                id=provider.id,
            ),
            ownConnection=True,
        )

    def _ensure_expected_auth_url(self, provider):
        auth_url = self._get_auth_url(provider)
        if auth_url != provider.invalid_auth_url:
            self._notify_provider_has_changed(provider, auth_url)

    def _get_auth_url(self, provider):
        res = self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement='SELECT auth_url ' +
                      'FROM providers ' +
                      'WHERE id = %(id)s',
            args=dict(
                id=provider.id,
            ),
            ownConnection=True,
        )
        if len(res) > 0:
            return res[0]['auth_url']

    def _notify_provider_has_changed(self, provider, auth_url):
        short_error_message = _(
            'Refused to update provider {provider_name}: '
            'Unexpected authentication URL.'
        ).format(provider_name=provider.name)
        long_error_message = _(
            'Refused to update provider {provider_name}. '
            'The authentication URL of has changed unexpectedly from '
            '"{invalid_auth_url}" to "{auth_url}".'
        ).format(
            provider_name=provider.name,
            invalid_auth_url=provider.invalid_auth_url,
            auth_url=auth_url,
        )

        self.dialog.note(
            text=long_error_message
        )
        raise RuntimeError(short_error_message)

# vim: expandtab tabstop=4 shiftwidth=4
