package core;

import graphics.Render;
import scene.Scene;

public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene, Render render, float diffTimeMillis);

    void update(Window window, Scene scene, Render render);
}
