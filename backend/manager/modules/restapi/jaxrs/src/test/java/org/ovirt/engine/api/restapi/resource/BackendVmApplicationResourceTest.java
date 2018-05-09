package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Applications;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmApplicationResourceTest
        extends AbstractBackendSubResourceTest<Application, Applications, BackendVmApplicationResource> {

    protected static final Guid VM_ID = GUIDS[1];
    protected static final int APPLICATION_INDEX = 1;
    protected static final Guid APPLICATION_ID = new Guid(NAMES[APPLICATION_INDEX].getBytes());

    protected static BackendVmApplicationsResource collection = new BackendVmApplicationsResource(VM_ID);

    public BackendVmApplicationResourceTest() {
        super(getResource(APPLICATION_ID));
    }

    protected static BackendVmApplicationResource getResource(Guid id) {
        return new BackendVmApplicationResource(
                id.toString(),
                collection);
    }

    protected BackendVmApplicationResource getNotFoundResource() {
        BackendVmApplicationResource ret = getResource(new Guid("0d0264ef-40de-45a1-b746-83a0088b47a7"));
        ret.setUriInfo(setUpBasicUriExpectations());
        initResource(ret);
        return ret;
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
        collection.setUriInfo(uriInfo);
    }

    @Override
    protected void init() {
        super.init();
        initCollection();
    }

    private void initCollection() {
        collection.setMappingLocator(mapperLocator);
        collection.setMessageBundle(messageBundle);
        collection.setHttpHeaders(httpHeaders);
    }

    @Override
    protected Applications getEntity(int index) {
        return null;
    }

    @Test
    public void testGetNotFound() {
        BackendVmApplicationResource resource = getNotFoundResource();
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations();
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations();

        Application application = resource.get();
        assertEquals(NAMES[APPLICATION_INDEX], application.getName());
        verifyLinks(application);
    }

    protected void setUpEntityQueryExpectations() {
        setUpEntityQueryExpectations(QueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{VM_ID},
                getVm());
    }

    protected VM getVm() {
        return setUpEntityExpectations(mock(VM.class),
                mock(VmDynamic.class));
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

}
