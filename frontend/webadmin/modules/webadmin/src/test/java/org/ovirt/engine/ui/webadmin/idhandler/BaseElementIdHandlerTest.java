package org.ovirt.engine.ui.webadmin.idhandler;

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
        tested.setElementId(object, "ElementId");
        verify(object).setElementId("ElementId");
    }

    @Test
    public void setElementId_withIdExtension_defaultBehavior() {
        tested.setIdExtension("IdExtension");
        tested.setElementId(object, "ElementId");
        verify(object).setElementId("ElementId_IdExtension");
    }

    @Test
    public void setElementId_withIdExtension_nullValue() {
        tested.setIdExtension(null);
        tested.setElementId(object, "ElementId");
        verify(object).setElementId("ElementId");
    }

    @Test
    public void setElementId_withIdExtension_emptyString() {
        tested.setIdExtension("");
        tested.setElementId(object, "ElementId");
        verify(object).setElementId("ElementId");
    }

}
