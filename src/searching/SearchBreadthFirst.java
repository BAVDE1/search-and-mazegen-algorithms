package searching;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class SearchBreadthFirst extends Runner {
    public SearchBreadthFirst(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void start() {
        super.start();
        Cell start = maze.getStartCell();
        for (Vec2 neighbour : maze.getEmptyNeighbors(start.pos, 1)) {
            queue.add(new Cell(neighbour));
        }
    }

    @Override
    public void performOperation() {
        if (complete) return;
        super.performOperation();
        visitFocussingCells(array);

        if (queue.isEmpty()) {
            finishSearch();
            return;
        }

        Cell cell = queue.remove();
        if (maze.get(cell.pos) == Maze.END) {
            focusOnCellParents(cell);
            finishSearch();
            return;
        }

        // set focus on full branch
        maze.set(cell.pos, Maze.FOCUSING);
        focusOnCellParents(cell);

        ArrayList<Vec2> neighbors = maze.getEmptyNeighbors(cell.pos, 1);
        for (Vec2 neighbour : neighbors) {
            Cell c = new Cell(neighbour);
            c.parent = cell;
            queue.add(c);
        }

        // always switching current branch
        Cell p = cell;
        while (p != null) {
            array.add(p);
            p = p.parent;
        }
    }
}
