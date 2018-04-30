package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;

public class GetGlusterHooksQueryTest extends
        AbstractQueryTest<GlusterParameters, GetGlusterHooksQuery<GlusterParameters>> {

    public static Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    List<GlusterHookEntity> expected;
    List<GlusterHookEntity> emptyList;

    @Mock
    GlusterHooksDao glusterHookDaoMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupMock();
    }


    private void setupMock() {
        expected = getExpectedHooksList();
        emptyList = new ArrayList<>();

        // Mock the query's parameters
        doReturn(CLUSTER_ID).when(getQueryParameters()).getClusterId();
    }

    private List<GlusterHookEntity> getExpectedHooksList() {
        List<GlusterHookEntity> glusterHooks = new ArrayList<>();
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setClusterId(CLUSTER_ID);
        hook.setGlusterCommand("start");
        hook.setStage("POST");
        hook.setName("cifs_config");
        hook.setStatus(GlusterHookStatus.ENABLED);
        hook.setChecksum("e72c504dc16c8fcd2fe8c74bb492affa");
        hook.setContentType(GlusterHookContentType.TEXT);

        hook.setConflictStatus(0);
        glusterHooks.add(hook);

        hook = new GlusterHookEntity();
        hook.setClusterId(CLUSTER_ID);
        hook.setGlusterCommand("create");
        hook.setStage("PRE");
        hook.setName("virt_config");
        hook.setStatus("DISABLED");
        hook.setChecksum("d72c504dc16c8fcd2fe8c74bb492affb");
        hook.setContentType(GlusterHookContentType.BINARY);
        hook.setConflictStatus(0);
        glusterHooks.add(hook);
        return glusterHooks;
    }

    private void mockEmptyListFromDb() {
        doReturn(emptyList).when(glusterHookDaoMock).getByClusterId(CLUSTER_ID);
    }

    private void mockExpectedListFromDb() {
        doReturn(expected).when(glusterHookDaoMock).getByClusterId(CLUSTER_ID);
    }

    @Test
    public void testExecuteQueryCommand1() {
        mockExpectedListFromDb();
        getQuery().executeQueryCommand();
        List<GlusterHookEntity> hooks = getQuery().getQueryReturnValue().getReturnValue();
        assertNotNull(hooks);
        assertEquals(expected, hooks);
    }

    @Test
    public void testExecuteQueryCommand2() {
        mockEmptyListFromDb();
        getQuery().executeQueryCommand();
        List<GlusterHookEntity> hooks = getQuery().getQueryReturnValue().getReturnValue();
        assertNotNull(hooks);
        assertEquals(emptyList, hooks);
    }
}
