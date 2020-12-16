package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AnsibleCommandParameters;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerLogger;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(AnsibleCallback.class)
public class AnsibleCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(AnsibleCallback.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private AnsibleRunnerHttpClient runnerClient;

    @Inject
    private VdsDao vdsDao;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<AnsibleCommandParameters> command = getCommand(cmdId);
        String playUuid = command.getParameters().getPlayUuid();
        StringBuilder stdout = command.getParameters().getStringBuilder();
        runnerClient.setLogger(new AnsibleRunnerLogger(command.getParameters().getLogFile()));
        BiConsumer<String, String> fn = (String taskName, String eventUrl) -> {
            AuditLogable logable = createAuditLogable(command, taskName);
            auditLogDirector.log(logable, AuditLogType.ANSIBLE_RUNNER_EVENT_NOTIFICATION);
            if (stdout != null) {
                try {
                    stdout.append(runnerClient.getCommandStdout(eventUrl));
                } catch (Exception ex) {
                    log.error("Error: {}", ex.getMessage());
                    log.debug("Exception: ", ex);
                }
            }
        };

        AnsibleReturnValue ret = new AnsibleReturnValue(AnsibleReturnCode.ERROR);
        ret.setLogFile(runnerClient.getLogger().getLogFile());
        String msg = "";
        int totalEvents;
        // Get the current status of the playbook:
        AnsibleRunnerHttpClient.PlaybookStatus playbookStatus = runnerClient.getPlaybookStatus(playUuid);
        String status = playbookStatus.getStatus();
        msg = playbookStatus.getMsg();
        // Process the events if the playbook is running:
        totalEvents = runnerClient.getTotalEvents(playUuid);

        if (msg.equalsIgnoreCase("running") || (msg.equalsIgnoreCase("successful")
                && command.getParameters().getLastEventId() < totalEvents)) {
            command.getParameters().setLastEventId(runnerClient.processEvents(
                    playUuid, command.getParameters().getLastEventId(), fn, msg, ret.getLogFile()));
            return;
        } else if (msg.equalsIgnoreCase("successful")) {
            log.info("Playbook (Play uuid = {}, command = {}) has completed!",
                    command.getParameters().getPlayUuid(), command.getActionType().name());
            // Exit the processing if playbook finished:
            ret.setAnsibleReturnCode(AnsibleReturnCode.OK);
            command.setSucceeded(true);
            command.setCommandStatus(CommandStatus.SUCCEEDED);
        } else if (status.equalsIgnoreCase("unknown")) {
            // ignore and continue:
            return;
        } else {
            // Playbook failed:
            command.setSucceeded(false);
            command.setCommandStatus(CommandStatus.FAILED);
        }
        command.persistCommand(command.getParameters().getParentCommand(), true);
    }

    private AuditLogable createAuditLogable(CommandBase<AnsibleCommandParameters> command, String taskName) {
        if (command.getParameters().getHostId() == null) {
            return AuditLogableImpl.createEvent(
                    command.getCorrelationId(),
                    Map.of("Message", taskName, "PlayAction", command.getParameters().getPlayAction())
            );
        }
        return AuditLogableImpl.createHostEvent(
                vdsDao.get(command.getParameters().getHostId()),
                command.getCorrelationId(),
                Map.of("Message", taskName, "PlayAction", command.getParameters().getPlayAction())
        );
    }

    private CommandBase<AnsibleCommandParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
    }
}
