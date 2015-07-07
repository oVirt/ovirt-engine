package org.ovirt.engine.ui.common.binding;

import org.ovirt.engine.ui.common.editor.AbstractUiCommonModelEditorDriver;
import org.ovirt.engine.ui.common.editor.UiCommonEventMap;
import org.ovirt.engine.ui.common.editor.UiCommonListenerMap;
import org.ovirt.engine.ui.common.editor.UiCommonModelEditorDelegate;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.rebind.AbstractEditorDriverGenerator;
import com.google.gwt.editor.rebind.model.EditorData;
import com.google.gwt.editor.rebind.model.EditorModel;
import com.google.gwt.user.rebind.SourceWriter;

public class UiCommonEditorDriverGenerator extends AbstractEditorDriverGenerator {

    private JClassType entityModelType;
    private JClassType listModelType;
    private TreeLogger logger;
    private EditorModel model;
    private SourceWriter sw;

    @Override
    protected Class<?> getDriverInterfaceType() {
        return SimpleBeanEditorDriver.class;
    }

    @Override
    protected Class<?> getDriverSuperclassType() {
        return AbstractUiCommonModelEditorDriver.class;
    }

    @Override
    protected Class<?> getEditorDelegateType() {
        return UiCommonModelEditorDelegate.class;
    }

    @Override
    protected String mutableObjectExpression(EditorData data,
            String sourceObjectExpression) {
        return sourceObjectExpression;
    }

    /**
     * Implement additional methods defined in {@link AbstractUiCommonModelEditorDriver}
     */
    @Override
    protected void writeAdditionalContent(TreeLogger logger,
            GeneratorContext context,
            EditorModel model,
            SourceWriter sw)
            throws UnableToCompleteException {
        TypeOracle typeOracle = context.getTypeOracle();
        entityModelType = eraseType(typeOracle.findType("org.ovirt.engine.ui.uicommonweb.models.EntityModel")); //$NON-NLS-1$
        listModelType = eraseType(typeOracle.findType("org.ovirt.engine.ui.uicommonweb.models.ListModel")); //$NON-NLS-1$

        this.logger = logger;
        this.model = model;
        this.sw = sw;

        logger.log(Type.DEBUG, "Starting to write additional Driver code"); //$NON-NLS-1$
        writeListenerMap();
        writeEventMap();
        writeOwnerModels();

    }

    /**
     * Writes the UiCommonListenerMap for the edited model
     */
    private void writeListenerMap() {
        logger.log(Type.DEBUG, "Starting to write ListenerMap"); //$NON-NLS-1$
        sw.println();
        sw.println("@Override"); //$NON-NLS-1$
        sw.println("protected " + UiCommonListenerMap.class.getName() + " getListenerMap() {"); //$NON-NLS-1$ //$NON-NLS-2$
        sw.indent();

        sw.println(UiCommonListenerMap.class.getName() + " listenerMap = new " + UiCommonListenerMap.class.getName() //$NON-NLS-1$
                + "();"); //$NON-NLS-1$
        sw.println();

        logger.log(Type.DEBUG, "Looking for top-level Editor Fields"); //$NON-NLS-1$

        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData); //$NON-NLS-1$
            String path = editorData.getPath();
            // Change first letter to Upper to comply with UiCommon Property Names
            path = Character.toUpperCase(path.charAt(0)) + path.substring(1, path.length());

            if (path.length() == 0) {
                continue;
            }

