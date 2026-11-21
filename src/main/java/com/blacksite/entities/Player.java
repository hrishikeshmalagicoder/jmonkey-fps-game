package com.blacksite.entities;

import com.blacksite.audio.SoundEffects;
import com.blacksite.core.GameState;
import com.blacksite.level.LevelBuilder;
import com.blacksite.ui.HUDManager;
import com.blacksite.ui.WeaponMeshBuilder;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.List;

public class Player {

    private final Camera cam;
    private final Node rootNode;
    private final LevelBuilder levelBuilder;
    private final HUDManager hud;
    private final WeaponMeshBuilder weapon;
    private final AssetManager assetManager;

    private final Vector3f position = new Vector3f(0, 1.8f, 48f);
    private float velocityY = 0f;
    private boolean onGround = true;

    // Look angles in radians
    private float yaw = FastMath.PI; // Face North (-Z)
    private float pitch = 0f;

    // Movement flags
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean strafeLeft = false;
    private boolean strafeRight = false;
    private boolean sprinting = false;
    private boolean shooting = false;
    private boolean interacting = false;

    // Stats
    private float health = 100f;
    private final float maxHealth = 100f;
    private int currentAmmo = 30;
    private final int magSize = 30;
    private int reserveAmmo = 90;
    private boolean isReloading = false;
    private float reloadTimer = 0f;
    private float shootCooldown = 0f;

    private final float walkSpeed = 8.5f;
    private final float sprintSpeed = 13.5f;
    private final float jumpVelocity = 9.0f;
    private final float gravity = 25.0f;
    private final float playerRadius = 0.5f;

