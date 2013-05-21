package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * BLL command to update gluster hook on servers where there's a content conflict
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class UpdateGlusterHookCommand extends GlusterHookCommandBase<GlusterHookManageParameters> {
    private GlusterHookEntity entity;

    protected List<String> errors = new ArrayList<String>();

    public UpdateGlusterHookCommand(GlusterHookManageParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_HOOK);
    }

    protected GlusterHookEntity getGlusterHook() {
        if (entity == null) {
            entity = getGlusterHooksDao().getById(getParameters().getHookId(),true);
        }
        return entity;
    }

    private List<GlusterServerHook> getContentConflictServerHooks() {
        //get all destination servers - only serverhooks where content is in conflict
        List<GlusterServerHook> serverHooks = new ArrayList<GlusterServerHook>();
        for (GlusterServerHook serverHook: getGlusterHook().getServerHooks()) {
            if (!serverHook.getStatus().equals(GlusterHookStatus.MISSING) && !serverHook.getChecksum().equals(getGlusterHook().getChecksum())) {
                serverHooks.add(serverHook);
            }
        }
        return serverHooks;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getContentConflictServerHooks().isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_HOOK_NO_CONFLICT_SERVERS);
            return false;
        }

        for (GlusterServerHook serverHook: getContentConflictServerHooks()) {
            VDS vds = getVdsDAO().get(serverHook.getServerId());
            if (vds == null || vds.getStatus() != VDSStatus.Up) {
                setVdsName(vds.getName());
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        //check source to copy hook from - if engine copy or server copy
        final Boolean copyfromEngine = (getParameters().getSourceServerId() == null) ?  true : false;

        entity = getGlusterHook();
        addCustomValue(GlusterConstants.HOOK_NAME, entity.getName());

        final String hookContent;
        final String hookChecksum;
        final GlusterHookContentType hookContentType;
        if (copyfromEngine) {
            hookContent = entity.getContent();
            hookChecksum = entity.getChecksum();
            hookContentType = entity.getContentType();
        } else {
            //use a server's copy
            GlusterServerHook sourceServerHook = getGlusterHooksDao().getGlusterServerHook(entity.getId(), getParameters().getSourceServerId());
            VDSReturnValue retValue = runVdsCommand(VDSCommandType.GetGlusterHookContent,
                                        new GlusterHookVDSParameters(getParameters().getSourceServerId(),
                                                entity.getGlusterCommand(),
                                                entity.getStage(),
                                                entity.getName()));
            if (!retValue.getSucceeded()) {
                // throw exception as we cannot continue without content
                log.errorFormat("Failed to get content from server with id {0} with error {1}", getParameters().getSourceServerId(), retValue.getExceptionString());
                throw new VdcBLLException(retValue.getVdsError().getCode(), retValue.getVdsError().getMessage());
            }
            hookContent = (String) retValue.getReturnValue();
            hookChecksum = sourceServerHook.getChecksum();
            hookContentType = sourceServerHook.getContentType();
        }


        List<Callable<Pair<GlusterServerHook, VDSReturnValue>>> taskList = new ArrayList<Callable<Pair<GlusterServerHook, VDSReturnValue>>>();
        for (final GlusterServerHook serverHook : getContentConflictServerHooks()) {
            taskList.add(new Callable<Pair<GlusterServerHook, VDSReturnValue>>() {
                @Override
                public Pair<GlusterServerHook, VDSReturnValue> call() throws Exception {
                    VDSReturnValue returnValue;
                        returnValue =
                               runVdsCommand(
                                       VDSCommandType.UpdateGlusterHook,
                                       new GlusterHookVDSParameters(serverHook.getServerId(),
                                               entity.getGlusterCommand(),
                                               entity.getStage(),
                                               entity.getName(),
                                               hookContent,
                                               hookChecksum));
                     return new Pair<GlusterServerHook, VDSReturnValue>(serverHook, returnValue);

                }
            });
        }

        setSucceeded(true);
        if (!taskList.isEmpty()) {
            List<Pair<GlusterServerHook, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
            for (Pair<GlusterServerHook, VDSReturnValue> pairResult : pairResults) {

                VDSReturnValue retValue = pairResult.getSecond();
                if (!retValue.getSucceeded() ) {
                    errors.add(retValue.getVdsError().getMessage());
                 }
            }
        } else {
            setSucceeded(false);
        }

        if (errors.size() > 0) {
            setSucceeded(false);
            errorType =  AuditLogType.GLUSTER_HOOK_UPDATE_FAILED;
            handleVdsErrors(getAuditLogTypeValue(), errors);
            addCustomValue(GlusterConstants.FAILURE_MESSAGE , StringUtils.join(errors, SystemUtils.LINE_SEPARATOR));
        }

        if (getSucceeded() && !copyfromEngine) {
            //update server's content copy
            entity.setChecksum(hookChecksum);
            entity.setContent(hookContent);
            entity.setContentType(hookContentType);
        }

        if (getSucceeded()) {
            entity.removeMissingConflict();
            getGlusterHooksDao().updateGlusterHook(entity);
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
        return getSucceeded() ? AuditLogType.GLUSTER_HOOK_UPDATED : errorType;
    }

}
