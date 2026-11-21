package com.blacksite.level;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

public class ExtractionZone {

    private final Node node;
    private final Vector3f position;
    private final float radius = 6.0f;
    private final Material padMat;
    private final Material beaconMat;

    public ExtractionZone(AssetManager assetManager, Vector3f position) {
        this.position = position;
        this.node = new Node("ExtractionZoneNode");

        // Landing pad disc
        Cylinder padCyl = new Cylinder(32, 32, radius, 0.2f, true);
        Geometry padGeom = new Geometry("ExtractionPad", padCyl);
        padMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        padMat.setColor("Color", new ColorRGBA(0.2f, 0.25f, 0.3f, 1f));
        padGeom.setMaterial(padMat);
        padGeom.rotate((float) (Math.PI / 2), 0, 0);
        padGeom.setLocalTranslation(0, 0.1f, 0);
        node.attachChild(padGeom);

        // Four corner beacon pillars
        beaconMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beaconMat.setColor("Color", new ColorRGBA(0.8f, 0.1f, 0.1f, 1f)); // Red standby

        float offset = radius * 0.7f;
        Vector3f[] pillarOffsets = {
            new Vector3f(-offset, 1.5f, -offset),
            new Vector3f(offset, 1.5f, -offset),
            new Vector3f(-offset, 1.5f, offset),
            new Vector3f(offset, 1.5f, offset)
        };

        for (Vector3f pOffset : pillarOffsets) {
            Box beaconBox = new Box(0.3f, 1.5f, 0.3f);
            Geometry beaconGeom = new Geometry("BeaconPillar", beaconBox);
            beaconGeom.setMaterial(beaconMat);
            beaconGeom.setLocalTranslation(pOffset);
            node.attachChild(beaconGeom);
        }

        node.setLocalTranslation(position);
    }

    public Node getNode() {
        return node;
    }

    public Vector3f getPosition() {
        return position;
    }

    public boolean isPlayerInside(Vector3f playerPos) {
        float dx = playerPos.x - position.x;
        float dz = playerPos.z - position.z;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    public void setUnlocked(boolean unlocked) {
        if (unlocked) {
            beaconMat.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.4f, 1f)); // Neon green when ready
            padMat.setColor("Color", new ColorRGBA(0.1f, 0.4f, 0.25f, 1f));
        } else {
            beaconMat.setColor("Color", new ColorRGBA(0.8f, 0.1f, 0.1f, 1f));
        }
    }
}
