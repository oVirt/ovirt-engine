package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageRegisterVmListModel extends StorageRegisterEntityListModel {

    public StorageRegisterVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmImportTitle());
        setHelpTag(HelpTag.vm_register);
        setHashName("vm_register"); //$NON-NLS-1$
    }

    @Override
    RegisterEntityModel createRegisterEntityModel() {
        RegisterVmModel model = new RegisterVmModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        model.setHelpTag(HelpTag.register_virtual_machine);
        model.setHashName("register_virtual_machine"); //$NON-NLS-1$

        return model;
    }

    @Override
    ImportEntityData createImportEntityData(Object entity) {
        return new ImportVmData((VM) entity);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        IdQueryParameters parameters = new IdQueryParameters((getEntity()).getId());
        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(VdcQueryType.GetUnregisteredVms, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        List<VM> vms = (ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        Collections.sort(vms, new Linq.VmComparator());
                        setItems(vms);
                    }
                }));
    }

    @Override
    protected String getListName() {
        return "StorageRegisterVmListModel"; //$NON-NLS-1$
    }
}
