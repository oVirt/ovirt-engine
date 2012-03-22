package org.ovirt.engine.core.itests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class RepoIsoFileTest extends AbstractBackendTest{

    @Test
    public void testToolsRegexValidation() {
        //getBasicSetup();
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_2.3_275.iso"));
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_2.2_46770.iso"));
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_3.0_1.iso"));
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_2.3_99999.iso"));

        Assert.assertFalse(isValidToolName(""));

        Assert.assertFalse(isValidToolName(null));

        // "_____" instead of "_"
        Assert.assertFalse(isValidToolName("RHEV-toolsSetup_____2.3_1.iso"));

        // No '_' before cluster version.
        Assert.assertFalse(isValidToolName("RHEV-toolsSetup+2.3_99999.iso"));

        // No '_' after cluster version.
        Assert.assertFalse(isValidToolName("RHEV-toolsSetup_2.399999.iso"));

        // Case sensitive check (setup instead of Setup).
        Assert.assertFalse(isValidToolName("RHEV-toolssetup_2.3.iso"));

        // No dot before iso (.iso)
        Assert.assertFalse(isValidToolName("tooSetup+2.3_99999iso"));

        // No tool name.
        Assert.assertFalse(isValidToolName("_2.3_99999.iso"));

        // No tool name.
        Assert.assertFalse(isValidToolName("RHEV-toolsSetup_2.3_9.iso2"));

        // Check case sensitive
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_2.3_99999.Iso"));

        // Check case sensitiv
        Assert.assertTrue(isValidToolName("RHEV-toolsSetup_2.3_99999.ISO"));
    }

    @Test
    public void testToolsClusterVersionValidation() {
        Assert.assertEquals(getToolCluster("RHEV-toolsSetup_2.3_275.iso"), "2.3");
        Assert.assertEquals(getToolCluster("RHEV-toolsSetup_2.2_275.iso"), "2.2");
        Assert.assertEquals(getToolCluster("RHEV-toolsSetup_9.9_275.iso"), "9.9");
    }

    @Test
    public void testToolVersionValidation() {
        Assert.assertEquals(getToolVersion("RHEV-toolsSetup_2.3_275.iso"), new Integer("275").intValue());
        Assert.assertEquals(getToolVersion("RHEV-toolsSetup_2.3_2.iso"), new Integer("2").intValue());
    }

    @Test
    public void testFaultGuid() {
        /**
        List<RepoFileMetaData> fileList =
                IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList(Guid.NewGuid(), true);

        // Wrong Guid should return an empty list.
        Assert.assertEquals(true, fileList.isEmpty());

        fileList = IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList(null, true);
        Assert.assertEquals(true, fileList.isEmpty());
        **/
    }

    @Test
    public void testStorageDomainRepoFileList() {
        IsoDomainListSyncronizer.getInstance().refresheIsoDomainWhenActivateDomain(null, null);

        // Wrong Guid shoud return an empty list.
        //Assert.assertEquals(true, repoFileMetaData.isEmpty());

        // simulate fetch from Iso domain.
        IsoDomainListSyncronizer.getInstance().refresheIsoDomainWhenActivateDomain(Guid.NewGuid(), null);
        //Assert.assertEquals(true, repoFileMetaData.isEmpty());

        IsoDomainListSyncronizer.getInstance().refresheIsoDomainWhenActivateDomain(null, Guid.NewGuid());
        //Assert.assertEquals(true, repoFileMetaData.isEmpty());
    }

    private boolean isValidToolName(String toolName)
    {
        Pattern pattern  = Pattern.compile(IsoDomainListSyncronizer.getRegexToolPattern());
        String toolNameValid = toolName != null ? toolName : new String();
        Matcher matchToolPattern = pattern.matcher(toolNameValid);
        return matchToolPattern.find();
    }

    private String getToolCluster(String toolName)
    {
        Pattern pattern  = Pattern.compile(IsoDomainListSyncronizer.getRegexToolPattern());
        Matcher matchToolPattern = pattern.matcher(toolName);
        if (matchToolPattern.find())
        {
            Version clusterVer = new Version(matchToolPattern.group(1));
            return clusterVer.getValue();
        }
        return null;
    }

    private int getToolVersion(String toolName)
    {
        Pattern pattern  = Pattern.compile(IsoDomainListSyncronizer.getRegexToolPattern());
        Matcher matchToolPattern = pattern.matcher(toolName);
        if (matchToolPattern.find())
        {
            int toolVer = new Integer(matchToolPattern.group(3)).intValue();
            return toolVer;
        }
        return -1;
    }
}
