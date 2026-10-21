package com.farcr.nomansland;

import com.farcr.nomansland.common.condition.BooleanConfigCondition;
import com.teamabnormals.blueprint.core.annotations.ConfigKey;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NMLConfig {

    public static ModConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_OVERRIDES = "overrides";
    public static final String CATEGORY_RECIPES = "recipe_tweaks";
    public static final List<String> VANILLA_RECIPE_TWEAKS = List.of(
            "andesite", "diorite", "granite", "lectern",
            "light_blue_dye_from_blue_orchid", "light_gray_dye_from_white_tulip", "lodestone",
            "mushroom_stew", "rabbit_stew_from_red_mushroom",
            "red_dye_from_rose_bush", "red_sandstone", "sandstone", "scaffolding",
            "smoker"
    );
    public static final Map<String, ModConfigSpec.BooleanValue> RECIPE_TWEAKS = new HashMap<>();
    public static ModConfigSpec.BooleanValue MYCELIUM_SPREADS;
    public static ModConfigSpec.BooleanValue GRASS_SPREADS;
    public static ModConfigSpec.BooleanValue MALEVOLENT_SPAWNER;
    public static ModConfigSpec.BooleanValue TRAMPLING;
    public static ModConfigSpec.BooleanValue TORCH_EXTINGUISHING;
    public static ModConfigSpec.BooleanValue GRASS_FROSTING;
    public static ModConfigSpec.BooleanValue PATH_TWEAKS;
    public static final String CATEGORY_BIOMES = "biomes";
    public static NMLConfig.Biomes BIOMES;
    public static final String CATEGORY_TAP = "tap";
    public static ModConfigSpec.DoubleValue FILLING_SPEED_MULTIPLIER;
    public static final String CATEGORY_ANCHOR = "monster_anchor";
    public static ModConfigSpec.IntValue TICKS_BETWEEN_RESURRECTIONS;
    public static final String CATEGORY_SPIKE = "spike";
    public static ModConfigSpec.DoubleValue POKING_DAMAGE;
    public static ModConfigSpec.DoubleValue FALLING_DAMAGE;
    public static ModConfigSpec.DoubleValue IMPALING_DAMAGE;
    public static ModConfigSpec.DoubleValue SKEWERING_DAMAGE;
    public static final String CATEGORY_BOMBS = "bombs";
    public static ModConfigSpec.DoubleValue EXPLOSIVE_STRENGTH;
    public static ModConfigSpec.DoubleValue FIREBOMB_STRENGTH;
    public static ModConfigSpec.BooleanValue INCENDIARY_ARROW_PLACES_FIRE;
    public static final String CATEGORY_BANDAGE = "bandage";
    public static ModConfigSpec.IntValue BANDAGE_COOLDOWN;
    public static ModConfigSpec.DoubleValue BANDAGE_HEAL_AMOUNT;
    public static final String CATEGORY_ANCIENT_BRONZE_MASK = "ancient_bronze_mask";
    public static ModConfigSpec.IntValue ANCIENT_BRONZE_MASK_HEAL_INTERVAL;
    public static ModConfigSpec.DoubleValue ANCIENT_BRONZE_MASK_HEAL_AMOUNT;
    public static final String CATEGORY_WARDING_EFFIGY = "warding_effigy";
    public static ModConfigSpec.IntValue WARDING_EFFIGY_BASE_RANGE;
    public static ModConfigSpec.IntValue WARDING_EFFIGY_RANGE_PER_EFFIGY;
    public static final String CATEGORY_BULK_PLACEMENT = "bulk_placement";
    public static ModConfigSpec.IntValue MAX_LADDER_PLACEMENT_LENGTH;
    public static ModConfigSpec.IntValue MAX_RAIL_PLACMENT_LENGTH;
    public static ModConfigSpec.IntValue MAX_FLOATING_RAILS;
    public static final String CATEGORY_MEETING_POINT = "meeting_point";
    public static ModConfigSpec.IntValue MIN_MEETING_POINT_DISTANCE;
    public static ModConfigSpec.IntValue MAX_MEETING_POINT_DISTANCE;
    public static ModConfigSpec.ConfigValue<List<? extends String>> FRIEND_MOON_DIMENSIONS;

    public static final String BELL_SANCTUARIES = "bell_sanctuaries";
    public static ModConfigSpec.IntValue MIN_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS;
    public static ModConfigSpec.IntValue MAX_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS;
    public static ModConfigSpec.IntValue BELL_CELL_SIZE_CHUNKS;

    public static final String CATEGORY_MISC = "miscellaneous";
    public static ModConfigSpec.DoubleValue BURIED_SPAWNING_CHANCE;
    public static ModConfigSpec.BooleanValue WITCHES_EAT_STEW;

    public static ModConfigSpec CLIENT_CONFIG;
    public static final String CATEGORY_FOG_MODIFIERS = "fog_modifiers";
    public static ModConfigSpec.BooleanValue FOG_MODIFIERS;
    public static ModConfigSpec.BooleanValue CAVE_BIOME_FOG_MODIFIER;
    public static ModConfigSpec.BooleanValue DEEP_DARK_FOG_MODIFIER;
    public static ModConfigSpec.BooleanValue FOGGY_BIOME_FOG_MODIFIER;
    public static ModConfigSpec.BooleanValue SURFACE_FOG_MODIFIER;
    public static ModConfigSpec.DoubleValue SURFACE_FOG_INTENSITY;

    public static final String INVERTED_BELL_CLIENT = "inverted_bell_client";
    public static ModConfigSpec.BooleanValue INVERTED_BELL_BLUR;

    public static ModConfigSpec STARTUP_CONFIG;
    public static final String CATEGORY_TORTOISE_SHELL_ATTRIBUTES = "tortoise_shell_attributes";
    public static ModConfigSpec.IntValue ARMOR_VALUE;
    public static ModConfigSpec.IntValue DURABILITY_VALUE;
    public static ModConfigSpec.DoubleValue ARMOR_TOUGHNESS_VALUE;
    public static ModConfigSpec.DoubleValue SPEED_REDUCTION_VALUE;
    public static ModConfigSpec.DoubleValue KNOCKBACK_RESISTANCE_VALUE;

    static {

        final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.comment("For configuring the mob remodels, go to the mixed litter startup config!");

        COMMON_BUILDER.push(CATEGORY_OVERRIDES);
        PATH_TWEAKS = COMMON_BUILDER
                .comment("If using a hoe/shovel on plants tills/paths the blocks below it")
                .define("farmlandPathTweaks", true);
        GRASS_SPREADS = COMMON_BUILDER
                .comment("If grass or mycelium spread to nearby dirt.")
                .define("grassSpreads", true);
        MYCELIUM_SPREADS = COMMON_BUILDER
                .define("myceliumSpreads", true);
        MALEVOLENT_SPAWNER = COMMON_BUILDER
                .comment("If monster spawners should produce malevolent (red) flames instead of regular flames.")
                .define("malevolentSpawner", true);
        TRAMPLING = COMMON_BUILDER
                .comment("If players and mobs can trample farmland")
                .define("allowTrampling", false);
        TORCH_EXTINGUISHING = COMMON_BUILDER
                .comment("If torches can be extinguished through interactions like campfires.")
                .define("torchExtinguishing", true);
        GRASS_FROSTING = COMMON_BUILDER
                .comment("If snow and grass can combine to form snowlogged frosted grass.")
                .define("allowGrassFrosting", true);
        COMMON_BUILDER.pop();

        BIOMES = new NMLConfig.Biomes(COMMON_BUILDER);

        COMMON_BUILDER.push(CATEGORY_TAP);
        COMMON_BUILDER.comment("Time is calculated in ticks. 20 ticks make 1 second.");
        FILLING_SPEED_MULTIPLIER = COMMON_BUILDER
                .comment("Multiplier on how fast taps should fill up cauldrons with resin. Set to 0.0 to disable.")
                .defineInRange("fillingSpeedMultiplier", 1.0, 0, 10);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_ANCHOR);
        TICKS_BETWEEN_RESURRECTIONS = COMMON_BUILDER
                .comment("The time between each resurrection from a monster anchor.")
                .defineInRange("ticksBetweenResurrections", 80, 78, 400);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_SPIKE);
        COMMON_BUILDER.comment("Damage is dealt in hit points. 2 hit points make 1 heart.");
        POKING_DAMAGE = COMMON_BUILDER
                .comment("The continuous damage dealt to an entity as it stands on an active spike.")
                .defineInRange("pokingDamage", 1.5, 0, Integer.MAX_VALUE);
        FALLING_DAMAGE = COMMON_BUILDER
                .comment("The added damage dealt to an entity when it falls on a spike from any height.")
                .defineInRange("fallingDamage", 2.0, 0, Integer.MAX_VALUE);
        IMPALING_DAMAGE = COMMON_BUILDER
                .comment("The damage dealt to an entity when an adjacent spike is activated.")
                .defineInRange("impalingDamage", 12.0, 0, Integer.MAX_VALUE);
        SKEWERING_DAMAGE = COMMON_BUILDER
                .comment("The damage dealt to an entity when an active spike is pushed into it.")
                .defineInRange("skeweringDamage", 12.0, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BOMBS);
        EXPLOSIVE_STRENGTH = COMMON_BUILDER
                .comment("The radius of explosives' explosion.")
                .defineInRange("explosiveExplosionRadius", 3.0, 0, Integer.MAX_VALUE);
        FIREBOMB_STRENGTH = COMMON_BUILDER
                .comment("The radius of firebombs' explosion.")
                .defineInRange("firebombExplosionRadius", 2.0, 0, Integer.MAX_VALUE);
        INCENDIARY_ARROW_PLACES_FIRE = COMMON_BUILDER
                .comment("If incendiary arrows place fire blocks where they land.")
                .define("incendiaryArrowPlacesFire", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BANDAGE);
        BANDAGE_COOLDOWN = COMMON_BUILDER
                .comment("The cooldown applied to all bandages after one is used. Time is calculated in ticks. 20 ticks make 1 second.")
                .defineInRange("bandageCooldown", 140, 0, Integer.MAX_VALUE);
        BANDAGE_HEAL_AMOUNT = COMMON_BUILDER
                .comment("The amount of health a bandage restores when applied. Healing is in hit points. 2 hit points make 1 heart.")
                .defineInRange("bandageHealAmount", 4.0, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_ANCIENT_BRONZE_MASK);
        COMMON_BUILDER.comment("Time is calculated in ticks. 20 ticks make 1 second. Healing is in hit points. 2 hit points make 1 heart.");
        ANCIENT_BRONZE_MASK_HEAL_INTERVAL = COMMON_BUILDER
                .comment("How often the Ancient Bronze Mask heals its wearer while worn on the head.")
                .defineInRange("ancientBronzeMaskHealInterval", 200, 1, Integer.MAX_VALUE);
        ANCIENT_BRONZE_MASK_HEAL_AMOUNT = COMMON_BUILDER
                .comment("The amount of health the Ancient Bronze Mask restores each interval.")
                .defineInRange("ancientBronzeMaskHealAmount", 1.0, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_WARDING_EFFIGY);
        WARDING_EFFIGY_BASE_RANGE = COMMON_BUILDER
                .comment("The warding range, in blocks, of a single Warding Effigy. Stacking additional effigies in the same block increases the range beyond this base value.")
                .defineInRange("wardingEffigyBaseRange", 24, 0, Integer.MAX_VALUE);
        WARDING_EFFIGY_RANGE_PER_EFFIGY = COMMON_BUILDER
                .comment("How many extra blocks of warding range each additional Warding Effigy stacked in the same block adds. Diminishing returns still apply as more are stacked.")
                .defineInRange("wardingEffigyRangePerEffigy", 20, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BULK_PLACEMENT);
        COMMON_BUILDER.comment("Set to 0 to disable any key.");
        MAX_LADDER_PLACEMENT_LENGTH = COMMON_BUILDER
                .comment("The maximum distance ladders can be placed like scaffolding.")
                .defineInRange("maxLadderPlacementLength", 8, 0, Integer.MAX_VALUE);
        MAX_RAIL_PLACMENT_LENGTH = COMMON_BUILDER
                .comment("The maximum distance rails can be placed like scaffolding.")
                .defineInRange("maxRailPlacementLength", 24, 0, Integer.MAX_VALUE);
        MAX_FLOATING_RAILS = COMMON_BUILDER
                .comment("The maximum distance rails can be from a supported block before breaking.")
                .defineInRange("maxFloatingRails", 5, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_MEETING_POINT);
        MIN_MEETING_POINT_DISTANCE = COMMON_BUILDER
                .comment("The minimum distance, from the center of the world, the Meeting Point should spawn at.")
                .defineInRange("minMeetingPointDistance", 1000, 0, Integer.MAX_VALUE);
        MAX_MEETING_POINT_DISTANCE = COMMON_BUILDER
                .comment("The maximum distance, from the center of the world, the Meeting Point should spawn at.")
                .defineInRange("maxMeetingPointDistance", 2500, 0, Integer.MAX_VALUE);
        FRIEND_MOON_DIMENSIONS = COMMON_BUILDER
                .comment("Dimensions the Friend Moon watches over, as full dimension ids such as \"minecraft:overworld\". Leave empty to use every dimension that shares the overworld's dimension type, which covers most custom overworld-like dimensions.")
                .defineListAllowEmpty("friendMoonDimensions", List.of(), () -> "", (entry) -> entry instanceof String);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(BELL_SANCTUARIES);
        final int bellSanctuaryMax = 50_000;
        MIN_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS = COMMON_BUILDER
                .comment("The minimum distance allowed between a pair of bell sanctuaries. Should NOT be changed after world has generated. Has NO impact on how close two DIFFERENT pairs of bell sanctuaries can be. This must be smaller than max distance")
                .defineInRange("minBellSanctuaryPairDistance", 50, 0, bellSanctuaryMax - 1);
        MAX_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS = COMMON_BUILDER
                .comment("The maximum distance allowed between a pair of bell sanctuaries. Should NOT be changed after world has generated. Has NO impact on how far away two DIFFERENT pairs of bell sanctuaries can be.")
                .defineInRange("maxBellSanctuaryPairDistance", 600, 0, bellSanctuaryMax);
        BELL_CELL_SIZE_CHUNKS = COMMON_BUILDER
                .comment("The side length of a cell used to contain bell santuary pair information. Should NOT be changed after world has generated. Has no impact on how bell sanctuary pairings are generated!")
                .defineInRange("bellSanctuarySideLength", 40, 10, 100);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_MISC);
        BURIED_SPAWNING_CHANCE = COMMON_BUILDER
                .comment("The chance a buried is spawned upon brushing a remains block.")
                .comment("This chance is multiplied by 4 when the block is broken and by 10 when the block falls.")
                .defineInRange("buriedSpawningChance", 0.05, 0, 1);
        WITCHES_EAT_STEW = COMMON_BUILDER
                .comment("Witches use bowls to drain cauldrons of witch stew.")
                .define("witchesEatStew", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_RECIPES);
        COMMON_BUILDER.comment("Toggle changes to vanilla crafting recipes.");
        for (String recipe : VANILLA_RECIPE_TWEAKS) {
            RECIPE_TWEAKS.put(recipe, COMMON_BUILDER.define(recipe, true));
        }
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();

        VANILLA_RECIPE_TWEAKS.forEach(recipe -> BooleanConfigCondition.OPTIONS.put(recipe, () -> RECIPE_TWEAKS.get(recipe).get()));

        final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        CLIENT_BUILDER.push(CATEGORY_FOG_MODIFIERS);
        FOG_MODIFIERS = CLIENT_BUILDER
                .comment("If any custom fog modifiers are enabled")
                .define("fogModifiers", true);
        CAVE_BIOME_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the caves biome fog modifier is enabled")
                .define("cavesBiomeFogModifier", true);
        DEEP_DARK_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the deep dark fog modifier is enabled")
                .define("deepDarkFogModifier", true);
        FOGGY_BIOME_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the foggy biome fog modifier is enabled")
                .define("foggyBiomeFogModifier", true);
        SURFACE_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the general fog modifier applied everywhere, is enabled")
                .define("surfaceFogModifier", true);
        SURFACE_FOG_INTENSITY = CLIENT_BUILDER
                .comment("How strongly the general fog modifier pulls fog towards the camera; 0 disables it entirely, 1 is the default look")
                .defineInRange("surfaceFogIntensity", 1.0, 0.1, 1.0);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push(INVERTED_BELL_CLIENT);
        INVERTED_BELL_BLUR = CLIENT_BUILDER
                .comment("Whether to apply a blur effect while teleporting via Inverted Bell")
                .define("invertedBellBlur", true);
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();

        final ModConfigSpec.Builder STARTUP_BUILDER = new ModConfigSpec.Builder();

        STARTUP_BUILDER.comment("For configuring the mob remodels, go to the mixed litter startup config!");

        STARTUP_BUILDER.push(CATEGORY_TORTOISE_SHELL_ATTRIBUTES);
        STARTUP_BUILDER.comment("The attributes of the tortoise shell armor item");
        DURABILITY_VALUE = STARTUP_BUILDER
                .defineInRange("durability", 670, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ARMOR_VALUE = STARTUP_BUILDER
                .defineInRange("armor", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ARMOR_TOUGHNESS_VALUE = STARTUP_BUILDER
                .defineInRange("toughness", 3.0, -100, 100);
        SPEED_REDUCTION_VALUE = STARTUP_BUILDER
                .defineInRange("speedReduction", -0.3, -100, 100);
        KNOCKBACK_RESISTANCE_VALUE = STARTUP_BUILDER
                .defineInRange("knockbackResistance", 0.2, -100, 100);
        STARTUP_BUILDER.pop();

        STARTUP_CONFIG = STARTUP_BUILDER.build();
    }

    public static final class Biomes {
        @ConfigKey("biomes")
        public final ModConfigSpec.BooleanValue biomes;

        @ConfigKey("caves_biomes")
        public final ModConfigSpec.BooleanValue cavesBiomes;

        @ConfigKey("maple_biomes")
        public final ModConfigSpec.BooleanValue mapleBiomes;

        @ConfigKey("old_growth_forest")
        public final ModConfigSpec.BooleanValue oldGrowthForest;

        @ConfigKey("autumnal_forest")
        public final ModConfigSpec.BooleanValue autumnalForest;

        @ConfigKey("dark_taiga")
        public final ModConfigSpec.BooleanValue darkTaiga;

        @ConfigKey("boreal_forest")
        public final ModConfigSpec.BooleanValue borealForest;

        @ConfigKey("dark_swamp")
        public final ModConfigSpec.BooleanValue darkSwamp;

        @ConfigKey("frozen_woods")
        public final ModConfigSpec.BooleanValue frozenWoods;

        @ConfigKey("bayou")
        public final ModConfigSpec.BooleanValue bayou;

        @ConfigKey("bog")
        public final ModConfigSpec.BooleanValue bog;

        @ConfigKey("prairie")
        public final ModConfigSpec.BooleanValue prairie;

        @ConfigKey("lavender_field")
        public final ModConfigSpec.BooleanValue lavenderField;

        @ConfigKey("blackwater_river")
        public final ModConfigSpec.BooleanValue blackwaterRiver;

        @ConfigKey("lush_river")
        public final ModConfigSpec.BooleanValue lushRiver;

        @ConfigKey("desert_river")
        public final ModConfigSpec.BooleanValue desertRiver;

        @ConfigKey("frozen_shore")
        public final ModConfigSpec.BooleanValue frozenShore;

        @ConfigKey("mud_beach")
        public final ModConfigSpec.BooleanValue mudBeach;

        @ConfigKey("tropical_beach")
        public final ModConfigSpec.BooleanValue tropicalBeach;

        public Biomes(ModConfigSpec.Builder builder) {
            builder.push(CATEGORY_BIOMES);
            this.biomes = builder
                .comment("If any custom biomes are enabled")
                .define("biomes", true);
            this.cavesBiomes = builder
                .comment("If the generic caves biomes are enabled")
                .define("genericCavesBiomes", true);
            this.mapleBiomes = builder
                .comment("If the maple biomes are enabled")
                .define("mapleBiomes", true);
            this.oldGrowthForest = builder
                .comment("If the old growth forest is enabled")
                .define("oldGrowthForest", true);
            this.autumnalForest = builder
                .comment("If the autumnal forest is enabled")
                .define("autumnal", true);
            this.borealForest = builder
                .comment("If the boreal forest is enabled")
                .define("borealForest", true);
            this.darkTaiga = builder
                .comment("If the dark taiga is enabled")
                .define("darkTaiga", true);
            this.darkSwamp = builder
                .comment("If the dark swamp is enabled")
                .define("darkSwamp", true);
            this.frozenWoods = builder
                .comment("If the frozen woods is enabled")
                .define("frozenWoods", true);
            this.bayou = builder
                .comment("If the bayou is enabled")
                .define("bayou", true);
            this.bog = builder
                .comment("If the bog is enabled")
                .define("bog", true);
            this.prairie = builder
                .comment("If the prairie is enabled")
                .define("prairie", true);
            this.lavenderField = builder
                .comment("If the lavender field is enabled")
                .define("lavenderField", true);
            this.blackwaterRiver = builder
                .comment("If the blackwater river sub-biome is enabled")
                .define("blackwaterRiver", true);
            this.lushRiver = builder
                .comment("If the lush river sub-biome is enabled")
                .define("lushRiver", true);
            this.desertRiver = builder
                .comment("If the desert river sub-biome is enabled")
                .define("desertRiver", true);
            this.frozenShore = builder
                .comment("If the frozen shore sub-biome is enabled")
                .define("frozenShore", true);
            this.mudBeach = builder
                .comment("If the mud beach sub-biome is enabled")
                .define("mudBeach", true);
            this.tropicalBeach = builder
                .comment("If the tropical beach sub-biome is enabled")
                .define("tropicalBeach", true);
            builder.pop();
        }
    }
}
