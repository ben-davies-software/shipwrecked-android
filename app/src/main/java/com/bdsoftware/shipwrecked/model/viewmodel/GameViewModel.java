package com.bdsoftware.shipwrecked.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bdsoftware.shipwrecked.model.ActionResult;
import com.bdsoftware.shipwrecked.model.CraftingRecipe;
import com.bdsoftware.shipwrecked.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameViewModel extends ViewModel {

    private final GameState gameState = new GameState();
    private final Random random = new Random();

    // LiveData that the UI observes
    private final MutableLiveData<GameState> stateLiveData = new MutableLiveData<>(gameState);
    private final MutableLiveData<ActionResult> lastActionResult = new MutableLiveData<>();
    private final MutableLiveData<String> narrativeLog = new MutableLiveData<>(
            "Your ship has gone down in a violent storm. You claw your way onto " +
                    "a rocky shore, gasping. The wreckage floats around you. You are alive. For now.\n\n" +
                    "Choose your first action."
    );

    // Days to survive after signal fire before rescue arrives
    private int signalFireCountdown = 0;
    private boolean signalFireActive = false;

    // -------------------------------------------------------
    // Public LiveData accessors
    // -------------------------------------------------------

    public LiveData<GameState> getStateLiveData() { return stateLiveData; }
    public LiveData<ActionResult> getLastActionResult() { return lastActionResult; }
    public LiveData<String> getNarrativeLog() { return narrativeLog; }

    // -------------------------------------------------------
    // Dice rolling
    // -------------------------------------------------------

    // Roll a single D6
    public int rollD6() {
        return random.nextInt(6) + 1;
    }

    // Roll a D6 with a modifier and clamp between 1 and 6
    private int rollWithModifier(int modifier) {
        int raw = rollD6() + modifier;
        return Math.max(1, Math.min(6, raw));
    }

    // Translate a roll value into an Outcome
    private ActionResult.Outcome toOutcome(int roll) {
        if (roll == 1) return ActionResult.Outcome.CRITICAL_FAIL;
        if (roll <= 3) return ActionResult.Outcome.FAIL;
        if (roll <= 5) return ActionResult.Outcome.SUCCESS;
        return ActionResult.Outcome.CRITICAL_SUCCESS;
    }

    // -------------------------------------------------------
    // Actions
    // -------------------------------------------------------

    public ActionResult performAction(String actionKey) {
        if (gameState.getActionsRemaining() <= 0) {
            return new ActionResult(0, ActionResult.Outcome.FAIL,
                    "You have no actions left today. End the day to rest.",
                    "");
        }

        gameState.setActionsRemaining(gameState.getActionsRemaining() - 1);
        gameState.setEnergy(gameState.getEnergy() - 20);

        ActionResult result;

        switch (actionKey) {
            case "forage":  result = actionForage();    break;
            case "hunt":    result = actionHunt();      break;
            case "fish":    result = actionFish();      break;
            case "wood":    result = actionGatherWood(); break;
            case "vine":    result = actionGatherVine(); break;
            case "explore": result = actionExplore();   break;
            case "rest":    result = actionRest();      break;
            case "build":   result = actionBuildRaft(); break;
            default:        result = new ActionResult(0, ActionResult.Outcome.FAIL,
                    "Unknown action.", "");
        }

        checkGameOver();
        stateLiveData.setValue(gameState);
        lastActionResult.setValue(result);
        appendNarrative(result.getNarrativeText());

        return result;
    }

    private ActionResult actionForage() {
        int roll = rollD6();
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                gameState.setHealth(gameState.getHealth() - 10);
                narrative = "You stumbled into a thorn bush searching for food. Scratched and empty-handed, you limp back to camp.";
                changes = "Health -10";
                break;
            case FAIL:
                narrative = "You searched the undergrowth for an hour but found nothing edible. The jungle is unforgiving today.";
                changes = "Nothing found";
                break;
            case SUCCESS:
                gameState.setFood(gameState.getFood() + 2);
                gameState.setHunger(gameState.getHunger() + 15);
                narrative = "You found a cluster of coconuts and some wild berries. Not a feast, but it will keep you alive.";
                changes = "Food +2, Hunger +15";
                break;
            default: // CRITICAL_SUCCESS
                gameState.setFood(gameState.getFood() + 4);
                gameState.setHunger(gameState.getHunger() + 30);
                narrative = "Jackpot! A fallen breadfruit tree loaded with ripe fruit. You carry back as much as you can hold.";
                changes = "Food +4, Hunger +30";
                break;
        }
        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionHunt() {
        int modifier = gameState.hasSpear() ? 1 : 0;
        int roll = rollWithModifier(modifier);
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                gameState.setHealth(gameState.getHealth() - 15);
                narrative = "A wild boar turned on you! You escaped, but not before it gored your leg.";
                changes = "Health -15";
                break;
            case FAIL:
                narrative = "You tracked an animal for hours through the forest. It vanished into the undergrowth. Nothing caught.";
                changes = "Nothing caught";
                break;
            case SUCCESS:
                gameState.setFood(gameState.getFood() + 3);
                gameState.setHunger(gameState.getHunger() + 25);
                narrative = "You brought down a large bird with a well-aimed throw. Tonight you eat well.";
                changes = "Food +3, Hunger +25";
                break;
            default: // CRITICAL_SUCCESS
                gameState.setFood(gameState.getFood() + 6);
                gameState.setHunger(gameState.getHunger() + 40);
                narrative = "You outsmarted a boar and drove it into a rocky trap. More meat than you can eat in a day.";
                changes = "Food +6, Hunger +40";
                break;
        }
        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionFish() {
        int modifier = gameState.hasFishingRod() ? 1 : 0;
        int roll = rollWithModifier(modifier);
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                narrative = "The current swept your improvised line away. You have nothing to show for it.";
                changes = "Nothing caught";
                break;
            case FAIL:
                narrative = "You sat on the rocks for an hour staring at the water. Not a single bite.";
                changes = "Nothing caught";
                break;
            case SUCCESS:
                gameState.setFood(gameState.getFood() + 3);
                gameState.setHunger(gameState.getHunger() + 20);
                narrative = "Three fish in an hour. Not bad. You gut them on the rocks and carry them back to camp.";
                changes = "Food +3, Hunger +20";
                break;
            default: // CRITICAL_SUCCESS
                gameState.setFood(gameState.getFood() + 6);
                gameState.setHunger(gameState.getHunger() + 35);
                narrative = "The lagoon was teeming with fish. You can barely carry them all. A brilliant haul.";
                changes = "Food +6, Hunger +35";
                break;
        }
        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionGatherWood() {
        int roll = rollD6();
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                gameState.setHealth(gameState.getHealth() - 5);
                narrative = "A branch came down on your shoulder as you worked. Bruised and sore, you retreat.";
                changes = "Health -5";
                break;
            case FAIL:
                gameState.setWood(gameState.getWood() + 1);
                narrative = "The forest floor yielded very little dry wood today. You managed one decent piece.";
                changes = "Wood +1";
                break;
            case SUCCESS:
                gameState.setWood(gameState.getWood() + 3);
                narrative = "Good session. Three solid lengths of driftwood dragged back to camp.";
                changes = "Wood +3";
                break;
            default: // CRITICAL_SUCCESS
                gameState.setWood(gameState.getWood() + 6);
                narrative = "You found the ship's broken mast washed up on the eastern shore. Excellent timber.";
                changes = "Wood +6";
                break;
        }
        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionGatherVine() {
        int roll = rollD6();
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                narrative = "The vines you collected were rotten and snapped apart. Useless.";
                changes = "Nothing gained";
                break;
            case FAIL:
                gameState.setVine(gameState.getVine() + 1);
                narrative = "Slim pickings in the canopy today. One length of usable vine.";
                changes = "Vine +1";
                break;
            case SUCCESS:
                gameState.setVine(gameState.getVine() + 3);
                narrative = "The strangler figs near the cliff gave up plenty of strong vine. Good binding material.";
                changes = "Vine +3";
                break;
            default: // CRITICAL_SUCCESS
                gameState.setVine(gameState.getVine() + 5);
                narrative = "A whole curtain of hanging vines near the waterfall. You coiled up as much as you could carry.";
                changes = "Vine +5";
                break;
        }
        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionExplore() {
        int roll = rollD6();
        return handleExploreEvent(roll);
    }

    private ActionResult handleExploreEvent(int roll) {
        // Exploration uses a random event table regardless of raw roll value
        int event = random.nextInt(8);
        String narrative;
        String changes;
        ActionResult.Outcome outcome = ActionResult.Outcome.SUCCESS;

        switch (event) {
            case 0:
                gameState.setWood(gameState.getWood() + 3);
                gameState.setFood(gameState.getFood() + 2);
                narrative = "You discovered a section of the ship's hull wedged between two rocks, still loaded with supplies.";
                changes = "Wood +3, Food +2";
                break;
            case 1:
                gameState.setHealth(gameState.getHealth() - 15);
                outcome = ActionResult.Outcome.CRITICAL_FAIL;
                narrative = "You slipped on wet rocks near the cliff edge. A nasty fall. You drag yourself back to camp, badly shaken.";
                changes = "Health -15";
                break;
            case 2:
                gameState.setFlint(gameState.getFlint() + 1);
                narrative = "Along the northern shore you found a sharp piece of flint. This could be useful for starting a fire.";
                changes = "Flint +1";
                break;
            case 3:
                gameState.setHunger(gameState.getHunger() + 20);
                narrative = "A freshwater stream running down from the hills! You drink deeply and feel your strength return.";
                changes = "Hunger +20";
                break;
            case 4:
                gameState.setHealth(gameState.getHealth() - 10);
                outcome = ActionResult.Outcome.FAIL;
                narrative = "A wild boar charged at you from the undergrowth. You ran, but not fast enough to avoid a glancing blow.";
                changes = "Health -10";
                break;
            case 5:
                gameState.setWood(gameState.getWood() + 2);
                gameState.setVine(gameState.getVine() + 2);
                narrative = "A hidden cove on the west side of the island. Driftwood and trailing vines everywhere.";
                changes = "Wood +2, Vine +2";
                break;
            case 6:
                narrative = "You found the remains of an old camp. Long abandoned. Whoever was here before did not make it.";
                changes = "No resources, but a grim reminder of your situation.";
                break;
            default:
                // Storm warning
                gameState.setHealth(gameState.getHealth() - (gameState.hasShelter() ? 5 : 20));
                outcome = ActionResult.Outcome.FAIL;
                narrative = gameState.hasShelter()
                        ? "Dark clouds rolled in while you were out. Your shelter held, but it was a rough night."
                        : "A sudden storm battered the island overnight. Without shelter you took a serious beating.";
                changes = gameState.hasShelter() ? "Health -5" : "Health -20";
                break;
        }

        return new ActionResult(roll, outcome, narrative, changes);
    }

    private ActionResult actionRest() {
        gameState.setEnergy(100); // Rest fully restores energy
        // Give back the energy cost of the rest action itself
        String narrative = "You lie beneath the palm trees and let your body recover. The sound of waves is almost peaceful.";
        return new ActionResult(0, ActionResult.Outcome.SUCCESS, narrative, "Energy fully restored");
    }

    private ActionResult actionBuildRaft() {
        // Requires materials
        if (gameState.getWood() < 3 || gameState.getVine() < 2) {
            return new ActionResult(0, ActionResult.Outcome.FAIL,
                    "You do not have enough materials to work on the raft. You need at least 3 Wood and 2 Vine.",
                    "Need Wood x3, Vine x2");
        }

        int roll = rollD6();
        ActionResult.Outcome outcome = toOutcome(roll);
        String narrative;
        String changes;

        switch (outcome) {
            case CRITICAL_FAIL:
                // Lose materials but no progress
                gameState.setWood(gameState.getWood() - 2);
                gameState.setVine(gameState.getVine() - 1);
                narrative = "The lashing snapped under tension and a section of the raft came apart. Materials wasted.";
                changes = "Wood -2, Vine -1. No progress.";
                break;
            case FAIL:
                // Lose materials, still no progress
                gameState.setWood(gameState.getWood() - 2);
                gameState.setVine(gameState.getVine() - 1);
                narrative = "You worked all morning but the joints are not right. You will need to redo this section.";
                changes = "Wood -2, Vine -1. No progress.";
                break;
            case SUCCESS:
                gameState.setWood(gameState.getWood() - 3);
                gameState.setVine(gameState.getVine() - 2);
                gameState.setRaftProgress(gameState.getRaftProgress() + 1);
                narrative = "Good progress. A solid section of the raft is taking shape. Keep going.";
                changes = String.format("Wood -3, Vine -2. Raft progress: %d/3", gameState.getRaftProgress());
                break;
            default: // CRITICAL_SUCCESS
                gameState.setWood(gameState.getWood() - 3);
                gameState.setVine(gameState.getVine() - 2);
                gameState.setRaftProgress(gameState.getRaftProgress() + 2);
                narrative = "Inspired work. The hull is coming together faster than you hoped. You can feel escape getting closer.";
                changes = String.format("Wood -3, Vine -2. Raft progress: %d/3", gameState.getRaftProgress());
                break;
        }

        // Check if raft is complete
        if (gameState.getRaftProgress() >= 3) {
            gameState.setHasRaft(true);
            narrative += "\n\nThe raft is COMPLETE. You stand back and look at your creation. It is rough, but it will float.";
        }

        return new ActionResult(roll, outcome, narrative, changes);
    }

    // -------------------------------------------------------
    // End of Day
    // -------------------------------------------------------

    public void endDay() {
        // Hunger drains each day
        gameState.setHunger(gameState.getHunger() - 15);

        // If hunger is empty, health starts dropping
        if (gameState.getHunger() <= 0) {
            gameState.setHealth(gameState.getHealth() - 20);
            appendNarrative("Day " + gameState.getDay() + " ended. You went to bed starving. Your body is weakening.");
        } else {
            appendNarrative("Day " + gameState.getDay() + " ended. You rest as best you can.");
        }

        // Signal fire countdown
        if (signalFireActive) {
            signalFireCountdown--;
            if (signalFireCountdown <= 0) {
                gameState.setEndingType("rescued");
            }
        }

        // Advance the day
        gameState.setDay(gameState.getDay() + 1);
        gameState.setActionsRemaining(3);
        gameState.setEnergy(100);

        // Day limit check
        if (gameState.getDay() > 30 && !gameState.isGameOver()) {
            gameState.setEndingType("stranded");
        }

        checkGameOver();
        stateLiveData.setValue(gameState);
    }

    // -------------------------------------------------------
    // Crafting
    // -------------------------------------------------------

    public List<CraftingRecipe> getAllRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        recipes.add(new CraftingRecipe("Fishing Rod",
                "Improves fishing success. +1 to Fish rolls.",
                2, 1, 0, "fishing_rod"));
        recipes.add(new CraftingRecipe("Spear",
                "Improves hunting success. +1 to Hunt rolls.",
                3, 1, 0, "spear"));
        recipes.add(new CraftingRecipe("Shelter",
                "Reduces storm and weather damage significantly.",
                5, 3, 0, "shelter"));
        recipes.add(new CraftingRecipe("Signal Fire",
                "Lights a signal fire. Rescue arrives in 3 days.",
                4, 0, 1, "signal_fire"));
        recipes.add(new CraftingRecipe("Water Collector",
                "Slows hunger drain by 5 each day.",
                2, 0, 0, "water_collector"));
        return recipes;
    }

    public String craftItem(String craftKey) {
        GameState s = gameState;

        switch (craftKey) {
            case "fishing_rod":
                if (s.hasFishingRod()) return "You have already built a Fishing Rod.";
                if (s.getWood() < 2 || s.getVine() < 1) return "Need: Wood x2, Vine x1";
                s.setWood(s.getWood() - 2);
                s.setVine(s.getVine() - 1);
                s.setHasFishingRod(true);
                break;
            case "spear":
                if (s.hasSpear()) return "You have already built a Spear.";
                if (s.getWood() < 3 || s.getVine() < 1) return "Need: Wood x3, Vine x1";
                s.setWood(s.getWood() - 3);
                s.setVine(s.getVine() - 1);
                s.setHasSpear(true);
                break;
            case "shelter":
                if (s.hasShelter()) return "You have already built a Shelter.";
                if (s.getWood() < 5 || s.getVine() < 3) return "Need: Wood x5, Vine x3";
                s.setWood(s.getWood() - 5);
                s.setVine(s.getVine() - 3);
                s.setHasShelter(true);
                break;
            case "signal_fire":
                if (s.hasSignalFire()) return "The signal fire is already burning.";
                if (s.getWood() < 4 || s.getFlint() < 1) return "Need: Wood x4, Flint x1";
                s.setWood(s.getWood() - 4);
                s.setFlint(s.getFlint() - 1);
                s.setHasSignalFire(true);
                signalFireActive = true;
                signalFireCountdown = 3;
                appendNarrative("You lit the signal fire! Thick smoke rises into the sky. Someone will see it. Survive 3 more days.");
                break;
            case "water_collector":
                if (s.hasWaterCollector()) return "You have already built a Water Collector.";
                if (s.getWood() < 2) return "Need: Wood x2";
                s.setWood(s.getWood() - 2);
                s.setHasWaterCollector(true);
                break;
            default:
                return "Unknown item.";
        }

        checkGameOver();
        stateLiveData.setValue(gameState);
        return "SUCCESS";
    }

    // -------------------------------------------------------
    // Escape (called when player taps Use Raft)
    // -------------------------------------------------------

    public void attemptEscape() {
        if (!gameState.hasRaft()) return;
        if (gameState.allItemsCrafted()) {
            gameState.setEndingType("master");
        } else {
            gameState.setEndingType("escape");
        }
        stateLiveData.setValue(gameState);
    }

    // -------------------------------------------------------
    // Game over check
    // -------------------------------------------------------

    private void checkGameOver() {
        if (gameState.isGameOver()) return;

        if (gameState.getHealth() <= 0) {
            gameState.setEndingType("perished");
        }
    }

    // -------------------------------------------------------
    // Narrative log helper
    // -------------------------------------------------------

    private void appendNarrative(String text) {
        String current = narrativeLog.getValue();
        if (current == null || current.isEmpty()) {
            narrativeLog.setValue("Your ship has gone down in a violent storm. You claw your way onto " +
                    "a rocky shore, gasping. The wreckage floats around you. You are alive. For now.\n\n" +
                    "Choose your first action.");
        } else {
            narrativeLog.setValue(current + "\n\n" + text);
        }
    }

    public void clearNarrativeLog() {
        narrativeLog.setValue("");
    }

    public void resetGame() {
        // Reset all game state to starting values
        gameState.setHealth(100);
        gameState.setHunger(100);
        gameState.setEnergy(100);
        gameState.setDay(1);
        gameState.setWood(0);
        gameState.setVine(0);
        gameState.setFood(0);
        gameState.setFlint(0);
        gameState.setHasFishingRod(false);
        gameState.setHasSpear(false);
        gameState.setHasShelter(false);
        gameState.setHasSignalFire(false);
        gameState.setHasRaft(false);
        gameState.setHasWaterCollector(false);
        gameState.setRaftProgress(0);
        gameState.setActionsRemaining(3);
        gameState.setEndingType(null);

        signalFireActive    = false;
        signalFireCountdown = 0;

        clearNarrativeLog();
        stateLiveData.setValue(gameState);
    }

}