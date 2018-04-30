package org.ovirt.engine.ui.common.binding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.ui.common.idhandler.WithElementId;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ElementIdTypeParserTest {

    @Mock
    private TreeLogger logger;

    @Mock
    private JClassType interfaceType;

    @Mock
    private JClassType ownerType;

    @Mock
    private JField ownerTypeField1;

    @Mock
    private JField ownerTypeField2;

    @Mock
    private JField ownerTypeParentField;

    @Mock
    private JField ownerTypeParentFieldTypeSubField1;

    @Mock
    private JClassType ownerTypeParentFieldTypeSubField1Type;

    @Mock
    private JField ownerTypeParentFieldTypeSubField2;

    @Mock
    private JField field;

    private ElementIdTypeParser tested;

    @BeforeEach
    public void setUp() throws UnableToCompleteException {
        when(logger.branch(any(), any(), any(), any())).thenReturn(logger);

        tested = new ElementIdTypeParser(logger, interfaceType) {
            @Override
            JClassType resolveOwnerType(JClassType interfaceToImplement) {
                return ownerType;
            }
        };

        stubPassingField(field, mock(JClassType.class), "field"); //$NON-NLS-1$

        JClassType ownerTypeParent = mock(JClassType.class);
        Set<? extends JClassType> ownerTypeFlattenedSupertypeHierarchy = new HashSet<>(
                Arrays.asList(ownerType, ownerTypeParent));
        doReturn(ownerTypeFlattenedSupertypeHierarchy).when(ownerType).getFlattenedSupertypeHierarchy();
        when(ownerType.getFields()).thenReturn(new JField[] { ownerTypeField1, ownerTypeField2 });
        when(ownerTypeParent.getFields()).thenReturn(new JField[] { ownerTypeParentField });
        when(ownerType.getName()).thenReturn("OwnerTypeName"); //$NON-NLS-1$

        JClassType ownerTypeParentFieldType = mock(JClassType.class, "ownerTypeParentFieldType"); //$NON-NLS-1$
        stubPassingField(ownerTypeField1, mock(JClassType.class), "ownerTypeField1"); //$NON-NLS-1$
        stubPassingField(ownerTypeField2, mock(JClassType.class), "ownerTypeField2"); //$NON-NLS-1$
        stubPassingField(ownerTypeParentField, ownerTypeParentFieldType, "ownerTypeParentField"); //$NON-NLS-1$

        Set<? extends JClassType> ownerTypeParentFieldTypeFlattenedSupertypeHierarchy =
                Collections.singleton(ownerTypeParentFieldType);
        doReturn(ownerTypeParentFieldTypeFlattenedSupertypeHierarchy).when(ownerTypeParentFieldType)
                .getFlattenedSupertypeHierarchy();
        when(ownerTypeParentFieldType.getFields()).thenReturn(new JField[] {
                ownerTypeParentFieldTypeSubField1, ownerTypeParentFieldTypeSubField2 });

        stubPassingField(ownerTypeParentFieldTypeSubField1, ownerTypeParentFieldTypeSubField1Type,
                "ownerTypeParentFieldTypeSubField1"); //$NON-NLS-1$
        stubPassingField(ownerTypeParentFieldTypeSubField2, mock(JClassType.class),
                "ownerTypeParentFieldTypeSubField2"); //$NON-NLS-1$
    }

    void stubPassingField(JField field, JClassType fieldType, String fieldName) {
        WithElementId idAnnotation = mock(WithElementId.class);
        when(field.isPrivate()).thenReturn(false);
        when(field.isStatic()).thenReturn(false);
        when(field.getType()).thenReturn(fieldType);
        when(field.getName()).thenReturn(fieldName);
        when(fieldType.isClass()).thenReturn(fieldType);
        when(field.getAnnotation(WithElementId.class)).thenReturn(idAnnotation);
        when(idAnnotation.value()).thenReturn(""); //$NON-NLS-1$
        when(idAnnotation.processType()).thenReturn(true);
    }

    @Test
    public void processField_private() {
        when(field.isPrivate()).thenReturn(true);
        verifyProcessFieldReturns(false);
    }

    @Test
    public void processField_defaultAccess() {
        when(field.isDefaultAccess()).thenReturn(true);
        verifyProcessFieldReturns(true);
    }

    @Test
    public void processField_protected() {
        when(field.isProtected()).thenReturn(true);
        verifyProcessFieldReturns(true);
    }

    @Test
    public void processField_public() {
        when(field.isPublic()).thenReturn(true);
        verifyProcessFieldReturns(true);
    }

    @Test
    public void processField_static() {
        when(field.isStatic()).thenReturn(true);
        verifyProcessFieldReturns(false);
    }

    @Test
    public void processField_final() {
        when(field.isFinal()).thenReturn(true);
        verifyProcessFieldReturns(true);
    }

    @Test
    public void processField_primitiveType() {
        JClassType fieldType = mock(JClassType.class);
        when(field.getType()).thenReturn(fieldType);
        when(fieldType.isClass()).thenReturn(null);
        verifyProcessFieldReturns(false);
    }

    @Test
    public void processField_missingIdAnnotation() {
        when(field.getAnnotation(WithElementId.class)).thenReturn(null);
        verifyProcessFieldReturns(false);
    }

    void verifyProcessFieldReturns(boolean expected) {
        boolean processField = tested.processField(field);
        assertThat(processField, is(equalTo(expected)));
    }

    @Test
    public void doParse_defaultBehavior() throws UnableToCompleteException {
        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix"); //$NON-NLS-1$ //$NON-NLS-2$

        List<ElementIdStatement> expected = Arrays.asList(
                getExpectedStatement("ownerTypeParentField", "IdPrefix_ownerTypeParentField"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField1", //$NON-NLS-1$
                        "IdPrefix_ownerTypeParentField_ownerTypeParentFieldTypeSubField1"), //$NON-NLS-1$
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField2", //$NON-NLS-1$
                        "IdPrefix_ownerTypeParentField_ownerTypeParentFieldTypeSubField2"), //$NON-NLS-1$
                getExpectedStatement("ownerTypeField1", "IdPrefix_ownerTypeField1"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField2", "IdPrefix_ownerTypeField2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(tested.statements.size(), is(equalTo(expected.size())));
        assertThat(tested.statements.containsAll(expected), is(equalTo(true)));
    }

    @Test
    public void doParse_customFieldId() throws UnableToCompleteException {
        stubFieldIdAnnotation(ownerTypeParentField, "ownerTypeParentFieldCustomId", true); //$NON-NLS-1$
        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix"); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(tested.statements.contains(getExpectedStatement(
                "ownerTypeParentField", "IdPrefix_ownerTypeParentFieldCustomId")), is(equalTo(true))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void doParse_duplicateFieldIds() {
        stubFieldIdAnnotation(ownerTypeParentFieldTypeSubField1, "customId", true); //$NON-NLS-1$
        stubFieldIdAnnotation(ownerTypeParentFieldTypeSubField2, "customId", true); //$NON-NLS-1$
        assertThrows(UnableToCompleteException.class,
                () -> tested.doParse(ownerType, new ArrayList<>(), ".", "IdPrefix")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void doParse_limitedFieldTypeRecursion() throws UnableToCompleteException {
        stubFieldIdAnnotation(ownerTypeParentField, "", false); //$NON-NLS-1$
        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix"); //$NON-NLS-1$ //$NON-NLS-2$

        List<ElementIdStatement> expected = Arrays.asList(
                getExpectedStatement("ownerTypeParentField", "IdPrefix_ownerTypeParentField"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField1", "IdPrefix_ownerTypeField1"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField2", "IdPrefix_ownerTypeField2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(tested.statements.size(), is(equalTo(expected.size())));
        assertThat(tested.statements.containsAll(expected), is(equalTo(true)));
    }

    @Test
    public void doParse_unhandledFieldTypeRecursion() {
        Set<? extends JClassType> ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy =
                Collections.singleton(ownerTypeParentFieldTypeSubField1Type);
        doReturn(ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy)
                .when(ownerTypeParentFieldTypeSubField1Type).getFlattenedSupertypeHierarchy();
        when(ownerTypeParentFieldTypeSubField1Type.getFields()).thenReturn(new JField[] { ownerTypeParentField });

        assertThrows(UnableToCompleteException.class,
                () -> tested.doParse(ownerType, new ArrayList<>(), ".", "IdPrefix")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void doParse_handledFieldTypeRecursion() throws UnableToCompleteException {
        Set<? extends JClassType> ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy =
                Collections.singleton(ownerTypeParentFieldTypeSubField1Type);
        doReturn(ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy)
                .when(ownerTypeParentFieldTypeSubField1Type).getFlattenedSupertypeHierarchy();
        when(ownerTypeParentFieldTypeSubField1Type.getFields()).thenReturn(new JField[] { ownerTypeParentField });

        stubFieldIdAnnotation(ownerTypeParentField, "", false); //$NON-NLS-1$
        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix"); //$NON-NLS-1$ //$NON-NLS-2$

        List<ElementIdStatement> expected = Arrays.asList(
                getExpectedStatement("ownerTypeParentField", "IdPrefix_ownerTypeParentField"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField1", "IdPrefix_ownerTypeField1"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField2", "IdPrefix_ownerTypeField2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertThat(tested.statements.size(), is(equalTo(expected.size())));
        assertThat(tested.statements.containsAll(expected), is(equalTo(true)));
    }

    @Test
    public void parseStatements_defaultBehavior() throws UnableToCompleteException {
        tested.parseStatements();

        List<ElementIdStatement> expected = Arrays.asList(
                getExpectedStatement("ownerTypeParentField", "OwnerTypeName_ownerTypeParentField"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField1", //$NON-NLS-1$
                        "OwnerTypeName_ownerTypeParentField_ownerTypeParentFieldTypeSubField1"), //$NON-NLS-1$
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField2", //$NON-NLS-1$
                        "OwnerTypeName_ownerTypeParentField_ownerTypeParentFieldTypeSubField2"), //$NON-NLS-1$
                getExpectedStatement("ownerTypeField1", "OwnerTypeName_ownerTypeField1"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement("ownerTypeField2", "OwnerTypeName_ownerTypeField2"), //$NON-NLS-1$ //$NON-NLS-2$
                getExpectedStatement(null, "OwnerTypeName") //$NON-NLS-1$
                );

        assertThat(tested.statements.size(), is(equalTo(expected.size())));
        assertThat(tested.statements.containsAll(expected), is(equalTo(true)));
    }

    void stubFieldIdAnnotation(JField field, String fieldId, boolean processType) {
        WithElementId idAnnotation = mock(WithElementId.class);
        when(field.getAnnotation(WithElementId.class)).thenReturn(idAnnotation);
        when(idAnnotation.value()).thenReturn(fieldId);
        when(idAnnotation.processType()).thenReturn(processType);
    }

    ElementIdStatement getExpectedStatement(String pathToField, String elementId) {
        String fieldExpression = ElementIdHandlerGenerator.ElementIdHandler_generateAndSetIds_owner;

        if (pathToField != null) {
            fieldExpression += "." + pathToField; //$NON-NLS-1$
        }

        return new ElementIdStatement(fieldExpression, elementId);
    }

}
