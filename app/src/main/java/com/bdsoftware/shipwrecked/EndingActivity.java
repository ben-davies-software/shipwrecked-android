package com.bdsoftware.shipwrecked;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bdsoftware.shipwrecked.model.GameState;
import com.bdsoftware.shipwrecked.model.viewmodel.GameViewModel;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class EndingActivity extends AppCompatActivity {

    private GameViewModel viewModel;

    private TextView tvEndingIcon;
    private TextView tvEndingTitle;
    private TextView tvDaysSurvived;
    private TextView tvEndingNarrative;
    private TextView tvSummaryDays;
    private TextView tvSummaryHealth;
    private TextView tvSummaryItems;
    private TextView tvSummaryScore;
    private Button   btnPlayAgain;
    private Button   btnMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_ending);

        viewModel = new ViewModelProvider(ShipwreckedApp.getInstance())
                .get(GameViewModel.class);

        bindViews();
        displayEnding();

        // Animate ending screen elements in
        Animation fadeIn  = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        tvEndingIcon.startAnimation(fadeIn);
        tvEndingTitle.startAnimation(fadeIn);
        tvEndingNarrative.startAnimation(fadeIn);
        findViewById(R.id.btnPlayAgain).startAnimation(slideUp);
        findViewById(R.id.btnMainMenu).startAnimation(slideUp);

        setupButtons();
    }

    private void bindViews() {
        tvEndingIcon      = findViewById(R.id.tvEndingIcon);
        tvEndingTitle     = findViewById(R.id.tvEndingTitle);
        tvDaysSurvived    = findViewById(R.id.tvDaysSurvived);
        tvEndingNarrative = findViewById(R.id.tvEndingNarrative);
        tvSummaryDays     = findViewById(R.id.tvSummaryDays);
        tvSummaryHealth   = findViewById(R.id.tvSummaryHealth);
        tvSummaryItems    = findViewById(R.id.tvSummaryItems);
        tvSummaryScore    = findViewById(R.id.tvSummaryScore);
        btnPlayAgain      = findViewById(R.id.btnPlayAgain);
        btnMainMenu       = findViewById(R.id.btnMainMenu);
    }

    private void displayEnding() {
        GameState state = viewModel.getStateLiveData().getValue();
        if (state == null) return;

        String endingType  = state.getEndingType();
        int    day         = state.getDay();
        int    health      = state.getHealth();
        int    itemsCrafted = countItemsCrafted(state);
        int    score       = calculateScore(state);

        // Set summary card values
        tvSummaryDays.setText(String.valueOf(day));
        tvSummaryHealth.setText(health + " / 100");
        tvSummaryItems.setText(itemsCrafted + " / 5");
        tvSummaryScore.setText(String.valueOf(score));
        tvDaysSurvived.setText("Day " + day);

        // Set ending-specific content
        switch (endingType) {

            case "escape":
                tvEndingIcon.setText("⛵");
                tvEndingTitle.setText("You Escaped!");
                tvEndingTitle.setTextColor(0xFF17B8C8);
                tvEndingNarrative.setText(
                        "With blistered hands and a sunburned face, you push the raft " +
                                "into the surf. The waves catch it immediately and carry you away " +
                                "from the island that nearly killed you.\n\n" +
                                "Three days later, a cargo vessel spots your improvised flag and " +
                                "hauls you aboard. You eat real food for the first time in weeks.\n\n" +
                                "You made it. Against all odds, you made it home."
                );
                break;

            case "rescued":
                tvEndingIcon.setText("🚁");
                tvEndingTitle.setText("You Were Rescued!");
                tvEndingTitle.setTextColor(0xFF1ABC9C);
                tvEndingNarrative.setText(
                        "The signal fire burns through the night, sending a thick column " +
                                "of smoke high into the sky. On the third day you hear it: the " +
                                "distant thrum of a helicopter.\n\n" +
                                "It circles once, twice, then drops lower. A figure in an orange " +
                                "vest waves from the door. You wave back with everything you have left.\n\n" +
                                "You are going home. Someone came for you."
                );
                break;

            case "master":
                tvEndingIcon.setText("🏆");
                tvEndingTitle.setText("Island Master!");
                tvEndingTitle.setTextColor(0xFFF1C40F);
                tvEndingNarrative.setText(
                        "You built everything. Every tool, every shelter, every contraption " +
                                "your desperate mind could devise. The island threw everything at you " +
                                "and you answered every challenge.\n\n" +
                                "As you sail away on your completed raft, you look back at the island " +
                                "one last time. It no longer frightens you. You conquered it.\n\n" +
                                "Not many people could have done what you did. Remember that."
                );
                break;

            case "stranded":
                tvEndingIcon.setText("🌅");
                tvEndingTitle.setText("Lost to the Island");
                tvEndingTitle.setTextColor(0xFFE67E22);
                tvEndingNarrative.setText(
                        "Thirty days pass. The search parties that were sent for you have " +
                                "long since turned back. The world has moved on.\n\n" +
                                "You are still alive, which is something. But the raft was never " +
                                "finished and the signal fire was never lit. The island has become " +
                                "your world now.\n\n" +
                                "Perhaps tomorrow will be different. It has to be."
                );
                break;

            case "perished":
            default:
                tvEndingIcon.setText("💀");
                tvEndingTitle.setText("You Perished");
                tvEndingTitle.setTextColor(0xFFE74C3C);
                tvEndingNarrative.setText(
                        "The island takes you quietly, as it takes everything eventually. " +
                                "Your camp sits empty on the shore. The fire goes cold.\n\n" +
                                "The waves continue regardless, indifferent to the small drama " +
                                "that played out here. The jungle reclaims what little you built.\n\n" +
                                "You fought hard. The island fought harder."
                );
                break;
        }
    }

    // -------------------------------------------------------
    // Score calculation
    // -------------------------------------------------------

    private int calculateScore(GameState state) {
        int score = 0;

        // Base points for surviving each day
        score += state.getDay() * 10;

        // Bonus for remaining health
        score += state.getHealth() * 2;

        // Bonus per item crafted
        score += countItemsCrafted(state) * 50;

        // Large bonus for successful endings
        String ending = state.getEndingType();
        if (ending != null) {
            switch (ending) {
                case "master":   score += 500; break;
                case "escape":   score += 300; break;
                case "rescued":  score += 250; break;
                case "stranded": score += 50;  break;
                case "perished": score += 0;   break;
            }
        }

        return score;
    }

    private int countItemsCrafted(GameState state) {
        int count = 0;
        if (state.hasFishingRod())     count++;
        if (state.hasSpear())          count++;
        if (state.hasShelter())        count++;
        if (state.hasSignalFire())     count++;
        if (state.hasWaterCollector()) count++;
        return count;
    }

    // -------------------------------------------------------
    // Button setup
    // -------------------------------------------------------

    private void setupButtons() {
        btnPlayAgain.setOnClickListener(v -> {
            // Reset the ViewModel and start a fresh game
            viewModel.resetGame();
            Intent intent = new Intent(EndingActivity.this, GameActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnMainMenu.setOnClickListener(v -> {
            viewModel.resetGame();
            Intent intent = new Intent(EndingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}