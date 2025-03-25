package utils;

import org.apache.logging.log4j.LogManager;

public class Logger {
    final static org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class);

    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    private static LogLevel currentLevel = LogLevel.INFO;
    
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
        switch (level) {
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARNING:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }
}
