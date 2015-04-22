package org.ovirt.engine.exttool.core;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ExtensionsToolExecutor {

    private static String PROGRAM_NAME = System.getProperty("org.ovirt.engine.exttool.core.programName");
    private static String PACKAGE_NAME = System.getProperty("org.ovirt.engine.exttool.core.packageName");
    private static String PACKAGE_VERSION = System.getProperty("org.ovirt.engine.exttool.core.packageVersion");
    private static String PACKAGE_DISPLAY_NAME = System.getProperty("org.ovirt.engine.exttool.core.packageDisplayName");
    private static String ENGINE_ETC = System.getProperty("org.ovirt.engine.exttool.core.engineEtc");

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExtensionsToolExecutor.class);
    private static final Logger OVIRT_LOGGER = Logger.getLogger("org.ovirt");

    public static void main(String... args) {
        int exitStatus = 1;
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(args));

        try {
            setupLogger();
            ArgumentsParser parser;
            try (InputStream stream = ExtensionsToolExecutor.class.getResourceAsStream("arguments.properties")) {
                parser = new ArgumentsParser(stream, "core");
            }
            parser.parse(cmdArgs);
            Map<String, Object> argMap = parser.getParsedArgs();
            setupLogger(argMap);

            Map<String, ModuleService> moduleServices = loadModules(ModuleService.class);
            if((Boolean)argMap.get("help") || (cmdArgs.size() > 0 && cmdArgs.get(0).equals("help"))) {
                System.out.format(
                    "usage: %s",
                    parser.getUsage()
                        .replace("@PROGRAM_NAME@", PROGRAM_NAME)
                        .replace("@MODULE_LIST@", getModules(moduleServices)
                    )
                );
                throw new ExitException("Help", 0);
            } else if((Boolean)argMap.get("version")) {
                System.out.format("%s-%s (%s)%n", PACKAGE_NAME, PACKAGE_VERSION, PACKAGE_DISPLAY_NAME);
                throw new ExitException("Version", 0);
            }
            if(!parser.getErrors().isEmpty()) {
                for(Throwable t : parser.getErrors()) {
                    logger.error(t.getMessage());
                    logger.debug(t.getMessage(), t);
                }
                throw new ExitException("Parsing error", 1);
            }

            if (cmdArgs.size() < 1) {
                logger.error("Please provide module.");
                throw new ExitException("Module not provided", 1);
            }
            String module = cmdArgs.get(0);
            ModuleService moduleService = moduleServices.get(module);
            if (moduleService == null) {
                logger.error("No such '{}' module exists.", module);
                throw new ExitException(1);
            }
            moduleService.parseArguments(cmdArgs);
            loadExtensions(moduleService, argMap);
            moduleService.run();
        } catch(ExitException e) {
            logger.debug(e.getMessage(), e);
            exitStatus = e.getExitCode();
        } catch (Throwable t) {
            String message = t.getMessage() != null ? t.getMessage() : t.getClass().getName();
            logger.error(message);
            logger.debug(message, t);
        }
        logger.debug("Exiting with status '{}'", exitStatus);
        System.exit(exitStatus);
    }

    private static void loadExtensions(ModuleService moduleService, Map<String, Object> argMap) {
        ExtensionsManager extensionsManager = new ExtensionsManager();
        Map<String, ExtensionProxy> proxies = moduleService.getContext().get(ModuleService.EXTENSIONS_MAP);
        List<File> files = (List<File>)argMap.get("extension-file");

        if(files == null) {
            files = listFiles(
                ((String) argMap.get("extensions-dir")).replace("@ENGINE_ETC@", ENGINE_ETC),
                "properties"
            );
        }

        for(File f : files) {
            proxies.put(extensionsManager.load(f), null);
        }

        for(Map.Entry<String, ExtensionProxy> entry : proxies.entrySet()) {
            extensionsManager.initialize(entry.getKey());
            entry.setValue(extensionsManager.getExtensionByName(entry.getKey()));
        }
    }

    private static Map<String, ModuleService> loadModules(Class cls) {
        Map<String, ModuleService> modules = new HashMap<>();
        if(cls != null) {
            Map<String, ExtensionProxy> proxies = new HashMap<>();
            ExtMap context = new ExtMap()
                .mput(ModuleService.EXTENSIONS_MAP, proxies)
                .mput(ModuleService.PROGRAM_NAME, PROGRAM_NAME);

            ServiceLoader<ModuleService> loader = ServiceLoader.load(cls);
            for (ModuleService module : loader) {
                modules.put(module.getName(), module);
                module.setContext(context);
            }
        }
        logger.debug("Loaded modules: {}", modules.keySet());

        return modules;
    }

    private static void setupLogger() {
        String logLevel = System.getenv("OVIRT_LOGGING_LEVEL");
        OVIRT_LOGGER.setLevel(
            logLevel != null ? Level.parse(logLevel) : Level.INFO
        );
    }

    private static void setupLogger(Map<String, Object> args) throws IOException {
        Logger logger = Logger.getLogger("");
        String logfile = (String)args.get("log-file");
        if(logfile != null) {
            FileHandler fh = new FileHandler(logfile);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        }

        OVIRT_LOGGER.setLevel((Level)args.get("log-level"));
    }

    private static List<File> listFiles(String directory, String suffix) {
        File dir = new File(directory);
        List<File> propFiles = new ArrayList<>();
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (File file : dirFiles) {
                if (file.getName().endsWith(suffix)) {
                    propFiles.add(file);
                }
            }
        }

        return propFiles;
    }

    private static String getModules(Map<String, ModuleService> modules) {
        StringBuilder sb = new StringBuilder();
        for(ModuleService entry : modules.values()) {
            sb.append(
                String.format("  %-10s - %s%n", entry.getName(), entry.getDescription())
            );
        }
        return sb.toString();
    }

}
