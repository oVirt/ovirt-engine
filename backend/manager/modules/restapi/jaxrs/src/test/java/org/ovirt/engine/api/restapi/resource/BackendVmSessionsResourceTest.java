package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import static org.easymock.EasyMock.expect;

public class BackendVmSessionsResourceTest extends AbstractBackendResourceTest<Session, VM> {

    BackendVmSessionsResource resource = new BackendVmSessionsResource(GUIDS[0]);

    @Override
    protected VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setConsoleCurrentUserName("admin");
        vmDynamic.setClientIp("1.1.1.1");
        vmDynamic.setGuestCurrentUserName("Ori");
        vm.setDynamicData(vmDynamic);
        return vm;
    }

    @Override
    protected void init() {
        resource.setBackend(backend);
        resource.setMappingLocator(mapperLocator);
        resource.setValidatorLocator(validatorLocator);
        resource.setSessionHelper(sessionHelper);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testList() throws Exception {
        BackendUserResource userResourceMock = control.createMock(BackendUserResource.class);
        expect(userResourceMock.getUserByName("admin")).andReturn(getUser()).anyTimes();
        resource.setUserResource(userResourceMock);
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getEntity(0));
        control.replay();
        Sessions sessions = resource.list();
        assertEquals(sessions.getSessions().size(), 2);
        assertNotNull(sessions.getSessions().get(0).getVm());
        assertNotNull(sessions.getSessions().get(1).getVm());
        assertNotNull(sessions.getSessions().get(0).getId());
        assertNotNull(sessions.getSessions().get(1).getId());
        Session consoleSession =
                sessions.getSessions().get(0).getUser().getName().equals("admin") ? sessions.getSessions().get(0)
                        : sessions.getSessions().get(1);
        assertEquals(consoleSession.getUser().getHref(), "/ovirt-engine/api/users/11111111-1111-1111-1111-111111111111");
    }

    private User getUser() {
        User user = new User();
        user.setName("admin");
        user.setId(GUIDS[1].toString());
        return user;
    }
}
