package org.ovirt.engine.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a very simple servlet used to make sure that errors result in
 * redirection (instead of forward) to the index page.
 */
public class ErrorServlet extends HttpServlet {
    // Serialization id:
    private static final long serialVersionUID = -8287468982176580929L;

    @Override
    protected void service (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("/");
    }
}
