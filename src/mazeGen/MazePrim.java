package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class MazePrim extends Runner {
    public MazePrim(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        Cell startCell = generateRandomCell();
        addWallsToArray(startCell.pos);
        maze.set(startCell.pos, Maze.EMPTY);
    }

    @Override
    public void performOperation() {
        super.performOperation();

        if (array.isEmpty()) {
            clearFocussingCells(arrayOther);
            finishMaze();
            return;
        }

        int randInx = random.nextInt(0, array.size());
        Cell wall = array.get(randInx);
        array.remove(wall);

        ArrayList<Vec2> visitedNeighborCells = maze.getNonWallNeighbors(wall.pos, 1);
        if (visitedNeighborCells.size() != 1) return;

        Vec2 visitedPos = visitedNeighborCells.getFirst();
        Vec2 unvisitedPos = wall.pos.add(wall.pos.sub(visitedPos));
        maze.set(wall.pos, Maze.FOCUSING);
        maze.set(unvisitedPos, Maze.FOCUSING);
        addWallsToArray(unvisitedPos);

        clearFocussingCells(arrayOther);
        arrayOther.add(wall);
        arrayOther.add(new Cell(unvisitedPos));
    }

    private void addWallsToArray(Vec2 cellPos) {
        ArrayList<Vec2> wallPositions = maze.getWallNeighbours(cellPos, 1);
        for (Vec2 wall : wallPositions) array.add(new Cell(wall));
    }
}
