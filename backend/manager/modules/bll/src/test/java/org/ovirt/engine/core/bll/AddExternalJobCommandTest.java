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
        when(commandMock.validate()).thenCallRealMethod();
        when(commandMock.getParameters()).thenReturn(parameters);
    }

    @Test
    public void validateDescriptionOkSucceeds() throws Exception {
        setupMock();
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() throws Exception {
        setupMock();
        parameters.setDescription("");
        assertTrue(! commandMock.validate());
    }

    @Test
    public void validateBlankDescriptionFails() throws Exception {
        parameters.setDescription("      ");
        setupMock();
        assertTrue(! commandMock.validate());
    }
}
