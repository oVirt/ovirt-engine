package org.ovirt.engine.core.utils.hostinstall;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class })
public class MinaInstallWrapperTimeoutTest {
    public MinaInstallWrapperTimeoutTest() {
        mockStatic(Config.class);
    }

    @Before
    public void setupConfig() {
        when(Config.<Integer> GetValue(ConfigValues.SSHInactivityTimoutSeconds)).thenReturn(0);
    }

    @SuppressWarnings("null")
    @Test
    public void connectTimeout() throws Exception {
        SshClient client = null;
        try {
            client = spy(SshClient.setUpDefaultClient());
        } catch (Throwable t) {
            fail("Unable to create SSH client. Please check mina jars");
        }
        ConnectFuture future = mock(ConnectFuture.class);
        MinaInstallWrapper wrapper = new MinaInstallWrapper(client);

        when(client.connect(anyString(), anyInt())).thenReturn(future);
        when(future.await(anyLong())).thenReturn(false);

        assertFalse(wrapper.ConnectToServer("localhost", "foo"));
        verify(future).await(anyLong());
    }

    @Test
    public void passwordTimeout() throws Exception {
        SshClient client = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);
        MinaInstallWrapper wrapper = new MinaInstallWrapper(client);
        ClientSession session = mock(ClientSession.class);
        when(client.connect(anyString(), anyInt())).thenReturn(future);
        when(future.await(anyLong())).thenReturn(true);
        when(future.getSession()).thenReturn(session);

        AuthFuture authFuture = mock(AuthFuture.class);
        when(authFuture.await(anyLong())).thenReturn(false);
        when(session.authPassword(anyString(), anyString())).thenReturn(authFuture);

        assertFalse(wrapper.ConnectToServer("localhost", "foo"));
        verify(future).await(anyLong());
        verify(authFuture).await(anyLong());

    }
}
