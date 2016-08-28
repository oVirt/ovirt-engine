package org.ovirt.engine.core.bll.host.util;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
public class ReportFailedChildHostOperationsUtil {

    private static final String FAILED_HOSTS_VAR = "failedHosts";
    private static final String COMMA_SEPARATOR = ", ";

    private final HostIdToLoggableNameFunction hostIdToLoggableNameFunction;

    @Inject
    ReportFailedChildHostOperationsUtil(HostIdToLoggableNameFunction hostIdToLoggableNameFunction) {
        this.hostIdToLoggableNameFunction = Objects.requireNonNull(hostIdToLoggableNameFunction);
    }

    public void setFailedHosts(final CommandBase command) {
        final List<CommandEntity> failedChildCommandEntities = findFailedChildCommandEntities(command);
        final List<Guid> failedHostIds = LinqUtils.transformToList(failedChildCommandEntities,
                new Function<CommandEntity, Guid>() {
                    @Override
                    public Guid eval(CommandEntity commandEntity) {
                        return ((VdsActionParameters) commandEntity.getCommandParameters()).getVdsId();
                    }
                });
        final List<String> failedHostNames = LinqUtils.transformToList(failedHostIds, hostIdToLoggableNameFunction);

        command.setCustomValues(FAILED_HOSTS_VAR, failedHostNames, COMMA_SEPARATOR);
    }

    private List<CommandEntity> findFailedChildCommandEntities(CommandBase command) {
        final List<CommandEntity> childCommandEntities =
                CommandCoordinatorUtil.findChildCommands(command.getCommandId());

        final List<CommandEntity> failedChildCommandEntities =
                LinqUtils.filter(childCommandEntities, new Predicate<CommandEntity>() {
                    @Override
                    public boolean eval(CommandEntity commandEntity) {
                        return !commandEntity.getReturnValue().getSucceeded();
                    }
                });

        return failedChildCommandEntities;
    }

}
