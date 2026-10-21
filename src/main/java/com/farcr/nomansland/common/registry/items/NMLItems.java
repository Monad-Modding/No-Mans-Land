package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NMLEnumParams;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.item.*;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NMLItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NoMansLand.MODID);
    public static List<ItemDefinition<?>> ITEM_DEFINITIONS = new ArrayList<>();
    public BuildCreativeModeTabContentsEvent event = null;

    public static final ItemDefinition<Item> NO_MANS_GLOBE = registerWithoutTab("no_mans_globe",
            () -> new Item(new Properties()), true);
    public static final ItemDefinition<Item> TRINKET = registerWithoutTab("trinket",
            () -> new Item(new Properties()));
    public static LinkedHashSet<ItemDefinition<?>> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();
    //Foods
    public static final ItemDefinition<Item> MASHED_POTATOES_WITH_MUSHROOMS = register("mashed_potatoes_with_mushrooms",
            () -> new Item(new Properties().food(NMLFoods.MASHED_POTATOES_WITH_MUSHROOMS).stacksTo(1)));
    public static final ItemDefinition<Item> GRILLED_MUSHROOMS = register("grilled_mushrooms", () -> new Item(new Properties().food(NMLFoods.GRILLED_MUSHROOMS)));

    public static final ItemDefinition<Item> FROG_LEG = register("frog_leg", () -> new Item(new Properties().food(NMLFoods.FROG_LEG)));
    public static final ItemDefinition<Item> COOKED_FROG_LEG = register("cooked_frog_leg", () -> new Item(new Properties().food(NMLFoods.COOKED_FROG_LEG)));
    public static final ItemDefinition<Item> RAW_HORSE = register("raw_horse", () -> new Item(new Properties().food(NMLFoods.RAW_HORSE)));
    public static final ItemDefinition<Item> HORSE_STEAK = register("horse_steak", () -> new Item(new Properties().food(NMLFoods.HORSE_STEAK)));
    public static final ItemDefinition<Item> RAW_VENISON = register("raw_venison", () -> new Item(new Properties().food(NMLFoods.RAW_VENISON)));
    public static final ItemDefinition<Item> COOKED_VENISON = register("cooked_venison", () -> new Item(new Properties().food(NMLFoods.COOKED_VENISON)));

    public static final ItemDefinition<Item> BILLHOOK_BASS = register("billhook_bass",
            () -> new Item(new Properties().food(NMLFoods.BILLHOOK_BASS)), true);
    public static final ItemDefinition<Item> COOKED_BILLHOOK_BASS = register("cooked_billhook_bass", () -> new Item(new Properties().food(NMLFoods.COOKED_BILLHOOK_BASS)));
