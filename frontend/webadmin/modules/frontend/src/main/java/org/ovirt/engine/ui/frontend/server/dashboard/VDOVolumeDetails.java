package org.ovirt.engine.ui.frontend.server.dashboard;

import org.ovirt.engine.core.compat.Guid;

public class VDOVolumeDetails {

    private Guid volumeId;

    private String volumeName;

    private Integer vdoSavings;



    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public Integer getVdoSavings() {
        return vdoSavings;
    }

    public void setVdoSavings(Integer vdoSavings) {
        this.vdoSavings = vdoSavings;
    }

}
