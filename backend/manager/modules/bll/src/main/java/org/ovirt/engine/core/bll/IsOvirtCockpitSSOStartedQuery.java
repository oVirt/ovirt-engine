package org.ovirt.engine.core.bll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsOvirtCockpitSSOStartedQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    public IsOvirtCockpitSSOStartedQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private static Logger log = LoggerFactory.getLogger(IsOvirtCockpitSSOStartedQuery.class);
    private static final String PID_FILE = "/var/run/ovirt-cockpit-sso/ovirt-cockpit-sso.pid";
    private static final String OVIRT_COCKPIT_SSO = "ovirt-cockpit-sso";
    private static final String PROC = "/proc";
    private static final String CMDLINE = "cmdline";

    private String readLine(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            return br.readLine();
        }
    }

    private String readCmdline(String pid) throws IOException {
        String cmdlineFile = String.format("%s/%s/%s", PROC, pid, CMDLINE);
        return readLine(cmdlineFile);
    }

    @Override
    protected void executeQueryCommand() {
        Boolean result = false;

        try {
            String pid = readLine(PID_FILE);
            if (pid != null) {
                String cmdline = readCmdline(pid.trim());
                result = cmdline == null ? false : cmdline.contains(OVIRT_COCKPIT_SSO);

                if (!result) {
                    log.info("ovirt-cockpit-sso is not running. Found '{}' PID with command line: '{}'", pid, cmdline);
                }
            } else {
                log.info("The PID of ovirt-cockpit-sso service can not be read from: '{}'.", PID_FILE);
            }
        } catch (IOException ioex) {
            log.info("The ovirt-cockpit-sso service is not running, pid file: '{}'.", PID_FILE);
            log.debug("Exception: ", ioex.getMessage());
        }

        getQueryReturnValue().setReturnValue(result);
    }
}
