package org.ovirt.engine.exttool.logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.logger.Logger;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.ovirt.engine.exttool.core.ExitException;
import org.ovirt.engine.exttool.core.ModuleService;
import org.slf4j.LoggerFactory;

public class LoggerServiceImpl implements ModuleService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerServiceImpl.class);

    private ExtMap context;
    private Runnable actionMethod;
    private Map<String, Object> moduleArgs = new HashMap<>();
    private Map<String, Object> actionArgs = new HashMap<>();
    private Map<String, Runnable> actions = new HashMap<>();
    {
        actions.put(
            "log-record",
            new Runnable() {
                @Override
                public void run() {
                    logrecord();
                }
            }
        );
    }

    public LoggerServiceImpl() {
    }

    @Override
    public String getName() {
        return "logger";
    }

    @Override
    public String getDescription() {
        return "Logger module test logger extension configuration.";
    }

    @Override
    public void setContext(ExtMap context) {
        this.context = context;
    }

    @Override
    public ExtMap getContext() {
        return this.context;
    }

    @Override
    public void parseArguments(List<String> args) throws Exception {
        final Map<String, String> substitutions = new HashMap<>();
        substitutions.put("@PROGRAM_NAME@", (String) context.get(PROGRAM_NAME));
        substitutions.put("@ACTION_LIST@", StringUtils.join(actions.keySet(), ", "));

        args.remove(0);
        ArgumentsParser parser;
        try (InputStream stream = getClass().getResourceAsStream("arguments.properties")) {
            parser = new ArgumentsParser(stream, "module");
            parser.getSubstitutions().putAll(substitutions);
        }
        parser.parse(args);
        moduleArgs = parser.getParsedArgs();
        if((Boolean)moduleArgs.get("help")) {
            printUsage(parser);
            throw new ExitException("Help", 0);
        }
        if(!parser.getErrors().isEmpty()) {
            for(Throwable t : parser.getErrors()) {
                logger.error(t.getMessage());
                logger.debug(t.getMessage(), t);
            }
            throw new ExitException("Parsing error", 1);
        }
        if(args.size() < 1) {
            logger.error("Please provide action.");
            throw new ExitException("Action not provided", 1);
        }

        String action = args.remove(0);
        actionMethod = actions.get(action);
        if(actionMethod == null) {
            throw new IllegalArgumentException(
                String.format("No such action '%1$s' exists for module '%2$s'", action, getName())
            );
        }
        try (InputStream stream = getClass().getResourceAsStream("arguments.properties")) {
            parser = new ArgumentsParser(stream, action);
            parser.getSubstitutions().putAll(substitutions);
        }
        parser.parse(args);
        actionArgs = parser.getParsedArgs();

        if((Boolean)actionArgs.get("help")) {
            printUsage(parser);
            throw new ExitException("Help", 0);
        }
        if(!parser.getErrors().isEmpty()) {
            for(Throwable t : parser.getErrors()) {
                logger.error(t.getMessage());
                logger.debug(t.getMessage(), t);
            }
            throw new ExitException("Parsing error", 1);
        }
        logger.debug(
            "Using action arguments: extension-name='{}' message='{}' level={}",
            actionArgs.get("extension-name"),
            actionArgs.get("message"),
            actionArgs.get("level")
        );
    }

    @Override
    public void run() throws Exception {
        actionMethod.run();
    }

    private void printUsage(ArgumentsParser parser) {
        System.out.format("Usage: %s",  parser.getUsage());
    }

    private void logrecord() {
        String extensionName = (String)actionArgs.get("extension-name");
        ExtensionProxy proxy = ((Map<String, ExtensionProxy>) context.get(EXTENSIONS_MAP)).get(extensionName);
        if(proxy == null) {
            throw new RuntimeException(String.format("Extension name '%1$s' not found", extensionName));
        }
        LogRecord logRecord = new LogRecord(
            (Level) actionArgs.get("level"),
            (String) actionArgs.get("message")
        );
        logRecord.setLoggerName((String) actionArgs.get("logger-name"));

        logger.debug("Invoking command {}", Logger.InvokeCommands.PUBLISH);
        proxy.invoke(
            new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Logger.InvokeCommands.PUBLISH
            ).mput(
                Logger.InvokeKeys.LOG_RECORD,
                logRecord
            )
        );

        logger.debug("Invoking command {}", Logger.InvokeCommands.FLUSH);
        proxy.invoke(
            new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Logger.InvokeCommands.FLUSH
            )
        );

        logger.debug("Invoking command {}", Logger.InvokeCommands.CLOSE);
        proxy.invoke(
            new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Logger.InvokeCommands.CLOSE
            )
        );
        logger.info("Log-record action completed");
    }
}
