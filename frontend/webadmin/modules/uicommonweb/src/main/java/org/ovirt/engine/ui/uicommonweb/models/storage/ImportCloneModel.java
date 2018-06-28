package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplatesModel;

public class ImportCloneModel extends ConfirmationModel {

    EntityModel<Boolean> noClone;
    EntityModel<Boolean> clone;
    EntityModel<Boolean> applyToAll;

    EntityModel<String> suffix;
    EntityModel<String> name;

    private Object entity;

    private ImportTemplatesModel importTemplatesModel;

    public ImportTemplatesModel getImportTemplatesModel() {
        return importTemplatesModel;
    }

    public void setImportTemplatesModel(ImportTemplatesModel importTemplatesModel) {
        this.importTemplatesModel = importTemplatesModel;
    }

    public EntityModel<Boolean> getNoClone() {
        return noClone;
    }

    public void setNoClone(EntityModel<Boolean> noClone) {
        this.noClone = noClone;
    }

    public EntityModel<Boolean> getClone() {
        return clone;
    }

    public void setClone(EntityModel<Boolean> clone) {
        this.clone = clone;
    }

    public EntityModel<Boolean> getApplyToAll() {
        return applyToAll;
    }

    public void setApplyToAll(EntityModel<Boolean> applyToAll) {
        this.applyToAll = applyToAll;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getSuffix() {
        return suffix;
    }

    public void setSuffix(EntityModel<String> suffix) {
        this.suffix = suffix;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public ImportCloneModel() {
        setNoClone(new EntityModel<Boolean>());
        getNoClone().setEntity(false);
        setClone(new EntityModel<Boolean>());
        getClone().setEntity(true);
        setName(new EntityModel<String>());
        setApplyToAll(new EntityModel<Boolean>());
        getApplyToAll().setEntity(false);
        setSuffix(new EntityModel<String>());
        getSuffix().setIsChangeable(false);
        getSuffix().setEntity("_Copy"); //$NON-NLS-1$
        getClone().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean value = getClone().getEntity();
            if (value) {
                getNoClone().setEntity(false);
                if (getApplyToAll().getEntity()) {
                    getSuffix().setIsChangeable(true);
                } else {
                    getName().setIsChangeable(true);
                }
            }
        });
        getNoClone().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean value = getNoClone().getEntity();
            if (value) {
                getClone().setEntity(false);
                getName().setIsChangeable(false);
                getSuffix().setIsChangeable(false);
            }
        });
        getApplyToAll().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!getNoClone().getEntity()) {
                Boolean value = getApplyToAll().getEntity();
                getSuffix().setIsChangeable(value);
                getName().setIsChangeable(!value);
            }
        });
    }
}