//    public static final ItemDefinition<Item> CAVE_CARP = registerItem("cave_carp",
//            () -> new Item(new Properties().food(NMLFoods.CAVE_CARP)), true);

    public static final ItemDefinition<Item> PEAR = register("pear", () -> new Item(new Properties().food(NMLFoods.PEAR)));
    public static final ItemDefinition<Item> SYRUPED_PEAR = register("syruped_pear", () -> new Item(new Properties().food(NMLFoods.SYRUPED_PEAR)));
    public static final ItemDefinition<Item> HONEYED_APPLE = register("honeyed_apple", () -> new Item(new Properties().food(NMLFoods.HONEYED_APPLE)));
    public static final ItemDefinition<Item> PANCAKE = register("pancake", () -> new Item(new Properties().food(NMLFoods.PANCAKE)));
    public static final ItemDefinition<Item> MAPLE_SYRUP_BOTTLE = register("maple_syrup_bottle",
            () -> new MapleSyrupBottleItem(new Properties().food(NMLFoods.MAPLE_SYRUP_BOTTLE).craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final ItemDefinition<Item> MAPLE_TART = register("maple_tart", () -> new Item(new Properties().food(NMLFoods.MAPLE_TART)));
    public static final ItemDefinition<Item> SWEET_TART = register("sweet_tart", () -> new Item(new Properties().food(NMLFoods.SWEET_TART)));
    public static final ItemDefinition<Item> PINE_NUTS = register("pine_nuts", () -> new Item(new Properties().food(NMLFoods.PINE_NUTS)));
    public static final ItemDefinition<Item> WALNUTS = register("walnuts", () -> new Item(new Properties().food(NMLFoods.WALNUTS)));
    public static final ItemDefinition<Item> TRAIL_MIX = register("trail_mix", () -> new Item(new Properties().food(NMLFoods.TRAIL_MIX)));

    public static final ItemDefinition<Item> HARDTACK = register("hardtack", () -> new Item(new Properties().food(NMLFoods.HARDTACK)));

    //Materials
    public static final ItemDefinition<Item> RESIN = register("resin",
            () -> new Item(new Properties()));
    public static final ItemDefinition<Item> STURDY_SCUTE = register("sturdy_scute",
            () -> new Item(new Properties()));
    public static final ItemDefinition<SulfurItem> SULFUR = register("sulfur",
            () -> new SulfurItem(new Properties()));
    public static final ItemDefinition<Item> ORPIMENT = register("orpiment",
            () -> new Item(new Properties()));
    public static final ItemDefinition<Item> BILE_SAC = register("bile_sac",
            () -> new Item(new Properties()));
    public static final ItemDefinition<Item> CHARCOAL_FILTER = register("charcoal_filter",
            () -> new Item(new Properties()));
    public static final ItemDefinition<GasMaskItem> GAS_MASK = register("gas_mask",
            () -> new GasMaskItem(NMLArmorMaterials.GAS_MASK, new Properties().stacksTo(1)
                    .component(NMLDataComponents.GAS_MASK_DURATION, GasMaskItem.CRAFT_DURATION)));

    public static final ItemDefinition<Item> RESIN_OIL_BOTTLE = register("resin_oil_bottle",
            () -> new ResinOilBottleItem(new Properties()
                    .stacksTo(16)
                    .craftRemainder(Items.GLASS_BOTTLE)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(NMLEffects.FLAMMABLE, 2400)))))
    );

    public static final ItemDefinition<Item> SCONCE_TORCH = register("sconce_torch",
            () -> new StandingAndWallBlockItem(NMLBlocks.SCONCE_TORCH.get(), NMLBlocks.SCONCE_WALL_TORCH.get(), new Properties(), Direction.DOWN));
    public static final ItemDefinition<Item> SCONCE_SOUL_TORCH = register("sconce_soul_torch",
            () -> new StandingAndWallBlockItem(NMLBlocks.SCONCE_SOUL_TORCH.get(), NMLBlocks.SCONCE_SOUL_WALL_TORCH.get(), new Properties(), Direction.DOWN));

    public static final ItemDefinition<AncientPotItem> ANCIENT_POT = registerWithoutTab("ancient_pot",
            () -> new AncientPotItem(NMLBlocks.ANCIENT_POT.get(), new Properties()));
    public static final ItemDefinition<AncientPotItem> LARGE_ANCIENT_POT = registerWithoutTab("large_ancient_pot",
            () -> new AncientPotItem(NMLBlocks.LARGE_ANCIENT_POT.get(), new Properties()));

    public static final ItemDefinition<AncientPotDebugItem> POTMASTER_DEBUG_STICK = registerWithoutTab("pot_debug_stick", () -> new AncientPotDebugItem(new Properties().stacksTo(1)));

    // Moonlight Items
    public static final ItemDefinition<BlockItem> MOONLIGHT_BASIN = registerWithoutTab("moonlight_basin",
            () -> new BlockItem(NMLBlocks.MOONLIGHT_BASIN.get(), new Properties()));
    public static final ItemDefinition<BlockItem> MOONLIGHT_CANDLE = registerWithoutTab("moonlight_candle",
            () -> new BlockItem(NMLBlocks.MOONLIGHT_CANDLE.get(), new Properties()));
    public static final ItemDefinition<BlockItem> MOON_CARVING = registerWithoutTab("moon_carving",
            () -> new BlockItem(NMLBlocks.MOON_CARVING.get(), new Properties()));
    public static final ItemDefinition<BlockItem> INVERTED_BELL = registerWithoutTab("inverted_bell",
            () -> new BlockItem(NMLBlocks.INVERTED_BELL.get(), new Properties()));

    public static final ItemDefinition<Item> FIREBOMB = register("firebomb",
            () -> new FirebombItem(new Properties().stacksTo(8)));
    public static final ItemDefinition<Item> INK_BOMB = register("ink_bomb",
            () -> new InkBombItem(new Properties().stacksTo(8)));
    public static final ItemDefinition<Item> EXPLOSIVE = register("explosive",
            () -> new ExplosiveItem(new Properties().stacksTo(8)));
    public static final ItemDefinition<Item> ACRID_BOMB = register("acrid_bomb",
            () -> new AcridBombItem(new Properties().stacksTo(8)));
    public static final ItemDefinition<AquaRegiaItem> AQUA_REGIA = register("aqua_regia",
            () -> new AquaRegiaItem(new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> LIVING_URN = register("living_urn",
            () -> new LivingUrnItem(new Properties().stacksTo(8).rarity(Rarity.UNCOMMON)));
    public static final ItemDefinition<Item> INCENDIARY_ARROW = register("incendiary_arrow",
            () -> new IncendiaryArrowItem(new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> TORTOISE_SHELL = register("tortoise_shell",
            () -> new TortoiseShellItem(NMLArmorMaterials.TORTOISE, ArmorItem.Type.CHESTPLATE, new Properties()
                    .durability(NMLConfig.DURABILITY_VALUE.get())
                    .component(NMLDataComponents.TIME_WHEN_DISABLED, 0L))
    );
    public static final ItemDefinition<Item> ANCIENT_BRONZE_MASK = register("ancient_bronze_mask",
            () -> new AncientBronzeMaskItem(NMLArmorMaterials.ANCIENT_BRONZE_MASK, ArmorItem.Type.HELMET, new Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(10))
                    .rarity(Rarity.RARE)
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(false)
                    ).component(NMLDataComponents.PUNCH_COOLDOWN, 0)
                    .component(NMLDataComponents.PUNCH_COUNT, 0))
    );

    public static final Tier TIER_RITUAL = new ToolTier(
            NMLTags.INCORRECT_FOR_RITUAL_TOOL,
            Integer.MAX_VALUE,
            Tiers.STONE.getSpeed(),
            0, 25,
            () -> Ingredient.EMPTY
    );

    public static final ItemDefinition<Item> ANCESTRAL_OATH_SWORD = register("ancestral_oath_sword",
        () -> new AncestralOathSwordItem(TIER_RITUAL, new Properties()
            .attributes(SwordItem.createAttributes(TIER_RITUAL, 5.0F, -2.4F))
            .rarity(Rarity.RARE)
            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
        )
    );

    public static final ItemDefinition<Item> RITUAL_PICK = register("ritual_pick",
            () -> new RitualPickItem(TIER_RITUAL, new Properties()
                    .attributes(PickaxeItem.createAttributes(TIER_RITUAL, 4.0F, -2.8F))
                    .rarity(Rarity.RARE)
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
            )
    );

    public static final ItemDefinition<ChiselItem> ANCIENT_BRONZE_CHISEL = register("ancient_bronze_chisel",
            () -> new ChiselItem(new Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
    ));

    public static final ItemDefinition<Item> MUSIC_DISC_GUIDANCE = register("music_disc_guidance",
            () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(NMLDiscs.GUIDANCE)), true);

    public static final ItemDefinition<Item> WOODEN_SCAFFOLDING = register("wooden_scaffolding",
            () -> new ScaffoldingBlockItem(NMLBlocks.WOODEN_SCAFFOLDING.get(), new Properties()));

    public static final ItemDefinition<Item> BILLHOOK_BASS_BUCKET = register("billhook_bass_bucket",
            () -> new MobBucketItem(NMLEntities.BILLHOOK_BASS.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH,
                    (new Properties()).stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)), true);

    public static final ItemDefinition<Item> BILLHOOK_BASS_SPAWN_EGG = register("billhook_bass_spawn_egg",
            () -> new DeferredSpawnEggItem(NMLEntities.BILLHOOK_BASS, 6443553, 11236417, new Properties()));

    public static final ItemDefinition<Item> TORTOISE_SPAWN_EGG = register("tortoise_spawn_egg", // TODO: color
            () -> new DeferredSpawnEggItem(NMLEntities.TORTOISE, 3681313, 7098676, new Properties()));

    public static final ItemDefinition<Item> DEER_SPAWN_EGG = register("deer_spawn_egg",
            () -> new DeferredSpawnEggItem(NMLEntities.DEER, 8412743, 12828347, new Properties()));

    public static final ItemDefinition<Item> GOOSE_SPAWN_EGG = register("goose_spawn_egg",
            () -> new DeferredSpawnEggItem(NMLEntities.GOOSE, 11773851, 11888408, new Properties()));

    // TODO: moose
