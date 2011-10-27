package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { LogFactoryCompat.class })
public abstract class AbstractBackendResourceLoggingTest extends Assert {

    @After
    public void tearDown() {
        verifyAll();
    }

    protected void setUpLogExpectations(boolean debug) {
        mockStatic(LogFactoryCompat.class);
        LogCompat log = createMock(LogCompat.class);
        expect(LogFactoryCompat.getLog(AbstractBackendResource.class)).andReturn(log);
        expect(log.isDebugEnabled()).andReturn(debug).anyTimes();

        replayAll();
    }
}
