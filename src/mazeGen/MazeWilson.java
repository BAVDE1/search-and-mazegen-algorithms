package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class MazeWilson extends Runner {
    Cell walkStart;
    ArrayList<Cell> currentPath;

    public MazeWilson(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        Cell initial = generateRandomCell();
        maze.set(initial.pos, Maze.EMPTY);
        do {
            walkStart = generateRandomCell();
        } while (initial.pos.equals(walkStart.pos));  // cant be the same!
    }

    @Override
    public void performOperation() {
        super.performOperation();

        // do random walk
        if (walkStart != null) {
            return;
        }

        // start a walk
        walkStart = chooseNextWall();
        if (walkStart == null) finishMaze();
    }

    private Cell chooseNextWall() {
        for (int y = 0; y < maze.getGridSize(); y += 2) {
            for (int x = 0; x < maze.getGridSize(); x += 2) {
                if (maze.get(x, y) != Maze.WALL) continue;
                return new Cell(new Vec2(x, y));
            }
        }
        return null;
    }
}
