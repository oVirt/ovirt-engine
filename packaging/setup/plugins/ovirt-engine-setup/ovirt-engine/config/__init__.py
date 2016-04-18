#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2016 Red Hat, Inc.
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


"""ovirt-host-setup config plugin."""


from otopi import util


from . import aaa
from . import aaainternal
from . import aaajdbc
from . import aaakerbldap
from . import jboss
from . import java
from . import database
from . import dwh_database
from . import protocols
from . import appmode
from . import domain_type
from . import firewall
from . import ca
from . import options
from . import tools
from . import iso_domain
from . import public_glance_repository
from . import storage
from . import sso
from . import notifier


@util.export
def createPlugins(context):
    aaa.Plugin(context=context)
    aaainternal.Plugin(context=context)
    aaajdbc.Plugin(context=context)
    aaakerbldap.Plugin(context=context)
    jboss.Plugin(context=context)
    java.Plugin(context=context)
    database.Plugin(context=context)
    dwh_database.Plugin(context=context)
    protocols.Plugin(context=context)
    appmode.Plugin(context=context)
    domain_type.Plugin(context=context)
    firewall.Plugin(context=context)
    ca.Plugin(context=context)
    options.Plugin(context=context)
    tools.Plugin(context=context)
    iso_domain.Plugin(context=context)
    public_glance_repository.Plugin(context=context)
    storage.Plugin(context=context)
    sso.Plugin(context=context)
    notifier.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
