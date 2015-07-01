package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterClusterServiceDaoTest extends BaseDaoTestCase {
    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid NEW_CLUSTER_ID = new Guid("eba797fb-8e3b-4777-b63c-92e7a5957d7e");
    private GlusterClusterServiceDao dao;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterClusterServiceDao();
    }

    @Test
    public void testGetByClusterId() {
        List<GlusterClusterService> services = dao.getByClusterId(CLUSTER_ID);
        assertNotNull(services);
        assertEquals(2, services.size());
        for (GlusterClusterService service : services) {
            switch (service.getServiceType()) {
            case GLUSTER:
                assertEquals(GlusterServiceStatus.RUNNING, service.getStatus());
                break;
            case GLUSTER_SWIFT:
                assertEquals(GlusterServiceStatus.STOPPED, service.getStatus());
                break;
            default:
                fail("Unexpected service type: " + service.getServiceType());
            }
        }
    }

    @Test
    public void testGetByClusterIdAndServiceType() {
        GlusterClusterService service =
                dao.getByClusterIdAndServiceType(CLUSTER_ID, ServiceType.GLUSTER);
        assertNotNull(service);
        assertTrue(service.getStatus() == GlusterServiceStatus.RUNNING);
    }

    @Test
    public void testSave() {
        List<GlusterClusterService> services = dao.getByClusterId(NEW_CLUSTER_ID);
        assertNotNull(services);
        assertEquals(0, services.size());

        insertTestService(NEW_CLUSTER_ID, ServiceType.SMB, GlusterServiceStatus.MIXED);

        services = dao.getByClusterId(NEW_CLUSTER_ID);
        assertNotNull(services);
        assertEquals(1, services.size());

        GlusterClusterService service = services.get(0);
        assertEquals(ServiceType.SMB, service.getServiceType());
        assertEquals(GlusterServiceStatus.MIXED, service.getStatus());
    }

    @Test
    public void testUpdate() {
        GlusterClusterService service =
                dao.getByClusterIdAndServiceType(CLUSTER_ID, ServiceType.GLUSTER);
        assertNotNull(service);
        assertEquals(GlusterServiceStatus.RUNNING, service.getStatus());
        service.setStatus(GlusterServiceStatus.STOPPED);

        dao.update(service);

        service = dao.getByClusterIdAndServiceType(CLUSTER_ID, ServiceType.GLUSTER);
        assertNotNull(service);
        assertEquals(GlusterServiceStatus.STOPPED, service.getStatus());
    }

    private void insertTestService(Guid clusterId, ServiceType serviceType, GlusterServiceStatus status) {
        GlusterClusterService service = new GlusterClusterService();
        service.setClusterId(clusterId);
        service.setServiceType(serviceType);
        service.setStatus(status);
        dao.save(service);
    }
}
