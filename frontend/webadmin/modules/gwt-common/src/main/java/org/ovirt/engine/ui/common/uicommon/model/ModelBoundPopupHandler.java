package org.ovirt.engine.ui.common.uicommon.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
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

    private AbstractModelBoundPopupPresenterWidget<?, ?> windowPopup;
    private AbstractModelBoundPopupPresenterWidget<?, ?> confirmWindowPopup;

    private Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider;

    public ModelBoundPopupHandler(ModelBoundPopupResolver<M> popupResolver, EventBus eventBus) {
        this.popupResolver = popupResolver;
        this.eventBus = eventBus;

        windowPropertyNames.addAll(Arrays.asList(popupResolver.getWindowPropertyNames()));
        confirmWindowPropertyNames.addAll(Arrays.asList(popupResolver.getConfirmWindowPropertyNames()));
    }

    /**
     * Adds a property change listener to the model that responds when a new window model is set.
     *
     * TODO rename addWindowModelChangeListener
     */
    public void addDialogModelListener(final M source) {
        hideAndClearAllPopups();
        source.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;

                if (windowPropertyNames.contains(propName)) {
                    handleWindowModelChange(source, windowPopup, false, propName);
                } else if (confirmWindowPropertyNames.contains(propName)) {
                    handleWindowModelChange(source, confirmWindowPopup, true, propName);
                }
            }
        });
    }

    public void setDefaultConfirmPopupProvider(
            Provider<? extends AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>> defaultConfirmPopupProvider) {
        this.defaultConfirmPopupProvider = defaultConfirmPopupProvider;
    }

    // TODO this should be redesigned -- way too complex. GS
    @SuppressWarnings("unchecked")
    void handleWindowModelChange(M sourceModel, AbstractModelBoundPopupPresenterWidget<?, ?> currentPopup,
            boolean isConfirmation, String propertyName) {
        Model windowModel = isConfirmation ? popupResolver.getConfirmWindowModel(sourceModel, propertyName)
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
                    // Fall back to basic confirmation popup if possible
                    newPopup = defaultConfirmPopupProvider.get();
                }
            } else {
                // Resolve main popup
                newPopup = popupResolver.getModelPopup(sourceModel, lastExecutedCommand, windowModel);
            }

            // 2. Reveal
            if (newPopup != null) {
                revealAndAssignPopup(windowModel,
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
            hideAndClearPopup(currentPopup, isConfirmation);
        }
    }

    /**
     * Reveals the popup (tells GWTP to actually show it)
     */
    <T extends Model> void revealPopup(final T model,
            final AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
        assert model != null : "Popup model must not be null"; //$NON-NLS-1$

        // Initialize popup
        popup.init(model);

        // Add "PROGRESS" property change handler to Window model
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) { //$NON-NLS-1$
                    updatePopupProgress(model, popup);
                }
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
     * Reveals the popup (tells GWTP to actually show it) and remembers its reference, so that it can be closed (hidden) later on.
     */
    protected <T extends Model> void revealAndAssignPopup(T model,
            AbstractModelBoundPopupPresenterWidget<T, ?> popup, boolean isConfirm) {
        revealPopup(model, popup);

        // Assign popup reference
        if (isConfirm) {
            confirmWindowPopup = popup;
        } else {
            windowPopup = popup;
        }
    }

    /**
     * Hides a popup and clears its reference, so that another popup can be opened.
     */
    protected void hideAndClearPopup(AbstractModelBoundPopupPresenterWidget<?, ?> popup, boolean isConfirm) {
        popup.hideAndUnbind();

        // Clear popup reference
        if (isConfirm) {
            confirmWindowPopup = null;
        } else {
            windowPopup = null;
        }
    }

    /**
     * Hides confirmation and window popups and clears their references.
     */
    void hideAndClearAllPopups() {
        if (confirmWindowPopup != null) {
            hideAndClearPopup(confirmWindowPopup, true);
        }
        if (windowPopup != null) {
            hideAndClearPopup(windowPopup, false);
        }
    }

}
