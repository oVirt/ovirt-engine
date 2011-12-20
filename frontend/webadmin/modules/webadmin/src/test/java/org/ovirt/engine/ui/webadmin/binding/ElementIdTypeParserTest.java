package org.ovirt.engine.ui.webadmin.binding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp() throws UnableToCompleteException {
        when(logger.branch(any(TreeLogger.Type.class), anyString(),
                any(Throwable.class), any(HelpInfo.class))).thenReturn(logger);

        tested = new ElementIdTypeParser(logger, interfaceType) {
            @Override
            JClassType resolveOwnerType(JClassType interfaceToImplement) throws UnableToCompleteException {
                return ownerType;
            }
        };

        stubPassingField(field, mock(JClassType.class), "field");

        JClassType ownerTypeParent = mock(JClassType.class);
        Set<? extends JClassType> ownerTypeFlattenedSupertypeHierarchy = new HashSet<JClassType>(
                Arrays.asList(ownerType, ownerTypeParent));
        doReturn(ownerTypeFlattenedSupertypeHierarchy).when(ownerType).getFlattenedSupertypeHierarchy();
        when(ownerType.getFields()).thenReturn(new JField[] { ownerTypeField1, ownerTypeField2 });
        when(ownerTypeParent.getFields()).thenReturn(new JField[] { ownerTypeParentField });

        JClassType ownerTypeParentFieldType = mock(JClassType.class, "ownerTypeParentFieldType");
        stubPassingField(ownerTypeField1, mock(JClassType.class), "ownerTypeField1");
        stubPassingField(ownerTypeField2, mock(JClassType.class), "ownerTypeField2");
        stubPassingField(ownerTypeParentField, ownerTypeParentFieldType, "ownerTypeParentField");

        Set<? extends JClassType> ownerTypeParentFieldTypeFlattenedSupertypeHierarchy = new HashSet<JClassType>(
                Arrays.asList(ownerTypeParentFieldType));
        doReturn(ownerTypeParentFieldTypeFlattenedSupertypeHierarchy).when(ownerTypeParentFieldType)
                .getFlattenedSupertypeHierarchy();
        when(ownerTypeParentFieldType.getFields()).thenReturn(new JField[] {
                ownerTypeParentFieldTypeSubField1, ownerTypeParentFieldTypeSubField2 });

        stubPassingField(ownerTypeParentFieldTypeSubField1, ownerTypeParentFieldTypeSubField1Type,
                "ownerTypeParentFieldTypeSubField1");
        stubPassingField(ownerTypeParentFieldTypeSubField2, mock(JClassType.class),
                "ownerTypeParentFieldTypeSubField2");
    }

    void stubPassingField(JField field, JClassType fieldType, String fieldName) {
        WithElementId idAnnotation = mock(WithElementId.class);
        when(field.isPrivate()).thenReturn(false);
        when(field.isStatic()).thenReturn(false);
        when(field.getType()).thenReturn(fieldType);
        when(fieldType.isClass()).thenReturn(fieldType);
        when(field.getAnnotation(WithElementId.class)).thenReturn(idAnnotation);
        when(idAnnotation.value()).thenReturn("");
        when(field.getName()).thenReturn(fieldName);
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
    public void doParse_expectedBehavior() throws UnableToCompleteException {
        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix");

        List<ElementIdStatement> expected = Arrays.asList(
                getExpectedStatement("ownerTypeParentField", "IdPrefix_ownerTypeParentField"),
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField1",
                        "IdPrefix_ownerTypeParentField_ownerTypeParentFieldTypeSubField1"),
                getExpectedStatement("ownerTypeParentField.ownerTypeParentFieldTypeSubField2",
                        "IdPrefix_ownerTypeParentField_ownerTypeParentFieldTypeSubField2"),
                getExpectedStatement("ownerTypeField1", "IdPrefix_ownerTypeField1"),
                getExpectedStatement("ownerTypeField2", "IdPrefix_ownerTypeField2"));
        assertTrue(tested.statements.containsAll(expected));
        assertThat(tested.statements.size(), is(equalTo(expected.size())));
    }

    @Test
    public void doParse_customIdAnnotationValue() throws UnableToCompleteException {
        WithElementId idAnnotation = mock(WithElementId.class);
        when(ownerTypeParentField.getAnnotation(WithElementId.class)).thenReturn(idAnnotation);
        when(idAnnotation.value()).thenReturn("ownerTypeParentFieldCustomId");

        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix");

        assertTrue(tested.statements.contains(getExpectedStatement(
                "ownerTypeParentField", "IdPrefix_ownerTypeParentFieldCustomId")));
    }

    @Test(expected = UnableToCompleteException.class)
    public void doParse_fieldTypeRecursion() throws UnableToCompleteException {
        Set<? extends JClassType> ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy =
                new HashSet<JClassType>(Arrays.asList(ownerTypeParentFieldTypeSubField1Type));
        doReturn(ownerTypeParentFieldTypeSubField1TypeFlattenedSupertypeHierarchy)
                .when(ownerTypeParentFieldTypeSubField1Type).getFlattenedSupertypeHierarchy();
        when(ownerTypeParentFieldTypeSubField1Type.getFields()).thenReturn(new JField[] { ownerTypeParentField });

        tested.doParse(ownerType, new ArrayList<JClassType>(), ".", "IdPrefix");
    }

    ElementIdStatement getExpectedStatement(String pathToField, String elementId) {
        return new ElementIdStatement(ElementIdHandlerGenerator.ElementIdHandler_generateAndSetIds_owner
                + "." + pathToField, elementId);
    }

}
