package com.bdsoftware.shipwrecked;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdsoftware.shipwrecked.model.CraftingRecipe;
import com.bdsoftware.shipwrecked.model.GameState;

import java.util.List;

public class CraftingAdapter extends RecyclerView.Adapter<CraftingAdapter.RecipeViewHolder> {

    public interface OnCraftClickListener {
        void onCraftClick(CraftingRecipe recipe);
    }

    private final List<CraftingRecipe> recipes;
    private GameState gameState;
    private final OnCraftClickListener listener;

    public CraftingAdapter(List<CraftingRecipe> recipes,
                           GameState gameState,
                           OnCraftClickListener listener) {
        this.recipes   = recipes;
        this.gameState = gameState;
        this.listener  = listener;
    }

    public void updateGameState(GameState gameState) {
        this.gameState = gameState;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crafting_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        CraftingRecipe recipe = recipes.get(position);
        holder.bind(recipe, gameState, listener);
    }

    @Override
    public int getItemCount() { return recipes.size(); }

    // -------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRecipeName;
        private final TextView tvRecipeDescription;
        private final TextView tvRecipeCost;
        private final TextView tvAffordable;
        private final TextView tvBuiltBadge;
        private final Button   btnCraft;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName        = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeDescription = itemView.findViewById(R.id.tvRecipeDescription);
            tvRecipeCost        = itemView.findViewById(R.id.tvRecipeCost);
            tvAffordable        = itemView.findViewById(R.id.tvAffordable);
            tvBuiltBadge        = itemView.findViewById(R.id.tvBuiltBadge);
            btnCraft            = itemView.findViewById(R.id.btnCraft);
        }

        void bind(CraftingRecipe recipe, GameState state, OnCraftClickListener listener) {
            tvRecipeName.setText(recipe.getItemName());
            tvRecipeDescription.setText(recipe.getDescription());

            // Build cost string
            StringBuilder cost = new StringBuilder("Cost: ");
            if (recipe.getWoodCost() > 0)  cost.append("Wood x").append(recipe.getWoodCost()).append("  ");
            if (recipe.getVineCost() > 0)  cost.append("Vine x").append(recipe.getVineCost()).append("  ");
            if (recipe.getFlintCost() > 0) cost.append("Flint x").append(recipe.getFlintCost());
            tvRecipeCost.setText(cost.toString().trim());

            // Check if already built
            boolean alreadyBuilt = isAlreadyBuilt(recipe.getCraftKey(), state);

            if (alreadyBuilt) {
                tvBuiltBadge.setVisibility(View.VISIBLE);
                tvAffordable.setText("");
                btnCraft.setEnabled(false);
                btnCraft.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF1A3A3A));
                btnCraft.setText("Already Built");
                return;
            }

            tvBuiltBadge.setVisibility(View.GONE);

            // Check if player can afford it
            boolean canAfford = state.getWood()  >= recipe.getWoodCost()
                    && state.getVine()  >= recipe.getVineCost()
                    && state.getFlint() >= recipe.getFlintCost();

            if (canAfford) {
                tvAffordable.setText("You have enough materials.");
                tvAffordable.setTextColor(0xFF1ABC9C);
                btnCraft.setEnabled(true);
                btnCraft.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF0A7B8C));
                btnCraft.setText("Craft");
            } else {
                // Work out what is missing
                StringBuilder missing = new StringBuilder("Missing: ");
                int woodShort  = recipe.getWoodCost()  - state.getWood();
                int vineShort  = recipe.getVineCost()  - state.getVine();
                int flintShort = recipe.getFlintCost() - state.getFlint();
                if (woodShort  > 0) missing.append("Wood x").append(woodShort).append("  ");
                if (vineShort  > 0) missing.append("Vine x").append(vineShort).append("  ");
                if (flintShort > 0) missing.append("Flint x").append(flintShort);
                tvAffordable.setText(missing.toString().trim());
                tvAffordable.setTextColor(0xFFE74C3C);
                btnCraft.setEnabled(false);
                btnCraft.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF1A2A30));
                btnCraft.setText("Cannot Craft");
            }

            btnCraft.setOnClickListener(v -> listener.onCraftClick(recipe));
        }

        private boolean isAlreadyBuilt(String craftKey, GameState state) {
            switch (craftKey) {
                case "fishing_rod":    return state.hasFishingRod();
                case "spear":          return state.hasSpear();
                case "shelter":        return state.hasShelter();
                case "signal_fire":    return state.hasSignalFire();
                case "water_collector":return state.hasWaterCollector();
                default:               return false;
            }
        }
    }
}