//    public static final ItemDefinition<Item> MOOSE_SPAWN_EGG = register("moose_spawn_egg",
//            () -> new DeferredSpawnEggItem(NMLEntities.MOOSE, 5323048, 2694937, new Properties()));

    public static final ItemDefinition<Item> BUDDY_SPAWN_EGG = register("buddy_spawn_egg",
        () -> new DeferredSpawnEggItem(NMLEntities.BUDDY, 9252139, 4798761, new Properties()));

//    public static final ItemDefinition<Item> CAVE_CARP_BUCKET = registerItem("cave_carp_bucket",
//            () -> new MobBucketItem(EntityType.PIG, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH,
//                    (new Properties()).stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)), true);

    public static final ItemDefinition<Item> PINE_SIGN = register("pine_sign",
            () -> new SignItem(new Properties().stacksTo(16), NMLBlocks.PINE.sign().get(), NMLBlocks.PINE.wallSign().get()));
    public static final ItemDefinition<Item> PINE_HANGING_SIGN = register("pine_hanging_sign",
            () -> new HangingSignItem(NMLBlocks.PINE.hangingSign().get(), NMLBlocks.PINE.hangingWallSign().get(), new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> PINE_BOAT = register("pine_boat",
            () -> new BoatItem(false, NMLEnumParams.PINE_BOAT_TYPE.getValue(), new Properties().stacksTo(1)));
    public static final ItemDefinition<Item> PINE_CHEST_BOAT = register("pine_chest_boat",
            () -> new BoatItem(true, NMLEnumParams.PINE_BOAT_TYPE.getValue(), new Properties().stacksTo(1)), true);

    public static final ItemDefinition<Item> MAPLE_SIGN = register("maple_sign",
            () -> new SignItem(new Properties().stacksTo(16), NMLBlocks.MAPLE.sign().get(), NMLBlocks.MAPLE.wallSign().get()));
    public static final ItemDefinition<Item> MAPLE_HANGING_SIGN = register("maple_hanging_sign",
            () -> new HangingSignItem(NMLBlocks.MAPLE.hangingSign().get(), NMLBlocks.MAPLE.hangingWallSign().get(), new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> MAPLE_BOAT = register("maple_boat",
            () -> new BoatItem(false, NMLEnumParams.MAPLE_BOAT_TYPE.getValue(), new Properties().stacksTo(1)));
    public static final ItemDefinition<Item> MAPLE_CHEST_BOAT = register("maple_chest_boat",
            () -> new BoatItem(true, NMLEnumParams.MAPLE_BOAT_TYPE.getValue(), new Properties().stacksTo(1)), true);

    public static final ItemDefinition<Item> WALNUT_SIGN = register("walnut_sign",
            () -> new SignItem(new Properties().stacksTo(16), NMLBlocks.WALNUT.sign().get(), NMLBlocks.WALNUT.wallSign().get()));
    public static final ItemDefinition<Item> WALNUT_HANGING_SIGN = register("walnut_hanging_sign",
            () -> new HangingSignItem(NMLBlocks.WALNUT.hangingSign().get(), NMLBlocks.WALNUT.hangingWallSign().get(), new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> WALNUT_BOAT = register("walnut_boat",
            () -> new BoatItem(false, NMLEnumParams.WALNUT_BOAT_TYPE.getValue(), new Properties().stacksTo(1)));
    public static final ItemDefinition<Item> WALNUT_CHEST_BOAT = register("walnut_chest_boat",
            () -> new BoatItem(true, NMLEnumParams.WALNUT_BOAT_TYPE.getValue(), new Properties().stacksTo(1)), true);

    public static final ItemDefinition<Item> WILLOW_SIGN = register("willow_sign",
            () -> new SignItem(new Properties().stacksTo(16), NMLBlocks.WILLOW.sign().get(), NMLBlocks.WILLOW.wallSign().get()));
    public static final ItemDefinition<Item> WILLOW_HANGING_SIGN = register("willow_hanging_sign",
            () -> new HangingSignItem(NMLBlocks.WILLOW.hangingSign().get(), NMLBlocks.WILLOW.hangingWallSign().get(), new Properties().stacksTo(16)));
    public static final ItemDefinition<Item> WILLOW_BOAT = register("willow_boat",
            () -> new BoatItem(false, NMLEnumParams.WILLOW_BOAT_TYPE.getValue(), new Properties().stacksTo(1)));
    public static final ItemDefinition<Item> WILLOW_CHEST_BOAT = register("willow_chest_boat",
            () -> new BoatItem(true, NMLEnumParams.WILLOW_BOAT_TYPE.getValue(), new Properties().stacksTo(1)), true);

    public static final ItemDefinition<Item> FIELD_MUSHROOM = register("field_mushroom", () -> new BlockItem(NMLBlocks.FIELD_MUSHROOM.get(), new Properties()));
    public static final ItemDefinition<Item> DUCKWEED = register("duckweed",
            () -> new PlaceOnWaterBlockItem(NMLBlocks.DUCKWEED.get(), new Properties()));

    public static final ItemDefinition<Item> FROSTED_GRASS = register("frosted_grass",
            () -> new FrostedGrassBlockItem(NMLBlocks.FROSTED_GRASS.get(), new Properties()));

    public static final ItemDefinition<Item> WATER_MOSAIC = register("water_mosaic",
            () -> new PlaceOnWaterBlockItem(NMLBlocks.WATER_MOSAIC.get(), new Properties()));

    public static final ItemDefinition<Item> AWKWARD_RESIDUE = register("awkward_residue", () -> new Item(new Properties().food(NMLFoods.AWKWARD_RESIDUE)));

    public static final ItemDefinition<Item> WARDING_EFFIGY = register("warding_effigy",
            () -> new BlockItem(NMLBlocks.WARDING_EFFIGY.get(), new Properties().rarity(Rarity.UNCOMMON)));

    public static final ItemDefinition<BandageItem> BANDAGE = register("bandage",
            () -> new BandageItem(new Properties().stacksTo(16)));

    public static final ItemDefinition<CuringBandageItem> ANTIDOTE_BANDAGE = register("antidote_bandage",
            () -> new CuringBandageItem(new Properties().stacksTo(16), List.of(
                    MobEffects.POISON,
                    NMLEffects.CORROSION
                    // Add Decay when it's implemented
            )));

    public static final ItemDefinition<CuringBandageItem> MEDICINAL_BANDAGE = register("medicinal_bandage",
            () -> new CuringBandageItem(new Properties().stacksTo(16), List.of(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    MobEffects.DIG_SLOWDOWN,
                    MobEffects.CONFUSION,
                    MobEffects.BLINDNESS,
                    MobEffects.HUNGER,
                    MobEffects.WEAKNESS,
                    MobEffects.WIND_CHARGED,
                    MobEffects.WEAVING,
                    MobEffects.OOZING,
                    MobEffects.INFESTED,
                    MobEffects.UNLUCK
            )));

    public static final ItemDefinition<BandageItem> WARDING_BANDAGE = register("warding_bandage", () -> new BandageItem(new Properties()
            .stacksTo(16)
            .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1))))
    ));

    public static <T extends Item> ItemDefinition<T> registerWithoutTab(String name, Supplier<T> item, boolean customLang) {
        DeferredItem<T> deferred = ITEMS.register(name, item);
        ItemDefinition<T> definition = ItemDefinition.fromHolder(deferred, customLang);
        ITEM_DEFINITIONS.add(definition);
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> register(String name, Supplier<T> item, boolean customLang) {
        ItemDefinition<T> definition = registerWithoutTab(name, item, customLang);
        CREATIVE_TAB_ITEMS.add(definition);
        return definition;
    }

    public static <T extends Item> ItemDefinition<T> registerWithoutTab(String name, Supplier<T> item) {
        return registerWithoutTab(name, item, false);
    }

    public static <T extends Item> ItemDefinition<T> register(String name, Supplier<T> item) {
        return register(name, item, false);
    }
}