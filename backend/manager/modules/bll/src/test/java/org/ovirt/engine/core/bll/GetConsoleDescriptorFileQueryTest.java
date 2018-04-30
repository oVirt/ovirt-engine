package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.compat.Guid;

public class GetConsoleDescriptorFileQueryTest extends
        AbstractUserQueryTest<ConsoleOptionsParams, GetConsoleDescriptorFileQuery<ConsoleOptionsParams>> {

    @Test
    public void shouldFailWhenVmNull() {
        ConsoleOptions options = new ConsoleOptions(GraphicsType.SPICE);

        when(getQueryParameters().getOptions()).thenReturn(options);
        assertFalse(getQuery().validateInputs());
    }

    @Test
    public void shouldFailWhenGraphicsTypeNull() {
        ConsoleOptions options = new ConsoleOptions();
        options.setVmId(Guid.Empty);

        when(getQueryParameters().getOptions()).thenReturn(options);
        assertFalse(getQuery().validateInputs());
    }

    @Test
    public void shouldPass() {
        ConsoleOptions options = new ConsoleOptions(GraphicsType.SPICE);
        options.setVmId(Guid.Empty);

        when(getQueryParameters().getOptions()).thenReturn(options);
        assertTrue(getQuery().validateInputs());
    }

}
