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
import mazeGen.*;
import org.lwjgl.glfw.GLFW;
import searching.SearchBreadthFirst;
import searching.SearchDepthFirst;

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

    Maze maze = new Maze();

    TextRenderer textRenderer = new TextRenderer();

    ButtonGroup navActionButtons = new ButtonGroup();
    ButtonGroup navAlgorithmButtons = new ButtonGroup();
    ButtonGroup actionButtons = new ButtonGroup();
    ButtonGroup mazeGenerationButtons = new ButtonGroup();
    ButtonGroup searchAlgorithmButtons = new ButtonGroup();
    ButtonGroup framesButtons = new ButtonGroup();

    InputGroup mazeInputs = new InputGroup();
    InputGroup framesInputs = new InputGroup();

    private final ShaderHelper separatorSh = new ShaderHelper();
    private final VertexArray separatorVa = new VertexArray();
    private final VertexBuffer separatorVb = new VertexBuffer();
    private final BufferBuilder2f separatorSb = new BufferBuilder2f();

    ToggleButton actionPage;
    ToggleButton framesPage;
    ToggleButton mazePage;
    ToggleButton searchAlgorithmsButton;
    ToggleButton genAlgorithmsButton;
    Button searchMazeAction;
    Button genMazeAction;
    InputRange mazeSizeInput;
    TextRenderer.TextObject selectedAlgorithms;
    TextRenderer.TextObject algorithmDetails;

    boolean useFPO = true;
    Runner mazeRunner;
    Runner searchRunner;

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
            }
            if (action == 1) {
                if (key >= 0 && key < heldKeys.length) heldKeys[key] = 1;

                switch (key) {
                    case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);
                    case GLFW_KEY_TAB -> maze.toggleDebugRender();
                    case GLFW_KEY_1 -> navActionButtons.toggleBtn(actionPage, true);
                    case GLFW_KEY_2 -> navActionButtons.toggleBtn(framesPage, true);
                    case GLFW_KEY_3 -> navActionButtons.toggleBtn(mazePage, true);
                    case GLFW_KEY_Q -> navAlgorithmButtons.toggleBtn(searchAlgorithmsButton, true);
                    case GLFW_KEY_E -> navAlgorithmButtons.toggleBtn(genAlgorithmsButton, true);
                    case GLFW_KEY_I -> mazeRunner.nextFrame(true);
                    case GLFW_KEY_O -> updateRunnerStatus(mazeRunner, genMazeAction, "o");
                    case GLFW_KEY_P -> resetMaze();
                    case GLFW_KEY_J -> searchRunner.nextFrame(true);
                    case GLFW_KEY_K -> {
                        if (!maze.searchable) return;
                        updateRunnerStatus(searchRunner, searchMazeAction, "k");
                    }
                    case GLFW_KEY_L -> resetSearch();
                }

                mazeInputs.keyPressed(key, scancode);
                framesInputs.keyPressed(key, scancode);
            }
        });
        GLFW.glfwSetMouseButtonCallback(this.window.handle, (window, button, action, mode) -> {
            if (action == 0) {
                this.heldMouseKeys[button] = 0;
                mazeInputs.mouseUp();
                framesInputs.mouseUp();
            }
            if (action == 1) {
                this.heldMouseKeys[button] = 1;
                if (button == 0) this.mousePosOnClick.set(this.mousePos);
                actionButtons.mouseClicked();
                navActionButtons.mouseClicked();
                navAlgorithmButtons.mouseClicked();
                mazeGenerationButtons.mouseClicked();
                searchAlgorithmButtons.mouseClicked();
                framesButtons.mouseClicked();
                mazeInputs.mouseDown(mousePos);
                framesInputs.mouseDown(mousePos);
            }
        });
        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> {
            mousePos.set((float) xPos, (float) yPos);
            actionButtons.updateMouse(mousePos);
            navActionButtons.updateMouse(mousePos);
            navAlgorithmButtons.updateMouse(mousePos);
            mazeGenerationButtons.updateMouse(mousePos);
            searchAlgorithmButtons.updateMouse(mousePos);
            framesButtons.updateMouse(mousePos);
            mazeInputs.updateMouse(mousePos);
            framesInputs.updateMouse(mousePos);
        });
    }

    public void setupBuffers() {
        // maze & runners
        maze.setupBufferObjects();
        mazeRunner = new MazeDepthFirst(maze, this);
        searchRunner = new SearchDepthFirst(maze, this);

        // text
        textRenderer.setupBufferObjects();
        TextRenderer.TextObject at = new TextRenderer.TextObject(1, "select algorithm", new Vec2(35, 185));
        at.setTextColour(Color.YELLOW);
        selectedAlgorithms = new TextRenderer.TextObject(2, "", new Vec2(20, Constants.SCREEN_SIZE.height - 60));
        selectedAlgorithms.setTextColour(Color.YELLOW);
        algorithmDetails = new TextRenderer.TextObject(2, "", new Vec2(265, 180));
        algorithmDetails.setTextColour(new Color(1, 1, 1, .75f));
        textRenderer.pushTextObject(at, selectedAlgorithms, algorithmDetails);

        // buttons
        navActionButtons.setupBufferObjects();
        navActionButtons.radioToggles = true;
        actionPage = new ToggleButton(new Vec2(270, 25), new Vec2(120, 30), "action (1)");
        framesPage = new ToggleButton(new Vec2(270, 65), new Vec2(120, 30), "frames (2)");
        mazePage = new ToggleButton(new Vec2(270, 105), new Vec2(120, 30), "maze (3)");
        actionPage.addCallback((Button btn) -> openActionMenu());
        framesPage.addCallback((Button btn) -> openFramesMenu());
        mazePage.addCallback((Button btn) -> openMazeMenu());
        navActionButtons.addButton(actionPage, framesPage, mazePage);
        navActionButtons.toggleBtn(actionPage, true);

        actionButtons.setupBufferObjects();
        searchMazeAction = new Button(new Vec2(480, 35), new Vec2(180, 50), "start search", Color.GREEN);
        Button clearSearch = new Button(new Vec2(480, 100), new Vec2(180, 30), "clear search [L]", Color.GRAY);
        genMazeAction = new Button(new Vec2(720, 35), new Vec2(180, 50), "generate maze", Color.MAGENTA);
        Button clearMaze = new Button(new Vec2(720, 100), new Vec2(180, 30), "clear maze [p]", Color.GRAY);
        clearSearch.textScale = clearMaze.textScale = .9f;
        searchMazeAction.addCallback((Button btn) -> {
            if (!maze.searchable) return;
            updateRunnerStatus(searchRunner, btn, "k");
        });
        genMazeAction.addCallback((Button btn) -> updateRunnerStatus(mazeRunner, btn, "o"));
        clearMaze.addCallback((Button btn) -> resetMaze());
        clearSearch.addCallback((Button btn) -> resetSearch());
        actionButtons.addButton(searchMazeAction, clearSearch, genMazeAction, clearMaze);

        navAlgorithmButtons.setupBufferObjects();
        navAlgorithmButtons.radioToggles = true;
        searchAlgorithmsButton = new ToggleButton(new Vec2(25, 30), new Vec2(200, 40), "search algorithm (q)");
        genAlgorithmsButton = new ToggleButton(new Vec2(25, 90), new Vec2(200, 40), "maze generation (e)");
        searchAlgorithmsButton.addCallback((Button btn) -> {
            ToggleButton toggleButton = (ToggleButton) btn;
            mazeGenerationButtons.setVisible(!toggleButton.toggled);
            searchAlgorithmButtons.setVisible(toggleButton.toggled);
        });
        navAlgorithmButtons.addButton(searchAlgorithmsButton, genAlgorithmsButton);
        navAlgorithmButtons.toggleBtn(searchAlgorithmsButton, true);

        mazeGenerationButtons.setupBufferObjects();
        mazeGenerationButtons.radioToggles = true;
        ToggleButton rdf = new ToggleButton(new Vec2(25, 230), new Vec2(200, 40), "rand depth first", Color.YELLOW);
        rdf.addCallback((Button btn) -> changeMazeRunner(new MazeDepthFirst(maze, this), btn));
        ToggleButton rk = new ToggleButton(new Vec2(25, 290), new Vec2(200, 40), "rand kruskal", Color.YELLOW);
        rk.addCallback((Button btn) -> changeMazeRunner(new MazeKruskal(maze, this), btn));
        ToggleButton rp = new ToggleButton(new Vec2(25, 350), new Vec2(200, 40), "rand prim", Color.YELLOW);
        rp.addCallback((Button btn) -> changeMazeRunner(new MazePrim(maze, this), btn));
        ToggleButton w = new ToggleButton(new Vec2(25, 410), new Vec2(200, 40), "wilson", Color.YELLOW);
        w.disabled = true;
        ToggleButton rd = new ToggleButton(new Vec2(25, 470), new Vec2(200, 40), "recursive division", Color.YELLOW);
        rd.disabled = true;
        ToggleButton ft = new ToggleButton(new Vec2(25, 530), new Vec2(200, 40), "fractal tesselation", Color.YELLOW);
        ft.addCallback((Button btn) -> changeMazeRunner(new MazeFractal(maze, this), btn));
        mazeGenerationButtons.addButton(rdf, rk, rp, w, rd, ft);
        mazeGenerationButtons.toggleBtn(rdf, true);

        searchAlgorithmButtons.setupBufferObjects();
        searchAlgorithmButtons.radioToggles = true;
        ToggleButton df = new ToggleButton(new Vec2(25, 230), new Vec2(200, 40), "depth first", Color.YELLOW);
        df.addCallback((Button btn) -> changeSearchRunner(new SearchDepthFirst(maze, this), btn));
        ToggleButton bf = new ToggleButton(new Vec2(25, 290), new Vec2(200, 40), "breadth first", Color.YELLOW);
        bf.addCallback((Button btn) -> changeSearchRunner(new SearchBreadthFirst(maze, this), btn));
        ToggleButton gbf = new ToggleButton(new Vec2(25, 350), new Vec2(200, 40), "greedy best first", Color.YELLOW);
        gbf.disabled = true;
        ToggleButton as = new ToggleButton(new Vec2(25, 410), new Vec2(200, 40), "a star", Color.YELLOW);
        as.disabled = true;
        searchAlgorithmButtons.addButton(df, bf, as, gbf);
        searchAlgorithmButtons.toggleBtn(df, true);

        // inputs
        mazeInputs.setupBufferObjects();
        mazeSizeInput = new InputRange(new Vec2(600, 20), "maze size", maze.getGridSize(), Maze.MIN_GRID_SIZE, Maze.MAX_GRID_SIZE, Color.YELLOW);
        mazeSizeInput.barRangeWidth = 250;
        mazeSizeInput.oddOnly = true;
        InputRange mazeWobble = new InputRange(new Vec2(850, 20), "wobble", (int) maze.wobbleFrequency, 0, 15, Color.YELLOW);
        mazeSizeInput.addCallback((Input input, String val) -> {
            maze.setGridSize(Integer.parseInt(val));
            resetMaze();
        });
        mazeWobble.addCallback((Input input, String val) -> maze.setWobbleFrequency(Float.parseFloat(val)));
        mazeInputs.addInput(mazeSizeInput, mazeWobble);
        mazeInputs.setVisible(false);

        framesInputs.setupBufferObjects();
        InputRange opf = new InputRange(new Vec2(550, 20), "op / frames", 1, 0, 20, Color.YELLOW);
        opf.barRangeWidth = 150;
        opf.addCallback((Input input, String val) -> {
            mazeRunner.opPerFrames = Integer.parseInt(val);
            searchRunner.opPerFrames = mazeRunner.opPerFrames;
        });
        InputRange fpo = new InputRange(new Vec2(850, 20), "frames / op", 1, 0, 20, Color.YELLOW);
        fpo.barRangeWidth = opf.barRangeWidth;
        fpo.addCallback((Input input, String val) -> {
            mazeRunner.framesPerOp = Integer.parseInt(val);
            searchRunner.framesPerOp = mazeRunner.framesPerOp;
        });
        opf.disabled = true;
        framesInputs.addInput(opf, fpo);
        framesInputs.setVisible(false);

        framesButtons.setupBufferObjects();
        Button doFPO = new Button(new Vec2(674, 50), new Vec2(50), "->", Color.YELLOW);
        doFPO.addCallback((Button btn) -> {
            useFPO = !useFPO;
            doFPO.text = useFPO ? "->" : "<-";
            fpo.disabled = !useFPO;
            opf.disabled = useFPO;
            framesInputs.hasChanged = true;
            framesButtons.hasChanged = true;

            mazeRunner.useFPO = useFPO;
            searchRunner.useFPO = useFPO;
        });
        framesButtons.addButton(doFPO);

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

    private void updateAlgorithmDetails() {
        selectedAlgorithms.setString(String.format(
                "search: %s\nmaze: %s",
                searchAlgorithmButtons.radioBtnSelected.text,
                mazeGenerationButtons.radioBtnSelected.text
        ));

        algorithmDetails.setString(String.format(
                """
                        maze size: %s
                        maze wobble: %s
                        %s: %s
                        
                        ========================
                        
                        verts: %s (%s)
                        floats %s / %s
                        
                        ========================
                        
                        maze: %s
                        status: %s
                        operations: %s
                        frames: %s
                        frames this op: %s
                        
                        ========================
                        
                        search: %s
                        status: %s
                        operations: %s
                        frames: %s
                        frames this op: %s
                        """,
                maze.getGridSize(),
                maze.wobbleFrequency,
                useFPO ? "frames per op" : "op per frames",
                useFPO ? mazeRunner.framesPerOp : mazeRunner.opPerFrames,
                maze.sbTiles.getVertexCount(), maze.sbTiles.getSeparationsCount(),
                maze.sbTiles.getFloatCount(), maze.sbTiles.getBufferSize(),
                mazeGenerationButtons.radioBtnSelected.text,
                mazeRunner.paused ? "paused" : (mazeRunner.running ? "running" : (mazeRunner.complete ? "completed" : "---")),
                mazeRunner.opNum,
                mazeRunner.frameNum,
                mazeRunner.opFrameNum,
                searchAlgorithmButtons.radioBtnSelected.text,
                searchRunner.paused ? "paused" : (searchRunner.running ? "running" : (searchRunner.complete ? "completed" : "---")),
                searchRunner.opNum,
                searchRunner.frameNum,
                searchRunner.opFrameNum
        ));
    }

    public void render() {
        Renderer.clearScreen();

        maze.render();
        textRenderer.draw();

        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);  // special render mode for buttons
        navActionButtons.renderAll();
        navAlgorithmButtons.renderAll();

        mazeGenerationButtons.renderAll();
        searchAlgorithmButtons.renderAll();
        actionButtons.renderAll();
        framesButtons.renderAll();

        mazeInputs.renderAll();
        framesInputs.renderAll();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        separatorSh.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, separatorVa, separatorSb.getVertexCount());
        Renderer.finish(window);
    }

    @Override
    public void mainLoop(double dt) {
        GLFW.glfwPollEvents();
        mazeRunner.nextFrame();
        searchRunner.nextFrame();
        updateAlgorithmDetails();
        render();
    }

    private void changeMazeRunner(Runner newRunner, Button btn) {
        if (!((ToggleButton) btn).toggled) return;
        resetMaze();
        if (mazeSizeInput != null) {
            mazeSizeInput.roundToPow2 = newRunner instanceof MazeFractal;
            mazeSizeInput.revalidateValue();
        }
        newRunner.useFPO = mazeRunner.useFPO;  // transfer settings
        newRunner.opPerFrames = mazeRunner.opPerFrames;
        newRunner.framesPerOp = mazeRunner.framesPerOp;
        mazeRunner = newRunner;
    }

    private void changeSearchRunner(Runner newRunner, Button btn) {
        if (!((ToggleButton) btn).toggled) return;
        resetSearch();
        newRunner.useFPO = mazeRunner.useFPO;  // transfer settings
        newRunner.opPerFrames = mazeRunner.opPerFrames;
        newRunner.framesPerOp = mazeRunner.framesPerOp;
        searchRunner = newRunner;
    }

    private void updateRunnerStatus(Runner runner, Button btn, String shortcut) {
        if (runner.complete) {
            if (btn == genMazeAction) resetMaze();
            if (btn == searchMazeAction) resetSearch();
            runner.start();
        } else if (!runner.running) {
            btn.text = String.format("pause [%s]", shortcut);
            runner.start();
        } else if (!runner.paused) {
            btn.text = String.format("resume [%s]", shortcut);
            runner.pause();
        } else {
            btn.text = String.format("pause [%s]", shortcut);
            runner.resume();
        }
        actionButtons.hasChanged = true;
    }

    private void resetMaze() {
        resetSearch();
        genMazeAction.text = "generate maze [o]";
        actionButtons.hasChanged = true;
        mazeRunner.reset();
        maze.clearMaze();
    }

    private void resetSearch() {
        searchMazeAction.text = "start search [k]";
        actionButtons.hasChanged = true;
        searchRunner.reset();
        maze.emptyVisitedMazeTiles();
    }

    public void mazeGenerationCompleted() {
        genMazeAction.text = "completed [o]";
        actionButtons.hasChanged = true;
    }

    public void searchCompleted() {
        searchMazeAction.text = "completed [k]";
        actionButtons.hasChanged = true;
    }

    public void openActionMenu() {
        actionButtons.setVisible(actionPage.toggled);
        mazeInputs.setVisible(!actionPage.toggled);
        framesButtons.setVisible(!actionPage.toggled);
        framesInputs.setVisible(!actionPage.toggled);
    }

    public void openFramesMenu() {
        actionButtons.setVisible(!framesPage.toggled);
        mazeInputs.setVisible(!framesPage.toggled);
        framesButtons.setVisible(framesPage.toggled);
        framesInputs.setVisible(framesPage.toggled);
    }

    public void openMazeMenu() {
        actionButtons.setVisible(!mazePage.toggled);
        mazeInputs.setVisible(mazePage.toggled);
        framesButtons.setVisible(!mazePage.toggled);
        framesInputs.setVisible(!mazePage.toggled);
    }
}
