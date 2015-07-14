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

import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackImageResourceTest
        extends AbstractBackendSubResourceTest<OpenStackImage, RepoImage, BackendOpenStackImageResource> {
    public BackendOpenStackImageResourceTest() {
        super(new BackendOpenStackImageResource(GUIDS[0].toString(), GUIDS[1].toString()));
    }

    @Test
    public void testBadId() throws Exception {
        control.replay();
        try {
            new BackendOpenStackImageProviderResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        control.replay();
        verifyModel(resource.get(), 1);
    }

    private List<StorageDomain> getStorageDomains() {
        StorageDomain storageDomain = control.createMock(StorageDomain.class);
        expect(storageDomain.getId()).andReturn(GUIDS[0]).anyTimes();
        expect(storageDomain.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return Collections.singletonList(storageDomain);
    }

    @Override
    protected RepoImage getEntity(int index) {
        RepoImage image = control.createMock(RepoImage.class);
        expect(image.getRepoImageId()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(image.getRepoImageName()).andReturn(NAMES[index]).anyTimes();
        return image;
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllStorageDomains,
            VdcQueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getStorageDomains()
        );
        setUpGetEntityExpectations(
            VdcQueryType.GetImageById,
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
