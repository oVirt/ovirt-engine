package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HorizontalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.AddVmHostDevicesModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev.AddVmHostDevicePopupPresenterWidget;

public class AddVmHostDevicePopupView extends AbstractModelBoundPopupView<AddVmHostDevicesModel> implements AddVmHostDevicePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<AddVmHostDevicesModel, AddVmHostDevicePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AddVmHostDevicePopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<AddVmHostDevicePopupView> {
    }

    private final Driver driver;

    @UiField(provided = true)
    @Path("pinnedHost.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> pinnedHostEditor;

    @UiField
    @Path("capability.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> capabilityEditor;

    @UiField(provided = true)
    HorizontalSplitTable<EntityModel<HostDeviceView>> splitTable;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<HostDeviceView>>> availableHostDevices;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<HostDeviceView>>> selectedHostDevices;

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public AddVmHostDevicePopupView(EventBus eventBus, Driver driver, ViewUiBinder uiBinder, ViewIdHandler idHandler) {
        super(eventBus);
        initEditors();
        initWidget(uiBinder.createAndBindUi(this));
        idHandler.generateAndSetIds(this);
        this.driver = driver;
        driver.initialize(this);
    }

    private void initEditors() {
        pinnedHostEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());
        pinnedHostEditor.setLabel(constants.pinnedHost());

        availableHostDevices = new EntityModelCellTable<>(true, false, true);
        selectedHostDevices = new EntityModelCellTable<>(true, false, true);
        splitTable = new HorizontalSplitTable<>(availableHostDevices,
                selectedHostDevices,
                constants.availableHostDevices(),
                constants.selectedHostDevices());

        initHostDeviceCellTable(availableHostDevices);
        initHostDeviceCellTable(selectedHostDevices);
    }

    private void initHostDeviceCellTable(EntityModelCellTable<ListModel<EntityModel<HostDeviceView>>> hostDeviceTable) {
        hostDeviceTable.enableColumnResizing();

        addHostDeviceColumn(hostDeviceTable, constants.deviceName(), "200px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return hostDevice.getEntity().getDeviceName();
            }
        });

        addHostDeviceColumn(hostDeviceTable, constants.product(), "350px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return renderNameId(
                        hostDevice.getEntity().getProductName(),
                        hostDevice.getEntity().getProductId());
            }
        });

        addHostDeviceColumn(hostDeviceTable, constants.vendor(), "200px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return renderNameId(
                        hostDevice.getEntity().getVendorName(),
                        hostDevice.getEntity().getVendorId());
            }
        });

        addHostDeviceColumn(hostDeviceTable, constants.currentlyUsedByVm(), "150px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return hostDevice.getEntity().getRunningVmName();
            }
        });

        addHostDeviceColumn(hostDeviceTable, constants.attachedToVms(), "150px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return StringUtils.join(hostDevice.getEntity().getAttachedVmNames(), ", "); //$NON-NLS-1$
            }
        });

        addHostDeviceColumn(hostDeviceTable, constants.iommuGroup(), "150px", new AbstractTextColumn<EntityModel<HostDeviceView>>() { //$NON-NLS-1$
            @Override
            public String getValue(EntityModel<HostDeviceView> hostDevice) {
                return hostDevice.getEntity().getIommuGroup() == null ? constants.notAvailableLabel() : hostDevice.getEntity().getIommuGroup().toString();
            }
        });
    }

    private String renderNameId(String name, String id) {
        if (StringUtils.isEmpty(name)) {
            return id;
        }
        // we assume that VDSM will never report name != null && id == null
        return messages.nameId(name, id);
    }

    private void addHostDeviceColumn(EntityModelCellTable<ListModel<EntityModel<HostDeviceView>>> hostDeviceTable,
                                     String header, String width, AbstractTextColumn<EntityModel<HostDeviceView>> column) {
        column.makeSortable();
        hostDeviceTable.addColumn(column, header, width);
    }

    @Override
    public void edit(AddVmHostDevicesModel model) {
        splitTable.edit(
                model.getAvailableHostDevices(),
                model.getSelectedHostDevices(),
                model.getAddDeviceCommand(),
                model.getRemoveDeviceCommand());
        driver.edit(model);
    }

    @Override
    public AddVmHostDevicesModel flush() {
        return driver.flush();
    }
}
