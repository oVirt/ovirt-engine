package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.branding.BrandingTheme;

/**
 * This Servlet renders a branded 404 error page.
 */
public class PageNotFoundServlet extends HttpServlet {
    /**
     * serialVersionIUD.
     */
    private static final long serialVersionUID = 854039610705171243L;

    /**
     * File not found jsp.
     */
    static final String FILE_NOT_FOUND_JSP = "/404.jsp";

    /**
     * Application type attribute key.
     */
    static final String APPLICATION_TYPE = "applicationType";

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
            ServletException {
        request.setAttribute(APPLICATION_TYPE, BrandingTheme.ApplicationType.PAGE_NOT_FOUND);
        RequestDispatcher dispatcher = request.getRequestDispatcher(FILE_NOT_FOUND_JSP);
        response.setContentType("text/html;charset=UTF-8");
        if (dispatcher != null) {
            dispatcher.include(request, response);
        }
    }

}
