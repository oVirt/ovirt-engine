package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.Model;

public class VolumeSnapshotOptionModel extends Model {
    String optionName;
    String optionValue;
    String correspodingClusterValue;

    public VolumeSnapshotOptionModel() {
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getCorrespodingClusterValue() {
        return correspodingClusterValue;
    }

    public void setCorrespodingClusterValue(String correspodingClusterValue) {
        this.correspodingClusterValue = correspodingClusterValue;
    }
}
