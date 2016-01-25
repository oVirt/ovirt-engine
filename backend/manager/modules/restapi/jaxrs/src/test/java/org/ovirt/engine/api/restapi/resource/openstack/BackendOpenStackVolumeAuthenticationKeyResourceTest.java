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

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeyUsageType;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeAuthenticationKeyResourceTest
        extends AbstractBackendSubResourceTest<OpenstackVolumeAuthenticationKey, LibvirtSecret, BackendOpenStackVolumeAuthenticationKeyResource> {
    public BackendOpenStackVolumeAuthenticationKeyResourceTest() {
        super(new BackendOpenStackVolumeAuthenticationKeyResource(GUIDS[0].toString(), GUIDS[1].toString()));
    }

    @Test
    public void testBadId() throws Exception {
        control.replay();
        try {
            new BackendOpenStackImageProviderResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
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
        } catch (WebApplicationException wae) {
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

    @Override
    protected LibvirtSecret getEntity(int index) {
        LibvirtSecret libvirtSecret = control.createMock(LibvirtSecret.class);
        expect(libvirtSecret.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(libvirtSecret.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(libvirtSecret.getProviderId()).andReturn(GUIDS[0]).anyTimes();
        expect(libvirtSecret.getUsageType()).andReturn(LibvirtSecretUsageType.CEPH).anyTimes();
        return libvirtSecret;
    }

    private OpenstackVolumeAuthenticationKey getModel(int index) {
        OpenstackVolumeAuthenticationKey model = new OpenstackVolumeAuthenticationKey();
        model.setId(GUIDS[index].toString());
        model.setDescription(DESCRIPTIONS[index]);
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(GUIDS[0].toString());
        model.setOpenstackVolumeProvider(provider);
        model.setUsageType(OpenstackVolumeAuthenticationKeyUsageType.CEPH);
        return model;
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpEntityQueryExpectations(
                VdcQueryType.GetLibvirtSecretById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                notFound ? null : getEntity(1));
    }

    @Override
    protected void verifyModel(OpenstackVolumeAuthenticationKey model, int index) {
        assertEquals(GUIDS[index], GuidUtils.asGuid(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(1));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2, false);
        setUriInfo(setUpActionExpectations(
                VdcActionType.UpdateLibvirtSecret,
                LibvirtSecretParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        verifyModel(resource.update(getModel(1)), 1);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                    VdcQueryType.GetLibvirtSecretById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[1] },
                    notFound ? null : getEntity(1));
        }
    }
}
