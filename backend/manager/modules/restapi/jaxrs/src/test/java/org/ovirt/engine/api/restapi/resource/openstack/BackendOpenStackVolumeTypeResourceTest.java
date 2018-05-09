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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackVolumeTypeResourceTest
        extends AbstractBackendSubResourceTest<OpenStackVolumeType, CinderVolumeType, BackendOpenStackVolumeTypeResource> {
    public BackendOpenStackVolumeTypeResourceTest() {
        super(new BackendOpenStackVolumeTypeResource(GUIDS[0].toString(), GUIDS[1].toString()));
    }

    @Test
    public void testBadId() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendOpenStackImageProviderResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        verifyModel(resource.get(), 1);
    }

    private List<StorageDomain> getStorageDomains() {
        StorageDomain storageDomain = mock(StorageDomain.class);
        when(storageDomain.getId()).thenReturn(GUIDS[0]);
        when(storageDomain.getStorage()).thenReturn(GUIDS[0].toString());
        return Collections.singletonList(storageDomain);
    }

    @Override
    protected CinderVolumeType getEntity(int index) {
        CinderVolumeType cinderVolumeType = mock(CinderVolumeType.class);
        when(cinderVolumeType.getId()).thenReturn(GUIDS[index].toString());
        when(cinderVolumeType.getName()).thenReturn(NAMES[index]);
        return cinderVolumeType;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
            QueryType.GetAllStorageDomains,
            QueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getStorageDomains()
        );
        setUpEntityQueryExpectations(
                QueryType.GetCinderVolumeTypesByStorageDomainId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                notFound ? Collections.emptyList() : getCinderVolumeTypes()
        );
    }

    @Override
    protected void verifyModel(OpenStackVolumeType model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }

    private List<CinderVolumeType> getCinderVolumeTypes() {
        List<CinderVolumeType> cinderVolumeTypes = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            cinderVolumeTypes.add(getEntity(i));
        }
        return cinderVolumeTypes;
    }
}
