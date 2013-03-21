package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Gluster Hook VDS parameter class with gluster command, hook name, and level as parameters. <br>
 * This will be used directly by Enable and Disable Gluster Hook VDS commands <br>
 */
public class GlusterHookVDSParameters extends VdsIdVDSCommandParametersBase {

    private String glusterCommand;
    private String hookName;
    private GlusterHookStage stage;

    public GlusterHookVDSParameters(Guid serverId, String glusterCommand, GlusterHookStage stage, String hookName) {
        super(serverId);
        setGlusterCommand(glusterCommand);
        setHookStage(stage);
        setHookName(hookName);
    }

    public String getGlusterCommand() {
        return glusterCommand;
    }

    public void setGlusterCommand(String glusterCommand) {
        this.glusterCommand = glusterCommand;
    }

    public String getHookName() {
        return hookName;
    }

    public void setHookName(String hookName) {
        this.hookName = hookName;
    }

    public GlusterHookStage getHookStage() {
        return stage;
    }

    public void setHookStage(GlusterHookStage stage) {
        this.stage = stage;
    }

}
