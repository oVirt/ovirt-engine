package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.CommandAssertUtils.checkSucceeded;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

public class GetoVirtISOsTest extends AbstractQueryTest<IdQueryParameters, GetoVirtISOsQuery<IdQueryParameters>> {

    private static final String AVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 6.2 - 20111010.0.el6";
    private static final String UNAVAILABLE_OVIRT_ISO_VERSION = "RHEV Hypervisor - 8.2 - 20111010.0.el6";

    @Mock
    private VdsDao vdsDao;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(AbstractQueryTest.mockConfiguration(), Stream.of(
            MockConfigDescriptor.of(ConfigValues.OvirtInitialSupportedIsoVersion, "2.5.5:5.8"),
            MockConfigDescriptor.of(ConfigValues.OvirtIsoPrefix, "^ovirt-node-iso-([0-9].*)\\.iso$:^rhevh-([0-9].*)\\.iso$"),
            MockConfigDescriptor.of(ConfigValues.OvirtNodeOS, "^ovirt.*$:^rhev.*$"),
            MockConfigDescriptor.of(ConfigValues.DataDir, "/usr/share/engine"),
            MockConfigDescriptor.of(ConfigValues.oVirtISOsRepositoryPath, "/usr/share/ovirt-node-iso:/usr/share/rhev-hypervisor"))
        );
    }

    @Test
    public void testQueryWithHostId() {
        Guid vdsId = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsId);
        vds.setVdsType(VDSType.oVirtVintageNode);
        vds.setHostOs(AVAILABLE_OVIRT_ISO_VERSION);
        when(vdsDao.get(any())).thenReturn(vds);

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
        when(vdsDao.get(any())).thenReturn(vds);

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
    @MockedConfig("mockedConfigForMultiplePaths")
    public void testQueryMultiplePaths() {
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    public static Stream<MockConfigDescriptor<?>> mockedConfigForMultiplePaths() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(
                        ConfigValues.oVirtISOsRepositoryPath, "src/test/resources/ovirt-isos:src/test/resources/rhev-isos")));
    }

    @SuppressWarnings("unchecked")
    @Test
    @MockedConfig("mockedConfigForPrefixChange")
    public void testPrefixChange() {
        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValueEmpty(getQuery());
    }

    public static Stream<MockConfigDescriptor<?>> mockedConfigForPrefixChange() {
        return Stream.concat(mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.OvirtIsoPrefix, "a different prefix")));
    }

    @SuppressWarnings("unchecked")
    private static void checkReturnValueEmpty(GetoVirtISOsQuery<IdQueryParameters> query) {
        List<RpmVersion> isosList = query.getQueryReturnValue().getReturnValue();
        assertTrue(isosList.isEmpty());
    }
}
