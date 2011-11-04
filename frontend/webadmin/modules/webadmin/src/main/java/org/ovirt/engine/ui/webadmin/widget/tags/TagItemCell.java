package org.ovirt.engine.ui.webadmin.widget.tags;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModelType;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TagItemCell extends CompositeCell<TagModel> {

    private static String TAG_BG_COLOR_SELECTED = "#A8EDA8";
    private static String TAG_BG_COLOR_UNSELECTED = "transparent";
    private static String TAG_BORDER_UNSELECTED = "transparent";
    private static String TAG_INNER_BORDER_COLOR = "#A9A9A7";

    @SuppressWarnings("unchecked")
    public TagItemCell(ApplicationResources resources, ApplicationTemplates templates) {
        super(new ArrayList<HasCell<TagModel, ?>>(
                Arrays.asList(getTagPanel(resources, templates), getTagButton(resources, templates))));
    }

    public static Column<TagModel, SafeHtml> getTagPanel(final ApplicationResources resources,
            final ApplicationTemplates templates) {
        return new Column<TagModel, SafeHtml>(
                new AbstractCell<SafeHtml>("click", "mouseover", "mouseout") {
                    @Override
                    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                        TagModel tagModel = (TagModel) context.getKey();
                        tagModel.setIsAvailable(false);

                        if (value != null) {
                            sb.append(value);
                        }
                    }

                    private void updateTagElement(boolean showOver, Element tagItem, Element tagBtn) {
                        tagItem.getStyle().setBorderColor(showOver ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED);
                        tagBtn.getStyle().setVisibility(showOver ? Visibility.VISIBLE : Visibility.HIDDEN);
                    }

                    @Override
                    public void onBrowserEvent(Context context, Element parent, SafeHtml value,
                            NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {

                        TagModel tagModel = (TagModel) context.getKey();
                        Element tagItemElement = parent.getFirstChildElement();
                        Element tagButtonElement = parent.getNextSiblingElement().getFirstChildElement();

                        // Update tag item and button on mouse over and mouse out
                        if (event.getType().equals("mouseover") && tagModel.getType() != TagModelType.Root) {
                            updateTagElement(true, tagItemElement, tagButtonElement);
                        }
                        else if (event.getType().equals("mouseout") && !tagModel.getSelection()) {
                            updateTagElement(false, tagItemElement, tagButtonElement);
                        }
                    }
                }) {

            @Override
            public SafeHtml getValue(TagModel object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                String tag = AbstractImagePrototype.create(resources.tagImage()).getHTML();
                String tagName = object.getName().getEntity().toString();

                // Build tag and update style by model's state
                SafeHtml tagImage = SafeHtmlUtils.fromTrustedString(tag);
                String bgColor = object.getSelection() ? TAG_BG_COLOR_SELECTED : TAG_BG_COLOR_UNSELECTED;
                String borderColor = object.getSelection() ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED;

                sb.append(templates.tagItem(tagImage, tagName, bgColor, borderColor));

                return sb.toSafeHtml();
            }
        };
    }

    public static Column<TagModel, Boolean> getTagButton(final ApplicationResources resources,
            final ApplicationTemplates templates) {
        return new Column<TagModel, Boolean>(
                new AbstractCell<Boolean>("click", "mouseover", "mouseout") {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        String tagPin = AbstractImagePrototype.create(resources.tagPinImage()).getHTML();
                        String tagPinGreen = AbstractImagePrototype.create(resources.tagPinGreenImage()).getHTML();

                        // Build tag's button and update style by model's state
                        SafeHtml tagImage = SafeHtmlUtils.fromTrustedString(value ? tagPinGreen : tagPin);
                        String bgColor = value ? TAG_BG_COLOR_SELECTED : TAG_BG_COLOR_UNSELECTED;
                        String borderColor = value ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED;
                        String visibility = value ? Visibility.VISIBLE.toString() : Visibility.HIDDEN.toString();

                        sb.append(templates.tagButton(tagImage, bgColor, borderColor, visibility));
                    }

                    private void updateTagElement(boolean isSelected, Element tagItem, Element tagBtn) {
                        tagItem.getStyle().setBorderColor(isSelected ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED);
                        tagBtn.getStyle().setBorderColor(isSelected ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED);
                        tagBtn.getStyle().setVisibility(isSelected ? Visibility.VISIBLE : Visibility.HIDDEN);
                    }

                    @Override
                    public void onBrowserEvent(Context context, Element parent, Boolean value,
                            NativeEvent event, ValueUpdater<Boolean> valueUpdater) {

                        TagModel tagModel = (TagModel) context.getKey();
                        Element tagButton = parent.getFirstChildElement();
                        Element tagItem = parent.getParentElement().getFirstChildElement().getFirstChildElement();

                        boolean isSelected = !value;

                        // Update tag item and button on click, mouse over and mouse out
                        if (event.getType().equals("click")) {
                            if (tagModel.getType() != TagModelType.Root) {
                                // Update model selection
                                tagModel.setSelection(isSelected);

                                // Update button
                                setValue(context, parent, isSelected);

                                // Update style
                                tagItem.getStyle().setBackgroundColor(
                                        isSelected ? TAG_BG_COLOR_SELECTED : TAG_BG_COLOR_UNSELECTED);
                            }
                        }
                        else if (event.getType().equals("mouseover") && tagModel.getType() != TagModelType.Root) {
                            updateTagElement(true, tagItem, tagButton);
                        }
                        else if (event.getType().equals("mouseout") && !tagModel.getSelection()) {
                            updateTagElement(false, tagItem, tagButton);
                        }
                    }
                }) {

            @Override
            public Boolean getValue(TagModel object) {
                return object.getSelection();
            }
        };
    }

}
