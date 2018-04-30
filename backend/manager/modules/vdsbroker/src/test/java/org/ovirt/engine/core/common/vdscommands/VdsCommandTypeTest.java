package org.ovirt.engine.core.common.vdscommands;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class VdsCommandTypeTest {
    private static final String VDSCommandsuffix = "VDSCommand";

    @ParameterizedTest
    @EnumSource(value = VDSCommandType.class)
    public void testPackages(VDSCommandType type) throws ClassNotFoundException {
        Class.forName(type.getPackageName() + "." + type + VDSCommandsuffix);
    }
}
