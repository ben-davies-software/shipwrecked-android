package com.bdsoftware.shipwrecked.model;

public class ActionResult {

    public enum Outcome { CRITICAL_FAIL, FAIL, SUCCESS, CRITICAL_SUCCESS }

    private final int diceRoll;
    private final Outcome outcome;
    private final String narrativeText;
    private final String statChangeSummary;

    public ActionResult(int diceRoll, Outcome outcome,
                        String narrativeText, String statChangeSummary) {
        this.diceRoll = diceRoll;
        this.outcome = outcome;
        this.narrativeText = narrativeText;
        this.statChangeSummary = statChangeSummary;
    }

    public int getDiceRoll() { return diceRoll; }
    public Outcome getOutcome() { return outcome; }
    public String getNarrativeText() { return narrativeText; }
    public String getStatChangeSummary() { return statChangeSummary; }
}