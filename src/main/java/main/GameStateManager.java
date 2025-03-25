package main;

import core.Window;
import rendering.Render;
import scene.Scene;

public class GameStateManager {
    public enum GameState {
        MAIN_MENU,
        PLAYING,
        PAUSED,
        OPTIONS,
        EXITING
    }
    
    private GameState currentState;
    private final Scene scene;
    private final Render render;
    private final Window window;
    
    public GameStateManager(Window window, Scene scene, Render render) {
        this.window = window;
        this.scene = scene;
        this.render = render;
        this.currentState = GameState.PLAYING;
    }
    
    public void update(float diffTimeMillis) {
        switch (currentState) {
            case PLAYING:
                updatePlaying(diffTimeMillis);
                break;
            case PAUSED:
                updatePaused(diffTimeMillis);
                break;
            case MAIN_MENU:
                updateMainMenu(diffTimeMillis);
                break;
            case OPTIONS:
                updateOptions(diffTimeMillis);
                break;
            case EXITING:
                break;
        }
    }
    
    private void updatePlaying(float diffTimeMillis) {
        float playerX = scene.getCamera().getPosition().x;
        float playerZ = scene.getCamera().getPosition().z;
        
        scene.updateWorldGeneration(playerX, playerZ);
        render.updateBlockOutline(scene);
    }
    
    private void updatePaused(float diffTimeMillis) {
    }
    
    private void updateMainMenu(float diffTimeMillis) {
    }
    
    private void updateOptions(float diffTimeMillis) {
    }
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(GameState newState) {
        this.currentState = newState;
    }
    
    public void togglePause() {
        if (currentState == GameState.PLAYING) {
            setCurrentState(GameState.PAUSED);
        } else if (currentState == GameState.PAUSED) {
            setCurrentState(GameState.PLAYING);
        }
    }
}
