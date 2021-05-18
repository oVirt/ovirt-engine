package org.ovirt.engine.api.restapi.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.ExternalTemplateImport;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromExternalUrlParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalTemplateImportsResourceTest extends AbstractBackendBaseTest {

    private static final Guid CLUSTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[1];
    private static final Guid HOST_ID = GUIDS[2];

    private static final String OVA_URL = "ova:///mnt/ova/template_ova.ova";
    private static final String CREATED_TEMPLATE_NAME = "new-template";

    private final BackendExternalTemplateImportsResource resource = new BackendExternalTemplateImportsResource();

    @Override
    protected void init() {
        initBackendResource(resource);
        resource.setMappingLocator(mapperLocator);
    }

    @BeforeEach
    public void setup() {
        setUpBasicUriExpectations("/externaltemplateimports");
    }

    @Test
    public void testOvaImport() {
        VmTemplate externalTemplate = new VmTemplate();

        setUpActionExpectations(ActionType.ImportVmTemplateFromExternalUrl,
                ImportVmTemplateFromExternalUrlParameters.class,
                new String[] {
                        "StorageDomainId",
                        "ClusterId",
                        "Url",
                        "ProxyHostId",
                        "NewTemplateName"
                },
                new Object[] {
                        STORAGE_DOMAIN_ID,
                        CLUSTER_ID,
                        OVA_URL,
                        HOST_ID,
                        CREATED_TEMPLATE_NAME
                },
                true,
                true,
                externalTemplate,
                true);

        ExternalTemplateImport templateImport = createTemplateImport();
        resource.add(templateImport);
    }

    private ExternalTemplateImport createTemplateImport() {
        ExternalTemplateImport templateImport = new ExternalTemplateImport();
        templateImport.setTemplate(new Template());
        templateImport.getTemplate().setName(CREATED_TEMPLATE_NAME);
        templateImport.setCluster(new Cluster());
        templateImport.getCluster().setId(CLUSTER_ID.toString());
        templateImport.setStorageDomain(new StorageDomain());
        templateImport.getStorageDomain().setId(STORAGE_DOMAIN_ID.toString());
        templateImport.setHost(new Host());
        templateImport.getHost().setId(HOST_ID.toString());
        templateImport.setUrl(OVA_URL);

        return templateImport;
    }
}
