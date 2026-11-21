package com.blacksite.ui;

import com.blacksite.core.GameState;
import com.blacksite.core.MissionManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class HUDManager {

    private final Node guiNode;
    private final AssetManager assetManager;
    private final BitmapFont font;
    private final float screenWidth;
    private final float screenHeight;

    // Crosshair & Hit marker
    private Geometry hitMarkerGeom;
    private float hitMarkerTimer = 0f;

    // Health HUD
    private Geometry healthBarFg;
    private BitmapText healthText;

    // Ammo HUD
    private BitmapText ammoText;

    // Objective HUD
    private BitmapText missionTitleText;
    private BitmapText objectiveText;
    private BitmapText interactionPromptText;

    // Hack Progress Bar
    private Node hackProgressNode;
    private Geometry hackProgressFg;

    // Screen Damage Vignette
    private Geometry damageFlashGeom;
    private float damageFlashTimer = 0f;

    // End Game Overlay
    private Node endOverlayNode;
    private BitmapText endTitleText;
    private BitmapText endSubtitleText;

    // Controls hint
    private BitmapText controlsHintText;

    public HUDManager(Node guiNode, AssetManager assetManager, float screenWidth, float screenHeight) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        buildCrosshair();
        buildHealthHud();
        buildAmmoHud();
        buildMissionHud();
        buildDamageFlash();
        buildEndScreen();
        buildControlsHint();
    }

    private void buildCrosshair() {
        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        Material chMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        chMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.85f));

        // Horizontal bar
        Quad hQuad = new Quad(16, 2);
        Geometry hGeom = new Geometry("CH_H", hQuad);
        hGeom.setMaterial(chMat);
        hGeom.setLocalTranslation(cx - 8, cy - 1, 0);
        guiNode.attachChild(hGeom);

        // Vertical bar
        Quad vQuad = new Quad(2, 16);
        Geometry vGeom = new Geometry("CH_V", vQuad);
        vGeom.setMaterial(chMat);
        vGeom.setLocalTranslation(cx - 1, cy - 8, 0);
        guiNode.attachChild(vGeom);

        // Center dot
        Quad dotQuad = new Quad(4, 4);
        Geometry dotGeom = new Geometry("CH_Dot", dotQuad);
        Material dotMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dotMat.setColor("Color", new ColorRGBA(0.2f, 0.9f, 1.0f, 1f));
        dotGeom.setMaterial(dotMat);
        dotGeom.setLocalTranslation(cx - 2, cy - 2, 0);
        guiNode.attachChild(dotGeom);

        // Hit marker (red cross ticks)
        Quad hmQuad = new Quad(22, 22);
        hitMarkerGeom = new Geometry("HitMarker", hmQuad);
        Material hmMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        hmMat.setColor("Color", new ColorRGBA(1.0f, 0.1f, 0.1f, 0.9f));
        hitMarkerGeom.setMaterial(hmMat);
        hitMarkerGeom.setLocalTranslation(cx - 11, cy - 11, 1);
        hitMarkerGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(hitMarkerGeom);
    }

    private void buildHealthHud() {
        float x = 30;
        float y = 50;

        // Health background
        Quad bgQuad = new Quad(240, 22);
        Geometry bgGeom = new Geometry("HealthBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.15f, 0.15f, 0.15f, 0.85f));
        bgGeom.setMaterial(bgMat);
        bgGeom.setLocalTranslation(x, y, 0);
        guiNode.attachChild(bgGeom);

        // Health foreground
        Quad fgQuad = new Quad(236, 18);
        healthBarFg = new Geometry("HealthFg", fgQuad);
        Material fgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fgMat.setColor("Color", new ColorRGBA(0.1f, 0.85f, 0.3f, 1f));
        healthBarFg.setMaterial(fgMat);
        healthBarFg.setLocalTranslation(x + 2, y + 2, 1);
        guiNode.attachChild(healthBarFg);

        // Health text
        healthText = new BitmapText(font, false);
        healthText.setSize(font.getCharSet().getRenderedSize() * 1.1f);
        healthText.setColor(ColorRGBA.White);
        healthText.setText("HP: 100 / 100");
        healthText.setLocalTranslation(x, y + 48, 1);
        guiNode.attachChild(healthText);
    }

    private void buildAmmoHud() {
        ammoText = new BitmapText(font, false);
        ammoText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        ammoText.setColor(new ColorRGBA(0.2f, 0.9f, 1.0f, 1f));
        ammoText.setText("AMMO: 30 / 90");
        ammoText.setLocalTranslation(screenWidth - 250, 80, 1);
        guiNode.attachChild(ammoText);
    }

    private void buildMissionHud() {
        missionTitleText = new BitmapText(font, false);
        missionTitleText.setSize(font.getCharSet().getRenderedSize() * 1.3f);
        missionTitleText.setColor(new ColorRGBA(1.0f, 0.85f, 0.1f, 1f));
        missionTitleText.setText("◆ OPERATION BLACKSITE BREACH");
        missionTitleText.setLocalTranslation(30, screenHeight - 25, 1);
        guiNode.attachChild(missionTitleText);

        objectiveText = new BitmapText(font, false);
        objectiveText.setSize(font.getCharSet().getRenderedSize() * 1.1f);
        objectiveText.setColor(ColorRGBA.White);
        objectiveText.setText("1. Eliminate Rogue AI Sentries (0/3)");
        objectiveText.setLocalTranslation(30, screenHeight - 60, 1);
        guiNode.attachChild(objectiveText);

        interactionPromptText = new BitmapText(font, false);
        interactionPromptText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        interactionPromptText.setColor(new ColorRGBA(0.2f, 1.0f, 0.5f, 1f));
        interactionPromptText.setText("[HOLD E] HACK CENTRAL DATA CORE");
        interactionPromptText.setLocalTranslation(screenWidth / 2f - 160, screenHeight / 2f - 70, 1);
        interactionPromptText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(interactionPromptText);

        // Hack progress bar
        hackProgressNode = new Node("HackProgressNode");
        Quad bgQuad = new Quad(280, 18);
        Geometry bgGeom = new Geometry("HackBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 0.8f));
        bgGeom.setMaterial(bgMat);
        bgGeom.setLocalTranslation(screenWidth / 2f - 140, screenHeight / 2f - 105, 1);
        hackProgressNode.attachChild(bgGeom);

        Quad fgQuad = new Quad(276, 14);
        hackProgressFg = new Geometry("HackFg", fgQuad);
        Material fgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fgMat.setColor("Color", new ColorRGBA(0.1f, 0.85f, 1.0f, 1f));
        hackProgressFg.setMaterial(fgMat);
        hackProgressFg.setLocalTranslation(screenWidth / 2f - 138, screenHeight / 2f - 103, 2);
        hackProgressNode.attachChild(hackProgressFg);
        hackProgressNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(hackProgressNode);
    }

    private void buildDamageFlash() {
        Quad dfQuad = new Quad(screenWidth, screenHeight);
        damageFlashGeom = new Geometry("DamageFlash", dfQuad);
        Material dfMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dfMat.setColor("Color", new ColorRGBA(0.8f, 0.05f, 0.05f, 0.0f));
        dfMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        damageFlashGeom.setMaterial(dfMat);
        damageFlashGeom.setLocalTranslation(0, 0, 0);
        damageFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(damageFlashGeom);
    }

    private void buildEndScreen() {
        endOverlayNode = new Node("EndScreenNode");

        Quad bgQuad = new Quad(screenWidth, screenHeight);
        Geometry bgGeom = new Geometry("EndBg", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.75f));
        bgMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        bgGeom.setMaterial(bgMat);
        endOverlayNode.attachChild(bgGeom);

        endTitleText = new BitmapText(font, false);
        endTitleText.setSize(font.getCharSet().getRenderedSize() * 2.2f);
        endTitleText.setColor(new ColorRGBA(0.2f, 1.0f, 0.4f, 1f));
        endTitleText.setText("MISSION ACCOMPLISHED");
        endTitleText.setLocalTranslation(screenWidth / 2f - 240, screenHeight / 2f + 60, 2);
        endOverlayNode.attachChild(endTitleText);

        endSubtitleText = new BitmapText(font, false);
        endSubtitleText.setSize(font.getCharSet().getRenderedSize() * 1.3f);
        endSubtitleText.setColor(ColorRGBA.White);
        endSubtitleText.setText("Facility Secured. Press [R] to Play Again");
        endSubtitleText.setLocalTranslation(screenWidth / 2f - 190, screenHeight / 2f - 10, 2);
        endOverlayNode.attachChild(endSubtitleText);

        endOverlayNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(endOverlayNode);
    }

    private void buildControlsHint() {
        controlsHintText = new BitmapText(font, false);
        controlsHintText.setSize(font.getCharSet().getRenderedSize() * 0.9f);
        controlsHintText.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0.85f));
        controlsHintText.setText("WASD: Move | MOUSE: Aim | LMB: Shoot | R: Reload | SHIFT: Sprint | E: Interact");
        controlsHintText.setLocalTranslation(30, 20, 1);
        guiNode.attachChild(controlsHintText);
    }

    public void triggerHitMarker() {
        hitMarkerTimer = 0.12f;
        hitMarkerGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
    }

    public void triggerDamageFlash() {
        damageFlashTimer = 0.25f;
        damageFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
        damageFlashGeom.getMaterial().setColor("Color", new ColorRGBA(0.85f, 0.1f, 0.1f, 0.35f));
    }

    public void update(float tpf, float playerHp, int currentAmmo, int reserveAmmo, boolean isReloading,
                       MissionManager mission, boolean canInteract, GameState gameState) {

        // Hit marker decay
        if (hitMarkerTimer > 0f) {
            hitMarkerTimer -= tpf;
            if (hitMarkerTimer <= 0f) {
                hitMarkerGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
        }

        // Damage flash decay
        if (damageFlashTimer > 0f) {
            damageFlashTimer -= tpf;
            float alpha = Math.max(0f, damageFlashTimer / 0.25f) * 0.35f;
            damageFlashGeom.getMaterial().setColor("Color", new ColorRGBA(0.85f, 0.1f, 0.1f, alpha));
            if (damageFlashTimer <= 0f) {
                damageFlashGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            }
        }

        // Health update
        float hpPct = Math.max(0f, playerHp / 100f);
        healthBarFg.setLocalScale(hpPct, 1f, 1f);
        if (hpPct > 0.5f) {
            healthBarFg.getMaterial().setColor("Color", new ColorRGBA(0.1f, 0.85f, 0.3f, 1f));
        } else if (hpPct > 0.25f) {
            healthBarFg.getMaterial().setColor("Color", new ColorRGBA(1.0f, 0.75f, 0.1f, 1f));
        } else {
            healthBarFg.getMaterial().setColor("Color", new ColorRGBA(0.95f, 0.15f, 0.1f, 1f));
        }
        healthText.setText("HP: " + (int) playerHp + " / 100");

        // Ammo update
        if (isReloading) {
            ammoText.setColor(new ColorRGBA(1.0f, 0.75f, 0.1f, 1f));
            ammoText.setText("RELOADING...");
        } else {
            ammoText.setColor(new ColorRGBA(0.2f, 0.9f, 1.0f, 1f));
            ammoText.setText("AMMO: " + currentAmmo + " / " + reserveAmmo);
        }

        // Objective update
        objectiveText.setText(mission.getCurrentObjective());

        // Interaction prompt
        if (canInteract && !mission.isTerminalHacked() && mission.areAllSentriesEliminated()) {
            interactionPromptText.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
        } else {
            interactionPromptText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }

        // Hack progress bar
        if (mission.getHackProgress() > 0f && !mission.isTerminalHacked()) {
            hackProgressNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            hackProgressFg.setLocalScale(mission.getHackProgress(), 1f, 1f);
        } else {
            hackProgressNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }

        // Game State overlays
        if (gameState == GameState.MISSION_COMPLETE) {
            endOverlayNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            endTitleText.setColor(new ColorRGBA(0.2f, 1.0f, 0.4f, 1f));
            endTitleText.setText("MISSION ACCOMPLISHED!");
            endSubtitleText.setText("All Rogue Sentries Neutralized & Data Secured.\nPress [R] to Play Again");
        } else if (gameState == GameState.GAME_OVER) {
            endOverlayNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
            endTitleText.setColor(new ColorRGBA(1.0f, 0.15f, 0.15f, 1f));
            endTitleText.setText("SIGNAL LOST - K.I.A.");
            endSubtitleText.setText("Agent Terminated by Security Forces.\nPress [R] to Restart Mission");
        } else {
            endOverlayNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }
    }
}
