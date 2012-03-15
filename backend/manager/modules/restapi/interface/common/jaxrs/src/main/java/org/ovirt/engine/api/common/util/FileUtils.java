package org.ovirt.engine.api.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtils {
    /**
     * Locates specified file in given package
     *
     * @param  packageName
     * @param  fileName
     * @throws IOException
     * @return InputStream
     */
    public static InputStream get(String packageName, String fileName) throws IOException {
        JarEntry jarEntry;
        String path = packageName.replace('.', '/');
        List<URL> dirs = ReflectionHelper.getDirectories(path);
        ClassLoader loader = URLClassLoader.newInstance(dirs.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());

        for (URL directory : dirs) {
            String resource = directory.getPath().replace("/" + path + "/", "");
            if (resource.endsWith(".jar")) {
                JarInputStream jarFileInputStream = null;
                try{
                    jarFileInputStream = new JarInputStream(new FileInputStream(resource));
                    while (true) {
                        jarEntry = jarFileInputStream.getNextJarEntry();
                        if (jarEntry == null)
                            break;
                        if (jarEntry.getName().equals(fileName)) {
                            InputStream str = loader.getResourceAsStream(loader.getResource(jarEntry.getName()).getFile());
                            if (str != null) {
                                return str;
                            }
                        }
                    }
                } finally {
                    closeQuietly(jarFileInputStream);
                }
            }
        }
        return null;
    }

    private static void closeQuietly(InputStream stream) {
        if(stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                //ignore exception
            }
        }
    }
}
