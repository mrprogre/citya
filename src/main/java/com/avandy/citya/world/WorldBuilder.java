package com.avandy.citya.world;

import com.avandy.citya.core.Common;
import com.avandy.citya.core.MazeGenerator;
import com.avandy.citya.model.Tree;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldBuilder {

    public static final float WALL_HEIGHT = 1.0f;

    private final AssetManager am;
    private final Node root;
    private final BulletAppState bullet;
    private final List<Tree> trees = new ArrayList<>();

    private final ColorRGBA[] WALL_COLORS = {
            new ColorRGBA(0.47f, 0.35f, 0.29f, 1f),
            new ColorRGBA(0.41f, 0.41f, 0.47f, 1f),
            new ColorRGBA(0.43f, 0.39f, 0.35f, 1f),
            new ColorRGBA(0.37f, 0.41f, 0.45f, 1f)
    };

    public WorldBuilder(AssetManager am, Node root, BulletAppState bullet) {
        this.am = am;
        this.root = root;
        this.bullet = bullet;
    }

    public List<Tree> getTrees() { return trees; }

    // ====== ПОЛ ======
    public void buildFloor(int size) {
        Box floor = new Box(size, 0.01f, size);
        Geometry g = new Geometry("floor", floor);
        Material mat = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse",  new ColorRGBA(0.24f, 0.19f, 0.18f, 1f));
        mat.setColor("Ambient",  new ColorRGBA(0.20f, 0.16f, 0.16f, 1f));
        mat.setColor("Specular", new ColorRGBA(0.05f, 0.05f, 0.05f, 1f));
        mat.setFloat("Shininess", 4f);
        g.setMaterial(mat);
        g.setLocalTranslation(size / 2f, -0.01f, size / 2f);
        root.attachChild(g);

        BoxCollisionShape shape = new BoxCollisionShape(
                new Vector3f(size, 0.01f, size));
        RigidBodyControl rbc = new RigidBodyControl(shape, 0f);
        g.addControl(rbc);
        bullet.getPhysicsSpace().add(rbc);
    }

    // ====== СТЕНЫ ======
    // Склеиваем по Z. Коллайдер чуть шире визуального блока — нет микрощелей на стыках.
    public void buildWalls(int[][] maze) {
        int size = maze.length;

        for (int x = 0; x < size; x++) {
            int runStart = -1;
            for (int y = 0; y <= size; y++) {
                boolean isWall = (y < size) && maze[x][y] == MazeGenerator.WALL;
                if (isWall && runStart < 0) runStart = y;
                if (!isWall && runStart >= 0) {
                    int len = y - runStart;
                    addWallBlockZ(x, runStart, len);
                    runStart = -1;
                }
            }
        }
    }

    private void addWallBlockZ(int x, int zStart, int len) {
        Box box = new Box(0.5f, WALL_HEIGHT / 2f, len / 2f);
        Geometry g = new Geometry("wall", box);
        Material mat = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        ColorRGBA c = WALL_COLORS[Math.floorMod(x * 31 + zStart * 17, WALL_COLORS.length)];
        mat.setColor("Diffuse",  c);
        mat.setColor("Ambient",  c.mult(0.5f));
        mat.setColor("Specular", new ColorRGBA(0.05f, 0.05f, 0.05f, 1f));
        mat.setFloat("Shininess", 4f);
        g.setMaterial(mat);
        g.setLocalTranslation(x + 0.5f, WALL_HEIGHT / 2f, zStart + len / 2f);
        root.attachChild(g);

        float pad = 0.02f;
        BoxCollisionShape shape = new BoxCollisionShape(
                new Vector3f(0.5f + pad, WALL_HEIGHT / 2f, len / 2f + pad));
        RigidBodyControl rbc = new RigidBodyControl(shape, 0f);
        g.addControl(rbc);
        bullet.getPhysicsSpace().add(rbc);
    }

    // ====== НАДПИСИ НА СТЕНАХ ======
    public void buildWallPhrases(int[][] maze) {
        int size = maze.length;
        BitmapFont font = am.loadFont("Interface/Fonts/Default.fnt");
        Random rnd = new Random();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (maze[x][y] != MazeGenerator.WALL) continue;
                if (x == 0 || y == 0 || x == size - 1 || y == size - 1) continue;
                if (rnd.nextDouble() > 0.35) continue;

                Vector3f normal = null;
                if (!MazeGenerator.isWall(maze, x, y - 1)) normal = new Vector3f(0, 0, -1);
                else if (!MazeGenerator.isWall(maze, x, y + 1)) normal = new Vector3f(0, 0, 1);
                else if (!MazeGenerator.isWall(maze, x - 1, y)) normal = new Vector3f(-1, 0, 0);
                else if (!MazeGenerator.isWall(maze, x + 1, y)) normal = new Vector3f(1, 0, 0);
                if (normal == null) continue;

                String text = Common.WALL_PHRASES[rnd.nextInt(Common.WALL_PHRASES.length)];

                BitmapText bt = new BitmapText(font, false);
                bt.setText(text);
                bt.setSize(0.18f);
                bt.setColor(new ColorRGBA(0.98f, 0.92f, 0.78f, 1f));
                bt.setQueueBucket(RenderQueue.Bucket.Transparent);

                Vector3f pos = new Vector3f(x + 0.5f, 0.55f, y + 0.5f)
                        .addLocal(normal.mult(0.52f));
                bt.setLocalTranslation(pos);

                float angle = (float) Math.atan2(normal.x, normal.z);
                bt.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));

                root.attachChild(bt);
            }
        }
    }

    // ====== ДЕРЕВЬЯ ======
    public void buildTrees(int[][] maze, int centerX, int centerY) {
        int size = maze.length;
        Random rnd = new Random();

        for (int x = 1; x < size - 1; x++) {
            for (int y = 1; y < size - 1; y++) {
                if (maze[x][y] != MazeGenerator.FLOOR) continue;
                if (Math.abs(x - centerX) <= 2 && Math.abs(y - centerY) <= 2) continue;
                if (x == 1 && y == 1) continue;
                if (rnd.nextDouble() > 0.18) continue;

                Tree t = new Tree();
                t.x = x + 0.5;
                t.y = y + 0.5;
                t.type = rnd.nextInt(3);
                t.scale = 0.7 + rnd.nextDouble() * 0.7;
                t.seed = rnd.nextLong();
                t.phase = rnd.nextDouble() * Math.PI * 2;
                t.appleCount = (t.type == 1) ? 3 + rnd.nextInt(5) : 0;
                trees.add(t);

                Node treeNode = TreeFactory.create(am, t);
                treeNode.setLocalTranslation((float) t.x, 0f, (float) t.y);
                root.attachChild(treeNode);
            }
        }
    }

    // ====== ФИГУРА В ЦЕНТРЕ ======
    public void buildCenterFigure(int centerX, int centerY) {
        Random rnd = new Random();
        int figureType = rnd.nextInt(5);

        Material mat = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse",  new ColorRGBA(1f, 0.86f, 0.39f, 1f));
        mat.setColor("Ambient",  new ColorRGBA(0.5f, 0.42f, 0.2f, 1f));
        mat.setColor("Specular", new ColorRGBA(0.9f, 0.8f, 0.4f, 1f));
        mat.setFloat("Shininess", 32f);

        Geometry figure;
        Vector3f zero = Vector3f.ZERO;

        switch (figureType) {
            case 0 -> figure = new Geometry("star",
                    new Dome(zero, 8, 8, 0.4f));
            case 1 -> figure = new Geometry("diamond",
                    new Dome(zero, 4, 4, 0.4f));
            case 2 -> figure = new Geometry("sphere",
                    new Sphere(16, 16, 0.4f));
            case 3 -> figure = new Geometry("pyramid",
                    new Dome(zero, 2, 4, 0.4f));
            default -> figure = new Geometry("box",
                    new Box(0.3f, 0.3f, 0.3f));
        }

        figure.setMaterial(mat);
        figure.setLocalTranslation(centerX + 0.5f, 0.8f, centerY + 0.5f);
        root.attachChild(figure);

        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(centerX + 0.5f, 1.5f, centerY + 0.5f));
        pl.setColor(new ColorRGBA(1f, 0.86f, 0.5f, 1f).multLocal(2.5f));
        pl.setRadius(6f);
        root.addLight(pl);
    }
}