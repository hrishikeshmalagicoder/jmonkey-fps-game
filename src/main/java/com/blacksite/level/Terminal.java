package com.blacksite.level;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Terminal {

    private final Node node;
    private final Vector3f position;
    private final Geometry screenGeom;
    private final Material screenMat;
    private final float interactRadius = 4.5f;

    public Terminal(AssetManager assetManager, Vector3f position) {
        this.position = position;
        this.node = new Node("DataTerminalNode");

        // Base console pedestal
        Box baseBox = new Box(1.2f, 1.0f, 0.8f);
        Geometry baseGeom = new Geometry("TerminalBase", baseBox);
        Material baseMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        baseMat.setColor("Color", new ColorRGBA(0.18f, 0.22f, 0.26f, 1f));
        baseGeom.setMaterial(baseMat);
        baseGeom.setLocalTranslation(0, 1.0f, 0);
        node.attachChild(baseGeom);

        // Angled holo screen
        Box screenBox = new Box(0.9f, 0.5f, 0.05f);
        screenGeom = new Geometry("TerminalScreen", screenBox);
        screenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        screenMat.setColor("Color", new ColorRGBA(0.1f, 0.8f, 1.0f, 1f));
        screenGeom.setMaterial(screenMat);
        screenGeom.setLocalTranslation(0, 2.3f, 0.2f);
        screenGeom.rotate(-0.35f, 0, 0);
        node.attachChild(screenGeom);

        // Glowing core pillar behind console
        Box coreBox = new Box(0.4f, 3.0f, 0.4f);
        Geometry coreGeom = new Geometry("TerminalCore", coreBox);
        Material coreMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        coreMat.setColor("Color", new ColorRGBA(0.0f, 0.5f, 0.9f, 1f));
        coreGeom.setMaterial(coreMat);
        coreGeom.setLocalTranslation(0, 3.0f, -0.6f);
        node.attachChild(coreGeom);

        node.setLocalTranslation(position);
    }

    public Node getNode() {
        return node;
    }

    public Vector3f getPosition() {
        return position;
    }

    public boolean isPlayerInRange(Vector3f playerPos) {
        return position.distance(playerPos) <= interactRadius;
    }

    public void updateVisuals(boolean isHacked, float progress) {
        if (isHacked) {
            screenMat.setColor("Color", new ColorRGBA(0.1f, 1.0f, 0.3f, 1f)); // Green for hacked
        } else if (progress > 0f) {
            screenMat.setColor("Color", new ColorRGBA(1.0f, 0.8f, 0.1f, 1f)); // Yellow while hacking
        } else {
            screenMat.setColor("Color", new ColorRGBA(0.1f, 0.8f, 1.0f, 1f)); // Cyan idle
        }
    }
}
