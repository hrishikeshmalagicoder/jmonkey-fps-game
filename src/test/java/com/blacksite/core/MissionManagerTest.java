package com.blacksite.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MissionManagerTest {

    private MissionManager missionManager;

    @BeforeEach
    public void setUp() {
        missionManager = new MissionManager();
    }

    @Test
    public void testInitialMissionState() {
        assertEquals(0, missionManager.getSentriesEliminated());
        assertEquals(3, missionManager.getTotalSentries());
        assertFalse(missionManager.areAllSentriesEliminated());
        assertFalse(missionManager.isTerminalHacked());
        assertFalse(missionManager.isExtractionReached());
        assertTrue(missionManager.getCurrentObjective().contains("Eliminate Rogue AI Sentries"));
    }

    @Test
    public void testSentryEliminationProgression() {
        missionManager.onSentryEliminated();
        assertEquals(1, missionManager.getSentriesEliminated());
        assertFalse(missionManager.areAllSentriesEliminated());

        missionManager.onSentryEliminated();
        missionManager.onSentryEliminated();
        assertEquals(3, missionManager.getSentriesEliminated());
        assertTrue(missionManager.areAllSentriesEliminated());
        assertTrue(missionManager.getCurrentObjective().contains("Hack Central Data Core"));
    }

    @Test
    public void testTerminalHackProgression() {
        missionManager.onSentryEliminated();
        missionManager.onSentryEliminated();
        missionManager.onSentryEliminated();

        missionManager.advanceHackProgress(0.5f);
        assertEquals(0.5f, missionManager.getHackProgress(), 0.001f);
        assertFalse(missionManager.isTerminalHacked());

        missionManager.advanceHackProgress(0.6f);
        assertEquals(1.0f, missionManager.getHackProgress(), 0.001f);
        assertTrue(missionManager.isTerminalHacked());
        assertTrue(missionManager.getCurrentObjective().contains("Extraction Pad"));
    }

    @Test
    public void testExtractionUnlockAndTrigger() {
        // Cannot extract before terminal is hacked
        missionManager.triggerExtraction();
        assertFalse(missionManager.isExtractionReached());

        // Complete sentries and terminal
        missionManager.onSentryEliminated();
        missionManager.onSentryEliminated();
        missionManager.onSentryEliminated();
        missionManager.advanceHackProgress(1.0f);

        missionManager.triggerExtraction();
        assertTrue(missionManager.isExtractionReached());
        assertTrue(missionManager.getCurrentObjective().contains("ACCOMPLISHED"));
    }

    @Test
    public void testReset() {
        missionManager.onSentryEliminated();
        missionManager.advanceHackProgress(1.0f);
        missionManager.triggerExtraction();

        missionManager.reset();
        assertEquals(0, missionManager.getSentriesEliminated());
        assertFalse(missionManager.isTerminalHacked());
        assertFalse(missionManager.isExtractionReached());
    }
}