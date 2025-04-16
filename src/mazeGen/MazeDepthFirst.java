package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class MazeDepthFirst extends Runner {
    public MazeDepthFirst(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        Cell startCell = generateRandomCell();
        stack.add(startCell);
        maze.set(startCell.pos, Maze.FOCUSING);
    }

    @Override
    public void performOperation() {
        super.performOperation();

        if (stack.empty()) {
            finishMaze();
            return;
        }

        Cell cell = stack.pop();
        ArrayList<Vec2> neighbours = maze.getWallNeighbours(cell.pos, 2);

        if (neighbours.isEmpty()) {
            maze.set(cell.pos, Maze.EMPTY);
            if (cell.hasInBetweenCell()) maze.set(cell.inBetweenCell, Maze.EMPTY);
            return;
        }

        int randInx = random.nextInt(0, neighbours.size());
        Cell chosenNeighbour = new Cell(neighbours.get(randInx));
        chosenNeighbour.inBetweenCell = maze.getCellInBetween(cell.pos, chosenNeighbour.pos);

        stack.push(cell);
        stack.push(chosenNeighbour);

        // set colours
        if (chosenNeighbour.hasInBetweenCell()) maze.set(chosenNeighbour.inBetweenCell, Maze.FOCUSING);
        maze.set(cell.pos, Maze.FOCUSING);
        maze.set(chosenNeighbour.pos, Maze.FOCUSING);
    }
}
