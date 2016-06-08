package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.text.shared.Renderer;

/**
 * The editor associated with a ListBox that allows multiple selections.
 * Due to the {@code EditorContext} setInModel single object limit we have to be a little creative in how we pass
 * multiple values around. If we set the type to be {@code List<T>} instead of T, we have the ability to pass around
 * multiple values in the list. However due to the generics the {@code Renderer} is now passed a List instead of a
 * single object. As an example of an EnumRenderer:
 * <pre>
 * <code>
 * editor = new ListModelMultipleSelectListBoxEditor&lt;T&gt;(new AbstractRenderer&lt;List&lt;T&gt;&gt;() {
 *   {@literal}@Override
 *   public String render(List&lt;T&gt; object) {
 *     //Get the first element of the list, as we are assuming this is a single item list.
 *     return EnumTranslator.getInstance().translate(object.get(0));
 *   }
 * }, new ModeSwitchingVisibilityRenderer());
 * </code>
 * </pre>
 *
 * Notice that the Renderer takes a List&lt;T&gt; parameter instead of a straight &lt;T&gt;, but that the type
 * of the {@code ListModelMultipleSelectListBoxEditor} takes a straight &lt;T&gt;<br>
 * <br>
 * The definition of the editor looks something like this:
 * <pre>
 * <code>
 * {@literal}@UiField(provided = true)
 * {@literal}@Path(value = "type.selectedItems")
 * {@literal}@WithElementId("type")
 * public ListModelMultipleSelectListBoxEditor&lt;T&gt; editor;
 * </code>
 * </pre>
 * Notice that the type of variable is a straight {@code <T>}. Also note the path value is selectedItems (plural). So
 * the ListModels setSelectedItems is used instead of the usual setSelectedItem.
 *
 * Usage in a ui binder xml file is the same as normal.<br>
 * <pre>
 * {@code <e:ListModelMultipleSelectListBoxEditor ui:field="editor" />}
 * </pre>
 * @param <T> The type of the object to use in the list box.
 */
public class ListModelMultipleSelectListBoxEditor<T>
    extends AbstractValidatedWidgetWithLabel<List<T>, ListModelMultipleSelectListBox<T>>
    implements IsEditor<WidgetWithLabelEditor<List<T>, ListModelMultipleSelectListBoxEditor<T>>> {

    private static class SingletonListRendererAdapter<T> extends NullSafeRenderer<List<T>> {

        private final Renderer<T> renderer;

        public SingletonListRendererAdapter(Renderer<T> renderer) {
            this.renderer = renderer;
        }

        @Override
        protected String renderNullSafe(List<T> list) {
            return list.isEmpty() ? "" : renderer.render(list.get(0));
        }

    }

    private final WidgetWithLabelEditor<List<T>, ListModelMultipleSelectListBoxEditor<T>> editor;

    public ListModelMultipleSelectListBoxEditor(Renderer<T> renderer, VisibilityRenderer visibilityRenderer) {
        super(new ListModelMultipleSelectListBox<>(new SingletonListRendererAdapter<>(renderer)), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<List<T>, ListModelMultipleSelectListBoxEditor<T>> asEditor() {
        return editor;
    }
}
