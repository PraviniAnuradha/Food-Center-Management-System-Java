package builder;

import java.util.HashSet;
import java.util.Set;
import model.Ingredient;

public class CartItem {

    private String item_id;
    private String price;
    private String qty;
    private String discount;
    private Set<Ingredient> ingredients;

    private CartItem(Builder builder) {
        this.item_id = builder.item_id;
        this.price = builder.price;
        this.qty = builder.qty;
        this.discount = builder.discount;
        this.ingredients = builder.ingredients;
    }

    public String getItemId() {
        return item_id;
    }

    public String getPrice() {
        return price;
    }

    public String getQty() {
        return qty;
    }

    public String getDiscount() {
        return discount;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public double calculateExtraPrice() {
        double itemTotal = 0;
        for (Ingredient ingredient : ingredients) {
            itemTotal += ingredient.getPrice();
        }
        return itemTotal;
    }

    public static class Builder {

        private String item_id;
        private String price;
        private String qty;
        private String discount;
        private Set<Ingredient> ingredients;

        public Builder(String item_id, String price, String qty) {
            this.item_id = item_id;
            this.price = price;
            this.qty = qty;
            this.ingredients = new HashSet<>();
        }

        public Builder discount(String discount) {
            this.discount = discount;
            return this;
        }

        public Builder addIngredient(Ingredient ingredient) {
            this.ingredients.add(ingredient);
            return this;
        }

        public CartItem build() {
            return new CartItem(this);
        }
    }
}
