package com.avandy.citya.core;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;

import java.nio.ByteBuffer;

public class SunsetSky {

    public static void apply(Node rootNode, ViewPort viewPort, AssetManager assetManager) {
        // 1. Голубое небо
        Texture skyTexture = createBlueSky();
        rootNode.attachChild(SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.EquirectMap));

        // 2. Солнце (яркий белый диск)
        Geometry sun = new Geometry("Sun", new Sphere(16, 16, 8f));
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", new ColorRGBA(1f, 1f, 0.95f, 1f)); // Почти белый
        sun.setMaterial(sunMat);
        sun.setLocalTranslation(-100f, 20f, -150f);
        rootNode.attachChild(sun);

        // 3. Дневной свет от солнца
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setDirection(new Vector3f(-0.5f, -0.15f, -1f).normalizeLocal());
        sunLight.setColor(new ColorRGBA(1f, 0.98f, 0.90f, 1f).multLocal(2.5f)); // Теплый белый
        rootNode.addLight(sunLight);

        // 4. Рассеянный свет (яркий, чтобы не было глубоких теней)
        AmbientLight amb = new AmbientLight();
        amb.setColor(new ColorRGBA(0.65f, 0.70f, 0.75f, 1f)); // Светло-голубоватый
        rootNode.addLight(amb);
    }

    private static Texture createBlueSky() {
        int w = 2, h = 256;
        ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);

        // --- ПАЛИТРА ГОЛУБОГО НЕБА ---
        // Верхняя часть: глубокий синий (Deep Sky Blue)
        ColorRGBA top    = new ColorRGBA(0.10f, 0.40f, 0.80f, 1f);
        // Середина: классический голубой (Sky Blue)
        ColorRGBA mid    = new ColorRGBA(0.35f, 0.65f, 0.95f, 1f);
        // Ниже: светло-голубой (Light Sky Blue)
        ColorRGBA low    = new ColorRGBA(0.70f, 0.85f, 1.00f, 1f);
        // У горизонта: белесый / пастельный (почти белый с голубым оттенком)
        ColorRGBA bottom = new ColorRGBA(0.90f, 0.95f, 1.00f, 1f);

        for (int y = 0; y < h; y++) {
            float f = 1f - (float) y / (h - 1); // 0 вверху, 1 внизу
            ColorRGBA c;

            if (f < 0.5f) {
                // Переход от глубокого синего к голубому
                c = top.interpolateLocal(mid, f / 0.5f);
            } else if (f < 0.85f) {
                // Переход от голубого к светло-голубому
                c = mid.interpolateLocal(low, (f - 0.5f) / 0.35f);
            } else {
                // Переход от светло-голубого к белому
                c = low.interpolateLocal(bottom, (f - 0.85f) / 0.15f);
            }

            for (int x = 0; x < w; x++) {
                buf.put((byte) (clamp(c.r) * 255));
                buf.put((byte) (clamp(c.g) * 255));
                buf.put((byte) (clamp(c.b) * 255));
                buf.put((byte) 255);
            }
        }
        buf.flip();

        Image img = new Image(Image.Format.RGBA8, w, h, buf);
        Texture2D tex = new Texture2D(img);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        return tex;
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}