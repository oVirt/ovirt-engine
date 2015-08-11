package org.ovirt.engine.ui.frontend;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.errors.EngineError;

@RunWith(Theories.class)
public class VdsmErrorsTest extends AbstractConstantsWithLookupTestCase {

    @DataPoints
    public static String[] vdsmErrors = methodNames(VdsmErrors.class);

    @DataPoint
    public static Class<? extends Enum<?>> vdsmErrorsClass = EngineError.class;
}
