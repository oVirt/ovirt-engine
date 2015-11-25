package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;

public class AddVdsSpmIdCommandTest extends BaseCommandTest {

    private AddVdsSpmIdCommand<VdsActionParameters> cmd;
    private Guid spId;
    private Guid vdsId;

    @Mock
    private VdsSpmIdMapDao vdsSpmIdMapDao;

    @Before
    public void setUp() {
        VdsActionParameters params = new VdsActionParameters();
        CommandContext ctx = new CommandContext(new EngineContext());
        cmd = spy(new AddVdsSpmIdCommand<>(params, ctx));
        doReturn(vdsSpmIdMapDao).when(cmd).getVdsSpmIdMapDao();

        spId = Guid.newGuid();
        vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setStoragePoolId(spId);
        cmd.setVds(vds);
    }

    @Test
    public void fromOne() {
        insertSpmIdToDb(3, 1, 2);
    }

    @Test
    public void oneMissing() {
        insertSpmIdToDb(1, 4, 3);
    }

    @Test
    public void empty() {
        insertSpmIdToDb(1);
    }

    @Test
    public void hole() {
        insertSpmIdToDb(2, 3, 1, 4);
    }

    private void insertSpmIdToDb(int expected, int... given) {
        List<VdsSpmIdMap> list =
                Arrays.stream(given).mapToObj(i -> new VdsSpmIdMap(spId, vdsId, i)).collect(Collectors.toList());
        cmd.insertSpmIdToDb(list);
        verify(vdsSpmIdMapDao).save(new VdsSpmIdMap(spId, vdsId, expected));
    }
}
