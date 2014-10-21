package org.ovirt.engine.core.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.RegisterVdsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RegisterServlet.class);

    private static final int SSH_PORT = 22;
    private static final int VDSM_PORT = 54321;
    private static final int INTERFACE_VERSION = 1;

    protected void getVersionV1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info(
            "Version request: source={}', secured='{}'",
            request.getRemoteHost(),
            request.isSecure()
        );

        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");
            out.print(INTERFACE_VERSION);
        }
    }

    protected void getPKITrustV1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info(
            "PKI Trust request: source='{}', secured='{}'",
            request.getRemoteHost(),
            request.isSecure()
        );

        try (PrintWriter out = response.getWriter()) {
            response.setContentType(PKIResources.Resource.CACertificate.getContentType(PKIResources.Format.X509_PEM_CA));
            out.print(PKIResources.Resource.CACertificate.toString(PKIResources.Format.X509_PEM_CA));
        }
    }

    protected void getSSHTrustV1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info(
            "SSH Trust request: source='{}', secured='{}'",
            request.getRemoteHost(),
            request.isSecure()
        );

        try (PrintWriter out = response.getWriter()) {
            response.setContentType(PKIResources.Resource.EngineCertificate.getContentType(PKIResources.Format.OPENSSH_PUBKEY));
            out.print(PKIResources.Resource.EngineCertificate.toString(PKIResources.Format.OPENSSH_PUBKEY));
        }
    }

    protected void doRegister(
        String hostAddress,
        int hostSSHPort,
        String hostSSHKeyFingerprint,
        String hostSSHUser,
        int hostVdsPort,
        String hostName,
        String hostUniqueId
    ) {
        if (hostSSHUser == null) {
            hostSSHUser = "root";
        }
        if (hostName == null) {
            hostName = hostAddress;
        }
        if (hostUniqueId == null) {
            throw new RuntimeException("Unique id was not provided");
        }

        VdcQueryReturnValue queryReturnValue  =  ((BackendInternal)EjbUtils.findBean(
            BeanType.BACKEND,
            BeanProxyType.LOCAL
        )).runInternalQuery(
            VdcQueryType.RegisterVds,
            new RegisterVdsParameters(
                Guid.Empty,
                hostAddress,
                hostSSHPort,
                hostSSHKeyFingerprint,
                hostSSHUser,
                hostName,
                hostUniqueId,
                hostVdsPort,
                Guid.Empty
            )
        );
        if (queryReturnValue == null) {
            throw new RuntimeException("runInternalQuery failed (null)");
        }

        if (!queryReturnValue.getSucceeded()) {
            String r = queryReturnValue.getExceptionString();
            if (r == null) {
                throw new RuntimeException("runInternalQuery failed (null)");
            }
            if (!r.equals(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE.name())) {
                throw new RuntimeException(String.format("runInternalQuery failed '%s'", r));
            }
        }
    }

    protected void registerV0(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String hostAddress = request.getParameter("vds_ip");
        String hostVdsPortString = request.getParameter("port");
        String hostName = request.getParameter("vds_name");
        String hostUniqueId = request.getParameter("vds_unique_id");

        if (hostAddress == null) {
            throw new RuntimeException("Missing vds_ip");
        }

        int hostVdsPort = VDSM_PORT;
        if (hostVdsPortString != null) {
            hostVdsPort = Integer.parseInt(hostVdsPortString);
        }

        log.info(
            "Registration request: source='{}', secured='{}', address='{}', vdsPort={}, name='{}', uniqueId='{}'",
            request.getRemoteHost(),
            request.isSecure(),
            hostAddress,
            hostVdsPort,
            hostName,
            hostUniqueId
        );

        if (hostUniqueId != null) {
            // remove legacy mac
            hostUniqueId = hostUniqueId.split("_")[0];
        }

        doRegister(
            hostAddress,
            SSH_PORT,
            null,
            null,
            hostVdsPort,
            hostName,
            hostUniqueId
        );

        response.setContentType("text/html");
        try (PrintWriter out = response.getWriter()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            out.print(format.format(new Date()));
        }
    }

    protected void registerV1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String hostAddress = request.getParameter("address");
        String hostSSHPortString = request.getParameter("sshPort");
        String hostSSHKeyFingerprint = request.getParameter("sshKeyFingerprint");
        String hostSSHUser = request.getParameter("sshUser");
        String hostVdsPortString = request.getParameter("vdsPort");
        String hostName = request.getParameter("name");
        String hostUniqueId = request.getParameter("uniqueId");

        if (hostAddress == null) {
            hostAddress = InetAddress.getByName(request.getRemoteHost()).getHostName();
        }

        int hostSSHPort = SSH_PORT;
        if (hostSSHPortString != null) {
            hostSSHPort = Integer.parseInt(hostSSHPortString);
        }

        int hostVdsPort = VDSM_PORT;
        if (hostVdsPortString != null) {
            hostVdsPort = Integer.parseInt(hostVdsPortString);
        }

        log.info(
            "Registration request: source='{}', secured='{}', address='{}{}:{}', sshKeyFingerprint={}, vdsPort={}, name='{}', uniqueId='{}'",
            request.getRemoteHost(),
            request.isSecure(),
            hostSSHUser != null ? hostSSHUser + "@" : "",
            hostAddress,
            hostSSHPort,
            hostSSHKeyFingerprint,
            hostVdsPort,
            hostName,
            hostUniqueId
        );

        doRegister(
            hostAddress,
            hostSSHPort,
            hostSSHKeyFingerprint,
            hostSSHUser,
            hostVdsPort,
            hostName,
            hostUniqueId
        );

        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");
            out.print("OK\n");
        }
    }

    protected void doV0(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            registerV0(request, response);
        }
        catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            log.debug("Exception", e);
            response.sendError(response.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doV1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String cmd = request.getParameter("command");
            if (cmd == null) {
                throw new RuntimeException("command parameter is missing");
            }

            if (cmd.equals("get-version")) {
                getVersionV1(request, response);
            }
            else if (cmd.equals("get-pki-trust")) {
                getPKITrustV1(request, response);
            }
            else if (cmd.equals("get-ssh-trust")) {
                getSSHTrustV1(request, response);
            }
            else if (cmd.equals("register")) {
                registerV1(request, response);
            }
        }
        catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            log.debug("Exception", e);
            response.sendError(response.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String versionString = request.getParameter("version");
        int version;
        if (versionString == null) {
            version = 0;
        }
        else {
            version = -1;
            try {
                version = Integer.parseInt(versionString);
            }
            catch(NumberFormatException e) {}
        }

        switch(version) {
            default:
                String m = String.format("Invalid registration protocol version %s", version);
                log.error(m);
                response.sendError(response.SC_BAD_REQUEST, m);
            break;

            case 0:
                doV0(request, response);
            break;

            case 1:
                doV1(request, response);
            break;
        }
    }
}
