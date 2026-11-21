package com.blacksite;

import com.blacksite.audio.SoundEffects;
import com.blacksite.core.GameState;
import com.blacksite.core.MissionManager;
import com.blacksite.entities.Player;
import com.blacksite.entities.Projectile;
import com.blacksite.entities.SentryDroneAI;
import com.blacksite.level.ExtractionZone;
import com.blacksite.level.LevelBuilder;
import com.blacksite.level.Terminal;
import com.blacksite.ui.HUDManager;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class BlacksiteBreachApp extends SimpleApplication implements ActionListener, AnalogListener {

    private LevelBuilder levelBuilder;
    private HUDManager hud;
    private MissionManager missionManager;
    private Player player;
    private final List<SentryDroneAI> sentries = new ArrayList<>();
    private final List<Projectile> activeProjectiles = new ArrayList<>();

    private GameState gameState = GameState.PLAYING;
    private float terminalBeepTimer = 0f;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Blacksite Breach 3D FPS - Operation Prototype");
        settings.setResolution(1280, 720);
        settings.setFrameRate(60);
        settings.setSamples(4); // 4x Anti-aliasing

        BlacksiteBreachApp app = new BlacksiteBreachApp();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Disable default flycam & lock mouse for FPS look
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(false);

        // Hide engine stats
        setDisplayStatView(false);
        setDisplayFps(false);

        // 1. Build Level & Lighting
        levelBuilder = new LevelBuilder(rootNode, assetManager);
        levelBuilder.buildLevel();

        // 2. Mission & HUD
        missionManager = new MissionManager();
        hud = new HUDManager(guiNode, assetManager, settings.getWidth(), settings.getHeight());

        // 3. Player
        player = new Player(cam, rootNode, assetManager, levelBuilder, hud);
        rootNode.attachChild(player.getWeaponNode());

        // 4. Spawn 3 AI Sentries
        spawnSentries();

        // 5. Setup Inputs
        initInputs();
    }

    private void spawnSentries() {
        // Sentry 1: Alpha (Hangar Patrol)
        List<Vector3f> wpAlpha = List.of(
            new Vector3f(-10, 2.2f, 25),
            new Vector3f(10, 2.2f, 25),
            new Vector3f(10, 2.2f, 38),
            new Vector3f(-10, 2.2f, 38)
        );
        sentries.add(new SentryDroneAI("Alpha", new Vector3f(-10, 2.2f, 25), wpAlpha,
                assetManager, rootNode, missionManager, levelBuilder));

        // Sentry 2: Bravo (Corridor & Generator Patrol)
        List<Vector3f> wpBravo = List.of(
            new Vector3f(-6, 2.2f, 8),
            new Vector3f(6, 2.2f, 8),
            new Vector3f(6, 2.2f, -8),
            new Vector3f(-6, 2.2f, -8)
        );
        sentries.add(new SentryDroneAI("Bravo", new Vector3f(-6, 2.2f, 8), wpBravo,
                assetManager, rootNode, missionManager, levelBuilder));

        // Sentry 3: Delta (Server Vault Core Patrol)
        List<Vector3f> wpDelta = List.of(
            new Vector3f(-8, 2.2f, -32),
            new Vector3f(8, 2.2f, -32),
            new Vector3f(8, 2.2f, -48),
            new Vector3f(-8, 2.2f, -48)
        );
        sentries.add(new SentryDroneAI("Delta", new Vector3f(-8, 2.2f, -32), wpDelta,
                assetManager, rootNode, missionManager, levelBuilder));
    }

    private void initInputs() {
        // Keys
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Reload", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));

        // Mouse Shoot & Look
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("MouseX_Pos", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseX_Neg", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseY_Pos", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MouseY_Neg", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        // Listeners
        inputManager.addListener(this, "Forward", "Backward", "Left", "Right", "Jump", "Sprint", "Reload", "Interact", "Shoot");
        inputManager.addListener(this, "MouseX_Pos", "MouseX_Neg", "MouseY_Pos", "MouseY_Neg");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (gameState == GameState.GAME_OVER || gameState == GameState.MISSION_COMPLETE) {
            if (name.equals("Reload") && isPressed) {
                restartGame();
            }
            return;
        }

        switch (name) {
            case "Forward" -> player.setMoveForward(isPressed);
            case "Backward" -> player.setMoveBackward(isPressed);
            case "Left" -> player.setStrafeLeft(isPressed);
            case "Right" -> player.setStrafeRight(isPressed);
            case "Sprint" -> player.setSprinting(isPressed);
            case "Jump" -> { if (isPressed) player.jump(); }
            case "Reload" -> { if (isPressed) player.startReload(); }
            case "Shoot" -> player.setShooting(isPressed);
            case "Interact" -> player.setInteracting(isPressed);
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (gameState != GameState.PLAYING) return;

        switch (name) {
            case "MouseX_Pos" -> player.applyMouseLook(-value, 0);
            case "MouseX_Neg" -> player.applyMouseLook(value, 0);
            case "MouseY_Pos" -> player.applyMouseLook(0, value);
            case "MouseY_Neg" -> player.applyMouseLook(0, -value);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Terminal terminal = levelBuilder.getTerminal();
        ExtractionZone extractionZone = levelBuilder.getExtractionZone();
        Vector3f playerPos = player.getPosition();

        boolean canInteractTerminal = terminal.isPlayerInRange(playerPos);

        if (gameState == GameState.PLAYING) {
            // 1. Update Player
            player.update(tpf, sentries, activeProjectiles, gameState);

            // 2. Terminal Hacking Logic
            if (canInteractTerminal && player.isInteracting() && missionManager.areAllSentriesEliminated() && !missionManager.isTerminalHacked()) {
                missionManager.advanceHackProgress(tpf * 0.4f); // 2.5 seconds to complete hack
                terminalBeepTimer += tpf;
                if (terminalBeepTimer >= 0.35f) {
                    SoundEffects.playTerminalBeep();
                    terminalBeepTimer = 0f;
                }
            } else if (!missionManager.isTerminalHacked()) {
                missionManager.resetHackProgress();
            }
            terminal.updateVisuals(missionManager.isTerminalHacked(), missionManager.getHackProgress());

            // 3. Extraction Unlock & Check
            if (missionManager.isTerminalHacked()) {
                extractionZone.setUnlocked(true);
                if (extractionZone.isPlayerInside(playerPos)) {
                    missionManager.triggerExtraction();
                    gameState = GameState.MISSION_COMPLETE;
                }
            }

            // 4. Update Sentries
            for (SentryDroneAI sentry : sentries) {
                sentry.update(tpf, playerPos, cam, activeProjectiles);
            }

            // 5. Update Projectiles
            for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
                Projectile p = activeProjectiles.get(i);
                p.update(tpf);

                if (!p.isActive()) {
                    activeProjectiles.remove(i);
                    continue;
                }

                // Check collision against player for enemy projectiles
                if (!p.isPlayerShot()) {
                    float distToPlayer = p.getPosition().distance(playerPos);
                    if (distToPlayer < 1.2f) {
                        player.takeDamage(p.getDamage());
                        p.destroy();
                        activeProjectiles.remove(i);

                        if (player.getHealth() <= 0f) {
                            gameState = GameState.GAME_OVER;
                            SoundEffects.playGameOver();
                        }
                        continue;
                    }
                }

                // Check collision against level walls
                Vector3f pPos = p.getPosition();
                if (levelBuilder.checkCollision(pPos.x, pPos.z, 0.2f)) {
                    p.destroy();
                    activeProjectiles.remove(i);
                }
            }
        }

        // 6. Update HUD
        hud.update(tpf, player.getHealth(), player.getCurrentAmmo(), player.getReserveAmmo(),
                player.isReloading(), missionManager, canInteractTerminal, gameState);
    }

    private void restartGame() {
        // Clear active projectiles
        for (Projectile p : activeProjectiles) {
            p.destroy();
        }
        activeProjectiles.clear();

        // Remove old sentries and spawn fresh ones
        for (SentryDroneAI sentry : sentries) {
            sentry.getNode().removeFromParent();
        }
        sentries.clear();
        spawnSentries();

        // Reset player & mission
        missionManager.reset();
        player.reset();
        levelBuilder.getExtractionZone().setUnlocked(false);
        levelBuilder.getTerminal().updateVisuals(false, 0f);

        gameState = GameState.PLAYING;
    }
}
