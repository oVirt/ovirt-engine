package org.ovirt.engine.core.common.utils.cinderlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

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
    private static final String CINDERLIB_DB_USER = "CINDERLIB_DB_USER";
    private static final String CINDERLIB_DB_PASSWORD = "CINDERLIB_DB_PASSWORD";
    private static final String CINDERLIB_DB_HOST= "CINDERLIB_DB_HOST";
    private static final String CINDERLIB_DB_PORT="CINDERLIB_DB_PORT";
    private static final String CINDERLIB_DB_DATABASE = "CINDERLIB_DB_DATABASE";
    private final String urlTemplate = "postgresql+psycopg2://%s:%s@%s:%s/%s";
    private String url;
    private File cinderlibDir;

    @PostConstruct
    private void init() {
        url = String.format(urlTemplate, config.getProperty(CINDERLIB_DB_USER),
                config.getProperty(CINDERLIB_DB_PASSWORD),
                config.getProperty(CINDERLIB_DB_HOST),
                config.getProperty(CINDERLIB_DB_PORT),
                config.getProperty(CINDERLIB_DB_DATABASE));
        cinderlibDir = Paths.get(config.getUsrDir().getAbsolutePath() + CINDERLIB_DIR).toFile();
    }

    public CinderlibReturnValue runCommand(CinderlibCommand command, CinderlibCommandParameters params)
            throws Exception {
        ProcessBuilder cinderlibProcessBuilder = new ProcessBuilder()
                .directory(cinderlibDir)
                .command(generateCommand(command, params))
                .redirectErrorStream(true);

        Process process = cinderlibProcessBuilder.start();
        String output = getOutput(process, params.getCorrelationId());
        if (!process.waitFor(Config.getValue(ConfigValues.CinderlibCommandTimeoutInMinutes), TimeUnit.MINUTES)) {
            throw new Exception("cinderlib call timed out");
        }
        CinderlibReturnValue returnValue = new CinderlibReturnValue(process.exitValue(), output);
        if (!returnValue.getSucceed()) {
            log.error("cinderlib execution failed: {}", output);
        } else {
            log.info("cinderlib output: {}", output);
        }
        return returnValue;
    }

    private String getOutput(Process process, String correlationId) throws IOException {
        final String ERROR_PREFIX = "error";
        String output;
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while ((output = br.readLine()) != null) {
            if (output.startsWith(ERROR_PREFIX)) {
                return output.substring(ERROR_PREFIX.length() + 1);
            } else if (output.startsWith(correlationId)) {
                return output.substring(correlationId.length() + 1);
            }

            log.debug("ignored cinderlib output: {}", output);
        }

        return "";
    }

    private List<String> generateCommand(CinderlibCommand command, CinderlibCommandParameters params) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(CINDERLIB_PREFIX);
        commandArgs.add(command.toString());
        commandArgs.add(params.getDriverInfo());
        commandArgs.add(url);
        commandArgs.add(params.getCorrelationId());
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
        GET_CONNECTION_INFO("get_connection_info"),
        STORAGE_STATS("storage_stats"),
        CLONE_VOLUME("clone_volume"),
        CREATE_SNAPSHOT("create_snapshot"),
        REMOVE_SNAPSHOT("remove_snapshot"),
        CREATE_VOLUME_FROM_SNAPSHOT("create_volume_from_snapshot");

        private final String commandName;

        CinderlibCommand(String commandName) {
            this.commandName = commandName;
        }

        public String toString() {
            return this.commandName;
        }
    }

}
