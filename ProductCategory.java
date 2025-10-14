/**
 * Enum representing the different product categories available in the marketplace.
 * Each category has a display name for user-friendly presentation.
 */
public enum ProductCategory {
    SHOES("Shoes"),
    CLOTHING("Clothing"),
    ACCESSORIES("Accessories"),
    ELECTRONICS("Electronics"),
    HOME_GARDEN("Home & Garden"),
    SPORTS_OUTDOORS("Sports & Outdoors"),
    BOOKS_MEDIA("Books & Media");
    
    private final String displayName;
    
    /**
     * Constructor for ProductCategory enum.
     * @param displayName The user-friendly display name for the category
     */
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name for the category.
     * @return The user-friendly display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Validates if a given string represents a valid category.
     * @param categoryName The category name to validate
     * @return true if the category is valid, false otherwise
     */
    public static boolean isValidCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        
        try {
            valueOf(categoryName.toUpperCase().replace(" ", "_").replace("&", ""));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Gets a ProductCategory from a display name string.
     * @param displayName The display name to convert
     * @return The corresponding ProductCategory, or null if not found
     */
    public static ProductCategory fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        for (ProductCategory category : values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName.trim())) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Gets a ProductCategory from an enum name string (case-insensitive).
     * @param enumName The enum name to convert
     * @return The corresponding ProductCategory, or null if not found
     */
    public static ProductCategory fromString(String enumName) {
        if (enumName == null || enumName.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(enumName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Returns all available categories as display names.
     * @return Array of display names for all categories
     */
    public static String[] getAllDisplayNames() {
        ProductCategory[] categories = values();
        String[] displayNames = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            displayNames[i] = categories[i].getDisplayName();
        }
        return displayNames;
    }
    
    /**
     * Returns a formatted string of all available categories for user display.
     * @return Formatted string listing all categories
     */
    public static String getFormattedCategoryList() {
        StringBuilder sb = new StringBuilder();
        ProductCategory[] categories = values();
        for (int i = 0; i < categories.length; i++) {
            sb.append((i + 1)).append(". ").append(categories[i].getDisplayName());
            if (i < categories.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}