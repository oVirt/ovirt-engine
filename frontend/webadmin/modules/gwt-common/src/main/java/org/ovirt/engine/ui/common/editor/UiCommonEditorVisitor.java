package org.ovirt.engine.ui.common.editor;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.HasAccess;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.editor.client.EditorContext;
import com.google.gwt.editor.client.EditorVisitor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Editor visitor that implements integration with UiCommon models.
 *
 * @see UiCommonEditor
 */
public class UiCommonEditorVisitor<M extends Model> extends EditorVisitor {

    private final UiCommonEventMap eventMap;
    private final Map<String, EntityModel> ownerModels;
    private int tabIndexCounter = 0;

    /**
     * A Visitor for UICommon Edited Models.
     */
    public UiCommonEditorVisitor(UiCommonEventMap eventMap, Map<String, EntityModel> ownerModels) {
        this.eventMap = eventMap;
        this.ownerModels = ownerModels;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean visit(final EditorContext<T> ctx) {
        String absolutePath = ctx.getAbsolutePath();
        LeafValueEditor<T> currentLeafEditor = ctx.asLeafValueEditor();

        if (currentLeafEditor == null) {
            // Ignore non-leaf Editors
            return super.visit(ctx);
        }

        final LeafValueEditor<T> editor = getActualEditor(currentLeafEditor);

        // If this Editor implements HasValueChangeHandlers, register a value change listener
        if (editor instanceof HasValueChangeHandlers) {
            ((HasValueChangeHandlers<T>) editor).addValueChangeHandler(new ValueChangeHandler<T>() {
                @Override
                public void onValueChange(ValueChangeEvent<T> event) {
                    // Set value in model
                    if (ctx.canSetInModel()) {
                        ctx.setInModel(event.getValue());
                    }
                }
            });
        }

        final UiCommonEditor<T> functionalEditor = getFunctionalEditor(currentLeafEditor);

        if (functionalEditor != null) {

            // Set tab index, unless it's being set manually (i.e. already been set)
            if (functionalEditor.getTabIndex() <= 0) {
                functionalEditor.setTabIndex(++tabIndexCounter);
            }

            // Add key press handler
            functionalEditor.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                        // Set value in model
                        if (ctx.canSetInModel()) {
                            ctx.setInModel(editor.getValue());
                        }
                    }
                }
            });
        }

        // Handle owner entity models
        if (ownerModels.containsKey(absolutePath)) {
            EntityModel ownerModel = ownerModels.get(absolutePath);

            // If this editor edits a ListModel, initialize it
            if (editor instanceof TakesConstrainedValueEditor && ownerModel instanceof ListModel) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) ownerModel);
            }

            if (functionalEditor != null) {
                // Register a property change listener on the owner entity model
                ownerModel.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
                    @Override
                    public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                        EntityModel ownerModel = (EntityModel) sender;
                        String propName = args.propertyName;

                        // IsValid
                        if ("IsValid".equals(propName)) { //$NON-NLS-1$
                            onIsValidPropertyChange(functionalEditor, ownerModel);
                        }

                        // IsChangable
                        else if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                            onIsChangablePropertyChange(functionalEditor, ownerModel);
                        }

                        // ChangeProhibitionReason
                        else if ("ChangeProhibitionReason".equals(propName)) { //$NON-NLS-1$
                            onChangeProhibitionReasonChange(functionalEditor, ownerModel);
                        }

                        // IsAvailable
                        else if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                            onIsAvailablePropertyChange(functionalEditor, ownerModel);
                        }
                    }
                });

                // Update editor since we might have missed property change
                // events fired as part of the entity model constructor
                onIsValidPropertyChange(functionalEditor, ownerModel);
                onIsChangablePropertyChange(functionalEditor, ownerModel);
                onChangeProhibitionReasonChange(functionalEditor, ownerModel);
                onIsAvailablePropertyChange(functionalEditor, ownerModel);
            }
        }

        // Register listeners
        eventMap.registerListener(absolutePath, "EntityChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                editor.setValue((T) ((EntityModel) sender).getEntity());
            }
        });
        eventMap.registerListener(absolutePath, "ItemsChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) sender);
            }
        });
        eventMap.registerListener(absolutePath, "SelectedItemChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                editor.setValue((T) ((ListModel) sender).getSelectedItem());
            }
        });

        return super.visit(ctx);
    }

    @SuppressWarnings("unchecked")
    <T> LeafValueEditor<T> getActualEditor(LeafValueEditor<T> editor) {
        if (editor instanceof UiCommonEditor) {
            return ((UiCommonEditor<T>) editor).getActualEditor();
        } else {
            return editor;
        }
    }

    @SuppressWarnings("unchecked")
    <T> UiCommonEditor<T> getFunctionalEditor(LeafValueEditor<T> editor) {
        if (editor instanceof UiCommonEditor) {
            return (UiCommonEditor<T>) editor;
        } else {
            return null;
        }
    }

    /**
     * Update a ListEditor according to the items in the ListModel <BR>
     * (this is required since the Editor is bound to the "selectedItem" property, and not to the "items" property).
     *
     * @param listEditor
     * @param parentModel
     */
    @SuppressWarnings("unchecked")
    <O> void updateListEditor(TakesConstrainedValueEditor<O> listEditor, ListModel parentModel) {
        Collection<O> items = (Collection<O>) parentModel.getItems();
        if (items != null) {
            if (items.size() > 0) {
                O value;
                if (parentModel.getSelectedItem() != null) {
                    value = (O) parentModel.getSelectedItem();
                } else {
                    value = items.iterator().next();
                    // Order is important
                    parentModel.setSelectedItem(value);
                }
                listEditor.setValue(value);

            }

            listEditor.setAcceptableValues(items);
        }
    }

    void onIsValidPropertyChange(HasValidation editor, EntityModel model) {
        if (model.getIsValid()) {
            editor.markAsValid();
        } else {
            editor.markAsInvalid(model.getInvalidityReasons());
        }
    }

    void onIsChangablePropertyChange(HasEnabledWithHints editor, EntityModel model) {
        if (model.getIsChangable()) {
            editor.setEnabled(true);
        } else {
            editor.disable(model.getChangeProhibitionReason());
        }
    }

    void onChangeProhibitionReasonChange(HasEnabledWithHints editor, EntityModel model) {
        if (!editor.isEnabled()) {
            editor.disable(model.getChangeProhibitionReason());
        }
    }

    void onIsAvailablePropertyChange(HasAccess editor, EntityModel model) {
        boolean isAvailable = model.getIsAvailable();
        editor.setAccessible(isAvailable);
    }

}
