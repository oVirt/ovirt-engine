package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.EventSubscription;
import org.ovirt.engine.api.model.NotifiableEvent;
import org.ovirt.engine.api.model.NotificationMethod;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;

public class EventSubscriptionMapper {

    @Mapping(from = EventSubscription.class, to = EventSubscriber.class)
    public static EventSubscriber map(EventSubscription eventSubscription, EventSubscriber eventSubscriber) {
        if (eventSubscription == null) {
            return null;
        }
        if (eventSubscription.isSetEvent()) {
            eventSubscriber.setEventUpName(map(eventSubscription.getEvent(), null).name());
        }
        if (eventSubscription.isSetNotificationMethod()) {
            eventSubscriber.setEventNotificationMethod(map(eventSubscription.getNotificationMethod(), null));
        }
        if (eventSubscription.isSetUser() && eventSubscription.getUser().isSetId()) {
            eventSubscriber.setSubscriberId(Guid.createGuidFromString(eventSubscription.getUser().getId()));
        }
        if (eventSubscription.isSetAddress()) {
            eventSubscriber.setMethodAddress(eventSubscription.getAddress());
        }
        return eventSubscriber;
    }

    @Mapping(from = EventSubscriber.class, to = EventSubscription.class)
    public static EventSubscription map(EventSubscriber eventSubscriber, EventSubscription eventSubscription) {
        if (eventSubscriber == null) {
            return null;
        }
        if (eventSubscription == null) {
            eventSubscription = new EventSubscription();
        }
        if (eventSubscriber.getEventUpName() != null) {
            AuditLogType eventName = AuditLogType.valueOf(eventSubscriber.getEventUpName());
            if (eventName != null) {
                NotifiableEvent event = map(eventName, null);
                eventSubscription.setId(event.toString().toLowerCase());
                eventSubscription.setEvent(event);
            }
        }
        if (eventSubscriber.getEventNotificationMethod() != null) {
            eventSubscription.setNotificationMethod(map(eventSubscriber.getEventNotificationMethod(), null));
        }
        if (eventSubscriber.getSubscriberId() != null) {
            if (!eventSubscription.isSetUser()) {
                eventSubscription.setUser(new User());
                eventSubscription.getUser().setId(eventSubscriber.getSubscriberId().toString());
            }
        }
        if (eventSubscriber.getMethodAddress() != null) {
            eventSubscription.setAddress(eventSubscriber.getMethodAddress());
        }
        return eventSubscription;
    }

    @Mapping(from = NotificationMethod.class, to = EventNotificationMethod.class)
    public static EventNotificationMethod map(NotificationMethod model, EventNotificationMethod template) {
        if (model == null) {
            return null;
        }
        if (model == NotificationMethod.SMTP) {
            return EventNotificationMethod.SMTP;
        }
        if (model == NotificationMethod.SNMP) {
            return EventNotificationMethod.SNMP;
        }
        assert false : "unknown notification method: " + model.toString();
        return null;
    }

    @Mapping(from = EventNotificationMethod.class, to = NotificationMethod.class)
    public static NotificationMethod map(EventNotificationMethod model, NotificationMethod template) {
        if (model == null) {
            return null;
        }
        if (model == EventNotificationMethod.SMTP) {
            return NotificationMethod.SMTP;
        }
        if (model == EventNotificationMethod.SNMP) {
            return NotificationMethod.SNMP;
        }
        assert false : "unknown notification method: " + model.toString();
        return null;
    }

