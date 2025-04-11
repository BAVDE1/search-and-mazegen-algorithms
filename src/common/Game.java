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
    ButtonGroup actionButtons = new ButtonGroup();
    ButtonGroup modeButtons = new ButtonGroup();
    ButtonGroup mazeGenerationButtons = new ButtonGroup();
    ButtonGroup searchAlgorithmButtons = new ButtonGroup();
    InputGroup inputGroup = new InputGroup();

    private final ShaderHelper separatorSh = new ShaderHelper();
    private final VertexArray separatorVa = new VertexArray();
    private final VertexBuffer separatorVb = new VertexBuffer();
    private final BufferBuilder2f separatorSb = new BufferBuilder2f();

    boolean searchesListed = true;
    Button startBtn;
    TextRenderer.TextObject selectedAlgorithms;

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
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 14, true);
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
                if (key >= 0 && key < heldKeys.length) heldKeys[key] = 0;
                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
            if (action == 1) {
                if (key >= 0 && key < heldKeys.length) heldKeys[key] = 1;
                inputGroup.keyPressed(key, scancode);
            }
        });
        GLFW.glfwSetMouseButtonCallback(this.window.handle, (window, button, action, mode) -> {
            if (action == 0) {
                this.heldMouseKeys[button] = 0;
                inputGroup.mouseUp();
            }
            if (action == 1) {
                this.heldMouseKeys[button] = 1;
                if (button == 0) this.mousePosOnClick.set(this.mousePos);
                actionButtons.mouseClicked();
                modeButtons.mouseClicked();
                mazeGenerationButtons.mouseClicked();
                searchAlgorithmButtons.mouseClicked();
                inputGroup.mouseDown(mousePos);
            }
        });
        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> {
            mousePos.set((float) xPos, (float) yPos);
            actionButtons.updateMouse(mousePos);
            modeButtons.updateMouse(mousePos);
            mazeGenerationButtons.updateMouse(mousePos);
            searchAlgorithmButtons.updateMouse(mousePos);
            inputGroup.updateMouse(mousePos);
        });
    }

    public void setupBuffers() {
        // text
        textRenderer.setupBufferObjects();
        TextRenderer.TextObject at = new TextRenderer.TextObject(1, "algorithm", new Vec2(75, 185));
        at.setTextColour(Color.YELLOW);
        selectedAlgorithms = new TextRenderer.TextObject(2, "", new Vec2(20, Constants.SCREEN_SIZE.height - 60));
        selectedAlgorithms.setTextColour(Color.YELLOW);
        textRenderer.pushTextObject(at, selectedAlgorithms);

        // buttons
        actionButtons.setupBufferObjects();
        startBtn = new Button(new Vec2(300, 55), new Vec2(120, 50), "start search", Color.GREEN);
        Button genAction = new Button(new Vec2(450, 55), new Vec2(150, 50), "generate maze", Color.LIGHT_GRAY);
        actionButtons.addButton(startBtn, genAction);

        modeButtons.setupBufferObjects();
        modeButtons.radioToggles = true;
        ToggleButton search = new ToggleButton(new Vec2(25, 30), new Vec2(200, 40), "search maze");
        ToggleButton gen = new ToggleButton(new Vec2(25, 90), new Vec2(200, 40), "maze generation");
        search.addCallback((Button btn) -> {
            ToggleButton toggleButton = (ToggleButton) btn;
            mazeGenerationButtons.setVisible(!toggleButton.toggled);
            searchAlgorithmButtons.setVisible(toggleButton.toggled);
        });
        modeButtons.addButton(search, gen);
        modeButtons.toggleBtn(search, true);

        mazeGenerationButtons.setupBufferObjects();
        mazeGenerationButtons.radioToggles = true;
        ToggleButton rdf = new ToggleButton(new Vec2(25, 230), new Vec2(200, 40), "rand depth first", Color.YELLOW);
        ToggleButton rk = new ToggleButton(new Vec2(25, 290), new Vec2(200, 40), "rand kruskal", Color.YELLOW);
        ToggleButton rp = new ToggleButton(new Vec2(25, 350), new Vec2(200, 40), "rand Prim", Color.YELLOW);
        ToggleButton w = new ToggleButton(new Vec2(25, 410), new Vec2(200, 40), "Wilson", Color.YELLOW);
        mazeGenerationButtons.addButton(rdf, rk, rp, w);
        mazeGenerationButtons.toggleBtn(rdf, true);

        searchAlgorithmButtons.setupBufferObjects();
        searchAlgorithmButtons.radioToggles = true;
        ToggleButton df = new ToggleButton(new Vec2(25, 230), new Vec2(200, 40), "depth first", Color.YELLOW);
        ToggleButton bf = new ToggleButton(new Vec2(25, 290), new Vec2(200, 40), "breadth first", Color.YELLOW);
        ToggleButton gbf = new ToggleButton(new Vec2(25, 350), new Vec2(200, 40), "greedy best first", Color.YELLOW);
        ToggleButton as = new ToggleButton(new Vec2(25, 410), new Vec2(200, 40), "a star", Color.YELLOW);
        searchAlgorithmButtons.addButton(df, bf, as, gbf);
        searchAlgorithmButtons.toggleBtn(df, true);

        // inputs
        inputGroup.setupBufferObjects();
        InputRange fpo = new InputRange(new Vec2(900, 20), "frames / op", 1, 0, 50, Color.YELLOW);
        InputRange ms = new InputRange(new Vec2(750, 20), "maze size", 20, 5, 50, Color.YELLOW);
        inputGroup.addInput(fpo, ms);

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
        separatorSb.pushSeparatedPolygon(Shape2d.createLine(new Vec2(260, 150), new Vec2(Constants.SCREEN_SIZE.width - 16, 150), 4, mode));
        separatorSb.pushSeparatedPolygon(Shape2d.createLine(new Vec2(250, 16), new Vec2(250, Constants.SCREEN_SIZE.height - 16), 4, mode));
        separatorVb.bufferSetData(separatorSb);
    }

    public void render() {
        Renderer.clearScreen();

        selectedAlgorithms.setString(String.format(
                "search: %s\nmaze: %s",
                searchAlgorithmButtons.radioBtnSelected.text,
                mazeGenerationButtons.radioBtnSelected.text
        ));

        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        actionButtons.renderAll();
        modeButtons.renderAll();
        mazeGenerationButtons.renderAll();
        searchAlgorithmButtons.renderAll();
        inputGroup.renderAll();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        textRenderer.draw();

        separatorSh.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, separatorVa, separatorSb.getVertexCount());
        Renderer.finish(window);
    }
}
