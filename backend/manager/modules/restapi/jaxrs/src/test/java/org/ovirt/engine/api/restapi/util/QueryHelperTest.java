/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;


public class QueryHelperTest {

    private static final String QUERY = "name=zibert AND id=0*";

    @Test
    public void testGetVMConstraint() {
        doTestGetConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraint() {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraint() {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraint() {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraint() {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraint() {
        doTestGetConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetNetworkConstraint() {
        doTestGetConstraint(Network.class, "Networks : ");
    }

    private void doTestGetConstraint(Class<?> clz, String expectedPrefix) {
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> queries = mock(MultivaluedMap.class);
        when(queries.containsKey("search")).thenReturn(true);
        when(queries.getFirst("search")).thenReturn(QUERY);
        when(queries.isEmpty()).thenReturn(false);
        when(uriInfo.getQueryParameters()).thenReturn(queries);

        if ("".equals(expectedPrefix)) {
            assertEquals(QUERY, QueryHelper.getConstraint(null, uriInfo, clz, false));
        } else {
            assertEquals(expectedPrefix + QUERY, QueryHelper.getConstraint(null, uriInfo, clz));
        }
    }

    @Test
    public void testGetVMConstraintNoPrefix() {
        doTestGetConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraintNoPrefix() {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraintNoPrefix() {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraintNoPrefix() {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraintNoPrefix() {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraintNoPrefix() {
        doTestGetConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetVMDefaultConstraint() {
        doTestGetDefaultConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostDefaultConstraint() {
        doTestGetDefaultConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterDefaultConstraint() {
        doTestGetDefaultConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterDefaultConstraint() {
        doTestGetDefaultConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageDefaultConstraint() {
        doTestGetDefaultConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateDefaultConstraint() {
        doTestGetDefaultConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetNetworkDefaultConstraint() {
        doTestGetDefaultConstraint(Network.class, "Networks : ");
    }

    private void doTestGetDefaultConstraint(Class<?> clz, String expectedConstraint) {
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> queries = mock(MultivaluedMap.class);
        when(queries.isEmpty()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(queries);

        assertEquals(expectedConstraint, QueryHelper.getConstraint(null, uriInfo, "", clz));
    }
}
