package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.console.ConsoleDescriptorGenerator;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;

/**
 * Generates descriptor file (for now only .vv file for SPICE and VNC is supported)
 * for given graphics protocol of given vm.
 *
 * @param <P> ConsoleOptions instance used for generating descriptor.
 */
public class GetConsoleDescriptorFileQuery<P extends ConsoleOptionsParams> extends QueriesCommandBase<P> {

    public GetConsoleDescriptorFileQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected boolean validateInputs() {
        ConsoleOptions options = getParameters().getOptions();
        if (options == null) {
            getQueryReturnValue().setExceptionString("Console options must be specified.");
            return false;
        }

        if (options.getVmId() == null) {
            getQueryReturnValue().setExceptionString("VM id must be specified.");
            return false;
        }

        if (options.getGraphicsType() == null) {
            getQueryReturnValue().setExceptionString("Graphics Type or Console Options must must be specified.");
            return false;
        }

        return true;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(ConsoleDescriptorGenerator.generateDescriptor(getParameters().getOptions()));
    }

}
