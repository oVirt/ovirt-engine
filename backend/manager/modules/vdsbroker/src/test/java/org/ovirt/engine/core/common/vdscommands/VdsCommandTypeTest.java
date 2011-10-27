package org.ovirt.engine.core.common.vdscommands;

import org.junit.Assert;
import org.junit.Test;

public class VdsCommandTypeTest {

    private static final String VDSCommandsuffix = "VDSCommand";

    @Test
    public void testPackages() {
        for (VDSCommandType type : VDSCommandType.values()) {
            String className = type.getPackageName() + "." + type + VDSCommandsuffix;
            try {
                Class.forName(className);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to generate the class " + className);
                Assert.fail("failed to generate the class " + className);
            }
        }

    }

}
