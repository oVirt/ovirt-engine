package org.ovirt.engine.core.utils.hostinstall;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class VdsInstallerSSHTimeoutTest {

    @Rule
    public static final MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.SSHInactivityTimoutSeconds, 0));

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
        VdsInstallerSSH wrapper = new VdsInstallerSSH(client);

        when(client.connect(anyString(), anyInt())).thenReturn(future);
        when(future.await(anyLong())).thenReturn(false);

        assertFalse(wrapper.connect("localhost", "foo"));
        verify(future).await(anyLong());
    }

    @Test
    public void passwordTimeout() throws Exception {
        SshClient client = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);
        VdsInstallerSSH wrapper = new VdsInstallerSSH(client);
        ClientSession session = mock(ClientSession.class);
        when(client.connect(anyString(), anyInt())).thenReturn(future);
        when(future.await(anyLong())).thenReturn(true);
        when(future.getSession()).thenReturn(session);

        AuthFuture authFuture = mock(AuthFuture.class);
        when(authFuture.await(anyLong())).thenReturn(false);
        when(session.authPassword(anyString(), anyString())).thenReturn(authFuture);

        assertFalse(wrapper.connect("localhost", "foo"));
        verify(future).await(anyLong());
        verify(authFuture).await(anyLong());

    }
}
