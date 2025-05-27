package de.oppa.mi4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class NativeLibraryLoader {
    static void loadMediaInfoLibrary() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String libName;
        if (os.contains("win")) {
            libName = arch.contains("64") ? "MediaInfo.dll" : "MediaInfo32.dll";
        } else if (os.contains("mac")) {
            libName = "libmediainfo.dylib";
        } else if (os.contains("nux") || os.contains("nix")) {
            libName = "libmediainfo.so";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        Path tempDir;
        Path libPath;

        try (InputStream in = NativeLibraryLoader.class.getResourceAsStream("/%s".formatted(libName))) {
            if (in == null) {
                throw new FileNotFoundException("Native library not found in resources: " + libName);
            }

            tempDir = Files.createTempDirectory("mediainfo");
            tempDir.toFile().deleteOnExit();
            libPath = tempDir.resolve(libName);

            try (OutputStream out = Files.newOutputStream(libPath)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }

        libPath.toFile().deleteOnExit();

        System.setProperty("jna.library.path", tempDir.toString());
    }
}
