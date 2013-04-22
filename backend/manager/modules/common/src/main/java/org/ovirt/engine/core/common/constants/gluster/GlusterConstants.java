package org.ovirt.engine.core.common.constants.gluster;

public class GlusterConstants {
    public static final int CODE_SUCCESS = 0;
    public static final String ON = "on";
    public static final String OFF = "off";

    public static final String OPTION_AUTH_ALLOW = "auth.allow";
    public static final String OPTION_NFS_DISABLE = "nfs.disable";
    public static final String OPTION_USER_CIFS = "user.cifs";
    public static final String OPTION_GROUP = "group";

    public static final int DEFAULT_REPLICA_COUNT = 2;
    public static final int DEFAULT_STRIPE_COUNT = 4;

    // Variables used in audit messages.
    // Keep the values lowercase to avoid call to String#toLowerCase()
    public static final String CLUSTER = "cluster";
    public static final String VOLUME = "glustervolume";
    public static final String BRICK = "brick";
    public static final String OPTION_KEY = "key";
    public static final String OPTION_VALUE = "value";
    public static final String OPTION_OLD_VALUE = "oldvalue";
    public static final String OPTION_NEW_VALUE = "newvalue";

    public static final String HOOK_NAME = "glusterhookname";
    public static final String FAILURE_MESSAGE = "failuremessage";
}
