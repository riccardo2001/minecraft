package core;

import graphics.Render;
import scene.Scene;

public class Engine {
    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private Scene scene;
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

    private void cleanup() {
        appLogic.cleanup();
        window.cleanup();
        render.cleanup();
        scene.cleanup();
    }

    private void resize() {
        scene.resize(window.getWidth(), window.getHeight());
    }

    private void run() {
        long initialTime = System.nanoTime(); // Usa nanoTime per la precisione in nanosecondi
        float timeU = 1e9f / targetUps; // Tempo per aggiornamento in nanosecondi
        float timeR = targetFps > 0 ? 1e9f / targetFps : 0; // Tempo per frame in nanosecondi
        float deltaUpdate = 0;
        float deltaFps = 0;

        int fps = 0;
        int fpsCount = 0;
        long fpsTime = System.nanoTime(); // Usa nanoTime per il conteggio degli FPS

        long updateTime = initialTime;
        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.nanoTime(); // Ottieni il tempo corrente in nanosecondi
            long deltaTime = now - initialTime; // Differenza di tempo in nanosecondi
            deltaUpdate += deltaTime / timeU; // Calcola deltaUpdate
            deltaFps += deltaTime / timeR; // Calcola deltaFps

            if (targetFps <= 0 || deltaFps >= 1) {
                window.getMouseInput().input();
                appLogic.input(window, scene, deltaTime / 1e6f); // Converti in millisecondi per l'input
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = (now - updateTime) / 1000000L; // Calcola la differenza in millisecondi
                appLogic.update(window, scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFps <= 0 || deltaFps >= 1) {
                render.render(window, scene);
                window.update();

                // Incrementa il contatore dei frame
                fpsCount++;

                // Aggiorna il valore degli FPS ogni secondo
                if (now - fpsTime >= 1e9) { // 1 secondo in nanosecondi
                    fps = fpsCount;
                    fpsCount = 0;
                    fpsTime = now;
                }
            }
            initialTime = now;

            String baseTitle = "Minecraft ";
            window.setTitle(baseTitle + " | FPS: " + fps);
        }

        cleanup();
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}
