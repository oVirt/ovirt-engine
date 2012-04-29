package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * An abstract class for setting up tests for queries using Sysprep.
 * This class statically mocks the Config values needed to avoid SysprepHandler's static init to crash
 */
public abstract class AbstractSysprepQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends AbstractUserQueryTest<P, Q> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final IConfigUtilsInterface configMock = Mockito.mock(IConfigUtilsInterface.class);
        Config.setConfigUtils(configMock);
        Mockito.when(configMock.GetValue(ConfigValues.AdUserName, Config.DefaultConfigurationVersion)).thenReturn("");
    }

}
