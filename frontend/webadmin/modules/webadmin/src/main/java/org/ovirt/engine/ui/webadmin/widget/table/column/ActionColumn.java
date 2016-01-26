package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;

/**
 * A Column that shows one Text Cell, and one or more Action Cells
 *
 * @param <T>
 *            The Column Type
 */
public class ActionColumn<T> extends IdentityColumn<T> {

    /**
     * The delegate that is used when the Action Cell is rendered/clicked.<BR>
     * An Implementation must provide either a Button Text or a Button Image for the Action Cell.<BR>
     * {@link #showAction(Object)} can be overwritten to decide whether to show the action button.
     *
     * @param <C>
     *            The Action Column Type
     */
    public abstract static class ActionCellDelegate<C> implements Delegate<C> {

        @Override
        public final void execute(C object) {
            // not used
        };

        public abstract void execute(C object, NativeEvent event);

        /**
         * Gets the Button Image<BR>
         * Default implementation returns <code>null</code>
         *
         * @param object
         *            the object which is the target for this Action Cell
         */
        public ImageResource getButtonImage(C object) {
            return null;
        }

        /**
         * Gets the Button Text<BR>
         * Default implementation returns <code>null</code>
         *
         * @param object
         *            the object which is the target for this Action Cell
         */
        public String getButtonText(C object) {
            return null;
        }

        /**
         * Whether to show the Button for the specified object
         *
         * @param object
         *            the object which is the target for this Action Cell
         */
        public boolean showAction(C object) {
            return true;
        }
    }

    /**
     * Gets string values for text cells
     *
     * @param <C>
     *            The Column Type
     */
    public interface ValueGetter<C> {

        /**
         * Get String the value for the Text Cell
         */
        public SafeHtml getValue(C object);
    }

    /**
     * An Action Cell the is left floated, and uses an {@link ActionCellDelegate}
     *
     * @param <C>
     *            The Action Cell Type
     */
    private static class FloatingActionCell<C> extends ActionCell<C> {
        private final ActionCellDelegate<C> delegate;
        private static final ApplicationTemplates templates = AssetProvider.getTemplates();

        public FloatingActionCell(ActionCellDelegate<C> delegate) {
            super("", delegate); //$NON-NLS-1$
            this.delegate = delegate;
        }

        @Override
        public void render(Context context, C value, SafeHtmlBuilder sb) {
            // show cell?
            if (delegate.showAction(value)) {
                String buttonText = delegate.getButtonText(value);
                ImageResource buttonImage = delegate.getButtonImage(value);
                // use text or image
                if (buttonText != null) {
                    sb.append(templates.actionButtonText(buttonText));
                } else if (buttonImage != null) {
                    sb.append(templates.actionButtonImage(buttonImage.getURL()));
                } else {
                    throw new IllegalArgumentException("Either getButtonText() or getButtonImage() must return non-null values"); //$NON-NLS-1$
                }
            }
        };

        @Override
        protected void onEnterKeyDown(Cell.Context context,
                Element parent,
                C value,
                NativeEvent event,
                ValueUpdater<C> valueUpdater) {
            delegate.execute(value, event);
        }
    }

    /**
     * Create the Column Cells from a single delegate
     */
    private static <S> List<HasCell<S, ?>> createCells(ValueGetter<S> getter, ActionCellDelegate<S> delegate) {
        List<ActionCellDelegate<S>> delegates = new ArrayList<>();
        delegates.add(delegate);
        return createCells(getter, delegates);
    }

    /**
     * Create the Column Cells from a a list of delegates
     */
    private static <S> List<HasCell<S, ?>> createCells(final ValueGetter<S> getter,
            List<ActionCellDelegate<S>> delegates) {
        List<HasCell<S, ?>> cells = new ArrayList<>();

        // text cell
        AbstractSafeHtmlColumn<S> textColumn = new AbstractSafeHtmlColumn<S>() {
            @Override
            public SafeHtml getValue(S object) {
                return getter.getValue(object);
            }
        };
        cells.add(textColumn);

        // action cells
        for (ActionCellDelegate<S> delegate : delegates) {
            Column<S, S> actionColumn = new IdentityColumn<>(new FloatingActionCell<>(delegate));
            cells.add(actionColumn);
        }

        return cells;
    }

    /**
     * Create an Action Column with the specified getter and delegate
     *
     * @param getter
     *            The getter for the Text Cell
     * @param delegate
     *            The delegate for the Action Cell
     */
    public ActionColumn(ValueGetter<T> getter, ActionCellDelegate<T> delegate) {
        super(new CompositeCell<>(createCells(getter, delegate)));
    }

    /**
     * Create an Action Column with the specified getter and list of delegates
     *
     * @param getter
     *            The getter for the Text Cell
     * @param delegate
     *            The list of delegates for the Action Cells
     */
    public ActionColumn(ValueGetter<T> getter, List<ActionCellDelegate<T>> delegates) {
        super(new CompositeCell<>(createCells(getter, delegates)));
    }
}
