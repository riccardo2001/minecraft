package utils;

import java.io.*;
import core.Main;

public class Utils {
    public static String readFile(String filePath) {
        try {
            InputStream in = Main.class.getClassLoader().getResourceAsStream(filePath);
            if (in == null) {
                in = new FileInputStream(filePath);
            }
            String content = new String(in.readAllBytes());
            in.close();
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}