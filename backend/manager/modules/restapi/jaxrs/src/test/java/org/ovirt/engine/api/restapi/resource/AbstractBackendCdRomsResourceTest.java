package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;


import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;

@Ignore
public class AbstractBackendCdRomsResourceTest<T extends AbstractBackendReadOnlyDevicesResource<CdRom, CdRoms, VM>>
        extends AbstractBackendCollectionResourceTest<CdRom, VM, T> {

    protected final static Guid PARENT_ID = GUIDS[1];
    protected final static String ISO_PATH = "Fedora-13-x86_64-Live.iso";

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
        return setUpEntityExpectations(control.createMock(VM.class),
                                       control.createMock(VmStatic.class),
                                       index);
    }

    static VM setUpEntityExpectations(VM entity, VmStatic staticVm, int index) {
        return setUpEntityExpectations(entity, staticVm, null, index);
    }

    static VM setUpEntityExpectations(VM entity, VmStatic staticVm, VMStatus status, int index) {
        expect(entity.getQueryableId()).andReturn(PARENT_ID).anyTimes();
        expect(entity.getStaticData()).andReturn(staticVm).anyTimes();
        expect(staticVm.getiso_path()).andReturn(ISO_PATH).anyTimes();
        if (status != null) {
            expect(entity.getStatus()).andReturn(status).anyTimes();
        }
        return entity;
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

    protected void verifyModel(CdRom model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(CdRom model, int index) {
        assertEquals(Guid.Empty.toString(), model.getId());
        assertTrue(model.isSetVm());
        assertEquals(PARENT_ID.toString(), model.getVm().getId());
        assertTrue(model.isSetFile());
        assertEquals(ISO_PATH, model.getFile().getId());
    }
}
