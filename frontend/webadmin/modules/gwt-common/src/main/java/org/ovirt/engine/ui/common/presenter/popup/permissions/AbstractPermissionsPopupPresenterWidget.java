package org.ovirt.engine.ui.common.presenter.popup.permissions;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.HasValue;

public abstract class AbstractPermissionsPopupPresenterWidget<V extends AbstractPermissionsPopupPresenterWidget.ViewDef<M>, M extends AdElementListModel>
        extends AbstractModelBoundPopupPresenterWidget<M, V> {

    public interface ViewDef<A extends AdElementListModel> extends AbstractModelBoundPopupPresenterWidget.ViewDef<A> {

        HasUiCommandClickHandlers getSearchButton();

        HasKeyPressHandlers getKeyPressSearchInputBox();

        HasValue<String> getSearchString();

        HasClickHandlers getEveryoneRadio();

        HasClickHandlers getSpecificUserOrGroupRadio();

        HasHandlers getSearchStringEditor();

        PopupNativeKeyPressHandler getNativeKeyPressHandler();

        void changeStateOfElementsWhenAccessIsForEveryone(boolean isEveryone);

        void hideRoleSelection(Boolean indic);

        void hideEveryoneSelection(Boolean indic);

    }

    public AbstractPermissionsPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void init(final M model) {
        // Let the parent do its work
        super.init(model);

        getView().getSearchButton().setCommand(model.getSearchCommand());

        registerHandler(getView().getSearchButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getSearchButton().getCommand().execute();
            }
        }));

        model.getSearchInProgress().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().getSearchButton()
                        .getCommand()
                        .setIsExecutionAllowed(!(Boolean) model.getSearchInProgress().getEntity());
            }
        });

        registerHandler(getView().getKeyPressSearchInputBox().addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                    model.setSearchString(getView().getSearchString().getValue());
                    getView().getSearchButton().getCommand().execute();
                }
            }
        }));

        registerHandler(getView().getEveryoneRadio().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.setIsEveryoneSelected(true);
                getView().changeStateOfElementsWhenAccessIsForEveryone(true);
                // Disable relevant elements
            }
        }));

        registerHandler(getView().getSpecificUserOrGroupRadio().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.setIsEveryoneSelected(false);
                getView().changeStateOfElementsWhenAccessIsForEveryone(false);
            }
        }));

        model.getProfile().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                model.populateNamespaces();
            }
        });

        model.getNamespace().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().getSearchButton()
                .getCommand()
                .setIsExecutionAllowed(model.availableNamespaces());
            }
        });

        model.getIsRoleListHiddenModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().hideRoleSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel()
                        .getEntity().toString()));
            }
        });

        getView().hideEveryoneSelection((Boolean) model.getIsEveryoneSelectionHidden().getEntity());

        model.getIsEveryoneSelectionHidden().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().hideEveryoneSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel()
                        .getEntity().toString()));
            }
        });

        PermissionPopupNativeKeyPressHandler keyPressHandler =
                new PermissionPopupNativeKeyPressHandler(getView().getNativeKeyPressHandler(), model);
        getView().setPopupKeyPressHandler(keyPressHandler);

    }

    class PermissionPopupNativeKeyPressHandler implements PopupNativeKeyPressHandler {

        private final PopupNativeKeyPressHandler decorated;
        private final M model;

        private boolean hasFocus = false;

        public PermissionPopupNativeKeyPressHandler(PopupNativeKeyPressHandler decorated, M model) {
            this.decorated = decorated;
            this.model = model;

            ((HasFocusHandlers) getView().getSearchStringEditor()).addFocusHandler(new FocusHandler() {

                @Override
                public void onFocus(FocusEvent event) {
                    hasFocus = true;
                }
            });

            ((HasBlurHandlers) getView().getSearchStringEditor()).addBlurHandler(new BlurHandler() {

                @Override
                public void onBlur(BlurEvent event) {
                    hasFocus = false;
                }
            });
        }

        @Override
        public void onKeyPress(NativeEvent event) {
            if (hasFocus && KeyCodes.KEY_ENTER == event.getKeyCode()) {
                model.setSearchString(getView().getSearchString().getValue());
                getView().getSearchButton().getCommand().execute();
            } else {
                decorated.onKeyPress(event);
            }
        }
    }

}
