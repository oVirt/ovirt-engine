/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;


public class QueryHelperTest extends Assert {

    private static final String QUERY = "name=zibert AND id=0*";

    @Test
    public void testGetVMConstraint() throws Exception {
        doTestGetConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraint() throws Exception {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraint() throws Exception {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraint() throws Exception {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraint() throws Exception {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraint() throws Exception {
        doTestGetConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetNetworkConstraint() throws Exception {
        doTestGetConstraint(Network.class, "Networks : ");
    }

    private void doTestGetConstraint(Class<?> clz, String expectedPrefix) throws Exception {
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
    public void testGetVMConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraintNoPrefix() throws Exception {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraintNoPrefix() throws Exception {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetVMDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Vm.class, "VMs : ");
    }

    @Test
    public void testGetHostDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetNetworkDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Network.class, "Networks : ");
    }

    private void doTestGetDefaultConstraint(Class<?> clz, String expectedConstraint) throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> queries = mock(MultivaluedMap.class);
        when(queries.isEmpty()).thenReturn(true);
        when(uriInfo.getQueryParameters()).thenReturn(queries);

        assertEquals(expectedConstraint, QueryHelper.getConstraint(null, uriInfo, "", clz));
    }
}
