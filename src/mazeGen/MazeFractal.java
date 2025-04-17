package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

public class MazeFractal extends Runner {
    int fractSize = 2;
    int onCopy = 1;

    int onX = 0;
    int onY = 0;

    public MazeFractal(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        maze.set(0, 0, Maze.EMPTY);
    }

    @Override
    public void reset() {
        super.reset();
        fractSize = 2;
        maze.set(0, 0, Maze.EMPTY);
        onCopy = 1;
        onX = 0;
        onY = 0;
    }

    @Override
    public void performOperation() {
        super.performOperation();
        clearFocussingCells(array);

        // create 3 paths & next size up
        if (onCopy == 4) {
            int excludeSide = random.nextInt(0, 4);
            int fs = fractSize-1;

            for (int side = 0; side < 4; side++) {
                if (side == excludeSide) continue;

                Vec2 pos;
                do {
                    int r = random.nextInt(1, fractSize);
                    switch (side) {
                        case 0 -> pos = new Vec2(fs, r - 1);
                        case 1 -> pos = new Vec2(fs + r, fs);
                        case 2 -> pos = new Vec2(fs, fs + r);
                        default -> pos = new Vec2(r - 1, fs);  // 3
                    }
                } while (maze.getNonWallNeighbors((int) pos.x, (int) pos.y, 1).size() < 2);  // needs to make a proper path
                maze.set(pos, Maze.FOCUSING);
                array.add(new Cell(pos));
            }

            fractSize *= 2;
            onCopy = 1;
            return;
        }

        // copying
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
