package dev.nightmarehuy.cryonite.compat.jei;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.item.ItemHints;
import dev.nightmarehuy.cryonite.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * JEI integration: documents everything a crafting screen cannot show.
 * <ul>
 *   <li>{@link WorldInteractionRecipeCategory} - quenching and stabilization done in the world;</li>
 *   <li>{@link AcquisitionRecipeCategory} - where the ores and the smithing templates are found;</li>
 *   <li>info pages ("i" tab of JEI) of every material, built from the same texts.</li>
 * </ul>
 * JEI is optional: this package is only ever loaded by JEI itself, on the client.
 */
@JeiPlugin
public class CryoniteJeiPlugin implements IModPlugin {
    public static final RecipeType<WorldInteractionJeiRecipe> WORLD_INTERACTION =
            RecipeType.create(Cryonite.MODID, "world_interaction", WorldInteractionJeiRecipe.class);
    public static final RecipeType<AcquisitionJeiRecipe> ACQUISITION =
            RecipeType.create(Cryonite.MODID, "acquisition", AcquisitionJeiRecipe.class);

    private static final ResourceLocation UID = Cryonite.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new WorldInteractionRecipeCategory(guiHelper, WorldInteractionJeiRecipe.all()),
                new AcquisitionRecipeCategory(guiHelper, AcquisitionJeiRecipe.all()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WORLD_INTERACTION, WorldInteractionJeiRecipe.all());
        registration.addRecipes(ACQUISITION, AcquisitionJeiRecipe.all());
        registerInfoPages(registration);
    }

    /** The same facts as the two categories, as plain text on the info page of every material. */
    private static void registerInfoPages(IRecipeRegistration registration) {
        // ---- tier 1: Cryonite
        info(registration, ModItems.CRYONITE_ORE, acquisition("cryonite_ore"), tool("cryonite_ore"));
        info(registration, ModItems.RAW_CRYONITE, acquisition("cryonite_ore"), tool("cryonite_ore"), page("raw_cryonite"));
        info(registration, ModItems.CRYONITE_NUGGET, page("cryonite_nugget"));
        info(registration, ModItems.CRYONITE_CRYSTAL_DUST, page("cryonite_crystal_dust"));
        info(registration, ModItems.CRYONITE_INGOT, page("cryonite_ingot"));
        info(registration, ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE, acquisition("cryonite_template"));

        // ---- tier 2: Infernite
        info(registration, ModItems.INFERNITE_NUGGET, page("infernite_nugget"));
        info(registration, ModItems.INFERNITE_ORE, acquisition("infernite_ore"), tool("infernite_ore"));
        info(registration, ModItems.MOLTEN_INFERNITE_SHARD, acquisition("infernite_ore"), tool("infernite_ore"),
                interaction("powder_snow_cauldron"), interaction("blue_ice"), page("molten_infernite_shard"));
        info(registration, ModItems.COOLED_INFERNITE_BLANK, page("cooled_infernite_blank"));
        info(registration, ModItems.INFERNITE_INGOT, page("infernite_ingot"));
        info(registration, ModItems.INFERNITE_BLOCK, page("infernite_block"));
        info(registration, ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE, acquisition("infernite_template"));

        // ---- tier 3: Void
        info(registration, ModItems.VOID_NUGGET, page("void_nugget"));
        info(registration, ModItems.VOID_ORE, acquisition("void_ore"), tool("void_ore"));
        info(registration, ModItems.UNSTABLE_VOID_SHARD, acquisition("void_ore"), tool("void_ore"),
                interaction("dragon_breath"), page("unstable_void_shard"));
        info(registration, ModItems.STABILIZED_VOID_CLUSTER, page("stabilized_void_cluster"));
        info(registration, ModItems.MOLTEN_VOID_BLANK, page("molten_void_blank"), interaction("crying_obsidian"), page("molten_void_blank.craft"));
        info(registration, ModItems.VOID_INGOT, page("void_ingot"));
        info(registration, ModItems.VOID_UPGRADE_SMITHING_TEMPLATE, acquisition("void_template"));

        // ---- tier 4: Aether, and the smelter
        info(registration, ModItems.AETHER_NUGGET, page("aether_nugget"));
        info(registration, ModItems.INACTIVE_AETHER, page("inactive_aether"));
        info(registration, ModItems.AETHER_INGOT, page("aether_ingot"));
        info(registration, ModItems.PRIMORDIAL_SMELTER, page("primordial_smelter"));

        // ---- Create integration: the block is only registered while Create is installed
        BuiltInRegistries.ITEM.getOptional(Cryonite.KINETIC_ENERGY_CONVERTER)
                .ifPresent(converter -> info(registration, converter, page("kinetic_energy_converter")));
    }

    private static void info(IRecipeRegistration registration, ItemLike item, String... translationKeys) {
        Component[] paragraphs = new Component[translationKeys.length];
        for (int i = 0; i < translationKeys.length; i++) {
            paragraphs[i] = Component.translatable(translationKeys[i]);
        }
        registration.addIngredientInfo(item, paragraphs);
    }

    private static String acquisition(String name) {
        return AcquisitionJeiRecipe.descriptionKey(name);
    }

    private static String interaction(String name) {
        return WorldInteractionJeiRecipe.descriptionKey(name);
    }

    /** The "Pickaxe: ..." line of the ore tooltip. */
    private static String tool(String ore) {
        return ItemHints.key(ore, "tool");
    }

    private static String page(String item) {
        return "jei.cryonite.info." + item;
    }
}
