package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.HasValidation;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IscsiStorageView extends AbstractStorageView<IscsiStorageModel> implements HasValidation {

    interface Driver extends SimpleBeanEditorDriver<IscsiStorageModel, IscsiStorageView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "GetLUNsFailure")
    Label message;

    @UiField
    DialogTab lunToTargetsTab;

    @UiField
    DialogTab targetsToLunTab;

    @UiField
    DialogTabPanel dialogTabPanel;

    @UiField
    SimplePanel lunsListPanel;

    @UiField
    SimplePanel targetsToLunsPanel;

    @UiField
    FlowPanel targetsToLunsTabContentPanel;

    @UiField
    FlowPanel lunsToTargetsTabContentPanel;

    @Ignore
    IscsiTargetToLunView iscsiTargetToLunView;

    @Ignore
    IscsiLunToTargetView iscsiLunToTargetView;

    @Inject
    public IscsiStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
        dialogTabPanel.addBarStyle(style.bar());
        lunToTargetsTab.setTabLabelStyle(style.dialogTab());
        targetsToLunTab.setTabLabelStyle(style.dialogTab());
    }

    void localize(ApplicationConstants constants) {
        lunToTargetsTab.setLabel(constants.storageIscsiPopupLunToTargetsTabLabel());
        targetsToLunTab.setLabel(constants.storageIscsiPopupTargetsToLunTabLabel());
    }

    @Override
    public void edit(final IscsiStorageModel object) {
        Driver.driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("IsValid")) {
                    onIsValidPropertyChange(object);
                }
                else if (propName.equals("IsGrouppedByTarget")) {
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
        iscsiTargetToLunView = new IscsiTargetToLunView(208, 306);
        iscsiLunToTargetView = new IscsiLunToTargetView();

        // Add view widgets to panel
        lunsListPanel.add(iscsiLunToTargetView);
        targetsToLunsPanel.add(iscsiTargetToLunView);
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

    void onIsValidPropertyChange(EntityModel model) {
        if (model.getIsValid()) {
            markAsValid();
        } else {
            markAsInvalid(model.getInvalidityReasons());
        }
    }

    @Override
    public void markAsValid() {
        markValidation(false, null);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        markValidation(true, validationHints);
    }

    private void markValidation(boolean isValid, List<String> validationHints) {
        String oldStyle = isValid ? style.validTabContentPanel() : style.invalidTabContentPanel();
        String newStyle = isValid ? style.invalidTabContentPanel() : style.validTabContentPanel();

        targetsToLunsTabContentPanel.removeStyleName(oldStyle);
        lunsToTargetsTabContentPanel.removeStyleName(oldStyle);
        targetsToLunsTabContentPanel.addStyleName(newStyle);
        lunsToTargetsTabContentPanel.addStyleName(newStyle);

        targetsToLunsTabContentPanel.setTitle(getValidationTitle(validationHints));
        lunsToTargetsTabContentPanel.setTitle(getValidationTitle(validationHints));
    }

    private String getValidationTitle(List<String> validationHints) {
        return validationHints != null && validationHints.size() > 0 ? validationHints.get(0) : null;
    }

    @Override
    public boolean isSubViewFocused() {
        return iscsiTargetToLunView.isDiscoverPanelFocused();
    }

    @Override
    public IscsiStorageModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
    }

    interface WidgetStyle extends CssResource {
        String bar();

        String dialogTab();

        String expandedlunsListPanel();

        String validTabContentPanel();

        String invalidTabContentPanel();
    }

}
