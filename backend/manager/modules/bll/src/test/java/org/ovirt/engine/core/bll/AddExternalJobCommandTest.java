package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddExternalJobCommandTest {

    private AddExternalJobParameters parameters;
    @Mock
    private AddExternalJobCommand<AddExternalJobParameters> commandMock;
    @Mock
    private Logger log;

    @Before
    public void createParameters() {
        parameters = new AddExternalJobParameters("test");
    }

    private void setupMock() throws Exception {
        when(commandMock.canDoAction()).thenCallRealMethod();
        when(commandMock.getParameters()).thenReturn(parameters);
    }

    @Test
    public void canDoActionDescriptionOkSucceeds() throws Exception {
        setupMock();
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionEmptyDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("");
        assertTrue(! commandMock.canDoAction());
    }

    @Test
    public void canDoActionBlankDescriptionFails() throws Exception {
        parameters.setDescription("      ");
        setupMock();
        assertTrue(! commandMock.canDoAction());
    }
}
