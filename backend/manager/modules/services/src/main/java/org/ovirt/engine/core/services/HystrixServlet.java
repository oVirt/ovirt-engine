package org.ovirt.engine.core.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

/**
 * The only purpose of this wrapper is to make all hystrix jars and its dependencies optional.
 */
public class HystrixServlet extends HttpServlet {

    private HystrixMetricsStreamServlet hystrixMetricsStreamServlet;

    @Override
    public void init() throws ServletException {
        if (Config.getValue(ConfigValues.HystrixMonitoringEnabled)) {
            hystrixMetricsStreamServlet = new HystrixMetricsStreamServlet();
            hystrixMetricsStreamServlet.init();
        }
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (Config.getValue(ConfigValues.HystrixMonitoringEnabled)) {
            hystrixMetricsStreamServlet.service(req, res);
        } else {
            super.service(req, res);
        }
    }

    @Override
    public void destroy() {
        if (Config.getValue(ConfigValues.HystrixMonitoringEnabled)) {
            hystrixMetricsStreamServlet.destroy();
        }
    }
}
