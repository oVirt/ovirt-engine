package org.ovirt.engine.ui.common.uicommon.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

/**
 * Contains some logic for handling window model changes for a given source model.
 * Contains the code that actually reveals the popups via GWTP.
 *
 * TODO rename to WindowModelPopupHandler
 * TODO this class is just a ball of stuff with no one well-defined purpose. Needs a refactor.
 *
 * <p>
 * Uses {@link ModelBoundPopupResolver} for resolving popup widgets from dialog models.
 *
 * @param <M> Model type.
 */
public class ModelBoundPopupHandler<M extends IModel> {

    private final ModelBoundPopupResolver<M> popupResolver;
    private final EventBus eventBus;

    private final Set<String> windowPropertyNames = new HashSet<>();
    private final Set<String> confirmWindowPropertyNames = new HashSet<>();

    private Map<String, AbstractModelBoundPopupPresenterWidget<?, ?>> windowPopupInstances = new HashMap<>();
    private Map<String, AbstractModelBoundPopupPresenterWidget<?, ?>> confirmWindowPopupInstances = new HashMap<>();

    private Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider;

    public ModelBoundPopupHandler(ModelBoundPopupResolver<M> popupResolver, EventBus eventBus) {
        this.popupResolver = popupResolver;
        this.eventBus = eventBus;

        init();
    }

    void init() {
        windowPropertyNames.addAll(Arrays.asList(popupResolver.getWindowPropertyNames()));
        confirmWindowPropertyNames.addAll(Arrays.asList(popupResolver.getConfirmWindowPropertyNames()));

        for (String propName : windowPropertyNames) {
            windowPopupInstances.put(propName, null);
        }

        for (String propName : confirmWindowPropertyNames) {
            confirmWindowPopupInstances.put(propName, null);
        }
    }

    /**
     * Adds a property change listener to the model that responds when a new window model is set.
     *
     * TODO rename addWindowModelChangeListener
     */
    public void addDialogModelListener(final M model) {
        hideAndClearAllPopups();

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;

            if (windowPropertyNames.contains(propName)) {
                handleWindowModelChange(model, propName, windowPopupInstances.get(propName), false);
            } else if (confirmWindowPropertyNames.contains(propName)) {
                handleWindowModelChange(model, propName, confirmWindowPopupInstances.get(propName), true);
            }
        });
    }

    public void setDefaultConfirmPopupProvider(
            Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider) {
        this.defaultConfirmPopupProvider = defaultConfirmPopupProvider;
    }

    // TODO this should be redesigned -- way too complex. GS
    @SuppressWarnings("unchecked")
    void handleWindowModelChange(M sourceModel, String propertyName,
            AbstractModelBoundPopupPresenterWidget<?, ?> currentPopup,
            boolean isConfirmation) {
        Model windowModel = isConfirmation
                ? popupResolver.getConfirmWindowModel(sourceModel, propertyName)
                : popupResolver.getWindowModel(sourceModel, propertyName);

        // Reveal new popup
        if (windowModel != null && currentPopup == null) {

            // 1. Resolve
            AbstractModelBoundPopupPresenterWidget<?, ?> newPopup = null;
            UICommand lastExecutedCommand = sourceModel.getLastExecutedCommand();

            if (windowModel instanceof ConfirmationModel) {
                // Resolve confirmation popup
                newPopup = popupResolver.getConfirmModelPopup(sourceModel, lastExecutedCommand);

                if (newPopup == null && defaultConfirmPopupProvider != null) {
                    // Fall back to basic confirmation popup
                    newPopup = defaultConfirmPopupProvider.get();
                }
            } else {
                // Resolve main popup
                newPopup = popupResolver.getModelPopup(sourceModel, lastExecutedCommand, windowModel);
            }

            // 2. Reveal
            if (newPopup != null) {
                revealAndAssignPopup(windowModel, propertyName,
                        (AbstractModelBoundPopupPresenterWidget<Model, ?>) newPopup,
                        isConfirmation);
            } else {
                if (isConfirmation) {
                    popupResolver.clearConfirmWindowModel(sourceModel, propertyName);
                } else {
                    popupResolver.clearWindowModel(sourceModel, propertyName);
                }
            }
        }

        else if (windowModel == null && currentPopup != null) {
            hideAndClearPopup(propertyName, currentPopup, isConfirmation);
        }
    }

    /**
     * Reveals the popup (tells GWTP to show it).
     */
    <T extends Model> void revealPopup(final T model, final AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
        assert model != null : "Popup model must not be null"; //$NON-NLS-1$

        // Initialize popup
        popup.init(model);

        // Add "progress" property change handler to Window model
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                updatePopupProgress(model, popup);
            }
        });
        updatePopupProgress(model, popup);

        // Reveal popup
        RevealRootPopupContentEvent.fire(eventBus, popup);
    }

    <T extends Model> void updatePopupProgress(T model, AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
        if (model.getProgress() != null) {
            popup.startProgress(model.getProgress().getCurrentOperation());
        } else {
            popup.stopProgress();
        }
    }

    /**
     * Reveals the popup (tells GWTP to show it) and remembers its reference,
     * so that it can be closed (hidden) later on.
     */
    protected <T extends Model> void revealAndAssignPopup(T model, String propertyName,
            AbstractModelBoundPopupPresenterWidget<T, ?> popup, boolean isConfirm) {
        revealPopup(model, popup);

        // Assign popup reference
        if (isConfirm) {
            confirmWindowPopupInstances.put(propertyName, popup);
        } else {
            windowPopupInstances.put(propertyName, popup);
        }
    }

    /**
     * Hides a popup and clears its reference, so that another popup can be opened.
     */
    protected void hideAndClearPopup(String propertyName,
            AbstractModelBoundPopupPresenterWidget<?, ?> popup,
            boolean isConfirm) {
        popup.hideAndUnbind();

        // Clear popup reference
        if (isConfirm) {
            confirmWindowPopupInstances.put(propertyName, null);
        } else {
            windowPopupInstances.put(propertyName, null);
        }
    }

    /**
     * Hides confirmation and window popups and clears their references.
     *
     * TODO(vs) this method existed so that all application popups can be
     * shown again after auto-logout and re-login without having to reload
     * WebAdmin page in browser. With SSO in place (no more GWT UI specific
     * login screen), this use case is irrelevant and this method should be
     * removed.
     */
    void hideAndClearAllPopups() {
        for (String propName : windowPropertyNames) {
            AbstractModelBoundPopupPresenterWidget<?, ?> popup = windowPopupInstances.get(propName);

            if (popup != null) {
                hideAndClearPopup(propName, popup, false);
            }
        }

        for (String propName : confirmWindowPropertyNames) {
            AbstractModelBoundPopupPresenterWidget<?, ?> confirmPopup = confirmWindowPopupInstances.get(propName);

            if (confirmPopup != null) {
                hideAndClearPopup(propName, confirmPopup, true);
            }
        }
    }

}
