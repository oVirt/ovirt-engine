package org.ovirt.engine.ui.common.widget.editor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.editor.UiCommonEditor;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * An Editor that shows an enum as a Radio Group, with one Radio button for each enum value.<BR>
 * Each enum radio button may be disabled separately.
 *
 * @param <E>
 *            The enum type
 */
public class EnumRadioEditor<E extends Enum<E>> implements EditorWidget<E, LeafValueEditor<E>>, UiCommonEditor<E>, HasValueChangeHandlers<E>, LeafValueEditor<E> {


    public interface EnumRadioCellTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
            /**
             * Applied to enabled rows
             */
            String cellTableEnabledRow();

            /**
             * Applied to disabled rows
             */
            String cellTableDisabledRow();
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/EnumRadioCellTable.css" })
        TableStyle cellTableStyle();
    }

    /**
     * A {@link CellTable} that shows a radio button for each enum value.<BR>
     * Each enum radio button may be disabled separately.
     *
     * @param <E>
     *            The enum type
     */
    public static class EnumRadioCellTable<E extends Enum<E>> extends CellTable<E> implements HasEnabled {

        /**
         * A {@link RadioboxCell} that can be disabled
         */
        private static class ExRadioboxCell<E extends Enum<E>> extends RadioboxCell {
            /**
             * An html string representation of a checked disabled input box.
             */
            private static final SafeHtml INPUT_CHECKED_DISABLED =
                    SafeHtmlUtils.fromSafeConstant("<input type=\"radio\" tabindex=\"-1\" checked disabled/>"); //$NON-NLS-1$

            /**
             * An html string representation of an unchecked disabled input box.
             */
            private static final SafeHtml INPUT_UNCHECKED_DISABLED =
                    SafeHtmlUtils.fromSafeConstant("<input type=\"radio\" tabindex=\"-1\" disabled/>"); //$NON-NLS-1$

            private final Set<E> disabledSet;

            public ExRadioboxCell(boolean dependsOnSelection, boolean handlesSelection, Set<E> disabledSet) {
                super(dependsOnSelection, handlesSelection);
                this.disabledSet = disabledSet;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
                // Get the view data.
                Object key = context.getKey();
                E keyValue = (E) key;
                if (disabledSet.contains(keyValue)) {
                    // disabled
                    Boolean viewData = getViewData(key);
                    if (viewData != null && viewData.equals(value)) {
                        clearViewData(key);
                        viewData = null;
                    }

                    if (value != null && ((viewData != null) ? viewData : value)) {
                        sb.append(INPUT_CHECKED_DISABLED);
                    } else {
                        sb.append(INPUT_UNCHECKED_DISABLED);
                    }
                } else {
                    // enabled
                    super.render(context, value, sb, id);
                }
            }
        }

        /**
         * holds the enum values that are disabled
         */
        private final Set<E> disabledSet;
        private final AbstractEnumColumn<E, E> nameColumn;

        public EnumRadioCellTable(Class<E> enumClass, final EnumRadioCellTableResources resources) {
            super(15, resources, new ListDataProvider<>(Arrays.asList(enumClass.getEnumConstants())));
            disabledSet = new HashSet<>();
            RowStyles<E> rowStyles = (row, rowIndex) -> disabledSet.contains(row)
                    ? resources.cellTableStyle().cellTableDisabledRow()
                    : resources.cellTableStyle().cellTableEnabledRow();

            setRowStyles(rowStyles);

            // Radio Column
            Column<E, Boolean> radioColumn = new Column<E, Boolean>(
                    new ExRadioboxCell<>(true, false, disabledSet)) {
                @Override
                public Boolean getValue(E object) {
                    return getSelectionModel().isSelected(object);
                }
            };

            // Text Column
            nameColumn = new AbstractEnumColumn<E, E>() {
                @Override
                protected E getRawValue(E object) {
                    return object;
                }
            };

            addColumn(radioColumn);
            addColumn(nameColumn);
            setRowData(Arrays.asList(enumClass.getEnumConstants()));
        }

        @Override
        public boolean isEnabled() {
            return getVisibleItemCount() > 0;
        }

        @Override
        public void setEnabled(boolean enabled) {
            for (E item : getVisibleItems()) {
                setEnabled(item, enabled);
            }
            if (enabled) {
                nameColumn.getCell().setStyleClass(OvirtCss.LABEL_ENABLED);
            } else {
                nameColumn.getCell().setStyleClass(OvirtCss.LABEL_DISABLED);
            }
        }

        /**
         * Enables a specific enum value
         */
        public void setEnabled(E value, boolean enabled) {
            if (enabled) {
                disabledSet.remove(value);
            } else {
                disabledSet.add(value);
            }
            redraw();
        }

        protected Set<E> getDisabledSet() {
            return disabledSet;
        }

    }

    private final HandlerManager handlerManager;
    private TakesValueWithChangeHandlersEditor<E> editor;
    private final EnumRadioCellTable<E> peer;

    private final SingleSelectionModel<E> selectionModel;

    /**
     * Create a new EnumRadioEditor, for the provided enum class
     *
     * @param enumClass
     *            the enum class
     */
    public EnumRadioEditor(Class<E> enumClass) {
        this.handlerManager = new HandlerManager(this);
        peer = new EnumRadioCellTable<>(enumClass, GWT.create(EnumRadioCellTableResources.class));

        // Selection Model
        selectionModel = new SingleSelectionModel<E>() {
            @Override
            public void setSelected(E object, boolean selected) {
                // enforce disabled set
                if (!peer.getDisabledSet().contains(object)) {
                    super.setSelected(object, selected);
                }
            }
        };
        peer.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(event -> {
            E selectedObject = selectionModel.getSelectedObject();
            setValue(selectedObject);
            ValueChangeEvent.fire(EnumRadioEditor.this, selectedObject);
        });
    }

    public void addStyleName(String styleName) {
        peer.addStyleName(styleName);
    }

    public void removeStyleName(String styleName) {
        peer.removeStyleName(styleName);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return peer.addHandler(handler, KeyDownEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return peer.addHandler(handler, KeyPressEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return peer.addHandler(handler, KeyUpEvent.getType());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<E> handler) {
        // don't add to peer, since its changed value is the entire item list
        return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public TakesValueWithChangeHandlersEditor<E> asEditor() {
        if (editor == null) {
            editor = TakesValueWithChangeHandlersEditor.of(this, this);
        }
        return editor;
    }

    @Override
    public EnumRadioCellTable<E> asWidget() {
        return peer;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        handlerManager.fireEvent(event);
    }

    @Override
    public int getTabIndex() {
        return peer.getTabIndex();
    }

    @Override
    public E getValue() {
        return selectionModel.getSelectedObject();
    }

    @Override
    public boolean isEnabled() {
        return peer.isEnabled();
    }

    @Override
    public void setAccessKey(char key) {
        peer.setAccessKey(key);
    }

    @Override
    public void setEnabled(boolean enabled) {
        peer.setEnabled(enabled);
    }

    public void setEnabled(E value, boolean enabled) {
        peer.setEnabled(value, enabled);
    }

    @Override
    public void setFocus(boolean focused) {
        peer.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        peer.setTabIndex(index);
    }

    @Override
    public void setValue(E value) {
        selectionModel.setSelected(value, true);
    }

    @Override
    public void setAccessible(boolean accessible) {
        peer.setVisible(accessible);
    }

    @Override
    public boolean isAccessible() {
        return peer.isVisible();
    }
    @Override
    public LeafValueEditor<E> getActualEditor() {
        return this;
    }

    @Override
    public void disable(String disabilityHint) {
        // not implemented
    }

    @Override
    public void markAsValid() {
        // not implemented
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        // not implemented
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
