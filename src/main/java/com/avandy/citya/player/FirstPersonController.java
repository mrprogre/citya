package com.avandy.citya.player;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class FirstPersonController extends BaseAppState
        implements ActionListener, AnalogListener {

    private final Camera cam;
    private final com.jme3.input.InputManager input;
    private final Node root;
    private final BulletAppState bullet;

    private BetterCharacterControl physics;
    private Node playerNode;

    private final float moveSpeed = 3.5f;

    // ✅ Чувствительность повышена (было 0.08f)
    private final float mouseSensitivity = 0.20f;

    private float yaw = 0f;
    private float pitch = 0f;

    private boolean fwd, back, left, right, jump;

    public FirstPersonController(Camera cam,
                                 com.jme3.input.InputManager input,
                                 Node root,
                                 int[][] maze,
                                 BulletAppState bullet) {
        this.cam = cam;
        this.input = input;
        this.root = root;
        this.bullet = bullet;
    }

    public void setSpawn(Vector3f pos, float yawRad) {
        this.yaw = yawRad;
        this.pitch = 0;
    }

    @Override
    protected void initialize(Application app) {
        physics = new BetterCharacterControl(0.25f, 0.9f, 70f);
        physics.setJumpForce(new Vector3f(0, 300f, 0));
        physics.setGravity(new Vector3f(0, -25f, 0));

        playerNode = new Node("player");
        playerNode.addControl(physics);
        root.attachChild(playerNode);

        bullet.getPhysicsSpace().add(physics);

        Vector3f spawn = new Vector3f(1.5f, 1.0f, 1.5f);
        playerNode.setLocalTranslation(spawn);
        physics.warp(spawn);

        // Движение
        input.addMapping("fwd",   new KeyTrigger(KeyInput.KEY_W),
                new KeyTrigger(KeyInput.KEY_UP));
        input.addMapping("back",  new KeyTrigger(KeyInput.KEY_S),
                new KeyTrigger(KeyInput.KEY_DOWN));
        input.addMapping("left",  new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT));
        input.addMapping("right", new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));
        input.addMapping("jump",  new KeyTrigger(KeyInput.KEY_SPACE));

        // Раздельная привязка осей мыши
        input.addMapping("mouseLeft",  new MouseAxisTrigger(MouseInput.AXIS_X, true));
        input.addMapping("mouseRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        input.addMapping("mouseUp",    new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        input.addMapping("mouseDown",  new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        input.addListener(this, "fwd", "back", "left", "right", "jump");
        input.addListener((AnalogListener) this,
                "mouseLeft", "mouseRight", "mouseUp", "mouseDown");

        input.setCursorVisible(false);
    }

    @Override
    protected void cleanup(Application app) {
        if (bullet != null && physics != null) {
            PhysicsSpace space = bullet.getPhysicsSpace();
            if (space != null) space.remove(physics);
        }
    }

    @Override protected void onEnable() { }
    @Override protected void onDisable() { }

    @Override
    public void update(float tpf) {
        // 1. Сначала поворот камеры (чтобы near-плоскость смотрела правильно)
        cam.setRotation(new Quaternion().fromAngles(pitch, yaw, 0));

        // 2. Движение
        Vector3f camDir  = cam.getDirection().clone().setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();

        Vector3f walkDir = new Vector3f();
        if (fwd)   walkDir.addLocal(camDir);
        if (back)  walkDir.addLocal(camDir.negate());
        if (left)  walkDir.addLocal(camLeft);
        if (right) walkDir.addLocal(camLeft.negate());

        if (walkDir.lengthSquared() > 0) walkDir.normalizeLocal();
        physics.setWalkDirection(walkDir.mult(moveSpeed));

        if (jump) {
            physics.jump();
            jump = false;
        }

        // 3. Позиция камеры — по центру игрока, но с подъёмом на уровень глаз.
        //    НЕ отодвигаем назад, потому что при повороте это смещает камеру вбок.
        //    Вместо этого уменьшим near-плоскость (см. Main.java).
        Vector3f pos = playerNode.getWorldTranslation();
        cam.setLocation(pos.add(0, 0.4f, 0));
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "fwd"   -> fwd   = isPressed;
            case "back"  -> back  = isPressed;
            case "left"  -> left  = isPressed;
            case "right" -> right = isPressed;
            case "jump"  -> jump  = isPressed;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case "mouseLeft"  -> yaw += value * mouseSensitivity;
            case "mouseRight" -> yaw -= value * mouseSensitivity;
            case "mouseUp"    -> pitch -= value * mouseSensitivity;
            case "mouseDown"  -> pitch += value * mouseSensitivity;
        }
        pitch = FastMath.clamp(pitch,
                -FastMath.PI / 2f + 0.05f,
                FastMath.PI / 2f - 0.05f);
    }
}