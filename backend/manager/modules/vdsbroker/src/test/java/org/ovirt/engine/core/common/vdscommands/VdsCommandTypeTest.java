package org.ovirt.engine.core.common.vdscommands;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class VdsCommandTypeTest {

    private static final String VDSCommandsuffix = "VDSCommand";

    @DataPoints
    public static final VDSCommandType[] types = VDSCommandType.values();

    @Theory
    public void testPackages(VDSCommandType type) throws ClassNotFoundException {
        Class.forName(type.getPackageName() + "." + type + VDSCommandsuffix);
    }
}
