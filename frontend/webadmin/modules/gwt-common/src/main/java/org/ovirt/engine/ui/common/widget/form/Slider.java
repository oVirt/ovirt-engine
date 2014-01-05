package org.ovirt.engine.ui.common.widget.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class Slider extends Composite {

    private static final int SPLITER_WIDTH = 8;

    interface WidgetUiBinder extends UiBinder<Widget, Slider> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public interface SliderValueChange {
        public void onSliderValueChange(String name, int value);
    }

    SliderValueChange sliderValueChange;

    private String name;

    public SliderValueChange getSliderValueChange() {
        return sliderValueChange;
    }

    public void setSliderValueChange(String name, SliderValueChange sliderValueChange) {
        this.sliderValueChange = sliderValueChange;
        this.name = name;
    }

    @UiField(provided = true)
    SplitLayoutPanel slider;

    @UiField(provided = true)
    Label percentLabel;

    SimplePanel centerPanel;

    SimplePanel westPanel;

    int pivot;
    int min;
    int max;
    int scale;

    private String color;

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getPivot() {
        return pivot;
    }

    public void setPivot(int pivot) {
        this.pivot = pivot;
        if (percentLabel != null) {
            percentLabel.setText(getPercent() + "%"); //$NON-NLS-1$
        }
    }

    public void setValue(int value) {

        if (value < 0) {
            return;
        }

        setPivot(value * getScale());
        setSlidersWidth();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getPercent() {
        return getPivot() / getScale();
    }

    public Slider(int scale, int min, int max, int pivot, String color) {
        setColor(color);
        setScale(scale);
        setMin(min * getScale());
        setMax(max * getScale());
        setPivot(pivot * getScale());
        initSlides();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        slider.setWidth(getMax() - getMin() + "px"); //$NON-NLS-1$
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private void initSlides() {
        slider = new SplitLayoutPanel() {

            @Override
            public void onResize() {
                super.onResize();
                setSliderWidth();
            }

            public void setSliderWidth() {
                double d = ((DockLayoutPanel.LayoutData) westPanel.getLayoutData()).size;

                setPivot((int) d + getMin());
                if (getPercent() == getMax() / getScale() - SPLITER_WIDTH / getScale()) {
                    setPivot(getPivot() + SPLITER_WIDTH);
                }

                if (sliderValueChange != null) {
                    sliderValueChange.onSliderValueChange(name, getPercent());
                }
            }
        };

        String style = "A" + getColor().replace('#', '_') + "_clusterPolicy"; //$NON-NLS-1$ //$NON-NLS-2$
        slider.addStyleName(style);
        StyleInjector.inject("." + style //$NON-NLS-1$
                + ".gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-HDragger { width: 8px !important; background: " //$NON-NLS-1$
                + getColor() + "; cursor: pointer; }"); //$NON-NLS-1$
        percentLabel = new Label();
        setSlidersWidth();

    }

    private void setSlidersWidth() {
        slider.clear();
        westPanel = new SimplePanel();

        int width = getPivot() - getMin();
        westPanel.setWidth((width < 0 ? 0 : width) + "px"); //$NON-NLS-1$
        percentLabel.setText(getPercent() + "%"); //$NON-NLS-1$
        centerPanel = new SimplePanel();
        width = getMax() - getMin();
        centerPanel.setWidth((width < 0 ? 0 : width) + "px"); //$NON-NLS-1$
        slider.addWest(westPanel, getPivot() - getMin());
        slider.add(centerPanel);
    }

}
