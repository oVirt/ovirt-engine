package org.ovirt.engine.ui.userportal.client.components;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.RangeEntityModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Slider;
import com.smartgwt.client.widgets.events.ValueChangedEvent;
import com.smartgwt.client.widgets.events.ValueChangedHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CanvasItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyDownEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class SliderItemRangeModelBinded extends CanvasItem {

	private Slider slider;
	private TextItem valueBox;
	private Label minLabel;
	private Label maxLabel;
	private RangeEntityModel model;
	
	private CanvasItem sp = this;
	
	private int min;
	private int max;
	String innerID;
	public SliderItemRangeModelBinded(String title, int width, final RangeEntityModel model, String id) {
		setTitle(title);
		setTitleAlign(Alignment.LEFT);

		this.model = model;
		
		slider = new Slider();
		slider.setVertical(false);
		slider.setWidth(width - 30);
		slider.setShowValue(false);
		slider.setShowTitle(false);
		slider.setShowRange(false);		
		slider.setID(id);
		if (model.getEntity() != null) slider.setValue((Integer)model.getEntity());
		if (model.getInterval() != 0) slider.setNumValues(((Double)((max-min+1)/model.getInterval())).intValue());
		
		minLabel = new Label();
		minLabel.setAutoFit(true);
		minLabel.setLayoutAlign(VerticalAlignment.BOTTOM);

		maxLabel = new Label();
		maxLabel.setAutoFit(true);
		maxLabel.setLayoutAlign(VerticalAlignment.BOTTOM);
		
		if (model.getMax() >= model.getMin())
		{
			setMax(((Double)model.getMax()).intValue());
			setMin(((Double)model.getMin()).intValue());			
		}
		
		valueBox = new TextItem();
		valueBox.setWidth(28);
		valueBox.setShowTitle(false);
		valueBox.setDisabled(true);
		valueBox.setShowDisabled(false);
		valueBox.setValue(((Float)slider.getValue()).intValue());
		// A workaround in order to have a hook for automatic testing, we can't directly set the cell id, so we are setting the 'style' attribute instead
		valueBox.setCellStyle(id + "_valueBox");

		DynamicForm valueBoxForm = new DynamicForm();
		valueBoxForm.setItems(valueBox);
		valueBoxForm.setAutoWidth();
		valueBoxForm.setAutoHeight();

		slider.setLayoutAlign(VerticalAlignment.CENTER);
		HLayout sliderLayout = new HLayout();
		sliderLayout.setAutoWidth();
		sliderLayout.setAutoHeight();
		sliderLayout.addMember(valueBoxForm);
		sliderLayout.addMember(minLabel);
		sliderLayout.addMember(slider);
		sliderLayout.addMember(maxLabel);
				
		addHandlers();
		
		setCanvas(sliderLayout);			
	}

	public void setMin(Integer value) {
		min = value;
		slider.setMinValue(min);
		minLabel.setContents(value.toString());
	}

	public void setMax(Integer value) {
		max = value;
		slider.setMaxValue(max);
		maxLabel.setContents(value.toString());
	}

	public void addHandlers() {
		slider.addValueChangedHandler(new ValueChangedHandler() {
			@Override
			public void onValueChanged(ValueChangedEvent event) {	
				int sliderValue = ((Float)slider.getValue()).intValue();
				if (sliderValue != 0 && model.getIsAllValuesSet())
				{
					model.setEntity(sliderValue);
					valueBox.setValue(sliderValue);
				}
			}
		});

		model.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
								
				if (propertyName.equals("Min")) {
					setMin(((Double)model.getMin()).intValue());
				}
				else if (propertyName.equals("Max")) {
					setMax(((Double)model.getMax()).intValue());
				}
				else if (propertyName.equals("Interval") && model.getInterval() != 0) {
					int interval = ((Double)((max-min)/model.getInterval())).intValue() + 1;
					if (interval == 1) {
						slider.setDisabled(true);
					}
					else {
						if (slider.getDisabled())
							slider.setDisabled(false);
						slider.setNumValues(interval);
					}
				}
				 
				if (model.getEntity() != null)
					slider.setValue((Integer)model.getEntity());				
			}
		});
		
		model.getEntityChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				slider.setValue((Integer)model.getEntity());					
				valueBox.setValue((Integer)model.getEntity());				
			}
		});
	}	
}
