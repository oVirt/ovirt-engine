package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Applications;
import org.ovirt.engine.api.resource.VmApplicationResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmApplicationsResourceTest extends AbstractBackendResourceTest {

    BackendVmApplicationsResource resource;
    protected static final Guid VM_ID = GUIDS[1];

    public BackendVmApplicationsResourceTest() {
        resource = new BackendVmApplicationsResource(VM_ID);
    }

    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
    }

    @Override
    protected void init() {
        resource.setMappingLocator(mapperLocator);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Override
    public Applications getEntity(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Test
    public void testList() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{VM_ID},
                    getVm());
        control.replay();
        verifyApplications(resource.list());
    }

    private void verifyApplications(Applications applications) {
        assertNotNull(applications);
        assertEquals(NAMES.length, applications.getApplications().size());
        int index = 0;
        for (Application app : applications.getApplications()) {
            assertEquals(NAMES[index], app.getName());
            verifyLinks(app);
            index++;
        }
    }

    protected VM getVm() {
        return setUpEntityExpectations(control.createMock(VM.class),
                control.createMock(VmDynamic.class));
    }

    static VM setUpEntityExpectations(VM entity, VmDynamic dynamicVm) {
        expect(entity.getQueryableId()).andReturn(VM_ID).anyTimes();
        expect(entity.getDynamicData()).andReturn(dynamicVm).anyTimes();
        expect(entity.getAppList()).andReturn(getAppList()).anyTimes();
        return entity;
    }

    static String getAppList() {
        StringBuilder buf = new StringBuilder();
        for (String name : NAMES) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(name);
        }
        return buf.toString();
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        assertTrue(resource.getApplicationResource(VM_ID.toString()) instanceof VmApplicationResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            resource.getApplicationResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

}
