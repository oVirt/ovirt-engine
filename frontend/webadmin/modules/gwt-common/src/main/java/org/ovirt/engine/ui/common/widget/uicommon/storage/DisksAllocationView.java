package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupSimpleTableResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationView extends Composite implements HasEditorDriver<DisksAllocationModel>, HasElementId,
    FocusableComponentsContainer, PatternFlyCompatible {

    interface Driver extends SimpleBeanEditorDriver<DisksAllocationModel, DisksAllocationView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    protected interface Style extends CssResource {
        String diskListPanel();
    }

    @UiField
    Style style;

    @UiField
    FlowPanel diskListPanel;

    @UiField
    SimplePanel diskListHeaderPanel;

    @Ignore
    @UiField
    Label diskAllocationLabel;

    @Ignore
    EntityModelCellTable<ListModel> listHeader;

    boolean showVolumeType;
    boolean showSource;
    boolean showTarget;
    boolean showQuota;
    boolean showVolumeFormat;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private String elementId = DOM.createUniqueId();

    @Override
    public int setTabIndexes(int nextTabIndex) {
        int nbWidgetsInDiskListPanel = diskListPanel.getWidgetCount();
        for (int i = 0; i < nbWidgetsInDiskListPanel; ++i) {
            Widget widget = diskListPanel.getWidget(i);
            if (widget instanceof FocusableComponentsContainer) {
                nextTabIndex = ((FocusableComponentsContainer) widget).setTabIndexes(nextTabIndex);
            }
        }
        return nextTabIndex;
    }

    public DisksAllocationView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    void updateListHeader(DisksAllocationModel model) {
        String width = "85px"; //$NON-NLS-1$
        listHeader = new EntityModelCellTable(false, (Resources) GWT.create(
                PopupSimpleTableResources.class), true);
        listHeader.addColumn(new EmptyColumn(), constants.aliasDisk(), width);
        listHeader.addColumn(new EmptyColumn(), constants.provisionedSizeDisk(), width);

        if (showVolumeType) {
            listHeader.addColumn(new EmptyColumn(), constants.allocationDisk(), width);
        }

        if (showVolumeFormat) {
            listHeader.addColumn(new EmptyColumn(), constants.formatDisk(), width);
        }

        if (showSource) {
            listHeader.addColumn(new EmptyColumn(), constants.sourceDisk(), width);
        }

        if (showTarget) {
            listHeader.addColumn(new EmptyColumn(), constants.targetDisk(), width);
        }

        listHeader.addColumn(new EmptyColumn(), constants.diskProfile(), width);

        if (showQuota) {
            listHeader.addColumn(new EmptyColumn(), constants.quotaDisk(), width);
        }

        listHeader.setRowData(new ArrayList());
        listHeader.setWidth("100%", true); //$NON-NLS-1$

        diskListHeaderPanel.setWidget(listHeader);
    }

    void updateColumnsAvailability(DisksAllocationModel model) {
        setShowVolumeType(model.getIsVolumeTypeAvailable());
        setShowVolumeFormat(model.getIsVolumeFormatAvailable());
        setShowQuota(model.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
        setShowSource(model.isSourceAvailable());
        setShowTarget(model.isTargetAvailable());
    }

    @Override
    public void edit(DisksAllocationModel model) {
        driver.edit(model);
        initListeners(model);
        updateColumnsAvailability(model);
        updateListHeader(model);
    }

    private void initListeners(final DisksAllocationModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Disks".equals(args.propertyName)) { //$NON-NLS-1$
                    addDiskList(model);
                }
                else if ("QuotaEnforcmentType".equals(args.propertyName)) { //$NON-NLS-1$
                    updateColumnsAvailability(model);
                    updateListHeader(model);
                }
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        diskListPanel.clear();
        diskAllocationLabel.setVisible(!model.getDisks().isEmpty());

        int diskIndex = 0;
        for (final DiskModel diskModel : model.getDisks()) {
            DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView();
            disksAllocationItemView.edit(diskModel);
            disksAllocationItemView.updateStyles(showQuota);
            disksAllocationItemView.setIsAliasChangeable(model.getIsAliasChangable());
            disksAllocationItemView.setElementId(
                    ElementIdUtils.createElementId(elementId, "disk" + diskIndex++)); //$NON-NLS-1$
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

    public void setShowVolumeFormat(boolean showVolumeFormat) {
        this.showVolumeFormat = showVolumeFormat;
    }

    public void setShowSource(boolean showSource) {
        this.showSource = showSource;
    }

    public void setShowTarget(boolean showTarget) {
        this.showTarget = showTarget;
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

    @Override
    public void setUsePatternFly(boolean use) {
        if (use) {
            diskListPanel.removeStyleName(style.diskListPanel());
        }
    }

}
