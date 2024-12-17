
/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.core.common.utils.ansible;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnsibleExecutor {

    public static final String DEFAULT_LOG_DIRECTORY = "ansible";

    private static Logger log = LoggerFactory.getLogger(AnsibleExecutor.class);
    private static final int POLL_INTERVAL = 3000;
    private static final String SSH_TIMEOUT = "SSH timeout waiting for response";
    private static Pattern taskPattern =
            Pattern.compile("(?<title>.*) \\[(?<severity>NORMAL|WARNING|ERROR|ALERT)\\] (?<message>.*)");

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private AnsibleClientFactory ansibleClientFactory;

    @Inject
    private AnsibleCommandLogFileFactory ansibleCommandLogFileFactory;

    /**
     * Executes ansible-playbook command. Default timeout is specified by ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT variable
     * in engine.conf.
     *
     * @param commandConfig
     *            the config of command to be executed
     * @return return code of ansible-playbook
     */
    public AnsibleReturnValue runCommand(AnsibleCommandConfig commandConfig) {
        return runCommand(commandConfig, 0, false);
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig commandConfig, boolean async) {
        return runCommand(commandConfig, 0, async);
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig command, int timeout) {
        return runCommand(command, timeout, false);
    }

    /**
     * Executes ansible-playbook command.
     *
     * @param command
     *            the config of command to be executed
     * @param timeout
     *            timeout in minutes to wait for command to finish
     * @param async
     *            true if the playbook executed asynchronously, false otherwise
     * @return return code of ansible-playbook
     */
    public AnsibleReturnValue runCommand(AnsibleCommandConfig command, int timeout, boolean async) {
        return runCommand(
                command,
                timeout,
                (String taskName, String eventUrl) -> {
                    AuditLogable logable = createAuditLogable(command, taskName);
                    auditLogDirector.log(logable, AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION);
                },
                async
        );
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig command, BiConsumer<String, String> fn) {
        int timeout = EngineLocalConfig.getInstance().getInteger("ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT");
        return runCommand(command, timeout, fn, false);
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig command,
            Logger originLogger,
            BiConsumer<String, String> eventUrlConsume) {
        return runCommand(command, originLogger, eventUrlConsume, false);
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig command,
            Logger originLogger,
            BiConsumer<String, String> eventUrlConsumer,
            boolean async) {
        int timeout = EngineLocalConfig.getInstance().getInteger("ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT");
        return runCommand(
                command,
                timeout,
                (String taskName, String eventUrl) -> {
                    AuditLogType alType = AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION;
                    String message = taskName;
                    Matcher matcher = taskPattern.matcher(taskName);
                    if (matcher.find()) {
                        message = String.format("%s %s", matcher.group("title"), matcher.group("message"));
                        AuditLogSeverity severity = AuditLogSeverity.valueOf(matcher.group("severity"));
                        switch (severity) {
                            case ERROR:
                                alType = AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION_ERROR;
                                break;
                            case ALERT:
                                alType = AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION_ALERT;
                                break;
                            case WARNING:
                                alType = AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION_WARNING;
                                break;
                            case NORMAL:
                            default:
                                alType = AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION;
                                break;
                        }
                    }

                    if (!message.contains("debug msg:")) {
                        AuditLogable logable = createAuditLogable(command, message);
                        auditLogDirector.log(logable, alType);
                    }

                    try {
                        eventUrlConsumer.accept(taskName, eventUrl);
                    } catch (Exception ex) {
                        originLogger.error("Error: {}", ex.getMessage());
                        originLogger.debug("Exception: ", ex);
                    }
                },
                async);
    }

    public AnsibleReturnValue runCommand(AnsibleCommandConfig commandConfig, int timeout, BiConsumer<String, String> fn, boolean async) {
        if (timeout <= 0) {
            timeout = EngineLocalConfig.getInstance().getInteger("ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT");
        }

        AnsibleReturnValue ret = new AnsibleReturnValue(AnsibleReturnCode.ERROR);

        String playUuid = null;
        AnsibleRunnerClient runnerClient = null;
        AnsibleRunnerLogger runnerLogger = null;
        try {
            runnerClient = ansibleClientFactory.create(commandConfig);
            runnerLogger = ansibleCommandLogFileFactory.create(commandConfig);
            ret.setLogFile(runnerLogger.getLogFile());
            playUuid = commandConfig.getUuid().toString();
            ret.setPlayUuid(playUuid);
            ret.setStdout(String.format("%1$s/%2$s/artifacts/%2$s/stdout", AnsibleConstants.ANSIBLE_RUNNER_PATH, playUuid));
            ret.setLastEventId(0);
            List<String> command = commandConfig.build();

            // Run the playbook:
            runnerClient.runPlaybook(command, timeout, playUuid);

            if (async) {
                ret.setAnsibleReturnCode(AnsibleReturnCode.OK);
                return ret;
            }

            runnerClient.artifactHandler(ret, timeout, fn, runnerLogger);
        } catch (InventoryException ex) {
            String message = ex.getMessage();
            log.error("Error executing playbook: {}", message);
            log.debug("InventoryException: ", ex);
            ret.setStderr(message);
            ret.setAnsibleReturnCode(message.contains(SSH_TIMEOUT) ? AnsibleReturnCode.UNREACHABLE : AnsibleReturnCode.FAIL);
        } catch (Exception ex) {
            log.error("Exception: {}", ex.getMessage());
            log.debug("Exception: ", ex);
            ret.setStderr(ex.getMessage());
        } finally {
            // Make sure all events are proccessed even in case of failure:
            if (playUuid != null && runnerClient != null && runnerLogger != null && !async) {
                runnerClient.processEvents(ret, fn, runnerLogger);
            }
        }

        return ret;
    }

    private AuditLogable createAuditLogable(AnsibleCommandConfig command, String taskName) {
        if (command.host() == null) {
            return AuditLogableImpl.createEvent(
                command.correlationId(),
                Map.of("Message", taskName, "PlayAction", command.playAction())
            );
        }
        return AuditLogableImpl.createHostEvent(
            command.host(),
            command.correlationId(),
            Map.of("Message", taskName, "PlayAction", command.playAction())
        );
    }
}
