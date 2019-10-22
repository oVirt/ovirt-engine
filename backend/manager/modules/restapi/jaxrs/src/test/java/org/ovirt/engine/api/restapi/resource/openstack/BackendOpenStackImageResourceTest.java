/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
