package org.ovirt.engine.core.bll.hostdeploy;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;

public class UpdateVdsCommandTest {

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.MaxVdsNameLength, 4));

    private VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setVdsName("BAR");
        newVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setClusterId(Guid.newGuid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateSameName() {
        UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
        Guid vdsId = Guid.newGuid();
        VDS newVdsData = makeTestVds(vdsId);
        VDS oldVdsData = newVdsData.clone();
        oldVdsData.setVdsName("FOO");
        oldVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        parameters.setvds(newVdsData);

        UpdateVdsCommand<UpdateVdsActionParameters> commandMock = Mockito.mock(UpdateVdsCommand.class);
        Mockito.when(commandMock.getVdsId()).thenReturn(vdsId);
        Mockito.when(commandMock.validate()).thenCallRealMethod();
        Mockito.when(commandMock.getParameters()).thenReturn(parameters);
        Version version = new Version("1.2.3");
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        when(commandMock.getCluster()).thenReturn(cluster);
        when(commandMock.isPowerManagementLegal(parameters.getVdsStaticData().isPmEnabled(),
                parameters.getFenceAgents(),
                new Version("1.2.3").toString())).thenReturn(true);

        VdsDao vdsDaoMock = Mockito.mock(VdsDao.class);
        Mockito.when(vdsDaoMock.get(vdsId)).thenReturn(oldVdsData);
        //now return the old vds data
        Mockito.when(vdsDaoMock.getByName("BAR")).thenReturn(oldVdsData);

        Mockito.when(commandMock.getVdsDao()).thenReturn(vdsDaoMock);
        Mockito.when(commandMock.getDbFacade()).thenReturn(Mockito.mock(DbFacade.class));
        VdsHandler.init();

        Assert.assertFalse(commandMock.validate());
    }

}
