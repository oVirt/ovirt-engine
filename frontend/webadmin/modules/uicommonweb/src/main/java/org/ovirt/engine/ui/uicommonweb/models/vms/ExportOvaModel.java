package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ExportOvaModel extends Model {

    private EntityModel<String> name;
    private EntityModel<String> path;
    private ListModel<VDS> proxy;

    public ExportOvaModel(String entityName) {
        this();
        getName().setIsAvailable(true);
        getName().setEntity(entityName + ".ova"); ////$NON-NLS-1$
    }

    public ExportOvaModel() {
        setName(new EntityModel<>());
        setPath(new EntityModel<>());
        setProxy(new ListModel<>());
        getName().setIsAvailable(false);
    }

    public EntityModel<String> getPath() {
        return path;
    }

    public void setPath(EntityModel<String> path) {
        this.path = path;
    }

    public ListModel<VDS> getProxy() {
        return proxy;
    }

    public void setProxy(ListModel<VDS> proxy) {
        this.proxy = proxy;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public boolean validate() {
        getPath().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getName().validateEntity(new IValidation[] { new NotEmptyValidation() });

        return getPath().getIsValid() && getName().getIsValid();
    }
}
