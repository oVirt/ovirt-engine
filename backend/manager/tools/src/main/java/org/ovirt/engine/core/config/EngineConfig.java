package org.ovirt.engine.core.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ovirt.engine.core.config.validation.ConfigActionType;
import org.ovirt.engine.core.tools.ToolConsole;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * The <code>EngineConfig</code> class represents the main class of the EngineConfig tool.
 */
public class EngineConfig {
    // The log:
    private static final Logger log = Logger.getLogger(EngineConfig.class);

    // The console:
    private static final ToolConsole console = ToolConsole.getInstance();

    public static final String CONFIG_FILE_PATH_PROPERTY = "engine-config.config.file.path";
    public static final File DEFAULT_CONFIG_PATH = new File(EngineLocalConfig.getInstance().getEtcDir(), "engine-config");
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
     * Initializes logging configuration
     */
    private static void initLogging() {
        String cfgFile = System.getProperty("log4j.configuration");
        if (StringUtils.isNotBlank(cfgFile)) {
            try {
                URL url = new URL(cfgFile);
                LogManager.resetConfiguration();
                DOMConfigurator.configure(url);
            } catch (FactoryConfigurationError | MalformedURLException ex) {
                System.out.println("Cannot configure logging: " + ex.getMessage());
            }
        }
    }

    /**
     * The main method, instantiates the parser and executes.
     *
     * @param args
     *            The arguments given by the user.
     */
    public static void main(String... args) {
        initLogging();
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
