package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportGeneralModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportVmGeneralSubTabView;

import com.google.gwt.user.client.ui.ScrollPanel;

public class RegisterVmInfoPanel extends RegisterEntityInfoPanel<VM> {

    private ImportVmGeneralSubTabView generalView;
    private VmImportGeneralModel vmImportGeneralModel;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public RegisterVmInfoPanel(RegisterEntityModel<VM> model) {
        super(model);
    }

    @Override
    protected void init() {
        // Initialize Tables
        initGeneralForm();
        initDisksTable();
        initNicsTable();
        initAppsTable();

        // Add Tabs
        add(new ScrollPanel(generalView.asWidget()), constants.generalLabel());
        add(new ScrollPanel(disksTable), constants.disksLabel());
        add(new ScrollPanel(nicsTable), constants.nicsLabel());
        add(new ScrollPanel(appsTable), constants.applicationsLabel());
    }

    @Override
    public void updateTabsData(ImportEntityData<VM> importEntityData) {
        ImportVmData importVmData = (ImportVmData) importEntityData;
        VM vm = importVmData.getVm();

        vmImportGeneralModel.setEntity(importVmData);
        generalView.setMainTabSelectedItem(vm);

        disksTable.setRowData((List) Arrays.asList(vm.getDiskMap().values().toArray()));
        nicsTable.setRowData((List) Arrays.asList(vm.getInterfaces().toArray()));
        appsTable.setRowData((List) Arrays.asList(vm.getAppList() != null ?
                vm.getAppList().split("[,]", -1) : new ArrayList<String>())); //$NON-NLS-1$
    }

    private void initGeneralForm() {
        DetailModelProvider<ImportVmFromExportDomainModel, VmImportGeneralModel> modelProvider =
            new DetailModelProvider<ImportVmFromExportDomainModel, VmImportGeneralModel>() {
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
            };
        generalView = new ImportVmGeneralSubTabView(modelProvider);
    }

    public VmImportGeneralModel getVmGeneralModel() {
        if (vmImportGeneralModel == null) {
            vmImportGeneralModel = new VmImportGeneralModel();
        }
        return vmImportGeneralModel;
    }
}
