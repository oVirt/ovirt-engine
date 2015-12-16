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
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeProviderResourceTest
        extends AbstractBackendSubResourceTest<OpenStackVolumeProvider, Provider, BackendOpenStackVolumeProviderResource> {
    public BackendOpenStackVolumeProviderResourceTest() {
        super(new BackendOpenStackVolumeProviderResource(GUIDS[0].toString(), new BackendOpenStackVolumeProvidersResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendOpenStackVolumeProviderResource("foo", resource.getParent());
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
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
        setUpGetEntityExpectations(1);
        control.replay();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUpGetEntityExpectationsOnDoPopulate(false);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        OpenStackVolumeProvider model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpActionExpectations(VdcActionType.RemoveProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                true,
                true);
        resource.remove();
    }

    private OpenStackVolumeProvider getModel(int index) {
        OpenStackVolumeProvider model = new OpenStackVolumeProvider();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected Provider getEntity(int index) {
        Provider provider = control.createMock(Provider.class);
        expect(provider.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(provider.getName()).andReturn(NAMES[index]).anyTimes();
        expect(provider.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return provider;
    }

    public StorageDomainStatic getStorageDomainStatic() {
        StorageDomainStatic storageDomainStatic = control.createMock(StorageDomainStatic.class);
        expect(storageDomainStatic.getId()).andReturn(GUIDS[0]).anyTimes();
        expect(storageDomainStatic.getName()).andReturn(NAMES[0]).anyTimes();
        return storageDomainStatic;
    }

    public List<StoragePool> getStoragePools() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(GUIDS[1]);
        return Collections.singletonList(storagePool);
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                VdcQueryType.GetProviderById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound? null: getEntity(0)
            );
        }
    }

    protected void setUpGetEntityExpectationsOnDoPopulate(boolean notFound) throws Exception {
        setUpGetEntityExpectations(
                VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                notFound ? null : getStorageDomainStatic());
        setUpGetEntityExpectations(
                VdcQueryType.GetStoragePoolsByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound ? null : getStoragePools());
    }
}
