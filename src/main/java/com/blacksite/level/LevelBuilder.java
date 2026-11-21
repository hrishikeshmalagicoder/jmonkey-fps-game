package com.blacksite.level;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;

public class LevelBuilder {

    private final Node rootNode;
    private final Node collidableNode;
    private final AssetManager assetManager;

    private Material wallMat;
    private Material floorMat;
    private Material crateMat;
    private Material pillarMat;
    private Material serverMat;

    private Terminal terminal;
    private ExtractionZone extractionZone;

    private final List<BoundingBox2D> wallBounds = new ArrayList<>();

    public record BoundingBox2D(float minX, float maxX, float minZ, float maxZ) {
        public boolean contains(float x, float z, float radius) {
            return (x + radius >= minX && x - radius <= maxX &&
                    z + radius >= minZ && z - radius <= maxZ);
        }
    }

    public LevelBuilder(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.collidableNode = new Node("LevelCollidables");
        this.rootNode.attachChild(collidableNode);
    }

    public void buildLevel() {
        initMaterials();
        setupLighting();

        // 1. Sector 1: Landing Bay & Hangar (Z: 15 to 55, X: -25 to 25)
        buildRoomFloor(0, 0, 35, 25, 20, new ColorRGBA(0.18f, 0.20f, 0.24f, 1f));
        // Outer boundaries
        createWall(0, 4, 55, 25, 4, 0.5f); // South Wall
        createWall(-25, 4, 35, 0.5f, 4, 20); // West Wall
        createWall(25, 4, 35, 0.5f, 4, 20);  // East Wall
        // Divider to Corridor with opening
        createWall(-16, 4, 15, 9, 4, 0.5f);
        createWall(16, 4, 15, 9, 4, 0.5f);

        // Crates in Hangar for cover
        createCrate(-8, 1.2f, 30, 1.5f, 1.2f, 1.5f);
        createCrate(-8, 1.2f, 33, 1.2f, 1.2f, 1.2f);
        createCrate(12, 1.5f, 25, 2.0f, 1.5f, 1.5f);
        createCrate(12, 1.0f, 29, 1.5f, 1.0f, 1.5f);

        // 2. Sector 2: Security Corridor & Generator Room (Z: -15 to 15, X: -18 to 18)
        buildRoomFloor(0, 0, 0, 18, 15, new ColorRGBA(0.15f, 0.16f, 0.18f, 1f));
        createWall(-18, 4, 0, 0.5f, 4, 15); // West Wall
        createWall(18, 4, 0, 0.5f, 4, 15);  // East Wall
        // Divider to Vault with opening
        createWall(-14, 4, -15, 10, 4, 0.5f);
        createWall(14, 4, -15, 10, 4, 0.5f);

        // Support pillars in corridor
        createPillar(-8, 4, 5, 1.0f, 4.0f, 1.0f);
        createPillar(8, 4, 5, 1.0f, 4.0f, 1.0f);
        createPillar(-8, 4, -5, 1.0f, 4.0f, 1.0f);
        createPillar(8, 4, -5, 1.0f, 4.0f, 1.0f);

        // 3. Sector 3: Server Core Vault (Z: -60 to -15, X: -25 to 25)
        buildRoomFloor(0, 0, -37.5f, 25, 22.5f, new ColorRGBA(0.12f, 0.14f, 0.18f, 1f));
        createWall(0, 4, -60, 25, 4, 0.5f); // North Wall
        createWall(-25, 4, -37.5f, 0.5f, 4, 22.5f); // West Wall
        createWall(25, 4, -37.5f, 0.5f, 4, 22.5f);  // East Wall

        // Server rack banks
        createServerBank(-15, 3f, -30, 2f, 3f, 6f);
        createServerBank(15, 3f, -30, 2f, 3f, 6f);
        createServerBank(-15, 3f, -45, 2f, 3f, 6f);
        createServerBank(15, 3f, -45, 2f, 3f, 6f);

        // 4. Interactive Objectives: Terminal & Extraction
        terminal = new Terminal(assetManager, new Vector3f(0, 0, -50));
        rootNode.attachChild(terminal.getNode());

        extractionZone = new ExtractionZone(assetManager, new Vector3f(0, 0, 40));
        rootNode.attachChild(extractionZone.getNode());
    }

    private void initMaterials() {
        wallMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wallMat.setColor("Color", new ColorRGBA(0.28f, 0.32f, 0.38f, 1f));

        floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", new ColorRGBA(0.16f, 0.18f, 0.20f, 1f));

        crateMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        crateMat.setColor("Color", new ColorRGBA(0.55f, 0.40f, 0.20f, 1f));

        pillarMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        pillarMat.setColor("Color", new ColorRGBA(0.35f, 0.40f, 0.45f, 1f));

        serverMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        serverMat.setColor("Color", new ColorRGBA(0.10f, 0.12f, 0.15f, 1f));
    }

    private void setupLighting() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1.0f, -0.3f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.2f));
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.4f, 0.45f, 0.5f, 1f));
        rootNode.addLight(ambient);
    }

    private void buildRoomFloor(float x, float y, float z, float xExt, float zExt, ColorRGBA color) {
        Box box = new Box(xExt, 0.1f, zExt);
        Geometry geom = new Geometry("Floor_" + x + "_" + z, box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(x, y - 0.1f, z);
        collidableNode.attachChild(geom);
    }

    private void createWall(float x, float y, float z, float xExt, float yExt, float zExt) {
        Box box = new Box(xExt, yExt, zExt);
        Geometry geom = new Geometry("Wall_" + x + "_" + z, box);
        geom.setMaterial(wallMat);
        geom.setLocalTranslation(x, y, z);
        collidableNode.attachChild(geom);
        wallBounds.add(new BoundingBox2D(x - xExt, x + xExt, z - zExt, z + zExt));
    }

    private void createCrate(float x, float y, float z, float xExt, float yExt, float zExt) {
        Box box = new Box(xExt, yExt, zExt);
        Geometry geom = new Geometry("Crate_" + x + "_" + z, box);
        geom.setMaterial(crateMat);
        geom.setLocalTranslation(x, y, z);
        collidableNode.attachChild(geom);
        wallBounds.add(new BoundingBox2D(x - xExt, x + xExt, z - zExt, z + zExt));
    }

    private void createPillar(float x, float y, float z, float xExt, float yExt, float zExt) {
        Box box = new Box(xExt, yExt, zExt);
        Geometry geom = new Geometry("Pillar_" + x + "_" + z, box);
        geom.setMaterial(pillarMat);
        geom.setLocalTranslation(x, y, z);
        collidableNode.attachChild(geom);
        wallBounds.add(new BoundingBox2D(x - xExt, x + xExt, z - zExt, z + zExt));
    }

    private void createServerBank(float x, float y, float z, float xExt, float yExt, float zExt) {
        Box box = new Box(xExt, yExt, zExt);
        Geometry geom = new Geometry("ServerBank_" + x + "_" + z, box);
        geom.setMaterial(serverMat);
        geom.setLocalTranslation(x, y, z);
        collidableNode.attachChild(geom);
        wallBounds.add(new BoundingBox2D(x - xExt, x + xExt, z - zExt, z + zExt));
    }

    public boolean checkCollision(float x, float z, float radius) {
        for (BoundingBox2D b : wallBounds) {
            if (b.contains(x, z, radius)) {
                return true;
            }
        }
        return false;
    }

    public Node getCollidableNode() {
        return collidableNode;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public ExtractionZone getExtractionZone() {
        return extractionZone;
    }

    public List<BoundingBox2D> getWallBounds() {
        return wallBounds;
    }
}
