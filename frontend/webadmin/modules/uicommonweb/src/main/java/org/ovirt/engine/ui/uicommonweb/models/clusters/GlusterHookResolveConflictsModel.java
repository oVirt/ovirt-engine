package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class GlusterHookResolveConflictsModel extends Model {

    private GlusterHookEntity hookEntity;

    private ListModel<EntityModel<GlusterServerHook>> hookSources;

    private GlusterHookContentModel contentModel;

    private EntityModel<Boolean> resolveContentConflict;

    private ListModel<GlusterServerHook> serverHooksList;

    private EntityModel<Boolean> resolveStatusConflict;

    private EntityModel<Boolean> resolveStatusConflictEnable;

    private EntityModel<Boolean> resolveStatusConflictDisable;

    private EntityModel<Boolean> resolveMissingConflict;

    private EntityModel<Boolean> resolveMissingConflictCopy;

    private EntityModel<Boolean> resolveMissingConflictRemove;

    public GlusterHookEntity getGlusterHookEntity() {
        return hookEntity;
    }

    public void setGlusterHookEntity(GlusterHookEntity hookEntity) {
        this.hookEntity = hookEntity;
        if (hookEntity != null) {
            getResolveContentConflict().setEntity(hookEntity.isContentConflict());
            getResolveStatusConflict().setEntity(hookEntity.isStatusConflict());
            getResolveMissingConflict().setEntity(hookEntity.isMissingHookConflict());
        }
    }

    public ListModel<EntityModel<GlusterServerHook>> getHookSources() {
        return hookSources;
    }

    public void setHookSources(ListModel<EntityModel<GlusterServerHook>> hookSources) {
        this.hookSources = hookSources;
    }

    public ListModel<GlusterServerHook> getServerHooksList() {
        return serverHooksList;
    }

    public void setServerHooksList(ListModel<GlusterServerHook> serverHooksList) {
        this.serverHooksList = serverHooksList;
    }

    public GlusterHookContentModel getContentModel() {
        return contentModel;
    }

    public void setContentModel(GlusterHookContentModel contentModel) {
        this.contentModel = contentModel;
    }

    public EntityModel<Boolean> getResolveContentConflict() {
        return resolveContentConflict;
    }

    public void setResolveContentConflict(EntityModel<Boolean> resolveContentConflict) {
        this.resolveContentConflict = resolveContentConflict;
    }

    public EntityModel<Boolean> getResolveStatusConflict() {
        return resolveStatusConflict;
    }

    public void setResolveStatusConflict(EntityModel<Boolean> resolveStatusConflict) {
        this.resolveStatusConflict = resolveStatusConflict;
    }

    public EntityModel<Boolean> getResolveStatusConflictEnable() {
        return resolveStatusConflictEnable;
    }

    public void setResolveStatusConflictEnable(EntityModel<Boolean> resolveStatusConflictEnable) {
        this.resolveStatusConflictEnable = resolveStatusConflictEnable;
    }

    public EntityModel<Boolean> getResolveStatusConflictDisable() {
        return resolveStatusConflictDisable;
    }

    public void setResolveStatusConflictDisable(EntityModel<Boolean> resolveStatusConflictDisable) {
        this.resolveStatusConflictDisable = resolveStatusConflictDisable;
    }

    public EntityModel<Boolean> getResolveMissingConflict() {
        return resolveMissingConflict;
    }

    public void setResolveMissingConflict(EntityModel<Boolean> resolveMissingConflict) {
        this.resolveMissingConflict = resolveMissingConflict;
    }

    public EntityModel<Boolean> getResolveMissingConflictCopy() {
        return resolveMissingConflictCopy;
    }

    public void setResolveMissingConflictCopy(EntityModel<Boolean> resolveMissingConflictCopy) {
        this.resolveMissingConflictCopy = resolveMissingConflictCopy;
    }

    public EntityModel<Boolean> getResolveMissingConflictRemove() {
        return resolveMissingConflictRemove;
    }

    public void setResolveMissingConflictRemove(EntityModel<Boolean> resolveMissingConflictRemove) {
        this.resolveMissingConflictRemove = resolveMissingConflictRemove;
    }

    public GlusterHookResolveConflictsModel() {
        setHookSources(new ListModel<EntityModel<GlusterServerHook>>());
        setContentModel(new GlusterHookContentModel());

        setResolveContentConflict(new EntityModel<Boolean>());
        setServerHooksList(new ListModel<GlusterServerHook>());

        setResolveStatusConflict(new EntityModel<Boolean>());
        setResolveStatusConflictEnable(new EntityModel<Boolean>());
        setResolveStatusConflictDisable(new EntityModel<Boolean>());

        setResolveMissingConflict(new EntityModel<Boolean>());
        setResolveMissingConflictCopy(new EntityModel<Boolean>());
        setResolveMissingConflictRemove(new EntityModel<Boolean>());

        getHookSources().getSelectedItemChangedEvent().addListener((ev, sender, args) -> onSelectedHookSourceChanged());

        getResolveContentConflict().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveContentConflict().getEntity() == null) {
                getServerHooksList().setIsChangeable(false);
            } else {
                getServerHooksList().setIsChangeable(getResolveContentConflict().getEntity());
            }
        });

        getResolveStatusConflict().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveStatusConflict().getEntity() == null) {
                getResolveStatusConflictEnable().setIsChangeable(false);
                getResolveStatusConflictDisable().setIsChangeable(false);
            } else {
                getResolveStatusConflictEnable().setIsChangeable(getResolveStatusConflict().getEntity());
                getResolveStatusConflictDisable().setIsChangeable(getResolveStatusConflict().getEntity());
            }
        });

        getResolveStatusConflictEnable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveStatusConflictEnable().getEntity()) {
                getResolveStatusConflictDisable().setEntity(Boolean.FALSE);
            }
        });

        getResolveStatusConflictDisable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveStatusConflictDisable().getEntity()) {
                getResolveStatusConflictEnable().setEntity(Boolean.FALSE);
            }
        });

        getResolveMissingConflict().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveMissingConflict().getEntity() == null) {
                getResolveMissingConflictCopy().setIsChangeable(false);
                getResolveMissingConflictRemove().setIsChangeable(false);
            } else {
                getResolveMissingConflictCopy().setIsChangeable(getResolveMissingConflict().getEntity());
                getResolveMissingConflictRemove().setIsChangeable(getResolveMissingConflict().getEntity());
            }
        });

        getResolveMissingConflictCopy().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveMissingConflictCopy().getEntity()) {
                getResolveMissingConflictRemove().setEntity(Boolean.FALSE);
            }
        });

        getResolveMissingConflictRemove().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getResolveMissingConflictRemove().getEntity()) {
                getResolveMissingConflictCopy().setEntity(Boolean.FALSE);
            }

            updateConflictActionsAvailability(getResolveMissingConflictRemove().getEntity());
        });


        getResolveContentConflict().setEntity(Boolean.FALSE);
        getResolveStatusConflict().setEntity(Boolean.FALSE);
        getResolveStatusConflictEnable().setEntity(Boolean.TRUE);
        getResolveStatusConflictDisable().setEntity(Boolean.FALSE);
        getResolveMissingConflictCopy().setEntity(Boolean.TRUE);
        getResolveMissingConflictRemove().setEntity(Boolean.FALSE);
    }

    private void updateConflictActionsAvailability(boolean isRemove) {
        getResolveContentConflict().setEntity(!isRemove);
        getResolveContentConflict().setIsChangeable(!isRemove);
        getResolveStatusConflict().setEntity(!isRemove);
        getResolveStatusConflict().setIsChangeable(!isRemove);
    }

    private void onSelectedHookSourceChanged() {
        EntityModel<GlusterServerHook> selectedItem = getHookSources().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        GlusterServerHook selectedServer = selectedItem.getEntity();
        getServerHooksList().setSelectedItem(selectedServer);

        if (selectedServer.getStatus() == GlusterHookStatus.MISSING) {
            getContentModel().getContent().setEntity(null);
            getContentModel().getStatus().setEntity(null);
            getContentModel().getMd5Checksum().setEntity(null);
            return;
        }

        getContentModel().getStatus().setEntity(selectedServer.getStatus());
        getContentModel().getMd5Checksum().setEntity(selectedServer.getChecksum());

        if (selectedServer.getContentType() == GlusterHookContentType.TEXT) {
            startProgress();
            AsyncDataProvider.getInstance().getGlusterHookContent(new AsyncQuery<>(content -> {
                getContentModel().getContent().setEntity(content);
                stopProgress();
            }), getGlusterHookEntity().getId(), selectedServer.getServerId());
        } else {
            getContentModel().getContent().setEntity(null);
        }
    }

    public boolean isAnyResolveActionSelected() {
        return getResolveContentConflict().getEntity() || getResolveStatusConflict().getEntity()
                || getResolveMissingConflict().getEntity();
    }
}
