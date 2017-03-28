package org.ovirt.engine.exttool.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoServiceImpl implements ModuleService {

    private static final Logger log = LoggerFactory.getLogger(InfoServiceImpl.class);

    private static final List<ExtKey> CONTEXT_IGNORE_KEYS = Arrays.asList(
            Base.ContextKeys.GLOBAL_CONTEXT,
            Base.ContextKeys.CONFIGURATION,
            Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS,
            ExtensionsManager.TRACE_LOG_CONTEXT_KEY
    );

    @FunctionalInterface
    private interface Logic {
        void execute(InfoServiceImpl module);
    }

    private enum Action {
        CONFIGURATION(
            module -> {
                ExtensionProxy extension = module.getExtensionsManager().getExtensionByName((String)module.argMap.get("extension-name"));
                Collection<?> sensitive = extension.getContext().get(Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS);


                Map<Object, Object> config = extension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION);
                Collection<Object> keys = new HashSet<>(config.keySet());
                if (module.argMap.get("key") != null) {
                    keys.retainAll((List<String>)module.argMap.get("key"));
                }

                for (Object key : keys) {
                    module.output(
                        ((String)module.argMap.get("format")).replace(
                            "{key}",
                            String.format("%s", key)
                        ).replace(
                            "{value}",
                            sensitive.contains(key) ? "***" : String.format("%s", config.get(key))
                        ),
                        "    "
                    );
                }
            }
        ),
        CONTEXT(
            module -> {
                ExtensionProxy extension = module.getExtensionsManager().getExtensionByName((String)module.argMap.get("extension-name"));
                Collection<ExtKey> keys = new HashSet<>(extension.getContext().keySet());
                if (module.argMap.get("key") != null) {
                    Collection<ExtKey> k = new HashSet<>();
                    for (String uuid : (List<String>)module.argMap.get("key")) {
                        k.add(new ExtKey("Unknown", Object.class, uuid));
                    }
                    keys.retainAll(k);
                }
                for (ExtKey key : keys) {
                    if (CONTEXT_IGNORE_KEYS.contains(key)) {
                        continue;
                    }
                    if ((key.getFlags() & ExtKey.Flags.SKIP_DUMP) != 0) {
                        continue;
                    }

                    module.output(
                        ((String)module.argMap.get("format")).replace(
                            "{key}",
                            key.getUuid().getUuid().toString()
                        ).replace(
                            "{name}",
                            key.getUuid().getName()
                        ).replace(
                            "{value}",
                            (key.getFlags() & ExtKey.Flags.SENSITIVE) != 0 ? "***" : extension.getContext().get(key).toString()
                        ),
                        "    "
                    );
                }
            }
        ),
        LIST_EXTENSIONS(
            module -> {
                for (ExtensionProxy extension : module.getExtensionsManager().getExtensions()) {
                    ExtMap extContext = extension.getContext();
                    module.output(
                        ((String)module.argMap.get("format")).replace(
                            "{instance}",
                            extContext.<String>get(Base.ContextKeys.INSTANCE_NAME, "")
                        ).replace(
                            "{name}",
                            extContext.<String>get(Base.ContextKeys.EXTENSION_NAME, "")
                        ).replace(
                            "{version}",
                            extContext.<String>get(Base.ContextKeys.VERSION, "")
                        ).replace(
                            "{license}",
                            extContext.<String>get(Base.ContextKeys.LICENSE, "")
                        ).replace(
                            "{notes}",
                            extContext.<String>get(Base.ContextKeys.EXTENSION_NOTES, "")
                        ),
                        ""
                    );
                }
            }
        );

        private Logic logic;

        private Action(Logic logic) {
            this.logic = logic;
        }

        Map<String, Object> parse(Map<String, String> substitutions, Properties props, List<String> actionArgs) {
            ArgumentsParser parser = new ArgumentsParser(props, actionArgs.remove(0));
            parser.getSubstitutions().putAll(substitutions);
            parser.parse(actionArgs);
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
            if (actionArgs.size() != 0) {
                log.error("Extra parameters in command-line");
                throw new ExitException("Parsing error", 1);
            }

            return argMap;
        }

        void execute(InfoServiceImpl module) {
            logic.execute(module);
        }
    }

    private ExtMap context;
    private Action action;
    private Map<String, Object> argModuleMap;
    private Map<String, Object> argMap;

    private ExtensionsManager getExtensionsManager() {
        return (ExtensionsManager)context.get(ContextKeys.EXTENSION_MANAGER);
    }

    private void output(String s, String logIndent) {
        if ("log".equals(argModuleMap.get("output"))) {
            log.info("{}{}", logIndent, s);
        } else if ("stdout".equals(argModuleMap.get("output"))) {
            System.out.println(s);
        }
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
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)
        ) {
            props.load(reader);
        }
        Map<String, String> substitutions = context.get(ContextKeys.CLI_PARSER_SUBSTITUTIONS);
        ArgumentsParser parser = new ArgumentsParser(props, "module");
        parser.getSubstitutions().putAll(substitutions);
        parser.parse(args);
        argModuleMap = parser.getParsedArgs();

        if((Boolean)argModuleMap.get("help")) {
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
        action.execute(this);
    }
}
