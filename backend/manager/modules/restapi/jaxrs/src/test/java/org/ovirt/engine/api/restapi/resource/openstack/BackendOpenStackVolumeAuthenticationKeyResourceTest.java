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

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeyUsageType;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendOpenStackVolumeAuthenticationKeyResourceTest
        extends AbstractBackendSubResourceTest<OpenstackVolumeAuthenticationKey, LibvirtSecret, BackendOpenStackVolumeAuthenticationKeyResource> {
    public BackendOpenStackVolumeAuthenticationKeyResourceTest() {
        super(new BackendOpenStackVolumeAuthenticationKeyResource(GUIDS[0].toString(), GUIDS[1].toString()));
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

    @Override
    protected LibvirtSecret getEntity(int index) {
        LibvirtSecret libvirtSecret = mock(LibvirtSecret.class);
        when(libvirtSecret.getId()).thenReturn(GUIDS[index]);
        when(libvirtSecret.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(libvirtSecret.getProviderId()).thenReturn(GUIDS[0]);
        when(libvirtSecret.getUsageType()).thenReturn(LibvirtSecretUsageType.CEPH);
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

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
                QueryType.GetLibvirtSecretById,
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
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(1))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2, false);
        setUriInfo(setUpActionExpectations(
                ActionType.UpdateLibvirtSecret,
                LibvirtSecretParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        verifyModel(resource.update(getModel(1)), 1);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                    QueryType.GetLibvirtSecretById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[1] },
                    notFound ? null : getEntity(1));
        }
    }
}
