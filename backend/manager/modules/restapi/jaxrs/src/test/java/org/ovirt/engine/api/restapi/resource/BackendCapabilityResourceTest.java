package org.ovirt.engine.api.restapi.resource;

import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendCapabilityResourceTest extends AbstractBackendResourceTest {

    BackendCapabilitiesResource parent;
    BackendCapabilityResource resource;

    private static final Version VERSION_2_3 = new Version() {
        {
            major = 2;
            minor = 3;
        }
    };

    public BackendCapabilityResourceTest() {
        parent = new BackendCapabilitiesResource();
        resource = new BackendCapabilityResource(parent.generateId(VERSION_2_3), parent);
    }

    @Ignore
    @Test
    public void testGet() throws Exception {
        HashSet<org.ovirt.engine.core.compat.Version> supportedVersions =
                new HashSet<>();
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(1, 5));
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(10, 3));

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "ConfigValue" },
                new Object[] { ConfigurationValues.SupportedClusterLevels },
                supportedVersions);

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.PredefinedVMProperties },
                "");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.UserDefinedVMProperties },
                "");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.PredefinedVMProperties },
                "foo=true|false");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.UserDefinedVMProperties },
                "bar=[a-z]");

        verifyCapabilities(resource.get());
    }

    private void verifyCapabilities(VersionCaps capabilities) {
        assertNotNull(capabilities);
    }

    @Override
    protected Object getEntity(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void init() {
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }
}
