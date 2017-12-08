package org.ovirt.engine.ui.common.presenter.popup.permissions;

import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel.AdSearchType;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.HasValue;

public abstract class AbstractPermissionsPopupPresenterWidget<V extends AbstractPermissionsPopupPresenterWidget.ViewDef<M>, M extends AdElementListModel>
        extends AbstractModelBoundPopupPresenterWidget<M, V> {

    public interface ViewDef<A extends AdElementListModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<A> {

        HasUiCommandClickHandlers getSearchButton();

        HasKeyPressHandlers getKeyPressSearchInputBox();

        HasValue<String> getSearchString();

        HasClickHandlers getEveryoneRadio();

        HasClickHandlers getSpecificUserRadio();

        HasClickHandlers getSpecificGroupRadio();

        HasClickHandlers getMyGroupsRadio();

        HasHandlers getSearchStringEditor();

        void changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(boolean isEveryone, boolean isMyGroups);

        void hideRoleSelection(boolean indic);

        void hideEveryoneSelection(boolean indic);

        void userTypeChanged(UserOrGroup newType, boolean setRadioValue);

        void setLoadingState(LoadingState state);
    }

    private boolean searchStringEditorHasFocus;

    public AbstractPermissionsPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void init(final M model) {
        // Let the parent do its work
        super.init(model);

        getView().getSearchButton().setCommand(model.getSearchCommand());

        registerHandler(getView().getSearchButton().addClickHandler(event -> {
            getView().setLoadingState(LoadingState.LOADING);
            getView().getSearchButton().getCommand().execute();
        }));

        model.getSearchInProgress().getEntityChangedEvent().addListener((ev, sender, args) -> getView().getSearchButton()
                .getCommand()
                .setIsExecutionAllowed(!model.getSearchInProgress().getEntity()));

        registerHandler(getView().getKeyPressSearchInputBox().addKeyPressHandler(event -> {
            if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                model.setSearchString(getView().getSearchString().getValue());
                getView().getSearchButton().getCommand().execute();
            }
        }));

        registerHandler(getView().getEveryoneRadio().addClickHandler(event -> {
            model.setSearchType(AdSearchType.EVERYONE);
            getView().changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(true, false);
            getView().userTypeChanged(UserOrGroup.User, false);
            model.setItems(null);
        }));

        registerHandler(getView().getMyGroupsRadio().addClickHandler(event -> {
            model.setSearchType(AdSearchType.MY_GROUPS);
            getView().changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(false, true);
            getModel().getSearchMyGroupsCommand().execute();
            getView().userTypeChanged(UserOrGroup.Group, false);
            model.setItems(null);
            getView().setLoadingState(LoadingState.LOADING);
        }));

        registerHandler(getView().getSpecificUserRadio().addClickHandler(event -> {
            model.setSearchType(AdSearchType.USER);
            getView().changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(false, false);
            getView().userTypeChanged(UserOrGroup.User, true);
            model.setItems(null);
        }));

        registerHandler(getView().getSpecificGroupRadio().addClickHandler(event -> {
            model.setSearchType(AdSearchType.GROUP);
            getView().changeStateOfElementsWhenAccessIsForEveryoneOrMyGroups(false, false);
            getView().userTypeChanged(UserOrGroup.Group, true);
            model.setItems(null);
        }));

        model.getProfile().getSelectedItemChangedEvent().addListener((ev, sender, args) -> model.populateNamespaces());

        model.getNamespace().getItemsChangedEvent().addListener((ev, sender, args) -> getView().getSearchButton()
        .getCommand()
        .setIsExecutionAllowed(model.availableNamespaces()));

        model.getIsRoleListHiddenModel().getPropertyChangedEvent().addListener((ev, sender, args) -> getView().hideRoleSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel()
                .getEntity().toString())));

        getView().hideEveryoneSelection(model.getIsEveryoneSelectionHidden().getEntity());

        model.getIsEveryoneSelectionHidden().getPropertyChangedEvent().addListener((ev, sender, args) -> getView().hideEveryoneSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel()
                .getEntity().toString())));

        HasHandlers searchStringEditor = getView().getSearchStringEditor();
        if (searchStringEditor instanceof HasFocusHandlers) {
            registerHandler(((HasFocusHandlers) searchStringEditor).addFocusHandler(event -> searchStringEditorHasFocus = true));
        }
        if (searchStringEditor instanceof HasBlurHandlers) {
            registerHandler(((HasBlurHandlers) searchStringEditor).addBlurHandler(event -> searchStringEditorHasFocus = false));
        }
    }

    @Override
    protected void onKeyPress(NativeEvent event) {
        M model = getModel();

        if (searchStringEditorHasFocus && KeyCodes.KEY_ENTER == event.getKeyCode()) {
            model.setSearchString(getView().getSearchString().getValue());
            getView().getSearchButton().getCommand().execute();
        } else {
            super.onKeyPress(event);
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        UserOrGroup searchType = UserOrGroup.User;
        if (getModel().getSearchType() == AdSearchType.GROUP) {
            searchType = UserOrGroup.Group;
        }
        getView().userTypeChanged(searchType, true);
    }

}