    public Player(Camera cam, Node rootNode, AssetManager assetManager, LevelBuilder levelBuilder, HUDManager hud) {
        this.cam = cam;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.levelBuilder = levelBuilder;
        this.hud = hud;

        this.weapon = new WeaponMeshBuilder(assetManager);
        // Attach weapon to camera so it renders first-person
        cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.05f, 500f);
        updateCameraRotation();
    }

    public void update(float tpf, List<SentryDroneAI> sentries, List<Projectile> activeProjectiles, GameState gameState) {
        if (gameState != GameState.PLAYING) return;

        // 1. Weapon update & timers
        weapon.update(tpf);
        if (shootCooldown > 0f) {
            shootCooldown -= tpf;
        }

        if (isReloading) {
            reloadTimer -= tpf;
            if (reloadTimer <= 0f) {
                completeReload();
            }
        }

        // 2. Shooting logic
        if (shooting && shootCooldown <= 0f && !isReloading) {
            fireWeapon(sentries, activeProjectiles);
        }

        // 3. Horizontal movement
        float speed = sprinting ? sprintSpeed : walkSpeed;
        Vector3f moveDir = new Vector3f();

        // Forward vector (horizontal plane only)
        Vector3f fwd = new Vector3f(-FastMath.sin(yaw), 0, -FastMath.cos(yaw)).normalizeLocal();
        // Right vector
        Vector3f right = new Vector3f(FastMath.cos(yaw), 0, -FastMath.sin(yaw)).normalizeLocal();

        if (moveForward) moveDir.addLocal(fwd);
        if (moveBackward) moveDir.subtractLocal(fwd);
        if (strafeRight) moveDir.addLocal(right);
        if (strafeLeft) moveDir.subtractLocal(right);

        if (moveDir.lengthSquared() > 0.001f) {
            moveDir.normalizeLocal().multLocal(speed * tpf);

            // Separate axis collision for smooth wall sliding
            float nextX = position.x + moveDir.x;
            if (!levelBuilder.checkCollision(nextX, position.z, playerRadius)) {
                position.x = nextX;
            }

            float nextZ = position.z + moveDir.z;
            if (!levelBuilder.checkCollision(position.x, nextZ, playerRadius)) {
                position.z = nextZ;
            }
        }

        // 4. Vertical physics & jumping
        velocityY -= gravity * tpf;
        position.y += velocityY * tpf;

        if (position.y <= 1.8f) {
            position.y = 1.8f;
            velocityY = 0f;
            onGround = true;
        } else {
            onGround = false;
        }

        // 5. Update Camera
        cam.setLocation(position);
        updateCameraRotation();

        // 6. Update First-Person Weapon Transform
        // Place weapon in camera coordinate space
        Node weaponPivot = weapon.getRootPivot();
        weaponPivot.setLocalTranslation(position);
        weaponPivot.setLocalRotation(cam.getRotation());
    }

    private void fireWeapon(List<SentryDroneAI> sentries, List<Projectile> activeProjectiles) {
        if (currentAmmo <= 0) {
            startReload();
            return;
        }

        currentAmmo--;
        shootCooldown = 0.13f; // ~460 RPM
        weapon.triggerRecoil();
        SoundEffects.playPlayerShoot();

        // Spawn visual tracer beam
        Vector3f origin = cam.getLocation().add(cam.getDirection().mult(0.5f));
        Vector3f dir = cam.getDirection();
        Projectile tracer = new Projectile(assetManager, origin, dir, 80.0f, 25.0f, true);
        activeProjectiles.add(tracer);
        rootNode.attachChild(tracer.getNode());

        // Hitscan raycast against sentries
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        SentryDroneAI hitSentry = null;
        float closestDist = Float.MAX_VALUE;

        for (SentryDroneAI sentry : sentries) {
            if (sentry.isDead()) continue;

            CollisionResults results = new CollisionResults();
            sentry.getNode().collideWith(ray, results);

            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                if (closest.getDistance() < closestDist) {
                    closestDist = closest.getDistance();
                    hitSentry = sentry;
                }
            }
        }

        if (hitSentry != null) {
            hitSentry.takeDamage(25.0f);
            hud.triggerHitMarker();
        }
    }

    public void startReload() {
        if (isReloading || currentAmmo >= magSize || reserveAmmo <= 0) return;

        isReloading = true;
        reloadTimer = 1.4f;
        weapon.startReload(1.4f);
        SoundEffects.playReload();
    }

    private void completeReload() {
        isReloading = false;
        int needed = magSize - currentAmmo;
        int toLoad = Math.min(needed, reserveAmmo);
        currentAmmo += toLoad;
        reserveAmmo -= toLoad;
    }

    public void jump() {
        if (onGround) {
            velocityY = jumpVelocity;
            onGround = false;
        }
    }

    public void applyMouseLook(float dx, float dy) {
        float sens = 0.0028f;
        yaw -= dx * sens;
        pitch -= dy * sens;

        // Clamp pitch to avoid gimbal flip
        float maxPitch = FastMath.HALF_PI - 0.05f;
        pitch = Math.clamp(pitch, -maxPitch, maxPitch);
    }

    private void updateCameraRotation() {
        Quaternion qYaw = new Quaternion().fromAngleAxis(yaw, Vector3f.UNIT_Y);
        Quaternion qPitch = new Quaternion().fromAngleAxis(pitch, Vector3f.UNIT_X);
        Quaternion rot = qYaw.mult(qPitch);
        cam.setRotation(rot);
    }

    public void takeDamage(float amount) {
        health = Math.max(0f, health - amount);
        hud.triggerDamageFlash();
    }

    public void reset() {
        position.set(0, 1.8f, 48f);
        velocityY = 0f;
        yaw = FastMath.PI;
        pitch = 0f;
        health = maxHealth;
        currentAmmo = magSize;
        reserveAmmo = 90;
        isReloading = false;
        shooting = false;
        updateCameraRotation();
    }

    public Node getWeaponNode() {
        return weapon.getRootPivot();
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getHealth() {
        return health;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getReserveAmmo() {
        return reserveAmmo;
    }

    public boolean isReloading() {
        return isReloading;
    }

    public boolean isInteracting() {
        return interacting;
    }

    public void setInteracting(boolean interacting) {
        this.interacting = interacting;
    }

    public void setMoveForward(boolean moveForward) { this.moveForward = moveForward; }
    public void setMoveBackward(boolean moveBackward) { this.moveBackward = moveBackward; }
    public void setStrafeLeft(boolean strafeLeft) { this.strafeLeft = strafeLeft; }
    public void setStrafeRight(boolean strafeRight) { this.strafeRight = strafeRight; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }
    public void setShooting(boolean shooting) { this.shooting = shooting; }
}
