package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

@Ignore
public class AbstractBackendCdRomsResourceTest<T extends AbstractBackendReadOnlyDevicesResource<CdRom, CdRoms, VM>>
        extends AbstractBackendCollectionResourceTest<CdRom, VM, T> {

    protected final static Guid PARENT_ID = GUIDS[1];
    protected final static String ISO_PATH = "Fedora-13-x86_64-Live.iso";
    protected final static String CURRENT_ISO_PATH = "Fedora-20-x86_64-Live.iso";

    protected VdcQueryType queryType;
    protected VdcQueryParametersBase queryParams;
    protected String queryIdName;

    public AbstractBackendCdRomsResourceTest(T collection,
                                             VdcQueryType queryType,
                                             VdcQueryParametersBase queryParams,
                                             String queryIdName) {
        super(collection, null, "");
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.queryIdName = queryIdName;
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class as searching
        // over CdRoms is unsupported by the backend
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(queryType,
                                         queryParams.getClass(),
                                         new String[] { queryIdName },
                                         new Object[] { PARENT_ID },
                                         getEntityList(),
                                         failure);
        }
    }

    protected List<VM> getEntityList() {
        List<VM> entities = new ArrayList<VM>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected VM getEntity(int index) {
        return setUpEntityExpectations();
    }

    static VM setUpEntityExpectations() {
        return setUpEntityExpectations(null);
    }

    static VM setUpEntityExpectations(VMStatus status) {
        VM vm = new VM();
        vm.setId(PARENT_ID);
        vm.setIsoPath(ISO_PATH);
        vm.setCurrentCd(CURRENT_ISO_PATH);
        vm.setStatus(status);

        return vm;
    }

    protected List<CdRom> getCollection() {
        return collection.list().getCdRoms();
    }

    static CdRom getModel(int index) {
        CdRom model = new CdRom();
        model.setFile(new File());
        model.getFile().setId(ISO_PATH);
        return model;
    }

    static CdRom getModelWithCurrentCd() {
        CdRom model = new CdRom();
        model.setFile(new File());
        model.getFile().setId(CURRENT_ISO_PATH);
        return model;
    }

    protected void verifyModel(CdRom model, int index) {
        verifyModelWithIso(model, ISO_PATH);
        verifyLinks(model);
    }

    static void verifyModelSpecific(CdRom model, int index) {
        verifyModelWithIso(model, ISO_PATH);
    }

    static void verifyModelWithIso(CdRom model, String isoPath) {
        assertEquals(Guid.Empty.toString(), model.getId());
        assertTrue(model.isSetVm());
        assertEquals(PARENT_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetFile());
        assertEquals(isoPath, model.getFile().getId());
    }
}
