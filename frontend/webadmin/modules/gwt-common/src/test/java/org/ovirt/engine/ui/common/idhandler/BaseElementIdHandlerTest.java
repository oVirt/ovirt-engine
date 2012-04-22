package org.ovirt.engine.ui.common.idhandler;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseElementIdHandlerTest {

    @Mock
    private HasElementId object;

    private BaseElementIdHandler<HasElementId> tested;

    @Before
    public void setUp() {
        tested = new BaseElementIdHandler<HasElementId>() {
            @Override
            public void generateAndSetIds(HasElementId owner) {
            }
        };
    }

    @Test
    public void setElementId_withoutIdExtension() {
        tested.setElementId(object, "ElementId"); //$NON-NLS-1$
        verify(object).setElementId("ElementId"); //$NON-NLS-1$
    }

    @Test
    public void setElementId_withIdExtension_defaultBehavior() {
        tested.setIdExtension("IdExtension"); //$NON-NLS-1$
        tested.setElementId(object, "ElementId"); //$NON-NLS-1$
        verify(object).setElementId("ElementId_IdExtension"); //$NON-NLS-1$
    }

    @Test
    public void setElementId_withIdExtension_nullValue() {
        tested.setIdExtension(null);
        tested.setElementId(object, "ElementId"); //$NON-NLS-1$
        verify(object).setElementId("ElementId"); //$NON-NLS-1$
    }

    @Test
    public void setElementId_withIdExtension_emptyString() {
        tested.setIdExtension(""); //$NON-NLS-1$
        tested.setElementId(object, "ElementId"); //$NON-NLS-1$
        verify(object).setElementId("ElementId"); //$NON-NLS-1$
    }

}
