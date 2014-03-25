package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class HandleVdsVersionCommandTest {
    private static Version lastVersion = Version.ALL.get(Version.ALL.size() - 1);
    protected Guid vdsId = Guid.newGuid();
    protected Guid vdsGroupId = Guid.newGuid();

    private HandleVdsVersionCommand<VdsActionParameters> command;

    @Mock
    private VdsDAO vdsDao;

    @Mock
    private VdsGroupDAO vdsGroupDao;

    private VDSGroup vdsGroup;

    private VDS vds;

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VdcVersion, "general", lastVersion.getValue()));

    @Test
    public void supportedVdsm() {
        mockVds(Version.ALL);
        mockVdsGroup(lastVersion);
        command.executeCommand();

        assertTrue(command.getSucceeded());
        Mockito.verify(command, Mockito.times(0)).reportNonOperationReason(Mockito.any(NonOperationalReason.class),
                Mockito.any(String.class),
                Mockito.any(String.class));
    }

    @Test
    public void supportedEngineNotCluster() {
        mockVds(Version.ALL);
        mockVdsGroup(new Version());
        command.executeCommand();
        assertTrue(command.getSucceeded());
        Mockito.verify(command, Mockito.times(1))
                .reportNonOperationReason(NonOperationalReason.CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER,
                        vdsGroup.getcompatibility_version().toString(),
                        vds.getSupportedClusterLevels().toString());
    }

    @Test
    public void unSupportedVdsm() {
        List<Version> versions = new ArrayList<>();
        versions.add(new Version());
        mockVds(versions);
        mockVdsGroup(lastVersion);
        command.executeCommand();
        assertTrue(command.getSucceeded());
        Mockito.verify(command, Mockito.times(1))
                .reportNonOperationReason(NonOperationalReason.VERSION_INCOMPATIBLE_WITH_CLUSTER,
                        lastVersion.getValue(),
                        new Version(vds.getVersion().getMajor(), vds.getVersion().getMinor()).toString());
    }

    protected HandleVdsVersionCommand<VdsActionParameters> createCommand() {
        return new HandleVdsVersionCommand<VdsActionParameters>(new VdsActionParameters(vdsId));
    }

    @Before
    public void initializeCommand() {
        command = spy(createCommand());
        Mockito.doNothing()
                .when(command)
                .reportNonOperationReason(Mockito.any(NonOperationalReason.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class));
    }

    protected void mockVds(List<Version> versions) {
        vds = Mockito.mock(VDS.class);
        HashSet<Version> supportedVersions = new HashSet<>();
        supportedVersions.addAll(versions);
        doReturn(supportedVersions).when(vds).getSupportedENGINESVersionsSet();
        doReturn(new RpmVersion("libvirt-0.9.10-21.el6_" + lastVersion.getValue(), "libvirt-", true)).when(vds)
                .getVersion();
        doReturn("" + lastVersion.getMajor() + lastVersion.getMinor()).when(vds).getSupportedEngines();
        doReturn("" + lastVersion.getMajor() + lastVersion.getMinor()).when(vds).getSupportedClusterLevels();
        doReturn(supportedVersions).when(vds).getSupportedClusterVersionsSet();

        doReturn(vdsDao).when(command).getVdsDAO();
        when(vdsDao.get(Mockito.any(Guid.class))).thenReturn(vds);
    }

    protected void mockVdsGroup(Version version) {
        vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(version);
        doReturn(vdsGroupDao).when(command).getVdsGroupDAO();
        when(vdsGroupDao.get(Mockito.any(Guid.class))).thenReturn(vdsGroup);
    }
}
