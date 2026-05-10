package com.bdsoftware.shipwrecked;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bdsoftware.shipwrecked.model.CraftingRecipe;
import com.bdsoftware.shipwrecked.model.GameState;
import com.bdsoftware.shipwrecked.model.viewmodel.GameViewModel;
import android.view.View;

import java.util.List;

public class CraftingActivity extends AppCompatActivity {

    private GameViewModel viewModel;
    private CraftingAdapter adapter;

    private TextView tvCraftWood;
    private TextView tvCraftVine;
    private TextView tvCraftFlint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        setContentView(R.layout.activity_crafting);

        // Share the same ViewModel instance as GameActivity
        viewModel = new ViewModelProvider(ShipwreckedApp.getInstance())
                .get(GameViewModel.class);

        tvCraftWood  = findViewById(R.id.tvCraftWood);
        tvCraftVine  = findViewById(R.id.tvCraftVine);
        tvCraftFlint = findViewById(R.id.tvCraftFlint);

        Button btnBack = findViewById(R.id.btnBackFromCrafting);
        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        RecyclerView rvRecipes = findViewById(R.id.rvRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        List<CraftingRecipe> recipes = viewModel.getAllRecipes();
        GameState currentState = viewModel.getStateLiveData().getValue();

        adapter = new CraftingAdapter(recipes, currentState, recipe -> {
            // Attempt to craft the item
            String result = viewModel.craftItem(recipe.getCraftKey());

            if (result.equals("SUCCESS")) {
                showCraftSuccess(recipe.getItemName());
            } else {
                showCraftFailure(result);
            }
        });

        rvRecipes.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getStateLiveData().observe(this, state -> {
            // Update inventory strip
            tvCraftWood.setText("Wood:" + state.getWood() + "  ");
            tvCraftVine.setText("Vine:" + state.getVine() + "  ");
            tvCraftFlint.setText("Flint:" + state.getFlint());

            // Refresh the adapter so affordability updates live
            adapter.updateGameState(state);
        });
    }

    private void showCraftSuccess(String itemName) {
        new AlertDialog.Builder(this)
                .setTitle("Crafted!")
                .setMessage("You successfully crafted: " + itemName
                        + "\n\nThis will improve your chances of survival.")
                .setPositiveButton("Great", null)
                .show();
    }

    private void showCraftFailure(String reason) {
        new AlertDialog.Builder(this)
                .setTitle("Cannot Craft")
                .setMessage(reason)
                .setPositiveButton("OK", null)
                .show();
    }
}