package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportVmGeneralSubTabView;

import com.google.gwt.user.client.ui.ScrollPanel;

public class RegisterVmInfoPanel extends RegisterEntityInfoPanel<VM, RegisterVmData, RegisterVmModel> {

    private ImportVmGeneralSubTabView generalView;
    private VmImportGeneralModel vmImportGeneralModel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public RegisterVmInfoPanel(RegisterVmModel model) {
        super(model);
    }

    @Override
    protected void init() {
        // Initialize Tables
        initGeneralForm();
        initDisksTable();
        initNicsTable();
        initAppsTable();
        initContainersTable();

        // Add Tabs
        add(new ScrollPanel(generalView.asWidget()), constants.generalLabel());
        add(new ScrollPanel(disksTable), constants.disksLabel());
        add(new ScrollPanel(nicsTable), constants.nicsLabel());
        add(new ScrollPanel(appsTable), constants.applicationsLabel());
        add(new ScrollPanel(containersTable), constants.containersLabel());
    }

    @Override
    public void updateTabsData(ImportEntityData<VM> importEntityData) {
        ImportVmData importVmData = (ImportVmData) importEntityData;
        VM vm = importVmData.getVm();

        vmImportGeneralModel.setEntity(importVmData);
        generalView.setMainSelectedItem(vm);

        disksTable.setRowData((List) Arrays.asList(vm.getDiskMap().values().toArray()));
        nicsTable.setRowData((List) Arrays.asList(vm.getInterfaces().toArray()));
        appsTable.setRowData(vm.getAppList() == null ? Collections.emptyList() :
                (List) Arrays.asList(vm.getAppList().split("[,]", -1))); //$NON-NLS-1$
    }

    private void initGeneralForm() {
        DetailModelProvider<ImportVmModel, VmImportGeneralModel> modelProvider =
            new DetailModelProvider<ImportVmModel, VmImportGeneralModel>() {
                @Override
                public VmImportGeneralModel getModel() {
                    return getVmGeneralModel();
                }

                @Override
                public void onSubTabSelected() {
                }

                @Override
                public void onSubTabDeselected() {
                }

                @Override
                public void activateDetailModel() {
                }

                @Override
                public ImportVmModel getMainModel() {
                    // Not used, here to satisfy interface contract.
                    return null;
                }
            };
        generalView = new ImportVmGeneralSubTabView(modelProvider);
    }

    public VmImportGeneralModel getVmGeneralModel() {
        if (vmImportGeneralModel == null) {
            vmImportGeneralModel = new VmImportGeneralModel();
            vmImportGeneralModel.setSource(ImportSource.EXPORT_DOMAIN);
        }
        return vmImportGeneralModel;
    }
}
