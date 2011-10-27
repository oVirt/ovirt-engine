package org.ovirt.engine.core.utils.hostinstall;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.TestCase;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class MinaInstallWrapperTest extends TestCase {
    private int port = 54321;
    private SshServer sshd;
    private boolean WinOS = false;
    private static String[] hostKstore = new String[] { "src/test/resources/.hostKstore" };
    private static String[] factoryLin = new String[] { "/bin/bash", "-i", "-l" };
    private static String[] factoryWin = new String[] { "cmd.exe" };

    @Before
    public void setup() throws IOException {
        WinOS = System.getProperty("os.name").startsWith("Win");

        ServerSocket s = new ServerSocket(0);
        port = s.getLocalPort();
        s.close();

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new KeystoreKeyPairProvider(hostKstore, "NoSoup4U", "engine"));

        if (WinOS) {
            sshd.setShellFactory(new ProcessShellFactory(factoryWin));
        } else {
            sshd.setShellFactory(new ProcessShellFactory(factoryLin));
        }
        sshd.setCommandFactory(new ScpCommandFactory(new CommandFactory() {
            public Command createCommand(String command) {
                return new ProcessShellFactory(command.split(" ")).create();
            }
        }));
        sshd.setPasswordAuthenticator(new DummyPasswordAuthenticator());
        sshd.setPublickeyAuthenticator(new DummyPublickeyAuthenticator());
        sshd.start();
        System.out.println("Finished setup !!! ");
    }

    @Test
    public void testMinaInstallWrapper() throws Exception {
        this.setup();

        IConfigUtilsInterface confInstance = new DefaultValuesConfigUtil();
        Config.setConfigUtils(confInstance);

        System.out.println("Testing password auth.");
        MinaInstallWrapper mina = new MinaInstallWrapper();
        mina.setPort(port);
        System.out.println("Server port=" + port);

        assertTrue(mina.ConnectToServer("127.0.0.1", "root"));

        String cmd = "ls -l /tmp";
        if (WinOS) {
            cmd = "cmd.exe /c dir c:\\";
        }
        assertTrue(mina.RunSSHCommand(cmd));

        mina.wrapperShutdown();
        mina = null;

        System.out.println("Testing public key auth.");
        mina = new MinaInstallWrapper();
        mina.setPort(port);

        assertTrue(mina.ConnectToServer("127.0.0.1", "src/test/resources/.keystore", "NoSoup4U"));

        mina.wrapperShutdown();
        mina = null;
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Stopping....");
        if (sshd != null) {
            sshd.stop(true);
            Thread.sleep(50);
        }
    }
}
