package org.ovirt.engine.ui.common.widget;

/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * A widget that allows the user to select a value within a range of possible
 * values using a sliding bar that responds to mouse events.
 *
 * <h3>Keyboard Events</h3>
 * <p>
 * SliderBar listens for the following key events. Holding down a key will
 * repeat the action until the key is released.
 * <ul class='css'>
 * <li>left arrow - shift left one step</li>
 * <li>right arrow - shift right one step</li>
 * <li>ctrl+left arrow - jump left 10% of the distance</li>
 * <li>ctrl+right arrow - jump right 10% of the distance</li>
 * <li>home - jump to min value</li>
 * <li>end - jump to max value</li>
 * <li>space - jump to middle value</li>
 * </ul>
 * </p>
 *
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-SliderBar-shell { primary style }</li>
 * <li>.gwt-SliderBar-shell-focused { primary style when focused }</li>
 * <li>.gwt-SliderBar-shell gwt-SliderBar-line { the line that the knob moves
 * along }</li>
 * <li>.gwt-SliderBar-shell gwt-SliderBar-line-sliding { the line that the knob
 * moves along when sliding }</li>
 * <li>.gwt-SliderBar-shell .gwt-SliderBar-knob { the sliding knob }</li>
 * <li>.gwt-SliderBar-shell .gwt-SliderBar-knob-sliding { the sliding knob when
 * sliding }</li>
 * <li>
 * .gwt-SliderBar-shell .gwt-SliderBar-tick { the ticks along the line }</li>
 * <li>.gwt-SliderBar-shell .gwt-SliderBar-label { the text labels along the
 * line }</li>
 * </ul>
 */

