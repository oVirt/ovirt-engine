package org.ovirt.engine.core.aaa;

import java.util.List;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class AcctUtils {

    public static void report(ExtMap input) {
        List<ExtensionProxy> acctExtensions =
                EngineExtensionsManager.getInstance().getExtensionsByService(Acct.class.getName());
        input.putIfAbsent(Base.InvokeKeys.COMMAND, Acct.InvokeCommands.REPORT);
        if (acctExtensions != null) {
            for (ExtensionProxy proxy : acctExtensions) {
                proxy.invoke(input);
            }
        }
    }

    public static void reportReason(int reportReason, String message, Object... msgArgs) {
        ExtMap input = new ExtMap().mput(
                Acct.InvokeKeys.REASON,
                reportReason
                ).mput(
                        Acct.InvokeKeys.MESSAGE,
                        String.format(message, msgArgs)
                );
        report(input);
    }

    public static void reportRecords(
            int reportReason,
            String authzName,
            String user,
            String message,
            Object... msgArgs
            ) {
        ExtMap input = new ExtMap();
        input.put(Acct.InvokeKeys.REASON, reportReason);
        input.put(Acct.InvokeKeys.PRINCIPAL_RECORD,
                new ExtMap().mput(
                        Acct.PrincipalRecord.AUTHZ_NAME,
                        authzName
                        ).mput(
                                Acct.PrincipalRecord.USER,
                                user
                        ).mput(
                                Acct.InvokeKeys.MESSAGE,
                                String.format(message, msgArgs)
                        )
                );
        report(input);
    }

}
