package org.ovirt.engine.core.utils.hostinstall;

import junit.framework.TestCase;

public class IPWorksInstallWrapperTest extends TestCase {
    public static String HOSTNAME = "hattrick.usersys.redhat.com";
    public static String ROOTPASS = "thincrust";

    public void testFoo() {
        // NOOP
    }

    /*
     * public void testLoginToHattrick() { IPWorksInstallWrapper wrapper = new IPWorksInstallWrapper() ;
     * wrapper.ConnectToServer(HOSTNAME, ROOTPASS) ; assertTrue(wrapper.RunSSHCommand("ls -lah")) ; }
     *
     * public void testFailCommand() { IPWorksInstallWrapper wrapper = new IPWorksInstallWrapper() ;
     * wrapper.ConnectToServer(HOSTNAME, ROOTPASS) ; assertFalse(wrapper.RunSSHCommand("lt -lah")) ; }
     *
     * public void testUpload() throws Exception { File file = File.createTempFile("foo", "txt") ; IPWorksInstallWrapper
     * wrapper = new IPWorksInstallWrapper() ; wrapper.ConnectToServer(HOSTNAME, ROOTPASS) ;
     * assertTrue("This should work", wrapper.UploadFile(file.getAbsolutePath(), "/tmp/" + file.getName())) ; }
     *
     * public void testUploadFailure() throws Exception { File file = File.createTempFile("foo", "txt") ;
     * IPWorksInstallWrapper wrapper = new IPWorksInstallWrapper() ; wrapper.ConnectToServer(HOSTNAME, ROOTPASS) ;
     * assertFalse(wrapper.UploadFile(file.getAbsolutePath(), "/tmpjjj/" + file.getName())) ; }
     *
     * public void testDownload() throws Exception { File file = File.createTempFile("foo", "txt") ;
     * IPWorksInstallWrapper wrapper = new IPWorksInstallWrapper() ; wrapper.ConnectToServer(HOSTNAME, ROOTPASS) ;
     * assertTrue("This should work", wrapper.DownloadFile("/root/.bashrc", file.getAbsolutePath())) ; }
     */

    public void XXXtestLoginToGitET() {
        IPWorksInstallWrapper wrapper = new IPWorksInstallWrapper();
        wrapper.ConnectToServer("www.et.redhat.com", "/home/bkearney/.ssh/id_rsa", "NOWAY!");
        wrapper.RunSSHCommand("ls -lah");
    }
}
