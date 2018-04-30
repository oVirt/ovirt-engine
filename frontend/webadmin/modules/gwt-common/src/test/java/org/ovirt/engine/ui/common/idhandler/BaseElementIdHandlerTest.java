package org.ovirt.engine.ui.common.idhandler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseElementIdHandlerTest {

    @Mock
    private HasElementId object;

    private BaseElementIdHandler<HasElementId> tested;

    @BeforeEach
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
