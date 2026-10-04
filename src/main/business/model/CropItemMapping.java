package business.model;

import java.util.*;

/**
 * Domain knowledge: maps each crop to its applicable seeds, fertilizers, and pesticides.
 * GRASP: Information Expert -- this class knows the crop-item relationships.
 *
 * Categories:
 *   "Seeds"       -- one seed item per crop
 *   "Fertilizers" -- crop-specific fertilizer list
 *   "Pesticides"  -- crop-specific pesticide list
 */
public class CropItemMapping {

    /**
     * Inner class to hold an item's ID and display name.
     */
    public static class ItemEntry {
        private final String itemTypeId;
        private final String displayName;

        public ItemEntry(String itemTypeId, String displayName) {
            this.itemTypeId = itemTypeId;
            this.displayName = displayName;
        }

        public String getItemTypeId() { return itemTypeId; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }

    // cropId -> category -> list of ItemEntry
    private static final Map<String, Map<String, List<ItemEntry>>> MAPPING = new LinkedHashMap<>();

    // cropId -> season (Rabi or Kharif)
    private static final Map<String, String> CROP_SEASON = new LinkedHashMap<>();

    static {
        // --- RABI CROPS ---

        // 1. Wheat
        CROP_SEASON.put("crop-wheat", "Rabi");
        Map<String, List<ItemEntry>> wheat = new LinkedHashMap<>();
        wheat.put("Seeds", Arrays.asList(new ItemEntry("seed-wheat", "Wheat Seeds")));
        wheat.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-npk", "NPK")
        ));
        wheat.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-confidor", "Confidor (aphids)"),
            new ItemEntry("pest-mospilan", "Mospilan (sucking pests)"),
            new ItemEntry("pest-sectin", "Sectin (fungicide for rust)")
        ));
        MAPPING.put("crop-wheat", wheat);

        // 2. Barley
        CROP_SEASON.put("crop-barley", "Rabi");
        Map<String, List<ItemEntry>> barley = new LinkedHashMap<>();
        barley.put("Seeds", Arrays.asList(new ItemEntry("seed-barley", "Barley Seeds")));
        barley.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP")
        ));
        barley.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-mospilan", "Mospilan (aphids)"),
            new ItemEntry("pest-sectin", "Sectin (fungal diseases)")
        ));
        MAPPING.put("crop-barley", barley);

        // 3. Gram (Chickpea)
        CROP_SEASON.put("crop-gram", "Rabi");
        Map<String, List<ItemEntry>> gram = new LinkedHashMap<>();
        gram.put("Seeds", Arrays.asList(new ItemEntry("seed-gram", "Gram Seeds")));
        gram.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-ssp", "SSP"),
            new ItemEntry("fert-dap", "DAP")
        ));
        gram.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-talstar", "Talstar (pod borer)"),
            new ItemEntry("pest-fame", "Fame (caterpillars)")
        ));
        MAPPING.put("crop-gram", gram);

        // 4. Mustard / Canola
        CROP_SEASON.put("crop-mustard", "Rabi");
        Map<String, List<ItemEntry>> mustard = new LinkedHashMap<>();
        mustard.put("Seeds", Arrays.asList(new ItemEntry("seed-mustard", "Mustard Seeds")));
        mustard.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-sop", "SOP")
        ));
        mustard.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-confidor", "Confidor (aphids)"),
            new ItemEntry("pest-mospilan", "Mospilan (whitefly)")
        ));
        MAPPING.put("crop-mustard", mustard);

        // 5. Peas
        CROP_SEASON.put("crop-peas", "Rabi");
        Map<String, List<ItemEntry>> peas = new LinkedHashMap<>();
        peas.put("Seeds", Arrays.asList(new ItemEntry("seed-peas", "Peas Seeds")));
        peas.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-npk", "NPK")
        ));
        peas.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-mospilan", "Mospilan (aphids)"),
            new ItemEntry("pest-sectin", "Sectin (fungus)")
        ));
        MAPPING.put("crop-peas", peas);

        // --- KHARIF CROPS ---

        // 6. Rice
        CROP_SEASON.put("crop-rice", "Kharif");
        Map<String, List<ItemEntry>> rice = new LinkedHashMap<>();
        rice.put("Seeds", Arrays.asList(new ItemEntry("seed-rice", "Rice Seeds")));
        rice.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-zinc", "Zinc Sulphate")
        ));
        rice.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-machete", "Machete (weeds)"),
            new ItemEntry("pest-fame", "Fame (stem borer)"),
            new ItemEntry("pest-glyphosate", "Glyphosate (pre-plant)")
        ));
        MAPPING.put("crop-rice", rice);

        // 7. Cotton
        CROP_SEASON.put("crop-cotton", "Kharif");
        Map<String, List<ItemEntry>> cotton = new LinkedHashMap<>();
        cotton.put("Seeds", Arrays.asList(new ItemEntry("seed-cotton", "Cotton Seeds")));
        cotton.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-sop", "SOP")
        ));
        cotton.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-confidor", "Confidor (whitefly)"),
            new ItemEntry("pest-curacron", "Curacron (bollworms)"),
            new ItemEntry("pest-talstar", "Talstar (insects)")
        ));
        MAPPING.put("crop-cotton", cotton);

        // 8. Maize
        CROP_SEASON.put("crop-maize", "Kharif");
        Map<String, List<ItemEntry>> maize = new LinkedHashMap<>();
        maize.put("Seeds", Arrays.asList(new ItemEntry("seed-maize", "Maize Seeds")));
        maize.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-ammsul", "Ammonium Sulfate")
        ));
        maize.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-match", "Match (fall armyworm)"),
            new ItemEntry("pest-talstar", "Talstar (borers)")
        ));
        MAPPING.put("crop-maize", maize);

        // 9. Sugarcane
        CROP_SEASON.put("crop-sugarcane", "Kharif");
        Map<String, List<ItemEntry>> sugarcane = new LinkedHashMap<>();
        sugarcane.put("Seeds", Arrays.asList(new ItemEntry("seed-sugarcane", "Sugarcane Setts")));
        sugarcane.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP"),
            new ItemEntry("fert-npk", "NPK")
        ));
        sugarcane.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-confidor", "Confidor (early pests)"),
            new ItemEntry("pest-glyphosate", "Glyphosate (weed control)")
        ));
        MAPPING.put("crop-sugarcane", sugarcane);

        // 10. Bajra (Millet)
        CROP_SEASON.put("crop-bajra", "Kharif");
        Map<String, List<ItemEntry>> bajra = new LinkedHashMap<>();
        bajra.put("Seeds", Arrays.asList(new ItemEntry("seed-bajra", "Bajra Seeds")));
        bajra.put("Fertilizers", Arrays.asList(
            new ItemEntry("fert-urea", "Urea"),
            new ItemEntry("fert-dap", "DAP")
        ));
        bajra.put("Pesticides", Arrays.asList(
            new ItemEntry("pest-mospilan", "Mospilan (aphids)"),
            new ItemEntry("pest-talstar", "Talstar (insects)")
        ));
        MAPPING.put("crop-bajra", bajra);
    }

    /** Get the list of items for a given crop and category. */
    public static List<ItemEntry> getItems(String cropId, String category) {
        Map<String, List<ItemEntry>> cropMap = MAPPING.get(cropId);
        if (cropMap == null) return Collections.emptyList();
        List<ItemEntry> items = cropMap.get(category);
        return items != null ? items : Collections.emptyList();
    }

    /** Get the categories available for a crop (always Seeds, Fertilizers, Pesticides). */
    public static List<String> getCategories() {
        return Arrays.asList("Seeds", "Fertilizers", "Pesticides");
    }

    /** Get the season for a crop (Rabi or Kharif). */
    public static String getSeasonForCrop(String cropId) {
        return CROP_SEASON.getOrDefault(cropId, "Rabi");
    }

    /** Get the unit label based on category: Bags for Seeds/Fertilizers, Litres for Pesticides. */
    public static String getUnitForCategory(String category) {
        if ("Pesticides".equals(category)) return "Litres";
        return "Bags";
    }

    /** Check if a crop ID is valid. */
    public static boolean isValidCrop(String cropId) {
        return MAPPING.containsKey(cropId);
    }

    /** Get crop IDs for a given season. */
    public static List<String> getCropIdsBySeason(String season) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : CROP_SEASON.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(season)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** All crop IDs in order. */
    public static List<String> getAllCropIds() {
        return new ArrayList<>(CROP_SEASON.keySet());
    }

    // cropId -> display name
    private static final Map<String, String> CROP_NAMES = new LinkedHashMap<>();
    static {
        CROP_NAMES.put("crop-wheat", "Wheat");
        CROP_NAMES.put("crop-barley", "Barley");
        CROP_NAMES.put("crop-gram", "Gram (Chickpea)");
        CROP_NAMES.put("crop-mustard", "Mustard / Canola");
        CROP_NAMES.put("crop-peas", "Peas");
        CROP_NAMES.put("crop-rice", "Rice");
        CROP_NAMES.put("crop-cotton", "Cotton");
        CROP_NAMES.put("crop-maize", "Maize");
        CROP_NAMES.put("crop-sugarcane", "Sugarcane");
        CROP_NAMES.put("crop-bajra", "Bajra (Millet)");
    }

    /** Get display name for a crop ID. */
    public static String getCropName(String cropId) {
        return CROP_NAMES.getOrDefault(cropId, cropId);
    }

    /** Get category for an item ID. */
    public static String getCategoryForItem(String itemTypeId) {
        for (Map<String, List<ItemEntry>> cropMap : MAPPING.values()) {
            for (Map.Entry<String, List<ItemEntry>> catEntry : cropMap.entrySet()) {
                for (ItemEntry entry : catEntry.getValue()) {
                    if (entry.getItemTypeId().equals(itemTypeId)) {
                        return catEntry.getKey();
                    }
                }
            }
        }
        return "Seeds"; // default fallback
    }

    /** NEW: Get all items for a season and category. */
    public static List<ItemEntry> getItemsBySeasonAndCategory(String season, String category) {
        List<ItemEntry> result = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        for (String cropId : getCropIdsBySeason(season)) {
            for (ItemEntry item : getItems(cropId, category)) {
                if (!seenIds.contains(item.getItemTypeId())) {
                    result.add(item);
                    seenIds.add(item.getItemTypeId());
                }
            }
        }
        return result;
    }

    /** NEW: Find a suitable crop ID for a given item and season. */
    public static String findCropForItemAndSeason(String itemTypeId, String season) {
        for (String cropId : getCropIdsBySeason(season)) {
            Map<String, List<ItemEntry>> cropMap = MAPPING.get(cropId);
            if (cropMap != null) {
                for (List<ItemEntry> items : cropMap.values()) {
                    for (ItemEntry entry : items) {
                        if (entry.getItemTypeId().equals(itemTypeId)) return cropId;
                    }
                }
            }
        }
        return null;
    }
}
