package com.avandy.citya;

import com.avandy.citya.core.MazeGenerator;
import com.avandy.citya.core.SunsetSky;
import com.avandy.citya.player.FirstPersonController;
import com.avandy.citya.world.CloudFactory;
import com.avandy.citya.world.WorldBuilder;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferAllocatorFactory;
import com.jme3.util.ReflectionAllocator;

import java.util.Random;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        System.setProperty(
                BufferAllocatorFactory.PROPERTY_BUFFER_ALLOCATOR_IMPLEMENTATION,
                ReflectionAllocator.class.getName());

        Main app = new Main();
        AppSettings s = new AppSettings(true);
        s.setTitle("Прогулка по улицам — закат (3D)");
        s.setResolution(1280, 720);
        s.setVSync(true);
        s.setSamples(0);   // отключаем сглаживание — на AMD оно ломает постобработку
        app.setSettings(s);
        app.setShowSettings(false);
        app.start();
    }

    private int[][] maze;
    private int centerX, centerY;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        // ✅ Ключевое: уменьшаем near-плоскость, чтобы камера не прорезала стены вплотную
        cam.setFrustumPerspective(
                45f,
                (float) cam.getWidth() / cam.getHeight(),
                0.01f,   // near — было по умолчанию 1f, стало 0.01f
                200f);   // far — 200 метров

        BulletAppState bullet = new BulletAppState();
        bullet.setSpeed(1.0f);
        stateManager.attach(bullet);

        int size = 31;
        MazeGenerator gen = new MazeGenerator(size, 1, 1);
        maze = gen.getMaze();
        centerX = gen.getCenterX();
        centerY = gen.getCenterY();

        SunsetSky.apply(rootNode, viewPort, assetManager);

        WorldBuilder wb = new WorldBuilder(assetManager, rootNode, bullet);
        wb.buildFloor(maze.length);
        wb.buildWalls(maze);
        wb.buildWallPhrases(maze);
        wb.buildTrees(maze, centerX, centerY);
        wb.buildCenterFigure(centerX, centerY);

        Random rnd = new Random();
        for (int i = 0; i < 20; i++) {
            Node cloud = CloudFactory.createCloud(assetManager, rnd.nextLong());
            float a = rnd.nextFloat() * FastMath.TWO_PI;
            float dist = 80 + rnd.nextFloat() * 60;
            float y = 30 + rnd.nextFloat() * 20;

            cloud.setLocalTranslation(
                    FastMath.cos(a) * dist, y, FastMath.sin(a) * dist);

            // Чтобы облака были всегда повернуты к игроку (billboard для групп)
            cloud.lookAt(cam.getLocation(), Vector3f.UNIT_Y);

            rootNode.attachChild(cloud);
        }

        FirstPersonController pc = new FirstPersonController(
                cam, inputManager, rootNode, maze, bullet);
        pc.setSpawn(new Vector3f(1.5f, 1.0f, 1.5f), 0f);
        stateManager.attach(pc);

        createCrosshair();
    }

    private void createCrosshair() {
        com.jme3.font.BitmapFont font =
                assetManager.loadFont("Interface/Fonts/Default.fnt");
        com.jme3.font.BitmapText ch = new com.jme3.font.BitmapText(font);
        ch.setText("+");
        ch.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        ch.setColor(com.jme3.math.ColorRGBA.White);
        ch.setLocalTranslation(
                settings.getWidth()  / 2f - ch.getLineWidth()  / 2f,
                settings.getHeight() / 2f + ch.getLineHeight() / 2f,
                0);
        guiNode.attachChild(ch);
    }

    @Override
    public void simpleUpdate(float tpf) { }

    @Override
    public void simpleRender(RenderManager rm) { }
}