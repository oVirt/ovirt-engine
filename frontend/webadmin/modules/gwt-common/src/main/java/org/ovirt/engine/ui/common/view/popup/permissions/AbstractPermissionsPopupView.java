package org.ovirt.engine.ui.common.view.popup.permissions;

import java.util.ArrayList;

import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Role;
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

public abstract class AbstractPermissionsPopupView<T extends AdElementListModel> extends AbstractModelBoundPopupView<T> implements AbstractPermissionsPopupPresenterWidget.ViewDef<T> {

    @SuppressWarnings("rawtypes")
    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractPermissionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @WithElementId
    public UiCommandButton searchButton;

    @UiField(provided = true)
    @Path("profile.selectedItem")
    @WithElementId("profile")
    public ListModelListBoxEditor<Object> profileSelection;

    @UiField
    @Ignore
    public Label roleToAssignLabel;

    @UiField(provided = true)
    @Path("role.selectedItem")
    @WithElementId("role")
    public ListModelListBoxEditor<Object> roleSelection;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public EntityModelCellTable<ListModel> searchItems;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton everyoneRadio;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton specificUserOrGroupRadio;

    @UiField
    @Path("searchString")
    @WithElementId("searchString")
    public TextBoxChanger searchStringEditor;

    @UiField
    public SimplePanel everyonePanel;

    @UiField
    public HorizontalPanel roleSelectionPanel;

    @UiField
    public ScrollPanel searchItemsScrollPanel;

    @UiField
    @Ignore
    Label errorMessage;

    private PopupNativeKeyPressHandler nativeKeyPressHandler;

    public AbstractPermissionsPopupView(EventBus eventBus, CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        searchItems = new EntityModelCellTable<ListModel>(true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
        searchStringEditor.setStyleName("");
        initTable(constants);
        specificUserOrGroupRadio.setValue(true);
        everyoneRadio.setValue(false);
        localize(constants);
    }

    protected abstract void generateIds();

    protected abstract T doFlush();

    private void initListBoxEditors() {
        profileSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((ProfileEntry) object).toString();
            }
        });

        roleSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Role) object).getname();
            }
        });
    }

    private void initTable(CommonApplicationConstants constants) {
        // Table Entity Columns
        searchItems.addEntityModelColumn(new EntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getFirstName();
            }
        }, constants.firsNamePermissionsPopup());

        searchItems.addEntityModelColumn(new EntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getLastName();
            }
        }, constants.lastNamePermissionsPopup());

        searchItems.addEntityModelColumn(new EntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getLoginName();
            }
        }, constants.userNamePermissionsPopup());
    }

    void localize(CommonApplicationConstants constants) {
        searchButton.setLabel(constants.goPermissionsPopup());
    }

    @Override
    public void edit(final T object) {
        searchItems.setRowData(new ArrayList<EntityModel>());
        searchItems.asEditor().edit(object);
    }

    @Override
    public T flush() {
        searchItems.flush();
        return doFlush();
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
        profileSelection.setEnabled(!isEveryone);
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

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        errorMessage.setText(message);
    }
}
