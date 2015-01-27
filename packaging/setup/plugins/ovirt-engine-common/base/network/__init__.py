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


"""ovirt-host-setup network plugin."""

from otopi import util
from . import firewall_manager
from . import firewall_manager_firewalld
from . import firewall_manager_human
from . import firewall_manager_iptables
from . import hostname


@util.export
def createPlugins(context):
    firewall_manager.Plugin(context=context)
    firewall_manager_firewalld.Plugin(context=context)
    firewall_manager_human.Plugin(context=context)
    firewall_manager_iptables.Plugin(context=context)
    hostname.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
