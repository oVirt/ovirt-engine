/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ApiSummary;
import org.ovirt.engine.api.v3.types.V3Hosts;
import org.ovirt.engine.api.v3.types.V3StorageDomains;
import org.ovirt.engine.api.v3.types.V3Users;
import org.ovirt.engine.api.v3.types.V3VMs;

public class V3ApiSummaryOutAdapter implements V3Adapter<ApiSummary, V3ApiSummary> {
    @Override
    public V3ApiSummary adapt(ApiSummary from) {
        V3ApiSummary to = new V3ApiSummary();
        if (from.isSetHosts()) {
            Hosts fromHosts = from.getHosts();
            V3Hosts toHosts = new V3Hosts();
            if (fromHosts.isSetTotal()) {
                toHosts.setTotal(fromHosts.getTotal());
            }
            if (fromHosts.isSetActive()) {
                toHosts.setActive(fromHosts.getActive());
            }
            to.setHosts(toHosts);
        }
        if (from.isSetStorageDomains()) {
            StorageDomains fromSds = from.getStorageDomains();
            V3StorageDomains toSds = new V3StorageDomains();
            if (fromSds.isSetTotal()) {
                toSds.setTotal(fromSds.getTotal());
            }
            if (fromSds.isSetActive()) {
                toSds.setActive(fromSds.getActive());
            }
            to.setStorageDomains(toSds);
        }
        if (from.isSetUsers()) {
            Users fromUsers = from.getUsers();
            V3Users toUsers = new V3Users();
            if (fromUsers.isSetTotal()) {
                toUsers.setTotal(fromUsers.getTotal());
            }
            if (fromUsers.isSetActive()) {
                toUsers.setActive(fromUsers.getActive());
            }
            to.setUsers(toUsers);
        }
        if (from.isSetVms()) {
            Vms fromVms = from.getVms();
            V3VMs toVms = new V3VMs();
            if (fromVms.isSetTotal()) {
                toVms.setTotal(fromVms.getTotal());
            }
            if (fromVms.isSetActive()) {
                toVms.setActive(fromVms.getActive());
            }
            to.setVMs(toVms);
        }
        return to;
    }
}
