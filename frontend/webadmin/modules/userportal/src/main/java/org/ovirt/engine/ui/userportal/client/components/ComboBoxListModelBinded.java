package org.ovirt.engine.ui.userportal.client.components;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.binders.ModelBindedComponent;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolver;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolverFactory;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;

public class ComboBoxListModelBinded extends UPComboBoxItem implements ModelBindedComponent {
	private ListModel listModel;
	private int itemsCount = 0;
	
	boolean async;
	
	public ComboBoxListModelBinded(String title, ListModel model, Class<?> type) {
		this(true, title, model, ObjectNameIdResolverFactory.getResolver(type));
	}
	
	public ComboBoxListModelBinded(boolean async, String title, ListModel model, Class<?> type) {
		this(async, title, model, ObjectNameIdResolverFactory.getResolver(type));
	}
	
	public ComboBoxListModelBinded(String title, ListModel model, ObjectNameIdResolver resolver) {
		this(true, title, model, resolver);
	}

	public ComboBoxListModelBinded(boolean async, String title, ListModel model, ObjectNameIdResolver resolver) {
		setTitle("<nobr>" + title + "</nobr>");
		setTitleAlign(Alignment.LEFT);
		this.async = async;
		this.listModel = model;
		setSelectItemEvents(async, resolver);
	}
	
	public Model getModel() {
		return listModel;
	}

	private void setSelectItemEvents(final boolean async, final ObjectNameIdResolver resolver) {
		final ListModel model = listModel;
		
		model.getItemsChangedEvent().addListener(new ChangedModelItemEventHandler(this, model) {
			public String getItemName(Object o) {
				return resolver.getItemName(o);
			}
			public String getItemId(Object o) {
				return resolver.getItemId(o);
			}
		});
		
		model.getSelectedItemChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
			    if (async && getForm() == null)
					return;
			    
				GWT.log("Item changed: " + getTitle() + " Changed to: " + model.getSelectedItem());
				if (model.getSelectedItem() != null)
				    setValue(resolver.getItemId(model.getSelectedItem()));
				else if (getDisplayValue() != null)
				    setValue((String)null); // Clear the selected item
			}
		});
		
		addChangedHandler(new BoxSelectItemChanged(model) {
			String getItemId(Object o) {
				return resolver.getItemId(o);
			}
		});		

		model.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
			    if (async && getForm() == null)
					return;
				
				String property = ((PropertyChangedEventArgs)args).PropertyName;
				
				if (property.equals("IsChangable")) {
					if (!model.getIsChangable()) {
						setShowDisabled(true);
						setDisabled(true);
					} else {
						setDisabled(false);
					}
				}
				else if (property.equals("IsAvailable")) {
					if (!model.getIsAvailable()) {
						hide();
					}
					else {
						show();
					}
				}
				else if (property.equals("IsValid")) {
					if (!listModel.getIsValid()) {
						setTextBoxStyle("selectBoxInvalid");
						if (listModel.getInvalidityReasons() != null && listModel.getInvalidityReasons().size()>0) {
							setTooltip("<nobr>" + listModel.getInvalidityReasons().iterator().next() + "</nobr>");
							setHoverStyle("gridToolTipStyle");
							setHoverWidth(1);
							setHoverDelay(100);
						}
					}
					else {
						setTextBoxStyle("engineSelectItem");
						setTooltip("");
					}
				}
			}
		});
		
		if (!async) {
			model.getItemsChangedEvent().raise(model, EventArgs.Empty);			
			model.getSelectedItemChangedEvent().raise(model, EventArgs.Empty);
		}
	}
	abstract class ChangedModelItemEventHandler implements IEventListener {
		
		private ComboBoxListModelBinded selectBox;
		private Model model;
		public LinkedHashMap<String,String> valueMap; 

		public ChangedModelItemEventHandler(ComboBoxListModelBinded selectBox, ListModel model) {
			this.selectBox = selectBox;
			this.model = model;
		}
		
		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {
			if (async && getForm() == null)
				return;
			
			valueMap = new LinkedHashMap<String, String>();
			ListModel listModel = (ListModel)sender;
			Iterable items = listModel.getItems();
				
			if (items != null) {
				GWT.log("Select box " + selectBox.getTitle() + " items loaded: " + items);
				itemsCount = 0;
				for (Object o : items) {
					itemsCount++;
					valueMap.put(getItemId(o), getItemName(o));
				}
				if (model.getIsChangable()) {
					setShowDisabled(false);
					setDisabled(false);
				}
				else {
					setShowDisabled(true);
					setDisabled(!model.getIsChangable());
				}
				
				// Set a tool tip with a prohibition reason when the select box is non-changeable
				if (!listModel.getIsChangable() && !listModel.getChangeProhibitionReasons().isEmpty()){
					setTooltip("<nobr>" + model.getChangeProhibitionReasons().iterator().next() + "</nobr>");
					setHoverStyle("gridToolTipStyle");
					setHoverWidth(1);
					setHoverDelay(100);
				}
			}
			else {
				GWT.log("Select box " + selectBox.getTitle() + " items loaded are null");
			}
			selectBox.setValueMap(valueMap);
		}
		
		public abstract String getItemId(Object o);
		public abstract String getItemName(Object o);
	}

	abstract class BoxSelectItemChanged implements ChangedHandler {

		private ListModel model;
		
		public BoxSelectItemChanged(ListModel model) {
			this.model = model;
		}
		
		@Override
		public void onChanged(ChangedEvent event) {
		    model.setSelectedItem(event.getValue().toString());
		}
		abstract String getItemId(Object o);
	}

}