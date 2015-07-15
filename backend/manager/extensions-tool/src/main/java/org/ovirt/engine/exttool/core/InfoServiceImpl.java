package org.ovirt.engine.exttool.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.slf4j.LoggerFactory;

public class InfoServiceImpl implements ModuleService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InfoServiceImpl.class);

    private interface Logic {
        void execute(ExtMap context, Map<String, Object> argMap);
    }

    private enum Action {
        CONFIGURATION(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy extension = getExtensionsManager(context).getExtensionByName((String)argMap.get("extension-name"));
                    Collection<?> sensitive = extension.getContext().<Collection<?>>get(Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS);
                    for (Map.Entry<Object, Object> entry : extension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).entrySet()) {
                        log.info("    {}: {}", entry.getKey(), sensitive.contains(entry.getKey()) ? "***" : entry.getValue());
                    }
                }
            }
        ),
        CONTEXT(
            new Logic() {
                private List<ExtKey> IGNORE_KEYS = Arrays.asList(
                    Base.ContextKeys.GLOBAL_CONTEXT,
                    Base.ContextKeys.CONFIGURATION,
                    Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS,
                    ExtensionsManager.TRACE_LOG_CONTEXT_KEY
                );

                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    ExtensionProxy extension = getExtensionsManager(context).getExtensionByName((String)argMap.get("extension-name"));
                    for (Map.Entry<ExtKey, Object> entry : extension.getContext().entrySet()) {
                        if (IGNORE_KEYS.contains(entry.getKey())) {
                            continue;
                        }
                        log.info("    {}: {}", entry.getKey().getUuid().getName(), entry.getValue());
                    }
                }
            }
        ),
        LIST_EXTENSIONS(
            new Logic() {
                @Override
                public void execute(ExtMap context, Map<String, Object> argMap) {
                    for (ExtensionProxy extension : getExtensionsManager(context).getExtensions()) {
                        ExtMap extContext = extension.getContext();
                        log.info(
                            "Extension name={} type={} version={} notes={}",
                            extContext.get(Base.ContextKeys.INSTANCE_NAME),
                            extContext.get(Base.ContextKeys.EXTENSION_NAME),
                            extContext.get(Base.ContextKeys.VERSION),
                            extContext.get(Base.ContextKeys.EXTENSION_NOTES)
                        );
                    }
                }
            }
        );

        private Logic logic;

        private Action(Logic logic) {
            this.logic = logic;
        }

        Map<String, Object> parse(Map<String, String> substitutions, Properties props, List<String> moduleArgs) {
            ArgumentsParser parser = new ArgumentsParser(props, moduleArgs.remove(0));
            parser.getSubstitutions().putAll(substitutions);
            parser.parse(moduleArgs);
            Map<String, Object> argMap = parser.getParsedArgs();

            if((Boolean)argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ExitException("Help", 0);
            }
            if(!parser.getErrors().isEmpty()) {
                for(Throwable t : parser.getErrors()) {
                    log.error(t.getMessage());
                }
                throw new ExitException("Parsing error", 1);
            }
            if (moduleArgs.size() != 0) {
                log.error("Extra parameters in command-line");
                throw new ExitException("Parsing error", 1);
            }

            return argMap;
        }

        void execute(ExtMap context, Map<String, Object> argMap) {
            logic.execute(context, argMap);
        }
    }

    private ExtMap context;
    private Action action;
    private Map<String, Object> argMap;

    private static ExtensionsManager getExtensionsManager(ExtMap context) {
        return (ExtensionsManager)context.get(ContextKeys.EXTENSION_MANAGER);
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "General information.";
    }

    @Override
    public void setContext(ExtMap context) {
        this.context = context;
    }

    @Override
    public ExtMap getContext() {
        return context;
    }

    @Override
    public void parseArguments(List<String> args) throws Exception {
        args.remove(0);

        Properties props = new Properties();
        try (
            InputStream in = InfoServiceImpl.class.getResourceAsStream("info.properties");
            Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        ) {
            props.load(reader);
        }
        Map<String, String> substitutions = (Map)context.get(ContextKeys.CLI_PARSER_SUBSTITUTIONS);
        ArgumentsParser parser = new ArgumentsParser(props, "module");
        parser.getSubstitutions().putAll(substitutions);
        parser.parse(args);
        Map<String, Object> moduleArgs = parser.getParsedArgs();

        if((Boolean)moduleArgs.get("help")) {
            System.out.format("Usage: %s", parser.getUsage());
            throw new ExitException("Help", 0);
        }
        if(!parser.getErrors().isEmpty()) {
            for(Throwable t : parser.getErrors()) {
                log.error(t.getMessage());
            }
            throw new ExitException("Parsing error", 1);
        }

        if (args.size() < 1) {
            log.error("Action not provided");
            throw new ExitException("Action not provided", 1);
        }

        try {
            action = Action.valueOf(args.get(0).toUpperCase().replace("-", "_"));
        } catch(IllegalArgumentException e) {
            log.error("Invalid action '{}'", args.get(0));
            throw new ExitException("Invalid action", 1);
        }

        argMap = action.parse(substitutions, props, args);
    }

    @Override
    public void run() throws Exception {
        action.execute(context, argMap);
    }
}
