package org.ovirt.engine.ui.webadmin.widget.tags;

import java.util.Arrays;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModelType;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TagItemCell extends CompositeCell<TagModel> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    private static class TagItemTextCell extends Column<TagModel, SafeHtml> {

        private TagItemTextCell() {
            super(new AbstractCell<SafeHtml>(BrowserEvents.CLICK, BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT) {
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
                    if (BrowserEvents.MOUSEOVER.equals(event.getType()) && tagModel.getType() != TagModelType.Root) {
                        updateTagElement(true, tagItemElement, tagButtonElement);
                    }
                    else if (BrowserEvents.MOUSEOUT.equals(event.getType()) && !tagModel.getSelection()) {
                        updateTagElement(false, tagItemElement, tagButtonElement);
                    }
                }
            });
        }

        @Override
        public SafeHtml getValue(TagModel object) {
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            String tag = AbstractImagePrototype.create(resources.tagImage()).getHTML();
            String tagName = object.getName().getEntity().toString();

            // Build tag and update style by model's state
            SafeHtml tagImage = SafeHtmlUtils.fromTrustedString(tag);
            String bgColor = object.getSelection() ? TAG_BG_COLOR_SELECTED : TAG_BG_COLOR_UNSELECTED;
            String borderColor = object.getSelection() ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED;

            sb.append(templates.tagItem(tagImage, tagName, bgColor, borderColor,
                    ElementIdUtils.createTreeCellElementId(elementIdPrefix + "_label", object, null))); //$NON-NLS-1$

            return sb.toSafeHtml();
        }

    }

    private static class TagItemButtonCell extends Column<TagModel, Boolean> {

        private TagItemButtonCell() {
            super(new AbstractCell<Boolean>(BrowserEvents.CLICK, BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT) {
                @Override
                public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                    String tagPin = AbstractImagePrototype.create(resources.tagPinImage()).getHTML();
                    String tagPinGreen = AbstractImagePrototype.create(resources.tagPinGreenImage()).getHTML();

                    // Build tag's button and update style by model's state
                    SafeHtml tagImage = SafeHtmlUtils.fromTrustedString(value ? tagPinGreen : tagPin);
                    String bgColor = value ? TAG_BG_COLOR_SELECTED : TAG_BG_COLOR_UNSELECTED;
                    String borderColor = value ? TAG_INNER_BORDER_COLOR : TAG_BORDER_UNSELECTED;
                    String visibility = value ? Visibility.VISIBLE.toString() : Visibility.HIDDEN.toString();

                    TagModel tagModel = (TagModel) context.getKey();

                    sb.append(templates.tagButton(tagImage, bgColor, borderColor, visibility,
                            ElementIdUtils.createTreeCellElementId(elementIdPrefix + "_pinButton", tagModel, null))); //$NON-NLS-1$
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
                    if (BrowserEvents.CLICK.equals(event.getType())) {
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
                    else if (BrowserEvents.MOUSEOVER.equals(event.getType()) && tagModel.getType() != TagModelType.Root) {
                        updateTagElement(true, tagItem, tagButton);
                    }
                    else if (BrowserEvents.MOUSEOUT.equals(event.getType()) && !tagModel.getSelection()) {
                        updateTagElement(false, tagItem, tagButton);
                    }
                }
            });
        }

        @Override
        public Boolean getValue(TagModel object) {
            return object.getSelection();
        }

    }

    private static final String TAG_BG_COLOR_SELECTED = "#A8EDA8"; //$NON-NLS-1$
    private static final String TAG_BG_COLOR_UNSELECTED = "transparent"; //$NON-NLS-1$
    private static final String TAG_BORDER_UNSELECTED = "transparent"; //$NON-NLS-1$
    private static final String TAG_INNER_BORDER_COLOR = "#A9A9A7"; //$NON-NLS-1$

    // This field is intentionally static since it's used by static inner classes
    private static String elementIdPrefix = DOM.createUniqueId();

    @SuppressWarnings("unchecked")
    public TagItemCell() {
        super(Arrays.<HasCell<TagModel, ?>> asList(
                new TagItemTextCell(),
                new TagItemButtonCell()));
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        TagItemCell.elementIdPrefix = elementIdPrefix;
    }

}
