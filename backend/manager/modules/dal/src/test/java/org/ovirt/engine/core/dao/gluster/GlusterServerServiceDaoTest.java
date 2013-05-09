package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;

/**
 * Tests for Services DAO
 */
public class GlusterServerServiceDaoTest extends BaseDAOTestCase {
    private static final String NEW_SERVICE_ID = "d1745ef8-8369-43e5-b55a-b4fceea63877";
    private static final String SERVER1_ID = "23f6d691-5dfb-472b-86dc-9e1d2d3c18f3";
    private static final String SERVER2_ID = "2001751e-549b-4e7a-aff6-32d36856c125";
    private static final String CLUSTER_ID = "ae956031-6be2-43d6-bb8f-5191c9253314";
    private static final String SERVICE1_ID = "c83c9ee3-b7d8-4709-ae4b-5d86a152e6b1";
    private static final String SERVICE1_NAME = "gluster-swift-test-1";
    private static final ServiceType SERVICE1_TYPE = ServiceType.GLUSTER_SWIFT;
    private static final String SERVICE2_ID = "fc00df54-4fcd-4495-8756-b217780bdad7";
    private static final String SERVICE2_NAME = "gluster-test";
    private static final ServiceType SERVICE2_TYPE = ServiceType.GLUSTER;
    private static final String SERVER1_SERVICE_ID = "c77b4d6a-a2c9-4c9f-a873-3dbff8a34720";

    private static final Integer PID_1 = 11111;
    private static final Integer PID_2 = 22222;
    private static final Integer NEW_PID = 33333;
    private GlusterServerServiceDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterServerServiceDao();
    }

    @Test
    public void testGetByClusterId() {
        List<GlusterServerService> services = dao.getByClusterId(Guid.createGuidFromString(CLUSTER_ID));
        assertNotNull(services);
        assertEquals(2, services.size());
        for (GlusterServerService service : services) {
            switch (service.getServerId().toString()) {
            case SERVER1_ID:
                verifyServiceOnServer1(service);
                break;
            case SERVER2_ID:
                verifyServiceOnServer2(service);
                break;
            default:
                fail("Unexpected server id: " + service.getServerId());
            }
        }
    }

    @Test
    public void testGetByServerId() {
        List<GlusterServerService> services = dao.getByServerId(Guid.createGuidFromString(SERVER1_ID));
        assertNotNull(services);
        assertEquals(1, services.size());
        verifyServiceOnServer1(services.get(0));
    }

    @Test
    public void testGetAllWithQuery() {
        List<GlusterServerService> services =
                dao.getAllWithQuery("select * from gluster_server_services_view where status = '"
                        + GlusterServiceStatus.STOPPED.name() + "'");

        assertTrue(services != null);
        assertTrue(services.size() == 1);
        verifyServiceOnServer2(services.get(0));
    }

    @Test
    public void testSaveAndGet() {
        GlusterServerService service = dao.get(Guid.createGuidFromString(NEW_SERVICE_ID));
        assertNull(service);

        GlusterServerService newService = insertTestService();
        service = dao.get(Guid.createGuidFromString(NEW_SERVICE_ID));

        assertNotNull(service);
        assertEquals(newService, service);
    }

    @Test
    public void testUpdate() {
        GlusterServerService existingService = dao.get(Guid.createGuidFromString(SERVER1_SERVICE_ID));
        assertNotNull(existingService);
        assertEquals(GlusterServiceStatus.RUNNING, existingService.getStatus());

        GlusterServerService serviceToModify = dao.get(Guid.createGuidFromString(SERVER1_SERVICE_ID));
        serviceToModify.setStatus(GlusterServiceStatus.STOPPED);
        dao.update(serviceToModify);

        GlusterServerService modifiedService = dao.get(Guid.createGuidFromString(SERVER1_SERVICE_ID));
        assertNotNull(modifiedService);
        assertEquals(GlusterServiceStatus.STOPPED, modifiedService.getStatus());
        assertEquals(GlusterServiceStatus.RUNNING, existingService.getStatus());
        assertFalse(existingService.equals(modifiedService));
    }

    private GlusterServerService insertTestService() {
        GlusterServerService service = new GlusterServerService();
        service.setId(Guid.createGuidFromString(NEW_SERVICE_ID));
        service.setServerId(Guid.createGuidFromString(SERVER1_ID));
        service.setServiceId(Guid.createGuidFromString(SERVICE2_ID));
        service.setServiceType(SERVICE2_TYPE);
        service.setStatus(GlusterServiceStatus.FAILED);
        service.setMessage("test message");
        service.setPid(NEW_PID);

        dao.save(service);
        return service;
    }

    private void verifyServiceOnServer2(GlusterServerService service) {
        assertEquals(SERVICE2_ID, service.getServiceId().toString());
        assertEquals(SERVICE2_NAME, service.getServiceName());
        assertEquals(SERVICE2_TYPE, service.getServiceType());
        assertEquals(PID_2, service.getPid());
        assertEquals(GlusterServiceStatus.STOPPED, service.getStatus());
    }

    private void verifyServiceOnServer1(GlusterServerService service) {
        assertEquals(SERVICE1_ID, service.getServiceId().toString());
        assertEquals(SERVICE1_NAME, service.getServiceName());
        assertEquals(SERVICE1_TYPE, service.getServiceType());
        assertEquals(PID_1, service.getPid());
        assertEquals(GlusterServiceStatus.RUNNING, service.getStatus());
    }
}
