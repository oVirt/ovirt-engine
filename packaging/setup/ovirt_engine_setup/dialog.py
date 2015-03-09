#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


"""Dialog."""


import gettext

from otopi import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
def queryBoolean(
    dialog,
    name,
    note,
    prompt,
    true=_('Yes'),
    false=_('No'),
    default=False,
):
    return dialog.queryString(
        name=name,
        note=note,
        prompt=prompt,
        validValues=(true, false),
        caseSensitive=False,
        default=true if default else false,
    ) != false.lower()


# vim: expandtab tabstop=4 shiftwidth=4
