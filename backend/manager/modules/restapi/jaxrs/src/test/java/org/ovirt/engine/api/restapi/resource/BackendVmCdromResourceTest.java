/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmCdromResourceTest
        extends AbstractBackendSubResourceTest<Cdrom, VM, BackendVmCdromResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid CDROM_ID = GUIDS[0];
    private static final String A_ISO = "a.iso";
    private static final String B_ISO = "b.iso";
    private static final String NO_ISO = "";

    public BackendVmCdromResourceTest() {
        super(new BackendVmCdromResource(CDROM_ID.toString(), VM_ID));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(getVm());

        Cdrom cdrom = resource.get();
        verifyModel(cdrom);
    }

    @Test
    public void testGetCurrent() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());

        Cdrom cdrom = resource.get();
        verifyModelWithCurrentCd(cdrom);
    }

    @Test
    public void testGetCurrentWithMatrixTrue() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current", "true");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());

        Cdrom cdrom = resource.get();
        verifyModelWithCurrentCd(cdrom);
    }

    @Test
    public void testGetCurrentWithMatrixFalse() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current", "false");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());

        Cdrom cdrom = resource.get();
        verifyModel(cdrom);
    }

    @Test
    public void testChangeCdNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getCdrom(B_ISO))));
    }

    @Test
    public void testEjectCd() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm(A_ISO, NO_ISO, VMStatus.Up));
        setUpActionExpectations(
            ActionType.ChangeDisk,
            ChangeDiskCommandParameters.class,
            new String[] { "CdImagePath" },
            new Object[] { NO_ISO },
            true,
            true
        );
        Cdrom cdrom = getCdrom(NO_ISO);
        cdrom = resource.update(cdrom);
        assertNull(cdrom.getFile());
    }

    @Test
    public void testChangeCdUsingQueryParameter() {
        resource.setUriInfo(setUpChangeCdUriQueryExpectations());
        setUpEntityQueryExpectations(getVm());
        setUpActionExpectations(
            ActionType.ChangeDisk,
            ChangeDiskCommandParameters.class,
            new String[] { "CdImagePath" },
            new Object[] {A_ISO},
            true,
            true
        );
        Cdrom cdrom = getCdrom(A_ISO);
        cdrom = resource.update(cdrom);
        assertTrue(cdrom.isSetFile());
    }

    @Test
    public void testChangeCdUsingMatrixParameter() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());
        setUpActionExpectations(
            ActionType.ChangeDisk,
            ChangeDiskCommandParameters.class,
            new String[] { "CdImagePath" },
            new Object[] {A_ISO},
            true,
            true
        );
        Cdrom cdrom = getCdrom(A_ISO);
        cdrom = resource.update(cdrom);
        assertTrue(cdrom.isSetFile());
    }

    protected UriInfo setUpChangeCdUriQueryExpectations() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        MultivaluedMap<String, String> queries = mock(MultivaluedMap.class);
        when(queries.containsKey("current")).thenReturn(true);
        when(queries.getFirst("current")).thenReturn("true");
        when(uriInfo.getQueryParameters()).thenReturn(queries);
        return uriInfo;
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        Cdrom cdrom = getCdrom(A_ISO);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(cdrom)));
    }

    @Test
    public void testUpdate() {
        setUpEntityQueryExpectations(getVm(A_ISO, A_ISO, VMStatus.Down));
        setUpEntityQueryExpectations(getVm(B_ISO, A_ISO, VMStatus.Down));
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] { "VmStaticData.IsoPath" },
                new Object[] { B_ISO },
                true,
                true
            )
        );
        Cdrom cdrom = getCdrom(B_ISO);
        cdrom = resource.update(cdrom);
        assertTrue(cdrom.isSetFile());
    }

    @Test
    public void testUpdateIncompleteParameters() {
        setUriInfo(setUpBasicUriExpectations());
        Cdrom cdrom = new Cdrom();
        cdrom.setFile(null);
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.update(cdrom)), "Cdrom", "update", "file");
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm()
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] { "VmStaticData.IsoPath" },
                new Object[] { null },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpEntityQueryExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            null
        );
        setUriInfo(setUpBasicUriExpectations());
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm()
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] { "VmStaticData.IsoPath" },
                new Object[] { null },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private Cdrom getCdrom(String path) {
        Cdrom cdrom = new Cdrom();
        cdrom.setFile(new File());
        cdrom.getFile().setId(path);
        return cdrom;
    }

    private VM getVm() {
        return getVm(A_ISO, B_ISO, VMStatus.Up);
    }

    private VM getVm(String savedIso, String currentIso, VMStatus status) {
        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setIsoPath(savedIso);
        vm.setCurrentCd(currentIso);
        vm.setStatus(status);
        return vm;
    }

    private void setUpEntityQueryExpectations(VM result) {
        setUpGetEntityExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            result
         );
    }

    private void verifyModel(Cdrom model) {
        verifyModelWithIso(model, A_ISO);
        verifyLinks(model);
    }

    private void verifyModelWithCurrentCd(Cdrom model) {
        verifyModelWithIso(model, B_ISO);
        verifyLinks(model);
    }

    private static void verifyModelWithIso(Cdrom model, String isoPath) {
        assertEquals(Guid.Empty.toString(), model.getId());
        assertTrue(model.isSetVm());
        assertEquals(VM_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetFile());
        assertEquals(isoPath, model.getFile().getId());
    }
}
