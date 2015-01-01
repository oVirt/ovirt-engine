package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;
import static org.ovirt.engine.core.utils.MockConfigRule.MockConfigDescriptor;

import java.util.Collections;
import java.util.Set;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * An abstract class for setting up tests for queries using Sysprep.
 * This class statically mocks the Config values needed to avoid SysprepHandler's static init to crash
 */
public abstract class AbstractSysprepQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends AbstractUserQueryTest<P, Q> {

    @Override
    public Set<MockConfigDescriptor<String>> getExtraConfigDescriptors() {
        return Collections.singleton(mockConfig(ConfigValues.AdUserName, ""));
    }
}
