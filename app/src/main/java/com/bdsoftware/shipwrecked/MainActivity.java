package com.bdsoftware.shipwrecked;

import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnNewGame = findViewById(R.id.btnNewGame);
        MaterialButton btnHowToPlay = findViewById(R.id.btnHowToPlay);
        MaterialButton btnCredits = findViewById(R.id.btnCredits);

        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnHowToPlay.setOnClickListener(v -> showHowToPlay());

        btnCredits.setOnClickListener(v -> showCredits());

        // Entrance animations
        TextView tvTitle    = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);

        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation slideUp    = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn     = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        tvTitle.startAnimation(fadeInSlow);
        tvSubtitle.startAnimation(fadeIn);

        findViewById(R.id.btnNewGame).startAnimation(slideUp);
        findViewById(R.id.btnHowToPlay).startAnimation(slideUp);
        findViewById(R.id.btnCredits).startAnimation(slideUp);

    }

    private void showHowToPlay() {
        new AlertDialog.Builder(this)
                .setTitle("How to Play")
                .setMessage(
                        "You are stranded on a deserted island after a shipwreck.\n\n" +
                                "Each day you may take up to 3 actions: forage for food, " +
                                "hunt, fish, gather wood, gather vines, explore, or rest.\n\n" +
                                "All actions are resolved with a dice roll. Craft tools to " +
                                "improve your odds.\n\n" +
                                "Build a raft or light a signal fire to escape.\n\n" +
                                "Survive 30 days or perish trying."
                )
                .setPositiveButton("Got it", null)
                .show();
    }

    private void showCredits() {
        new AlertDialog.Builder(this)
                .setTitle("Credits")
                .setMessage("Shipwrecked\n\nDeveloped in Android Studio Panda 4\nBuilt with Java\n\nA portfolio project.")
                .setPositiveButton("Close", null)
                .show();
    }
}