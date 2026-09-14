package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.definitions.PotionDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.items.NMLPotions;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class NMLLanguageProvider extends LanguageProvider {

    public NMLLanguageProvider(PackOutput output) {
        super(output, NoMansLand.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            if (!definition.hasCustomLang()) {
                add(definition.langKey(), definition.langName());
            }
        }

        for (ItemDefinition<?> definition : NMLItems.ITEM_DEFINITIONS) {
            if (!definition.hasCustomLang() && !definition.isBlockItem()) {
                add(definition.langKey(), definition.langName());
            }
        }

        for (PotionDefinition definition : NMLPotions.POTION_DEFINITIONS) {
            if (!definition.hasCustomLang()) {
                add("item.nomansland.bandage.effect." + definition.trimmedLangKey(),
                    definition.effectLanguageValue() + " Bandage");
                add("item.minecraft.potion.effect." + definition.trimmedLangKey(),
                    "Potion of " + definition.effectLanguageValue());
                add("item.minecraft.lingering_potion.effect." + definition.trimmedLangKey(),
                    "Lingering Potion of " + definition.effectLanguageValue());
                add("item.minecraft.splash_potion.effect." + definition.trimmedLangKey(),
                    "Splash Potion of " + definition.effectLanguageValue());
                add("item.minecraft.tipped_arrow.effect." + definition.trimmedLangKey(),
                    "Arrow of " + definition.effectLanguageValue());
            }
        }

        for (ResourceKey<Biome> biome : NMLBiomes.BIOMES) {
            add(NMLBiomes.langKey(biome), NMLBiomes.langName(biome));
        }

        add("itemGroup.nomansland", "No Man's Land");
        add("itemGroup.nomansland.ancient_pots", "Ancient Pots");

        add("nomansland.subtitle.block.spike_trap.extend", "Spikes extend");
        add("nomansland.subtitle.block.spike_trap.retract", "Spikes retract");
        add("nomansland.subtitle.block.monster_anchor.monster_resurrection", "Monster begins resurrection");
        add("nomansland.subtitle.block.monster_anchor.monster_spawns", "Monster spawns");
        add("nomansland.subtitle.block.monster_anchor.activate", "Monster anchor activates");
        add("nomansland.subtitle.block.monster_anchor.deactivate", "Monster anchor deactivates");
        add("nomansland.subtitle.block.torch.extinguish", "Torch extinguishes");
        add("nomansland.subtitle.block.torch.light", "Torch lights");
        add("nomansland.subtitle.block.witch_stew_cauldron.ambient", "Witch Stew Cauldron gurgles");
        add("nomansland.subtitle.block.witch_stew_cauldron.clean", "Witch Stew Cauldron is cleaned");
        add("nomansland.subtitle.block.witch_stew_cauldron.empty", "Witch Stew pours into bowl");
        add("nomansland.subtitle.block.wooden_platform.crack", "Wooden Platform cracks");
        add("nomansland.subtitle.block.wooden_platform.break", "Wooden Platform breaks");
        add("nomansland.subtitle.entity.player.drink_milk", "Player drinks milk");
        add("nomansland.subtitle.entity.generic.sticky_cauldron_slide", "Sliding in a sticky cauldron");
        add("nomansland.subtitle.item.generic.consumed", "Item consumed");
        add("nomansland.subtitle.item.bomb.primed", "Bomb primed");
        add("nomansland.subtitle.item.ancestral_oath_sword.block", "Sword blocks");
        add("nomansland.subtitle.item.ancestral_oath_sword.parry", "Sword parries");
        add("nomansland.subtitle.item.ritual_pick.locate", "Ritual Pick resonates");
        add("nomansland.subtitle.item.ritual_pick.glint", "Block resonates");
        add("nomansland.subtitle.entity.billhook_bass.death", "Billhook Bass dies");
        add("nomansland.subtitle.entity.billhook_bass.flop", "Billhook Bass flops");
        add("nomansland.subtitle.entity.billhook_bass.hurt", "Billhook Bass hurts");
        add("nomansland.subtitle.entity.deer.death", "Deer dies");
        add("nomansland.subtitle.entity.deer.hurt", "Deer hurts");
        add("nomansland.subtitle.entity.deer.ambient", "Deer bleats");
        add("nomansland.subtitle.entity.deer.shed_antlers", "Deer sheds it's antlers");
        add("nomansland.subtitle.entity.moose.death", "Moose dies");
        add("nomansland.subtitle.entity.moose.hurt", "Moose hurts");
        add("nomansland.subtitle.entity.moose.ambient", "Moose breathes in deeply");
        add("nomansland.subtitle.entity.moose.eat", "Moose eats");
        add("nomansland.subtitle.entity.moose.reject_food", "Moose rejects food");
        add("nomansland.subtitle.entity.moose.reject_saddle", "Moose rejects saddle");
        add("nomansland.subtitle.entity.moose.swing", "Moose swings wildly");
        add("nomansland.subtitle.entity.moose.swing_blunder", "Moose tires aimlessly");
        add("nomansland.subtitle.entity.moose.swing_perfect", "Moose bashes marvelously");
        add("nomansland.subtitle.entity.moose.uppercut", "Moose deals damage");
        add("nomansland.subtitle.entity.moose.stomp", "Moose stomps in frustration");
        add("nomansland.subtitle.entity.moose.warn", "Moose huffs out air in frustration");
        add("nomansland.subtitle.entity.moose.shed_antlers", "Moose sheds it's antlers");
        add("nomansland.subtitle.entity.goose.death", "Goose dies");
        add("nomansland.subtitle.entity.goose.hurt", "Goose hurts");
        add("nomansland.subtitle.entity.goose.ambient", "Goose honks");
        add("nomansland.subtitle.entity.tortoise.ambient", "Tortoise chirps");
        add("nomansland.subtitle.entity.tortoise.hurt", "Tortoise hurts");
        add("nomansland.subtitle.entity.tortoise.hurt_baby", "Baby Tortoise hurts");
        add("nomansland.subtitle.entity.tortoise.death", "Tortoise dies");
        add("nomansland.subtitle.entity.tortoise.death_baby", "Baby Tortoise dies");
        add("nomansland.subtitle.entity.tortoise.swim", "Tortoise swims");
        add("nomansland.subtitle.entity.tortoise.shell_deflect", "Tortoise Shell blocks");
        add("nomansland.subtitle.entity.tortoise.lay_egg", "Tortoise lays egg");
        add("nomansland.subtitle.entity.tortoise.eat", "Burp");
        add("nomansland.subtitle.entity.explosive.throw", "Explosive thrown");
        add("nomansland.subtitle.entity.explosive.fused", "Explosive fused");
        add("nomansland.subtitle.item.armor.equip_tortoise", "Tortoise Shell thunks");
        add("nomansland.subtitle.item.armor.equip_ancient_bronze_mask", "Ancient Bronze Mask clinks");
        add("nomansland.subtitle.particle.droplet.fall", "Droplet falls");
        add("nomansland.subtitle.entity.living_urn.shatter", "Living Urn shatters");
        add("nomansland.subtitle.item.bandage.wrap", "Bandage wraps");
        add("nomansland.subtitle.block.inverted_bell.ring", "Inverted Bell rings");
        add("nomansland.subtitle.entity.buddy.ambient", "Buddy exhales");
        add("nomansland.subtitle.entity.buddy.hurt", "Buddy hurts");
        add("nomansland.subtitle.entity.buddy.death", "Buddy dies");
        add("nomansland.subtitle.entity.buddy.bone_break", "Buddy's bones snap");
        add("nomansland.subtitle.entity.friend_moon.speak", "Friend Moon speaks");
        add("nomansland.subtitle.entity.friend_moon.speak_sad", "Friend Moon speaks sadly");
        add("nomansland.subtitle.block.icicle.shatter", "Icicle shatters");
        add("nomansland.subtitle.block.tap.turns", "Tap turns");
        add("nomansland.subtitle.entity.living_pot.charge", "Living Pot charges");

        add("item.nomansland.billhook_bass_bucket", "Bucket of Billhook Bass");
        add("item.nomansland.billhook_bass", "Raw Billhook Bass");
        add("item.nomansland.no_mans_globe", "No Man's Globe");
        add("item.nomansland.maple_chest_boat", "Maple Boat with Chest");
        add("item.nomansland.pine_chest_boat", "Pine Boat with Chest");
        add("item.nomansland.walnut_chest_boat", "Walnut Boat with Chest");
        add("item.nomansland.willow_chest_boat", "Willow Boat with Chest");
        add("item.nomansland.maple_furnace_boat", "Maple Boat with Furnace");
        add("item.nomansland.pine_furnace_boat", "Pine Boat with Furnace");
        add("item.nomansland.walnut_furnace_boat", "Walnut Boat with Furnace");
        add("item.nomansland.willow_furnace_boat", "Willow Boat with Furnace");
        add("item.nomansland.music_disc_guidance", "Music Disc");
        add("jukebox_song.nomansland.guidance", "Samuel Organ - Guidance");
        add("entity.nomansland.buried", "Buried");
        add("entity.nomansland.cave_carp", "Cave Carp");
        add("entity.nomansland.billhook_bass", "Billhook Bass");
        add("entity.nomansland.deer", "Deer");
        add("entity.nomansland.moose", "Moose");
        add("entity.nomansland.tortoise", "Tortoise");
        add("entity.nomansland.goose", "Goose");
        add("entity.nomansland.living_pot", "Living Pot");
        add("entity.nomansland.frienderman", "Frienderman");
        add("entity.nomansland.fat_joint", "Fat Joint");
        add("entity.nomansland.buddy", "Buddy");
        add("entity.nomansland.incendiary_arrow", "Incendiary Arrow");
        add("entity.nomansland.ember", "Ember");
        add("entity.nomansland.ink_bomb", "Ink Bomb");
        add("entity.nomansland.lingering_cloud", "Lingering Cloud");
        add("entity.nomansland.pacified_cloud", "Pacified Cloud");
        add("entity.nomansland.living_urn", "Living Urn");
        add("fluid_type.nomansland.resin_oil", "Resin Oil");
        add("nomansland.tooltip.mask.regeneration", "Slowly regenerates health");

        add("effect.nomansland.pacified", "Pacified");
        add("entity.nomansland.ink_cloud", "Ink Cloud");
        add("painting.nomansland.sun.title", "Sun");
        add("painting.nomansland.sun.author", "Farcr");
        add("painting.nomansland.white_eyes.title", "White Eyes");
        add("painting.nomansland.white_eyes.author", "Probleyes");
        add("death.attack.nomansland.icicle_pierce", "%1$s was pierced by an icicle");
        add("death.attack.nomansland.icicle_pierce.player", "%1$s was pierced by an icicle while fighting %2$s");
        add("death.attack.nomansland.spike_fall", "%1$s fell for a spike trap");
        add("death.attack.nomansland.spike_fall.player", "%1$s fell for a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_impale", "%1$s was impaled in a spike trap");
        add("death.attack.nomansland.spike_impale.player", "%1$s was impaled in a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_poke", "%1$s got stuck in a spike trap");
        add("death.attack.nomansland.spike_poke.player", "%1$s got stuck in a spike trap while fighting %2$s");
        add("death.attack.nomansland.spike_skewer", "%1$s was skewered by a spike trap");
        add("death.attack.nomansland.spike_skewer.player", "%1$s was skewered by a spike trap while fighting %2$s");
        add("death.attack.nomansland.combust", "%1$s faced immolation");
        add("death.attack.nomansland.combust.player", "%1$s was immolated by %2$s");
        add("entity.nomansland.explosive", "Explosive");
        add("entity.nomansland.firebomb", "Firebomb");
        add("effect.nomansland.flammable", "Flammable");
        add("effect.nomansland.friendship", "Friendship");
        add("effect.nomansland.happiness", "Happiness");
        add("effect.nomansland.stasis", "Stasis");
        add("item.nomansland.bandage.with_effect", "%s Bandage");
        add("item.nomansland.bandage.effect.harming", "Harming Bandage");
        add("item.nomansland.bandage.effect.healing", "Healing Bandage");
        add("item.nomansland.bandage.effect.infested", "Infestation Bandage");
        add("item.nomansland.bandage.effect.leaping", "Leaping Bandage");
        add("item.nomansland.bandage.effect.swiftness", "Swiftness Bandage");
        add("item.nomansland.bandage.effect.turtle_master", "Turtle Master Bandage");
        add("item.nomansland.bandage.effect.wind_charged", "Wind Charging Bandage");
        add("nomansland.advancements.use_tap.title", "Tree Juice");
        add("nomansland.advancements.use_tap.description", "Collect Resin or Maple Syrup with a Tap and a Cauldron");
        add("nomansland.advancements.kill_anchored_mob.title", "Can't Even Die Right");
        add("nomansland.advancements.kill_anchored_mob.description", "Defeat an enemy under the influence of a Monster Anchor");
        add("nomansland.advancements.when_pigs_fly.title", "When Pigs Fly");
        add("nomansland.advancements.when_pigs_fly.description", "Ride a pig down a 40 block fall without it taking damage");
        add("nomansland.advancements.ignite_flammable_enemy.title", "Grossly Incandescent");
        add("nomansland.advancements.ignite_flammable_enemy.description", "Set an enemy on fire after dousing it in Resin Oil");
        add("nomansland.advancements.explode_ore.title", "Gets The Job Done");
        add("nomansland.advancements.explode_ore.description", "Mine Ores with an Explosive instead of a Pickaxe");
        add("nomansland.advancements.find_ancient_city.title", "Blue Hum");
        add("nomansland.advancements.find_ancient_city.description", "Find an Ancient City");
        add("nomansland.advancements.find_mineshaft.title", "To Blisters and Bedrock");
        add("nomansland.advancements.find_mineshaft.description", "Find a Mineshaft");
        add("nomansland.advancements.find_alchemist_ruins.title", "Old Stories");
        add("nomansland.advancements.find_alchemist_ruins.description", "Discover an ancestral ruin lost to time");
        add("nomansland.advancements.collect_ancient_tools.title", "Sticks and Rope");
        add("nomansland.advancements.collect_ancient_tools.description", "Collect every piece of ancient bronze equipment");
        add("nomansland.advancements.dream_friend_moon.title", "First Contact");
        add("nomansland.advancements.dream_friend_moon.description", "Dream of a cosmic presence");
        add("nomansland.advancements.meet_friend_moon.title", "Friendship");
        add("nomansland.advancements.meet_friend_moon.description", "Meet the Friend Moon");
        add("nomansland.advancements.buddy_ascension.title", "Friend of my Friend");
        add("nomansland.advancements.buddy_ascension.description", "Find some company for the Friend Moon");
        add("nomansland.filled_map.alchemist_ruins", "Ancestral Ruins Map");
        add("nomansland.filled_map.bell_sanctuary", "Ancestral Sanctuary Map");
        add("nomansland.filled_map.mineshaft", "Lost Miner's Map");
        add("block.nomansland.resin_cauldron", "Cauldron Filled with Resin");
        add("block.nomansland.honey_cauldron", "Cauldron Filled with Honey");
        add("block.nomansland.maple_syrup_cauldron", "Cauldron Filled with Maple Syrup");
        add("block.nomansland.resin_oil_cauldron", "Cauldron Filled with Resin Oil");
        add("block.nomansland.milk_cauldron", "Cauldron Filled with Milk");
        add("block.nomansland.inverted_bell.bad_teleport", "The bell's ringing leaves you battered...");
        add("block_type.nomansland.trimmed_planks", "Trimmed %s Planks");
        add("block_type.nomansland.bookshelf", "%s Bookshelf");
        add("design.nomansland.tortoise.sandy", "Sandy");
        add("design.nomansland.tortoise.green", "Green");
        add("design.nomansland.tortoise.gray", "Gray");
        add("design.nomansland.ancient_bronze_mask.alchemist", "Alchemist");
        add("design.nomansland.warp_worn.warp_worn", "Warp-worn");
        add("screen.nomansland.carving", "Scroll to select carving");

        add("item.nomansland.ancestral_oath_sword.refuse", "The sword refuses to violate its oath of pacifism...");
        add("nomansland.carving.hint", "Right Click to carve, Left Click to cancel");

        add("commands.nomansland.dream.start.pass", "Successfully started dream %s for %s.");
        add("commands.nomansland.dream.start.pass_count", "Successfully started dream %s for %s players.");
        add("commands.nomansland.dream.clear.fail", "Information about dream not found, nothing to clear.");
        add("commands.nomansland.dream.clear.pass", "Cleared information about dream %s for %s.");
        add("commands.nomansland.dream.clear.pass_count", "Cleared information about dream %s for %s players.");
    }
}
