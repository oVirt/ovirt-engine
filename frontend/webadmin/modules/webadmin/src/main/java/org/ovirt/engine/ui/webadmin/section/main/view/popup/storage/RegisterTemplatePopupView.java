package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterTemplateModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterTemplatePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RegisterTemplatePopupView extends RegisterEntityPopupView<VmTemplate, ImportTemplateData, RegisterTemplateModel>
        implements RegisterTemplatePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<
            RegisterTemplateModel,
            RegisterEntityPopupView<VmTemplate, ImportTemplateData, RegisterTemplateModel>> {
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public RegisterTemplatePopupView(EventBus eventBus, Driver driver) {
        super(eventBus, driver);
    }

    private VmTemplate getEntity(Object object) {
        return ((ImportTemplateData) object).getTemplate();
    }

    @Override
    protected void createEntityTable(RegisterTemplateModel model) {
        AbstractTextColumn<Object> nameColumn = new AbstractTextColumn<Object>() {
            @Override
            public String getValue(Object object) {
                return getEntity(object).getName();
            }
        };
        entityTable.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<Object> originColumn = new AbstractEnumColumn<Object, OriginType>() {
            @Override
            protected OriginType getRawValue(Object object) {
                return getEntity(object).getOrigin();
            }
        };
        entityTable.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<Object> memoryColumn = new AbstractTextColumn<Object>() {
            @Override
            public String getValue(Object object) {
                int size = getEntity(object).getMemSizeMb();
                return messages.megabytes(String.valueOf(size));
            }
        };
        entityTable.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<Object> cpuColumn = new AbstractTextColumn<Object>() {
            @Override
            public String getValue(Object object) {
                int numOfCpus = getEntity(object).getNumOfCpus();
                return String.valueOf(numOfCpus);
            }
        };
        entityTable.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<Object> archColumn = new AbstractTextColumn<Object>() {
            @Override
            public String getValue(Object object) {
                ArchitectureType clusterArch = getEntity(object).getClusterArch();
                return String.valueOf(clusterArch);
            }
        };
        entityTable.addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<Object> diskColumn = new AbstractTextColumn<Object>() {
            @Override
            public String getValue(Object object) {
                int numOfDisks = getEntity(object).getDiskTemplateMap().size();
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
    protected void createInfoPanel(RegisterTemplateModel model) {
        registerEntityInfoPanel = new RegisterTemplateInfoPanel(model);
        entityInfoContainer.add(registerEntityInfoPanel);
    }

    @Override
    protected UiCommandButton createCommandButton(String label, String uniqueId) {
        if (RegisterTemplateModel.VNIC_PROFILE_MAPPING_COMMAND.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        } else {
            return super.createCommandButton(label, uniqueId);
        }
    }

    @Override
    public void edit(RegisterTemplateModel object) {
        super.edit(object);
    }

    @Override
    public RegisterTemplateModel flush() {
        return super.flush();
    }
}
