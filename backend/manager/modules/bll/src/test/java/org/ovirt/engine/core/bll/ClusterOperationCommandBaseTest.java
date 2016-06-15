package org.ovirt.engine.core.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ClusterOperationCommandBaseTest {

    private static final int MAX_VDS_MEMORY_OVER_COMMIT = 200;

    private static final String CLUSTER_OVER_COMMIT_VALUE_INVALID = "CLUSTER_OVER_COMMIT_VALUE_INVALID";

    @Spy @InjectMocks
    private ClusterOperationCommandBase<ClusterOperationParameters> underTest =
            new ClusterOperationCommandBase(new ClusterOperationParameters(new Cluster()),
                    CommandContext.createContext("testClusterOperationCommandBase")) {
                @Override protected void executeCommand() {

                }
            };

    private static final Guid CLUSTER_ID = Guid.newGuid();

    @Before
    public void setup() {
        Cluster cluster = underTest.getParameters().getCluster();
        cluster.setId(CLUSTER_ID);
        IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        when(configUtils.getValue(ConfigValues.MaxVdsMemOverCommit, ConfigCommon.defaultConfigurationVersion))
                .thenReturn(MAX_VDS_MEMORY_OVER_COMMIT);
        Config.setConfigUtils(configUtils);

    }

    @Test
    public void testValidOverCommitValues() {
        //given no cluster changes
        initClusterWithMemoryOverCommitValue(0);
        assertThat(underTest.validateInputs()).isTrue();
        assertThat(underTest.getReturnValue().getValidationMessages()).isEmpty();
        initClusterWithMemoryOverCommitValue(120);
        assertThat(underTest.validateInputs()).isTrue();
        assertThat(underTest.getReturnValue().getValidationMessages()).isEmpty();
        initClusterWithMemoryOverCommitValue(220);
        assertThat(underTest.validateInputs()).isTrue();
        assertThat(underTest.getReturnValue().getValidationMessages()).isEmpty();
    }

    @Test
    public void testInValidOverCommitValues() {
        initClusterWithMemoryOverCommitValue(-1);
        assertThat(underTest.validateInputs()).isFalse();
        assertThat(underTest.getReturnValue().getValidationMessages()).contains(CLUSTER_OVER_COMMIT_VALUE_INVALID);
        initClusterWithMemoryOverCommitValue(-200);
        assertThat(underTest.validateInputs()).isFalse();
        assertThat(underTest.getReturnValue().getValidationMessages()).contains(CLUSTER_OVER_COMMIT_VALUE_INVALID);
    }

    private void initClusterWithMemoryOverCommitValue(int overCommitValue) {
        Cluster cluster = underTest.getParameters().getCluster();
        cluster.setMaxVdsMemoryOverCommit(overCommitValue);
    }
}
