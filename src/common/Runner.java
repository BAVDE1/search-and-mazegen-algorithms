package common;

import boilerplate.utility.Vec2;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Runner {
    public static class Cell {
        public Vec2 pos;
        public Vec2 inBetweenCell;

        public Cell() {}
        public Cell(Vec2 pos) {
            this.pos = pos;
        }
        public Cell(Vec2 pos, Vec2 inBetweenCell) {
            this.pos = pos;
            this.inBetweenCell = inBetweenCell;
        }

        public boolean hasInBetweenCell() {
            return inBetweenCell != null;
        }
    }

    public int opNum = 0;
    public int frameNum = 0;
    public int opFrameNum = 0;

    public boolean useFPO = true;
    public int framesPerOp = 1;
    public int opPerFrames = 1;

    public boolean running = false;
    public boolean paused = false;
    public boolean complete = false;

    public Maze maze;
    public Game game;
    public ThreadLocalRandom random = ThreadLocalRandom.current();

    public Stack<Cell> stack = new Stack<>();

    public Runner(Maze maze, Game game) {
        this.maze = maze;
        this.game = game;
    }

    public void start() {
        if (running) return;
        running = true;
    }

    public void pause() {
        if (paused) return;
        paused = true;
    }

    public void resume() {
        if (!paused) return;
        paused = false;
    }

    public void finish() {
        if (complete) return;
        complete = true;
    }

    public void reset() {
        opNum = 0;
        frameNum = 0;
        opFrameNum = 0;

        running = false;
        paused = false;
        complete = false;
        stack.clear();
    }

    public void nextFrame() {
        if (complete || !running || paused) return;
        frameNum++;
        opFrameNum++;

        // instant complete
        if (framesPerOp == 0 || opPerFrames == 0) {
            while (!complete) performOperation();
        }

        // frames between operations
        if (useFPO && opFrameNum == framesPerOp) performOperation();

        // multiple operations per frame
        if (!useFPO) {
            int i = 0;
            while (i < opPerFrames && !complete) {
                i++;
                performOperation();
            }
        }
    }

    public void performOperation() {
        opNum++;
        opFrameNum = 0;
    };

    public Cell generateRandomCell() {
        Vec2 startPos;
        do {
            int x = random.nextInt(0, maze.getGridSize() + 1);
            int y = random.nextInt(0, maze.getGridSize() + 1);
            startPos = new Vec2(x, y).sub(x % 2, y % 2);
        } while (maze.get(startPos) != Maze.WALL);
        return new Cell(startPos);
    }
}
