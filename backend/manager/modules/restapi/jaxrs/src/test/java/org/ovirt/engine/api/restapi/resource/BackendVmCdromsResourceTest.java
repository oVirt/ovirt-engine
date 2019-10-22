/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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

    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }

    private void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    private void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetVmByVmId,
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
    public void testAddCdRom() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm(),
            1
        );
        setUpCreationExpectations(
            ActionType.UpdateVm,
            VmManagementParametersBase.class,
            new String[] {},
            new Object[] {},
            true,
            true,
            null,
            QueryType.GetVmByVmId,
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
    public void testAddCdRomCantDo() {
        doTestBadAddCdRom(false, true, CANT_DO);
    }

    @Test
    public void testAddCdRomFailure() {
        doTestBadAddCdRom(true, false, FAILURE);
    }

    private void doTestBadAddCdRom(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(
            QueryType.GetVmByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getVm(),
            1
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                valid,
                success
            )
        );
        Cdrom cdrom = getCdrom();
        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(cdrom)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        Cdrom model = new Cdrom();
        model.setName(NAMES[0]);
        model.setFile(new File());
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Cdrom", "add", "file.id");
    }


    @Test
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getCdromResource("foo")));
    }

    private void setUpEntityQueryExpectations(
            QueryType query,
            Class<? extends QueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn);
        }
    }

    @Override
    protected void verifyCollection(List<Cdrom> cdroms) {
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
