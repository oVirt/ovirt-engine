/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalHostsResourceTest
        extends AbstractBackendCollectionResourceTest<
            ExternalHost,
            VDS,
        BackendExternalHostsResource
        > {
    public BackendExternalHostsResourceTest() {
        super(
            new BackendExternalHostsResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<ExternalHost> getCollection() {
        return collection.list().getExternalHosts();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetHostListFromExternalProvider,
            GetHostListFromExternalProviderParameters.class,
            new String[] { "ProviderId" },
            new Object[] { GUIDS[0] },
            getHosts(),
            failure
        );
        control.replay();
    }

    private List<VDS> getHosts() {
        List<VDS> hosts = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            hosts.add(getEntity(i));
        }
        return hosts;
    }

    @Override
    protected VDS getEntity(int index) {
        VDS host = control.createMock(VDS.class);
        expect(host.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(host.getName()).andReturn(NAMES[index]).anyTimes();
        return host;
    }

    @Override
    protected void verifyModel(ExternalHost model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
