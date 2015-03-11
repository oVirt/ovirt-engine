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

package org.ovirt.engine.api.common.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VM;


public class QueryHelperTest extends Assert {

    private static final String QUERY = "name=zibert AND id=0*";
    private static final String MATRIX_CONSTRAINT = "cons";
    protected static final String[] PARAMS = {"00000000-0000-0000-0000-000000000000"};

    protected IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }

    @Test
    public void testGetVMConstraint() throws Exception {
        doTestGetConstraint(VM.class, "VMs : ");
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

        UriInfo uriInfo = createMock(UriInfo.class);
        MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
        List<String> queryParam = new ArrayList<String>();
        queryParam.add(QUERY);
        expect(queries.get("search")).andReturn(queryParam).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();

        replay(uriInfo, queries);

        if ("".equals(expectedPrefix)) {
            assertEquals(QUERY, QueryHelper.getConstraint(uriInfo, clz, false));
        } else {
            assertEquals(expectedPrefix + QUERY, QueryHelper.getConstraint(uriInfo, clz));
        }

        verify(uriInfo, queries);
    }

    @Test
    public void testGetVMConstraintNoPrefix() throws Exception {
        doTestGetConstraint(VM.class, "VMs : ");
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
        doTestGetDefaultConstraint(VM.class, "VMs : ");
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
        UriInfo uriInfo = createMock(UriInfo.class);
        MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
        List<String> queryParam = new ArrayList<String>();
        expect(queries.get("search")).andReturn(queryParam).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();

        replay(uriInfo, queries);

        assertEquals(expectedConstraint, QueryHelper.getConstraint(uriInfo, "", clz));

        verify(uriInfo, queries);
    }

    @Test
    public void testHasMatrixParam() throws Exception {
        UriInfo uriInfo = setUpMatrixParamExpectations();
        assertEquals(QueryHelper.hasMatrixParam(uriInfo, MATRIX_CONSTRAINT), true);
    }

    private UriInfo setUpMatrixParamExpectations() {

        UriInfo uriInfo = control.createMock(UriInfo.class);

        List<PathSegment> psl = new ArrayList<PathSegment>();

        PathSegment ps = control.createMock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = control.createMock(MultivaluedMap.class);
        expect(matrixParams.isEmpty()).andReturn(false);
        expect(matrixParams.containsKey(MATRIX_CONSTRAINT)).andReturn(true);
        expect(ps.getMatrixParameters()).andReturn(matrixParams);

        psl.add(ps);

        expect(uriInfo.getPathSegments()).andReturn(psl).anyTimes();

        control.replay();

        return uriInfo;
    }

    @Test
    public void testGetMatrixConstraints() throws Exception {
        UriInfo uriInfo = setUpGetMatrixConstraintsExpectations();
        HashMap<String, String> results = QueryHelper.getMatrixConstraints(uriInfo, MATRIX_CONSTRAINT);

        assertEquals(results.size(), 1);
        assertEquals(results.get(MATRIX_CONSTRAINT), PARAMS[0]);
    }

    private UriInfo setUpGetMatrixConstraintsExpectations() {
        UriInfo uriInfo = control.createMock(UriInfo.class);
        List<PathSegment> psl = new ArrayList<PathSegment>();

        PathSegment ps = control.createMock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = control.createMock(MultivaluedMap.class);

        expect(matrixParams.isEmpty()).andReturn(false);
        expect(matrixParams.containsKey(MATRIX_CONSTRAINT)).andReturn(true);

        List<String> matrixParamsList = control.createMock(List.class);
        expect(matrixParams.get(MATRIX_CONSTRAINT)).andReturn(matrixParamsList).anyTimes();

        expect(ps.getMatrixParameters()).andReturn(matrixParams).anyTimes();
        expect(matrixParamsList.size()).andReturn(1);
        expect(matrixParamsList.get(0)).andReturn(PARAMS[0]);

        psl.add(ps);

        expect(uriInfo.getPathSegments()).andReturn(psl).anyTimes();

        control.replay();

        return uriInfo;
    }
}
