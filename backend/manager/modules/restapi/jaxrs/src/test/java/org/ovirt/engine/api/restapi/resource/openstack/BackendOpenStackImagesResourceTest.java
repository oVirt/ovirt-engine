/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackImagesResourceTest extends
        AbstractBackendCollectionResourceTest<OpenStackImage, RepoImage, BackendOpenStackImagesResource> {
    public BackendOpenStackImagesResourceTest() {
        super(
            new BackendOpenStackImagesResource(GUIDS[0].toString()),
            null,
            ""
        );
    }

    @Override
    protected List<OpenStackImage> getCollection() {
        return collection.list().getOpenStackImages();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
            QueryType.GetAllStorageDomains,
            QueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getStorageDomains()
        );
        setUpEntityQueryExpectations(
            QueryType.GetImagesList,
            GetImagesListParameters.class,
            new String[] { "StorageDomainId" },
            new Object[] { GUIDS[0] },
            getImages(),
            failure
        );
    }

    private List<StorageDomain> getStorageDomains() {
        StorageDomain storageDomain = mock(StorageDomain.class);
        when(storageDomain.getId()).thenReturn(GUIDS[0]);
        when(storageDomain.getStorage()).thenReturn(GUIDS[0].toString());
        return Collections.singletonList(storageDomain);
    }

    private List<RepoImage> getImages() {
        List<RepoImage> images = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            images.add(getEntity(i));
        }
        return images;
    }

    @Override
    protected RepoImage getEntity(int index) {
        RepoImage image = mock(RepoImage.class);
        when(image.getRepoImageId()).thenReturn(GUIDS[index].toString());
        when(image.getRepoImageName()).thenReturn(NAMES[index]);
        return image;
    }

    @Override
    protected void verifyModel(OpenStackImage model, int index) {
        assertEquals(NAMES[index], model.getName());
        verifyLinks(model);
    }
}
