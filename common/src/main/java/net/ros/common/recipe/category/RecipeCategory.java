package net.ros.common.recipe.category;

import lombok.Getter;
import net.ros.common.recipe.RecipeBase;
import net.ros.common.recipe.ingredient.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class RecipeCategory
{
    private String           name;
    private List<RecipeBase> recipes;

    public RecipeCategory(String name)
    {
        this.name = name;

        this.recipes = new ArrayList<>();
    }

    public <T> boolean inputMatchWithoutCount(final int recipeSlot, final T ingredient)
    {
        return this.recipes.stream().anyMatch(recipe ->
        {
            if (!recipe.hasInputType(ingredient.getClass())
                    || recipe.getRecipeInputs(ingredient.getClass()).size() < recipeSlot)
                return false;
            return ((RecipeIngredient<T>) recipe.getRecipeInputs(ingredient.getClass()).get(recipeSlot))
                    .match(ingredient);
        });
    }

    public <T> boolean inputMatchWithCount(final int recipeSlot, final T ingredient)
    {
        return this.recipes.stream().anyMatch(recipe ->
        {
            if (!recipe.hasInputType(ingredient.getClass())
                    || recipe.getRecipeInputs(ingredient.getClass()).size() < recipeSlot)
                return false;
            return ((RecipeIngredient<T>) recipe.getRecipeInputs(ingredient.getClass()).get(recipeSlot))
                    .matchWithQuantity(ingredient);
        });
    }

    @SuppressWarnings("unchecked")
    public Optional<RecipeBase> getRecipe(Object... inputs)
    {
        return this.recipes.stream().filter(recipe ->
        {
            int i = 0;
            for (final Object ingredient : inputs)
            {
                if (!recipe.hasInputType(ingredient.getClass())
                        || i >= recipe.getRecipeInputs(ingredient.getClass()).size())
                    break;
                if (!((RecipeIngredient<Object>) recipe.getRecipeInputs(ingredient.getClass()).get(i))
                        .matchWithQuantity(ingredient))
                    return false;
                i++;
            }
            return true;
        }).findFirst();
    }

    @SuppressWarnings("unchecked")
    public List<RecipeBase> getRecipesLike(Object... inputs)
    {
        return this.recipes.stream().filter(recipe ->
        {
            for (Object ingredient : inputs)
            {
                if (!recipe.hasInputType(ingredient.getClass()))
                    break;
                if (recipe.getRecipeInputs(ingredient.getClass()).stream().noneMatch(recipeIngredient ->
                        ((RecipeIngredient<Object>) recipeIngredient).match(ingredient)))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public void add(RecipeBase recipe)
    {
        recipes.add(recipe);
    }
}
