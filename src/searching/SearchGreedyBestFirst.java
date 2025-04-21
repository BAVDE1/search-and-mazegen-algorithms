package searching;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class SearchGreedyBestFirst extends Runner {
    CellComparison comparison = (Cell c) -> (int) (c.pos.y + (maze.getGridSize() - c.pos.x));

    public SearchGreedyBestFirst(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        Cell start = maze.getStartCell();
        for (Vec2 neighbour : maze.getEmptyNeighbors(start.pos, 1)) {
            stack.add(new Cell(neighbour));
        }
    }

    @Override
    public void performOperation() {
        if (complete) return;
        super.performOperation();
        visitFocussingCells(array);
        stackSort(comparison);

        if (stack.empty()) {
            finishSearch();
            return;
        }

        Cell cell = stack.pop();
        if (maze.get(cell.pos) == Maze.END) {
            focusOnCellParents(cell);
            finishSearch();
            return;
        }

        maze.set(cell.pos, Maze.FOCUSING);
        focusOnCellParents(cell);

        ArrayList<Vec2> neighbors = maze.getEmptyNeighbors(cell.pos, 1);
        for (Vec2 neighbour : neighbors) {
            Cell c = new Cell(neighbour);
            c.parent = cell;
            stack.add(c);
        }

        visitCellAndParents(cell);
    }
}
