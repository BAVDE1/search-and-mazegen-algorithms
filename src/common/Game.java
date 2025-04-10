package common;

import Interactables.*;
import Interactables.Button;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Vec2;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game extends GameBase {
    public Window window = new Window();
    public Window.Options winOptions = new Window.Options();
    double timeStarted = 0;

    Vec2 mousePos = new Vec2();
    Vec2 mousePosOnClick = new Vec2();
    int[] heldMouseKeys = new int[8];
    int[] heldKeys = new int[350];

    TextRenderer textRenderer = new TextRenderer();
    ButtonGroup algorithmButtons = new ButtonGroup();
    InputGroup inputGroup = new InputGroup();

    private final ShaderHelper separatorSh = new ShaderHelper();
    private final VertexArray separatorVa = new VertexArray();
    private final VertexBuffer separatorVb = new VertexBuffer();
    private final BufferBuilder2f separatorSb = new BufferBuilder2f();

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
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 18, true);
        FontManager.loadFont(FontManager.FONT_TINY, Font.PLAIN, 19, false);
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
            if (action == 0) {
                this.heldKeys[key] = 0;
                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
            if (action == 1) {
                this.heldKeys[key] = 1;
                inputGroup.keyPressed(key, scancode);
            }
        });
        GLFW.glfwSetMouseButtonCallback(this.window.handle, (window, button, action, mode) -> {
            if (action == 0) this.heldMouseKeys[button] = 0;
            if (action == 1) {
                this.heldMouseKeys[button] = 1;
                if (button == 0) this.mousePosOnClick.set(this.mousePos);
                algorithmButtons.mouseClicked();
                inputGroup.mouseClicked();
            }
        });
        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> {
            mousePos.set((float) xPos, (float) yPos);
            algorithmButtons.updateMouse(mousePos);
            inputGroup.updateMouse(mousePos);
        });
    }

    public void setupBuffers() {
        // text
        textRenderer.setupBufferObjects();
        TextRenderer.TextObject title = new TextRenderer.TextObject(1, "...ssearchingg...", new Vec2(20, 40));
        TextRenderer.TextObject searchTypeText = new TextRenderer.TextObject(1, "algorithm", new Vec2(25, 115));
        title.setTextColour(Color.YELLOW);
        searchTypeText.setTextColour(Color.YELLOW);
        textRenderer.pushTextObject(title, searchTypeText);

        // buttons
        algorithmButtons.setupBufferObjects();
        algorithmButtons.radioToggles = true;
        ToggleButton df = new ToggleButton(new Vec2(25, 160), new Vec2(200, 40), "depth first", Color.YELLOW);
        Button bf = new ToggleButton(new Vec2(25, 220), new Vec2(200, 40), "breadth first", Color.YELLOW);
        Button gbf = new ToggleButton(new Vec2(25, 280), new Vec2(200, 40), "greedy best first", Color.YELLOW);
        Button as = new ToggleButton(new Vec2(25, 340), new Vec2(200, 40), "a star", Color.YELLOW);
        algorithmButtons.addButton(df, bf, as, gbf);
        algorithmButtons.toggleBtn(df, true);

        // inputs
        inputGroup.setupBufferObjects();
        Input i = new Input(new Vec2(400), "hahaha", "0");
        Input i2 = new Input(new Vec2(500), "other", "4", Color.CYAN);
        inputGroup.addInput(i, i2);

        // separators
        separatorSh.autoInitializeShadersMulti("shaders/simple_colour.glsl");
        ShaderHelper.uniformResolutionData(separatorSh, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);

        separatorVa.genId();
        separatorVb.genId();
        VertexArray.Layout vaLayout = new VertexArray.Layout();
        vaLayout.pushFloat(2);  // pos
        vaLayout.pushFloat(4);  // color
        separatorVa.pushBuffer(separatorVb, vaLayout);

        separatorSb.setAutoResize(true);
        separatorSb.setAdditionalVertFloats(vaLayout.getTotalItems() - 2);  // minus pos

        Color c = Color.YELLOW;
        ShapeMode.Append mode = new ShapeMode.Append(new float[] {c.getRed(), c.getGreen(), c.getBlue(), .4f});
        separatorSb.pushPolygon(Shape2d.createRectOutline(new Vec2(0), new Vec2(Constants.SCREEN_SIZE), 4, mode));
        separatorSb.pushPolygon(Shape2d.createRectOutline(new Vec2(8), new Vec2(Constants.SCREEN_SIZE).sub(16), 2, mode));
        separatorSb.pushSeparatedPolygon(Shape2d.createLine(new Vec2(16, 100), new Vec2(Constants.SCREEN_SIZE.width - 16, 100), 4, mode));
        separatorSb.pushSeparatedPolygon(Shape2d.createLine(new Vec2(250, 110), new Vec2(250, Constants.SCREEN_SIZE.height - 16), 4, mode));
        separatorVb.bufferSetData(separatorSb);
    }

    public void render() {
        Renderer.clearScreen();

        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        algorithmButtons.renderAll();
        inputGroup.renderAll();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        textRenderer.draw();

        separatorSh.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, separatorVa, separatorSb.getVertexCount());
        Renderer.finish(window);
    }
}
