package com.avandy.citya.world;

import com.avandy.citya.model.Tree;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

import java.util.Random;

public class TreeFactory {

    public static Node create(AssetManager am, Tree t) {
        Node n = new Node("tree");
        Random rnd = new Random(t.seed);

        float s = (float) t.scale;
        Material trunkMat = litMat(am, new ColorRGBA(0.35f, 0.24f, 0.16f, 1f), 4f);

        switch (t.type) {
            case 0 -> { // Ёлка
                // Ствол (цилиндр вдоль Z, поворачиваем на 90° по X, чтобы был вертикально)
                Cylinder trunkMesh = new Cylinder(6, 8, 0.06f * s, 0.6f * s, true);
                Geometry trunk = new Geometry("trunk", trunkMesh);
                trunk.setMaterial(trunkMat);
                trunk.rotate(FastMath.HALF_PI, 0, 0); // поворот: Z -> Y
                trunk.setLocalTranslation(0, 0.3f * s, 0);
                n.attachChild(trunk);

                // 3 уровня ёлки (используем цилиндры с разным радиусом и высотой)
                for (int i = 0; i < 3; i++) {
                    float baseY = (0.3f + i * 0.25f) * s;
                    float radius = (0.35f - i * 0.08f) * s;
                    float height = 0.4f * s;

                    Cylinder coneMesh = new Cylinder(6, 8, radius, height, true);
                    Geometry cone = new Geometry("cone", coneMesh);
                    cone.setMaterial(litMat(am,
                            new ColorRGBA(0.20f + i * 0.06f,
                                    0.34f + i * 0.04f,
                                    0.16f, 1f), 4f));
                    cone.rotate(FastMath.HALF_PI, 0, 0);
                    cone.setLocalTranslation(0, baseY + height / 2f, 0);
                    n.attachChild(cone);
                }
            }
            case 1 -> { // Лиственное
                // Ствол
                Cylinder trunkMesh = new Cylinder(6, 8, 0.08f * s, 0.7f * s, true);
                Geometry trunk = new Geometry("trunk", trunkMesh);
                trunk.setMaterial(trunkMat);
                trunk.rotate(FastMath.HALF_PI, 0, 0);
                trunk.setLocalTranslation(0, 0.35f * s, 0);
                n.attachChild(trunk);

                // Крона — 5 шаров
                for (int i = 0; i < 5; i++) {
                    double a = i * Math.PI * 2 / 5;
                    float ox = (float) Math.cos(a) * 0.25f * s;
                    float oz = (float) Math.sin(a) * 0.25f * s;
                    Geometry ball = new Geometry("leaf",
                            new Sphere(10, 10, 0.28f * s));
                    ball.setMaterial(litMat(am,
                            new ColorRGBA(0.24f + i * 0.02f,
                                    0.45f + i * 0.02f,
                                    0.20f, 1f), 4f));
                    ball.setLocalTranslation(ox, 0.85f * s, oz);
                    n.attachChild(ball);
                }

                // Яблоки
                for (int i = 0; i < t.appleCount; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2;
                    float ox = (float) Math.cos(a) * 0.35f * s;
                    float oz = (float) Math.sin(a) * 0.35f * s;
                    float oy = (0.7f + rnd.nextFloat() * 0.4f) * s;
                    Geometry apple = new Geometry("apple",
                            new Sphere(8, 8, 0.05f * s));
                    apple.setMaterial(litMat(am,
                            new ColorRGBA(0.78f, 0.16f, 0.14f, 1f), 32f));
                    apple.setLocalTranslation(ox, oy, oz);
                    n.attachChild(apple);
                }
            }
            default -> { // Кипарис
                Cylinder trunkMesh = new Cylinder(6, 8, 0.06f * s, 1.8f * s, true);
                Geometry trunk = new Geometry("trunk", trunkMesh);
                trunk.setMaterial(litMat(am,
                        new ColorRGBA(0.14f, 0.28f, 0.16f, 1f), 4f));
                trunk.rotate(FastMath.HALF_PI, 0, 0);
                trunk.setLocalTranslation(0, 0.9f * s, 0);
                n.attachChild(trunk);
            }
        }

        return n;
    }

    private static Material litMat(AssetManager am, ColorRGBA c, float shininess) {
        Material m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse",  c);
        m.setColor("Ambient",  c.mult(0.5f));
        m.setColor("Specular", new ColorRGBA(0.15f, 0.15f, 0.15f, 1f));
        m.setFloat("Shininess", shininess);
        return m;
    }
}