package com.bdsoftware.shipwrecked;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bdsoftware.shipwrecked.model.ActionResult;
import com.bdsoftware.shipwrecked.model.GameState;
import com.bdsoftware.shipwrecked.model.viewmodel.GameViewModel;
import com.bdsoftware.shipwrecked.util.SoundManager;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class GameActivity extends AppCompatActivity {

    private GameViewModel viewModel;

    // Top bar
    private TextView tvDayCounter;
    private TextView tvActionsRemaining;

    // Stats
    private ProgressBar pbHealth, pbHunger, pbEnergy;
    private TextView tvHealthVal, tvHungerVal, tvEnergyVal;

    // Inventory
    private TextView tvWood, tvVine, tvFood, tvFlint;

    // Raft progress
    private LinearLayout layoutRaftProgress;
    private ProgressBar pbRaft;
    private TextView tvRaftProgress;

    // Narrative
    private ScrollView scrollNarrative;
    private TextView tvNarrative;

    // Action buttons
    private Button btnForage, btnHunt, btnFish;
    private Button btnWood, btnVine, btnExplore;
    private Button btnRest, btnBuild, btnEndDay;
    private Button btnCrafting;

    // Dice dialog views
    private AlertDialog diceDialog;
    private ImageView ivDiceFace;
    private TextView tvDiceRollLabel, tvDiceResult;
    private Button btnDismissDice;

    private SoundManager soundManager;

    // Dice drawables array
    private int[] diceFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Push content below the status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        setContentView(R.layout.activity_game);

        viewModel = new ViewModelProvider(ShipwreckedApp.getInstance())
                .get(GameViewModel.class);

        bindViews();
        setupDiceFaces();
        setupButtonListeners();
        observeViewModel();

        soundManager = SoundManager.getInstance(this);
        soundManager.startAmbient();

        // Animate the narrative area fading in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        scrollNarrative.startAnimation(fadeIn);

        // Pulse the End Day button
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        btnEndDay.startAnimation(pulse);

    }

    // -------------------------------------------------------
    // View binding
    // -------------------------------------------------------

    private void bindViews() {
        tvDayCounter       = findViewById(R.id.tvDayCounter);
        tvActionsRemaining = findViewById(R.id.tvActionsRemaining);
        pbHealth           = findViewById(R.id.pbHealth);
        pbHunger           = findViewById(R.id.pbHunger);
        pbEnergy           = findViewById(R.id.pbEnergy);
        tvHealthVal        = findViewById(R.id.tvHealthVal);
        tvHungerVal        = findViewById(R.id.tvHungerVal);
        tvEnergyVal        = findViewById(R.id.tvEnergyVal);
        tvWood             = findViewById(R.id.tvWood);
        tvVine             = findViewById(R.id.tvVine);
        tvFood             = findViewById(R.id.tvFood);
        tvFlint            = findViewById(R.id.tvFlint);
        layoutRaftProgress = findViewById(R.id.layoutRaftProgress);
        pbRaft             = findViewById(R.id.pbRaft);
        tvRaftProgress     = findViewById(R.id.tvRaftProgress);
        scrollNarrative    = findViewById(R.id.scrollNarrative);
        tvNarrative        = findViewById(R.id.tvNarrative);
        btnForage          = findViewById(R.id.btnForage);
        btnHunt            = findViewById(R.id.btnHunt);
        btnFish            = findViewById(R.id.btnFish);
        btnWood            = findViewById(R.id.btnWood);
        btnVine            = findViewById(R.id.btnVine);
        btnExplore         = findViewById(R.id.btnExplore);
        btnRest            = findViewById(R.id.btnRest);
        btnBuild           = findViewById(R.id.btnBuild);
        btnEndDay          = findViewById(R.id.btnEndDay);
        btnCrafting        = findViewById(R.id.btnCrafting);
    }

    private void setupDiceFaces() {
        diceFaces = new int[]{
                R.drawable.dice_1,
                R.drawable.dice_2,
                R.drawable.dice_3,
                R.drawable.dice_4,
                R.drawable.dice_5,
                R.drawable.dice_6
        };
    }

    // -------------------------------------------------------
    // Button listeners
    // -------------------------------------------------------

    private void setupButtonListeners() {
        btnForage.setOnClickListener(v -> triggerAction("forage"));
        btnHunt.setOnClickListener(v -> triggerAction("hunt"));
        btnFish.setOnClickListener(v -> triggerAction("fish"));
        btnWood.setOnClickListener(v -> triggerAction("wood"));
        btnVine.setOnClickListener(v -> triggerAction("vine"));
        btnExplore.setOnClickListener(v -> triggerAction("explore"));
        btnRest.setOnClickListener(v -> triggerAction("rest"));
        btnBuild.setOnClickListener(v -> triggerAction("build"));

        btnEndDay.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("End the Day?")
                    .setMessage("Are you sure you want to end the day? Unused actions are lost.")
                    .setPositiveButton("End Day", (dialog, which) -> viewModel.endDay())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnCrafting.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, CraftingActivity.class);
            startActivity(intent);
        });

    }

    // -------------------------------------------------------
    // Action trigger - shows dice animation then applies result
    // -------------------------------------------------------

    private void triggerAction(String actionKey) {
        // Rest does not use dice - apply immediately
        if (actionKey.equals("rest")) {
            viewModel.performAction("rest");
            return;
        }

        showDiceDialog(actionKey);
    }

    private void showDiceDialog(String actionKey) {
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_dice_roll, null);
        ivDiceFace      = dialogView.findViewById(R.id.ivDiceFace);
        tvDiceRollLabel = dialogView.findViewById(R.id.tvDiceRollLabel);
        tvDiceResult    = dialogView.findViewById(R.id.tvDiceResult);
        btnDismissDice  = dialogView.findViewById(R.id.btnDismissDice);

        diceDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        diceDialog.show();

        animateDice(actionKey);
    }

    private void animateDice(String actionKey) {
        final int[] frame = {0};
        final Handler handler = new Handler(Looper.getMainLooper());

        soundManager.playDiceRoll();

        // Spin through faces rapidly for 1.2 seconds
        ValueAnimator animator = ValueAnimator.ofInt(0, 30);
        animator.setDuration(1200);

        animator.addUpdateListener(animation -> {
            frame[0] = (frame[0] + 1) % 6;
            ivDiceFace.setImageResource(diceFaces[frame[0]]);
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Perform the action after animation finishes
                handler.postDelayed(() -> {
                    ActionResult result = viewModel.performAction(actionKey);
                    showDiceResult(result);
                }, 100);
            }
        });

        animator.start();
    }

    private void showDiceResult(ActionResult result) {
        // Show the final dice face
        if (result.getDiceRoll() >= 1 && result.getDiceRoll() <= 6) {
            ivDiceFace.setImageResource(diceFaces[result.getDiceRoll() - 1]);
        }

        // Outcome label and colour
        String outcomeLabel;
        int outcomeColour;
        switch (result.getOutcome()) {
            case CRITICAL_FAIL:
                outcomeLabel = "Critical Fail!";
                outcomeColour = 0xFFC0392B;
                break;
            case FAIL:
                outcomeLabel = "Fail";
                outcomeColour = 0xFFE67E22;
                break;
            case SUCCESS:
                outcomeLabel = "Success!";
                outcomeColour = 0xFF27AE60;
                break;
            default:
                outcomeLabel = "Critical Success!";
                outcomeColour = 0xFFF1C40F;
                break;
        }

        tvDiceRollLabel.setText(outcomeLabel);
        tvDiceRollLabel.setTextColor(outcomeColour);
        soundManager.playOutcome(result.getOutcome().name());
        tvDiceResult.setText(result.getStatChangeSummary());
        btnDismissDice.setVisibility(View.VISIBLE);

        btnDismissDice.setOnClickListener(v -> {
            diceDialog.dismiss();
            checkForEnding();
        });
    }

    // -------------------------------------------------------
    // ViewModel observers
    // -------------------------------------------------------

    private int lastDay = 1;

    private void observeViewModel() {
        viewModel.getStateLiveData().observe(this, this::updateUI);
        viewModel.getNarrativeLog().observe(this, text -> {
            tvNarrative.setText(text);
            // Auto-scroll to bottom
            scrollNarrative.post(() ->
                    scrollNarrative.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) soundManager.stopAmbient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null) soundManager.resumeAmbient();
    }

    private void updateUI(GameState state) {
        // Day and actions
        tvDayCounter.setText("Day " + state.getDay());
        tvActionsRemaining.setText("Actions: " + state.getActionsRemaining());

        // Stats bars
        pbHealth.setProgress(state.getHealth());
        pbHunger.setProgress(state.getHunger());
        pbEnergy.setProgress(state.getEnergy());
        tvHealthVal.setText(String.valueOf(state.getHealth()));
        tvHungerVal.setText(String.valueOf(state.getHunger()));
        tvEnergyVal.setText(String.valueOf(state.getEnergy()));

        // Inventory
        tvWood.setText("Wood:" + state.getWood() + " ");
        tvVine.setText("Vine:" + state.getVine() + " ");
        tvFood.setText("Food:" + state.getFood() + " ");
        tvFlint.setText("Flint:" + state.getFlint());

        // Raft progress bar
        if (state.getRaftProgress() > 0 || state.hasRaft()) {
            layoutRaftProgress.setVisibility(View.VISIBLE);
            pbRaft.setProgress(state.hasRaft() ? 3 : state.getRaftProgress());
            tvRaftProgress.setText((state.hasRaft() ? 3 : state.getRaftProgress()) + "/3");
        }

        // Disable action buttons when no actions remain
        boolean hasActions = state.getActionsRemaining() > 0 && !state.isGameOver();
        setActionButtonsEnabled(hasActions);

        // Show escape button if raft is complete
        if (state.hasRaft()) {
            btnBuild.setText("Use Raft");
            btnBuild.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF27AE60));
            btnBuild.setOnClickListener(v -> confirmEscape());
        }

        updateStatBarColours(state);

        if (state.getDay() != lastDay) {
            lastDay = state.getDay();
            Animation flash = AnimationUtils.loadAnimation(this, R.anim.flash);
            tvDayCounter.startAnimation(flash);
        }

    }

    private void updateStatBarColours(GameState state) {
        // Health bar colour warning
        if (state.getHealth() <= 25) {
            pbHealth.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE74C3C));
        } else if (state.getHealth() <= 50) {
            pbHealth.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE67E22));
        } else {
            pbHealth.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE74C3C));
        }

        // Hunger bar colour warning
        if (state.getHunger() <= 25) {
            pbHunger.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE74C3C));
        } else if (state.getHunger() <= 50) {
            pbHunger.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE67E22));
        } else {
            pbHunger.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFFF39C12));
        }
    }

    private void setActionButtonsEnabled(boolean enabled) {
        btnForage.setEnabled(enabled);
        btnHunt.setEnabled(enabled);
        btnFish.setEnabled(enabled);
        btnWood.setEnabled(enabled);
        btnVine.setEnabled(enabled);
        btnExplore.setEnabled(enabled);
        btnRest.setEnabled(enabled);
        btnBuild.setEnabled(enabled);
    }

    // -------------------------------------------------------
    // Escape and endings
    // -------------------------------------------------------

    private void confirmEscape() {
        new AlertDialog.Builder(this)
                .setTitle("Launch the Raft?")
                .setMessage("The raft is ready. Do you want to attempt your escape now?")
                .setPositiveButton("Set Sail", (dialog, which) -> {
                    viewModel.attemptEscape();
                    checkForEnding();
                })
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void checkForEnding() {
        GameState state = viewModel.getStateLiveData().getValue();
        if (state != null && state.isGameOver()) {
            Intent intent = new Intent(GameActivity.this, EndingActivity.class);
            intent.putExtra("ending_type", state.getEndingType());
            intent.putExtra("day_survived", state.getDay());
            startActivity(intent);
            finish();
        }
    }
}