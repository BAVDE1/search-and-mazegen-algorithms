package common;

public class Maze {
    public static char WALL = '#';
    public static char EMPTY = ' ';

    public static char NOT_VISITED = ' ';
    public static char VISITED = '-';
    public static char FOCUS = '+';

    public int gridSize = 5;
    private char[][] mazeGrid = new char[gridSize][gridSize];
    private char[][] mazeGridStatus = new char[gridSize][gridSize];

    public void clearMaze() {
        mazeGrid = new char[gridSize][gridSize];
    }

    private void setSpaceStatus(int x, int y, char status) {
        if (!(status == NOT_VISITED || status == VISITED || status == FOCUS)) return;
        mazeGridStatus[y][x] = status;
    }

    private char getSpaceStatus(int x, int y) {
        return mazeGridStatus[y][x];
    }

    private void setOnGrid(int x, int y, char val) {
        if (!(val == WALL || val == EMPTY)) return;
        mazeGrid[y][x] = val;
    }

    private char getOnGrid(int x, int y) {
        return mazeGrid[y][x];
    }
}
