/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
                QueryType.GetAllLibvirtSecretsByProviderId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getLibvirtSecrets(),
                failure);
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
        LibvirtSecret libvirtSecret = mock(LibvirtSecret.class);
        when(libvirtSecret.getId()).thenReturn(GUIDS[index]);
        when(libvirtSecret.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(libvirtSecret.getProviderId()).thenReturn(GUIDS[0]);
        when(libvirtSecret.getUsageType()).thenReturn(LibvirtSecretUsageType.CEPH);
        return libvirtSecret;
    }

    @Override
    protected void verifyModel(OpenstackVolumeAuthenticationKey model, int index) {
        assertEquals(GUIDS[index], GuidUtils.asGuid(model.getId()));
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
