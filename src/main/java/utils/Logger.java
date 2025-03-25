package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    private static LogLevel currentLevel = LogLevel.INFO;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
    }
    
    public static void debug(String message) {
        if (currentLevel.ordinal() <= LogLevel.DEBUG.ordinal()) {
            log(LogLevel.DEBUG, message);
        }
    }
    
    public static void info(String message) {
        if (currentLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            log(LogLevel.INFO, message);
        }
    }
    
    public static void warning(String message) {
        if (currentLevel.ordinal() <= LogLevel.WARNING.ordinal()) {
            log(LogLevel.WARNING, message);
        }
    }
    
    public static void error(String message) {
        if (currentLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            log(LogLevel.ERROR, message);
        }
    }
    
    private static void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "][" + level + "] " + message);
    }
}
