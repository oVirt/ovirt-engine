package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;

public class AddVdsSpmIdCommandTest extends BaseCommandTest {
    @InjectMocks
    private AddVdsSpmIdCommand<VdsActionParameters> cmd = new AddVdsSpmIdCommand<>(new VdsActionParameters(), null);
    private Guid spId;
    private Guid vdsId;

    @Mock
    private VdsSpmIdMapDao vdsSpmIdMapDao;

    @BeforeEach
    public void setUp() {
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
