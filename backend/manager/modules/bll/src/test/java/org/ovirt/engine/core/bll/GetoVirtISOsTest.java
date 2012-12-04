package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.CommandAssertUtils.checkSucceeded;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class GetoVirtISOsTest extends AbstractQueryTest<VdsIdParametersBase, GetoVirtISOsQuery<VdsIdParametersBase>> {

    private static final String OVIRT_INIT_SUPPORTED_VERSION = "5.8";
    private static final String OVIRT_ISO_PREFIX = "rhevh";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String OVIRT_ISOS_DATA_DIR = ".";
    private static final String AVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 6.2 - 20111010.0.el6";
    private static final String UNAVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 8.2 - 20111010.0.el6";
    private static final Version EXISTING_CLUSTER_VERSION = new Version("3.1");

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.oVirtISOsRepositoryPath, OVIRT_ISOS_REPOSITORY_PATH),
            mockConfig(ConfigValues.DataDir, OVIRT_ISOS_DATA_DIR),
            mockConfig(ConfigValues.OvirtIsoPrefix, OVIRT_ISO_PREFIX),
            mockConfig(ConfigValues.OvirtInitialSupportedIsoVersion, OVIRT_INIT_SUPPORTED_VERSION)
            );

    @Mock
    private VdsDAO vdsDAO;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDAO);
    }

    @Test
    public void testQueryWithHostId() {
        Guid vdsId = Guid.NewGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setvds_type(VDSType.oVirtNode);
        vds.sethost_os(AVAILABLE_OVIRT_ISO_VERSION);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);

        when(getQueryParameters().getVdsId()).thenReturn(vdsId);

        getQuery().setInternalExecution(true);
        getQuery().ExecuteCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValue(getQuery());
    }

    @Test
    public void testQueryClusterLevel() {
        Guid vdsId = Guid.NewGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setvds_type(VDSType.oVirtNode);
        vds.sethost_os(UNAVAILABLE_OVIRT_ISO_VERSION);
        vds.setvds_group_compatibility_version(EXISTING_CLUSTER_VERSION);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);

        when(getQueryParameters().getVdsId()).thenReturn(vdsId);

        getQuery().setInternalExecution(true);
        getQuery().ExecuteCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValue(getQuery());
    }

    @Test
    public void testQueryWithNonExistingHostId() {
        when(getQueryParameters().getVdsId()).thenReturn(Guid.NewGuid());
        getQuery().setInternalExecution(true);
        getQuery().ExecuteCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValue(getQuery());
    }

    @Test
    public void testQueryWithoutHostId() {
        getQuery().setInternalExecution(true);
        getQuery().ExecuteCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValue(getQuery());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrefixChange() {
        mcr.mockConfigValue(ConfigValues.OvirtIsoPrefix, "a different prefix");
        getQuery().setInternalExecution(true);
        getQuery().ExecuteCommand();

        checkSucceeded(getQuery(), true);
        assertTrue("Prefix was changed, no ISOs should be returned",
                ((List<RpmVersion>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static void checkReturnValue(GetoVirtISOsQuery<VdsIdParametersBase> query) {
        List<RpmVersion> isosList = (List<RpmVersion>) query.getQueryReturnValue().getReturnValue();
        assertTrue(!isosList.isEmpty());
    }

}
