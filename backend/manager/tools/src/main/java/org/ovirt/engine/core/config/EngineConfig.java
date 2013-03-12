package org.ovirt.engine.core.config;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.validation.ConfigActionType;
import org.ovirt.engine.core.tools.ToolConsole;

/**
 * The <code>EngineConfig</code> class represents the main class of the EngineConfig tool.
 */
public class EngineConfig {
    // The log:
    private static final Logger log = Logger.getLogger(EngineConfig.class);

    // The console:
    private static final ToolConsole console = ToolConsole.getInstance();

    public static final String CONFIG_FILE_PATH_PROPERTY = "engine-config.config.file.path";
    public static final String DEFAULT_CONFIG_PATH = "/etc/ovirt-engine/engine-config/";
    private EngineConfigCLIParser parser;
    private EngineConfigLogic engineConfigLogic;
    private static EngineConfig instance = new EngineConfig();

    private EngineConfig() {
    }

    /**
     * Parses the arguments, validates that they are valid, instantiates the EngineConfigLogic object and executes the
     * desired action.
     *
     * @param args
     *            The arguments given by the user.
     *
     * @throws Exception
     */
    public void setUpAndExecute(String... args) throws Exception {
        parser.parse(args);
        log.debug("Arguments have been parsed: " + parser.engineConfigMapToString());
        ConfigActionType actionType = parser.getConfigAction();
        actionType.validate(parser.getEngineConfigMap());
        setEngineConfigLogic(new EngineConfigLogic(parser));
        engineConfigLogic.execute();
    }

    /**
     * The main method, instantiates the parser and executes.
     *
     * @param args
     *            The arguments given by the user.
     */
    public static void main(String... args) {
        try {
            getInstance().setParser(new EngineConfigCLIParser());
            getInstance().setUpAndExecute(args);

        } catch (Throwable t) {
            log.debug("Exiting with error: ", t);
            console.writeErrorLine(t.getMessage());
            System.exit(1);
        }
    }

    public void setEngineConfigLogic(EngineConfigLogic engineConfigLogic) {
        this.engineConfigLogic = engineConfigLogic;
    }

    public void setParser(EngineConfigCLIParser parser) {
        this.parser = parser;
    }

    public EngineConfigLogic getEngineConfigLogic() {
        return instance.engineConfigLogic;
    }

    public static EngineConfig getInstance() {
        return instance;
    }
}