    @Mapping(from = NotifiableEvent.class, to = AuditLogType.class)
    public static AuditLogType map(NotifiableEvent model, AuditLogType template) {
        if (model == null) {
            return null;
        }
        switch (model) {
        case ENGINE_STOP:
            return AuditLogType.VDC_STOP;
        case ENGINE_BACKUP_STARTED:
            return AuditLogType.ENGINE_BACKUP_STARTED;
        case ENGINE_BACKUP_COMPLETED:
            return AuditLogType.ENGINE_BACKUP_COMPLETED;
        case ENGINE_BACKUP_FAILED:
            return AuditLogType.ENGINE_BACKUP_FAILED;
        case ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return AuditLogType.ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case ENGINE_CA_CERTIFICATION_HAS_EXPIRED:
            return AuditLogType.ENGINE_CA_CERTIFICATION_HAS_EXPIRED;
        case ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return AuditLogType.ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case ENGINE_CERTIFICATION_HAS_EXPIRED:
            return AuditLogType.ENGINE_CERTIFICATION_HAS_EXPIRED;
        case CLUSTER_ALERT_HA_RESERVATION:
            return AuditLogType.CLUSTER_ALERT_HA_RESERVATION;
        case NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM:
            return AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM;
        case CLUSTER_ALERT_HA_RESERVATION_DOWN:
            return AuditLogType.CLUSTER_ALERT_HA_RESERVATION_DOWN;
        case HOST_FAILURE:
            return AuditLogType.VDS_FAILURE;
        case HOST_UPDATES_ARE_AVAILABLE:
            return AuditLogType.HOST_UPDATES_ARE_AVAILABLE;
        case HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES:
            return AuditLogType.HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES;
        case USER_HOST_MAINTENANCE:
            return AuditLogType.USER_VDS_MAINTENANCE;
        case USER_HOST_MAINTENANCE_MANUAL_HA:
            return AuditLogType.USER_VDS_MAINTENANCE_MANUAL_HA;
        case USER_HOST_MAINTENANCE_MIGRATION_FAILED:
            return AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED;
        case HOST_ACTIVATE_MANUAL_HA:
            return AuditLogType.VDS_ACTIVATE_MANUAL_HA;
        case HOST_ACTIVATE_FAILED:
            return AuditLogType.VDS_ACTIVATE_FAILED;
        case HOST_RECOVER_FAILED:
            return AuditLogType.VDS_RECOVER_FAILED;
        case HOST_APPROVE_FAILED:
            return AuditLogType.VDS_APPROVE_FAILED;
        case HOST_INSTALL_FAILED:
            return AuditLogType.VDS_INSTALL_FAILED;
        case HOST_TIME_DRIFT_ALERT:
            return AuditLogType.VDS_TIME_DRIFT_ALERT;
        case HOST_SET_NONOPERATIONAL:
            return AuditLogType.VDS_SET_NONOPERATIONAL;
        case HOST_SET_NONOPERATIONAL_IFACE_DOWN:
            return AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN;
        case HOST_LOW_MEM:
            return AuditLogType.VDS_LOW_MEM;
        case HOST_HIGH_MEM_USE:
            return AuditLogType.VDS_HIGH_MEM_USE;
        case HOST_INTERFACE_HIGH_NETWORK_USE:
            return AuditLogType.HOST_INTERFACE_HIGH_NETWORK_USE;
        case HOST_HIGH_CPU_USE:
            return AuditLogType.VDS_HIGH_CPU_USE;
        case HOST_HIGH_SWAP_USE:
            return AuditLogType.VDS_HIGH_SWAP_USE;
        case HOST_LOW_SWAP:
            return AuditLogType.VDS_LOW_SWAP;
        case HOST_INTERFACE_STATE_DOWN:
            return AuditLogType.HOST_INTERFACE_STATE_DOWN;
        case HOST_BOND_SLAVE_STATE_DOWN:
            return AuditLogType.HOST_BOND_SLAVE_STATE_DOWN;
        case NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM:
            return AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM;
        case HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return AuditLogType.HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case HOST_CERTIFICATION_HAS_EXPIRED:
            return AuditLogType.HOST_CERTIFICATION_HAS_EXPIRED;
        case HOST_CERTIFICATE_HAS_INVALID_SAN:
            return AuditLogType.HOST_CERTIFICATE_HAS_INVALID_SAN;
        case HOST_SET_NONOPERATIONAL_DOMAIN:
            return AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN;
        case SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM:
            return AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM;
        case SYSTEM_DEACTIVATED_STORAGE_DOMAIN:
            return AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN;
        case VM_FAILURE:
            return AuditLogType.VM_FAILURE;
        case VM_MIGRATION_START:
            return AuditLogType.VM_MIGRATION_START;
        case VM_MIGRATION_FAILED:
            return AuditLogType.VM_MIGRATION_FAILED;
        case VM_MIGRATION_TO_SERVER_FAILED:
            return AuditLogType.VM_MIGRATION_TO_SERVER_FAILED;
        case VM_NOT_RESPONDING:
            return AuditLogType.VM_NOT_RESPONDING;
        case VM_STATUS_RESTORED:
            return AuditLogType.VM_STATUS_RESTORED;
        case HA_VM_RESTART_FAILED:
            return AuditLogType.HA_VM_RESTART_FAILED;
        case HA_VM_FAILED:
            return AuditLogType.HA_VM_FAILED;
        case VM_CONSOLE_CONNECTED:
            return AuditLogType.VM_CONSOLE_CONNECTED;
        case VM_CONSOLE_DISCONNECTED:
            return AuditLogType.VM_CONSOLE_DISCONNECTED;
        case VM_SET_TICKET:
            return AuditLogType.VM_SET_TICKET;
        case VM_DOWN_ERROR:
            return AuditLogType.VM_DOWN_ERROR;
        case HOST_INITIATED_RUN_VM_FAILED:
            return AuditLogType.VDS_INITIATED_RUN_VM_FAILED;
        case VM_PAUSED:
            return AuditLogType.VM_PAUSED;
        case VM_PAUSED_EIO:
            return AuditLogType.VM_PAUSED_EIO;
        case VM_PAUSED_ENOSPC:
            return AuditLogType.VM_PAUSED_ENOSPC;
        case VM_PAUSED_EPERM:
            return AuditLogType.VM_PAUSED_EPERM;
        case VM_PAUSED_ERROR:
            return AuditLogType.VM_PAUSED_ERROR;
        case VM_RECOVERED_FROM_PAUSE_ERROR:
            return AuditLogType.VM_RECOVERED_FROM_PAUSE_ERROR;
        case MAC_ADDRESS_IS_EXTERNAL:
            return AuditLogType.MAC_ADDRESS_IS_EXTERNAL;
        case HOST_SLOW_STORAGE_RESPONSE_TIME:
            return AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME;
        case IRS_FAILURE:
            return AuditLogType.IRS_FAILURE;
        case IRS_DISK_SPACE_LOW:
            return AuditLogType.IRS_DISK_SPACE_LOW;
        case IRS_CONFIRMED_DISK_SPACE_LOW:
            return AuditLogType.IRS_CONFIRMED_DISK_SPACE_LOW;
        case IRS_DISK_SPACE_LOW_ERROR:
            return AuditLogType.IRS_DISK_SPACE_LOW_ERROR;
        case NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD:
            return AuditLogType.NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD;
        case GLUSTER_VOLUME_CREATE:
            return AuditLogType.GLUSTER_VOLUME_CREATE;
        case GLUSTER_VOLUME_CREATE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_CREATE_FAILED;
        case GLUSTER_VOLUME_OPTION_ADDED:
            return AuditLogType.GLUSTER_VOLUME_OPTION_ADDED;
        case GLUSTER_VOLUME_OPTION_MODIFIED:
            return AuditLogType.GLUSTER_VOLUME_OPTION_MODIFIED;
        case GLUSTER_VOLUME_OPTION_SET_FAILED:
            return AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED;
        case GLUSTER_VOLUME_START:
            return AuditLogType.GLUSTER_VOLUME_START;
        case GLUSTER_VOLUME_START_FAILED:
            return AuditLogType.GLUSTER_VOLUME_START_FAILED;
        case GLUSTER_VOLUME_STOP:
            return AuditLogType.GLUSTER_VOLUME_STOP;
        case GLUSTER_VOLUME_STOP_FAILED:
            return AuditLogType.GLUSTER_VOLUME_STOP_FAILED;
        case GLUSTER_VOLUME_OPTIONS_RESET:
            return AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET;
        case GLUSTER_VOLUME_OPTIONS_RESET_ALL:
            return AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_ALL;
        case GLUSTER_VOLUME_OPTIONS_RESET_FAILED:
            return AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED;
        case GLUSTER_VOLUME_DELETE:
            return AuditLogType.GLUSTER_VOLUME_DELETE;
        case GLUSTER_VOLUME_DELETE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_DELETE_FAILED;
        case GLUSTER_VOLUME_ADD_BRICK:
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK;
        case GLUSTER_VOLUME_ADD_BRICK_FAILED:
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED;
        case GLUSTER_VOLUME_REMOVE_BRICKS:
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS;
        case GLUSTER_VOLUME_REMOVE_BRICKS_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS:
            return AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED:
            return AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED;
        case GLUSTER_VOLUME_REMOVE_BRICKS_STOP:
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP;
        case GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED;
        case GLUSTER_VOLUME_REBALANCE_START:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_START;
        case GLUSTER_VOLUME_REBALANCE_START_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED;
        case GLUSTER_VOLUME_REBALANCE_STOP:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP;
        case GLUSTER_VOLUME_REBALANCE_STOP_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED;
        case GLUSTER_VOLUME_REPLACE_BRICK_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_FAILED;
        case GLUSTER_VOLUME_REPLACE_BRICK_START:
            return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START;
        case GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED:
            return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED;
        case GLUSTER_VOLUME_BRICK_REPLACED:
            return AuditLogType.GLUSTER_VOLUME_BRICK_REPLACED;
        case GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI:
            return AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI;
        case GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI;
        case REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI:
            return AuditLogType.REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI;
        case GLUSTER_VOLUME_SNAPSHOT_CREATED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATED;
        case GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED;
        case GLUSTER_SERVER_ADD_FAILED:
            return AuditLogType.GLUSTER_SERVER_ADD_FAILED;
        case GLUSTER_SERVER_REMOVE:
            return AuditLogType.GLUSTER_SERVER_REMOVE;
        case GLUSTER_SERVER_REMOVE_FAILED:
            return AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
        case GLUSTER_VOLUME_PROFILE_START:
            return AuditLogType.GLUSTER_VOLUME_PROFILE_START;
        case GLUSTER_VOLUME_PROFILE_START_FAILED:
            return AuditLogType.GLUSTER_VOLUME_PROFILE_START_FAILED;
        case GLUSTER_VOLUME_PROFILE_STOP:
            return AuditLogType.GLUSTER_VOLUME_PROFILE_STOP;
        case GLUSTER_VOLUME_PROFILE_STOP_FAILED:
            return AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED;
        case GLUSTER_HOOK_ENABLE:
            return AuditLogType.GLUSTER_HOOK_ENABLE;
        case GLUSTER_HOOK_ENABLE_FAILED:
            return AuditLogType.GLUSTER_HOOK_ENABLE_FAILED;
        case GLUSTER_HOOK_DISABLE:
            return AuditLogType.GLUSTER_HOOK_DISABLE;
        case GLUSTER_HOOK_DISABLE_FAILED:
            return AuditLogType.GLUSTER_HOOK_DISABLE_FAILED;
        case GLUSTER_HOOK_DETECTED_NEW:
            return AuditLogType.GLUSTER_HOOK_DETECTED_NEW;
        case GLUSTER_HOOK_CONFLICT_DETECTED:
            return AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED;
        case GLUSTER_HOOK_DETECTED_DELETE:
            return AuditLogType.GLUSTER_HOOK_DETECTED_DELETE;
        case GLUSTER_HOOK_ADDED:
            return AuditLogType.GLUSTER_HOOK_ADDED;
        case GLUSTER_HOOK_ADD_FAILED:
            return AuditLogType.GLUSTER_HOOK_ADD_FAILED;
        case GLUSTER_HOOK_REMOVED:
            return AuditLogType.GLUSTER_HOOK_REMOVED;
        case GLUSTER_HOOK_REMOVE_FAILED:
            return AuditLogType.GLUSTER_HOOK_REMOVE_FAILED;
        case GLUSTER_SERVICE_STARTED:
            return AuditLogType.GLUSTER_SERVICE_STARTED;
        case GLUSTER_SERVICE_START_FAILED:
            return AuditLogType.GLUSTER_SERVICE_START_FAILED;
        case GLUSTER_SERVICE_STOPPED:
            return AuditLogType.GLUSTER_SERVICE_STOPPED;
        case GLUSTER_SERVICE_STOP_FAILED:
            return AuditLogType.GLUSTER_SERVICE_STOP_FAILED;
        case GLUSTER_SERVICE_RESTARTED:
            return AuditLogType.GLUSTER_SERVICE_RESTARTED;
        case GLUSTER_SERVICE_RESTART_FAILED:
            return AuditLogType.GLUSTER_SERVICE_RESTART_FAILED;
        case GLUSTER_BRICK_STATUS_CHANGED:
            return AuditLogType.GLUSTER_BRICK_STATUS_CHANGED;
        case GLUSTER_VOLUME_SNAPSHOT_DELETED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETED;
        case GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED;
        case GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED:
            return AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED;
        case GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_ACTIVATED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATED;
        case GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED;
        case GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_RESTORED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORED;
        case GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED:
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED;
        case GLUSTER_VOLUME_CONFIRMED_SPACE_LOW:
            return AuditLogType.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW;
        case DWH_STOPPED:
            return AuditLogType.DWH_STOPPED;
        case DWH_ERROR:
            return AuditLogType.DWH_ERROR;
        case GLUSTER_VOLUME_REBALANCE_FINISHED:
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_FINISHED;
        case GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED:
            return AuditLogType.GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED;
        case HOST_UNTRUSTED:
            return AuditLogType.VDS_UNTRUSTED;
        case USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case FAULTY_MULTIPATHS_ON_HOST:
            return AuditLogType.FAULTY_MULTIPATHS_ON_HOST;
        case NO_FAULTY_MULTIPATHS_ON_HOST:
            return AuditLogType.NO_FAULTY_MULTIPATHS_ON_HOST;
        case MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST:
            return AuditLogType.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST;
        default:
            assert false : "unknown or unnotifiable event: " + model.toString();
            return null;
        }
    }

