package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * BLL command to add gluster hook on servers where the hook is missing
 */
@NonTransactiveCommandAttribute
public class AddGlusterHookCommand<T extends GlusterHookManageParameters> extends GlusterHookCommandBase<T> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private GlusterHooksDao glusterHooksDao;

    protected List<String> errors = new ArrayList<>();
    private List<GlusterServerHook> missingServerHooks = null;

    public AddGlusterHookCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_HOOK);
    }

    private List<GlusterServerHook> getMissingServerHooks() {
        //get all destination servers - only serverhooks where hook is missing
        if (missingServerHooks == null) {
            missingServerHooks = new ArrayList<>();
            for (GlusterServerHook serverHook: getGlusterHook().getServerHooks()) {
                if (serverHook.getStatus().equals(GlusterHookStatus.MISSING)) {
                    missingServerHooks.add(serverHook);
                }
            }
        }
        return missingServerHooks;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getMissingServerHooks().isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS);
            return false;
        }

        for (GlusterServerHook serverHook: getMissingServerHooks()) {
            VDS vds = vdsDao.get(serverHook.getServerId());
            if (vds == null || vds.getStatus() != VDSStatus.Up) {
                String vdsName = vds != null ? vds.getName() : GlusterConstants.NO_SERVER;
                setVdsName(vdsName);
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP);
                addValidationMessage(String.format("$%1$s %2$s", "VdsName", vdsName));
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {

        entity = getGlusterHook();
        addCustomValue(GlusterConstants.HOOK_NAME, entity.getName());

        final boolean hookEnabled = entity.getStatus() == GlusterHookStatus.ENABLED;

        List<Callable<Pair<GlusterServerHook, VDSReturnValue>>> taskList = new ArrayList<>();
        for (final GlusterServerHook serverHook : getMissingServerHooks()) {
            taskList.add(() -> {
                VDSReturnValue returnValue;
                returnValue =
                        runVdsCommand(
                                VDSCommandType.AddGlusterHook,
                                new GlusterHookVDSParameters(serverHook.getServerId(),
                                        entity.getGlusterCommand(),
                                        entity.getStage(),
                                        entity.getName(),
                                        entity.getContent(),
                                        entity.getChecksum(),
                                        hookEnabled));
                return new Pair<>(serverHook, returnValue);

            });
        }

        if (!taskList.isEmpty()) {
            List<Pair<GlusterServerHook, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
            for (Pair<GlusterServerHook, VDSReturnValue> pairResult : pairResults) {

                VDSReturnValue retValue = pairResult.getSecond();
                if (!retValue.getSucceeded() ) {
                    errors.add(retValue.getVdsError().getMessage());
                } else {
                    //hook added successfully, so remove from gluster server hooks table
                    glusterHooksDao.removeGlusterServerHook(pairResult.getFirst().getHookId(),
                            pairResult.getFirst().getServerId());
                }
            }
        }

        if (errors.size() > 0) {
            setSucceeded(false);
            errorType = AuditLogType.GLUSTER_HOOK_ADD_FAILED;
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE , StringUtils.join(errors, System.lineSeparator()));
        } else {
            setSucceeded(true);
        }

        if (getSucceeded()) {
            entity.removeMissingConflict();
            updateGlusterHook(entity);
        }

    }


    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getGlusterHook() != null) {
                jobProperties.put(GlusterConstants.HOOK_NAME, getGlusterHook().getName());
            }
        }

        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.GLUSTER_HOOK_ADDED : errorType;
    }

}
