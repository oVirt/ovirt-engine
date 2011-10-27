package org.ovirt.engine.core.compat.backendcompat;

public class Environment {

    public final static String MachineName;

    static {
        String strMachine = null;
        if (System.getProperty("os.name").startsWith("Win")) {
            strMachine = System.getenv("COMPUTERNAME");
        } else {
            strMachine = System.getenv("HOSTNAME");
        }

        if (strMachine == null)
        {
            try
            {
                strMachine = java.net.InetAddress.getLocalHost().getHostName();
            }
            catch (Exception e) {
                strMachine = null;
            }
        }

        MachineName = strMachine;
    }
}
