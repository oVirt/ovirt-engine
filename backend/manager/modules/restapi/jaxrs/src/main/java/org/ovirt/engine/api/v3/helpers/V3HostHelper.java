/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.helpers;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.types.V3Host;

/**
 * This class is a collection of static methods useful when working with hosts.
 */
public class V3HostHelper {
    /**
     * Retrieves host statistics from version 4 of the API and adds them to a version 4 host.
     *
     * @param host the host where the statistics should be added, should contain at least the 'id' attribute
     */
    public static void addStatistics(V3Host host) {
        Statistics statistics = BackendApiResource.getInstance()
            .getHostsResource()
            .getHostResource(host.getId())
            .getStatisticsResource()
            .list();
        host.setStatistics(adaptOut(statistics));
    }
}
