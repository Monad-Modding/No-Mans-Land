package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, NoMansLand.MODID);


    //Blocks
    public static final DeferredSoundType MONSTER_ANCHOR = registerSoundType("monster_anchor", 1, 1.2F);
    public static final DeferredSoundType MUSHROOM_CAP = registerSoundType("mushroom_cap", 1, 1.2F);
    public static final DeferredSoundType SEASHELLS = registerSoundType("seashells", 1, 1.2F);
    public static final DeferredSoundType SCONCE_TORCH = registerSoundType("sconce_torch", 1, 1.2F);

    public static final DeferredSoundType WOODEN_SCAFFOLDING = registerSoundType("wooden_scaffolding", 1, 1f);
    public static final DeferredSoundType EARTHEN_TILES = registerSoundType("earthen_tiles", 1, 1.25f);
    public static final DeferredSoundType DROSS_TILES = registerSoundType("dross_tiles", 1, 1f);
    public static final DeferredSoundType SILTSTONE = registerSoundType("siltstone", 1, 1f);
    public static final DeferredSoundType QUARTZITE = registerSoundType("quartzite", 1, 1f);
    public static final DeferredSoundType QUARTZITE_CLUSTER = registerSoundType("quartzite_cluster", 1, 1f);
    public static final DeferredSoundType THATCH = registerSoundType("thatch", 1, 1f);

    //Block-Related
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_ANCHOR_ACTIVATE = registerSound("block.monster_anchor.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_ANCHOR_DEACTIVATE = registerSound("block.monster_anchor.deactivate");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_ANCHOR_RESURRECTION = registerSound("block.monster_anchor.monster_resurrection");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_ANCHOR_SPAWN = registerSound("block.monster_anchor.monster_spawns");

    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_TRAP_EXTEND = registerSound("block.spike_trap.extend");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_TRAP_RETRACT = registerSound("block.spike_trap.retract");

    public static final DeferredHolder<SoundEvent, SoundEvent> TORCH_EXTINGUISH = registerSound("block.torch.extinguish");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORCH_LIGHT_BY_FLINT_AND_STEEL = registerSound("block.torch.light_by_flint_and_steel");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORCH_LIGHT = registerSound("block.torch.light");

    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_STEW_CAULDRON_AMBIENT = registerSound("block.witch_stew_cauldron.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_STEW_CAULDRON_EMPTY = registerSound("block.witch_stew_cauldron.empty");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_STEW_CAULDRON_CLEAN = registerSound("block.witch_stew_cauldron.clean");

    public static final DeferredHolder<SoundEvent, SoundEvent> WOODEN_PLATFORM_CRACKS = registerSound("block.wooden_platform.crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WOODEN_PLATFORM_BREAKS = registerSound("block.wooden_platform.break");

    public static final DeferredHolder<SoundEvent, SoundEvent> INVERTED_BELL_RING = registerSound("block.inverted_bell.ring");
    public static final DeferredHolder<SoundEvent, SoundEvent> INVERTED_BELL_BLACKOUT = registerSound("block.inverted_bell.blackout");

    public static final DeferredHolder<SoundEvent, SoundEvent> MOON_CARVING_ACTIVATE = registerSound("block.moon_carving.activate");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRUDE_DOOR_OPEN = registerSound("block.crude_door.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUDE_DOOR_CLOSE = registerSound("block.crude_door.close");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUDE_TRAPDOOR_OPEN = registerSound("block.crude_trapdoor.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUDE_TRAPDOOR_CLOSE = registerSound("block.crude_trapdoor.close");

    public static final DeferredHolder<SoundEvent, SoundEvent> ICICLE_SHATTER = registerSound("block.icicle.shatter");

    public static final DeferredHolder<SoundEvent, SoundEvent> TAP_TURNS = registerSound("block.tap.turns");

    //Items
    public static final DeferredHolder<SoundEvent, SoundEvent> BANDAGE_WRAP = registerSound("item.bandage.wrap");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_PRIMED = registerSound("item.bomb.primed");
    public static final DeferredHolder<SoundEvent, SoundEvent> OATH_BLOCK = registerSound("item.ancestral_oath_sword.block");
    public static final DeferredHolder<SoundEvent, SoundEvent> OATH_PARRY = registerSound("item.ancestral_oath_sword.parry");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_PICK_LOCATE = registerSound("item.ritual_pick.locate");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_PICK_GLINT = registerSound("item.ritual_pick.glint");

    //Cauldron Interactions
    public static final DeferredHolder<SoundEvent, SoundEvent> HONEYCOMB_CONSUMED = registerSound("item.honeycomb.consumed");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_CONSUMED = registerSound("item.resin.consumed");
    public static final DeferredHolder<SoundEvent, SoundEvent> STICKY_CAULDRON_SLIDE = registerSound("entity.generic.sticky_cauldron_slide");

    //Bass
    public static final DeferredHolder<SoundEvent, SoundEvent> BASS_DEATH = registerSound("entity.billhook_bass.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASS_FLOP = registerSound("entity.billhook_bass.flop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASS_HURT = registerSound("entity.billhook_bass.hurt");

    //Deer
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT = registerSound("entity.deer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_DEATH = registerSound("entity.deer.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT = registerSound("entity.deer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_STEP = registerSound("entity.deer.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_SHED_ANTLERS = registerSound("entity.deer.shed_antlers");

    //Moose
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_AMBIENT = registerSound("entity.moose.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_DEATH = registerSound("entity.moose.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_HURT = registerSound("entity.moose.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_STEP = registerSound("entity.moose.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_EAT = registerSound("entity.moose.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_REJECTS_FOOD = registerSound("entity.moose.reject_food");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_REJECTS_SADDLE = registerSound("entity.moose.reject_saddle");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_SWINGS = registerSound("entity.moose.swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_SWINGS_BLUNDER = registerSound("entity.moose.swing_blunder");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_SWINGS_PERFECT = registerSound("entity.moose.swing_perfect");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_HITS_TARGET = registerSound("entity.moose.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_STOMPS = registerSound("entity.moose.stomp");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_SHOWS_WARNING = registerSound("entity.moose.warn");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOOSE_SHEDS_ANTLERS = registerSound("entity.moose.shed_antlers");

    //Goose
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_AMBIENT = registerSound("entity.goose.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_DEATH = registerSound("entity.goose.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_HURT = registerSound("entity.goose.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_STEP = registerSound("entity.goose.step");

    //Tortoise
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_AMBIENT = registerSound("entity.tortoise.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_HURT = registerSound("entity.tortoise.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_HURT_BABY = registerSound("entity.tortoise.hurt_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_DEATH = registerSound("entity.tortoise.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_DEATH_BABY = registerSound("entity.tortoise.death_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_SWIM = registerSound("entity.tortoise.swim");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_SHELL_DEFLECT = registerSound("entity.tortoise.shell_deflect");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_LAY_EGG = registerSound("entity.tortoise.lay_egg");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_EAT = registerSound("entity.tortoise.eat");

    //Explosives
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_THROW = registerSound("entity.bomb.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_FUSED = registerSound("entity.bomb.fused");

    //Armor Equip
    public static final DeferredHolder<SoundEvent, SoundEvent> TORTOISE_ARMOR_EQUIP = registerSound("item.armor.equip_tortoise");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_BRONZE_MASK_EQUIP = registerSound("item.armor.equip_ancient_bronze_mask");

    //Player
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_DRINK_MILK = registerSound("entity.player.drink_milk");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_HURT_SPIKE_TRAP = registerSound("entity.player.hurt_spike_trap");

    //Buddy
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDDY_AMBIENT = registerSound("entity.buddy.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDDY_DEATH = registerSound("entity.buddy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDDY_HURT = registerSound("entity.buddy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDDY_BONE_BREAK = registerSound("entity.buddy.bone_break");

    //Living Urn
    public static final DeferredHolder<SoundEvent, SoundEvent> LIVING_URN_SHATTERS = registerSound("entity.living_urn.shatter");

    //Living Pot
    public static final DeferredHolder<SoundEvent, SoundEvent> LIVING_POT_CHARGE = registerSound("entity.living_pot.charge");

    //Friend Moon
    public static final DeferredHolder<SoundEvent, SoundEvent> FRIEND_MOON_SPEAK = registerSound("entity.friend_moon.speak");
    public static final DeferredHolder<SoundEvent, SoundEvent> FRIEND_MOON_SPEAK_BGR = registerSound("entity.friend_moon.speak_bgr");
    public static final DeferredHolder<SoundEvent, SoundEvent> FRIEND_MOON_SPEAK_SAD = registerSound("entity.friend_moon.speak_sad");
    public static final DeferredHolder<SoundEvent, SoundEvent> FRIEND_MOON_SPEAK_AMBIENT_LOOP = registerSound("entity.friend_moon.speak_ambient_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> FRIEND_MOON_OFFERING_LOOP = registerSound("entity.friend_moon.offering_loop");

    //Music
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVE_MUSIC = registerSound("music.overworld.caves");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVE_DEPTH_MUSIC = registerSound("music.overworld.cave_depths");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_RUINS_MUSIC = registerSound("music.overworld.alchemist_ruins");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSHROOM_BIOME_MUSIC = registerSound("music.overworld.mushroom_biome");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANACHRONISTIC_MUSIC = registerSound("music.overworld.anachronistic");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GUIDANCE = registerSound("music_disc.guidance");

    //Misc
    public static final DeferredHolder<SoundEvent, SoundEvent> DROPLET_FALLS = registerSound("particle.droplet.fall");


    protected static DeferredSoundType registerSoundType(String name, float volume, float pitch) {
        return new DeferredSoundType(
                volume,
                pitch,
                registerSound("block." + name + "." + "break"),
                registerSound("block." + name + "." + "step"),
                registerSound("block." + name + "." + "place"),
                registerSound("block." + name + "." + "hit"),
                registerSound("block." + name + "." + "fall")
        );
    }

    protected static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(NoMansLand.location(name)));
    }
}
