package org.ovirt.engine.core.utils.archivers.tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * A simple recursive tar based on javatar.
 */
public class Tar {

    private static void recurse(
        TarArchiveOutputStream archive,
        File file,
        String base
    ) throws SecurityException, IOException {

        TarArchiveEntry entry = (TarArchiveEntry)archive.createArchiveEntry(
            file,
            base
        );
        if (entry.getFile().isDirectory()) {
            entry.setMode(0700);
            archive.putArchiveEntry(entry);
            archive.closeArchiveEntry();
            String[] names = entry.getFile().list();
            if (names != null) {
                for (String f : names) {
                    recurse(
                        archive,
                        new File(entry.getFile(), f),
                        new File(entry.getName(), f).getPath()
                    );
                }
            }
        }
        else if (entry.getFile().isFile()) {
            if (entry.getFile().canExecute()) {
                entry.setMode(0700);
            }
            else {
                entry.setMode(0600);
            }
            archive.putArchiveEntry(entry);
            try (InputStream is = new FileInputStream(entry.getFile())) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) != -1) {
                    archive.write(buffer, 0, n);
                }
            }
            archive.closeArchiveEntry();
        }
    }

    /**
     *  Crete tar.
     *  @param os output stream to write into.
     *  @param base base directory.
     *
     *  Only regular files and directories are supported.
     *  Files will be owner rw and optional execute bit.
     */
    public static void doTar(
        OutputStream os,
        File base
    ) throws SecurityException, IOException {

        if (!base.exists()) {
            throw new FileNotFoundException(
                String.format(
                    "File or directory %1$s not found",
                    base
                )
            );
        }

        try (TarArchiveOutputStream archive = new TarArchiveOutputStream(os)) {
            // TODO: use LONGFILE_POSIX in newer version of commons-compress
            archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            recurse(archive, base, "./");
        }
    }

    public static void main(String[] args) throws Exception {
        try (OutputStream os = new FileOutputStream(args[0])) {
            Tar.doTar(os, new File(args[1]));
        }
    }
}
