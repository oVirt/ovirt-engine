package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import org.junit.Test;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUserResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVmSessionsResourceTest extends AbstractBackendResourceTest<Session, VM> {

    BackendVmSessionsResource resource = new BackendVmSessionsResource(GUIDS[0]);

    @Override
    protected VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setConsoleCurrentUserName("admin@internal");
        vmDynamic.setClientIp("1.1.1.1");
        vmDynamic.setGuestCurrentUserName("Ori");
        vm.setDynamicData(vmDynamic);
        return vm;
    }

    @Override
    protected void init() {
        resource.setMappingLocator(mapperLocator);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testList() throws Exception {
        BackendUserResource userResourceMock = control.createMock(BackendUserResource.class);
        expect(userResourceMock.getUserByNameAndDomain("admin", "internal")).andReturn(getUser()).anyTimes();
        resource.setUserResource(userResourceMock);
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getEntity(0));
        control.replay();
        Sessions sessions = resource.list();
        assertEquals(2, sessions.getSessions().size());
        assertNotNull(sessions.getSessions().get(0).getVm());
        assertNotNull(sessions.getSessions().get(1).getVm());
        assertNotNull(sessions.getSessions().get(0).getId());
        assertNotNull(sessions.getSessions().get(1).getId());
    }

    private User getUser() {
        User user = new User();
        user.setUserName("admin");
        user.setId(GUIDS[1].toString());
        Domain domain = new Domain();
        domain.setName("internal");
        user.setDomain(domain);
        return user;
    }
}
