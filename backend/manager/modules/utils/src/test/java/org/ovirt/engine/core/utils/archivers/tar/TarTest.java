package org.ovirt.engine.core.utils.archivers.tar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

public class TarTest {

    private void writeFile(File file, String content, boolean executable) throws IOException {
        File f = file.getParentFile();
        if (f == null) {
            throw new IOException("File not found " + f);
        }
        if (!f.mkdirs()) {
            // void
        }
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(content.getBytes("UTF-8"));
            file.setExecutable(executable);
        }
    }

    private void digestDirectory(MessageDigest md, File base, File file) throws Exception {
        File fullFile = new File(base, file.getPath());
        md.update(file.getPath().getBytes("UTF-8"));
        if (fullFile.isDirectory()) {
            String[] files = fullFile.list();
            Arrays.sort(files);
            for (String f : files) {
                digestDirectory(md, base, new File(file, f));
            }
        }
        else {
            MessageDigest fmd = MessageDigest.getInstance(md.getAlgorithm());
            InputStream fis = null;
            InputStream is = null;
            try {
                fis = new FileInputStream(fullFile);
                is = new DigestInputStream(fis, fmd);
                byte[] buf = new byte[1024];
                int n;
                while ((n = is.read(buf)) != -1) {
                    // do nothing
                }
                md.update(fmd.digest());
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        // ignore
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    @Test
    public void testSimple() throws Exception {
        assumeTrue(SystemUtils.IS_OS_UNIX);

        File tmpTar = null;
        File tmpDir1 = null;
        File tmpDir2 = null;

        try {
            tmpTar = File.createTempFile("test1", "tar");
            tmpDir1 = File.createTempFile("test1", "tmp");
            if (!tmpDir1.delete()) {
                throw new IOException("Cannot delete " + tmpDir1);
            }
            if (!tmpDir1.mkdir()) {
                throw new IOException("Cannot create " + tmpDir1);
            }
            tmpDir2 = File.createTempFile("test1", "tmp");
            if (!tmpDir2.delete()) {
                throw new IOException("Cannot delete " + tmpDir2);
            }
            if (!tmpDir2.mkdir()) {
                throw new IOException("Cannot create " + tmpDir2);
            }

            writeFile(new File(tmpDir1, "script1"), "script1", true);
            writeFile(new File(tmpDir1, "script2"), "script2", true);
            writeFile(new File(tmpDir1, "file1"), "file1", false);
            writeFile(new File(tmpDir1, "file2"), "file2", false);
            writeFile(new File(tmpDir1, "dir1/file3"), "file2", false);
            writeFile(new File(tmpDir1, "dir1/dir2/file4"), "file4", false);

            OutputStream os = null;
            try {
                os = new FileOutputStream(tmpTar);
                Tar.doTar(os, tmpDir1);
            }
            finally {
                if (os != null) {
                    try {
                        os.close();
                    }
                    catch(IOException e) {
                        // ignore
                    }
                }
            }

            new ProcessBuilder("tar", "-C", tmpDir2.getPath(), "-xf", tmpTar.getPath()).start().waitFor();

            MessageDigest md1 = MessageDigest.getInstance("SHA");
            MessageDigest md2 = MessageDigest.getInstance("SHA");
            digestDirectory(md1, tmpDir1, new File("."));
            digestDirectory(md2, tmpDir2, new File("."));

            assertArrayEquals(md1.digest(), md2.digest());
            assertTrue(new File(tmpDir2, "script1").canExecute());
            assertFalse(new File(tmpDir2, "file1").canExecute());
        }
        finally {
            for (File file : new File[] {tmpDir1, tmpDir2, tmpTar}) {
                if (file != null) {
                    if (!file.delete()) {
                        // void
                    }
                }
            }
        }
    }

    @Test(expected=FileNotFoundException.class)
    public void testNoBase() throws IOException {
        Tar.doTar(new ByteArrayOutputStream(), new File("/asdasdsadasdasdsa"));
    }
}
