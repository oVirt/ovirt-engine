package org.ovirt.engine.core.common.utils.cinderlib;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CinderlibExecutor {
    EngineLocalConfig config = EngineLocalConfig.getInstance();
    private static final Logger log = LoggerFactory.getLogger(CinderlibExecutor.class);
    private static final String CINDERLIB_PREFIX = "./cinderlib-client.py";
    private static final String CINDERLIB_DIR = "/cinderlib";
    private static final String ENGINE_DB_USER = "ENGINE_DB_USER";
    private static final String ENGINE_DB_PASSWORD = "ENGINE_DB_PASSWORD";
    private static final String ENGINE_DB_HOST= "ENGINE_DB_HOST";
    private static final String ENGINE_DB_PORT="ENGINE_DB_PORT";
    private final String urlTemplate = "postgresql+psycopg2://%s:%s@%s:%s/cinder";
    private String url;
    private File cinderlibDir;

    @PostConstruct
    private void init() {
        url = String.format(urlTemplate, config.getProperty(ENGINE_DB_USER),
                config.getProperty(ENGINE_DB_PASSWORD),
                config.getProperty(ENGINE_DB_HOST),
                config.getProperty(ENGINE_DB_PORT));
        cinderlibDir = Paths.get(config.getUsrDir().getAbsolutePath() + CINDERLIB_DIR).toFile();
    }

    public CinderlibReturnValue runCommand(CinderlibCommand command, CinderlibCommandParameters params)
            throws Exception {
        ProcessBuilder cinderlibProcessBuilder = new ProcessBuilder()
                .directory(cinderlibDir)
                .command(generateCommand(command, params))
                .redirectErrorStream(true);

        Process process = cinderlibProcessBuilder.start();
        String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        log.info("cinderlib output: {}", output);
        if (!process.waitFor(Config.getValue(ConfigValues.CinderlibCommandTimeoutInMinutes), TimeUnit.MINUTES)) {
            throw new Exception("cinderlib call timed out");
        }

        CinderlibReturnValue returnValue = new CinderlibReturnValue(process.exitValue(), output);
        return returnValue;
    }

    private List<String> generateCommand(CinderlibCommand command, CinderlibCommandParameters params) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(CINDERLIB_PREFIX);
        commandArgs.add(command.toString());
        commandArgs.add(params.getDriverInfo());
        commandArgs.add(url);
        commandArgs.addAll(params.getExtraParams());

        return commandArgs;
    }


    public enum CinderlibCommand {
        CREATE_VOLUME("create_volume"),
        DELETE_VOLUME("delete_volume"),
        CONNECT_VOLUME("connect_volume"),
        DISCONNECT_VOLUME("disconnect_volume"),
        EXTEND_VOLUME("extend_volume"),
        SAVE_DEVICE("save_device"),
        STORAGE_STATS("storage_stats");

        private final String commandName;

        CinderlibCommand(String commandName) {
            this.commandName = commandName;
        }

        public String toString() {
            return this.commandName;
        }
    }

}
