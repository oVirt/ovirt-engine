package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RegisterVmPopupView extends RegisterEntityPopupView<VM, ImportVmData, RegisterVmModel>
        implements RegisterVmPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RegisterVmModel, RegisterEntityPopupView<VM, ImportVmData, RegisterVmModel>> {
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RegisterVmPopupView(EventBus eventBus, Driver driver) {
        super(eventBus, driver);
    }

    @Override
    protected void createEntityTable(RegisterVmModel model) {
        AbstractTextColumn<ImportVmData> nameColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData importVmData) {
                return importVmData.getVm().getName();
            }
        };
        entityTable.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> originColumn = new AbstractEnumColumn<ImportVmData, OriginType>() {
            @Override
            protected OriginType getRawValue(ImportVmData importVmData) {
                return importVmData.getVm().getOrigin();
            }
        };
        entityTable.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> memoryColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData importVmData) {
                int size = importVmData.getVm().getVmMemSizeMb();
                return size + " MB"; //$NON-NLS-1$
            }
        };
        entityTable.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> cpuColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData importVmData) {
                int numOfCpus = importVmData.getVm().getNumOfCpus();
                return String.valueOf(numOfCpus);
            }
        };
        entityTable.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> archColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData importVmData) {
                ArchitectureType clusterArch = importVmData.getVm().getClusterArch();
                return String.valueOf(clusterArch);
            }
        };
        entityTable.addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> diskColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData importVmData) {
                int numOfDisks = importVmData.getVm().getDiskMap().size();
                return String.valueOf(numOfDisks);
            }
        };
        entityTable.addColumn(diskColumn, constants.disksVm(), "50px"); //$NON-NLS-1$

        entityTable.addColumn(getClusterColumn(), constants.clusterVm(), "150px"); //$NON-NLS-1$

        if (model.isQuotaEnabled()) {
            entityTable.addColumn(getClusterQuotaColumn(), constants.quotaVm(), "150px"); //$NON-NLS-1$
        }
    }

    @Override
    protected void createInfoPanel(RegisterVmModel model) {
        registerEntityInfoPanel = new RegisterVmInfoPanel(model);
        entityInfoContainer.add(registerEntityInfoPanel);
    }

    @Override
    protected AbstractUiCommandButton createCommandButton(String label, String uniqueId) {
        if (RegisterVmModel.VNIC_PROFILE_MAPPING_COMMAND.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        } else {
            return super.createCommandButton(label, uniqueId);
        }
    }
}
