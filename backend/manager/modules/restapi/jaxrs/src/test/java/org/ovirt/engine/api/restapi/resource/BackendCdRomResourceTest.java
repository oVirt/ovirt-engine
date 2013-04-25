package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendCdRomResourceTest
        extends AbstractBackendSubResourceTest<CdRom, VM, BackendDeviceResource<CdRom, CdRoms, VM>> {

    protected final static String ISO_PATH = "Fedora-13-x86_64-Live.iso";

    protected static BackendCdRomsResource collection = getCollection();

    public BackendCdRomResourceTest() {
        super(getResource(GUIDS[0]));
    }

    protected static BackendDeviceResource<CdRom, CdRoms, VM> getResource(Guid id) {
        return new BackendCdRomResource(CdRom.class,
                                        VM.class,
                                        id,
                                        collection,
                                        VdcActionType.UpdateVm,
                                        collection.getUpdateParametersProvider(),
                                        collection.getRequiredUpdateFields());
    }

    protected BackendDeviceResource<CdRom, CdRoms, VM> getNotFoundResource() {
        BackendDeviceResource<CdRom, CdRoms, VM> ret = getResource(new Guid("0d0264ef-40de-45a1-b746-83a0088b47a7"));
        ret.setUriInfo(setUpBasicUriExpectations());
        initResource(ret);
        initResource(ret.getCollection());
        return ret;
    }

    protected static BackendCdRomsResource getCollection() {
       return new BackendCdRomsResource(PARENT_ID,
                                        VdcQueryType.GetVmByVmId,
                                        new IdQueryParameters(PARENT_ID));
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        BackendDeviceResource<CdRom, CdRoms, VM> resource = getNotFoundResource();
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
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
        setUpEntityQueryExpectations(1);
        control.replay();

        CdRom cdrom = resource.get();
        verifyModelSpecific(cdrom, 1);
        verifyLinks(cdrom);
    }

    @Test
    public void testChangeCdNotFound() throws Exception {
        BackendDeviceResource<CdRom, CdRoms, VM> resource = getNotFoundResource();
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();
        try {
            resource.update(getUpdate());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testChangeCdIncompleteParameters() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());
        CdRom update = getUpdate();
        update.getFile().setId(null);
        control.replay();
        try {
            resource.update(update);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "CdRom", "update", "file.id");
        }
    }

    @Test
    public void testChangeCdUsingQueryParameter() throws Exception {
        resource.setUriInfo(setUpChangeCdUriQueryExpectations());
        setUpGetEntityExpectations(1, VMStatus.Up);
        setUpActionExpectations(VdcActionType.ChangeDisk,
                ChangeDiskCommandParameters.class,
                new String[] { "CdImagePath" },
                new Object[] { ISO_PATH },
                true,
                true);
        CdRom cdrom = resource.update(getUpdate());
        assertTrue(cdrom.isSetFile());
    }

    @Test
    public void testChangeCdUsingMatrixParameter() throws Exception {
        resource.setUriInfo(setUpChangeCdUriMatrixExpectations());
        setUpGetEntityExpectations(1, VMStatus.Up);
        setUpActionExpectations(VdcActionType.ChangeDisk,
                ChangeDiskCommandParameters.class,
                new String[] { "CdImagePath" },
                new Object[] { ISO_PATH },
                true,
                true);
        CdRom cdrom = resource.update(getUpdate());
        assertTrue(cdrom.isSetFile());
    }

    protected UriInfo setUpChangeCdUriQueryExpectations() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        MultivaluedMap<String, String> queries = control.createMock(MultivaluedMap.class);
        expect(queries.containsKey("current")).andReturn(true).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
        return uriInfo;
    }

    protected UriInfo setUpChangeCdUriMatrixExpectations() {
        UriInfo uriInfo = control.createMock(UriInfo.class);

        List<PathSegment> psl = new ArrayList<PathSegment>();

        PathSegment ps = control.createMock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = control.createMock(MultivaluedMap.class);
        expect(matrixParams.isEmpty()).andReturn(false);
        expect(matrixParams.containsKey("current")).andReturn(true);
        expect(ps.getMatrixParameters()).andReturn(matrixParams);

        psl.add(ps);

        expect(uriInfo.getPathSegments()).andReturn(psl).anyTimes();

        return uriInfo;
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        BackendDeviceResource<CdRom, CdRoms, VM> resource = getNotFoundResource();
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, VMStatus.Down);
        control.replay();
        try {
            resource.update(getUpdate());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(3, VMStatus.Down);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] { "VmStaticData.IsoPath" },
                                           new Object[] { ISO_PATH },
                                           true,
                                           true));

        CdRom cdrom = resource.update(getUpdate());
        assertTrue(cdrom.isSetFile());
    }

    @Test
    public void testUpdateIncompleteParameters() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        CdRom update = getUpdate();
        update.getFile().setId(null);
        control.replay();
        try {
            resource.update(update);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "CdRom", "update", "file.id");
        }
    }

    protected CdRom getUpdate() {
        CdRom update = new CdRom();
        update.setFile(new File());
        update.getFile().setId(ISO_PATH);
        return update;
    }

    @Override
    protected VM getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VM.class),
                                       control.createMock(VmStatic.class),
                                       index);
    }

    protected VM getEntity(int index, VMStatus status) {
        return setUpEntityExpectations(control.createMock(VM.class),
                control.createMock(VmStatic.class),
                status,
                index);
    }

    protected List<VM> getEntityList(VMStatus status) {
        List<VM> entities = new ArrayList<VM>();
        for (int i = 0; i < NAMES.length; i++) {
            if (status != null) {
                entities.add(getEntity(i, status));
            } else {
                entities.add(getEntity(i));
            }
        }
        return entities;

    }

    protected List<VM> getEntityList() {
        return getEntityList(null);

    }

    protected void setUpEntityQueryExpectations(int times, VMStatus status) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getEntityList(status));
        }
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, null);
    }

    protected void setUpGetEntityExpectations(int times, VMStatus status) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { PARENT_ID },
                                       status !=null ? getEntity(0, status)
                                                       :
                                                       getEntity(0));
        }
    }

}
