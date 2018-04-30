package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterServiceDaoTest extends BaseDaoTestCase<GlusterServiceDao> {
    private static final String GLUSTER_SERVICE = "gluster-test";
    private static final Guid GLUSTER_SERVICE_ID = new Guid("fc00df54-4fcd-4495-8756-b217780bdad7");
    private static final String GLUSTER_SWIFT_SERVICE1 = "gluster-swift-test-1";
    private static final Guid GLUSTER_SWIFT_SERVICE1_ID =
            new Guid("c83c9ee3-b7d8-4709-ae4b-5d86a152e6b1");
    private static final String GLUSTER_SWIFT_SERVICE2 = "gluster-swift-test-2";

    @Test
    public void testGetByServiceType() {
        List<GlusterService> services = dao.getByServiceType(ServiceType.GLUSTER);
        assertNotNull(services);
        assertEquals(1, services.size());
        assertEquals(GLUSTER_SERVICE, services.get(0).getServiceName());

        services = dao.getByServiceType(ServiceType.GLUSTER_SWIFT);
        services.sort((p1, p2) -> p1.getServiceName().compareTo(p2.getServiceName()));
        assertNotNull(services);
        assertEquals(2, services.size());
        assertEquals(GLUSTER_SWIFT_SERVICE1, services.get(0).getServiceName());
        assertEquals(GLUSTER_SWIFT_SERVICE2, services.get(1).getServiceName());
    }

    @Test
    public void testGetByServiceTypeAndName() {
        GlusterService service = dao.getByServiceTypeAndName(ServiceType.GLUSTER, GLUSTER_SERVICE);
        assertNotNull(service);
        assertEquals(GLUSTER_SERVICE_ID, service.getId());
    }

    @Test
    public void testGetAll() {
        List<GlusterService> services = dao.getAll();
        assertNotNull(services);
        assertEquals(3, services.size());
        assertEquals(GLUSTER_SWIFT_SERVICE1, services.get(0).getServiceName());
        assertEquals(GLUSTER_SWIFT_SERVICE2, services.get(1).getServiceName());
        assertEquals(GLUSTER_SERVICE, services.get(2).getServiceName());
    }

    @Test
    public void testGet() {
        GlusterService service = dao.get(GLUSTER_SWIFT_SERVICE1_ID);
        assertNotNull(service);
        assertEquals(ServiceType.GLUSTER_SWIFT, service.getServiceType());
        assertEquals(GLUSTER_SWIFT_SERVICE1, service.getServiceName());
    }
}
