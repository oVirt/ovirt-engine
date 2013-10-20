package org.ovirt.engine.core.common.businessentities.gluster;


/**
 * Enum for status of gluster related services
 */
public enum GlusterServiceStatus {
    ERROR(0 /* 0000 */, "Error"),
    RUNNING(8 /* 1000 */, "Up"),
    STOPPED(4 /* 0100 */, "Down"),
    NOT_AVAILABLE(2 /* 0010 */, "Not Installed"), // service is not installed in the host
    UNKNOWN(1 /* 0001 */, "Unknown"), // Couldn't fetch status
    MIXED(12 /* 1100 */, "Partially Up"); // cluster-wide status, few up, few down

    private int statusCode;
    private String statusMsg;

    private GlusterServiceStatus(int code, String msg) {
        statusCode = code;
        statusMsg = msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public GlusterServiceStatus getCompositeStatus(GlusterServiceStatus otherStatus) {
        int compositeStatusCode = this.getStatusCode() | otherStatus.getStatusCode();

        switch(compositeStatusCode) {
            case 8 : // 1000
                return GlusterServiceStatus.RUNNING;
            case 12 : // 1100
            case 10 : // 1010
            case 9 : // 1001
            case 14: // 1110
            case 13: // 1101
                return GlusterServiceStatus.MIXED;
            case 4 : // 0100
            case 6 : // 0110
            case 5 : // 0101
                return GlusterServiceStatus.STOPPED;
            case 1 : // 0001
                return GlusterServiceStatus.UNKNOWN;
            case 2 : // 0010
            case 3 : // 0011
                return GlusterServiceStatus.NOT_AVAILABLE;
        }

        return GlusterServiceStatus.UNKNOWN;
    }
}
