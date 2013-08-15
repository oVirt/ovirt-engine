package org.ovirt.engine.core.utils.servlet;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.branding.BrandingTheme;

@RunWith(MockitoJUnitRunner.class)
public class PageNotFoundServletTest {

    PageNotFoundServlet testServlet;

    @Mock
    HttpServletRequest mockRequest;
    @Mock
    HttpServletResponse mockResponse;

    @Before
    public void setUp() throws Exception {
        testServlet = new PageNotFoundServlet();

    }

    @Test
    public void testDoGetHttpServletRequestHttpServletResponse() throws ServletException, IOException {
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(PageNotFoundServlet.APPLICATION_TYPE,
                BrandingTheme.ApplicationType.PAGE_NOT_FOUND);
        verify(mockRequest).getRequestDispatcher(PageNotFoundServlet.FILE_NOT_FOUND_JSP);
    }

}
