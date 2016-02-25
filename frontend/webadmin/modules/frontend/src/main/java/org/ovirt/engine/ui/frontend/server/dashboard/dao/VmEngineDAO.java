package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.VmStatusMap;

public class VmEngineDAO {
    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String VM_INVENTORY = "vm.inventory"; //$NON-NLS-1$

    private final DataSource engineDataSource;
    private final Properties vmProperties;

    public VmEngineDAO(DataSource engineDataSource) throws DashboardDataException {
        this.engineDataSource = engineDataSource;
        vmProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("VmEngineDAO.properties")) { //$NON-NLS-1$
            vmProperties.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load DAO queries"); //$NON-NLS-1$
        }
    }

    public InventoryStatus getVmInventoryStatus() throws SQLException {
        InventoryStatus result = new InventoryStatus();
        try (Connection con = engineDataSource.getConnection();
                PreparedStatement vmStatusPS = con.prepareStatement(vmProperties.getProperty(VM_INVENTORY));
                ResultSet vmStatusRS = vmStatusPS.executeQuery()) {
            while (vmStatusRS.next()) {
                processVmStatus(result, vmStatusRS.getInt(STATUS));
            }
        }
        return result;
    }

    private InventoryStatus processVmStatus(InventoryStatus summary, int status) {
        summary.addCount();
        if (VmStatusMap.WARNING.isType(status)) {
            summary.addStatus(VmStatusMap.WARNING.name().toLowerCase());
        } else if (VmStatusMap.DOWN.isType(status)) {
            summary.addStatus(VmStatusMap.DOWN.name().toLowerCase());
        } else {
            summary.addStatus(VmStatusMap.UP.name().toLowerCase());
        }
        return summary;
    }

}
