package org.ovirt.engine.ui.common.view.popup.permissions;

import java.util.ArrayList;

import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.permissions.AbstractPermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.TextBoxChanger;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

public abstract class AbstractPermissionsPopupView<T extends AdElementListModel> extends AbstractModelBoundPopupView<T>
    implements AbstractPermissionsPopupPresenterWidget.ViewDef<T>, HasCleanup {

    @SuppressWarnings("rawtypes")
    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractPermissionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String alignBottomSearch();
    }

    /**
     * This is the max width of a column in this dialogs
     */
    private static final String MAX_COL_WIDTH = "260px"; // $NON-NLS-1$
    private static final String SEARCH_GRID_HEIGHT = "258px"; // $NON-NLS-1$

    @UiField
    @WithElementId
    public UiCommandButton searchButton;

    @UiField(provided = true)
    @Path("profile.selectedItem")
    @WithElementId("profile")
    public ListModelListBoxEditor<ProfileEntry> profileSelection;

    @UiField(provided = true)
    @Path("namespace.selectedItem")
    @WithElementId("namespace")
    public ListModelListBoxEditor<String> namespaceSelection;

    @UiField(provided = true)
    @Path("role.selectedItem")
    @WithElementId("role")
    public ListModelListBoxEditor<Role> roleSelection;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public EntityModelCellTable<AdElementListModel> searchItems;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton everyoneRadio;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton specificUserRadio;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton specificGroupRadio;

    @UiField
    @Ignore
    @WithElementId
    public RadioButton myGroupsRadio;

    @UiField
    @Path("searchString")
    @WithElementId("searchString")
    public TextBoxChanger searchStringEditor;

    @UiField
    public FlowPanel roleSelectionPanel;

    @UiField
    @Ignore
    Label errorMessage;

    @UiField
    Style style;

    private AbstractEntityModelTextColumn<DbUser> firstNameColumn;
    private AbstractEntityModelTextColumn<DbUser> groupNameColumn;
    private AbstractEntityModelTextColumn<DbUser> lastNameColumn;
    private AbstractEntityModelTextColumn<DbUser> userNameColumn;
    private AbstractEntityModelTextColumn<DbUser> displayNameColumn;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public AbstractPermissionsPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        searchItems = new EntityModelCellTable<>(true);
        searchItems.enableColumnResizing();
        searchItems.setHeight(SEARCH_GRID_HEIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
        searchStringEditor.setStyleName("");
        initTable();
        specificUserRadio.setValue(true);
        everyoneRadio.setValue(false);
        myGroupsRadio.setValue(false);
        //Have to add these classes to the searchStringEditor as the UiBinder seems to remove them
        searchStringEditor.addStyleName("form-control"); //$NON-NLS-1$
        searchStringEditor.addStyleName(style.alignBottomSearch());
        localize();
    }

    protected abstract void generateIds();

    protected abstract T doFlush();

    private void initListBoxEditors() {
        profileSelection = new ListModelListBoxEditor<>(new NullSafeRenderer<ProfileEntry>() {
            @Override
            public String renderNullSafe(ProfileEntry object) {
                return object.toString();
            }
        });

        roleSelection = new ListModelListBoxEditor<>(new NameRenderer<Role>());
        namespaceSelection = new ListModelListBoxEditor<>(new NullSafeRenderer<String>() {
            @Override
            protected String renderNullSafe(String object) {
                return object;
            }
        });
    }

    private void initTable() {
        groupNameColumn = new AbstractEntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getFirstName();
            }
        };
        searchItems.addColumn(groupNameColumn, constants.groupNamePermissionsPopup(), MAX_COL_WIDTH);

        displayNameColumn = new AbstractEntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getNote();
            }
        };
        searchItems.addColumn(displayNameColumn, constants.displayNamePermissionsPopup(), MAX_COL_WIDTH);
        // Table Entity Columns
        firstNameColumn = new AbstractEntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getFirstName();
            }
        };
        searchItems.addColumn(firstNameColumn, constants.firstNamePermissionsPopup(), MAX_COL_WIDTH);

        lastNameColumn = new AbstractEntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getLastName();
            }
        };
        searchItems.addColumn(lastNameColumn, constants.lastNamePermissionsPopup(), MAX_COL_WIDTH);

        userNameColumn = new AbstractEntityModelTextColumn<DbUser>() {
            @Override
            public String getText(DbUser user) {
                return user.getLoginName();
            }
        };
        searchItems.addColumn(userNameColumn, constants.userNamePermissionsPopup(), MAX_COL_WIDTH);
    }

    @Override
    public void userTypeChanged(UserOrGroup newType, boolean setRadioValue) {
        boolean isUser = newType == UserOrGroup.User;
        searchItems.ensureColumnVisible(firstNameColumn, constants.firstNamePermissionsPopup(), isUser, MAX_COL_WIDTH);
        searchItems.ensureColumnVisible(groupNameColumn, constants.groupNamePermissionsPopup(), !isUser, MAX_COL_WIDTH);
        searchItems.ensureColumnVisible(lastNameColumn, constants.lastNamePermissionsPopup(), isUser, MAX_COL_WIDTH);
        searchItems.ensureColumnVisible(userNameColumn, constants.userNamePermissionsPopup(), isUser, MAX_COL_WIDTH);
        searchItems.ensureColumnVisible(displayNameColumn, constants.displayNamePermissionsPopup(), !isUser,
                MAX_COL_WIDTH);
        if (setRadioValue) {
            if (isUser) {
                specificUserRadio.setValue(true, false);
            } else {
                specificGroupRadio.setValue(true, false);
            }
        }
    }

    void localize() {
        searchButton.setLabel(constants.goPermissionsPopup());
    }

    @Override
    public void edit(final T object) {
        searchItems.setRowData(new ArrayList<>());
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
    public HasClickHandlers getSpecificUserRadio() {
        return specificUserRadio;
    }

    @Override
    public HasClickHandlers getSpecificGroupRadio() {
        return specificGroupRadio;
    }

    @Override
    public HasClickHandlers getMyGroupsRadio() {
        return myGroupsRadio;
    }

    @Override
    public HasHandlers getSearchStringEditor() {
        return searchStringEditor;
    }

    @Override
    public void changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(boolean isEveryone, boolean isMyGroups) {
        boolean isEveryoneOrMyGroups = isEveryone || isMyGroups;
        profileSelection.setEnabled(!isEveryoneOrMyGroups);
        namespaceSelection.setEnabled(!isEveryoneOrMyGroups);
        searchStringEditor.setEnabled(!isEveryoneOrMyGroups);
        searchButton.getCommand().setIsExecutionAllowed(!isEveryoneOrMyGroups);
        searchItems.setVisible(!isEveryone);
    }

    @Override
    public HasValue<String> getSearchString() {
        return searchStringEditor;
    }

    @Override
    public void hideRoleSelection(boolean indic) {
        roleSelectionPanel.setVisible(!indic);
    }

    @Override
    public void hideEveryoneSelection(boolean indic) {
        everyoneRadio.setVisible(!indic);
        myGroupsRadio.setVisible(!indic);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        errorMessage.setText(message);
    }

    @Override
    public void setLoadingState(LoadingState state) {
        searchItems.setLoadingState(state);
    }

}
