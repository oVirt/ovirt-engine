package org.ovirt.engine.core.common.utils.ansible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class AnsibleRunnerLogger {

    private static Logger logger = LoggerFactory.getLogger(AnsibleRunnerHttpClient.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

    private Path logFile;

    public AnsibleRunnerLogger(String logFile) {
        this(Paths.get(logFile));
    }

    public AnsibleRunnerLogger(Path logFile) {
        this.logFile = logFile;
    }

    public Path getLogFile() {
        return logFile;
    }

    public void log(String str, Object ...params) {
        if (str == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(ZonedDateTime.now().format(formatter));
        sb.append(" - ");
        sb.append(str);
        write(sb.toString(), params);
    }

    public void log(JsonNode node) {
        try {
            Object json = mapper.readValue(node.toString(), Object.class);
            StringBuilder sb = new StringBuilder(ZonedDateTime.now().format(formatter));
            sb.append(" - ");
            sb.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
            sb.append(NEW_LINE);
            write(sb.toString());
        } catch (IOException ex) {
            logger.error("Failed to parse node: {}", node.asText());
            logger.debug("Exception: ", ex);
        }
    }

    public void log(int index, JsonNode node) {
        try {
            Object json = mapper.readValue(node.toString(), Object.class);
            StringBuilder sb = new StringBuilder(ZonedDateTime.now().format(formatter));
            sb.append(" [");
            sb.append(index);
            sb.append("]: ");
            sb.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
            sb.append(NEW_LINE);
            write(sb.toString());
        } catch (IOException ex) {
            logger.error("Failed to parse node: {}", node.asText());
            logger.debug("Exception: ", ex);
        }
    }

    private void write(String str, Object... params) {
        if (params != null && params.length > 0) {
            str = String.format(str, params);
        }
        try {
            Files.write(logFile, str.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            Files.write(logFile, NEW_LINE.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            logger.error("Failed to write log event to Ansible runner service log: {}", ex.getMessage());
            logger.debug("Exception: ", ex);
        }
    }

    private void write(String str) {
        try {
            Files.write(logFile, str.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            Files.write(logFile, NEW_LINE.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            logger.error("Failed to write log event to Ansible runner service log: {}", ex.getMessage());
            logger.debug("Exception: ", ex);
        }
    }

}
