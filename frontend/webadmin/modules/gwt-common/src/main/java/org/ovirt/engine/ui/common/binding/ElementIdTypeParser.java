package org.ovirt.engine.ui.common.binding;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.editor.rebind.model.ModelUtils;

/**
 * Walks through fields of an owner type for the given {@link ElementIdHandler} subinterface, generating DOM element IDs
 * and collecting corresponding field statements.
 *
 * @see ElementIdStatement
 */
public class ElementIdTypeParser {

    private static final String ID_ELEMENT_SEPARATOR = "_"; //$NON-NLS-1$

    private final TreeLogger logger;
    private final JClassType ownerType;

    final List<ElementIdStatement> statements = new ArrayList<>();

    public ElementIdTypeParser(TreeLogger logger, JClassType interfaceToImplement) throws UnableToCompleteException {
        assert logger != null : "logger was null"; //$NON-NLS-1$
        assert interfaceToImplement != null : "interfaceToImplement was null"; //$NON-NLS-1$

        this.logger = logger.branch(TreeLogger.DEBUG, "Creating ElementIdTypeParser for " //$NON-NLS-1$
                + interfaceToImplement.getQualifiedSourceName());

        this.ownerType = resolveOwnerType(interfaceToImplement);
    }

    JClassType resolveOwnerType(JClassType interfaceToImplement) throws UnableToCompleteException {
        TypeOracle oracle = interfaceToImplement.getOracle();

        JClassType handlerInterface = oracle.findType(ElementIdHandler.class.getName()).isInterface();
        assert handlerInterface != null : "No ElementIdHandler type"; //$NON-NLS-1$

        if (!handlerInterface.isAssignableFrom(interfaceToImplement)) {
            die(String.format("Unexpected input type: %s is not assignable from %s", //$NON-NLS-1$
                    handlerInterface.getQualifiedSourceName(), interfaceToImplement.getQualifiedSourceName()));
        } else if (interfaceToImplement.equals(handlerInterface)) {
            die(String.format("You must declare an interface that extends the %s type", //$NON-NLS-1$
                    handlerInterface.getSimpleSourceName()));
        }

        JClassType[] interfaces = interfaceToImplement.getImplementedInterfaces();
        if (interfaces.length != 1) {
            die(String.format("The type %s extends more than one interface", //$NON-NLS-1$
                    interfaceToImplement.getQualifiedSourceName()));
        }

        JClassType[] parameters = ModelUtils.findParameterizationOf(handlerInterface, interfaceToImplement);
        if (parameters.length != 1) {
            die(String.format("The type %s has unexpected number of type parameters", //$NON-NLS-1$
                    interfaceToImplement.getQualifiedSourceName()));
        }

        return parameters[0];
    }

    public JClassType getOwnerType() {
        return ownerType;
    }

    public ElementIdStatement[] parseStatements() throws UnableToCompleteException {
        statements.clear();

        statements.add(new ElementIdStatement(
                ElementIdHandlerGenerator.ElementIdHandler_generateAndSetIds_owner,
                getOwnerTypeId()));

        doParse(ownerType, new ArrayList<JClassType>(), ".", getOwnerTypeId()); //$NON-NLS-1$

        return statements.toArray(new ElementIdStatement[0]);
    }

    void doParse(JClassType parentType, List<JClassType> grandParents, String parentFieldExpression,
            String idPrefix) throws UnableToCompleteException {
        for (JClassType type : parentType.getFlattenedSupertypeHierarchy()) {
            for (JField field : type.getFields()) {
                if (!processField(field)) {
                    continue;
                }

                JClassType fieldType = field.getType().isClass();
                String fieldName = field.getName();

                if (grandParents.contains(fieldType)) {
                    die(String.format("Field %s of type %s is already present on path from the owner type %s: %s", //$NON-NLS-1$
                            fieldName, fieldType.getQualifiedSourceName(), ownerType.getQualifiedSourceName(),
                            grandParents.toString()));
                }

                WithElementId idAnnotation = field.getAnnotation(WithElementId.class);
                String fieldId = idAnnotation.value();
                if ("".equals(fieldId)) { //$NON-NLS-1$
                    fieldId = fieldName;
                }

                String elementId = idPrefix + ID_ELEMENT_SEPARATOR + fieldId;
                String fieldExpression = ElementIdHandlerGenerator.ElementIdHandler_generateAndSetIds_owner
                        + parentFieldExpression + fieldName;
                ElementIdStatement statement = new ElementIdStatement(fieldExpression, elementId);

                if (statements.contains(statement)) {
                    die(String.format("Duplicate element ID %s for field %s of type %s", //$NON-NLS-1$
                            elementId, fieldName, fieldType.getQualifiedSourceName()));
                }

                statements.add(statement);

                if (idAnnotation.processType()) {
                    List<JClassType> newGrandParents = new ArrayList<>(grandParents);
                    newGrandParents.add(fieldType);

                    doParse(fieldType, newGrandParents, parentFieldExpression + fieldName + ".", elementId); //$NON-NLS-1$
                }
            }
        }
    }

    String getOwnerTypeId() {
        return ownerType.getName().replace(".", ID_ELEMENT_SEPARATOR); //$NON-NLS-1$
    }

    boolean processField(JField field) {
        return !field.isPrivate() && !field.isStatic()
                && field.getType().isClass() != null
                && field.getAnnotation(WithElementId.class) != null;
    }

    void die(String lastWords) throws UnableToCompleteException {
        logger.log(TreeLogger.ERROR, lastWords);
        throw new UnableToCompleteException();
    }

}
