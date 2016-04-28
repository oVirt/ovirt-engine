/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
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
        setUpEntityQueryExpectations(getVm());
        control.replay();

        Cdrom cdrom = resource.get();
        verifyModel(cdrom);
    }

    @Test
    public void testGetCurrent() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());
        control.replay();

        Cdrom cdrom = resource.get();
        verifyModelWithCurrentCd(cdrom);
    }

    @Test
    public void testGetCurrentWithMatrixTrue() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current", "true");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());
        control.replay();

        Cdrom cdrom = resource.get();
        verifyModelWithCurrentCd(cdrom);
    }

    @Test
    public void testGetCurrentWithMatrixFalse() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current", "false");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());
        control.replay();

        Cdrom cdrom = resource.get();
        verifyModel(cdrom);
    }

    @Test
    public void testChangeCdNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        control.replay();
        try {
            resource.update(getCdrom(B_ISO));
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testEjectCd() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm(A_ISO, NO_ISO, VMStatus.Up));
        setUpActionExpectations(
            VdcActionType.ChangeDisk,
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
    public void testChangeCdUsingQueryParameter() throws Exception {
        resource.setUriInfo(setUpChangeCdUriQueryExpectations());
        setUpEntityQueryExpectations(getVm());
        setUpActionExpectations(
            VdcActionType.ChangeDisk,
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
    public void testChangeCdUsingMatrixParameter() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, "current");
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(getVm());
        setUpActionExpectations(
            VdcActionType.ChangeDisk,
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
        MultivaluedMap<String, String> queries = control.createMock(MultivaluedMap.class);
        expect(queries.containsKey("current")).andReturn(true).anyTimes();
        expect(queries.getFirst("current")).andReturn("true").anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
        return uriInfo;
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(null);
        control.replay();
        try {
            Cdrom cdrom = getCdrom(A_ISO);
            resource.update(cdrom);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(getVm(A_ISO, A_ISO, VMStatus.Down));
        setUpEntityQueryExpectations(getVm(B_ISO, A_ISO, VMStatus.Down));
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVm,
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
    public void testUpdateIncompleteParameters() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Cdrom cdrom = new Cdrom();
        cdrom.setFile(null);
        control.replay();
        try {
            resource.update(cdrom);
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Cdrom", "update", "file");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm()
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVm,
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
    public void testRemoveNonExistant() throws Exception{
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            null
        );
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm()
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] { "VmStaticData.IsoPath" },
                new Object[] { null },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

    private void setUpEntityQueryExpectations(VM result) throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetVmByVmId,
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
