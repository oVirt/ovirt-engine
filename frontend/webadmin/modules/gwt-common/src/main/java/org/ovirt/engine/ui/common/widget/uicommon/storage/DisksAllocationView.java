package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupSimpleTableResources;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationView extends Composite implements HasEditorDriver<DisksAllocationModel>, HasElementId, FocusableComponentsContainer {

    interface Driver extends SimpleBeanEditorDriver<DisksAllocationModel, DisksAllocationView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FlowPanel diskListPanel;

    @UiField
    SimplePanel diskListHeaderPanel;

    @Ignore
    EntityModelCellTable<ListModel> listHeader;

    boolean showVolumeType;
    boolean showSource;
    boolean showQuota;

    private final Driver driver = GWT.create(Driver.class);

    private CommonApplicationConstants constants;

    private String elementId = DOM.createUniqueId();

    @UiConstructor
    public DisksAllocationView() {
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        int nbWidgetsInDiskListPanel = diskListPanel.getWidgetCount();
        for (int i = 0; i < nbWidgetsInDiskListPanel; ++i) {
            Widget widget = diskListPanel.getWidget(i);
            if (widget instanceof FocusableComponentsContainer)
                nextTabIndex = ((FocusableComponentsContainer) widget).setTabIndexes(nextTabIndex);
        }
        return nextTabIndex;
    }

    public DisksAllocationView(CommonApplicationConstants constants) {
        this.constants = constants;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    void updateListHeader(DisksAllocationModel model) {
        String width = "85px"; //$NON-NLS-1$
        listHeader = new EntityModelCellTable(false, (Resources) GWT.create(
                PopupSimpleTableResources.class), true);
        listHeader.addColumn(new EmptyColumn(), constants.aliasDisk(), width);
        listHeader.addColumn(new EmptyColumn(), constants.provisionedSizeDisk(), width);

        if (showVolumeType)
            listHeader.addColumn(new EmptyColumn(), constants.allocationDisk(), width);

        if (showSource)
            listHeader.addColumn(new EmptyColumn(), constants.sourceDisk(), width);

        listHeader.addColumn(new EmptyColumn(), constants.targetDisk(), width);
        listHeader.addColumn(new EmptyColumn(), constants.diskProfile(), width);

        if (showQuota)
            listHeader.addColumn(new EmptyColumn(), constants.quotaDisk(), width);

        listHeader.setRowData(new ArrayList());
        listHeader.setWidth("100%", true); //$NON-NLS-1$

        diskListHeaderPanel.setWidget(listHeader);
    }

    void updateColumnsAvailability(DisksAllocationModel model) {
        setShowVolumeType(model.getIsVolumeFormatAvailable());
        setShowQuota(model.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
    }

    @Override
    public void edit(DisksAllocationModel model) {
        driver.edit(model);
        initListerners(model);
        updateColumnsAvailability(model);
        updateListHeader(model);
    }

    private void initListerners(final DisksAllocationModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs) {
                    PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                    if ("Disks".equals(changedArgs.propertyName)) { //$NON-NLS-1$
                        addDiskList(model);
                    }
                    else if ("QuotaEnforcmentType".equals(changedArgs.propertyName)) { //$NON-NLS-1$
                        updateColumnsAvailability(model);
                        updateListHeader(model);
                    }
                }
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        diskListPanel.clear();

        int diskIndex = 0;
        for (final DiskModel diskModel : model.getDisks()) {
            DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView(constants);
            disksAllocationItemView.edit(diskModel);
            disksAllocationItemView.updateStyles(showQuota);
            disksAllocationItemView.setIsAliasChangeable(model.getIsAliasChangable());
            disksAllocationItemView.setElementId(
                    ElementIdUtils.createElementId(elementId, "disk" + (diskIndex++))); //$NON-NLS-1$
            diskListPanel.add(disksAllocationItemView);
        }
    }

    @Override
    public DisksAllocationModel flush() {
        return driver.flush();
    }

    public void setListHeight(String listHeight) {
        diskListPanel.setHeight(listHeight);
    }

    public void setListWidth(String listWidth) {
        diskListPanel.setWidth(listWidth);
    }

    public void setShowVolumeType(boolean showVolumeType) {
        this.showVolumeType = showVolumeType;
    }

    public void setShowSource(boolean showSource) {
        this.showSource = showSource;
    }

    public void setShowQuota(boolean showQuota) {
        this.showQuota = showQuota;
    }

    public void addDiskListPanelStyle(String style) {
        diskListPanel.addStyleName(style);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
