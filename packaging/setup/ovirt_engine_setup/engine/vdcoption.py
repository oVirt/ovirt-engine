#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext

from otopi import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class VdcOption():

    def __init__(
        self,
        statement,
    ):
        self._statement = statement

    def getVdcOptionVersions(
        self,
        name,
        type=str,
        ownConnection=False,
    ):
        result = self._statement.execute(
            statement="""
                select version, option_value
                from vdc_options
                where option_name = %(name)s
            """,
            args=dict(
                name=name,
            ),
            ownConnection=ownConnection,
        )
        if len(result) == 0:
            raise RuntimeError(
                _('Cannot locate application option {name}').format(
                    name=name,
                )
            )

        return dict([
            (
                r['version'],
                (
                    r['option_value']
                    if type != bool
                    else r['option_value'].lower() not in ('false', '0')
                )
            ) for r in result
        ])

    def getVdcOption(
        self,
        name,
        version='general',
        type=str,
        ownConnection=False,
    ):
        return self.getVdcOptionVersions(
            name=name,
            type=type,
            ownConnection=ownConnection,
        )[version]

    def updateVdcOptions(
        self,
        options,
        ownConnection=False,
    ):
        for option in options:
            name = option['name']
            value = option['value']
            version = option.get('version', 'general')

            if option.get('encrypt', False):
                # AFAICT there aren't anymore users of this function that
                # ask to encrypt. The only ones I know of were:
                # AdminPassword - the engine admin password
                # LocalAdminPassword - for Windows Guests admin password
                # in plugins/ovirt-engine-setup/ovirt-engine/config/options.py
                # Both removed 5 years ago.
                raise RuntimeError(_(
                    'encrypting vdc options is not supported'
                ))

            if isinstance(value, bool):
                value = 'true' if value else 'false'

            res = self._statement.execute(
                statement="""
                    select count(*) as count
                    from vdc_options
                    where
                        option_name=%(name)s and
                        version=%(version)s
                """,
                args=dict(
                    name=name,
                    version=version,
                ),
                ownConnection=ownConnection,
            )
            if res[0]['count'] == 0:
                self._statement.execute(
                    statement="""
                        select fn_db_add_config_value (
                            %(name)s,
                            %(value)s,
                            %(version)s
                        )
                    """,
                    args=dict(
                        name=name,
                        version=version,
                        value=value,
                    ),
                    ownConnection=ownConnection,
                )
            else:
                self._statement.execute(
                    statement="""
                        select fn_db_update_config_value (
                            %(name)s,
                            %(value)s,
                            %(version)s
                        )
                    """,
                    args=dict(
                        name=name,
                        version=version,
                        value=value,
                    ),
                    ownConnection=ownConnection,
                )


# vim: expandtab tabstop=4 shiftwidth=4
