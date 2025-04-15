package common;

import boilerplate.rendering.*;
import boilerplate.utility.Vec2;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class Maze {
    public static final int WALL = 0;
    public static final int EMPTY = 1;
    public static final int VISITED = 2;
    public static final int FOCUSING = 3;
    private static final int START = 4;
    private static final int END = 5;

    public Vec2 pos = new Vec2(520, 220);
    public Vec2 size = new Vec2(400);

    private Vec2 startPos;
    private Vec2 endPos;

    public static final int MIN_GRID_SIZE = 5;
    public static final int MAX_GRID_SIZE = 50;
    private int gridSize = 10;
    private int[][] mazeGrid = new int[gridSize][gridSize];

    private final ShaderHelper shWall = new ShaderHelper();
    private final ShaderHelper shSpace = new ShaderHelper();
    private final VertexArray vaWall = new VertexArray();
    private final VertexArray vaSpace = new VertexArray();
    private final VertexBuffer vbWall = new VertexBuffer();
    private final VertexBuffer vbSpace = new VertexBuffer();
    private final BufferBuilder2f sbWall = new BufferBuilder2f(true);
    private final BufferBuilder2f sbSpace = new BufferBuilder2f(true);
    public boolean hasChangedWalls = true;
    public boolean hasChangedSpace = true;

    public float wobbleFrequency = 2;
    private static final float wobbleSpeed = .5f;

    public void setupBufferObjects() {
        shWall.autoInitializeShadersMulti("shaders/maze_wall.glsl");
        shSpace.autoInitializeShadersMulti("shaders/maze_space.glsl");
        setWobbleFrequency(wobbleFrequency);
        setGridSize(gridSize);
        ShaderHelper.uniformResolutionData(shWall, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniformResolutionData(shSpace, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniform1f(shSpace, "maxScale", percentSize(MAX_GRID_SIZE));

        vaWall.genId();
        vaSpace.genId();
        vbWall.genId();
        vbSpace.genId();

        VertexArray.Layout wallLayout = new VertexArray.Layout();
        wallLayout.pushFloat(2);  // pos
        wallLayout.pushFloat(1);  // wobble speed
        wallLayout.pushFloat(1);  // wobble index
        vaWall.pushBuffer(vbWall, wallLayout);
        sbWall.setAdditionalVertFloats(wallLayout.getTotalItems() - 2);  // -pos

        VertexArray.Layout spaceLayout = new VertexArray.Layout();
        spaceLayout.pushFloat(2);  // pos
        spaceLayout.pushFloat(1);  // status
        spaceLayout.pushFloat(1);  // wobble speed
        spaceLayout.pushFloat(1);  // wobble index
        vaSpace.pushBuffer(vbSpace, spaceLayout);
        sbSpace.setAdditionalVertFloats(spaceLayout.getTotalItems() - 2);  // -pos
        clearMaze();
    }

    public void setSize(int newSize) {
        if (newSize == gridSize) return;
        gridSize = newSize;
        clearMaze();
    }

    public void clearMaze() {
        mazeGrid = new int[gridSize][gridSize];
        set(new Vec2(0, gridSize-1), START);
        set(new Vec2(gridSize-1, 0), END);
        hasChangedWalls = hasChangedSpace = true;
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
        if (val == EMPTY || val == WALL) hasChangedWalls = true;
        hasChangedSpace = true;
        mazeGrid[y][x] = val;
    }

    public int get(Vec2 pos) {
        return get((int) pos.x, (int) pos.y);
    }
    public int get(int x, int y) {
        if (isOutOfBounds(x, y)) return WALL;
        return mazeGrid[y][x];
    }

    public boolean isOutOfBounds(Vec2 pos) {
        return isOutOfBounds((int) pos.x, (int) pos.y);
    }
    public boolean isOutOfBounds(int x, int y) {
        return y == -1 || x == -1 || y > gridSize-1 || x > gridSize-1;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void reBuildBuffers() {
        int gridMargin = 40;
        boolean separateNext = false;
        Vec2 tileSize = size.div(gridSize);

        Vec2 prevIndex = new Vec2(-2);
        for (int y = -1; y < gridSize+1; y++) {
            for (int x = -1; x < gridSize+1; x++) {
                int s = get(x, y);
                Vec2 tilePos = new Vec2(x, y).mul(tileSize).add(pos);

                List<float[]> wobbleFloats = getWobbleFloats(x, y);
                boolean isMarginTile = isOutOfBounds(x, y);

                if (hasChangedWalls) {
                    // margins
                    if (isMarginTile) {
                        boolean xEdgeLeft = x == -1;
                        boolean yEdgeTop = y == -1;
                        boolean xEdgeRight = x == gridSize;
                        boolean yEdgeBtm = y == gridSize;
                        Vec2 marginTilePos = tilePos.add(xEdgeLeft ? tileSize.x- gridMargin : 0, yEdgeTop ? tileSize.y- gridMargin : 0);
                        Vec2 marginTileSize = new Vec2(xEdgeLeft || xEdgeRight ? gridMargin : tileSize.x, yEdgeTop || yEdgeBtm ? gridMargin : tileSize.y);

                        // stabilize edges
                        if (xEdgeLeft || yEdgeTop) wobbleFloats.get(0)[0] = 0;
                        if (xEdgeLeft || yEdgeBtm) wobbleFloats.get(1)[0] = 0;
                        if (xEdgeRight || yEdgeTop) wobbleFloats.get(2)[0] = 0;
                        if (xEdgeRight || yEdgeBtm) wobbleFloats.get(3)[0] = 0;

                        Shape2d.Poly poly = Shape2d.createRect(marginTilePos, marginTileSize, new ShapeMode.Unpack(wobbleFloats));
                        if (xEdgeLeft) sbWall.pushSeparatedPolygon(poly);
                        else sbWall.pushPolygon(poly);
                        continue;
                    }

                    // walls
                    if (s == EMPTY) separateNext = true;
                    else {
                        Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.Unpack(wobbleFloats));
                        if (separateNext) sbWall.pushSeparatedPolygon(poly);
                        else sbWall.pushPolygon(poly);
                    }
                }

                // spaces
                if (hasChangedSpace && !isMarginTile && s != WALL) {
                    Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.AppendUnpack(new float[] {s}, wobbleFloats));
                    if (prevIndex.y != y || prevIndex.x + 1 != x) sbSpace.pushSeparatedPolygon(poly);
                    else sbSpace.pushPolygon(poly);
                    prevIndex.set(x, y);
                }
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

    private float percentSize(int size) {
        return ((float) size + 20) / MAX_GRID_SIZE;
    }

    public void setGridSize(int val) {
        gridSize = val;
        float scale = percentSize(gridSize);
        ShaderHelper.uniform1f(shWall, "scale", scale);
        ShaderHelper.uniform1f(shSpace, "scale", scale);
        clearMaze();
    }

    public void setWobbleFrequency(float val) {
        wobbleFrequency = val;
        ShaderHelper.uniform1f(shWall, "wobbleFrequency", wobbleFrequency);
        ShaderHelper.uniform1f(shSpace, "wobbleFrequency", wobbleFrequency);
    }

    public void render() {
        if (hasChangedWalls || hasChangedSpace) {
            if (hasChangedWalls) sbWall.clear();
            if (hasChangedSpace) sbSpace.clear();
            reBuildBuffers();
            if (hasChangedWalls) vbWall.bufferSetData(sbWall);
            if (hasChangedSpace) vbSpace.bufferSetData(sbSpace);
            hasChangedWalls = hasChangedSpace = false;
        }

        float t = (float) glfwGetTime();
        ShaderHelper.uniform1f(shWall, "time", t);
        ShaderHelper.uniform1f(shSpace, "time", t);

        shSpace.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, vaSpace, sbSpace.getVertexCount());

        shWall.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, vaWall, sbWall.getVertexCount());
    }
}
