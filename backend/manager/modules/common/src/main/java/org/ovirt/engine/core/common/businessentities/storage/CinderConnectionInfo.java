package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class CinderConnectionInfo implements Serializable {

    private String driverVolumeType;

    private Map<String, Object> data;

    /**
     * @return the driverVolumeType
     */
    public String getDriverVolumeType() {
        return driverVolumeType;
    }

    /**
     * @param driverVolumeType the driverVolumeType to set
     */
    public void setDriverVolumeType(String driverVolumeType) {
        this.driverVolumeType = driverVolumeType;
    }

    /**
     * @return the data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConnectionInfo [driverVolumeType=" + driverVolumeType + "," +
                " data=" + data + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CinderConnectionInfo)) {
            return false;
        }

        CinderConnectionInfo other = (CinderConnectionInfo) obj;
        return Objects.equals(driverVolumeType, other.driverVolumeType)
                && Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                driverVolumeType,
                data
        );
    }
}

