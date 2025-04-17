package common;

import boilerplate.rendering.*;
import boilerplate.utility.Vec2;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class Maze {
    public static final int OUT_OF_BOUNDS = -1;
    public static final int WALL = 0;
    public static final int EMPTY = 1;
    public static final int VISITED = 2;
    public static final int FOCUSING = 3;
    private static final int START = 4;
    private static final int END = 5;

    public Vec2 pos = new Vec2(520, 220);
    public Vec2 size = new Vec2(400);
    public boolean searchable = false;

    public static final int MIN_GRID_SIZE = 5;
    public static final int MAX_GRID_SIZE = 65;
    private int gridSize = 11;
    private int[][] mazeGrid = new int[gridSize][gridSize];

    private final ShaderHelper shBg = new ShaderHelper();
    private final ShaderHelper shTiles = new ShaderHelper();
    private final VertexArray vaBg = new VertexArray();
    private final VertexArray vaTiles = new VertexArray();
    private final VertexBuffer vbBg = new VertexBuffer();
    private final VertexBuffer vbTiles = new VertexBuffer();
    private final BufferBuilder2f sbBg = new BufferBuilder2f(true);
    public final BufferBuilder2f sbTiles = new BufferBuilder2f(true);
    public boolean hasChanged = true;

    public float wobbleFrequency = 1;
    private static final float wobbleSpeed = .5f;

    public void setupBufferObjects() {
        shBg.autoInitializeShadersMulti("shaders/maze_bg.glsl");
        shTiles.autoInitializeShadersMulti("shaders/maze_tiles.glsl");
        setWobbleFrequency(wobbleFrequency);
        setGridSize(gridSize);
        ShaderHelper.uniformResolutionData(shBg, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniformResolutionData(shTiles, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniform1f(shTiles, "maxScale", percentSize(MAX_GRID_SIZE));

        vaBg.genId();
        vaTiles.genId();
        vbBg.genId();
        vbTiles.genId();

        VertexArray.Layout wallLayout = new VertexArray.Layout();
        wallLayout.pushFloat(2);  // pos
        vaBg.pushBuffer(vbBg, wallLayout);

        VertexArray.Layout spaceLayout = new VertexArray.Layout();
        spaceLayout.pushFloat(2);  // pos
        spaceLayout.pushFloat(1);  // status
        spaceLayout.pushFloat(1);  // wobble speed
        spaceLayout.pushFloat(1);  // wobble index
        vaTiles.pushBuffer(vbTiles, spaceLayout);
        sbTiles.setAdditionalVertFloats(spaceLayout.getTotalItems() - 2);  // -pos

        int gridMargin = 40;
        sbBg.pushPolygon(Shape2d.createRect(pos.sub(gridMargin), size.add(gridMargin*2)));
        vbBg.bufferSetData(sbBg);
        updateScaleUniforms();
        clearMaze();
    }

    public void clearMaze() {
        mazeGrid = new int[gridSize][gridSize];
        hasChanged = true;
        searchable = false;
    }

    public void placeStartEndPoints() {
        set(new Vec2(0, gridSize-1), START);
        set(new Vec2(gridSize-1, 0), END);
        searchable = true;
    }

    public boolean hasVisited(Vec2 pos) {
        return hasVisited((int) pos.x, (int) pos.y);
    }
    public boolean hasVisited(int x, int y) {
        if (isOutOfBounds(x, y)) return false;
        int s = get(x, y);
        return s == VISITED || s == FOCUSING;
    }

    public boolean isWall(Vec2 pos) {
        return isWall((int) pos.x, (int) pos.y);
    }
    public boolean isWall(int x, int y) {
        if (isOutOfBounds(x, y)) return true;
        return mazeGrid[y][x] == WALL;
    }

    public void set(Vec2 pos, int val) {
        set((int) pos.x, (int) pos.y, val);
    }
    public void set(int x, int y, int val) {
        if (isOutOfBounds(x, y)) return;
        if (get(x, y) == val || !(val == WALL || val == EMPTY || val == VISITED || val == FOCUSING || val == START || val == END)) return;
        hasChanged = true;
        mazeGrid[y][x] = val;
    }

    public int get(Vec2 pos) {
        return get((int) pos.x, (int) pos.y);
    }
    public int get(int x, int y) {
        if (isOutOfBounds(x, y)) return OUT_OF_BOUNDS;
        return mazeGrid[y][x];
    }

    public ArrayList<Vec2> getNonWallNeighbors(Vec2 pos, int step) {
        return getNonWallNeighbors((int) pos.x, (int) pos.y, step);
    }
    public ArrayList<Vec2> getNonWallNeighbors(int x, int y, int step) {
        ArrayList<Vec2> l = new ArrayList<>(4);
        for (Vec2 off : List.of(new Vec2(0, step), new Vec2(step, 0), new Vec2(0, -step), new Vec2(-step, 0))) {
            Vec2 neighbourPos = off.add(x, y);
            int s = get(neighbourPos);
            if (s != WALL && s != OUT_OF_BOUNDS) l.add(neighbourPos);
        }
        return l;
    }

    public ArrayList<Vec2> getWallNeighbours(Vec2 pos, int step) {
        return getWallNeighbours((int) pos.x, (int) pos.y, step);
    }
    public ArrayList<Vec2> getWallNeighbours(int x, int y, int step) {
        ArrayList<Vec2> l = new ArrayList<>(4);
        for (Vec2 off : List.of(new Vec2(0, step), new Vec2(step, 0), new Vec2(0, -step), new Vec2(-step, 0))) {
            Vec2 neighbourPos = off.add(x, y);
            if (get(neighbourPos) == WALL) l.add(neighbourPos);
        }
        return l;
    }

    /** in between cells with 1 space in between */
    public Vec2 getCellInBetween(Vec2 p1, Vec2 p2) {
        int xDiff = (int) (p2.x - p1.x);
        int yDiff = (int) (p2.y - p1.y);

        if (Math.abs(xDiff) > 1) return p1.add(xDiff * .5f, 0);
        if (Math.abs(yDiff) > 1) return p1.add(0, yDiff * .5f);
        return null;
    }

    public boolean isOutOfBounds(Vec2 pos) {
        return isOutOfBounds((int) pos.x, (int) pos.y);
    }
    public boolean isOutOfBounds(int x, int y) {
        return y < 0 || x < 0 || y > gridSize-1 || x > gridSize-1;
    }

    public int getGridSize() {
        return gridSize;
    }

    private float percentSize(int size) {
        return ((float) size + MAX_GRID_SIZE * .5f) / MAX_GRID_SIZE;
    }

    private void updateScaleUniforms() {
        float scale = percentSize(gridSize);
        ShaderHelper.uniform1f(shBg, "scale", scale);
        ShaderHelper.uniform1f(shTiles, "scale", scale);
    }

    public void setGridSize(int val) {
        if (gridSize == val) return;
        gridSize = val;
        updateScaleUniforms();
        clearMaze();
    }

    public void setWobbleFrequency(float val) {
        wobbleFrequency = val;
        ShaderHelper.uniform1f(shTiles, "wobbleFrequency", wobbleFrequency);
    }

    public void reBuildTilesBuffer() {
        Vec2 tileSize = size.div(gridSize);
        Vec2 prevIndex = new Vec2();

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int status = get(x, y);
                if (status == WALL) continue;

                Vec2 tilePos = new Vec2(x, y).mul(tileSize).add(pos);
                Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.AppendUnpack(new float[] {status}, getWobbleFloats(x, y)));
                if (prevIndex.y != y || prevIndex.x + 1 != x) sbTiles.pushSeparatedPolygon(poly);
                else sbTiles.pushPolygon(poly);
                prevIndex.set(x, y);
            }
        }
    }

    private static final List<float[]> wobbleFloatsA = List.of(new float[]{0, 0}, new float[]{0, 1}, new float[]{0, 2}, new float[]{0, 3});
    private static final List<float[]> wobbleFloatsB = List.of(new float[]{0, 2}, new float[]{0, 3}, new float[]{0, 0}, new float[]{0, 1});
    private static final List<float[]> wobbleFloatsC = List.of(new float[]{0, 1}, new float[]{0, 0}, new float[]{0, 3}, new float[]{0, 2});
    private static final List<float[]> wobbleFloatsD = List.of(new float[]{0, 3}, new float[]{0, 2}, new float[]{0, 1}, new float[]{0, 0});
    private static List<float[]> getWobbleFloats(int x, int y) {
        List<float[]> wobbleIndexes;
        if (y % 2 == 0) wobbleIndexes = x % 2 == 0 ? wobbleFloatsA : wobbleFloatsB;
        else wobbleIndexes = x % 2 == 0 ? wobbleFloatsC : wobbleFloatsD;
        for (float[] floats : wobbleIndexes) floats[0] = wobbleSpeed;
        return wobbleIndexes;
    }

    public void render() {
        if (hasChanged) {
            sbTiles.clear();
            reBuildTilesBuffer();
            vbTiles.bufferSetData(sbTiles);
            hasChanged = false;
        }

        float t = (float) glfwGetTime();
        ShaderHelper.uniform1f(shBg, "time", t);
        ShaderHelper.uniform1f(shTiles, "time", t);

        shBg.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, vaBg, sbBg.getVertexCount());

        shTiles.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, vaTiles, sbTiles.getVertexCount());
    }
}
