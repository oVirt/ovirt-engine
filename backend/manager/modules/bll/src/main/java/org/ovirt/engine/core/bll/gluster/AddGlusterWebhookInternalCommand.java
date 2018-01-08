package org.ovirt.engine.core.bll.gluster;

import static org.ovirt.engine.core.common.FeatureSupported.supportedInConfig;

import java.net.MalformedURLException;
import java.net.URL;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterWebhookVDSParameters;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * BLL command to add a gluster webhook to nodes in cluster as a callback for gluster events
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class AddGlusterWebhookInternalCommand<T extends VdsActionParameters> extends GlusterCommandBase<T> {
    private static final String WEBHOOK_SERVLET_PATH = "/services/glusterevents";

    public AddGlusterWebhookInternalCommand(T params, CommandContext context) {
        super(params, context);
        setVdsId(getParameters().getVdsId());
    }

/*    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }*/

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_WEBHOOK);
    }



    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (!supportedInConfig(ConfigValues.GlusterEventingSupported, getCluster().getCompatibilityVersion())) {
            //if eventing is not supported, we do not want to process further
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null) {
            handleVdsError(AuditLogType.GLUSTER_WEBHOOK_ADD_FAILED, "No webhook url");
            setSucceeded(false);
            return;
        }
        // check for a server to run the command on
        VDS upServer = getUpServer();
        if (upServer == null) {
            //there's no other server than the one being activated
            upServer = getVds();
        }
        //TODO: check if the webhook is already added before adding this.
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.AddGlusterWebhook,
                                        new GlusterWebhookVDSParameters(upServer.getId(), webhookUrl, null));
        setSucceeded(returnValue.getSucceeded());
        if(!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_WEBHOOK_ADD_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    private String getWebhookUrl() {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        URL servletUrl;
        try {
            if (config.isHttpsEnabled()) {
                servletUrl = config.getExternalHttpsUrl(WEBHOOK_SERVLET_PATH);
            }
            else {
                servletUrl = config.getExternalHttpUrl(WEBHOOK_SERVLET_PATH);
            }
            return servletUrl.toExternalForm();
        }
        catch (MalformedURLException exception) {
            log.debug("Failed to get engine webhook url", exception);
            return null;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_WEBHOOK_ADDED;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_WEBHOOK_ADD_FAILED : errorType;
        }
    }
}
