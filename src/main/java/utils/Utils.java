package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import core.Main;

public class Utils {
    private Utils() {
        // Utility class
    }

    public static String readFile(String filePath) {
        String str = null;

        // Controlla se il file è una risorsa nel classpath (interno al JAR) o un file
        // esterno
        try {
            // Prova a leggere il file come risorsa nel JAR (o classe di classpath)
            InputStream inputStream = readFromClasspath(filePath);

            if (inputStream == null) {
                // Se non è stato trovato nel classpath, prova a leggere come file esterno
                inputStream = new FileInputStream(filePath);
            }

            // Leggi il contenuto del file
            str = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }

        return str;
    }

    private static InputStream readFromClasspath(String filePath) {
        // Usa il ClassLoader per cercare la risorsa nel classpath (incluso JAR)
        return Main.class.getClassLoader().getResourceAsStream(filePath);
    }

}
