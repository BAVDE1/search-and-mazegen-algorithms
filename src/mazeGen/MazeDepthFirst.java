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
    public void finish() {
        super.finish();
        maze.placeStartEndPoints();
        game.mazeGenerationCompleted();
    }

    @Override
    public void start() {
        if (running) return;
        super.start();
        Cell startCell = generateRandomCell();
        stack.add(startCell);
        maze.set(startCell.pos, Maze.FOCUSING);
    }

    @Override
    public void performOperation() {
        super.performOperation();

        if (stack.empty()) {
            finish();
            return;
        }

        Cell cell = stack.pop();
        ArrayList<Vec2> neighbours = maze.getWallNeighbours(cell.pos, 2);

        if (neighbours.isEmpty()) {
            maze.set(cell.pos, Maze.EMPTY);
            if (cell.hasInBetweenCell()) maze.set(cell.inBetweenCell, Maze.EMPTY);
            return;
        }

        int inx = random.nextInt(0, neighbours.size());
        Cell chosenCell = new Cell(neighbours.get(inx));
        chosenCell.inBetweenCell = maze.getCellInBetween(cell.pos, chosenCell.pos);

        stack.push(cell);
        stack.push(chosenCell);

        if (chosenCell.hasInBetweenCell()) maze.set(chosenCell.inBetweenCell, Maze.FOCUSING);
        maze.set(cell.pos, Maze.FOCUSING);
        maze.set(chosenCell.pos, Maze.FOCUSING);
    }
}
