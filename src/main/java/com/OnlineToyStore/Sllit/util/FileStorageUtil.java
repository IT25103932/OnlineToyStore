package com.OnlineToyStore.Sllit.util;

import java.io.File;
import java.io.IOException;

public final class FileStorageUtil {

    private FileStorageUtil() {
    }

    public static File ensureDataFile(String dataFilePath, String fileName) throws IOException {
        File directory = new File(dataFilePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static String ensureDataFilePath(String dataFilePath, String fileName) {
        try {
            return ensureDataFile(dataFilePath, fileName).getPath();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare data file: " + fileName, e);
        }
    }
}
