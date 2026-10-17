package com.avandy.citya.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.renderer.queue.RenderQueue;

import java.util.Random;

public class CloudFactory {

    public static Node createCloud(AssetManager am, long seed) {
        Node cloud = new Node("cloud");
        Random rnd = new Random(seed);

        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 0.95f, 0.9f, 0.5f)); // Полупрозрачный
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);

        // Состоит из 5-8 сфер разного размера
        int puffs = 5 + rnd.nextInt(4);
        for (int i = 0; i < puffs; i++) {
            float radius = 3f + rnd.nextFloat() * 4f;
            Geometry puff = new Geometry("puff", new Sphere(8, 8, radius));
            puff.setMaterial(mat);
            puff.setLocalTranslation(
                    (rnd.nextFloat() - 0.5f) * 12f,
                    (rnd.nextFloat() - 0.5f) * 4f,
                    (rnd.nextFloat() - 0.5f) * 12f
            );
            cloud.attachChild(puff);
        }

        cloud.setQueueBucket(RenderQueue.Bucket.Transparent);
        return cloud;
    }
}