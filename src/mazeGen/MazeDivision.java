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
        areaQueue.clear();
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

        // create 3 holes in the walls
        if (opNum % 2 == 0) {
            wallFocussingCells(array);
            int excludeSide = random.nextInt(0, 4);

            for (int side = 0; side < 4; side++) {
                if (side == excludeSide) continue;

                Vec2 pos;
                boolean retry;
                do {
                    retry = false;
                    switch (side) {
                        case 0 -> {  // top
                            int y = random.nextInt((int) focussingArea.pos.y, (int) focussingArea.cellWithin.pos.y);
                            if (y % 2 == 1) retry = true;
                            pos = new Vec2(focussingArea.cellWithin.pos.x, y);
                        }
                        case 1 -> {  // right
                            int x = random.nextInt((int) focussingArea.cellWithin.pos.x + 1, (int) (focussingArea.pos.x + focussingArea.size.x));
                            if (x % 2 == 1) retry = true;
                            pos = new Vec2(x, focussingArea.cellWithin.pos.y);
                        }
                        case 2 -> {  // bottom
                            int y = random.nextInt((int) focussingArea.cellWithin.pos.y + 1, (int) (focussingArea.pos.y + focussingArea.size.y));
                            if (y % 2 == 1) retry = true;
                            pos = new Vec2(focussingArea.cellWithin.pos.x, y);
                        }
                        default -> {  // left
                            int x = random.nextInt((int) focussingArea.pos.x, (int) focussingArea.cellWithin.pos.x);
                            if (x % 2 == 1) retry = true;
                            pos = new Vec2(x, focussingArea.cellWithin.pos.y);
                        }
                    }
                } while (retry || maze.getNonWallNeighbors((int) pos.x, (int) pos.y, 1).size() < 2);  // needs to make a proper path
                maze.set(pos, Maze.FOCUSING);
                array.add(new Cell(pos));
            }
            focussingArea = null;
            return;
        }

        clearFocussingCells(array);
        if (areaQueue.isEmpty()) {
            finishMaze();
            return;
        }

        // new area
        focussingArea = areaQueue.remove();
        int cellX = random.nextInt((int) focussingArea.pos.x, (int) (focussingArea.pos.x + focussingArea.size.x) - 1);
        int cellY = random.nextInt((int) focussingArea.pos.y, (int) (focussingArea.pos.y + focussingArea.size.y) - 1);
        Vec2 cellPos = new Vec2(cellX, cellY).sub(cellX % 2, cellY % 2).add(1);
        focussingArea.cellWithin = new Cell(cellPos);

        // make new crossed walls
        for (int x = (int) focussingArea.pos.x; x < focussingArea.pos.x + focussingArea.size.x; x++) {
            Vec2 pos = new Vec2(x, (int) cellPos.y);
            maze.set(pos, Maze.FOCUSING);
            array.add(new Cell(pos));
        }

        for (int y = (int) focussingArea.pos.y; y < focussingArea.pos.y + focussingArea.size.y; y++) {
            Vec2 pos = new Vec2((int) cellPos.x, y);
            maze.set(pos, Maze.FOCUSING);
            array.add(new Cell(pos));
        }

        // add newly created areas
        for (int quater = 0; quater < 4; quater++) {
            switch (quater) {
                case 0 -> {  // top left
                    int areaX = (int) (cellPos.x - focussingArea.pos.x);
                    int areaY = (int) (cellPos.y - focussingArea.pos.y);
                    if (areaX == 1 || areaY == 1) continue;
                    areaQueue.add(new Area(focussingArea.pos, new Vec2(areaX, areaY)));
                }
                case 1 -> {  // top right
                    int areaX = (int) ((focussingArea.pos.x + focussingArea.size.x) - cellPos.x - 1);
                    int areaY = (int) (cellPos.y - focussingArea.pos.y);
                    if (areaX == 1 || areaY == 1) continue;
                    areaQueue.add(new Area(new Vec2(cellPos.x + 1, focussingArea.pos.y), new Vec2(areaX, areaY)));
                }
                case 2 -> {  // bottom left
                    int areaX = (int) (cellPos.x - focussingArea.pos.x);
                    int areaY = (int) ((focussingArea.pos.y + focussingArea.size.y) - cellPos.y - 1);
                    if (areaX == 1 || areaY == 1) continue;
                    areaQueue.add(new Area(new Vec2(focussingArea.pos.x, cellPos.y + 1), new Vec2(areaX, areaY)));
                }
                default -> {  // bottom right
                    int areaX = (int) ((focussingArea.pos.x + focussingArea.size.x) - cellPos.x - 1);
                    int areaY = (int) ((focussingArea.pos.y + focussingArea.size.y) - cellPos.y - 1);
                    if (areaX == 1 || areaY == 1) continue;
                    areaQueue.add(new Area(new Vec2(cellPos.x + 1, cellPos.y + 1), new Vec2(areaX, areaY)));
                }
            }
        }
    }
}