            // only relevant for top-level properties
            if (!editorData.isDeclaredPathNested()) {
                logger.log(Type.DEBUG, "Found top-level Field: " + editorData); //$NON-NLS-1$

                sw.println("listenerMap.addListener(\"%s\", \"PropertyChanged\", new org.ovirt.engine.ui.uicompat.IEventListener() {", //$NON-NLS-1$
                        path);
                sw.indent();
                sw.println("@Override"); //$NON-NLS-1$
                sw.println("public void eventRaised(org.ovirt.engine.ui.uicompat.Event ev, Object sender, org.ovirt.engine.ui.uicompat.EventArgs args) {"); //$NON-NLS-1$
                sw.indent();
                sw.println("getEditor().%s.setValue(getObject()%s);", //$NON-NLS-1$
                        editorData.getExpression(),
                        editorData.getGetterExpression());
                sw.outdent();
                sw.println("}"); //$NON-NLS-1$
                sw.outdent();
                sw.println("});"); //$NON-NLS-1$
                sw.println();
            }
        }

        sw.println("return listenerMap;"); //$NON-NLS-1$
        sw.outdent();
        sw.println("}"); //$NON-NLS-1$
    }

    /**
     * Writes the UiCommonEventMap for the edited model
     */
    private void writeEventMap() {
        logger.log(Type.DEBUG, "Starting to write EventMap"); //$NON-NLS-1$

        sw.println();
        sw.println("@Override"); //$NON-NLS-1$
        sw.println("protected " + UiCommonEventMap.class.getName() + " getEventMap() {"); //$NON-NLS-1$ //$NON-NLS-2$
        sw.indent();

        sw.println(UiCommonEventMap.class.getName() + " eventMap = new " + UiCommonEventMap.class.getName() + "();"); //$NON-NLS-1$ //$NON-NLS-2$

        logger.log(Type.DEBUG, "Looking for Model Fields"); //$NON-NLS-1$

        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData); //$NON-NLS-1$

            String path = editorData.getPath();
            if (path.length() == 0) {
                continue;
            }

            JClassType propertyOwnerType = eraseType(editorData.getPropertyOwnerType());

            if (propertyOwnerType == entityModelType) {
                logger.log(Type.DEBUG, "Found EntityModel Field: " + editorData); //$NON-NLS-1$

                sw.println("eventMap.addEvent(\"%s\", \"EntityChanged\", getObject()%s.getEntityChangedEvent());", //$NON-NLS-1$
                        path, editorData.getBeanOwnerExpression());

            } else if (propertyOwnerType == listModelType) {
                logger.log(Type.DEBUG, "Found ListModel Field: " + editorData); //$NON-NLS-1$

                sw.println("eventMap.addEvent(\"%s\", \"ItemsChanged\", getObject()%s.getItemsChangedEvent());", //$NON-NLS-1$
                        path, editorData.getBeanOwnerExpression());
                sw.println("eventMap.addEvent(\"%s\", \"SelectedItemsChanged\", getObject()%s.getSelectedItemsChangedEvent());", //$NON-NLS-1$
                        path,
                        editorData.getBeanOwnerExpression());
                sw.println("eventMap.addEvent(\"%s\", \"SelectedItemChanged\", getObject()%s.getSelectedItemChangedEvent());", //$NON-NLS-1$
                        path,
                        editorData.getBeanOwnerExpression());
            }
        }

        sw.println("return eventMap;"); //$NON-NLS-1$
        sw.outdent();
        sw.println("}"); //$NON-NLS-1$
    }

    /**
     * Writes the map of the owner Models
     */
    private void writeOwnerModels() {
        logger.log(Type.DEBUG, "Starting to write OwnerModels"); //$NON-NLS-1$
        sw.println();
        sw.println("@Override"); //$NON-NLS-1$
        sw.println("protected java.util.Map<String, org.ovirt.engine.ui.uicommonweb.models.Model> getOwnerModels() {"); //$NON-NLS-1$
        sw.indent();

        sw.println("java.util.Map<String, org.ovirt.engine.ui.uicommonweb.models.Model> regs = new java.util.HashMap<String, org.ovirt.engine.ui.uicommonweb.models.Model>();"); //$NON-NLS-1$

        logger.log(Type.DEBUG, "Going over Editor Fields"); //$NON-NLS-1$
        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData); //$NON-NLS-1$
            String path = editorData.getPath();
            if (path.length() == 0) {
                continue;
            }

            JClassType propertyOwnerType = eraseType(editorData.getPropertyOwnerType());

            if (propertyOwnerType == listModelType || propertyOwnerType == entityModelType) {
                logger.log(Type.DEBUG, "Found owner Model Field: " + editorData); //$NON-NLS-1$
                sw.println("regs.put(\"%s\", getObject()%s);", path, editorData.getBeanOwnerExpression()); //$NON-NLS-1$
            }
        }
        sw.println("return regs;"); //$NON-NLS-1$
        sw.outdent();
        sw.println("}"); //$NON-NLS-1$

    }

    private JClassType eraseType(JClassType classType) {
        return classType == null ? null : classType.getErasedType();
    }
}
