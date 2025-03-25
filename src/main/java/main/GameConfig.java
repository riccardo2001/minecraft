package main;

import core.Engine;
import core.Window;


public class GameConfig {
    // Configurazione finestra
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private int targetFps = 1000;
    private int targetUps = Engine.TARGET_UPS;
    private boolean compatibleProfile = false;
    private String windowTitle = "Minecraft";
    
    // Configurazione del mondo
    private float initialWorldX = 0f;
    private float initialWorldZ = 0f;
    private float initialPlayerY = 75f;
    
    // Singleton pattern
    private static GameConfig instance;
    
    private GameConfig() {
    }
    
    public static synchronized GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    public Window.WindowOptions createWindowOptions() {
        Window.WindowOptions opts = new Window.WindowOptions();
        opts.width = windowWidth;
        opts.height = windowHeight;
        opts.fps = targetFps;
        opts.ups = targetUps;
        opts.compatibleProfile = compatibleProfile;
        return opts;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public float getInitialWorldX() {
        return initialWorldX;
    }

    public float getInitialWorldZ() {
        return initialWorldZ;
    }

    public float getInitialPlayerY() {
        return initialPlayerY;
    }
}
