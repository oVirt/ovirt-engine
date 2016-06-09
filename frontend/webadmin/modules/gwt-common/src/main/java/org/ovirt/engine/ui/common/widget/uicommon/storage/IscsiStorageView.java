package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class IscsiStorageView extends AbstractStorageView<IscsiStorageModel> implements HasValidation {

    interface Driver extends SimpleBeanEditorDriver<IscsiStorageModel, IscsiStorageView> {
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
    Label warning;

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

    private double treeCollapsedHeight = 206;
    private double treeExpandedHeight = 305;
    private double lunsTreeHeight = 342;
    private double tabContentHeight = 376;
    private double tabHeight = 188;
    private double textTop = 80;
    private double textLeft = -92;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public IscsiStorageView(boolean multiSelection) {
        this.multiSelection = multiSelection;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        addStyles();
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
        this.tabHeight = tabHeight;
        this.textTop = textTop;
        this.textLeft = textLeft;
    }

    void addStyles() {
        dialogTabPanel.addBarStyle(style.bar());
        lunToTargetsTab.setTabLabelStyle(style.dialogTab());
        targetsToLunTab.setTabLabelStyle(style.dialogTab());
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
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    onIsValidPropertyChange(object);
                }
                else if (propName.equals("IsGrouppedByTarget")) { //$NON-NLS-1$
                    updateListByGropping(object);
                }
            }
        });

        // Edit sub-views
        iscsiTargetToLunView.edit(object);
        iscsiLunToTargetView.edit(object);

        // Add click handlers
        targetsToLunTab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                iscsiLunToTargetView.disableItemsUpdate();
                object.setIsGrouppedByTarget(true);
            }
        });

        lunToTargetsTab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                iscsiTargetToLunView.disableItemsUpdate();
                object.setIsGrouppedByTarget(false);
            }
        });

        // Update selected tab and list
        dialogTabPanel.switchTab(object.getIsGrouppedByTarget() ? targetsToLunTab : lunToTargetsTab);
        updateListByGropping(object);

        // Set tree style
        iscsiLunToTargetView.setTreeContainerStyleName(style.expandedlunsListPanel());
    }

    void initLists(IscsiStorageModel object) {
        // Create discover panel and storage lists
        iscsiTargetToLunView = new IscsiTargetToLunView(treeCollapsedHeight, treeExpandedHeight, false, multiSelection);
        iscsiLunToTargetView = new IscsiLunToTargetView(lunsTreeHeight, multiSelection);

        // Update Style
        dialogTabPanel.getElement().getStyle().setHeight(tabContentHeight, Unit.PX);
        updateStyle(targetsToLunTab);
        updateStyle(lunToTargetsTab);

        // Add view widgets to panel
        lunsListPanel.setWidget(iscsiLunToTargetView);
        targetsToLunsPanel.setWidget(iscsiTargetToLunView);
    }

    void updateStyle(DialogTab dialogTab) {
        dialogTab.getElement().getStyle().setHeight(tabHeight, Unit.PX);
        dialogTab.getTabLabel().getElement().getStyle().setTop(textTop, Unit.PX);
        dialogTab.getTabLabel().getElement().getStyle().setLeft(textLeft, Unit.PX);
        dialogTab.getTabLabel().getElement().getStyle().setWidth(tabHeight, Unit.PX);
        dialogTab.getTabLabel().setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    }

    void updateListByGropping(IscsiStorageModel object) {
        // Update view by 'IsGrouppedByTarget' flag
        if (object.getIsGrouppedByTarget()) {
            iscsiTargetToLunView.activateItemsUpdate();
        }
        else {
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

    @Override
    public IscsiStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void focus() {
    }

    interface WidgetStyle extends CssResource {
        String bar();

        String dialogTab();

        String expandedlunsListPanel();
    }

}