public class SliderBar extends FocusPanel implements RequiresResize,
        HasValue<Integer>, HasValueChangeHandlers<Integer> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    /**
     * A formatter used to format the labels displayed in the widget.
     */
    public static interface LabelFormatter {
        /**
         * Generate the text to display in each label based on the label's
         * value.
         *
         * Override this method to change the text displayed within the
         * SliderBar.
         *
         * @param slider
         *            the Slider bar
         * @param value
         *            the value the label displays
         * @return the text to display for the label
         */
        String formatLabel(SliderBar slider, double value);
    }

    /**
     * A {@link ClientBundle} that provides images for {@link SliderBar}.
     */
    /*
     * public static interface SliderBarImages extends ClientBundle { public static final SliderBarImages INSTANCE = GWT
     * .create(SliderBarresources.class);
     *
     * @Source("images/slider.gif") ImageResource slider();
     *
     * @Source("images/sliderDisabled.gif") ImageResource sliderDisabled();
     *
     * @Source("images/sliderSliding.gif") ImageResource sliderSliding();
     *
     * @NotStrict
     *
     * @Source("css/SliderBar.css") CssResource sliderBarCss(); }
     */

    /**
     * The timer used to continue to shift the knob as the user holds down one
     * of the left/right arrow keys. Only IE auto-repeats, so we just keep
     * catching the events.
     */
    private class KeyTimer extends Timer {
        /**
         * A bit indicating that this is the first run.
         */
        private boolean firstRun = true;

        /**
         * The delay between shifts, which shortens as the user holds down the
         * button.
         */
        private static final int repeatDelay = 30;

        /**
         * A bit indicating whether we are shifting to a higher or lower value.
         */
        private boolean shiftRight = false;

        /**
         * The number of steps to shift with each press.
         */
        private int multiplier = 1;

        /**
         * This method will be called when a timer fires. Override it to
         * implement the timer's logic.
         */
        @Override
        public void run() {
            // Highlight the knob on first run
            if (firstRun) {
                firstRun = false;
                startSliding(true, false);
            }

            // Slide the slider bar
            if (shiftRight) {
                setCurrentValue(curValue + multiplier * stepSize);
            } else {
                setCurrentValue(curValue - multiplier * stepSize);
            }

            // Repeat this timer until cancelled by keyup event
            schedule(repeatDelay);
        }

        /**
         * Schedules a timer to elapse in the future.
         *
         * @param delayMillis
         *            how long to wait before the timer elapses, in milliseconds
         * @param shiftRight
         *            whether to shift up or not
         * @param multiplier
         *            the number of steps to shift
         */
        public void schedule(int delayMillis, boolean shiftRight, int multiplier) {
            firstRun = true;
            this.shiftRight = shiftRight;
            this.multiplier = multiplier;
            super.schedule(delayMillis);
        }
    }

    /**
     * The current value.
     */
    private Double curValue = 0.0;

    /**
     * The knob that slides across the line.
     */
    private final Image knobImage = new Image();

    /**
     * The timer used to continue to shift the knob if the user holds down a
     * key.
     */
    private final KeyTimer keyTimer = new KeyTimer();

    /**
     * The elements used to display labels above the ticks.
     */
    private final List<Element> labelElements = new ArrayList<>();

    /**
     * The formatter used to generate label text.
     */
    private LabelFormatter labelFormatter;

    /**
     * The line that the knob moves over.
     */
    private final Element lineElement;

    /**
     * The offset between the edge of the shell and the line.
     */
    private int lineLeftOffset = 0;

    /**
     * The maximum slider value.
     */
    private double maxValue;

    /**
     * The minimum slider value.
     */
    private double minValue;

    /**
     * The number of labels to show.
     */
    private int numLabels = 0;

    /**
     * The number of tick marks to show.
     */
    private int numTicks = 0;

    /**
     * A bit indicating whether or not we are currently sliding the slider bar
     * due to keyboard events.
     */
    private boolean slidingKeyboard = false;

    /**
     * A bit indicating whether or not we are currently sliding the slider bar
     * due to mouse events.
     */
    private boolean slidingMouse = false;

    /**
     * A bit indicating whether or not the slider is enabled
     */
    private boolean enabled = true;

    /**
     * The images used with the sliding bar.
     */
    // private SliderBarImages images;

    /**
     * The size of the increments between knob positions.
     */
    private double stepSize;

    /**
     * The elements used to display tick marks, which are the vertical lines
     * along the slider bar.
     */
    private final List<Element> tickElements = new ArrayList<>();

    /**
     * Create a slider bar.
     *
     * @param minValue
     *            the minimum value in the range
     * @param maxValue
     *            the maximum value in the range
     */
    public SliderBar(double minValue, double maxValue) {
        this(minValue, maxValue, null);
    }

    /**
     * Create a slider bar.
     *
     * @param minValue
     *            the minimum value in the range
     * @param maxValue
     *            the maximum value in the range
     * @param labelFormatter
     *            the label formatter
     * @param images
     *            the images to use for the slider
     */
    public SliderBar(double minValue, double maxValue, LabelFormatter labelFormatter) {
        super();
        resources.sliderBarCss().ensureInjected();
        this.minValue = minValue;
        this.maxValue = maxValue;
        setLabelFormatter(labelFormatter);

        // Create the outer shell
        DOM.setStyleAttribute(getElement(), "position", "relative"); //$NON-NLS-1$ //$NON-NLS-2$
        setStyleName("gwt-SliderBar-shell"); //$NON-NLS-1$

        // Create the line
        lineElement = DOM.createDiv();
        DOM.appendChild(getElement(), lineElement);
        DOM.setStyleAttribute(lineElement, "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        DOM.setElementProperty(lineElement, "className", "gwt-SliderBar-line"); //$NON-NLS-1$ //$NON-NLS-2$

        // Create the knob
        knobImage.setResource(resources.slider());
        Element knobElement = knobImage.getElement();
        DOM.appendChild(getElement(), knobElement);
        DOM.setStyleAttribute(knobElement, "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        DOM.setElementProperty(knobElement, "className", "gwt-SliderBar-knob"); //$NON-NLS-1$ //$NON-NLS-2$

        sinkEvents(Event.MOUSEEVENTS | Event.KEYEVENTS | Event.FOCUSEVENTS);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Integer> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Return the current value.
     *
     * @return the current value
     */
    public double getCurrentValue() {
        return curValue;
    }

    /**
     * Return the label formatter.
     *
     * @return the label formatter
     */
    public LabelFormatter getLabelFormatter() {
        return labelFormatter;
    }

    /**
     * Return the max value.
     *
     * @return the max value
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Return the minimum value.
     *
     * @return the minimum value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Return the number of labels.
     *
     * @return the number of labels
     */
    public int getNumLabels() {
        return numLabels;
    }

    /**
     * Return the number of ticks.
     *
     * @return the number of ticks
     */
    public int getNumTicks() {
        return numTicks;
    }

    /**
     * Return the step size.
     *
     * @return the step size
     */
    public double getStepSize() {
        return stepSize;
    }

    /**
     * Return the total range between the minimum and maximum values.
     *
     * @return the total range
     */
    public double getTotalRange() {
        if (minValue > maxValue) {
            return 0;
        } else {
            return maxValue - minValue;
        }
    }

    @Override
    public Integer getValue() {
        return curValue.intValue();
    }

    /**
     * @return Gets whether this widget is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Listen for events that will move the knob.
     *
     * @param event
     *            the event that occurred
     */
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (enabled) {
            switch (DOM.eventGetType(event)) {
            // Unhighlight and cancel keyboard events
            case Event.ONBLUR:
                keyTimer.cancel();
                if (slidingMouse) {
                    DOM.releaseCapture(getElement());
                    slidingMouse = false;
                    slideKnob(event);
                    stopSliding(true, true);
                } else if (slidingKeyboard) {
                    slidingKeyboard = false;
                    stopSliding(true, true);
                }
                unhighlight();
                break;

            // Highlight on focus
            case Event.ONFOCUS:
                highlight();
                break;

            // Mousewheel events
            case Event.ONMOUSEWHEEL:
                int velocityY = DOM.eventGetMouseWheelVelocityY(event);
                DOM.eventPreventDefault(event);
                if (velocityY > 0) {
                    shiftRight(1);
                } else {
                    shiftLeft(1);
                }
                break;

            // Shift left or right on key press
            case Event.ONKEYDOWN:
                if (!slidingKeyboard) {
                    int multiplier = 1;
                    if (DOM.eventGetCtrlKey(event)) {
                        multiplier = (int) (getTotalRange() / stepSize / 10);
                    }

                    switch (DOM.eventGetKeyCode(event)) {
                    case KeyCodes.KEY_HOME:
                        DOM.eventPreventDefault(event);
                        setCurrentValue(minValue);
                        break;
                    case KeyCodes.KEY_END:
                        DOM.eventPreventDefault(event);
                        setCurrentValue(maxValue);
                        break;
                    case KeyCodes.KEY_LEFT:
                        DOM.eventPreventDefault(event);
                        slidingKeyboard = true;
                        startSliding(false, true);
                        shiftLeft(multiplier);
                        keyTimer.schedule(400, false, multiplier);
                        break;
                    case KeyCodes.KEY_RIGHT:
                        DOM.eventPreventDefault(event);
                        slidingKeyboard = true;
                        startSliding(false, true);
                        shiftRight(multiplier);
                        keyTimer.schedule(400, true, multiplier);
                        break;
                    case 32:
                        DOM.eventPreventDefault(event);
                        setCurrentValue(minValue + getTotalRange() / 2);
                        break;
                    }
                }
                break;
            // Stop shifting on key up
            case Event.ONKEYUP:
                keyTimer.cancel();
                if (slidingKeyboard) {
                    slidingKeyboard = false;
                    stopSliding(true, true);
                }
                break;

            // Mouse Events
            case Event.ONMOUSEDOWN:
                setFocus(true);
                slidingMouse = true;
                DOM.setCapture(getElement());
                startSliding(true, true);
                DOM.eventPreventDefault(event);
                slideKnob(event);
                break;
            case Event.ONMOUSEUP:
                if (slidingMouse) {
                    DOM.releaseCapture(getElement());
                    slidingMouse = false;
                    slideKnob(event);
                    stopSliding(true, true);
                }
                break;
            case Event.ONMOUSEMOVE:
                if (slidingMouse) {
                    slideKnob(event);
                }
                break;
            }
        }
    }

    /**
     * This method is called when the dimensions of the parent element change.
     * Subclasses should override this method as needed.
     *
     * @param width
     *            the new client width of the element
     * @param height
     *            the new client height of the element
     */
    public void onResize(int width, int height) {
        // Center the line in the shell
        int lineWidth = lineElement.getOffsetWidth();
        lineLeftOffset = (width / 2) - (lineWidth / 2);
        DOM.setStyleAttribute(lineElement, "left", lineLeftOffset + "px"); //$NON-NLS-1$ //$NON-NLS-2$

        // Draw the other components
        drawLabels();
        drawTicks();
        drawKnob();
    }

    /**
     * Redraw the progress bar when something changes the layout.
     */
    public void redraw() {
        if (isAttached()) {
            int width = getElement().getClientWidth();
            int height = getElement().getClientHeight();
            onResize(width, height);
        }
    }

    /**
     * Set the current value and fire the onValueChange event.
     *
     * @param curValue
     *            the current value
     */
    public void setCurrentValue(double curValue) {
        setCurrentValue(curValue, true);
    }

    /**
     * Set the current value and optionally fire the onValueChange event.
     *
     * @param curValue
     *            the current value
     * @param fireEvent
     *            fire the onValue change event if true
     */
    public void setCurrentValue(double curValue, boolean fireEvent) {
        // Confine the value to the range
        this.curValue = Math.max(minValue, Math.min(maxValue, curValue));
        double remainder = (this.curValue - minValue) % stepSize;
        this.curValue -= remainder;

        // Go to next step if more than halfway there
        if ((remainder > (stepSize / 2))
                && ((this.curValue + stepSize) <= maxValue)) {
            this.curValue += stepSize;
        }

        // Redraw the knob
        drawKnob();

        // Fire the ValueChangeEvent
        if (fireEvent) {
            ValueChangeEvent.fire(this, this.curValue.intValue());
        }
    }

    /**
     * Sets whether this widget is enabled.
     *
     * @param enabled
     *            true to enable the widget, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            knobImage.setResource(resources.slider());
            DOM.setElementProperty(lineElement, "className", //$NON-NLS-1$
                    "gwt-SliderBar-line"); //$NON-NLS-1$
        } else {
            knobImage.setResource(resources.sliderDisabled());
            DOM.setElementProperty(lineElement, "className", //$NON-NLS-1$
                    "gwt-SliderBar-line gwt-SliderBar-line-disabled"); //$NON-NLS-1$
        }
        redraw();
    }

    /**
     * Set the label formatter.
     *
     * @param labelFormatter
     *            the label formatter
     */
    public void setLabelFormatter(LabelFormatter labelFormatter) {
        this.labelFormatter = labelFormatter;
    }

    /**
     * Set the max value.
     *
     * @param maxValue
     *            the current value
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        drawLabels();
        resetCurrentValue();
    }

    /**
     * Set the minimum value.
     *
     * @param minValue
     *            the current value
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
        drawLabels();
        resetCurrentValue();
    }

    /**
     * Set the number of labels to show on the line. Labels indicate the value
     * of the slider at that point. Use this method to enable labels.
     *
     * If you set the number of labels equal to the total range divided by the
     * step size, you will get a properly aligned "jumping" effect where the
     * knob jumps between labels.
     *
     * Note that the number of labels displayed will be one more than the number
     * you specify, so specify 1 labels to show labels on either end of the
     * line. In other words, numLabels is really the number of slots between the
     * labels.
     *
     * setNumLabels(0) will disable labels.
     *
     * @param numLabels
     *            the number of labels to show
     */
    public void setNumLabels(int numLabels) {
        this.numLabels = numLabels;
        drawLabels();
    }

    /**
     * Set the number of ticks to show on the line. A tick is a vertical line
     * that represents a division of the overall line. Use this method to enable
     * ticks.
     *
     * If you set the number of ticks equal to the total range divided by the
     * step size, you will get a properly aligned "jumping" effect where the
     * knob jumps between ticks.
     *
     * Note that the number of ticks displayed will be one more than the number
     * you specify, so specify 1 tick to show ticks on either end of the line.
     * In other words, numTicks is really the number of slots between the ticks.
     *
     * setNumTicks(0) will disable ticks.
     *
     * @param numTicks
     *            the number of ticks to show
     */
    public void setNumTicks(int numTicks) {
        this.numTicks = numTicks;
        drawTicks();
    }

    /**
     * Set the step size.
     *
     * @param stepSize
     *            the current value
     */
    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
        resetCurrentValue();
    }

    @Override
    public void setValue(Integer value) {
        setCurrentValue(value, false);
    }

    @Override
    public void setValue(Integer value, boolean fireEvent) {
        setCurrentValue(value, fireEvent);
    }

    /**
     * Shift to the left (smaller value).
     *
     * @param numSteps
     *            the number of steps to shift
     */
    public void shiftLeft(int numSteps) {
        setCurrentValue(getCurrentValue() - numSteps * stepSize);
    }

    /**
     * Shift to the right (greater value).
     *
     * @param numSteps
     *            the number of steps to shift
     */
    public void shiftRight(int numSteps) {
        setCurrentValue(getCurrentValue() + numSteps * stepSize);
    }

    /**
     * Format the label to display above the ticks
     *
     * Override this method in a subclass to customize the format. By default,
     * this method returns the integer portion of the value.
     *
     * @param value
     *            the value at the label
     * @return the text to put in the label
     */
    protected String formatLabel(double value) {
        if (labelFormatter != null) {
            return labelFormatter.formatLabel(this, value);
        } else {
            return (int) (10 * value) / 10.0 + ""; //$NON-NLS-1$
        }
    }

    /**
     * Get the percentage of the knob's position relative to the size of the
     * line. The return value will be between 0.0 and 1.0.
     *
     * @return the current percent complete
     */
    protected double getKnobPercent() {
        // If we have no range
        if (maxValue <= minValue) {
            return 0;
        }

        // Calculate the relative progress
        double percent = (curValue - minValue) / (maxValue - minValue);
        return Math.max(0.0, Math.min(1.0, percent));
    }

    /**
     * This method is called immediately after a widget becomes attached to the
     * browser's document.
     */
    @Override
    protected void onLoad() {
        // Reset the position attribute of the parent element
        DOM.setStyleAttribute(getElement(), "position", "relative"); //$NON-NLS-1$ //$NON-NLS-2$
        // workaround to render properly when parent Widget does not
        // implement ProvidesResize since DOM doesn't provide element
        // height and width until onModuleLoad() finishes.
        Scheduler.get().scheduleDeferred(() -> onResize());
    }

    /**
     * Draw the knob where it is supposed to be relative to the line.
     */
    private void drawKnob() {
        // Abort if not attached
        if (!isAttached()) {
            return;
        }

        // Move the knob to the correct position
        Element knobElement = knobImage.getElement();
        int lineWidth = lineElement.getOffsetWidth();
        int knobWidth = knobElement.getOffsetWidth();
        int knobLeftOffset = lineLeftOffset
                + ((int) getKnobPercent() * lineWidth) - (knobWidth / 2);
        knobLeftOffset = Math.min(knobLeftOffset, lineLeftOffset + lineWidth
                - (knobWidth / 2) - 1);
        DOM.setStyleAttribute(knobElement, "left", knobLeftOffset + "px"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Draw the labels along the line.
     */
    private void drawLabels() {
        // Abort if not attached
        if (!isAttached()) {
            return;
        }

        // Draw the labels
        int lineWidth = lineElement.getOffsetWidth();
        if (numLabels > 0) {
            // Create the labels or make them visible
            for (int i = 0; i <= numLabels; i++) {
                Element label = null;
                if (i < labelElements.size()) {
                    label = labelElements.get(i);
                } else { // Create the new label
                    label = DOM.createDiv();
                    DOM.setStyleAttribute(label, "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
                    DOM.setStyleAttribute(label, "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (enabled) {
                        DOM.setElementProperty(label, "className", //$NON-NLS-1$
                                "gwt-SliderBar-label"); //$NON-NLS-1$
                    } else {
                        DOM.setElementProperty(label, "className", //$NON-NLS-1$
                                "gwt-SliderBar-label-disabled"); //$NON-NLS-1$
                    }
                    DOM.appendChild(getElement(), label);
                    labelElements.add(label);
                }

                // Set the label text
                double value = minValue + (getTotalRange() * i / numLabels);
                DOM.setStyleAttribute(label, "visibility", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
                DOM.setStyleAttribute(label, "display", ""); //$NON-NLS-1$ //$NON-NLS-2$
                DOM.setElementProperty(label, "innerHTML", formatLabel(value)); //$NON-NLS-1$

                // Move to the left so the label width is not clipped by the
                // shell
                DOM.setStyleAttribute(label, "left", "0px"); //$NON-NLS-1$ //$NON-NLS-2$

                // Position the label and make it visible
                int labelWidth = label.getOffsetWidth();
                int labelLeftOffset = lineLeftOffset
                        + (lineWidth * i / numLabels) - (labelWidth / 2);
                labelLeftOffset = Math.min(labelLeftOffset, lineLeftOffset
                        + lineWidth - labelWidth);
                labelLeftOffset = Math.max(labelLeftOffset, lineLeftOffset);
                DOM.setStyleAttribute(label, "left", labelLeftOffset + "px"); //$NON-NLS-1$ //$NON-NLS-2$
                DOM.setStyleAttribute(label, "visibility", "visible"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // Hide unused labels
            for (int i = numLabels + 1; i < labelElements.size(); i++) {
                DOM.setStyleAttribute(labelElements.get(i), "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else { // Hide all labels
            for (Element elem : labelElements) {
                DOM.setStyleAttribute(elem, "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * Draw the tick along the line.
     */
    private void drawTicks() {
        // Abort if not attached
        if (!isAttached()) {
            return;
        }

        // Draw the ticks
        int lineWidth = lineElement.getOffsetWidth();
        if (numTicks > 0) {
            // Create the ticks or make them visible
            for (int i = 0; i <= numTicks; i++) {
                Element tick = null;
                if (i < tickElements.size()) {
                    tick = tickElements.get(i);
                } else { // Create the new tick
                    tick = DOM.createDiv();
                    DOM.setStyleAttribute(tick, "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
                    DOM.setStyleAttribute(tick, "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
                    DOM.appendChild(getElement(), tick);
                    tickElements.add(tick);
                }
                if (enabled) {
                    DOM.setElementProperty(tick, "className", //$NON-NLS-1$
                            "gwt-SliderBar-tick"); //$NON-NLS-1$
                } else {
                    DOM.setElementProperty(tick, "className", //$NON-NLS-1$
                            "gwt-SliderBar-tick gwt-SliderBar-tick-disabled"); //$NON-NLS-1$
                }
                // Position the tick and make it visible
                DOM.setStyleAttribute(tick, "visibility", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
                DOM.setStyleAttribute(tick, "display", ""); //$NON-NLS-1$ //$NON-NLS-2$
                int tickWidth = tick.getOffsetWidth();
                int tickLeftOffset = lineLeftOffset
                        + (lineWidth * i / numTicks) - (tickWidth / 2);
                tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset
                        + lineWidth - tickWidth);
                DOM.setStyleAttribute(tick, "left", tickLeftOffset + "px"); //$NON-NLS-1$ //$NON-NLS-2$
                DOM.setStyleAttribute(tick, "visibility", "visible"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // Hide unused ticks
            for (int i = numTicks + 1; i < tickElements.size(); i++) {
                DOM.setStyleAttribute(tickElements.get(i), "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else { // Hide all ticks
            for (Element elem : tickElements) {
                DOM.setStyleAttribute(elem, "display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * Highlight this widget.
     */
    private void highlight() {
        String styleName = getStylePrimaryName();
        DOM.setElementProperty(getElement(), "className", styleName + " " //$NON-NLS-1$ //$NON-NLS-2$
                + styleName + "-focused"); //$NON-NLS-1$
    }

    /**
     * Reset the progress to constrain the progress to the current range and
     * redraw the knob as needed.
     */
    private void resetCurrentValue() {
        setCurrentValue(getCurrentValue());
    }

    /**
     * Slide the knob to a new location.
     *
     * @param event
     *            the mouse event
     */
    private void slideKnob(Event event) {
        int x = DOM.eventGetClientX(event);
        if (x > 0) {
            int lineWidth = lineElement.getOffsetWidth();
            int lineLeft = lineElement.getAbsoluteLeft();
            double percent = (double) (x - lineLeft) / lineWidth * 1.0;
            setCurrentValue(getTotalRange() * percent + minValue, true);
        }
    }

    /**
     * Start sliding the knob.
     *
     * @param highlight
     *            true to change the style
     * @param fireEvent
     *            true to fire the event
     */
    private void startSliding(boolean highlight, boolean fireEvent) {
        if (highlight) {
            DOM.setElementProperty(lineElement, "className", //$NON-NLS-1$
                    "gwt-SliderBar-line gwt-SliderBar-line-sliding"); //$NON-NLS-1$
            DOM.setElementProperty(knobImage.getElement(), "className", //$NON-NLS-1$
                    "gwt-SliderBar-knob gwt-SliderBar-knob-sliding"); //$NON-NLS-1$
            knobImage.setResource(resources.sliderSliding());
        }
    }

    /**
     * Stop sliding the knob.
     *
     * @param unhighlight
     *            true to change the style
     * @param fireEvent
     *            true to fire the event
     */
    private void stopSliding(boolean unhighlight, boolean fireEvent) {
        if (unhighlight) {
            DOM.setElementProperty(lineElement, "className", //$NON-NLS-1$
                    "gwt-SliderBar-line"); //$NON-NLS-1$

            DOM.setElementProperty(knobImage.getElement(), "className", //$NON-NLS-1$
                    "gwt-SliderBar-knob"); //$NON-NLS-1$
            knobImage.setResource(resources.slider());
        }
    }

    /**
     * Unhighlight this widget.
     */
    private void unhighlight() {
        DOM.setElementProperty(getElement(), "className", getStylePrimaryName()); //$NON-NLS-1$
    }

    @Override
    public void onResize() {
        redraw();
    }

}
