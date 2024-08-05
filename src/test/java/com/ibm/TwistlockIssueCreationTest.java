package com.ibm;

import com.ibm.maas360.toolchain.EvidenceFolderReader;
import com.ibm.maas360.toolchain.twistlock.TwistlockAppOwnerFileReader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TwistlockIssueCreationTest
{
    @Test
    public void testAppOwnerFileRead() throws Exception
    {
        String testFilePath = CommonTestUtils.getTestFilePath("sample_twistlock_app_owners.log");
        Map<String, String> appNameOwnerMap = TwistlockAppOwnerFileReader.loadAppOwnersFromLog(testFilePath);

        Assert.assertEquals(9, appNameOwnerMap.size());
        Assert.assertNull(appNameOwnerMap.get("randomApp"));
        Assert.assertEquals("username1", appNameOwnerMap.get("test-app-3"));
        Assert.assertEquals("username2", appNameOwnerMap.get("test-app-5"));
        Assert.assertEquals("username4", appNameOwnerMap.get("test-app-9"));
    }

    @Test
    public void testEvidenceFolderReading() throws Exception
    {
        String sampleEvidenceFolder = CommonTestUtils.getTestFilePath("sample-evidences");
        List<Path> pathsList = EvidenceFolderReader.findAndCopyCSVFiles(sampleEvidenceFolder);

        Assert.assertEquals(6, pathsList.size());
        Assert.assertTrue(pathsList.get(0).toString().contains("community-insight-ws"));

        Map<String, Map<String, Object>> result = EvidenceFolderReader.prepareContentForUnifiedCSVFile(pathsList);

        Assert.assertEquals(8, result.size());
    }
}
