package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUserResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.GetDbUserByUserNameAndDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testList() {
        BackendUserResource userResourceMock = mock(BackendUserResource.class);
        resource.setUserResource(userResourceMock);
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(QueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getEntity(0));
        setUpGetEntityExpectations(QueryType.GetDbUserByUserNameAndDomain,
            GetDbUserByUserNameAndDomainQueryParameters.class,
            new String[] { "UserName", "DomainName" },
            new Object[] { "admin", "internal" },
            null
        );
        Sessions sessions = resource.list();
        assertEquals(2, sessions.getSessions().size());
        assertNotNull(sessions.getSessions().get(0).getVm());
        assertNotNull(sessions.getSessions().get(1).getVm());
        assertNotNull(sessions.getSessions().get(0).getId());
        assertNotNull(sessions.getSessions().get(1).getId());
    }
}