    @Mapping(from = AuditLogType.class, to = NotifiableEvent.class)
    public static NotifiableEvent map(AuditLogType model, NotifiableEvent template) {
        if (model == null) {
            return null;
        }
        switch (model) {
        case VDC_STOP:
            return NotifiableEvent.ENGINE_STOP;
        case ENGINE_BACKUP_STARTED:
            return NotifiableEvent.ENGINE_BACKUP_STARTED;
        case ENGINE_BACKUP_COMPLETED:
            return NotifiableEvent.ENGINE_BACKUP_COMPLETED;
        case ENGINE_BACKUP_FAILED:
            return NotifiableEvent.ENGINE_BACKUP_FAILED;
        case ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return NotifiableEvent.ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case ENGINE_CA_CERTIFICATION_HAS_EXPIRED:
            return NotifiableEvent.ENGINE_CA_CERTIFICATION_HAS_EXPIRED;
        case ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return NotifiableEvent.ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case ENGINE_CERTIFICATION_HAS_EXPIRED:
            return NotifiableEvent.ENGINE_CERTIFICATION_HAS_EXPIRED;
        case CLUSTER_ALERT_HA_RESERVATION:
            return NotifiableEvent.CLUSTER_ALERT_HA_RESERVATION;
        case NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM:
            return NotifiableEvent.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM;
        case CLUSTER_ALERT_HA_RESERVATION_DOWN:
            return NotifiableEvent.CLUSTER_ALERT_HA_RESERVATION_DOWN;
        case VDS_FAILURE:
            return NotifiableEvent.HOST_FAILURE;
        case HOST_UPDATES_ARE_AVAILABLE:
            return NotifiableEvent.HOST_UPDATES_ARE_AVAILABLE;
        case HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES:
            return NotifiableEvent.HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES;
        case USER_VDS_MAINTENANCE:
            return NotifiableEvent.USER_HOST_MAINTENANCE;
        case USER_VDS_MAINTENANCE_MANUAL_HA:
            return NotifiableEvent.USER_HOST_MAINTENANCE_MANUAL_HA;
        case USER_VDS_MAINTENANCE_MIGRATION_FAILED:
            return NotifiableEvent.USER_HOST_MAINTENANCE_MIGRATION_FAILED;
        case VDS_ACTIVATE_MANUAL_HA:
            return NotifiableEvent.HOST_ACTIVATE_MANUAL_HA;
        case VDS_ACTIVATE_FAILED:
            return NotifiableEvent.HOST_ACTIVATE_FAILED;
        case VDS_RECOVER_FAILED:
            return NotifiableEvent.HOST_RECOVER_FAILED;
        case VDS_APPROVE_FAILED:
            return NotifiableEvent.HOST_APPROVE_FAILED;
        case VDS_INSTALL_FAILED:
            return NotifiableEvent.HOST_INSTALL_FAILED;
        case VDS_TIME_DRIFT_ALERT:
            return NotifiableEvent.HOST_TIME_DRIFT_ALERT;
        case VDS_SET_NONOPERATIONAL:
            return NotifiableEvent.HOST_SET_NONOPERATIONAL;
        case VDS_SET_NONOPERATIONAL_IFACE_DOWN:
            return NotifiableEvent.HOST_SET_NONOPERATIONAL_IFACE_DOWN;
        case VDS_LOW_MEM:
            return NotifiableEvent.HOST_LOW_MEM;
        case VDS_HIGH_MEM_USE:
            return NotifiableEvent.HOST_HIGH_MEM_USE;
        case HOST_INTERFACE_HIGH_NETWORK_USE:
            return NotifiableEvent.HOST_INTERFACE_HIGH_NETWORK_USE;
        case VDS_HIGH_CPU_USE:
            return NotifiableEvent.HOST_HIGH_CPU_USE;
        case VDS_HIGH_SWAP_USE:
            return NotifiableEvent.HOST_HIGH_SWAP_USE;
        case VDS_LOW_SWAP:
            return NotifiableEvent.HOST_LOW_SWAP;
        case HOST_INTERFACE_STATE_DOWN:
            return NotifiableEvent.HOST_INTERFACE_STATE_DOWN;
        case HOST_BOND_SLAVE_STATE_DOWN:
            return NotifiableEvent.HOST_BOND_SLAVE_STATE_DOWN;
        case NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM:
            return NotifiableEvent.NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM;
        case HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE:
            return NotifiableEvent.HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE;
        case HOST_CERTIFICATION_HAS_EXPIRED:
            return NotifiableEvent.HOST_CERTIFICATION_HAS_EXPIRED;
        case HOST_CERTIFICATE_HAS_INVALID_SAN:
            return NotifiableEvent.HOST_CERTIFICATE_HAS_INVALID_SAN;
        case VDS_SET_NONOPERATIONAL_DOMAIN:
            return NotifiableEvent.HOST_SET_NONOPERATIONAL_DOMAIN;
        case SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM:
            return NotifiableEvent.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM;
        case SYSTEM_DEACTIVATED_STORAGE_DOMAIN:
            return NotifiableEvent.SYSTEM_DEACTIVATED_STORAGE_DOMAIN;
        case VM_FAILURE:
            return NotifiableEvent.VM_FAILURE;
        case VM_MIGRATION_START:
            return NotifiableEvent.VM_MIGRATION_START;
        case VM_MIGRATION_FAILED:
            return NotifiableEvent.VM_MIGRATION_FAILED;
        case VM_MIGRATION_TO_SERVER_FAILED:
            return NotifiableEvent.VM_MIGRATION_TO_SERVER_FAILED;
        case VM_NOT_RESPONDING:
            return NotifiableEvent.VM_NOT_RESPONDING;
        case VM_STATUS_RESTORED:
            return NotifiableEvent.VM_STATUS_RESTORED;
        case HA_VM_RESTART_FAILED:
            return NotifiableEvent.HA_VM_RESTART_FAILED;
        case HA_VM_FAILED:
            return NotifiableEvent.HA_VM_FAILED;
        case VM_CONSOLE_CONNECTED:
            return NotifiableEvent.VM_CONSOLE_CONNECTED;
        case VM_CONSOLE_DISCONNECTED:
            return NotifiableEvent.VM_CONSOLE_DISCONNECTED;
        case VM_SET_TICKET:
            return NotifiableEvent.VM_SET_TICKET;
        case VM_DOWN_ERROR:
            return NotifiableEvent.VM_DOWN_ERROR;
        case VDS_INITIATED_RUN_VM_FAILED:
            return NotifiableEvent.HOST_INITIATED_RUN_VM_FAILED;
        case VM_PAUSED:
            return NotifiableEvent.VM_PAUSED;
        case VM_PAUSED_EIO:
            return NotifiableEvent.VM_PAUSED_EIO;
        case VM_PAUSED_ENOSPC:
            return NotifiableEvent.VM_PAUSED_ENOSPC;
        case VM_PAUSED_EPERM:
            return NotifiableEvent.VM_PAUSED_EPERM;
        case VM_PAUSED_ERROR:
            return NotifiableEvent.VM_PAUSED_ERROR;
        case VM_RECOVERED_FROM_PAUSE_ERROR:
            return NotifiableEvent.VM_RECOVERED_FROM_PAUSE_ERROR;
        case MAC_ADDRESS_IS_EXTERNAL:
            return NotifiableEvent.MAC_ADDRESS_IS_EXTERNAL;
        case VDS_SLOW_STORAGE_RESPONSE_TIME:
            return NotifiableEvent.HOST_SLOW_STORAGE_RESPONSE_TIME;
        case IRS_FAILURE:
            return NotifiableEvent.IRS_FAILURE;
        case IRS_DISK_SPACE_LOW:
            return NotifiableEvent.IRS_DISK_SPACE_LOW;
        case IRS_CONFIRMED_DISK_SPACE_LOW:
            return NotifiableEvent.IRS_CONFIRMED_DISK_SPACE_LOW;
        case IRS_DISK_SPACE_LOW_ERROR:
            return NotifiableEvent.IRS_DISK_SPACE_LOW_ERROR;
        case NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD:
            return NotifiableEvent.NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD;
        case GLUSTER_VOLUME_CREATE:
            return NotifiableEvent.GLUSTER_VOLUME_CREATE;
        case GLUSTER_VOLUME_CREATE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_CREATE_FAILED;
        case GLUSTER_VOLUME_OPTION_ADDED:
            return NotifiableEvent.GLUSTER_VOLUME_OPTION_ADDED;
        case GLUSTER_VOLUME_OPTION_MODIFIED:
            return NotifiableEvent.GLUSTER_VOLUME_OPTION_MODIFIED;
        case GLUSTER_VOLUME_OPTION_SET_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_OPTION_SET_FAILED;
        case GLUSTER_VOLUME_START:
            return NotifiableEvent.GLUSTER_VOLUME_START;
        case GLUSTER_VOLUME_START_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_START_FAILED;
        case GLUSTER_VOLUME_STOP:
            return NotifiableEvent.GLUSTER_VOLUME_STOP;
        case GLUSTER_VOLUME_STOP_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_STOP_FAILED;
        case GLUSTER_VOLUME_OPTIONS_RESET:
            return NotifiableEvent.GLUSTER_VOLUME_OPTIONS_RESET;
        case GLUSTER_VOLUME_OPTIONS_RESET_ALL:
            return NotifiableEvent.GLUSTER_VOLUME_OPTIONS_RESET_ALL;
        case GLUSTER_VOLUME_OPTIONS_RESET_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_OPTIONS_RESET_FAILED;
        case GLUSTER_VOLUME_DELETE:
            return NotifiableEvent.GLUSTER_VOLUME_DELETE;
        case GLUSTER_VOLUME_DELETE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_DELETE_FAILED;
        case GLUSTER_VOLUME_REMOVE_BRICKS:
            return NotifiableEvent.GLUSTER_VOLUME_REMOVE_BRICKS;
        case GLUSTER_VOLUME_REMOVE_BRICKS_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS:
            return NotifiableEvent.START_REMOVING_GLUSTER_VOLUME_BRICKS;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED:
            return NotifiableEvent.START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED;
        case GLUSTER_VOLUME_REMOVE_BRICKS_STOP:
            return NotifiableEvent.GLUSTER_VOLUME_REMOVE_BRICKS_STOP;
        case GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED;
        case GLUSTER_VOLUME_REBALANCE_START:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_START;
        case GLUSTER_VOLUME_REBALANCE_START_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_START_FAILED;
        case GLUSTER_VOLUME_REBALANCE_STOP:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_STOP;
        case GLUSTER_VOLUME_REBALANCE_STOP_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_STOP_FAILED;
        case GLUSTER_VOLUME_REPLACE_BRICK_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REPLACE_BRICK_FAILED;
        case GLUSTER_VOLUME_REPLACE_BRICK_START:
            return NotifiableEvent.GLUSTER_VOLUME_REPLACE_BRICK_START;
        case GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED;
        case GLUSTER_VOLUME_BRICK_REPLACED:
            return NotifiableEvent.GLUSTER_VOLUME_BRICK_REPLACED;
        case GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI;
        case START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI:
            return NotifiableEvent.START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI;
        case GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI;
        case REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI:
            return NotifiableEvent.REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI;
        case GLUSTER_VOLUME_SNAPSHOT_CREATED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_CREATED;
        case GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED;
        case GLUSTER_SERVER_ADD_FAILED:
            return NotifiableEvent.GLUSTER_SERVER_ADD_FAILED;
        case GLUSTER_SERVER_REMOVE:
            return NotifiableEvent.GLUSTER_SERVER_REMOVE;
        case GLUSTER_SERVER_REMOVE_FAILED:
            return NotifiableEvent.GLUSTER_SERVER_REMOVE_FAILED;
        case GLUSTER_VOLUME_PROFILE_START:
            return NotifiableEvent.GLUSTER_VOLUME_PROFILE_START;
        case GLUSTER_VOLUME_PROFILE_START_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_PROFILE_START_FAILED;
        case GLUSTER_VOLUME_PROFILE_STOP:
            return NotifiableEvent.GLUSTER_VOLUME_PROFILE_STOP;
        case GLUSTER_VOLUME_PROFILE_STOP_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_PROFILE_STOP_FAILED;
        case GLUSTER_HOOK_ENABLE:
            return NotifiableEvent.GLUSTER_HOOK_ENABLE;
        case GLUSTER_HOOK_ENABLE_FAILED:
            return NotifiableEvent.GLUSTER_HOOK_ENABLE_FAILED;
        case GLUSTER_HOOK_DISABLE:
            return NotifiableEvent.GLUSTER_HOOK_DISABLE;
        case GLUSTER_HOOK_DISABLE_FAILED:
            return NotifiableEvent.GLUSTER_HOOK_DISABLE_FAILED;
        case GLUSTER_HOOK_DETECTED_NEW:
            return NotifiableEvent.GLUSTER_HOOK_DETECTED_NEW;
        case GLUSTER_HOOK_CONFLICT_DETECTED:
            return NotifiableEvent.GLUSTER_HOOK_CONFLICT_DETECTED;
        case GLUSTER_HOOK_DETECTED_DELETE:
            return NotifiableEvent.GLUSTER_HOOK_DETECTED_DELETE;
        case GLUSTER_HOOK_ADDED:
            return NotifiableEvent.GLUSTER_HOOK_ADDED;
        case GLUSTER_HOOK_ADD_FAILED:
            return NotifiableEvent.GLUSTER_HOOK_ADD_FAILED;
        case GLUSTER_HOOK_REMOVED:
            return NotifiableEvent.GLUSTER_HOOK_REMOVED;
        case GLUSTER_HOOK_REMOVE_FAILED:
            return NotifiableEvent.GLUSTER_HOOK_REMOVE_FAILED;
        case GLUSTER_SERVICE_STARTED:
            return NotifiableEvent.GLUSTER_SERVICE_STARTED;
        case GLUSTER_SERVICE_START_FAILED:
            return NotifiableEvent.GLUSTER_SERVICE_START_FAILED;
        case GLUSTER_SERVICE_STOPPED:
            return NotifiableEvent.GLUSTER_SERVICE_STOPPED;
        case GLUSTER_SERVICE_STOP_FAILED:
            return NotifiableEvent.GLUSTER_SERVICE_STOP_FAILED;
        case GLUSTER_SERVICE_RESTARTED:
            return NotifiableEvent.GLUSTER_SERVICE_RESTARTED;
        case GLUSTER_SERVICE_RESTART_FAILED:
            return NotifiableEvent.GLUSTER_SERVICE_RESTART_FAILED;
        case GLUSTER_BRICK_STATUS_CHANGED:
            return NotifiableEvent.GLUSTER_BRICK_STATUS_CHANGED;
        case GLUSTER_VOLUME_SNAPSHOT_DELETED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_DELETED;
        case GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED;
        case GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED:
            return NotifiableEvent.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED;
        case GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_ACTIVATED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_ACTIVATED;
        case GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED;
        case GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED;
        case GLUSTER_VOLUME_SNAPSHOT_RESTORED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_RESTORED;
        case GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED;
        case GLUSTER_VOLUME_CONFIRMED_SPACE_LOW:
            return NotifiableEvent.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW;
        case DWH_STOPPED:
            return NotifiableEvent.DWH_STOPPED;
        case DWH_ERROR:
            return NotifiableEvent.DWH_ERROR;
        case GLUSTER_VOLUME_ADD_BRICK:
            return NotifiableEvent.GLUSTER_VOLUME_ADD_BRICK;
        case GLUSTER_VOLUME_ADD_BRICK_FAILED:
            return NotifiableEvent.GLUSTER_VOLUME_ADD_BRICK_FAILED;
        case GLUSTER_VOLUME_REBALANCE_FINISHED:
            return NotifiableEvent.GLUSTER_VOLUME_REBALANCE_FINISHED;
        case GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED:
            return NotifiableEvent.GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED;
        case VDS_UNTRUSTED:
            return NotifiableEvent.HOST_UNTRUSTED;
        case USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED;
        case IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED:
            return NotifiableEvent.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED;
        case USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED:
            return NotifiableEvent.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED;
        case FAULTY_MULTIPATHS_ON_HOST:
            return NotifiableEvent.FAULTY_MULTIPATHS_ON_HOST;
        case NO_FAULTY_MULTIPATHS_ON_HOST:
            return NotifiableEvent.NO_FAULTY_MULTIPATHS_ON_HOST;
        case MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST:
            return NotifiableEvent.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST;
        default:
            assert false : "unknown or unnotifiable event: " + model.toString();
            return null;
        }
    }
}
