package org.ovirt.engine.core.register;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.queries.RegisterVdsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 2156012277778558480L;
    public static final String MASK = "255.255.255.0";
    public static final String VDS_IP = "vds_ip";
    public static final String VDS_NAME = "vds_name";
    public static final String VDS_ID = "vds_unique_id";
    public static final String PORT = "port";
    public static final String OTP = "ticket";

    private SimpleDateFormat m_sdfFormatter;
    private static Log log = LogFactory.getLog(RegisterServlet.class);

    @Override
    public void init() throws ServletException {
        // yyyy-MM-ddTHH:mm:ss
        m_sdfFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        m_sdfFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private VdcQueryReturnValue runQuery(HttpServletRequest request) {
        VdcQueryReturnValue fReturn = null;
        // TODO: should be BackendLocal
        BackendInternal backend = null;
        RegisterVdsParameters params = null;

        // Implement basic sanity: check each parameter exists
        String strIP = "";
        String strName = "";
        String strID = "";
        int nPort = -1;
        String otpMessage = "";

        try {
            backend = (BackendInternal) EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
            strIP = request.getParameterValues(VDS_IP)[0];
            strName = request.getParameterValues(VDS_NAME)[0];
            strID = request.getParameterValues(VDS_ID)[0];
            nPort = Integer.parseInt(request.getParameterValues(PORT)[0]);
            String[] otpValues = request.getParameterValues(OTP);
            String otpString = (otpValues != null && otpValues.length > 0) ? otpValues[0] : null;
            Long otp = null;

            // if OTP was provided, assume registering oVirt host
            if (StringUtils.isNotBlank(otpString)) {
                otp = Long.parseLong(otpString);
                otpMessage = ", OTP is set.";
            }

            log.debug("Using the following parameters to call query:\nIP: " + strIP + ", Name: "
                    + strName + ", UUID: " + strID + ", Port: " + nPort + otpMessage);

            /*
             * Ignore MAC if exists (old format)
             */
            String strIDNoMAC = strID.split("_")[0];
            params = new RegisterVdsParameters(Guid.Empty, strIP, strName, strIDNoMAC, nPort, MASK,
                    Guid.Empty, VDSType.oVirtNode);

            params.setOtp(otp);

            fReturn = backend.runInternalQuery(VdcQueryType.RegisterVds, params);
            if (fReturn == null) {
                log.error("Got NULL from backend.RunQuery!");
            }
        } catch (Throwable t) {
            log.error("Caught exception while trying to run query: ", t);
            log.error("Parameters used to call query:\nIP: " + strIP + ", Name: " + strName
                    + ", UUID: " + strID + ", Port: " + nPort + otpMessage);
            fReturn = null;
        }

        return fReturn;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            VdcQueryReturnValue runQuery = runQuery(request);
            if (runQuery != null && succeddedOrOther(runQuery)) {
                out.print(m_sdfFormatter.format(new Date()));
                log.info("Succeeded to run RegisterVds.");
            } else {
                response.setStatus(500);
                log.error("Failed to run RegisterVds.");
            }
        } catch (Exception e) {
            response.setStatus(500);
            log.error("Error calling runQuery: ", e);
        } finally {
            out.close();
        }
    }

    private boolean succeddedOrOther(VdcQueryReturnValue runQuery) {
        if (runQuery.getSucceeded()) {
            return true;
        } else if (VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE.name().equals(runQuery.getExceptionString())) {
            log.debug("host was't updated due to its status - ignoring and reporting succesful registration.");
            return true;
        } else {
            return false;
        }
    }
}
