package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.ClassRule;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * An abstract class for setting up tests for queries using Sysprep.
 * This class statically mocks the Config values needed to avoid SysprepHandler's static init to crash
 */
public abstract class AbstractSysprepQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends AbstractUserQueryTest<P, Q> {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.AdUserName, ""),
            mockConfig(ConfigValues.UserSessionTimeOutInterval, 60));
}
