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

import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackImageResourceTest
        extends AbstractBackendSubResourceTest<OpenStackImage, RepoImage, BackendOpenStackImageResource> {
    public BackendOpenStackImageResourceTest() {
        super(new BackendOpenStackImageResource(GUIDS[0].toString(), GUIDS[1].toString()));
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
    protected RepoImage getEntity(int index) {
        RepoImage image = mock(RepoImage.class);
        when(image.getRepoImageId()).thenReturn(GUIDS[index].toString());
        when(image.getRepoImageName()).thenReturn(NAMES[index]);
        return image;
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
            QueryType.GetAllStorageDomains,
            QueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getStorageDomains()
        );
        setUpGetEntityExpectations(
            QueryType.GetImageById,
            GetImageByIdParameters.class,
            new String[] { "RepoImageId" },
            new Object[] { GUIDS[1].toString() },
            notFound? null: getEntity(1)
        );
    }

    @Override
    protected void verifyModel(OpenStackImage model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
