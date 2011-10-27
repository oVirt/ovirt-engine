package org.ovirt.engine.ui.userportal.client.components;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.binders.ModelBindedComponent;
import org.ovirt.engine.ui.userportal.client.parsers.UPParser;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;

public class TextItemEntityModelBinded extends UPTextItem implements ModelBindedComponent {
	EntityModel entityModel;
	UPParser parser;
	
	public TextItemEntityModelBinded(String title, EntityModel model) {
		this(true, title, model, false);
	}

	public TextItemEntityModelBinded(String title, EntityModel model, boolean isIntField) {
		this(true, title, model, isIntField, null);
	}

	public TextItemEntityModelBinded(String title, EntityModel model, boolean isIntField, UPParser parser) {
		this(true, title, model, isIntField, parser);
	}

	public TextItemEntityModelBinded(boolean async, String title, EntityModel model, boolean isIntField) {
		this(async, title, model, isIntField, null);
	}
	
	public TextItemEntityModelBinded(boolean async, String title, EntityModel model, boolean isIntField, UPParser parser) {
		this.parser = parser;
		entityModel = model;
		
		setDisabled(!model.getIsChangable());
		setTitle("<nobr>" + title + "</nobr>");
		setTitleAlign(Alignment.LEFT);
		
		String value = "";

		if (model.getEntity() instanceof String)
			value = (String)model.getEntity();

		if (model.getEntity() instanceof Integer) {
			value = ((Integer)model.getEntity()).toString();
		}

		if (parser != null) {
			value = parser.format(value);
		}
		
		setValue(value);
		
		addKeyUpHandler(new NewVmTextItemKeyUpHandler(isIntField));
		addBlurHandler(new NewVmTextItemBlurHandler(isIntField));
		model.getEntityChangedEvent().addListener(new TextItemChangedEventListener(this));
		model.getPropertyChangedEvent().addListener(new TextItemPropertyChangedEventListener(this));
		
		if (!async) {
			entityModel.getEntityChangedEvent().raise(model, EventArgs.Empty);
		}
	}
	
	public class NewVmTextItemKeyUpHandler implements KeyUpHandler {
		boolean convertToInt = false;
		public NewVmTextItemKeyUpHandler(boolean convertToInt) {
			this.convertToInt = convertToInt;
		}

        @Override
        public void onKeyUp(KeyUpEvent event) {
            TextItemEntityModelBinded source = (TextItemEntityModelBinded) event.getSource();
            String title = source.getTitle();
            Object inputValue = source.getValue();
            Object currentValue = entityModel.getEntity();
            GWT.log("Getting:" + currentValue);

            // parse
            if (parser != null) {
                inputValue = parser.parse((String) inputValue);
            }

            // convert int
            if (convertToInt) {
                try {
                    inputValue = Integer.parseInt((String) inputValue);
                } catch (NumberFormatException e) {
                    return;
                }
            }

            // do not set if value has not changed
            if (currentValue != null && currentValue.equals(inputValue)) {
                return;
            }

            // set
            GWT.log("Setting " + title + " to: " + inputValue);
            entityModel.setEntity(inputValue);

        }
    }

	public class NewVmTextItemBlurHandler implements BlurHandler {	    
	       boolean convertToInt = false;
	        public NewVmTextItemBlurHandler(boolean convertToInt) {
	            this.convertToInt = convertToInt;
	        }
   
        @Override
        public void onBlur(BlurEvent event) {
            TextItemEntityModelBinded source = (TextItemEntityModelBinded) event.getSource();
            String inputValue = (String) source.getValue();

            // display formatted value if a parser exists
            if (parser != null) {
                String displayValue = parser.format((String) inputValue);
                source.setValue(displayValue);
            }
        }
    }

    public class TextItemChangedEventListener implements IEventListener {
		private TextItemEntityModelBinded textItem;
		public TextItemChangedEventListener(TextItemEntityModelBinded textItem) {
			this.textItem = textItem;
		}

		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {
			GWT.log("Text item changed: " + textItem.getTitle() + " Changed to: " + ((EntityModel)sender).getEntity());

			if (((EntityModel)sender).getEntity() != null) {
				String newValue = ((EntityModel)sender).getEntity().toString(); 
			    // if the value is same as the current one, do nothing
                if (newValue.equals(textItem.getValue())) {
                    return;
                }
				if (parser == null) {
					textItem.setValue(newValue);
				}
				else {
					textItem.setValue(parser.format(newValue));
					
				}
				textItem.redraw();
			}
			else {
				textItem.setValue("");
			}
		}
	}

	public class TextItemPropertyChangedEventListener implements IEventListener {
		private TextItemEntityModelBinded textItem;
		public TextItemPropertyChangedEventListener(TextItemEntityModelBinded textItem) {
			this.textItem = textItem;
		}

		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {
			String property = ((PropertyChangedEventArgs)args).PropertyName;
			if (property.equals("IsChangable")) {
				textItem.setDisabled(!((Model)sender).getIsChangable());
			}
			else if (property.equals("IsAvailable")) {
				if (!entityModel.getIsAvailable()) {
					hide();
				}
				else {
					show();
				}
			}
			else if (property.equals("IsValid")) {
				if (!entityModel.getIsValid()) {
					setTextBoxStyle("textBoxInvalid");
					if (entityModel.getInvalidityReasons() != null && entityModel.getInvalidityReasons().size()>0) {
						setTooltip("<nobr>" + entityModel.getInvalidityReasons().iterator().next() + "</nobr>");
						setHoverStyle("gridToolTipStyle");
						setHoverWidth(1);
						setHoverDelay(100);
					}
				}
				else {
					setTextBoxStyle("engineTextItem");
					setTooltip("");
				}	
			}
		}
	}
	
	
	public Model getModel() {
		return entityModel;
	}
}