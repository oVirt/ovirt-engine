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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class,
                () -> new BackendOpenStackVolumeProviderResource("foo", resource.getParent()))
        );
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUpGetEntityExpectationsOnDoPopulate(false);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateProvider,
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
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateProvider,
                ProviderParameters.class,
                new String[] { "Provider.Id" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        OpenStackVolumeProvider model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpActionExpectations(ActionType.RemoveProvider,
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
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(GUIDS[index]);
        when(provider.getName()).thenReturn(NAMES[index]);
        when(provider.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return provider;
    }

    public StorageDomainStatic getStorageDomainStatic() {
        StorageDomainStatic storageDomainStatic = mock(StorageDomainStatic.class);
        when(storageDomainStatic.getId()).thenReturn(GUIDS[0]);
        when(storageDomainStatic.getName()).thenReturn(NAMES[0]);
        return storageDomainStatic;
    }

    public List<StoragePool> getStoragePools() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(GUIDS[1]);
        return Collections.singletonList(storagePool);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                QueryType.GetProviderById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound? null: getEntity(0)
            );
        }
    }

    protected void setUpGetEntityExpectationsOnDoPopulate(boolean notFound) {
        setUpGetEntityExpectations(
                QueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                notFound ? null : getStorageDomainStatic());
        setUpGetEntityExpectations(
                QueryType.GetStoragePoolsByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                notFound ? null : getStoragePools());
    }
}
