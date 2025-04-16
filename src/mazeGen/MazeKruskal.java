package mazeGen;

import boilerplate.utility.Vec2;
import common.Game;
import common.Maze;
import common.Runner;

import java.util.Map;

public class MazeKruskal extends Runner {
    public static class Tree {
        Tree parent;

        /** Retrieves the very first parent */
        public Tree root() {
            return parent != null ? parent.root() : this;
        }

        public boolean connected(Tree otherTree) {
            return root() == otherTree.root();
        }

        public void connect(Tree otherTree) {
            otherTree.root().parent = this;
        }
    }

    int N = 1;
    int E = 2;
    int S = 3;
    int W = 4;
    Map<Integer, Integer> otherX = Map.of(N, 0, E, 2, S, 0, W, -2);  // move in twos
    Map<Integer, Integer> otherY = Map.of(N, -2, E, 0, S, 2, W, 0);

    Tree[][] sets = new Tree[maze.getGridSize()][maze.getGridSize()];

    public MazeKruskal(Maze maze, Game game) {
        super(maze, game);
    }

    @Override
    public void reset() {
        super.reset();
        sets = new Tree[maze.getGridSize()][maze.getGridSize()];
    }

    @Override
    public void start() {
        super.start();

        // grab a set of all walls (separated in twos cause the walls are every 2nd rows and columns of the grid)
        for (int y = 0; y < maze.getGridSize(); y += 2) {
            for (int x = 0; x < maze.getGridSize(); x += 2) {
                Vec2 pos = new Vec2(x, y);
                Cell cellA = new Cell(pos);
                Cell cellB = new Cell(pos);
                cellA.direction = N;
                cellB.direction = W;
                if (y > 0) stack.add(cellA);
                if (x > 0) stack.add(cellB);
            }
        }

        try {
            stack.sort((_, _) -> random.nextInt());
        } catch (IllegalArgumentException _) {  // rarely an error is thrown, just retry (maybe cause nextInt generated a duplicate number)
            start();
        }
    }

    @Override
    public void performOperation() {
        super.performOperation();

        if (stack.empty()) {
            clearFocussingCells(array);
            finishMaze();
            return;
        }

        Cell cellA = stack.pop();
        Cell cellB = new Cell(new Vec2(
                cellA.pos.x + otherX.get(cellA.direction),
                cellA.pos.y + otherY.get(cellA.direction)
        ));

        Tree set1 = sets[(int) cellA.pos.y][(int) cellA.pos.x];
        Tree set2 = sets[(int) cellB.pos.y][(int) cellB.pos.x];
        if (set1 == null) {
            set1 = new Tree();
            sets[(int) cellA.pos.y][(int) cellA.pos.x] = set1;
        }
        if (set2 == null) {
            set2 = new Tree();
            sets[(int) cellB.pos.y][(int) cellB.pos.x] = set2;
        }

        if (!set1.connected(set2)) {
            set1.connect(set2);

            // set & update colours
            cellA.inBetweenCell = maze.getCellInBetween(cellA.pos, cellB.pos);
            maze.set(cellA.pos, Maze.FOCUSING);
            maze.set(cellB.pos, Maze.FOCUSING);
            maze.set(cellA.inBetweenCell, Maze.FOCUSING);

            clearFocussingCells(array);
            array.add(cellA);
            array.add(cellB);
        }
    }
}
