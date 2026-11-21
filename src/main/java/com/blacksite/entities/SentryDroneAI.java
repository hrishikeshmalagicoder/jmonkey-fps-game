package com.blacksite.entities;

import com.blacksite.audio.SoundEffects;
import com.blacksite.core.MissionManager;
import com.blacksite.level.LevelBuilder;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

import java.util.List;

public class SentryDroneAI {

    public enum AIState {
        PATROL,
        ALERT,
        CHASE,
        ATTACK,
        DEAD
    }

    private final String id;
    private final Node droneNode;
    private final AssetManager assetManager;
    private final Node rootNode;
    private final MissionManager missionManager;
    private final LevelBuilder levelBuilder;

    private Vector3f position;
    private final List<Vector3f> waypoints;
    private int currentWaypointIndex = 0;

    private AIState state = AIState.PATROL;
    private float health = 100f;
    private final float maxHealth = 100f;

    private float stateTimer = 0f;
    private float shootCooldown = 0f;
    private float hoverTime = 0f;
    private float baseHeight = 2.2f;

    // Visual nodes
    private Geometry eyeGeom;
    private Material eyeMat;
    private Geometry antennaGeom;
    private Geometry healthBarFg;
    private Node healthBarNode;

    private final float sightDistance = 28.0f;
    private final float attackRange = 18.0f;
    private final float patrolSpeed = 3.5f;
    private final float chaseSpeed = 5.2f;

    public SentryDroneAI(String id, Vector3f startPos, List<Vector3f> waypoints,
                         AssetManager assetManager, Node rootNode,
                         MissionManager missionManager, LevelBuilder levelBuilder) {
        this.id = id;
        this.position = new Vector3f(startPos);
        this.waypoints = waypoints;
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.missionManager = missionManager;
        this.levelBuilder = levelBuilder;

        this.droneNode = new Node("Drone_" + id);
        buildDroneModel();
        buildHealthBar();

        droneNode.setLocalTranslation(position);
        rootNode.attachChild(droneNode);
    }

