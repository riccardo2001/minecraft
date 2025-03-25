package main;

import scene.Scene;
import core.Engine;
import core.IAppLogic;
import core.Window;
import input.InputHandler;
import rendering.Render;
import rendering.ui.TextRenderer;
import utils.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main implements IAppLogic {
    private InputHandler inputHandler;
    private GameStateManager gameStateManager;
    
    public static void main(String[] args) {
        GameConfig config = GameConfig.getInstance();
        
        Main main = new Main();
        
        Window.WindowOptions opts = config.createWindowOptions();
        
        Engine gameEng = new Engine(config.getWindowTitle(), opts, main);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        initWorld(scene);
        initPlayer(scene);
        initUI(window);
        initInputHandler();
        
        gameStateManager = new GameStateManager(window, scene, render);
        
        logSystemInfo();
    }
    
    private void initWorld(Scene scene) {
        GameConfig config = GameConfig.getInstance();
        scene.getWorld().generateInitialWorld(
                config.getInitialWorldX(), 
                config.getInitialWorldZ()
        );
        Logger.info("World generated");
    }
    
    private void initPlayer(Scene scene) {
        GameConfig config = GameConfig.getInstance();
        scene.getCamera().setPosition(
                config.getInitialWorldX(),
                config.getInitialPlayerY(),
                config.getInitialWorldZ()
        );
        Logger.info("Player positioned");
    }
    
    private void initUI(Window window) {
        window.setTextRenderer(new TextRenderer());
        Logger.info("UI inizialized");
    }
    
    private void initInputHandler() {
        inputHandler = new InputHandler();
        Logger.info("Input handler inizialized");
    }
    
    private void logSystemInfo() {
        Logger.info("OpenGL Vendor: " + glGetString(GL_VENDOR));
        Logger.info("OpenGL Renderer: " + glGetString(GL_RENDERER));
        Logger.info("OpenGL Version: " + glGetString(GL_VERSION));
    }

    @Override
    public void input(Window window, Scene scene, Render render, float diffTimeMillis) {
        switch (gameStateManager.getCurrentState()) {
            case PLAYING:
                inputHandler.handleInput(window, scene, diffTimeMillis);
                
                if (window.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
                    gameStateManager.togglePause();
                }
                
                if (window.isKeyJustPressed(GLFW_KEY_F3)) {
                    render.getSceneRender().setUseCoordinates(
                            !render.getSceneRender().isUsingCoordinates()
                    );
                }
                break;
                
            case PAUSED:
                if (window.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
                    gameStateManager.togglePause();
                }
                break;
                
            default:
                break;
        }
    }

    @Override
    public void update(Window window, Scene scene, Render render) {
        gameStateManager.update(0);
    }

    @Override
    public void cleanup() {
        Logger.info("Cleaning resources...");
    }
}