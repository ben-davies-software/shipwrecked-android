package com.bdsoftware.shipwrecked.model;

public class GameState {

    // Core survival stats
    private int health;
    private int hunger;
    private int energy;
    private int day;

    // Inventory
    private int wood;
    private int vine;
    private int food;
    private int flint;

    // Crafted items (true = built)
    private boolean hasFishingRod;
    private boolean hasSpear;
    private boolean hasShelter;
    private boolean hasSignalFire;
    private boolean hasRaft;
    private boolean hasWaterCollector;

    // Raft build progress (needs 3 successful Build actions)
    private int raftProgress;

    // Actions remaining today
    private int actionsRemaining;

    // Ending type - set when game is over
    private String endingType; // "escape", "rescued", "perished", "stranded", "master"

    // Constructor sets starting values
    public GameState() {
        health = 100;
        hunger = 100;
        energy = 100;
        day = 1;
        wood = 0;
        vine = 0;
        food = 0;
        flint = 0;
        hasFishingRod = false;
        hasSpear = false;
        hasShelter = false;
        hasSignalFire = false;
        hasRaft = false;
        hasWaterCollector = false;
        raftProgress = 0;
        actionsRemaining = 3;
        endingType = null;
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------

    public int getHealth() { return health; }
    public int getHunger() { return hunger; }
    public int getEnergy() { return energy; }
    public int getDay() { return day; }
    public int getWood() { return wood; }
    public int getVine() { return vine; }
    public int getFood() { return food; }
    public int getFlint() { return flint; }
    public boolean hasFishingRod() { return hasFishingRod; }
    public boolean hasSpear() { return hasSpear; }
    public boolean hasShelter() { return hasShelter; }
    public boolean hasSignalFire() { return hasSignalFire; }
    public boolean hasRaft() { return hasRaft; }
    public boolean hasWaterCollector() { return hasWaterCollector; }
    public int getRaftProgress() { return raftProgress; }
    public int getActionsRemaining() { return actionsRemaining; }
    public String getEndingType() { return endingType; }

    // -------------------------------------------------------
    // Setters
    // -------------------------------------------------------

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
    }
    public void setHunger(int hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(100, energy));
    }
    public void setDay(int day) { this.day = day; }
    public void setWood(int wood) { this.wood = Math.max(0, wood); }
    public void setVine(int vine) { this.vine = Math.max(0, vine); }
    public void setFood(int food) { this.food = Math.max(0, food); }
    public void setFlint(int flint) { this.flint = Math.max(0, flint); }
    public void setHasFishingRod(boolean b) { hasFishingRod = b; }
    public void setHasSpear(boolean b) { hasSpear = b; }
    public void setHasShelter(boolean b) { hasShelter = b; }
    public void setHasSignalFire(boolean b) { hasSignalFire = b; }
    public void setHasRaft(boolean b) { hasRaft = b; }
    public void setHasWaterCollector(boolean b) { hasWaterCollector = b; }
    public void setRaftProgress(int p) { raftProgress = p; }
    public void setActionsRemaining(int a) { actionsRemaining = a; }
    public void setEndingType(String type) { endingType = type; }

    // -------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------

    public boolean isGameOver() { return endingType != null; }

    public boolean allItemsCrafted() {
        return hasFishingRod && hasSpear && hasShelter
                && hasSignalFire && hasRaft && hasWaterCollector;
    }
}