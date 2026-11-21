package com.blacksite.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Projectile {

    private final Node node;
    private final Vector3f position;
    private final Vector3f velocity;
    private final float damage;
    private final boolean isPlayerShot;
    private float lifeTime;
    private boolean active = true;

    public Projectile(AssetManager assetManager, Vector3f origin, Vector3f direction, float speed, float damage, boolean isPlayerShot) {
        this.position = new Vector3f(origin);
        this.velocity = direction.normalize().mult(speed);
        this.damage = damage;
        this.isPlayerShot = isPlayerShot;
        this.lifeTime = 2.5f; // Active for up to 2.5 seconds

        this.node = new Node("ProjectileNode");

        Box box = new Box(0.08f, 0.08f, 0.35f);
        Geometry geom = new Geometry("ProjectileGeom", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        if (isPlayerShot) {
            mat.setColor("Color", new ColorRGBA(0.2f, 0.8f, 1.0f, 1f)); // Blue/Cyan plasma
        } else {
            mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.1f, 1f)); // Red plasma
        }

        geom.setMaterial(mat);
        node.attachChild(geom);
        node.setLocalTranslation(position);
        node.lookAt(position.add(direction), Vector3f.UNIT_Y);
    }

    public void update(float tpf) {
        if (!active) return;

        position.addLocal(velocity.mult(tpf));
        node.setLocalTranslation(position);

        lifeTime -= tpf;
        if (lifeTime <= 0f) {
            destroy();
        }
    }

    public void destroy() {
        active = false;
        if (node.getParent() != null) {
            node.removeFromParent();
        }
    }

    public boolean isActive() {
        return active;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isPlayerShot() {
        return isPlayerShot;
    }

    public Node getNode() {
        return node;
    }
}
