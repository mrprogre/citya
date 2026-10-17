package com.avandy.citya.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class MazeGenerator {
    public static final int WALL = 1;
    public static final int FLOOR = 0;

    private final int[][] maze;
    private final int size;
    private final int centerX, centerY;

    public MazeGenerator(int size, int startX, int startY) {
        this.size = size;
        this.maze = new int[size][size];
        this.centerX = size / 2;
        this.centerY = size / 2;

        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                maze[x][y] = WALL;

        Random rnd = new Random();
        carve(startX, startY, rnd);

        // Открытая площадка в центре
        for (int x = centerX - 1; x <= centerX + 1; x++)
            for (int y = centerY - 1; y <= centerY + 1; y++)
                maze[x][y] = FLOOR;
    }

    private void carve(int sx, int sy, Random rnd) {
        maze[sx][sy] = FLOOR;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{sx, sy});

        int[] dx = {2, -2, 0, 0};
        int[] dy = {0, 0, 2, -2};

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int x = cur[0], y = cur[1];

            int[] order = {0, 1, 2, 3};
            for (int i = 3; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int t = order[i]; order[i] = order[j]; order[j] = t;
            }

            boolean advanced = false;
            for (int k : order) {
                int nx = x + dx[k];
                int ny = y + dy[k];
                if (nx > 0 && ny > 0 && nx < size - 1 && ny < size - 1
                        && maze[nx][ny] == WALL) {
                    maze[(x + nx) / 2][(y + ny) / 2] = FLOOR;
                    maze[nx][ny] = FLOOR;
                    stack.push(new int[]{nx, ny});
                    advanced = true;
                    break;
                }
            }
            if (!advanced) stack.pop();
        }
    }

    public int[][] getMaze() { return maze; }
    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }

    public static boolean isWall(int[][] m, int x, int y) {
        if (x < 0 || y < 0 || x >= m.length || y >= m.length) return true;
        return m[x][y] == WALL;
    }
}