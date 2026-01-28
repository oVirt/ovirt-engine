package org.ovirt.engine.core.common.utils.managedblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ManagedBlockExecutor {
    EngineLocalConfig config = EngineLocalConfig.getInstance();
    private static final Logger log = LoggerFactory.getLogger(ManagedBlockExecutor.class);
    private static final String CINDERLIB_ADAPTER = "cinderlib-client.py";
    private static final String MANAGEDBLOCK_DIR = "/managedblock";
    private static final String MANAGEDBLOCK_DB_USER = "MANAGEDBLOCK_DB_USER";
    private static final String MANAGEDBLOCK_DB_PASSWORD = "MANAGEDBLOCK_DB_PASSWORD";
    private static final String MANAGEDBLOCK_DB_HOST = "MANAGEDBLOCK_DB_HOST";
    private static final String MANAGEDBLOCK_DB_PORT = "MANAGEDBLOCK_DB_PORT";
    private static final String MANAGEDBLOCK_DB_DATABASE = "MANAGEDBLOCK_DB_DATABASE";
    private final String urlTemplate = "postgresql+psycopg2://%s:%s@%s:%s/%s";
    private String url;
    private File managedBlockDir;

    @PostConstruct
    private void init() {
        url = String.format(urlTemplate, config.getProperty(MANAGEDBLOCK_DB_USER),
                config.getProperty(MANAGEDBLOCK_DB_PASSWORD),
                config.getProperty(MANAGEDBLOCK_DB_HOST),
                config.getProperty(MANAGEDBLOCK_DB_PORT),
                config.getProperty(MANAGEDBLOCK_DB_DATABASE));
        managedBlockDir = Paths.get(config.getUsrDir().getAbsolutePath() + MANAGEDBLOCK_DIR).toFile();
    }

    public ManagedBlockReturnValue runCommand(ManagedBlockCommand command, ManagedBlockCommandParameters params)
            throws Exception {
        ProcessBuilder commandProcessBuilder = new ProcessBuilder()
                .directory(managedBlockDir)
                .command(generateCommand(command, params))
                .redirectErrorStream(true);

        Process process = commandProcessBuilder.start();
        String output = getOutput(process, params.getCorrelationId());
        if (!process.waitFor(Config.getValue(ConfigValues.ManagedBlockCommandTimeoutInMinutes), TimeUnit.MINUTES)) {
            throw new Exception("managed block call timed out");
        }
        ManagedBlockReturnValue returnValue = new ManagedBlockReturnValue(process.exitValue(), output);
        if (!returnValue.getSucceed()) {
            log.error("managed block execution failed: {}", output);
        } else {
            log.info("managed block output: {}", output);
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

            log.debug("ignored managed block output: {}", output);
        }

        return "";
    }

    private List<String> generateCommand(ManagedBlockCommand command, ManagedBlockCommandParameters params) throws Exception {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(getStorageAdapter(params.getDriverInfo()));
        commandArgs.add(command.toString());
        commandArgs.add(params.getDriverInfo());
        commandArgs.add(url);
        commandArgs.add(params.getCorrelationId());
        commandArgs.addAll(params.getExtraParams());

        return commandArgs;
    }

    public String getStorageAdapter(String driverInfo) throws Exception {
        String adapterFile = CINDERLIB_ADAPTER;
        try {
            Map<String, Object> info = JsonHelper.jsonToMap(driverInfo);
            if (info.containsKey("adapter")) {
                adapterFile = info.get("adapter") + "-adapter";
            }
        } catch (IOException e) {
            log.warn("Failed to parse driver info JSON: {}, using default adapter", e.getMessage());
        }
        File adapterPath = new File(managedBlockDir, adapterFile);
        if (!adapterPath.exists()) {
            throw new Exception("Storage adapter not found at " + adapterPath.getAbsolutePath());
        }
        return adapterPath.getAbsolutePath();
    }


    public enum ManagedBlockCommand {
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

        ManagedBlockCommand(String commandName) {
            this.commandName = commandName;
        }

        public String toString() {
            return this.commandName;
        }
    }

}
