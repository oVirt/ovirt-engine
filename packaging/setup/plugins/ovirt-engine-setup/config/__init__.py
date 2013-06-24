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


"""ovirt-host-setup config plugin."""


from otopi import util


from . import java
from . import jboss
from . import database
from . import protocols
from . import hostname
from . import appmode
from . import domain_type
from . import ca
from . import options
from . import tools
from . import iso_domain
from . import macrange
from . import websocket_proxy


@util.export
def createPlugins(context):
    java.Plugin(context=context)
    jboss.Plugin(context=context)
    database.Plugin(context=context)
    protocols.Plugin(context=context)
    hostname.Plugin(context=context)
    appmode.Plugin(context=context)
    domain_type.Plugin(context=context)
    ca.Plugin(context=context)
    options.Plugin(context=context)
    tools.Plugin(context=context)
    iso_domain.Plugin(context=context)
    macrange.Plugin(context=context)
    websocket_proxy.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
