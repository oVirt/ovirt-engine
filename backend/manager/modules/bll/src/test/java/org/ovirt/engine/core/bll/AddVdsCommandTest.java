package org.ovirt.engine.core.bll;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

public class AddVdsCommandTest {
    private VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setVdsName("BAR");
        newVdsData.setVdsGroupCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setVdsGroupId(new Guid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.MaxVdsNameLength, 4),
                    MockConfigRule.mockConfig(ConfigValues.UseSecureConnectionWithServers, false));

    @Test
    public void canDoAction() {
        AddVdsActionParameters parameters = new AddVdsActionParameters();
        parameters.setRootPassword("secret");

        VdsGroupDAO groupDAOMock = Mockito.mock(VdsGroupDAO.class);

        Guid vdsId = new Guid();
        VDS newVds = makeTestVds(vdsId);
        parameters.setvds(newVds);
        @SuppressWarnings("unchecked")
        AddVdsCommand<AddVdsActionParameters> commandMock = Mockito.mock(AddVdsCommand.class);
        Mockito.when(commandMock.canDoAction()).thenCallRealMethod();
        Mockito.when(commandMock.getParameters()).thenReturn(parameters);

        Mockito.when(commandMock.isGlusterSupportEnabled()).thenReturn(false);
        Mockito.when(commandMock.getVdsGroupDAO()).thenReturn(groupDAOMock);

        VdsDAO vdsDaoMock = Mockito.mock(VdsDAO.class);
        Mockito.when(vdsDaoMock.get(vdsId)).thenReturn(null);
        Mockito.when(commandMock.getVdsDAO()).thenReturn(vdsDaoMock);
        Mockito.when(commandMock.validateVdsGroup()).thenReturn(true);
        Mockito.when(commandMock.validateSingleHostAttachedToLocalStorage()).thenReturn(true);
        Mockito.when(commandMock.isPowerManagementLegal()).thenReturn(true);
        Mockito.when(commandMock.canConnect(Mockito.any(VDS.class))).thenReturn(true);

        Assert.assertTrue(commandMock.canDoAction());
    }
}
