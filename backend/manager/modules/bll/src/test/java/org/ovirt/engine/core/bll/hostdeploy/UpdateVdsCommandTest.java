package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVdsCommandTest {

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.MaxVdsNameLength, 4));

    @Mock
    private VdsDao vdsDaoMock;

    @Spy
    private VdsHandler vdsHandler = new VdsHandler();

    @Spy
    @InjectMocks
    private UpdateVdsCommand<UpdateVdsActionParameters> commandMock
            = new UpdateVdsCommand<UpdateVdsActionParameters>(createParameters(), null) {
        @Override
        UpdateHostValidator getUpdateHostValidator() {
            return new UpdateHostValidator(oldHost,
                    getParameters().getvds(),
                    getParameters().isInstallHost());
        }
    };

    private static VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setVdsName("BAR");
        newVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setClusterId(Guid.newGuid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    private static UpdateVdsActionParameters createParameters() {
        UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
        Guid vdsId = Guid.newGuid();
        VDS newVdsData = makeTestVds(vdsId);
        VDS oldVdsData = newVdsData.clone();
        oldVdsData.setVdsName("FOO");
        oldVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        parameters.setvds(newVdsData);
        return parameters;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateSameName() {
        Version version = new Version("1.2.3");
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        doReturn(cluster).when(commandMock).getCluster();
        VdsStatic vdsStatic = commandMock.getParameters().getVdsStaticData();
        List<FenceAgent> fenceAgents = commandMock.getParameters().getFenceAgents();
        doReturn(true).when(commandMock).isPowerManagementLegal(
                vdsStatic.isPmEnabled(), fenceAgents, new Version("1.2.3").toString());

        Guid vdsId = commandMock.getParameters().getVdsId();
        VDS vds = commandMock.getParameters().getvds();
        when(vdsDaoMock.get(vdsId)).thenReturn(vds);

        //now return the old vds data
        when(vdsDaoMock.getByName("BAR")).thenReturn(vds);

        when(commandMock.getDbFacade()).thenReturn(mock(DbFacade.class));
        vdsHandler.init();

        assertFalse(commandMock.validate());
    }

}
