package com.bdsoftware.shipwrecked.model;

public class CraftingRecipe {

    private final String itemName;
    private final String description;
    private final int woodCost;
    private final int vineCost;
    private final int flintCost;
    private final String craftKey; // matches a string to identify item

    public CraftingRecipe(String itemName, String description,
                          int woodCost, int vineCost,
                          int flintCost, String craftKey) {
        this.itemName = itemName;
        this.description = description;
        this.woodCost = woodCost;
        this.vineCost = vineCost;
        this.flintCost = flintCost;
        this.craftKey = craftKey;
    }

    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public int getWoodCost() { return woodCost; }
    public int getVineCost() { return vineCost; }
    public int getFlintCost() { return flintCost; }
    public String getCraftKey() { return craftKey; }
}