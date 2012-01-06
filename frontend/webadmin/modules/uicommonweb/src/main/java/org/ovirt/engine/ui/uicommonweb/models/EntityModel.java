package org.ovirt.engine.ui.uicommonweb.models;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicommonweb.*;

@SuppressWarnings("unused")
public class EntityModel extends Model
{

	public static EventDefinition EntityChangedEventDefinition;
	private Event privateEntityChangedEvent;
	public Event getEntityChangedEvent()
	{
		return privateEntityChangedEvent;
	}
	private void setEntityChangedEvent(Event value)
	{
		privateEntityChangedEvent = value;
	}



	private Object entity;
	public Object getEntity()
	{
		return entity;
	}
	public void setEntity(Object value)
	{
		if (entity != value)
		{
			EntityChanging(value, entity);
			entity = value;
			OnEntityChanged();
				//EntityChanged(this, EventArgs.Empty);
			getEntityChangedEvent().raise(this, EventArgs.Empty);
			OnPropertyChanged(new PropertyChangedEventArgs("Entity"));
		}
	}

	public void setEntity(Object value, boolean fireEvents){
	    if (fireEvents){
	        setEntity(value);
	    }
	    else{
	        entity = value;
	    }
	}

	static
	{
		EntityChangedEventDefinition = new EventDefinition("EntityChanged", EntityModel.class);
	}

	public EntityModel()
	{
		setEntityChangedEvent(new Event(EntityChangedEventDefinition));
	}

	protected void EntityChanging(Object newValue, Object oldValue)
	{
		IProvidePropertyChangedEvent notifier = (IProvidePropertyChangedEvent)((oldValue instanceof IProvidePropertyChangedEvent) ? oldValue : null);
		if (notifier != null)
		{
			notifier.getPropertyChangedEvent().removeListener(this);
		}

		notifier = (IProvidePropertyChangedEvent)((newValue instanceof IProvidePropertyChangedEvent) ? newValue : null);
		if (notifier != null)
		{
			notifier.getPropertyChangedEvent().addListener(this);
		}
	}

	protected void OnEntityChanged()
	{
	}

	/**
	 Invoked whenever some property of the entity was changed.
	*/
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(EntityChangedEventDefinition))
		{
			OnEntityChanged();
		}
		else if (ev.equals(ProvidePropertyChangedEvent.Definition))
		{
			EntityPropertyChanged(sender, (PropertyChangedEventArgs)args);
		}
	}


	public void ValidateEntity(IValidation[] validations)
	{
		setIsValid(true);

		if (!getIsAvailable() || !getIsChangable())
		{
			return;
		}

		for (IValidation validation : validations)
		{
			ValidationResult result = validation.Validate(getEntity());
			if (!result.getSuccess())
			{
				for (String reason : result.getReasons())
				{
					getInvalidityReasons().add(reason);
				}
				setIsValid(false);

				break;
			}
		}
	}
}