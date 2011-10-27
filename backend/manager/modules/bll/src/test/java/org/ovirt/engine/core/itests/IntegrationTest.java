package org.ovirt.engine.core.itests;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.bll.BackendRemote;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class IntegrationTest extends AbstractBackendTest {

    @Ignore
    @Test
    public void firstQuery() {
        List<storage_pool> nfsStoragePools = DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
        storage_pool pool = nfsStoragePools.get(0);
        List<storage_domains> storageDomain = DbFacade.getInstance()
                .getStorageDomainDAO().getAllForStoragePool(pool.getId());
        storage_domains domain = storageDomain.get(0);

        GetAllFromExportDomainQueryParamenters params = new GetAllFromExportDomainQueryParamenters(pool.getId(),
                domain.getid());
        VdcQueryReturnValue queryReturnValue = backend.runInternalQuery(VdcQueryType.GetVmsFromExportDomain, params);
        System.out.println("after");
    }

    // test as main
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            properties.put("java.naming.factory.url.pkgs", "=org.jboss.naming:org.jnp.interfaces");
            properties.put("java.naming.provider.url", "localhost:1099");
            Context context = new InitialContext(properties);
            BackendRemote backendRemote = (BackendRemote) context.lookup("engine/Backend/remote");

            List<storage_pool> nfsStoragePools =
                    DbFacade.getInstance().getStoragePoolDAO().getAllOfType(StorageType.NFS);
            storage_pool pool = nfsStoragePools.get(0);
            List<storage_domains> storageDomain = DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(
                    pool.getId());
            storage_domains domain = storageDomain.get(0);

            GetAllFromExportDomainQueryParamenters params = new GetAllFromExportDomainQueryParamenters(pool.getId(),
                    domain.getid());
            VdcQueryReturnValue queryReturnValue = backendRemote.RunQuery(VdcQueryType.GetVmsFromExportDomain,
                    params);
            System.out.println("after");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
