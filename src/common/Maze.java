package common;

import boilerplate.rendering.*;
import boilerplate.utility.Vec2;

import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class Maze {
    public static char WALL = '\0';  // null character
    public static char EMPTY = ' ';
    public static char VISITED = '-';
    public static char FOCUSING = '+';

    public Vec2 pos = new Vec2(400);
    public Vec2 size = new Vec2(200);

    private int gridSize = 3;
    private final int gridMargin = 40;
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

    public void setupBufferObjects() {
        shWall.autoInitializeShadersMulti("shaders/maze_wall.glsl");
        shSpace.autoInitializeShadersMulti("shaders/maze_space.glsl");
        ShaderHelper.uniformResolutionData(shWall, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniformResolutionData(shSpace, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);

        vaWall.genId();
        vaSpace.genId();
        vbWall.genId();
        vbSpace.genId();

        VertexArray.Layout wallLayout = new VertexArray.Layout();
        wallLayout.pushFloat(2);  // pos
        wallLayout.pushFloat(1);  // wobble strength
        wallLayout.pushFloat(1);  // wobble index
        wallLayout.pushFloat(1);  // alpha
        vaWall.pushBuffer(vbWall, wallLayout);
        sbWall.setAdditionalVertFloats(wallLayout.getTotalItems() - 2);  // minus pos

        VertexArray.Layout spaceLayout = new VertexArray.Layout();
        spaceLayout.pushFloat(2);  // pos
        spaceLayout.pushFloat(1);  // status
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
        if (mazeGrid[y][x] == val || !(val == WALL || val == EMPTY || val == VISITED || val == FOCUSING)) return;
        if (val == EMPTY) hasChangedWalls = true;
        else hasChangedSpace = true;
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
        Vec2 tileSize = size.div(gridSize);
        for (int y = -1; y < gridSize+1; y++) {
            for (int x = -1; x < gridSize+1; x++) {
                char c = get(x, y);
                Vec2 tilePos = new Vec2(x, y).mul(tileSize).add(pos);

                boolean isMarginTile = isOutOfBounds(x, y);

                if (hasChangedWalls) {
                    float[] wobbleStrength = new float[]{2};
                    List<float[]> wobbleIndexes;

                    if (y % 2 == 0) {
                        if (x % 2 == 0) {
                            wobbleIndexes = List.of(new float[]{0, 1}, new float[]{1, 1}, new float[]{2, 1}, new float[]{3, 1});
                        } else {
                            wobbleIndexes = List.of(new float[]{2, 1}, new float[]{3, 1}, new float[]{0, 1}, new float[]{1, 1});
                        }
                    } else {
                        if (x % 2 == 0) {
                            wobbleIndexes = List.of(new float[]{1, 1}, new float[]{0, 1}, new float[]{3, 1}, new float[]{2, 1});
                        } else {
                            wobbleIndexes = List.of(new float[]{3, 1}, new float[]{2, 1}, new float[]{1, 1}, new float[]{0, 1});
                        }
                    }

                    // margins
                    if (isMarginTile) {
                        Vec2 marginTilePos = tilePos.add(x > -1 ? 0 : tileSize.x-gridMargin, y > -1 ? 0 : tileSize.y-gridMargin);
                        Vec2 marginTileSize = new Vec2(x > -1 && x < gridSize ? tileSize.x : gridMargin, y > -1 && y < gridSize ? tileSize.y : gridMargin);
                        Shape2d.Poly poly = Shape2d.createRect(marginTilePos, marginTileSize, new ShapeMode.AppendUnpack(wobbleStrength, wobbleIndexes));
                        sbWall.pushSeparatedPolygon(poly);
                        continue;
                    }

                    Shape2d.Poly poly = Shape2d.createRect(tilePos, tileSize, new ShapeMode.AppendUnpack(wobbleStrength, wobbleIndexes));
                    sbWall.pushSeparatedPolygon(poly);
                }

                if (hasChangedSpace && !isMarginTile) {
                    continue;
                }
            }
        }
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

        shWall.bind();
        ShaderHelper.uniform1f(shWall, "time", (float) glfwGetTime());
        Renderer.draw(GL_TRIANGLE_STRIP, vaWall, sbWall.getVertexCount());

        shSpace.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, vaSpace, sbSpace.getVertexCount());
    }
}
