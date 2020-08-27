package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.Convertible;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IProvidePropertyChangedEvent;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ProvidePropertyChangedEvent;

import com.google.gwt.event.shared.HasHandlers;

public class EntityModel<T> extends Model implements HasHandlers, HasEntity<T> {

    public static final String ENTITY = "Entity"; //$NON-NLS-1$
    private Event<EventArgs> privateEntityChangedEvent;
    private boolean entityPresent = true;

    @Override
    public Event<EventArgs> getEntityChangedEvent() {
        return privateEntityChangedEvent;
    }

    private void setEntityChangedEvent(Event<EventArgs> value) {
        privateEntityChangedEvent = value;
    }

    private T entity;

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public void setEntity(T value) {
        if (entity != value) {
            entityChanging(value, entity);
            entity = value;
            onEntityChanged();
            // EntityChanged(this, EventArgs.Empty);
            getEntityChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(ENTITY);
        }
    }

    @Override
    public boolean isEntityPresent() {
        return entityPresent;
    }

    @Override
    public void setEntityPresent(boolean flag) {
        this.entityPresent = flag;
    }

    @Override
    public EntityModel<T> setIsChangeable(boolean value) {
        return setIsChangeable(value, null);
    }

    @Override
    public EntityModel<T> setIsChangeable(boolean value, String reason) {
        super.setIsChangeable(value, reason);
        return this;
    }

    @Override
    public EntityModel<T> setTitle(String value) {
        super.setTitle(value);
        return this;
    }

    public void setEntity(T value, boolean fireEvents) {
        if (fireEvents) {
            setEntity(value);
        } else {
            entity = value;
        }
    }

    public EntityModel() {
        setEntityChangedEvent(new Event<>(entityChangedEventDefinition));
    }

    public EntityModel(T entity) {
        this();

        setEntity(entity);
    }

    public EntityModel(String title, T entity) {
        this(entity);

        setTitle(title);
    }

    protected void entityChanging(T newValue, T oldValue) {
        IProvidePropertyChangedEvent notifier =
                (IProvidePropertyChangedEvent) ((oldValue instanceof IProvidePropertyChangedEvent) ? oldValue : null);
        if (notifier != null) {
            notifier.getPropertyChangedEvent().removeListener(this);
        }

        notifier =
                (IProvidePropertyChangedEvent) ((newValue instanceof IProvidePropertyChangedEvent) ? newValue : null);
        if (notifier != null) {
            notifier.getPropertyChangedEvent().addListener(this);
        }
    }

    protected void onEntityChanged() {
    }

    /**
     * Invoked whenever some property of the entity was changed.
     */
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(entityChangedEventDefinition)) {
            onEntityChanged();
        } else if (ev.matchesDefinition(ProvidePropertyChangedEvent.definition)) {
            entityPropertyChanged(sender, (PropertyChangedEventArgs) args);
        }
    }

    public void validateEntity(IValidation[] validations) {
        setIsValid(true);

        if (!getIsAvailable() || !getIsChangable()) {
            return;
        }

        if (!isEntityPresent()) {
            setIsValid(false);
            return;
        }

        for (IValidation validation : validations) {
            ValidationResult result = validation.validate(getEntity());
            if (!result.getSuccess()) {
                for (String reason : result.getReasons()) {
                    getInvalidityReasons().add(reason);
                }
                setIsValid(false);

                break;
            }
        }
    }

    public Convertible asConvertible() {
        return new Convertible(this);
    }

    @Override
    public void cleanup() {
        cleanupEvents(getEntityChangedEvent());
        super.cleanup();
    }
}
