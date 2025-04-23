package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MazeDivision extends Runner {
    static class Area {
        public Cell cellWithin;
        public Vec2 pos;
        public Vec2 size;

        public Area(Vec2 pos, Vec2 size) {
            this.pos = pos;
            this.size = size;
        }
    }

    private Queue<Area> areaQueue = new LinkedList<>();
    private Area focussingArea;

    public MazeDivision(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        emptyMaze();
        areaQueue.add(new Area(new Vec2(), new Vec2(maze.getGridSize())));
    }

    @Override
    public void reset() {
        super.reset();
    }

    private void emptyMaze() {
        for (int y = 0; y < maze.getGridSize(); y++) {
            for (int x = 0; x < maze.getGridSize(); x++) {
                maze.set(x, y, Maze.EMPTY);
            }
        }
    }

    @Override
    public void performOperation() {
        super.performOperation();
        clearFocussingCells(array);

        // create 3 holes in the walls
        if (opNum % 2 == 1) {
            int excludeSide = random.nextInt(0, 4);

            for (int side = 0; side < 4; side++) {
                if (side == excludeSide) continue;

                Vec2 pos;
                do {
                    switch (side) {
                        case 0 -> {  // top
                            int y = random.nextInt((int) focussingArea.pos.y, (int) focussingArea.cellWithin.pos.y);
                            pos = new Vec2(focussingArea.cellWithin.pos.x, y);
                        }
                        case 1 -> {  // right
                            int x = random.nextInt((int) focussingArea.cellWithin.pos.x + 1, (int) (focussingArea.pos.x + focussingArea.size.x));
                            pos = new Vec2(x, focussingArea.cellWithin.pos.y);
                        }
                        case 2 -> {  // bottom
                            int y = random.nextInt((int) focussingArea.cellWithin.pos.y + 1, (int) (focussingArea.pos.y + focussingArea.size.y));
                            pos = new Vec2(focussingArea.cellWithin.pos.x, y);
                        }
                        default -> {  // left
                            int x = random.nextInt((int) focussingArea.pos.x, (int) focussingArea.cellWithin.pos.x);
                            pos = new Vec2(x, focussingArea.cellWithin.pos.y);
                        }
                    }
                } while (maze.getNonWallNeighbors((int) pos.x, (int) pos.y, 1).size() < 2);  // needs to make a proper path
                maze.set(pos, Maze.WALL);
                array.add(new Cell(pos));
            }
            return;
        }

        // new area
        focussingArea = areaQueue.remove();
        int x = random.nextInt();
        int y = random.nextInt();
        focussingArea.cellWithin = new Cell(new Vec2(x, y));

        // make walls

        int s = maze.get(onX, onY);
        Vec2 pos = new Vec2(
                (onCopy != 2 ? fractSize : 0) + onX,
                (onCopy > 1 ? fractSize : 0) + onY
        );
        maze.set(pos, s == Maze.EMPTY ? Maze.FOCUSING : s);
        if (s == Maze.EMPTY) array.add(new Cell(pos));

        onX++;
        if (onX >= fractSize-1) {
            onY++;
            onX = 0;
        }

        if (onY >= fractSize-1 && onCopy < 4) {
            onCopy++;
            onX = 0;
            onY = 0;
        }

        if (fractSize >= maze.getGridSize()) finishMaze();
    }
}
