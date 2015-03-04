#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


import gettext

from otopi import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


DB_KEY_RUNNING = 'DwhCurrentlyRunning'
DB_KEY_HOSTNAME = 'dwhHostname'
DB_KEY_UUID = 'dwhUuid'


@util.export
def getValueFromTimekeeping(statement, name, raise_if_empty=False):
    result = statement.execute(
        statement="""
            select * from GetDwhHistoryTimekeepingByVarName(
                %(name)s
            )
        """,
        args=dict(
            name=name,
        ),
        ownConnection=True,
    )
    if not result and raise_if_empty:
        raise RuntimeError(
            _(
                'Missing row {name} in the table dwh_history_timekeeping '
                'in the engine database'
            ).format(
                name=name,
            )
        )
    return result[0]['var_value'] if result else None


def updateValueInTimekeeping(statement, name, value):
    getValueFromTimekeeping(statement, name, raise_if_empty=True)
    statement.execute(
        statement="""
            select UpdateDwhHistoryTimekeeping(
                %(name)s,
                %(value)s,
                NULL
            )
        """,
        args=dict(
            name=name,
            value=value,
        ),
        ownConnection=False,
    )


def dwhIsUp(statement):
    return getValueFromTimekeeping(
        statement,
        name=DB_KEY_RUNNING
    ) == '1'


# vim: expandtab tabstop=4 shiftwidth=4
