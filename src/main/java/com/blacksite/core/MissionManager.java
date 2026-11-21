package com.blacksite.core;

import com.blacksite.audio.SoundEffects;

/**
 * Manages mission progression and objectives for "Operation Blacksite Breach".
 * Phase 1: Eliminate Rogue Sentries (3 total)
 * Phase 2: Infiltrate Server Vault and Hack Central Data Terminal
 * Phase 3: Reach Extraction Beacon in Sector 4
 */
public class MissionManager {

    private final int totalSentries = 3;
    private int sentriesEliminated = 0;
    private boolean terminalHacked = false;
    private boolean extractionReached = false;
    private float hackProgress = 0f; // 0.0 to 1.0

    public void onSentryEliminated() {
        sentriesEliminated++;
        if (sentriesEliminated > totalSentries) {
            sentriesEliminated = totalSentries;
        }
    }

    public boolean areAllSentriesEliminated() {
        return sentriesEliminated >= totalSentries;
    }

    public int getSentriesEliminated() {
        return sentriesEliminated;
    }

    public int getTotalSentries() {
        return totalSentries;
    }

    public boolean isTerminalHacked() {
        return terminalHacked;
    }

    public float getHackProgress() {
        return hackProgress;
    }

    public void advanceHackProgress(float amount) {
        if (terminalHacked) return;
        hackProgress = Math.min(1.0f, hackProgress + amount);
        if (hackProgress >= 1.0f) {
            terminalHacked = true;
            SoundEffects.playHackComplete();
        }
    }

    public void resetHackProgress() {
        if (!terminalHacked) {
            hackProgress = 0f;
        }
    }

    public boolean isExtractionReached() {
        return extractionReached;
    }

    public void triggerExtraction() {
        if (isTerminalHacked()) {
            this.extractionReached = true;
            SoundEffects.playVictory();
        }
    }

    public String getCurrentObjective() {
        if (!areAllSentriesEliminated()) {
            return "1. Eliminate Rogue AI Sentries (" + sentriesEliminated + "/" + totalSentries + ")";
        }
        if (!terminalHacked) {
            return "2. Hack Central Data Core in Server Vault [Hold E]";
        }
        if (!extractionReached) {
            return "3. Evacuate to Extraction Pad in Landing Bay!";
        }
        return "MISSION ACCOMPLISHED - Blacksite Secured!";
    }

    public void reset() {
        sentriesEliminated = 0;
        terminalHacked = false;
        extractionReached = false;
        hackProgress = 0f;
    }
}
