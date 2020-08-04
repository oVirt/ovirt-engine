package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupSimpleTableResources;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationView extends Composite implements HasEditorDriver<DisksAllocationModel>, HasElementId,
    FocusableComponentsContainer, PatternFlyCompatible {

    private static final String HEADER_HEIGHT = "26px"; // $NON-NLS-1$

    interface Driver extends UiCommonEditorDriver<DisksAllocationModel, DisksAllocationView> {
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

    @UiField(provided = true)
    @Path(value = "diskAllocationTargetEnabled.entity")
    public EntityModelCheckBoxEditor diskAllocationTargetEnabled;

    @Ignore
    EntityModelCellTable<ListModel<DisksAllocationModel>> listHeader;

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
        diskAllocationTargetEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    void updateListHeader() {
        String width = calculateColumnWidthPercentage() + "%"; //$NON-NLS-1$
        listHeader = new EntityModelCellTable<>(false, (Resources) GWT.create(
                PopupSimpleTableResources.class), true);
        listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.aliasDisk(), width);
        listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.provisionedSizeDisk(), width);

        if (showVolumeType) {
            listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.allocationDisk(), width);
        }

        if (showVolumeFormat) {
            listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.formatDisk(), width);
        }

        if (showSource) {
            listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.sourceDisk(), width);
        }

        if (showTarget) {
            listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.targetDisk(), width);
        }

        listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.diskProfile(), width);

        if (showQuota) {
            listHeader.addColumn(new EmptyColumn<DisksAllocationModel>(), constants.quotaDisk(), width);
        }

        listHeader.setRowData(new ArrayList<DisksAllocationModel>());
        listHeader.setWidth("100%"); // $NON-NLS-1$
        listHeader.setHeight(HEADER_HEIGHT);

        diskListHeaderPanel.setWidget(listHeader);
    }

    private String calculateColumnWidthPercentage() {
        int columnCount = 2;
        if (showVolumeType) {
            columnCount++;
        }
        if (showVolumeFormat) {
            columnCount++;
        }
        if (showSource) {
            columnCount++;
        }
        if (showTarget) {
            columnCount++;
        }
        columnCount++;
        if (showQuota) {
            columnCount++;
        }
        // Note the f, forcing this to be a real double instead of an int, is important, eventually this value
        // is turning into a width of the column percentage, and that allows for fractions of whole percentages
        // to be used.
        double columnPercentage = 100f / columnCount;
        return String.valueOf(columnPercentage); //$NON-NLS-1$
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
        updateListHeader();
    }

    private void initListeners(final DisksAllocationModel model) {
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("Disks".equals(args.propertyName)) { //$NON-NLS-1$
                addDiskList(model);
            } else if ("QuotaEnforcmentType".equals(args.propertyName)) { //$NON-NLS-1$
                updateColumnsAvailability(model);
                updateListHeader();
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        diskListPanel.clear();
        diskAllocationLabel.setVisible(!model.getDisks().isEmpty());

        int diskIndex = 0;
        String columnWidth = calculateColumnWidthPercentage();
        for (final DiskModel diskModel : model.getDisks()) {
            DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView(columnWidth);
            disksAllocationItemView.edit(diskModel);
            disksAllocationItemView.setIsAliasChangeable(model.getIsAliasChangeable());
            disksAllocationItemView.setElementId(
                    ElementIdUtils.createElementId(elementId, "disk" + diskIndex++)); //$NON-NLS-1$
            diskListPanel.add(disksAllocationItemView);
        }
    }

    @Override
    public DisksAllocationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    public void setListHeight(String listHeight) {
        diskListPanel.setHeight(listHeight);
    }

    public void setListWidth(String listWidth) {
        diskListPanel.setWidth(listWidth);
    }

    public void setListOverflow(Overflow overflow) {
        diskListPanel.getElement().getStyle().setOverflow(overflow);
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
    public void setUsePatternFly(boolean usePatternFly) {
        diskAllocationTargetEnabled.setUsePatternFly(usePatternFly);
        if (usePatternFly) {
            diskListPanel.removeStyleName(style.diskListPanel());
            diskAllocationTargetEnabled.setRemoveFormGroup(true);
        }
    }

}
