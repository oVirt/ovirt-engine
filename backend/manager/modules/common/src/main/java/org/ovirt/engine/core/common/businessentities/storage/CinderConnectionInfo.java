package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Map;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CinderConnectionInfo that = (CinderConnectionInfo) o;

        if (driverVolumeType != null ? !driverVolumeType.equals(that.driverVolumeType) : that.driverVolumeType != null)
            return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);

    }

    @Override
    public int hashCode() {
        int result = driverVolumeType != null ? driverVolumeType.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}

