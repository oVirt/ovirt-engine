package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class, DbFacade.class })
public class GetoVirtISOsTest extends BaseMockitoTest {

    private static final String OVIRT_INIT_SUPPORTED_VERSION = "5.8";
    private static final String OVIRT_ISO_PREFIX = "rhevh";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String AVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 6.2 - 20111010.0.el6";

    @Mock private DbFacade dbFacade;
    @Mock private VdsDAO vdsDAO;

    @Before
    public void setUp() {
        initMocks(this);
        ConfigMocker cfgMocker = new ConfigMocker();
        cfgMocker.mockOVirtISOsRepositoryPath(OVIRT_ISOS_REPOSITORY_PATH);
        cfgMocker.mockConfigOvirtIsoPrefix(OVIRT_ISO_PREFIX);
        cfgMocker.mockConfigOvirtInitialSupportedIsoVersion(OVIRT_INIT_SUPPORTED_VERSION);

        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getVdsDAO()).thenReturn(vdsDAO);
        when(vdsDAO.get(any(Guid.class))).thenReturn(null);
    }

    @Test
    public void testQueryWithHostId() {
        Guid vdsId = Guid.NewGuid();
        VDS vds = new VDS();
        vds.setvds_id(vdsId);
        vds.setvds_type(VDSType.oVirtNode);
        vds.sethost_os(AVAILABLE_OVIRT_ISO_VERSION);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);

        VdsIdParametersBase params = new VdsIdParametersBase(vds.getvds_id());
        GetoVirtISOsQuery<VdsIdParametersBase> query = new GetoVirtISOsQuery<VdsIdParametersBase>(params);
        query.ExecuteCommand();

        checkSucceeded(query, true);
        checkReturnValue(query);
    }

    @Test
    public void testQueryWithNonExistingHostId() {
        VdsIdParametersBase params = new VdsIdParametersBase(Guid.NewGuid());
        GetoVirtISOsQuery<VdsIdParametersBase> query = new GetoVirtISOsQuery<VdsIdParametersBase>(params);
        query.ExecuteCommand();

        checkSucceeded(query, true);
        checkReturnValue(query);
    }

    @Test
    public void testQueryWithoutHostId() {
        VdsIdParametersBase params = new VdsIdParametersBase();
        GetoVirtISOsQuery<VdsIdParametersBase> query = new GetoVirtISOsQuery<VdsIdParametersBase>(params);
        query.ExecuteCommand();

        checkSucceeded(query, true);
        checkReturnValue(query);
    }

    @SuppressWarnings("unchecked")
    private void checkReturnValue(GetoVirtISOsQuery<VdsIdParametersBase> query) {
        List<RpmVersion> isosList = (List<RpmVersion>) query.getQueryReturnValue().getReturnValue();
        assertTrue(!isosList.isEmpty());
    }

}
