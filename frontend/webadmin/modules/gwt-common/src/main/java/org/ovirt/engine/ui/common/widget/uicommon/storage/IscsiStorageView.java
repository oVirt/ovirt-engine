package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class IscsiStorageView extends AbstractStorageView<IscsiStorageModel> implements HasValidation {

    interface Driver extends UiCommonEditorDriver<IscsiStorageModel, IscsiStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "getLUNsFailure")
    Label message;

    @UiField
    @Path(value = "selectedLunWarning")
    AlertPanel warning;

    @UiField
    DialogTab lunToTargetsTab;

    @UiField
    DialogTab targetsToLunTab;

    @UiField
    DialogTabPanel dialogTabPanel;

    @UiField
    ValidatedPanelWidget lunsListPanel;

    @UiField
    ValidatedPanelWidget targetsToLunsPanel;

    @Ignore
    IscsiTargetToLunView iscsiTargetToLunView;

    @Ignore
    IscsiLunToTargetView iscsiLunToTargetView;

    @UiField
    @Ignore
    Label mainLabel;

    @UiField
    @Ignore
    Label subLabel;

    private double treeCollapsedHeight = 245;
    private double treeExpandedHeight = 355;
    private double lunsTreeHeight = 405;
    private double tabContentHeight = 100;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public IscsiStorageView(boolean multiSelection) {
        this.multiSelection = multiSelection;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    public IscsiStorageView(boolean multiSelection,
            double treeCollapsedHeight, double treeExpandedHeight, double lunsTreeHeight,
            double tabContentHeight, double tabHeight,
            double textTop, double textLeft) {
        this(multiSelection);

        this.treeCollapsedHeight = treeCollapsedHeight;
        this.treeExpandedHeight = treeExpandedHeight;
        this.lunsTreeHeight = lunsTreeHeight;
        this.tabContentHeight = tabContentHeight;
    }

    void localize() {
        lunToTargetsTab.setLabel(constants.storageIscsiPopupLunToTargetsTabLabel());
        targetsToLunTab.setLabel(constants.storageIscsiPopupTargetsToLunTabLabel());
    }

    @Override
    public void edit(final IscsiStorageModel object) {
        driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("IsValid")) { //$NON-NLS-1$
                onIsValidPropertyChange(object);
            } else if (propName.equals("IsGroupedByTarget")) { //$NON-NLS-1$
                updateListByGrouping(object);
            }
        });

        // Edit sub-views
        iscsiTargetToLunView.edit(object);
        iscsiLunToTargetView.edit(object);

        // Add click handlers
        targetsToLunTab.addClickHandler(event -> {
            iscsiLunToTargetView.disableItemsUpdate();
            object.setIsGroupedByTarget(true);
        });

        lunToTargetsTab.addClickHandler(event -> {
            iscsiTargetToLunView.disableItemsUpdate();
            object.setIsGroupedByTarget(false);
        });

        // Update selected tab and list
        dialogTabPanel.switchTab(object.getIsGroupedByTarget() ? targetsToLunTab : lunToTargetsTab);
        updateListByGrouping(object);

        // Set labels above table
        if (!object.getContainer().isNewStorage()) {
            switch (object.getContainer().getStorage().getStatus()) {
            case Maintenance:
                mainLabel.setText(constants.storageIscsiRemoveLUNsLabel());
                subLabel.setText(constants.storageIscsiAvailableActionsOnMaintenanceLabel());
                break;
            case Active:
                mainLabel.setText(constants.storageIscsiActionsLabel());
                subLabel.setText(constants.storageIscsiAvailableActionsForActiveDomainsLabel());
                break;
            }
        }

        if (object.getContainer().isNewStorage()) {
            dialogTabPanel.setBarStyle(multiSelection ? style.barNewDomain() : style.barNewDisk());
        } else {
            dialogTabPanel.setBarStyle(style.barEditDomain());
        }

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("SelectedLunWarning")) { //$NON-NLS-1$
                String warningText = object.getSelectedLunWarning();
                warning.clearMessages();
                warning.setType(AlertPanel.Type.WARNING);
                warning.addMessage(SafeHtmlUtils.fromString(warningText));
                warning.setVisible(!StringHelper.isNullOrEmpty(warningText));
            }
        });
    }

    void initLists(IscsiStorageModel object) {
        // Create discover panel and storage lists
        iscsiTargetToLunView = new IscsiTargetToLunView(treeCollapsedHeight, treeExpandedHeight, false, multiSelection);
        iscsiLunToTargetView = new IscsiLunToTargetView(lunsTreeHeight, multiSelection);

        // Update Style
        dialogTabPanel.getElement().getStyle().setHeight(tabContentHeight, Unit.PCT);

        // Add view widgets to panel
        lunsListPanel.setWidget(iscsiLunToTargetView);
        targetsToLunsPanel.setWidget(iscsiTargetToLunView);
    }

    void updateListByGrouping(IscsiStorageModel object) {
        // Update view by 'IsGroupedByTarget' flag
        if (object.getIsGroupedByTarget()) {
            iscsiTargetToLunView.activateItemsUpdate();
        } else {
            iscsiLunToTargetView.activateItemsUpdate();
        }

    }

    void onIsValidPropertyChange(Model model) {
        if (model.getIsValid()) {
            markAsValid();
        } else {
            markAsInvalid(model.getInvalidityReasons());
        }
    }

    @Override
    public void markAsValid() {
        lunsListPanel.markAsValid();
        targetsToLunsPanel.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        lunsListPanel.markAsInvalid(validationHints);
        targetsToLunsPanel.markAsInvalid(validationHints);
    }

    @Override
    public boolean isValid() {
        return lunsListPanel.isValid() && targetsToLunsPanel.isValid();
    }

    @Override
    public boolean isSubViewFocused() {
        return iscsiTargetToLunView.isDiscoverPanelFocused();
    }

    public void setBarTop(double value, Unit unit) {
        dialogTabPanel.getBar().getElement().getStyle().setTop(value, unit);
    }

    @Override
    public IscsiStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
    }

    interface WidgetStyle extends CssResource {
        String barEditDomain();

        String barNewDomain();

        String barNewDisk();
    }

}
