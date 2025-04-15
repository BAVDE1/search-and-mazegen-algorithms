package common;

import boilerplate.rendering.*;
import boilerplate.utility.Vec2;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class Maze {
    public static char WALL = '\0';  // null character
    public static char EMPTY = ' ';
    public static char VISITED = '-';
    public static char FOCUSING = '+';

    public Vec2 pos = new Vec2(520, 220);
    public Vec2 size = new Vec2(400);

    public static final int MIN_GRID_SIZE = 5;
    public static final int MAX_GRID_SIZE = 50;
    private int gridSize = 10;
    private char[][] mazeGrid = new char[gridSize][gridSize];

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
    }

    public void setSize(int newSize) {
        if (newSize == gridSize) return;
        gridSize = newSize;
        clearMaze();
    }

    public void clearMaze() {
        mazeGrid = new char[gridSize][gridSize];
        hasChangedWalls = hasChangedSpace = true;
    }

    public boolean hasVisited(Vec2 pos) {
        return hasVisited((int) pos.x, (int) pos.y);
    }
    public boolean hasVisited(int x, int y) {
        if (isOutOfBounds(x, y)) return false;
        char c = get(x, y);
        return c == VISITED || c == FOCUSING;
    }

    public boolean isWall(Vec2 pos) {
        return isWall((int) pos.x, (int) pos.y);
    }
    public boolean isWall(int x, int y) {
        if (isOutOfBounds(x, y)) return true;
        return mazeGrid[y][x] == WALL;
    }

    public void set(Vec2 pos, char val) {
        set((int) pos.x, (int) pos.y, val);
    }
    public void set(int x, int y, char val) {
        if (isOutOfBounds(x, y)) return;
        if (get(x, y) == val || !(val == WALL || val == EMPTY || val == VISITED || val == FOCUSING)) return;
        hasChangedSpace = true;
        if (val == EMPTY || val == WALL) hasChangedWalls = true;
        mazeGrid[y][x] = val;
    }

    public char get(Vec2 pos) {
        return get((int) pos.x, (int) pos.y);
    }
    public char get(int x, int y) {
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
                char c = get(x, y);
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
                    if (c == EMPTY) separateNext = true;
                    else {
                        Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.Unpack(wobbleFloats));
                        if (separateNext) sbWall.pushSeparatedPolygon(poly);
                        else sbWall.pushPolygon(poly);
                    }
                }

                // spaces
                if (hasChangedSpace && !isMarginTile && c != WALL) {
                    Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.AppendUnpack(new float[] {0}, wobbleFloats));
                    if (prevIndex.y != y || prevIndex.x + 1 != x) sbSpace.pushSeparatedPolygon(poly);
                    else sbSpace.pushPolygon(poly);
                }
                prevIndex.x = x;
            }
            prevIndex.y = y;
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

    public void setGridSize(int val) {
        gridSize = val;
        float scale = ((float) gridSize + 20) / MAX_GRID_SIZE;
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
