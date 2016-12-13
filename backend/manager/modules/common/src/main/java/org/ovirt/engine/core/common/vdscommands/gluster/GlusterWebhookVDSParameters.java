package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * This will be used directly by Gluster Webhook commands.
 */
public class GlusterWebhookVDSParameters extends VdsIdVDSCommandParametersBase {
    private String webhookUrl;

    private String bearerToken;

    public GlusterWebhookVDSParameters(Guid serverId, String engineWebhookUrl, String bearerToken) {
        super(serverId);
        this.webhookUrl = engineWebhookUrl;
        this.bearerToken = bearerToken;
    }

    public GlusterWebhookVDSParameters() {
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}

