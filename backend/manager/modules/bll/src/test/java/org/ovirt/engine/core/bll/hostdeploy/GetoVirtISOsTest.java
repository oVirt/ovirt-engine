package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.CommandAssertUtils.checkSucceeded;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dao.VdsDao;

@RunWith(MockitoJUnitRunner.class)
public class GetoVirtISOsTest extends AbstractQueryTest<IdQueryParameters, GetoVirtISOsQuery<IdQueryParameters>> {

    private static final String OVIRT_INIT_SUPPORTED_VERSION = "5.8";
    private static final String OVIRT_ISO_PREFIX = "^rhevh-(.*)\\.*\\.iso$";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String OVIRT_ISOS_DATA_DIR = ".";
    private static final String AVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 6.2 - 20111010.0.el6";
    private static final String UNAVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 8.2 - 20111010.0.el6";
    private static final String OVIRT_NODEOS = "^rhevh.*";

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.oVirtISOsRepositoryPath, OVIRT_ISOS_REPOSITORY_PATH),
            mockConfig(ConfigValues.DataDir, OVIRT_ISOS_DATA_DIR),
            mockConfig(ConfigValues.OvirtIsoPrefix, OVIRT_ISO_PREFIX),
            mockConfig(ConfigValues.OvirtInitialSupportedIsoVersion, OVIRT_INIT_SUPPORTED_VERSION),
            mockConfig(ConfigValues.OvirtNodeOS, OVIRT_NODEOS),
            mockConfig(ConfigValues.UserSessionTimeOutInterval, 60)
            );

    @Mock
    private VdsDao vdsDao;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDao);
    }

    @Test
    public void testQueryWithHostId() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setVdsType(VDSType.oVirtVintageNode);
        vds.setHostOs(AVAILABLE_OVIRT_ISO_VERSION);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);

        when(getQueryParameters().getId()).thenReturn(vdsId);

        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @Test
    public void testQueryClusterLevel() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setVdsType(VDSType.oVirtVintageNode);
        vds.setHostOs(UNAVAILABLE_OVIRT_ISO_VERSION);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);

        when(getQueryParameters().getId()).thenReturn(vdsId);

        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @Test
    public void testQueryWithNonExistingHostId() {
        when(getQueryParameters().getId()).thenReturn(Guid.newGuid());
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @Test
    public void testQueryWithoutHostId() {
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @Test
    public void testQueryMultiplePaths() {
        mcr.mockConfigValue(ConfigValues.oVirtISOsRepositoryPath, "src/test/resources/ovirt-isos:src/test/resources/rhev-isos");
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrefixChange() {
        mcr.mockConfigValue(ConfigValues.OvirtIsoPrefix, "a different prefix");
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    @SuppressWarnings("unchecked")
    private static void checkReturnValueEmpty(GetoVirtISOsQuery<IdQueryParameters> query) {
        List<RpmVersion> isosList = query.getQueryReturnValue().getReturnValue();
        assertTrue(isosList.isEmpty());
    }
}
