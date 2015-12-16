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

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCdromsResourceTest
        extends AbstractBackendCollectionResourceTest<Cdrom, VM, BackendVmCdromsResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final String ISO_PATH = "Fedora-13-x86_64-Live.iso";
    private static final String CURRENT_ISO_PATH = "Fedora-14-x86_64-Live.iso";

    public BackendVmCdromsResourceTest() {
        super(new BackendVmCdromsResource(VM_ID), null, null);
    }

    @Override
    protected List<Cdrom> getCollection() {
        return collection.list().getCdroms();
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    private void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    private void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getVm(),
                failure
            );
        }
    }

    protected VM getVm() {
        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setIsoPath(ISO_PATH);
        vm.setCurrentCd(CURRENT_ISO_PATH);
        return vm;
    }

    private Cdrom getCdrom() {
        Cdrom model = new Cdrom();
        model.setFile(new File());
        model.getFile().setId(ISO_PATH);
        return model;
    }

    @Test
    public void testAddCdRom() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm(),
            1
        );
        setUpCreationExpectations(
            VdcActionType.UpdateVm,
            VmManagementParametersBase.class,
            new String[] {},
            new Object[] {},
            true,
            true,
            null,
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm()
        );
        Cdrom cdrom = getCdrom();
        Response response = collection.add(cdrom);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Cdrom);
        verifyModel((Cdrom) response.getEntity());
    }

    @Test
    public void testAddCdRomCantDo() throws Exception {
        doTestBadAddCdRom(false, true, CANT_DO);
    }

    @Test
    public void testAddCdRomFailure() throws Exception {
        doTestBadAddCdRom(true, false, FAILURE);
    }

    private void doTestBadAddCdRom(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm(),
            1
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                valid,
                success
            )
        );
        Cdrom cdrom = getCdrom();
        try {
            collection.add(cdrom);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Cdrom model = new Cdrom();
        model.setName(NAMES[0]);
        model.setFile(new File());
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Cdrom", "add", "file.id");
        }
    }


    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getCdromResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    private void setUpEntityQueryExpectations(
            VdcQueryType query,
            Class<? extends VdcQueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn);
        }
    }

    @Override
    protected void verifyCollection(List<Cdrom> cdroms) throws Exception {
        assertNotNull(cdroms);
        assertEquals(1, cdroms.size());
        verifyModel(cdroms.get(0));
    }

    private void verifyModel(Cdrom model) {
        verifyModelWithIso(model, ISO_PATH);
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
