package org.ovirt.engine.core.bll;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * An abstract class for setting up tests for queries using Sysprep.
 * This class statically mocks the Config values needed to avoid SysprepHandler's static init to crash
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public abstract class AbstractSysprepQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends AbstractUserQueryTest<P, Q> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Mock away the static initializer
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.AdUserName)).thenReturn("");
    }

}
