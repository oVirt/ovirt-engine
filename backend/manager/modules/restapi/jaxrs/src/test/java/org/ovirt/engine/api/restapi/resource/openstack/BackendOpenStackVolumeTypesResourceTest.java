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

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeTypesResourceTest extends
        AbstractBackendCollectionResourceTest<OpenStackVolumeType, CinderVolumeType, BackendOpenStackVolumeTypesResource> {
    public BackendOpenStackVolumeTypesResourceTest() {
        super(
            new BackendOpenStackVolumeTypesResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<OpenStackVolumeType> getCollection() {
        return collection.list().getOpenStackVolumeTypes();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllStorageDomains,
            VdcQueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getStorageDomains()
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetCinderVolumeTypesByStorageDomainId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getCinderVolumeTypes(),
            failure
        );
        control.replay();
    }

    private List<StorageDomain> getStorageDomains() {
        StorageDomain storageDomain = control.createMock(StorageDomain.class);
        expect(storageDomain.getId()).andReturn(GUIDS[0]).anyTimes();
        expect(storageDomain.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return Collections.singletonList(storageDomain);
    }

    private List<CinderVolumeType> getCinderVolumeTypes() {
        List<CinderVolumeType> cinderVolumeTypes = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            cinderVolumeTypes.add(getEntity(i));
        }
        return cinderVolumeTypes;
    }

    @Override
    protected CinderVolumeType getEntity(int index) {
        CinderVolumeType cinderVolumeType = control.createMock(CinderVolumeType.class);
        expect(cinderVolumeType.getId()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(cinderVolumeType.getName()).andReturn(NAMES[index]).anyTimes();
        return cinderVolumeType;
    }

    @Override
    protected void verifyModel(OpenStackVolumeType model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
