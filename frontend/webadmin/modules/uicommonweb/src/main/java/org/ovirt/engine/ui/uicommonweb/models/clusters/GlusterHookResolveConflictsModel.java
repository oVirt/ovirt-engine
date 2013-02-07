package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class GlusterHookResolveConflictsModel extends Model {

    private GlusterHookEntity hookEntity;

    private ListModel hookSources;

    private GlusterHookContentModel contentModel;

    private EntityModel resolveContentConflict;

    private ListModel serverHooksList;

    private EntityModel resolveStatusConflict;

    private EntityModel resolveStatusConflictEnable;

    private EntityModel resolveStatusConflictDisable;

    private EntityModel resolveMissingConflict;

    private EntityModel resolveMissingConflictCopy;

    private EntityModel resolveMissingConflictRemove;

    public GlusterHookEntity getGlusterHookEntiry() {
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

    public ListModel getHookSources() {
        return hookSources;
    }

    public void setHookSources(ListModel hookSources) {
        this.hookSources = hookSources;
    }

    public ListModel getServerHooksList() {
        return serverHooksList;
    }

    public void setServerHooksList(ListModel serverHooksList) {
        this.serverHooksList = serverHooksList;
    }

    public GlusterHookContentModel getContentModel() {
        return contentModel;
    }

    public void setContentModel(GlusterHookContentModel contentModel) {
        this.contentModel = contentModel;
    }

    public EntityModel getResolveContentConflict() {
        return resolveContentConflict;
    }

    public void setResolveContentConflict(EntityModel resolveContentConflict) {
        this.resolveContentConflict = resolveContentConflict;
    }

    public EntityModel getResolveStatusConflict() {
        return resolveStatusConflict;
    }

    public void setResolveStatusConflict(EntityModel resolveStatusConflict) {
        this.resolveStatusConflict = resolveStatusConflict;
    }

    public EntityModel getResolveStatusConflictEnable() {
        return resolveStatusConflictEnable;
    }

    public void setResolveStatusConflictEnable(EntityModel resolveStatusConflictEnable) {
        this.resolveStatusConflictEnable = resolveStatusConflictEnable;
    }

    public EntityModel getResolveStatusConflictDisable() {
        return resolveStatusConflictDisable;
    }

    public void setResolveStatusConflictDisable(EntityModel resolveStatusConflictDisable) {
        this.resolveStatusConflictDisable = resolveStatusConflictDisable;
    }

    public EntityModel getResolveMissingConflict() {
        return resolveMissingConflict;
    }

    public void setResolveMissingConflict(EntityModel resolveMissingConflict) {
        this.resolveMissingConflict = resolveMissingConflict;
    }

    public EntityModel getResolveMissingConflictCopy() {
        return resolveMissingConflictCopy;
    }

    public void setResolveMissingConflictCopy(EntityModel resolveMissingConflictCopy) {
        this.resolveMissingConflictCopy = resolveMissingConflictCopy;
    }

    public EntityModel getResolveMissingConflictRemove() {
        return resolveMissingConflictRemove;
    }

    public void setResolveMissingConflictRemove(EntityModel resolveMissingConflictRemove) {
        this.resolveMissingConflictRemove = resolveMissingConflictRemove;
    }

    public GlusterHookResolveConflictsModel() {
        setHookSources(new ListModel());
        setContentModel(new GlusterHookContentModel());

        setResolveContentConflict(new EntityModel());
        setServerHooksList(new ListModel());

        setResolveStatusConflict(new EntityModel());
        setResolveStatusConflictEnable(new EntityModel());
        setResolveStatusConflictDisable(new EntityModel());

        setResolveMissingConflict(new EntityModel());
        setResolveMissingConflictCopy(new EntityModel());
        setResolveMissingConflictRemove(new EntityModel());

        getHookSources().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onSelectedHookSourceChanged();
            }
        });

        getResolveContentConflict().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if(getResolveContentConflict().getEntity() == null) {
                    getServerHooksList().setIsChangable(false);
                }
                else {
                    getServerHooksList().setIsChangable((Boolean) getResolveContentConflict().getEntity());
                }
            }
        });

        getResolveStatusConflict().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getResolveStatusConflict().getEntity() == null) {
                    getResolveStatusConflictEnable().setIsChangable(false);
                    getResolveStatusConflictDisable().setIsChangable(false);
                }
                else {
                    getResolveStatusConflictEnable().setIsChangable((Boolean) getResolveStatusConflict().getEntity());
                    getResolveStatusConflictDisable().setIsChangable((Boolean) getResolveStatusConflict().getEntity());
                }
            }
        });

        getResolveStatusConflictEnable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getResolveStatusConflictEnable().getEntity()) {
                    getResolveStatusConflictDisable().setEntity(Boolean.FALSE);
                }
            }
        });

        getResolveStatusConflictDisable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getResolveStatusConflictDisable().getEntity()) {
                    getResolveStatusConflictEnable().setEntity(Boolean.FALSE);
                }
            }
        });

        getResolveMissingConflict().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getResolveMissingConflict().getEntity() == null) {
                    getResolveMissingConflictCopy().setIsChangable(false);
                    getResolveMissingConflictRemove().setIsChangable(false);
                }
                else {
                    getResolveMissingConflictCopy().setIsChangable((Boolean) getResolveMissingConflict().getEntity());
                    getResolveMissingConflictRemove().setIsChangable((Boolean) getResolveMissingConflict().getEntity());
                }
            }
        });

        getResolveMissingConflictCopy().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getResolveMissingConflictCopy().getEntity()) {
                    getResolveMissingConflictRemove().setEntity(Boolean.FALSE);
                }
            }
        });

        getResolveMissingConflictRemove().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getResolveMissingConflictRemove().getEntity()) {
                    getResolveMissingConflictCopy().setEntity(Boolean.FALSE);
                }
            }
        });


        getResolveContentConflict().setEntity(Boolean.FALSE);
        getResolveStatusConflict().setEntity(Boolean.FALSE);
        getResolveStatusConflictEnable().setEntity(Boolean.TRUE);
        getResolveStatusConflictDisable().setEntity(Boolean.FALSE);
        getResolveMissingConflictCopy().setEntity(Boolean.TRUE);
        getResolveMissingConflictRemove().setEntity(Boolean.FALSE);
    }

    private void onSelectedHookSourceChanged() {
        EntityModel selectedItem = (EntityModel) getHookSources().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        startProgress(null);

        GlusterServerHook selectedServer = (GlusterServerHook) selectedItem.getEntity();
        getServerHooksList().setSelectedItem(selectedServer);

        if (selectedServer.getStatus() == GlusterHookStatus.MISSING) {
            getContentModel().getContent().setEntity(null);
            getContentModel().getStatus().setEntity(null);
            getContentModel().getMd5Checksum().setEntity(null);
            stopProgress();
            return;
        }

        AsyncDataProvider.getGlusterHookContent(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                String content = (String) returnValue;
                getContentModel().getContent().setEntity(content);
                stopProgress();
            }
        }), getGlusterHookEntiry().getId(), selectedServer.getServerId());

        if (selectedServer.getServerId() == null) {
            getContentModel().getStatus().setEntity(getGlusterHookEntiry().getStatus());
            getContentModel().getMd5Checksum().setEntity(getGlusterHookEntiry().getChecksum());
        }
        else {
            for (GlusterServerHook serverHook : getGlusterHookEntiry().getServerHooks()) {
                if (serverHook.getServerId() != null
                        && serverHook.getServerId().equals(selectedServer.getServerId())) {
                    getContentModel().getStatus().setEntity(serverHook.getStatus());
                    getContentModel().getMd5Checksum().setEntity(serverHook.getChecksum());
                    break;
                }
            }
        }
    }

    public boolean isAnyResolveActionSelected() {
        return (Boolean) getResolveContentConflict().getEntity() || (Boolean) getResolveStatusConflict().getEntity()
                || (Boolean) getResolveMissingConflict().getEntity();
    }
}
