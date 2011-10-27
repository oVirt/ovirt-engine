package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Backend.class, DbFacade.class, StorageHelperDirector.class })
public class BaseMockitoTest extends Assert {

    protected static final Guid[] GUIDS = new Guid[] {
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222"),
            new Guid("33333333-3333-3333-3333-333333333333"),
    };

    @Before
    public void setUp() {
        mockStatic(Backend.class);
        mockStatic(DbFacade.class);
        mockStatic(StorageHelperDirector.class);
    }

    protected DbFacade setUpDB() {
        DbFacade db = mock(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        return db;
    }

    protected BackendInternal setUpBackend() {
        BackendInternal backend = mock(BackendInternal.class);
        when(Backend.getInstance()).thenReturn(backend);
        return backend;
    }

    protected void checkSucceeded(QueriesCommandBase<?> query, boolean expected) {
        assertEquals(expected, query.getQueryReturnValue().getSucceeded());
    }

    protected void checkSucceeded(CommandBase<?> cmd, boolean expected) {
        assertEquals(expected, cmd.getReturnValue().getSucceeded());
    }

    protected void checkMessages(CommandBase<?> cmd, VdcBllMessages... expected) {
        List<String> returned = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals(expected.length, returned.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), returned.get(i));
        }
    }
}
