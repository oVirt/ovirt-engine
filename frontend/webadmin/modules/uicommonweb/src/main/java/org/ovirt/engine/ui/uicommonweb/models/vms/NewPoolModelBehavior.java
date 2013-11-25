package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NewPoolNameLengthValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewPoolModelBehavior extends PoolModelBehaviorBase {

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        getModel().getVmType().setIsChangable(true);

        templateValidate();
    }

    @Override
    protected void postDataCentersLoaded(List<StoragePool> dataCenters) {
        if (!dataCenters.isEmpty()) {
            super.postDataCentersLoaded(dataCenters);
        } else {
            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
        }
    }

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        getModel().getTemplate().setSelectedItem(Linq.<VmTemplate> firstOrDefault(templates));
    }

    @Override
    public void template_SelectedItemChanged() {
        super.template_SelectedItemChanged();
        VmTemplate template = getModel().getTemplate().getSelectedItem();

        if (template == null) {
            return;
        }

        setupWindowModelFrom(template);
        updateHostPinning(template.getMigrationSupport());
        doChangeDefautlHost(template.getDedicatedVmForVds());
    }

    @Override
    protected DisplayType extractDisplayType(VmBase vmBase) {
        if (vmBase instanceof VmTemplate) {
            return ((VmTemplate) vmBase).getDefaultDisplayType();
        }

        return null;
    }

    @Override
    public boolean validate() {
        boolean parentValidation = super.validate();
        if (getModel().getName().getIsValid()) {
            getModel().getName().validateEntity(new IValidation[] { new NewPoolNameLengthValidation(
                    getModel().getName().getEntity(),
                    getModel().getNumOfDesktops().getEntity(),
                    getModel().getOSType().getSelectedItem()
                    ) });

            return getModel().getName().getIsValid() && parentValidation;
        }

        return parentValidation;
    }

    private void templateValidate() {
         AsyncDataProvider.countAllTemplates(new AsyncQuery(getModel(), new INewAsyncCallback() {
             @Override
             public void onSuccess(Object model, Object returnValue) {
                 int count = (Integer) returnValue;
                 if(count <= 1) {
                     getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoTemplates());
                 }
             }
         }));

    }
}
