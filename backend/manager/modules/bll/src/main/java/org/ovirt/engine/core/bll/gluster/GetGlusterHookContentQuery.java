package org.ovirt.engine.core.bll.gluster;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookContentQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;

/**
 * Query to fetch gluster hook content for the Gluster cluster
 */
public class GetGlusterHookContentQuery<P extends GlusterHookContentQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterHookContentQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterHookEntity hook = glusterHooksDao.getById(getParameters().getGlusterHookId());
        String content = "";

        if (getParameters().getGlusterServerId() == null) {
            if (hook.getContentType().equals(GlusterHookContentType.TEXT)) {
                content = glusterHooksDao.getGlusterHookContent(getParameters().getGlusterHookId());
            }
        } else {
            GlusterServerHook serverHook =
                    glusterHooksDao.getGlusterServerHook(hook.getId(), getParameters().getGlusterServerId());

            if (serverHook != null && serverHook.getContentType() == GlusterHookContentType.TEXT) {
                VDSReturnValue returnValue =
                        runVdsCommand(VDSCommandType.GetGlusterHookContent,
                                new GlusterHookVDSParameters(getParameters().getGlusterServerId(),
                                        hook.getGlusterCommand(),
                                        hook.getStage(),
                                        hook.getName()));
                if (returnValue.getSucceeded()) {
                    content = (String) returnValue.getReturnValue();
                }
            }
        }

        content = StringUtils.newStringUtf8(Base64.decodeBase64(content));
        getQueryReturnValue().setReturnValue(content);
    }

}
