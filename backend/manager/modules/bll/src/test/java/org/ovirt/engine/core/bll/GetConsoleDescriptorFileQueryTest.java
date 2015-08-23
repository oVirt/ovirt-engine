package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.compat.Guid;

public class GetConsoleDescriptorFileQueryTest extends BaseCommandTest {

    @Test
    public void shouldFailWhenVmNull() throws Exception {
        ConsoleOptions options = new ConsoleOptions(GraphicsType.SPICE);

        ConsoleOptionsParams params = new ConsoleOptionsParams(options);
        GetConsoleDescriptorFileQuery query = new GetConsoleDescriptorFileQuery(params);
        assertFalse(query.validateInputs());
    }

    @Test
    public void shouldFailWhenGraphicsTypeNull() throws Exception {
        ConsoleOptions options = new ConsoleOptions();
        options.setVmId(Guid.Empty);

        ConsoleOptionsParams params = new ConsoleOptionsParams(options);
        GetConsoleDescriptorFileQuery query = new GetConsoleDescriptorFileQuery(params);
        assertFalse(query.validateInputs());
    }

    @Test
    public void shouldPass() throws Exception {
        ConsoleOptions options = new ConsoleOptions(GraphicsType.SPICE);
        options.setVmId(Guid.Empty);

        ConsoleOptionsParams params = new ConsoleOptionsParams(options);
        GetConsoleDescriptorFileQuery query = new GetConsoleDescriptorFileQuery(params);
        assertTrue(query.validateInputs());
    }

}
