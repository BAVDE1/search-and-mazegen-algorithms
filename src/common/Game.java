package common;

import Interactables.ButtonGroup;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.Renderer;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Vec2;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class Game extends GameBase {
    public Window window = new Window();
    public Window.Options winOptions = new Window.Options();
    double timeStarted = 0;

    Vec2 mousePos = new Vec2();
    Vec2 mousePosOnClick = new Vec2();
    int[] heldMouseKeys = new int[8];
    int[] heldKeys = new int[350];

    TextRenderer.TextObject to1;
    TextRenderer textRenderer = new TextRenderer();

    ButtonGroup buttons = new ButtonGroup();

    @Override
    public void start() {
        this.timeStarted = (double)System.currentTimeMillis();
        TimeStepper.startTimeStepper(1f / 60f, this);
    }

    @Override
    public void createCapabilitiesAndOpen() {
        winOptions.initWindowSize = Constants.SCREEN_SIZE;
        winOptions.title = "searching my mind";
        window.setOptions(winOptions);

        window.setup();
        Renderer.setupGLContext();
        window.show();

        FontManager.init();
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 24, true);
        FontManager.generateAndBindAllFonts(Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);

        bindEvents();
        setupBuffers();
    }

    @Override
    public void mainLoop(double dt) {
        GLFW.glfwPollEvents();
        render();
    }

    @Override
    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.window.handle);
    }

    @Override
    public void close() {
        this.window.close();
    }

    public void bindEvents() {
        GLFW.glfwSetKeyCallback(this.window.handle, (window, key, scancode, action, mods) -> {
            if (action == 0) this.heldKeys[key] = 0;
            if (action == 1) this.heldKeys[key] = 1;
            if (key == GLFW_KEY_ESCAPE && action == 0) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }
        });
        GLFW.glfwSetMouseButtonCallback(this.window.handle, (window, button, action, mode) -> {
            if (action == 0) this.heldMouseKeys[button] = 0;
            if (action == 1) {
                this.heldMouseKeys[button] = 1;
                if (button == 0) this.mousePosOnClick.set(this.mousePos);
            }
        });
    }

    public void setupBuffers() {
        textRenderer.setupBufferObjects();
        to1 = new TextRenderer.TextObject(1, "some string", new Vec2(300), Color.CYAN, Color.DARK_GRAY);
        textRenderer.pushTextObject(to1);

        buttons.setupBufferObjects();
        buttons.addButton(new ButtonGroup.Button(new Vec2(50), new Vec2(50), "haha"));
    }

    public void render() {
        Renderer.clearScreen();
        buttons.renderAll();
        textRenderer.draw();
        Renderer.finish(window);
    }
}
