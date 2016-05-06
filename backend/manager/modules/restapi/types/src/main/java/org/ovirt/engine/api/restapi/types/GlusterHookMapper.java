package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.model.GlusterHookStatus;
import org.ovirt.engine.api.model.GlusterServerHooks;
import org.ovirt.engine.api.model.HookContentType;
import org.ovirt.engine.api.model.HookStage;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;

public class GlusterHookMapper {

    @Mapping(from = GlusterHook.class, to = GlusterHookEntity.class)
    public static GlusterHookEntity map(GlusterHook hook, GlusterHookEntity entity) {
        GlusterHookEntity hookEntity = entity != null ? entity : new GlusterHookEntity();

        if (hook.isSetId()) {
            hookEntity.setId(GuidUtils.asGuid(hook.getId()));
        }

        if (hook.isSetName()) {
            hookEntity.setName(hook.getName());
        }

        if (hook.isSetGlusterCommand()) {
            hookEntity.setGlusterCommand(hook.getGlusterCommand());
        }

        if (hook.isSetChecksum()) {
            hookEntity.setChecksum(hook.getChecksum());
        }

        if (hook.isSetContent()) {
            hookEntity.setContent(hook.getContent());
        }

        return hookEntity;
    }

    @Mapping(from = GlusterHookEntity.class, to = GlusterHook.class)
    public static GlusterHook map(GlusterHookEntity entity, GlusterHook hook) {
        GlusterHook model = hook != null ? hook : new GlusterHook();

        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }

        if (entity.getClusterId() != null) {
            model.setCluster(new Cluster());
            model.getCluster().setId(entity.getClusterId().toString());
        }

        if (entity.getHookKey() != null) {
            model.setName(entity.getHookKey());
        }

        if (entity.getGlusterCommand() != null) {
            model.setGlusterCommand(entity.getGlusterCommand());
        }

        if (entity.getStage() != null) {
            model.setStage(map(entity.getStage(), null));
        }

        if (entity.getStatus() != null) {
            model.setStatus(mapHookStatus(entity.getStatus()));
        }

        if (entity.getContentType() != null) {
            model.setContentType(map(entity.getContentType(), null));
        }

        if (entity.getChecksum() != null) {
            model.setChecksum(entity.getChecksum());
        }

        if (entity.getContent() != null) {
            model.setContent(entity.getContent());
        }

        if (entity.getConflictStatus() != null) {
            model.setConflictStatus(entity.getConflictStatus());
            model.setConflicts(mapConflicts(entity));
        }

        if (entity.getServerHooks() != null && !entity.getServerHooks().isEmpty()) {
            model.setServerHooks(new GlusterServerHooks());
            for (GlusterServerHook serverHookEntity : entity.getServerHooks()) {
                model.getServerHooks().getGlusterServerHooks().add(map(serverHookEntity));
            }
        }

        return model;
    }

    private static String mapConflicts(GlusterHookEntity hook) {
        StringBuilder sb = new StringBuilder();
        if (hook.isMissingHookConflict()) {
            sb.append(GlusterHookConflictFlags.MISSING_HOOK.toString());
        }
        if (hook.isContentConflict()) {
            sb.append(sb.length() > 0 ? ",":"").append(GlusterHookConflictFlags.CONTENT_CONFLICT.toString());
        }
        if (hook.isStatusConflict()) {
            sb.append(sb.length() > 0 ? ",":"").append(GlusterHookConflictFlags.STATUS_CONFLICT.toString());
        }
        return sb.toString();
    }

    @Mapping (from=org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook.class, to=org.ovirt.engine.api.model.GlusterServerHook.class)
    public static org.ovirt.engine.api.model.GlusterServerHook map(GlusterServerHook serverHookEntity) {
        org.ovirt.engine.api.model.GlusterServerHook serverHookModel = new org.ovirt.engine.api.model.GlusterServerHook();
        if (serverHookEntity.getServerId() != null) {
            serverHookModel.setHost(new Host());
            serverHookModel.getHost().setId(serverHookEntity.getServerId().toString());
        }
        return serverHookModel;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType.class, to = HookContentType.class)
    public static HookContentType map(org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType hookContentType,
             HookContentType template) {
        switch (hookContentType) {
        case BINARY:
            return HookContentType.BINARY;
        case TEXT:
            return HookContentType.TEXT;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage.class, to = HookStage.class)
    public static HookStage map(org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage hookStage,
            HookStage template) {
        switch (hookStage) {
        case POST:
            return HookStage.POST;
        case PRE:
            return HookStage.PRE;
        default:
            return null;
        }
    }

    private static GlusterHookStatus mapHookStatus(org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus status) {
        switch (status) {
        case DISABLED:
            return GlusterHookStatus.DISABLED;
        case ENABLED:
            return GlusterHookStatus.ENABLED;
        case MISSING:
            return GlusterHookStatus.MISSING;
        default:
            return null;
        }
    }
}
