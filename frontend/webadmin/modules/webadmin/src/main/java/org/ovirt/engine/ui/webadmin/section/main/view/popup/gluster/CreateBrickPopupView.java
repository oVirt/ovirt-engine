package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.CreateBrickModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.CreateBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class CreateBrickPopupView extends AbstractModelBoundPopupView<CreateBrickModel> implements CreateBrickPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<CreateBrickModel, CreateBrickPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, CreateBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CreateBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface WidgetStyle extends CssResource {
        String editorContentWidget();

        String forceEditorWidget();
    }

    @UiField
    @Path(value = "lvName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor lvNameEditor;

    @UiField
    @Path(value = "mountPoint.entity")
    @WithElementId
    StringEntityModelTextBoxEditor mountPointEditor;

    @UiField
    @Ignore
    @WithElementId
    Label deviceHeader;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    ListModelObjectCellTable<StorageDevice, ListModel<StorageDevice>> deviceTable;

    @UiField
    @Path(value = "size.entity")
    @WithElementId
    StringEntityModelLabelEditor sizeEditor;

    @UiField(provided = true)
    @Path(value = "raidTypeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<RaidType> raidTypeEditor;

    @UiField
    @Ignore
    Label deviceSelectionInfo;

    @UiField
    @Path(value = "noOfPhysicalDisksInRaidVolume.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor noOfPhysicalDisksEditor;

    @UiField
    @Path(value = "stripeSize.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor stripeSizeEditor;

    private final Driver driver = GWT.create(Driver.class);

    private final ApplicationConstants constants;

    @Inject
    public CreateBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;
        initListBoxEditors();
        deviceTable = new ListModelObjectCellTable<StorageDevice, ListModel<StorageDevice>>(true, false);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        initTableColumns(constants);
        driver.initialize(this);
    }

    private void initListBoxEditors() {
        raidTypeEditor = new ListModelListBoxEditor<RaidType>(new EnumRenderer<RaidType>());
    }

    protected void initTableColumns(ApplicationConstants constants) {
        // Table Entity Columns
        deviceTable.addColumn(new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice entity) {
                return entity.getName();
            }
        }, constants.deviceName());

        deviceTable.addColumnAndSetWidth(new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice entity) {
                return entity.getDevType();
            }
        }, constants.deviceType(), "100px"); //$NON-NLS-1$

        deviceTable.addColumnAndSetWidth(new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice entity) {
                Pair<SizeUnit, Double> convertedSize = SizeConverter.autoConvert(entity.getSize(), SizeUnit.MB);
                return formatSize(convertedSize.getSecond()) + " " + convertedSize.getFirst().toString(); //$NON-NLS-1$
            }
        }, constants.size(), "100px"); //$NON-NLS-1$
    }

    private void localize(ApplicationConstants constants) {
        lvNameEditor.setLabel(constants.logicalVolume());
        mountPointEditor.setLabel(constants.mountPoint());
        sizeEditor.setLabel(constants.lvSize());
        raidTypeEditor.setLabel(constants.raidType());
        noOfPhysicalDisksEditor.setLabel(constants.noOfPhysicalDisksInRaidVolume());
        stripeSizeEditor.setLabel(constants.stripeSize());
        deviceHeader.setText(constants.storageDevices());
        deviceSelectionInfo.setText(constants.getStorageDeviceSelectionInfo());
    }

    @Override
    public void edit(final CreateBrickModel object) {
        deviceTable.asEditor().edit(object.getStorageDevices());
        driver.edit(object);
        deviceSelectionInfo.setText(null);
        setDeviceInfoVisibility(false);
    }

    @Override
    public CreateBrickModel flush() {
        deviceTable.flush();
        return driver.flush();
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }

    @Override
    public void setDeviceInfoText(String raidType) {
        deviceSelectionInfo.setText("(" + constants.getStorageDeviceSelectionInfo() + raidType + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setDeviceInfoVisibility(boolean isVisiable) {
        deviceSelectionInfo.setVisible(isVisiable);
    }
}
