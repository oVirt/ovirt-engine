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
 * Encapsulates logic for handling dialog models of the given source model.
 * <p>
 * Uses {@link ModelBoundPopupResolver} for resolving popup widgets from dialog models.
 *
 * @param <M>
 *            Model type.
 */
public class ModelBoundPopupHandler<M extends IModel> {

    private final ModelBoundPopupResolver<M> popupResolver;
    private final EventBus eventBus;

    private final Set<String> windowPropertyNames = new HashSet<String>();
    private final Set<String> confirmWindowPropertyNames = new HashSet<String>();

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
     * Adds a property change listener to the given source model that handles its dialog models.
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

    @SuppressWarnings("unchecked")
    void handleWindowModelChange(M source, AbstractModelBoundPopupPresenterWidget<?, ?> popup,
            boolean isConfirm, String propertyName) {
        Model windowModel = isConfirm ? popupResolver.getConfirmWindowModel(source, propertyName)
                : popupResolver.getWindowModel(source, propertyName);

        // Reveal new popup
        if (windowModel != null && popup == null) {
            // 1. Resolve
            AbstractModelBoundPopupPresenterWidget<?, ?> newPopup = null;
            UICommand lastExecutedCommand = source.getLastExecutedCommand();

            if (windowModel instanceof ConfirmationModel) {
                // Resolve confirmation popup
                newPopup = popupResolver.getConfirmModelPopup(source, lastExecutedCommand);

                if (newPopup == null && defaultConfirmPopupProvider != null) {
                    // Fall back to basic confirmation popup if possible
                    newPopup = defaultConfirmPopupProvider.get();
                }
            } else {
                // Resolve main popup
                newPopup = popupResolver.getModelPopup(source, lastExecutedCommand, windowModel);
            }

            // 2. Reveal
            if (newPopup != null) {
                revealAndAssignPopup(windowModel,
                        (AbstractModelBoundPopupPresenterWidget<Model, ?>) newPopup,
                        isConfirm);
            } else {
                // No popup bound to model, need to clear model reference manually
                if (isConfirm) {
                    popupResolver.clearConfirmWindowModel(source, propertyName);
                } else {
                    popupResolver.clearWindowModel(source, propertyName);
                }
            }
        }

        // Hide existing popup
        else if (windowModel == null && popup != null) {
            hideAndClearPopup(popup, isConfirm);
        }
    }

    /**
     * Reveals a popup bound to the given model.
     */
    <T extends Model> void revealPopup(final T model,
            final AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
        assert (model != null) : "Popup model must not be null"; //$NON-NLS-1$

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
     * Reveals a model-bound popup and remembers its reference, so that it can be closed (hidden) later on.
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
     * Hides a model-bound popup and clears its reference, so that another popup can be opened.
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
     * Hides all model-bound popups and clears their references.
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
