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
import java.util.List;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeAuthenticationKeysResourceTest extends
        AbstractBackendCollectionResourceTest<OpenstackVolumeAuthenticationKey, LibvirtSecret, BackendOpenStackVolumeAuthenticationKeysResource> {
    public BackendOpenStackVolumeAuthenticationKeysResourceTest() {
        super(new BackendOpenStackVolumeAuthenticationKeysResource(GUIDS[0].toString()), null, "");
    }

    @Override
    protected List<OpenstackVolumeAuthenticationKey> getCollection() {
        return collection.list().getOpenstackVolumeAuthenticationKeys();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
                VdcQueryType.GetAllLibvirtSecretsByProviderId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getLibvirtSecrets(),
                failure);
        control.replay();
    }

    private List<LibvirtSecret> getLibvirtSecrets() {
        List<LibvirtSecret> libvirtSecrets = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            libvirtSecrets.add(getEntity(i));
        }
        return libvirtSecrets;
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

    @Override
    protected void verifyModel(OpenstackVolumeAuthenticationKey model, int index) {
        assertEquals(GUIDS[index], GuidUtils.asGuid(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
