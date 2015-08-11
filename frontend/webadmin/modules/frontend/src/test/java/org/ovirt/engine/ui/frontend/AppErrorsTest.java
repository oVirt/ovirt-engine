package org.ovirt.engine.ui.frontend;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(Theories.class)
public class AppErrorsTest extends AbstractConstantsWithLookupTestCase {

    @DataPoints
    public static String[] appErrors = methodNames(AppErrors.class);

    @DataPoint
    public static Class<? extends Enum<?>> appErrorsClass = EngineMessage.class;
}
