package org.ovirt.engine.ui.common.uicommon.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(ModelBoundPopupHandler.class.getName());

    private final ModelBoundPopupResolver<M> popupResolver;
    private final EventBus eventBus;

    private final Set<String> windowPropertyNames = new HashSet<>();
    private final Set<String> confirmWindowPropertyNames = new HashSet<>();

    private final Map<String, AbstractModelBoundPopupPresenterWidget<?, ?>> windowPopupInstances = new HashMap<>();
    private final Map<String, AbstractModelBoundPopupPresenterWidget<?, ?>> confirmWindowPopupInstances = new HashMap<>();

    private Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider;

    private boolean initialized = false;

    public ModelBoundPopupHandler(ModelBoundPopupResolver<M> popupResolver, EventBus eventBus) {
        this.popupResolver = popupResolver;
        this.eventBus = eventBus;
    }

    /**
     * Sets up a property change listener for model's "Window"-like and
     * "ConfirmWindow"-like properties, which are used to trigger dialogs.
     * <p>
     * Shouldn't be called more than once - each {@link ModelBoundPopupHandler}
     * instance should handle a specific model.
     */
    public void initDialogModelListener(M model) {
        if (initialized) {
            logger.warning("Trying to re-initialize dialog model listener for " + model.getClass().getName()); //$NON-NLS-1$
        }

        // Init property names
        windowPropertyNames.clear();
        windowPropertyNames.addAll(model.getWindowProperties().keySet());

        confirmWindowPropertyNames.clear();
        confirmWindowPropertyNames.addAll(model.getConfirmWindowProperties().keySet());

        // Init popup instance maps
        windowPopupInstances.clear();
        for (String propName : windowPropertyNames) {
            windowPopupInstances.put(propName, null);
        }

        confirmWindowPopupInstances.clear();
        for (String propName : confirmWindowPropertyNames) {
            confirmWindowPopupInstances.put(propName, null);
        }

        // Add property change listener to the model
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;

            if (windowPropertyNames.contains(propName)) {
                handleWindowModelChange(model, propName, windowPopupInstances.get(propName), false);
            } else if (confirmWindowPropertyNames.contains(propName)) {
                handleWindowModelChange(model, propName, confirmWindowPopupInstances.get(propName), true);
            }
        });

        initialized = true;
    }

    public void setDefaultConfirmPopupProvider(
            Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider) {
        this.defaultConfirmPopupProvider = defaultConfirmPopupProvider;
    }

    /**
     * Handles the change of given property (as defined by {@code propertyName})
     * for the given model.
     * <p>
     * {@code currentPopup} represents the GWTP popup instance associated with the
     * property (can be {@code null} to indicate that the associated popup is not
     * active at the moment).
     * <p>
     * {@code isConfirmation} is used to differentiate between "Window"-like and
     * "ConfirmWindow"-like properties:
     * <ul>
     * <li>"Window"-like properties have popups resolved via
     *      {@link ModelBoundPopupResolver#getModelPopup} method</li>
     * <li>"ConfirmWindow"-like properties have popups resolved via
     *      {@link ModelBoundPopupResolver#getConfirmModelPopup} method</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    void handleWindowModelChange(M sourceModel, String propertyName,
            AbstractModelBoundPopupPresenterWidget<?, ?> currentPopup,
            boolean isConfirmation) {
        // Model behind the popup
        Model windowModel = isConfirmation
                ? sourceModel.getConfirmWindowProperties().get(propertyName)
                : sourceModel.getWindowProperties().get(propertyName);

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
                    sourceModel.setConfirmWindowProperty(propertyName, null);
                } else {
                    sourceModel.setWindowProperty(propertyName, null);
                }
            }
        } else if (windowModel == null && currentPopup != null) {
            // Close existing popup
            hideAndClearPopup(propertyName, currentPopup, isConfirmation);
        }
    }

    /**
     * Reveals the popup (tells GWTP to show it).
     */
    <T extends Model> void revealPopup(T model, AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
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

    /**
     * Tells the popup to start or stop progress indicator, based on model's
     * {@linkplain Model#getProgress progress} property.
     */
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

}
