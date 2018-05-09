package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Applications;
import org.ovirt.engine.api.resource.VmApplicationResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testList() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetVmByVmId,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{VM_ID},
                    getVm());
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
        return setUpEntityExpectations(mock(VM.class), mock(VmDynamic.class));
    }

    static VM setUpEntityExpectations(VM entity, VmDynamic dynamicVm) {
        when(entity.getQueryableId()).thenReturn(VM_ID);
        when(entity.getDynamicData()).thenReturn(dynamicVm);
        when(entity.getAppList()).thenReturn(getAppList());
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
    public void testSubResourceLocator() {
        assertTrue(resource.getApplicationResource(VM_ID.toString()) instanceof VmApplicationResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> resource.getApplicationResource("foo")));
    }

}