    private void buildDroneModel() {
        // Main Chassis
        Box chassisBox = new Box(0.6f, 0.45f, 0.6f);
        Geometry chassisGeom = new Geometry("Chassis", chassisBox);
        Material chassisMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        chassisMat.setColor("Color", new ColorRGBA(0.2f, 0.22f, 0.25f, 1f));
        chassisGeom.setMaterial(chassisMat);
        chassisGeom.setLocalTranslation(0, 0, 0);
        droneNode.attachChild(chassisGeom);

        // Glowing red optic eye
        Sphere eyeSphere = new Sphere(16, 16, 0.22f);
        eyeGeom = new Geometry("OpticEye", eyeSphere);
        eyeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        eyeMat.setColor("Color", new ColorRGBA(1.0f, 0.1f, 0.1f, 1f));
        eyeGeom.setMaterial(eyeMat);
        eyeGeom.setLocalTranslation(0, 0.05f, 0.62f);
        droneNode.attachChild(eyeGeom);

        // Twin Weapon Blasters (Left & Right)
        Box barrelBox = new Box(0.08f, 0.08f, 0.45f);
        Material gunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gunMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f));

        Geometry leftGun = new Geometry("LeftGun", barrelBox);
        leftGun.setMaterial(gunMat);
        leftGun.setLocalTranslation(-0.75f, -0.15f, 0.2f);
        droneNode.attachChild(leftGun);

        Geometry rightGun = new Geometry("RightGun", barrelBox);
        rightGun.setMaterial(gunMat);
        rightGun.setLocalTranslation(0.75f, -0.15f, 0.2f);
        droneNode.attachChild(rightGun);

        // Hover Thruster pods
        Cylinder thrusterCyl = new Cylinder(16, 16, 0.25f, 0.35f, true);
        Material thrusterMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        thrusterMat.setColor("Color", new ColorRGBA(0.15f, 0.18f, 0.22f, 1f));

        Geometry leftThruster = new Geometry("LeftThruster", thrusterCyl);
        leftThruster.setMaterial(thrusterMat);
        leftThruster.rotate(FastMath.HALF_PI, 0, 0);
        leftThruster.setLocalTranslation(-0.85f, 0.1f, -0.2f);
        droneNode.attachChild(leftThruster);

        Geometry rightThruster = new Geometry("RightThruster", thrusterCyl);
        rightThruster.setMaterial(thrusterMat);
        rightThruster.rotate(FastMath.HALF_PI, 0, 0);
        rightThruster.setLocalTranslation(0.85f, 0.1f, -0.2f);
        droneNode.attachChild(rightThruster);

        // Rotating Top Antenna
        Box antBox = new Box(0.04f, 0.35f, 0.04f);
        antennaGeom = new Geometry("Antenna", antBox);
        Material antMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        antMat.setColor("Color", new ColorRGBA(0.7f, 0.7f, 0.1f, 1f));
        antennaGeom.setMaterial(antMat);
        antennaGeom.setLocalTranslation(0, 0.7f, 0);
        droneNode.attachChild(antennaGeom);
    }

    private void buildHealthBar() {
        healthBarNode = new Node("HealthBar_" + id);

        // Background (dark red)
        Box bgBox = new Box(0.6f, 0.08f, 0.02f);
        Geometry bgGeom = new Geometry("HB_Bg", bgBox);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.3f, 0.05f, 0.05f, 0.9f));
        bgGeom.setMaterial(bgMat);
        healthBarNode.attachChild(bgGeom);

        // Foreground (bright green)
        Box fgBox = new Box(0.6f, 0.07f, 0.03f);
        healthBarFg = new Geometry("HB_Fg", fgBox);
        Material fgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fgMat.setColor("Color", new ColorRGBA(0.1f, 0.95f, 0.2f, 1f));
        healthBarFg.setMaterial(fgMat);
        healthBarNode.attachChild(healthBarFg);

        healthBarNode.setLocalTranslation(0, 1.4f, 0);
        droneNode.attachChild(healthBarNode);
    }

    public void update(float tpf, Vector3f playerPos, Camera cam, List<Projectile> activeProjectiles) {
        if (state == AIState.DEAD) return;

        hoverTime += tpf;
        shootCooldown -= tpf;
        stateTimer += tpf;

        // Hover bobbing
        float hoverBob = FastMath.sin(hoverTime * 2.8f) * 0.18f;
        position.y = baseHeight + hoverBob;

        // Antenna spin
        if (antennaGeom != null) {
            antennaGeom.rotate(0, 4.0f * tpf, 0);
        }

        // Health bar billboard (faces camera)
        if (healthBarNode != null && cam != null) {
            healthBarNode.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
        }

        float distToPlayer = position.distance(playerPos);
        boolean hasLineOfSight = checkLineOfSight(playerPos);

        // AI State Machine
        switch (state) {
            case PATROL -> {
                eyeMat.setColor("Color", new ColorRGBA(0.2f, 0.7f, 1.0f, 1f)); // Blue patrol eye
                patrolMovement(tpf);

                if (distToPlayer < sightDistance && hasLineOfSight) {
                    state = AIState.ALERT;
                    stateTimer = 0f;
                    SoundEffects.playSentryAlert();
                }
            }
            case ALERT -> {
                eyeMat.setColor("Color", new ColorRGBA(1.0f, 0.8f, 0.0f, 1f)); // Yellow alert eye
                lookTowards(playerPos, tpf * 4f);

                if (stateTimer > 0.8f) {
                    state = (distToPlayer <= attackRange) ? AIState.ATTACK : AIState.CHASE;
                }
            }
            case CHASE -> {
                eyeMat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.1f, 1f)); // Red hostile eye
                lookTowards(playerPos, tpf * 5f);

                // Move towards player
                Vector3f toPlayer = playerPos.subtract(position);
                toPlayer.y = 0;
                if (toPlayer.lengthSquared() > 0.01f) {
                    Vector3f moveDir = toPlayer.normalize();
                    float nextX = position.x + moveDir.x * chaseSpeed * tpf;
                    float nextZ = position.z + moveDir.z * chaseSpeed * tpf;

                    if (!levelBuilder.checkCollision(nextX, nextZ, 1.0f)) {
                        position.x = nextX;
                        position.z = nextZ;
                    }
                }

                if (distToPlayer <= attackRange && hasLineOfSight) {
                    state = AIState.ATTACK;
                } else if (distToPlayer > sightDistance * 1.3f) {
                    state = AIState.PATROL;
                }
            }
            case ATTACK -> {
                eyeMat.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 1f)); // Intense red attack eye
                lookTowards(playerPos, tpf * 6f);

                // Fire laser bursts
                if (shootCooldown <= 0f && distToPlayer <= attackRange + 4f && hasLineOfSight) {
                    shootAtPlayer(playerPos, activeProjectiles);
                    shootCooldown = 1.25f; // Fire every 1.25s
                }

                if (distToPlayer > attackRange + 5f || !hasLineOfSight) {
                    state = AIState.CHASE;
                }
            }
            case DEAD -> {}
        }

        droneNode.setLocalTranslation(position);
    }

    private void patrolMovement(float tpf) {
        if (waypoints == null || waypoints.isEmpty()) return;

        Vector3f targetWp = waypoints.get(currentWaypointIndex);
        Vector3f diff = targetWp.subtract(position);
        diff.y = 0;

        if (diff.length() < 1.2f) {
            currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
        } else {
            Vector3f moveDir = diff.normalize();
            lookTowards(position.add(moveDir), tpf * 3f);
            position.x += moveDir.x * patrolSpeed * tpf;
            position.z += moveDir.z * patrolSpeed * tpf;
        }
    }

    private void lookTowards(Vector3f target, float turnSpeed) {
        Vector3f dir = target.subtract(position);
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalizeLocal();
            Quaternion targetRot = new Quaternion();
            targetRot.lookAt(dir, Vector3f.UNIT_Y);
            Quaternion cur = droneNode.getLocalRotation(); cur.slerp(targetRot, turnSpeed); droneNode.setLocalRotation(cur);
        }
    }

    private boolean checkLineOfSight(Vector3f playerPos) {
        // Direct distance check & angle check
        return position.distance(playerPos) <= sightDistance;
    }

    private void shootAtPlayer(Vector3f playerPos, List<Projectile> activeProjectiles) {
        Vector3f shotOrigin = position.add(0, 0.1f, 0);
        // Aim at player center (height 1.2m)
        Vector3f targetPoint = playerPos.add(0, 0.4f, 0);
        Vector3f aimDir = targetPoint.subtract(shotOrigin).normalizeLocal();

        Projectile shot = new Projectile(assetManager, shotOrigin, aimDir, 28.0f, 15.0f, false);
        activeProjectiles.add(shot);
        rootNode.attachChild(shot.getNode());

        SoundEffects.playEnemyLaser();
    }

    public void takeDamage(float amount) {
        if (state == AIState.DEAD) return;

        health -= amount;
        SoundEffects.playHitMarker();

        // If in patrol, get alerted immediately
        if (state == AIState.PATROL) {
            state = AIState.ALERT;
            stateTimer = 0.5f;
        }

        // Update health bar scale
        float pct = Math.max(0f, health / maxHealth);
        healthBarFg.setLocalScale(pct, 1f, 1f);
        healthBarFg.setLocalTranslation(-(0.6f * (1f - pct)), 0, 0.01f);

        if (health <= 0f) {
            die();
        }
    }

    private void die() {
        state = AIState.DEAD;
        SoundEffects.playSentryExplode();
        missionManager.onSentryEliminated();

        // Detach visual nodes or drop to floor
        droneNode.setLocalTranslation(position.x, 0.2f, position.z);
        droneNode.rotate(0.3f, 0.4f, 1.2f); // Collapsed on floor
        if (eyeMat != null) {
            eyeMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f)); // Optics extinguished
        }
        if (healthBarNode != null) {
            healthBarNode.removeFromParent();
        }
    }

    public boolean isDead() {
        return state == AIState.DEAD;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Node getNode() {
        return droneNode;
    }

    public String getId() {
        return id;
    }

    public float getHealth() {
        return health;
    }
}
