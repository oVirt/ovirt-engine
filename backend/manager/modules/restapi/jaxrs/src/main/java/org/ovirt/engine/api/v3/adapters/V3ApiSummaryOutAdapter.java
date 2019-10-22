/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.ApiSummaryItem;
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
            ApiSummaryItem fromHosts = from.getHosts();
            V3Hosts toHosts = new V3Hosts();
            if (fromHosts.isSetTotal()) {
                toHosts.setTotal(fromHosts.getTotal().longValue());
            }
            if (fromHosts.isSetActive()) {
                toHosts.setActive(fromHosts.getActive().longValue());
            }
            to.setHosts(toHosts);
        }
        if (from.isSetStorageDomains()) {
            ApiSummaryItem fromSds = from.getStorageDomains();
            V3StorageDomains toSds = new V3StorageDomains();
            if (fromSds.isSetTotal()) {
                toSds.setTotal(fromSds.getTotal().longValue());
            }
            if (fromSds.isSetActive()) {
                toSds.setActive(fromSds.getActive().longValue());
            }
            to.setStorageDomains(toSds);
        }
        if (from.isSetUsers()) {
            ApiSummaryItem fromUsers = from.getUsers();
            V3Users toUsers = new V3Users();
            if (fromUsers.isSetTotal()) {
                toUsers.setTotal(fromUsers.getTotal().longValue());
            }
            if (fromUsers.isSetActive()) {
                toUsers.setActive(fromUsers.getActive().longValue());
            }
            to.setUsers(toUsers);
        }
        if (from.isSetVms()) {
            ApiSummaryItem fromVms = from.getVms();
            V3VMs toVms = new V3VMs();
            if (fromVms.isSetTotal()) {
                toVms.setTotal(fromVms.getTotal().longValue());
            }
            if (fromVms.isSetActive()) {
                toVms.setActive(fromVms.getActive().longValue());
            }
            to.setVMs(toVms);
        }
        return to;
    }
}
