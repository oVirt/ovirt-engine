package org.ovirt.engine.api.restapi.resource;

import static org.easymock.classextension.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {LoggerFactory.class })
public abstract class AbstractBackendResourceLoggingTest extends Assert {

    @After
    public void tearDown() {
        verifyAll();
    }

    protected void setUpLogExpectations(boolean debug) {
        mockStatic(LoggerFactory.class);
        Logger log = createMock(Logger.class);
        expect(LoggerFactory.getLogger(AbstractBackendResource.class)).andReturn(log);
        expect(log.isDebugEnabled()).andReturn(debug).anyTimes();

        replayAll();
    }
}
