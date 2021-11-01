package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.JsonHelper;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class GetManagedBlockStorageStatsCommand<T extends AddManagedBlockStorageDomainParameters> extends CommandBase<T> {

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    public GetManagedBlockStorageStatsCommand(Guid commandId) {
        super(commandId);
    }

    public GetManagedBlockStorageStatsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        Map<String, Object> driverOptions = new HashMap<>(getParameters().getDriverOptions());
        if (getParameters().getDriverSensitiveOptions() != null) {
            driverOptions.putAll(getParameters().getDriverSensitiveOptions());
        }

        CinderlibReturnValue returnValue = null;
        Map<String, Object> storageStats = null;

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(driverOptions,
                            false),
                            Collections.singletonList(Boolean.TRUE.toString()),
                            getCorrelationId());
            returnValue = cinderlibExecutor.runCommand(CinderlibExecutor.CinderlibCommand.STORAGE_STATS, params);

            if(returnValue.getSucceed()) {
                storageStats = JsonHelper.jsonToMap(returnValue.getOutput());
            }
        } catch (Exception e) {
            log.error("Failed to fetch Managed block storage stats, output: '{}', '{}'",
                    returnValue != null ? returnValue.getOutput() : "", e);
            return;
        }

        if (returnValue.getSucceed() && storageStats != null) {
            log.debug(storageStats.toString());
            getReturnValue().setActionReturnValue(storageStats);
            setSucceeded(true);
        }
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }
}
