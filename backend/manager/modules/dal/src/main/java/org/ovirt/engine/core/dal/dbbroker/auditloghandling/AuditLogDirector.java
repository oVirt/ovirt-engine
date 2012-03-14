package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DictionaryEntry;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.ResXResourceReader;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class AuditLogDirector {
    private static Log log = LogFactory.getLog(AuditLogDirector.class);
    private static Map<AuditLogType, String> mMessages = new EnumMap<AuditLogType, String>(AuditLogType.class);
    private static Map<AuditLogType, AuditLogSeverity> mSeverities = new EnumMap<AuditLogType, AuditLogSeverity>(AuditLogType.class);
    private static final Pattern pattern = Pattern.compile("\\$\\{\\w*\\}"); // match ${<alphanumeric>...}

    static {
        initMessages();
        initSeverities();
    }

    private static void initSeverities() {
        initDefaultSeverities();
        initNetworkSeverities();
        initImportExportSeverities();
        initEngineSeverities();
        initVMsPoolSeverities();
        initBookmarkSeverities();
        initVMSeverities();
        initQuotaSeverities();
        initTagSeverities();
        initClusterSeverities();
        initMLASeverities();
        initHostSeverities();
        initStorageSeverities();
        initTaskSeverities();
        checkSeverities();
    }

    private static void initDefaultSeverities() {
        mSeverities.put(AuditLogType.UNASSIGNED, AuditLogSeverity.NORMAL);
    }

    private static void initTaskSeverities() {
        mSeverities.put(AuditLogType.TASK_CLEARING_ASYNC_TASK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.TASK_STOPPING_ASYNC_TASK, AuditLogSeverity.NORMAL);
    }

    private static void initEngineSeverities() {
        mSeverities.put(AuditLogType.VDC_STOP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDC_START, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.CERTIFICATE_FILE_NOT_FOUND, AuditLogSeverity.ERROR);
    }

    private static void initBookmarkSeverities() {
        mSeverities.put(AuditLogType.USER_ADD_BOOKMARK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_BOOKMARK_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_BOOKMARK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_BOOKMARK_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_BOOKMARK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_BOOKMARK_FAILED, AuditLogSeverity.ERROR);
    }

    private static void initVMsPoolSeverities() {
        mSeverities.put(AuditLogType.USER_ADD_VM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_VM_POOL_WITH_VMS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_POOL_WITH_VMS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_VM_POOL_WITH_VMS_ADD_VDS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_VM_TO_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_TO_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_FROM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_POOL_INTERNAL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_POOL_FAILED_INTERNAL, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_AD_GROUP_TO_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_AD_GROUP_TO_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_INTERNAL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TIME_LEASED_POOL_FAILED_INTERNAL, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_INTERNAL, AuditLogSeverity.NORMAL);
        mSeverities
                .put(AuditLogType.USER_DETACH_AD_GROUP_FROM_TIME_LEASED_POOL_FAILED_INTERNAL, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_USER_TO_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_USER_TO_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS_FAILED, AuditLogSeverity.ERROR);
    }

    private static void initMLASeverities() {
        mSeverities.put(AuditLogType.USER_VDC_LOGIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_VDC_LOGIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_VDC_LOGOUT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_VDC_LOGOUT_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_AD_GROUP_TO_TIME_LEASED_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_AD_GROUP_TO_TIME_LEASED_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.AD_COMPUTER_ACCOUNT_SUCCEEDED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.AD_COMPUTER_ACCOUNT_FAILED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_PERMISSION, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_PERMISSION_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_PERMISSION, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_PERMISSION_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_ROLE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_ROLE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_ROLE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_ROLE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_ROLE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_ROLE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACHED_ACTION_GROUP_TO_ROLE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACHED_ACTION_GROUP_TO_ROLE_FAILED,
                AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACHED_ACTION_GROUP_FROM_ROLE_FAILED,
                AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_ROLE_WITH_ACTION_GROUP, AuditLogSeverity.NORMAL);
        mSeverities
                .put(AuditLogType.USER_ADD_ROLE_WITH_ACTION_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_ADUSER, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_ADUSER, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_ADUSER, AuditLogSeverity.WARNING);
    }

    private static void initHostSeverities() {
        mSeverities.put(AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST_ALL_TAKEN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_HOST_IS_ACTIVE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_ERROR_UPDATING_NAME, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_ERROR_UPDATING_NAMES_ALL_TAKEN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_NAME_IS_ACTIVE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_AUTO_APPROVE_PATTERN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_REGISTER_SUCCEEDED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_REGISTER_EXISTING_VDS_UPDATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_IS_NOT_CONFIGURED, AuditLogSeverity.ALERT);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_TEST_FAILED, AuditLogSeverity.ALERT);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_OPERATION_FAILED, AuditLogSeverity.ALERT);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_OPERATION_SKIPPED, AuditLogSeverity.ALERT);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_STATUS_VERIFICATION_FAILED, AuditLogSeverity.ALERT);
        mSeverities.put(AuditLogType.VDS_RUN_IN_NO_KVM_MODE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_VERSION_NOT_SUPPORTED_FOR_CLUSTER, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_CPU_LOWER_THAN_CLUSTER, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.CPU_FLAGS_NX_IS_MISSING, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_CPU_RETRIEVE_FAILED, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL_NETWORK, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_DOMAIN_DELAY_INTERVAL, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ADD_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_VDS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_RECOVER, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_RECOVER_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_RECOVER_FAILED_VMS_UNKNOWN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_MAINTENANCE, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_MAINTENANCE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_VDS_SHUTDOWN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_VDS_SHUTDOWN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_VDS_RESTART, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.SYSTEM_FAILED_VDS_RESTART, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_ACTIVATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_ACTIVATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_UPDATE_VDS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_VDS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_VDS_RESTART, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_VDS_RESTART, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_VDS_START, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_VDS_START, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_VDS_STOP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_VDS_STOP, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_FAILED_TO_RUN_VMS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_INSTALL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_INSTALL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_INSTALL_IN_PROGRESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_INITIATED_RUN_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_INITIATED_RUN_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_FENCE_STATUS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_FENCE_STATUS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_APPROVE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_APPROVE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_INITIALIZING, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ADD_VM_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_DETECTED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IRS_HOSTED_ON_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_MANUAL_FENCE_STATUS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_MANUAL_FENCE_STATUS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_MANUAL_FENCE_FAILED_CALL_FENCE_SPM, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_VDS_MAINTENANCE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_ALERT_FENCING_NO_PROXY_HOST, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VDS_LOW_MEM, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_STORAGE_CONNECTION_FAILED_BUT_LAST_VDS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VDS_LOW_DISK_SPACE, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VDS_LOW_DISK_SPACE_ERROR, AuditLogSeverity.ERROR);
    }

    @SuppressWarnings("deprecation")
    private static void initStorageSeverities() {
        mSeverities.put(AuditLogType.USER_ADD_STORAGE_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_STORAGE_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_STORAGE_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_STORAGE_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_STORAGE_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_STORAGE_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ACTIVATED_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ACTIVATE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DEACTIVATED_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_DEACTIVATE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_EXTENDED_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_EXTENDED_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_CONNECT_HOSTS_TO_LUN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_VG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ACTIVATE_STORAGE_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ACTIVATE_STORAGE_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_FAILED_CHANGE_STORAGE_POOL_STATUS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_SEARCHING_NEW_SPM,
                AuditLogSeverity.WARNING);
        mSeverities
                .put(AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_WITH_ERROR, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.RECONSTRUCT_MASTER_DONE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.RECONSTRUCT_MASTER_FAILED_NO_MASTER, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.RECONSTRUCT_MASTER_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.SYSTEM_MASTER_DOMAIN_NOT_IN_SYNC, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.RECOVERY_STORAGE_POOL_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_MOVED_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVED_TEMPLATE_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVED_TEMPLATE_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_MOVE_TEMPLATE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_COPY_TEMPLATE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_UPDATE_VM_DISK, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_HOTPLUG_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_HOTPLUG_DISK, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_HOTUNPLUG_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_HOTUNPLUG_DISK, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_COPY_TEMPLATE_DISK, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_MOVED_VM_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_MOVED_VM_DISK, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_MOVED_VM_DISK_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IRS_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IRS_DISK_SPACE_LOW_ERROR, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IRS_DISK_SPACE_LOW, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.REFRESH_REPOSITORY_FILE_LIST_SUCCEEDED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_FROM_NON_OPERATIONAL,
                AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.RECOVERY_STORAGE_POOL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.CONNECT_STORAGE_SERVERS_FAILED, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.CONNECT_STORAGE_POOL_FAILED, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.STORAGE_DOMAIN_ERROR, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.REFRESH_REPOSITORY_FILE_LIST_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.STORAGE_ALERT_VG_METADATA_CRITICALLY_FULL, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.STORAGE_ALERT_SMALL_VG_METADATA, AuditLogSeverity.WARNING);
    }

    private static void initQuotaSeverities() {
        mSeverities.put(AuditLogType.USER_ADD_QUOTA, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_QUOTA, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_QUOTA, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_UPDATE_QUOTA, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DELETE_QUOTA, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_DELETE_QUOTA, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_GRACE_LIMIT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_LIMIT, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_THRESHOLD, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD, AuditLogSeverity.WARNING);
    }

    private static void initVMSeverities() {
        mSeverities.put(AuditLogType.USER_ATTACH_VM_TO_AD_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_VM_TO_AD_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_VM_POOL_TO_AD_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_VM_POOL_TO_AD_GROUP_INTERNAL, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_VM_POOL_TO_AD_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_VM_POOL_TO_AD_GROUP_FAILED_INTERNAL, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_VM_TO_AD_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_VM_TO_AD_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_VM_POOL_TO_AD_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_VM_POOL_TO_AD_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_AD_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_AD_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.AUTO_SUSPEND_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.AUTO_SUSPEND_VM_FINISH_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.AUTO_SUSPEND_VM_FINISH_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.AUTO_FAILED_SUSPEND_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_MIGRATION_START, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_CANCEL_MIGRATION, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_CANCEL_MIGRATION_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_IMPORT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_IMPORT_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.TEMPLATE_IMPORT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.TEMPLATE_IMPORT_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_NOT_RESPONDING, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VM_MIGRATION_TRYING_RERUN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VM_PAUSED_ENOSPC, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_PAUSED_ERROR, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_PAUSED_EIO, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_PAUSED_EPERM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RUN_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_RUN_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RUN_VM_AS_STATELESS_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RUN_VM_FAILURE_STATELESS_SNAPSHOT_LEFT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_PAUSE_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_PAUSE_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_SUSPEND_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_SUSPEND_VM_FINISH_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_SUSPEND_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_STOP_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_STOP_SUSPENDED_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_STOP_SUSPENDED_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_SUSPEND_VM_OK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_STOP_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_STARTED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_UPDATE_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_FINISHED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_CHANGE_DISK_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_EJECT_VM_DISK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_EJECT_VM_FLOPPY, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_CHANGE_DISK_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RESUME_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_RESUME_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_INITIATED_RUN_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_STARTED_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_INITIATED_RUN_VM_FAILED, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_EXPORT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_EXPORT_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_EXPORT_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_EXPORT_TEMPLATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_INITIATED_SHUTDOWN_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_SHUTDOWN_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_STOPPED_VM_INSTEAD_OF_SHUTDOWN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_STOPPING_VM_INSTEAD_OF_SHUTDOWN, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RUN_VM_ON_NON_DEFAULT_VDS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ADD_DISK_TO_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_DISK_TO_VM_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_DISK_TO_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_DISK_FROM_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_DISK_FROM_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_MOVED_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVED_VM_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVED_VM_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_MOVE_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_ADD_VM_TEMPLATE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_UPDATE_VM_TEMPLATE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VM_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_VM_TEMPLATE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_USER_TO_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_ATTACH_USER_TO_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_CREATE_SNAPSHOT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_CREATE_SNAPSHOT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_SNAPSHOT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_REMOVE_SNAPSHOT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_TRY_BACK_TO_SNAPSHOT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_TRY_BACK_TO_SNAPSHOT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_RESTORE_FROM_SNAPSHOT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_RESTORE_FROM_SNAPSHOT_START, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_RESTORE_FROM_SNAPSHOT_FINISH_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_FAILED_RESTORE_FROM_SNAPSHOT, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_PASSWORD_CHANGED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_PASSWORD_CHANGE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_CLEAR_UNKNOWN_VMS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_CLEAR_UNKNOWN_VMS, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_MIGRATION_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_MIGRATION_ABORT, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTANANCE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_DOWN_ERROR, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_MIGRATION_DONE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_FAILURE, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_WAS_SET_DOWN_DUE_TO_HOST_REBOOT_OR_MANUAL_FENCE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.RUN_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_SET_TO_UNKNOWN_STATUS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_LOGGED_OUT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_DETACH_USER_FROM_VM, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.VM_DOWN, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_CHANGE_FLOPPY_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_FAILED_CHANGE_FLOPPY_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_MIGRATION_FAILED_FROM_TO, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_LOGGED_IN_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_LOCKED_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UNLOCKED_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_MIGRATION_ON_CONNECT_CHECK_FAILED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_MIGRATION_ON_CONNECT_CHECK_SUCCEEDED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT_FAILED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.VM_CLEARED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE, AuditLogSeverity.WARNING);
    }

    private static void initClusterSeverities() {
        mSeverities.put(AuditLogType.USER_ADD_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_UPDATE_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
    }

    private static void initTagSeverities() {
        mSeverities.put(AuditLogType.USER_UPDATE_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_UPDATE_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ADD_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ADD_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_REMOVE_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_REMOVE_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VDS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_VDS_FROM_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_VDS_FROM_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_VM_FROM_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_VM_FROM_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_FROM_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER_EXISTS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP_EXISTS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VM_EXISTS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_ATTACH_TAG_TO_VDS_EXISTS, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.USER_MOVE_TAG, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.USER_MOVE_TAG_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.UPDATE_TAGS_VM_DEFAULT_DISPLAY_TYPE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.UPDATE_TAGS_VM_DEFAULT_DISPLAY_TYPE_FAILED, AuditLogSeverity.NORMAL);
    }

    private static void initImportExportSeverities() {
        mSeverities.put(AuditLogType.IMPORTEXPORT_EXPORT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_IMPORT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_REMOVE_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_REMOVE_VM_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_IMPORT_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_EXPORT_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_IMPORT_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_REMOVE_TEMPLATE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_STARTING_REMOVE_VM, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE, AuditLogSeverity.WARNING);
    }

    private static void initNetworkSeverities() {
        mSeverities.put(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_DETACH_NETWORK_FROM_VDS_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_ADD_BOND, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ADD_BOND_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_BOND, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_BOND_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_VDS_NETWORK_MATCH_CLUSTER, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_VDS_NETWORK_NOT_MATCH_CLUSTER, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_VM_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_VM_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_ADD_VM_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ADD_VM_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_VM_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_VM_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_ADD_TEMPLATE_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ADD_TEMPLATE_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_ADD_NETWORK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ADD_NETWORK_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_NETWORK, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_REMOVE_NETWORK_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_DISPLAY_TO_VDS_GROUP, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_DISPLAY_TO_VDS_GROUP_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_NETWORK_TO_VDS_INTERFACE, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_UPDATE_NETWORK_TO_VDS_INTERFACE_FAILED, AuditLogSeverity.ERROR);
        mSeverities.put(AuditLogType.NETWORK_HOST_USING_WRONG_CLUSER_VLAN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.NETWORK_HOST_MISSING_CLUSER_VLAN, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.NETWORK_COMMINT_NETWORK_CHANGES, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.NETWORK_COMMINT_NETWORK_CHANGES_FAILED, AuditLogSeverity.NORMAL);
        mSeverities.put(AuditLogType.MAC_POOL_EMPTY, AuditLogSeverity.WARNING);
        mSeverities.put(AuditLogType.MAC_ADDRESS_IS_IN_USE, AuditLogSeverity.WARNING);
    }

    private static void initMessages() {
        ResXResourceReader reader = new ResXResourceReader("bundles/AuditLogMessages");
        for (DictionaryEntry entry : reader) {
            try {
                AuditLogType type = AuditLogType.valueOf(entry.getKey());
                if (!mMessages.containsKey(type)) {
                    mMessages.put(type, (String) entry.getValue());
                } else {
                    String secondPart = String.format(" First value : %1$s", mMessages.get(type));
                    String thirdPart = String.format("Second value : %1$s", entry.getValue());
                    log.errorFormat("Type {0} appears more then once in string table.{0}{1}", secondPart, thirdPart);
                }
            } catch (Exception e) {
                log.errorFormat("Cannot convert string {0} to AuditLogType", entry.getKey());
            }
        }
        checkMessages();
    }

    static void checkMessages() {
        AuditLogType[] values = AuditLogType.values();
        if (values.length != mMessages.size()) {
            for (AuditLogType value : values) {
                if (!mMessages.containsKey(value)) {
                    log.infoFormat("AuditLogType: {0} not exist in string table", value.toString());
                }
            }
        }
    }

    static void checkSeverities() {
        AuditLogType[] values = AuditLogType.values();
        if (values.length != mSeverities.size()) {
            for (AuditLogType value : values) {
                if (!mSeverities.containsKey(value)) {
                    log.warnFormat("AuditLogType: {0} not have severity. Assumed Normal", value.toString());
                }
            }
        }
    }

    /**
     * Gets the message.
     * @param logType
     *            Type of the log.
     * @return
     */
    public static String GetMessage(AuditLogType logType) {
        String value = "";
        if (mMessages.containsKey(logType)) {
            value = mMessages.get(logType);
        }
        return value;
    }

    public static void log(AuditLogableBase auditLogable) {
        AuditLogType logType = auditLogable.getAuditLogTypeValue();
        log(auditLogable, logType);
    }

    public static void log(AuditLogableBase auditLogable, AuditLogType logType) {
        log(auditLogable, logType, "");
    }

    public static void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        updateTimeoutLogableObject(auditLogable, logType);

        if (auditLogable == null || auditLogable.getLegal()) {
            String message = null;
            if ((message = mMessages.get(logType)) != null) {
                String resolvedMessage = resolveMessage(message, auditLogable);
                AuditLogSeverity severity = AuditLogSeverity.forValue(0);
                if (!((severity = mSeverities.get(logType)) != null)) {
                    severity = AuditLogSeverity.NORMAL;
                    log.infoFormat("No severity for {0} type", logType);
                }
                AuditLog auditLog;
                if (auditLogable != null) {
                    AuditLog tempVar = new AuditLog(logType, severity, resolvedMessage, auditLogable.getUserId(),
                            auditLogable.getUserName(), auditLogable.getVmIdRef(), auditLogable.getVmName(),
                            auditLogable.getVdsIdRef(), auditLogable.getVdsName(), auditLogable.getVmTemplateIdRef(),
                            auditLogable.getVmTemplateName());
                    tempVar.setstorage_domain_id(auditLogable.getStorageDomainId());
                    tempVar.setstorage_domain_name(auditLogable.getStorageDomainName());
                    tempVar.setstorage_pool_id(auditLogable.getStoragePoolId());
                    tempVar.setstorage_pool_name(auditLogable.getStoragePoolName());
                    tempVar.setvds_group_id(auditLogable.getVdsGroupId());
                    tempVar.setvds_group_name(auditLogable.getVdsGroupName());
                    tempVar.setCorrelationId(auditLogable.getCorrelationId());
                    tempVar.setJobId(auditLogable.getJobId());
                    tempVar.setQuotaId(auditLogable.getQuotaId());
                    tempVar.setQuotaName(auditLogable.getQuotaName());
                    tempVar.setGlusterVolumeId(auditLogable.getGlusterVolumeId());
                    tempVar.setGlusterVolumeName(auditLogable.getGlusterVolumeName());
                    auditLog = tempVar;
                } else {
                    auditLog = new AuditLog(logType, severity, resolvedMessage, null, null, null, null, null, null,
                            null, null);
                }
                getDbFacadeInstance().getAuditLogDAO().save(auditLog);
                if (!StringHelper.EqOp(loggerString, "")) {
                    log.infoFormat(loggerString, resolvedMessage);
                }
            } else if (auditLogable != null) {
                log.infoFormat("No string for {0} type. Use default Log", auditLogable.getAuditLogTypeValue());
                defaultLog(auditLogable);
            }
        }
    }

    /**
     * Update the logged object timeout attribute by log type definition
     * @param auditLogable
     *            the logable object to be updated
     * @param logType
     *            the log type which determine if timeout is used for it
     */
    private static void updateTimeoutLogableObject(AuditLogableBase auditLogable, AuditLogType logType) {
        if (logType.getDuplicateEventsIntervalValue() > 0) {
            auditLogable.setEndTime(DateTime.getNow().AddSeconds(logType.getDuplicateEventsIntervalValue()));
            auditLogable.setTimeoutObjectId(ComposeObjectId(auditLogable, logType));
        }
    }

    public static DbFacade getDbFacadeInstance() {
        return DbFacade.getInstance();
    }

    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     * @param logable
     *            the object to log
     * @param logType
     *            the log type associated with the object
     * @return a unique object id
     */
    private static String ComposeObjectId(AuditLogableBase logable, AuditLogType logType) {
        final char DELIMITER = ',';
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(logType);
        sb.append(DELIMITER);
        sb.append("sd=");
        sb.append(logable.getStorageDomainId() == null ? "" : logable.getStorageDomainId().toString());
        sb.append(DELIMITER);
        sb.append("dc=");
        sb.append(logable.getStoragePoolId() == null ? "" : logable.getStoragePoolId().toString());
        sb.append(DELIMITER);
        sb.append("user=");
        sb.append(logable.getUserId() == null ? "" : logable.getUserId().toString());
        sb.append(DELIMITER);
        sb.append("cluster=");
        sb.append(logable.getVdsGroupId().toString());
        sb.append(DELIMITER);
        sb.append("vds=");
        sb.append(logable.getVdsId().toString());
        sb.append(DELIMITER);
        sb.append("vm=");
        sb.append(logable.getVmId().equals(Guid.Empty) ? "" : logable.getVmId().toString());
        sb.append(DELIMITER);
        sb.append("template=");
        sb.append(logable.getVmTemplateId().equals(Guid.Empty) ? "" : logable.getVmTemplateId().toString());
        sb.append(DELIMITER);

        return sb.toString();
    }

    static String resolveMessage(String message, AuditLogableBase logable) {
        String returnValue = message;
        if (logable != null) {
            Map<String, String> map = getAvalableValues(logable);
            returnValue = resolveMessage(message, map);
        }
        return returnValue;
    }

    /**
     * Resolves a message which contains place holders by replacing them with the value from the map.
     *
     * @param message
     *            A text representing a message with place holders
     * @param values
     *            a map of the place holder to its values
     * @return a resolved message
     */
    public static String resolveMessage(String message, Map<String, String> values) {
        Matcher matcher = pattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        String value;
        String token;
        while (matcher.find()) {
            token = matcher.group();

            // remove leading ${
            token = token.substring(2);

            // remove trailing }
            token = token.substring(0, token.length() - 1);

            // get value from value map
            value = values.get(token.toLowerCase());
            if (value == null || value.isEmpty()) {
                // replace value with token if value not defined
                value = token;
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value)); // put the value into message
        }

        // append the rest of the message
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    static Map<String, String> getAvalableValues(AuditLogableBase logable) {
        Map<String, String> returnValue =
                new HashMap<String, String>(logable.getCustomValues());
        Class<?> type = AuditLogableBase.class;
        for (PropertyInfo propertyInfo : TypeCompat.GetProperties(type)) {
            Object value = propertyInfo.GetValue(logable, null);
            String stringValue = value != null ? value.toString() : null;
            if (!returnValue.containsKey(propertyInfo.getName().toLowerCase())) {
                returnValue.put(propertyInfo.getName().toLowerCase(), stringValue);
            } else {
                log.errorFormat("Try to add duplicate values with same name. Type: {0}. Value: {1}",
                        logable.getAuditLogTypeValue(), propertyInfo.getName().toLowerCase());
            }
        }
        List<String> attributes = AuditLogHelper.getCustomLogFields(logable.getClass(), true);
        if (attributes != null && attributes.size() > 0) {
            TypeCompat.getPropertyValues(logable, new HashSet<String>(attributes), returnValue);
        }
        return returnValue;
    }

    private static void defaultLog(AuditLogableBase auditLogable) {
        auditLogable.DefaultLog();
    }

}
