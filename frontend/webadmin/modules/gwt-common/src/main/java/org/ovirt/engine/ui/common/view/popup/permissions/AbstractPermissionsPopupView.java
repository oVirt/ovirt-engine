package org.ovirt.engine.ui.common.view.popup.permissions;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.permissions.AbstractPermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.TextBoxChanger;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class AbstractPermissionsPopupView extends AbstractModelBoundPopupView<AdElementListModel> implements AbstractPermissionsPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<AdElementListModel, AbstractPermissionsPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractPermissionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @WithElementId
    public UiCommandButton searchButton;

    @UiField(provided = true)
    @Path("domain.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<Object> domainSelection;

    @UiField
    @Ignore
    Label roleToAssignLabel;

    @UiField(provided = true)
    @Path("role.selectedItem")
    ListModelListBoxEditor<Object> roleSelection;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public EntityModelCellTable<ListModel> searchItems;

    @UiField
    @Ignore
    RadioButton everyoneRadio;

    @UiField
    @Ignore
    RadioButton specificUserOrGroupRadio;

    @UiField
    @Path("searchString")
    @WithElementId
    public TextBoxChanger searchStringEditor;

    @UiField
    SimplePanel everyonePanel;

    @UiField
    HorizontalPanel roleSelectionPanel;

    @UiField
    ScrollPanel searchItemsScrollPanel;

    private PopupNativeKeyPressHandler nativeKeyPressHandler;

    public AbstractPermissionsPopupView(EventBus eventBus, CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        searchItems = new EntityModelCellTable<ListModel>(true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
        initTable();
        specificUserOrGroupRadio.setValue(true);
        everyoneRadio.setValue(false);
        localize(constants);
        Driver.driver.initialize(this);
    }

    protected abstract void generateIds();

    private void initListBoxEditors() {
        domainSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        roleSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((roles) object).getname();
            }
        });
    }

    private void initTable() {
        // Table Entity Columns
        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser) model.getEntity()).getname();
                else {
                    return model.getEntity().toString();
                }
            }
        }, "First Name");

        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser) model.getEntity()).getsurname();
                else {
                    return "";
                }
            }
        }, "Last Name");

        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser) model.getEntity()).getusername();
                else {
                    return "";
                }
            }
        }, "User Name");
    }

    void localize(CommonApplicationConstants constants) {
        searchButton.setLabel("GO");
    }

    @Override
    public void edit(final AdElementListModel object) {
        searchItems.setRowData(new ArrayList<EntityModel>());
        searchItems.edit(object);
        Driver.driver.edit(object);
    }

    @Override
    public AdElementListModel flush() {
        searchItems.flush();
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        searchStringEditor.setFocus(true);
    }

    @Override
    public HasUiCommandClickHandlers getSearchButton() {
        return searchButton;
    }

    @Override
    public HasKeyPressHandlers getKeyPressSearchInputBox() {
        return searchStringEditor;
    }

    @Override
    public HasClickHandlers getEveryoneRadio() {
        return everyoneRadio;
    }

    @Override
    public HasClickHandlers getSpecificUserOrGroupRadio() {
        return specificUserOrGroupRadio;
    }

    @Override
    public PopupNativeKeyPressHandler getNativeKeyPressHandler() {
        return nativeKeyPressHandler;
    }

    @Override
    public HasHandlers getSearchStringEditor() {
        return searchStringEditor;
    }

    @Override
    public void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler) {
        super.setPopupKeyPressHandler(handler);
        this.nativeKeyPressHandler = handler;
    }

    @Override
    public void changeStateOfElementsWhenAccessIsForEveryone(boolean isEveryone) {
        domainSelection.setEnabled(!isEveryone);
        searchStringEditor.setEnabled(!isEveryone);
        searchButton.getCommand().setIsExecutionAllowed(!isEveryone);
        searchItems.setVisible(!isEveryone);
    }

    @Override
    public HasValue<String> getSearchString() {
        return searchStringEditor;
    }

    @Override
    public void hideRoleSelection(Boolean indic) {
        roleSelectionPanel.setVisible(!indic);
    }

    @Override
    public void hideEveryoneSelection(Boolean indic) {
        everyonePanel.setVisible(!indic);
    }

}
