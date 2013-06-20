package org.ovirt.engine.core.common.asynctasks.gluster;

import java.io.Serializable;

public class GlusterTaskParameters implements Serializable{

    private static final long serialVersionUID = 7151931460799410911L;
    private String volumeName;
    private String[] bricks;

    public String getVolumeName() {
        return volumeName;
    }
    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }
    public String[] getBricks() {
        return bricks;
    }
    public void setBricks(String[] bricks) {
        this.bricks = bricks;
    }

}
