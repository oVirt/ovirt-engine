package org.ovirt.engine.core.common.constants.gluster;

public class GlusterConstants {
    public static final int CODE_SUCCESS = 0;
    public static final String ON = "on";
    public static final String OFF = "off";

    public static final String ENABLE = "enable";
    public static final String DISABLE = "disable";

    public static final String OPTION_AUTH_ALLOW = "auth.allow";
    public static final String OPTION_NFS_DISABLE = "nfs.disable";
    public static final String OPTION_USER_CIFS = "user.cifs";
    public static final String OPTION_GROUP = "group";
    public static final String OPTION_QUORUM_TYPE = "cluster.quorum-type";
    public static final String OPTION_QUORUM_COUNT = "cluster.quorum-count";
    public static final String OPTION_SERVER_QUORUM_TYPE = "cluster.server-quorum-type";
    public static final String OPTION_SERVER_QUORUM_RATIO = "cluster.server-quorum-ratio";

    public static final String OPTION_QUORUM_TYPE_FIXED = "fixed";
    public static final String OPTION_QUORUM_TYPE_AUTO = "auto";

    public static final String NO_OF_BRICKS = "NoOfBricks";
    public static final String BRICK_PATH = "brickpath";
    public static final String BRICK_NAME = "brickName";
    public static final String SERVER_NAME = "servername";
    public static final String VOLUME_NAME = "glustervolumename";
    public static final String VOLUME_TYPE = "glusterVolumeType";
    public static final String VDS_NAME = "VdsName";

    public static final String FS_TYPE_XFS = "xfs";

    // Variables used in audit messages.
    // Keep the values lowercase to avoid call to String#toLowerCase()
    public static final String CLUSTER = "cluster";
    public static final String VOLUME = "glustervolume";
    public static final String BRICK = "brick";
    public static final String OPTION_KEY = "key";
    public static final String OPTION_VALUE = "value";
    public static final String OPTION_OLD_VALUE = "oldvalue";
    public static final String OPTION_NEW_VALUE = "newvalue";
    public static final String OLD_STATUS = "oldstatus";
    public static final String NEW_STATUS = "newstatus";
    public static final String SERVICE_TYPE = "servicetype";
    public static final String SERVICE_NAME = "servicename";
    public static final String MANAGE_GLUSTER_SERVICE_ACTION_TYPE_START = "start";
    public static final String MANAGE_GLUSTER_SERVICE_ACTION_TYPE_STOP = "stop";
    public static final String MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART = "restart";
    public static final String COMMAND = "command";
    public static final String GEO_REP_SESSION_KEY = "geoRepSessionKey";
    public static final String GEO_REP_SLAVE_VOLUME_NAME = "geoRepSlaveVolumeName";
    public static final String GEO_REP_USER = "geoRepUserName";
    public static final String GEO_REP_USER_GROUP = "geoRepGroupName";
    public static final String NEW_BRICK = "newBrick";

    public static final String HOOK_NAME = "glusterhookname";
    public static final String FAILURE_MESSAGE = "failuremessage";
    public static final String JOB_STATUS = "status";
    public static final String JOB_INFO = "info";
    public static final String NO_SERVER = "NO SERVER";
    public static final String VOLUME_SNAPSHOT_MAX_HARD_LIMIT = "snap-max-hard-limit";
    public static final String VOLUME_SNAPSHOT_NAME = "snapname";
    public static final String CLUSTER_NAME = "ClusterName";
    public static final String GEOREP_CHECKPOINT_OPTION = "checkpoint";
    public static final String GEOREP_CHECKPOINT_VALUE = "now";
    public static final String SOURCE = "source";
    public static final String SOURCE_CLI = "cli";
    public static final String SOURCE_EVENT = "gluster event";

}
