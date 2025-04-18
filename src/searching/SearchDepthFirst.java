package searching;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.ArrayList;

public class SearchDepthFirst extends Runner {
    public SearchDepthFirst(Maze maze, Game game) {
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

        if (stack.empty()) {
            finishSearch();
            return;
        }

        Cell cell = stack.pop();
        if (maze.get(cell.pos) == Maze.END) {
            finishSearch();
            return;
        }

        // set focus on full branch
        Cell p = cell;
        while (p != null) {
            maze.set(p.pos, Maze.FOCUSING);
            p = p.parent;
        }

        ArrayList<Vec2> neighbors = maze.getEmptyNeighbors(cell.pos, 1);
        for (Vec2 neighbour : neighbors) {
            Cell c = new Cell(neighbour);
            c.parent = cell;
            stack.add(c);
        }

        // end of this branch, stop focussing
        if (neighbors.isEmpty()) {
            Cell parentToClear = cell;
            while (parentToClear != null) {
                array.add(parentToClear);
                parentToClear = parentToClear.parent;
            }
        }
    }
}
