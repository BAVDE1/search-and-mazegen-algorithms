package mazeGen;

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
        maze.set(generateRandomCell().pos, Maze.EMPTY);  // initial cell
    }

    @Override
    public void performOperation() {
        super.performOperation();

        // start a new walk
        if (walkStart == null) {
            walkStart = chooseRandomWall();
        }

        // do random walk
    }

    private Cell chooseRandomWall() {
        return generateRandomCell();
    }
}
