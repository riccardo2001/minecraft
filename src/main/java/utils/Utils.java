package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import core.Main;

public class Utils {

    public static String readFile(String filePath) {
        String str = null;

        try {
            InputStream inputStream = readFromClasspath(filePath);

            if (inputStream == null) {
                inputStream = new FileInputStream(filePath);
            }

            str = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }

        return str;
    }

    private static InputStream readFromClasspath(String filePath) {
        return Main.class.getClassLoader().getResourceAsStream(filePath);
    }

}
