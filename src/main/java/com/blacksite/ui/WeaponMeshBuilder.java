package com.blacksite.ui;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

public class WeaponMeshBuilder {

    private final Node weaponNode;
    private final Node rootWeaponPivot;
    private final Geometry muzzleFlashGeom;
    private final Vector3f restPosition = new Vector3f(0.35f, -0.32f, 0.65f);
    private final Vector3f currentPosition = new Vector3f(restPosition);
    private float recoilAmount = 0f;
    private float muzzleFlashTimer = 0f;
    private boolean reloading = false;
    private float reloadTimer = 0f;

    public WeaponMeshBuilder(AssetManager assetManager) {
        this.rootWeaponPivot = new Node("WeaponPivot");
        this.weaponNode = new Node("AssaultRifleNode");
        rootWeaponPivot.attachChild(weaponNode);

        // Materials
        Material gunmetalMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gunmetalMat.setColor("Color", new ColorRGBA(0.12f, 0.13f, 0.15f, 1f));

        Material darkTrimMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        darkTrimMat.setColor("Color", new ColorRGBA(0.06f, 0.07f, 0.08f, 1f));

        Material energyGlowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        energyGlowMat.setColor("Color", new ColorRGBA(0.0f, 0.85f, 1.0f, 1f)); // Sci-fi cyan glow

        Material holoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        holoMat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.1f, 1f)); // Red dot optic

        // 1. Receiver / Main Body
        Box receiverBox = new Box(0.045f, 0.065f, 0.28f);
        Geometry receiver = new Geometry("Receiver", receiverBox);
        receiver.setMaterial(gunmetalMat);
        weaponNode.attachChild(receiver);

        // 2. Barrel
        Cylinder barrelCyl = new Cylinder(16, 16, 0.022f, 0.35f, true);
        Geometry barrel = new Geometry("Barrel", barrelCyl);
        barrel.setMaterial(darkTrimMat);
        barrel.setLocalTranslation(0, 0.02f, 0.40f);
        weaponNode.attachChild(barrel);

        // 3. Muzzle Device
        Box muzzleBox = new Box(0.03f, 0.035f, 0.06f);
        Geometry muzzle = new Geometry("MuzzleBrake", muzzleBox);
        muzzle.setMaterial(darkTrimMat);
        muzzle.setLocalTranslation(0, 0.02f, 0.60f);
        weaponNode.attachChild(muzzle);

        // 4. Magazine Clip
        Box magBox = new Box(0.025f, 0.12f, 0.06f);
        Geometry mag = new Geometry("Magazine", magBox);
        mag.setMaterial(darkTrimMat);
        mag.rotate(0.25f, 0, 0);
        mag.setLocalTranslation(0, -0.12f, 0.08f);
        weaponNode.attachChild(mag);

        // 5. Tactical Grip / Stock
        Box stockBox = new Box(0.035f, 0.09f, 0.12f);
        Geometry stock = new Geometry("Stock", stockBox);
        stock.setMaterial(gunmetalMat);
        stock.setLocalTranslation(0, -0.05f, -0.28f);
        weaponNode.attachChild(stock);

        // 6. Holographic Sight Mount & Glass
        Box sightRailBox = new Box(0.025f, 0.025f, 0.08f);
        Geometry sightRail = new Geometry("SightRail", sightRailBox);
        sightRail.setMaterial(darkTrimMat);
        sightRail.setLocalTranslation(0, 0.085f, 0.05f);
        weaponNode.attachChild(sightRail);

        Box holoGlass = new Box(0.02f, 0.03f, 0.01f);
        Geometry optic = new Geometry("OpticSight", holoGlass);
        optic.setMaterial(holoMat);
        optic.setLocalTranslation(0, 0.125f, 0.08f);
        weaponNode.attachChild(optic);

        // 7. Glowing Energy Conduit Trim
        Box conduitBox = new Box(0.048f, 0.012f, 0.22f);
        Geometry conduit = new Geometry("ConduitStrip", conduitBox);
        conduit.setMaterial(energyGlowMat);
        conduit.setLocalTranslation(0, 0.01f, 0.05f);
        weaponNode.attachChild(conduit);

        // 8. Muzzle Flash Flare
        Box flashBox = new Box(0.09f, 0.09f, 0.09f);
        muzzleFlashGeom = new Geometry("MuzzleFlash", flashBox);
        Material flashMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        flashMat.setColor("Color", new ColorRGBA(1.0f, 0.85f, 0.2f, 1f));
        muzzleFlashGeom.setMaterial(flashMat);
        muzzleFlashGeom.setLocalTranslation(0, 0.02f, 0.72f);
        muzzleFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        weaponNode.attachChild(muzzleFlashGeom);

        weaponNode.setLocalTranslation(restPosition);
    }

    public void triggerRecoil() {
        recoilAmount = 0.08f;
        muzzleFlashTimer = 0.05f;
        muzzleFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
    }

    public void startReload(float duration) {
        reloading = true;
        reloadTimer = duration;
    }

    public void update(float tpf) {
        // Flash timer
        if (muzzleFlashTimer > 0f) {
            muzzleFlashTimer -= tpf;
            if (muzzleFlashTimer <= 0f) {
                muzzleFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
        }

        // Recoil kick & recovery
        if (recoilAmount > 0f) {
            recoilAmount = Math.max(0f, recoilAmount - tpf * 0.7f);
        }

        if (reloading) {
            reloadTimer -= tpf;
            if (reloadTimer <= 0f) {
                reloading = false;
            }
            // Dip gun down during reload
            float dip = FastMath.sin((1.0f - (reloadTimer / 1.5f)) * FastMath.PI) * 0.25f;
            currentPosition.set(restPosition.x, restPosition.y - dip, restPosition.z - recoilAmount);
            weaponNode.setLocalRotation(new Quaternion().fromAngleAxis(-dip * 1.5f, Vector3f.UNIT_X));
        } else {
            currentPosition.set(restPosition.x, restPosition.y, restPosition.z - recoilAmount);
            weaponNode.setLocalRotation(new Quaternion().fromAngleAxis(recoilAmount * 1.2f, Vector3f.UNIT_X));
        }

        weaponNode.setLocalTranslation(currentPosition);
    }

    public Node getRootPivot() {
        return rootWeaponPivot;
    }
}
