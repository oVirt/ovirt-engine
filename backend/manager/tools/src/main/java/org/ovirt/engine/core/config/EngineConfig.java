package org.ovirt.engine.core.config;

import java.io.File;

import org.ovirt.engine.core.config.validation.ConfigActionType;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>EngineConfig</code> class represents the main class of the EngineConfig tool.
 */
public class EngineConfig {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(EngineConfig.class);

    public static final String CONFIG_FILE_PATH_PROPERTY = "engine-config.config.file.path";
    public static final File DEFAULT_CONFIG_PATH = new File(EngineLocalConfig.getInstance().getEtcDir(), "engine-config");
    private EngineConfigLogic engineConfigLogic;
    private static EngineConfig instance = new EngineConfig();

    private EngineConfig() {
    }

    /**
     * Parses the arguments, validates that they are valid, instantiates the EngineConfigLogic object and executes the
     * desired action.
     *
     * @param parser
     *            parser instance with parsed args
     */
    public void setUpAndExecute(EngineConfigCLIParser parser) throws Exception {
        log.debug("Arguments have been parsed: {}", parser.engineConfigMapToString());
        ConfigActionType actionType = parser.getConfigAction();
        actionType.validate(parser.getEngineConfigMap());
        setEngineConfigLogic(new EngineConfigLogic(parser));
        engineConfigLogic.execute();
    }

    public void setEngineConfigLogic(EngineConfigLogic engineConfigLogic) {
        this.engineConfigLogic = engineConfigLogic;
    }

    public EngineConfigLogic getEngineConfigLogic() {
        return instance.engineConfigLogic;
    }

    public static EngineConfig getInstance() {
        return instance;
    }
}
