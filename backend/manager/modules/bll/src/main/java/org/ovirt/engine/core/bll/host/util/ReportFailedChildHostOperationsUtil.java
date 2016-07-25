package org.ovirt.engine.core.bll.host.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@Singleton
public class ReportFailedChildHostOperationsUtil {

    private static final String FAILED_HOSTS_VAR = "failedHosts";

    private final AuditLogDirector auditLogDirector;
    private final HostIdToLoggableNameFunction hostIdToLoggableNameFunction;

    @Inject
    ReportFailedChildHostOperationsUtil(
            AuditLogDirector auditLogDirector,
            HostIdToLoggableNameFunction hostIdToLoggableNameFunction) {
        this.auditLogDirector = Objects.requireNonNull(auditLogDirector);
        this.hostIdToLoggableNameFunction = Objects.requireNonNull(hostIdToLoggableNameFunction);
    }

    public void setFailedHosts(CommandBase command) {
        final List<String> failedHostNames = findFailedChildCommandEntities(command)
                .map(vdsCommand -> ((VdsActionParameters) vdsCommand.getCommandParameters()).getVdsId())
                .map(hostIdToLoggableNameFunction)
                .collect(Collectors.toList());
        command.setCustomCommaSeparatedValues(FAILED_HOSTS_VAR, failedHostNames);
    }

    private Stream<CommandEntity> findFailedChildCommandEntities(CommandBase command) {
        final List<CommandEntity> childCommandEntities =
                CommandCoordinatorUtil.findChildCommands(command.getCommandId());

        return childCommandEntities
                .stream()
                .filter(childCommand -> !childCommand.getReturnValue().getSucceeded());
    }

}
