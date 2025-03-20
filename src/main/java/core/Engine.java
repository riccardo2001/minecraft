package core;

import graphics.Render;
import scene.Scene;

public class Engine {
    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private final Render render;
    private final Scene scene;
    private boolean running;
    private int targetFps;
    private int targetUps;

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic) {
        window = new Window(windowTitle, opts, () -> {
            resize();
            return null;
        });
        targetFps = opts.fps;
        targetUps = opts.ups;
        render = new Render();
        scene = new Scene(window.getWidth(), window.getHeight());
        this.appLogic = appLogic;
        this.appLogic.init(window, scene, render);
        running = true;
    }

    private void run() {
        final double nsPerUpdate = 1e9 / targetUps;
        final double nsPerFrame = targetFps > 0 ? 1e9 / targetFps : 0;

        long lastTime = System.nanoTime();
        long lastUpdateTime = lastTime;
        long lastFrameTime = lastTime;
        int frames = 0;
        long fpsTimer = lastTime;

        while (running && !window.windowShouldClose()) {
            window.pollEvents();
            long now = System.nanoTime();

            if (now - lastUpdateTime >= nsPerUpdate) {
                appLogic.update(window, scene, render);
                lastUpdateTime = now;
            }

            if (targetFps <= 0 || now - lastFrameTime >= nsPerFrame) {
                long frameDiffMillis = (now - lastFrameTime) / 1_000_000L;
                appLogic.input(window, scene, (float) frameDiffMillis);
                render.render(window, scene);
                window.update();
                frames++;
                lastFrameTime = now;
            }

            if (now - fpsTimer >= 1e9) {
                window.setTitle("Minecraft | FPS: " + frames);
                frames = 0;
                fpsTimer = now;
            }

            lastTime = now;
        }
        cleanup();
    }

    private void cleanup() {
        appLogic.cleanup();
        window.cleanup();
        render.cleanup();
        scene.cleanup();
    }

    private void resize() {
        scene.resize(window.getWidth(), window.getHeight());
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}