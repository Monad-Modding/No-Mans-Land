package com.farcr.nomansland.datagen.sound;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.datagen.sound.lodestone.LodestoneBlockSoundEventSystem;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static net.neoforged.neoforge.common.data.SoundDefinition.SoundType.EVENT;


public class NMLSoundProvider extends LodestoneBlockSoundEventSystem {

    public NMLSoundProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, NoMansLand.MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        //Blocks
        add(NMLSounds.MONSTER_ANCHOR, "block/monster_anchor", b -> b.setStepHitFallSoundPaths("minecraft:block/spawner"));
        add(NMLSounds.MUSHROOM_CAP, "block/mushroom_cap", b -> b.modifyBreakPlaceSounds(se -> se.volume(0.7f)).modifyStepHitFallSounds(se -> se.volume(0.8f)));
        add(NMLSounds.SEASHELLS, "block/seashells", b -> b.modifyBreakPlaceSounds(se -> se.volume(0.6f)));
        add(NMLSounds.SCONCE_TORCH, "block/sconce_torch", b -> b
                .modifyBreakPlaceSounds(s -> s.pitch(1.1f).volume(0.6f))
                .setStepSoundPath("minecraft:block/lantern").setStepSoundName("break")
                .setHitSoundPath("minecraft:step").setHitSoundName("wood")
                .setFallSoundPath("minecraft:block/lantern").setFallSoundName("break")
                //Evil Block... Hoards ALL the hyper-specific sound preferences
        );

        add(NMLSounds.WOODEN_SCAFFOLDING, "block/wooden_scaffold", b -> b
                .setStepHitFallSoundPaths("minecraft:step")
                .setStepHitFallSoundNames("scaffold")
                .modifyStepHitFallSounds(se -> se.pitch(0.85f)));
        add(NMLSounds.EARTHEN_TILES, "block/tiles/earthen");
        add(NMLSounds.DROSS_TILES, "block/tiles/dross", b -> b.modifySounds(se -> se.pitch(0.8f)));
        add(NMLSounds.SILTSTONE, "block/siltstone");
        add(NMLSounds.QUARTZITE, "block/quartzite");
        add(NMLSounds.QUARTZITE_CLUSTER, "block/quartzite/cluster", b -> b.setStepHitFallSoundPaths("block/quartzite"));
        add(NMLSounds.THATCH, "block/thatch", b -> b.setStepHitFallSoundPaths("minecraft:block/moss"));

        //Block Related
        add(NMLSounds.MONSTER_ANCHOR_ACTIVATE, s -> s.with(allSounds("block/monster_anchor/activate")));
        add(NMLSounds.MONSTER_ANCHOR_DEACTIVATE, s -> s.with(allSounds("block/monster_anchor/deactivate")));
        add(NMLSounds.MONSTER_ANCHOR_RESURRECTION, s -> s.with(allSounds("block/monster_anchor/resurrect")));
        add(NMLSounds.MONSTER_ANCHOR_SPAWN, s -> s.with(allSounds("block/monster_anchor/spawn")));


        add(NMLSounds.SPIKE_TRAP_EXTEND, s -> s
                .with(allSounds("block/spike_trap/extend", se -> se.pitch(0.95f)))
                .with(allSounds("block/spike_trap/extend", se -> se.pitch(1.05f)))
        );
        add(NMLSounds.SPIKE_TRAP_RETRACT, s -> s
                .with(allSounds("block/spike_trap/retract", se -> se.pitch(0.95f)))
                .with(allSounds("block/spike_trap/retract", se -> se.pitch(1.05f)))
        );

        add(NMLSounds.TORCH_EXTINGUISH, s -> s.with(sound("minecraft:block.candle.extinguish", EVENT).pitch(0.85f).volume(1.75f)));
        add(NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL, s -> s.subtitle("nomansland.subtitle.block.torch.light").with(allSounds("block/torch/light_by_flint_and_steel", se -> se.pitch(1.1f).volume(0.6f))));
        add(NMLSounds.TORCH_LIGHT, s -> s.with(allSounds("block/torch/light", se -> se.pitch(1.1f).volume(0.6f))));

        add(NMLSounds.WITCH_STEW_CAULDRON_AMBIENT, s -> s.with(allSounds("block/witch_stew_cauldron/ambient", se -> se.pitch(0.8f))));
        add(NMLSounds.WITCH_STEW_CAULDRON_EMPTY, s -> s.with(allSounds("block/witch_stew_cauldron/empty", se -> se.pitch(0.8f).volume(1.2f))));
        add(NMLSounds.WITCH_STEW_CAULDRON_CLEAN, s -> s.with(allSounds("block/witch_stew_cauldron/clean")));

        add(NMLSounds.WOODEN_PLATFORM_CRACKS, s -> s.with(sound("minecraft:item.axe.strip", EVENT)));
        add(NMLSounds.WOODEN_PLATFORM_BREAKS, s -> s.with(sound("minecraft:mob/zombie/woodbreak").volume(0.5f)));

        add(NMLSounds.INVERTED_BELL_RING, s -> s.subtitle(null).with(allSounds("block/inverted_bell/ring")));
        add(NMLSounds.INVERTED_BELL_BLACKOUT, s -> s.subtitle(null).with(allSounds("block/inverted_bell/blackout")));

        add(NMLSounds.MOON_CARVING_ACTIVATE, s -> s.with(sound("minecraft:ambient/cave/cave1").volume(0.75f)).subtitle("subtitles.ambient.cave"));

        add(NMLSounds.CRUDE_DOOR_OPEN, s -> s.subtitle("subtitles.block.door.toggle").with(allSounds("block/crude_door/open")));
        add(NMLSounds.CRUDE_DOOR_CLOSE, s -> s.subtitle("subtitles.block.door.toggle").with(allSounds("block/crude_door/close")));
        add(NMLSounds.CRUDE_TRAPDOOR_OPEN, s -> s.subtitle("subtitles.block.trapdoor.toggle").with(allSounds("block/crude_door/open")));
        add(NMLSounds.CRUDE_TRAPDOOR_CLOSE, s -> s.subtitle("subtitles.block.trapdoor.toggle").with(allSounds("block/crude_door/close")));

        add(NMLSounds.ICICLE_SHATTER, s -> s.subtitle("nomansland.subtitle.block.icicle.shatter").with(sound("block.glass.break", EVENT)));
        add(NMLSounds.TAP_TURNS, s -> s.subtitle("nomansland.subtitle.block.tap.turns").with(sound("block/tap/turns")));

        //Items
        add(NMLSounds.BANDAGE_WRAP, s -> s.with(sound("item/bandage/wrap")));
        add(NMLSounds.OATH_BLOCK, s -> s.with(allSounds("item/ancestral_oath_sword/block")));
        add(NMLSounds.OATH_PARRY, s -> s.with(allSounds("item/ancestral_oath_sword/parry")));
        add(NMLSounds.RITUAL_PICK_LOCATE, s -> s.with(sound("item/ritual_pick/ritual_pick_locate")));
        add(NMLSounds.RITUAL_PICK_GLINT, s -> s.with(sound("item/ritual_pick/glint")));
        add(NMLSounds.BOMB_PRIMED, s -> s.with(sound("minecraft:item/crossbow/loading_end"), sound("minecraft:item/crossbow/loading_end").pitch(1.3f)));

        //Cauldron Interactions
        add(NMLSounds.HONEYCOMB_CONSUMED, s -> s.subtitle("nomansland.subtitle.item.generic.consumed").with(allSounds("minecraft:item/honeycomb/wax_on")));
        add(NMLSounds.RESIN_CONSUMED, s -> s.subtitle("nomansland.subtitle.item.generic.consumed").with(allSounds("minecraft:block/honeyblock/break")));

        add(NMLSounds.BASS_DEATH, s -> s.with(
                allSounds("minecraft:entity/fish/hurt", se -> se.pitch(0.8f))
        ));
        add(NMLSounds.BASS_FLOP, s -> s.with(
                allSounds("minecraft:entity/fish/flop", se -> se.pitch(0.8f).volume(0.3f))
        ));
        add(NMLSounds.BASS_HURT, s -> s.with(
                allSounds("minecraft:entity/fish/hurt", se -> se.pitch(0.8f))
        ));

        add(NMLSounds.DEER_AMBIENT, s -> s.with(allSounds("minecraft:mob/fox/idle", se -> se.pitch(0.4f))));
        add(NMLSounds.DEER_DEATH, s -> s.with(allSounds("minecraft:mob/fox/death", se -> se.pitch(0.4f).volume(0.9f))));
        add(NMLSounds.DEER_HURT, s -> s.with(allSounds("minecraft:mob/fox/hurt", se -> se.pitch(0.4f).volume(0.75f))));
        add(NMLSounds.DEER_STEP, s -> s.with(allSounds("minecraft:mob/cow/step", se -> se.pitch(1.5f))).subtitle("subtitles.block.generic.footsteps"));
        add(NMLSounds.DEER_SHED_ANTLERS, s -> s.with(allSounds("minecraft:item/axe/strip", se -> se.volume(0.9f))));

        add(NMLSounds.MOOSE_AMBIENT, s -> s.with(allSounds("minecraft:mob/ravager/idle", se -> se.pitch(0.8f).volume(0.6f))));
        add(NMLSounds.MOOSE_DEATH, s -> s.with(allSounds("minecraft:mob/ravager/death", se -> se.pitch(1.4f).volume(0.9f))));
        add(NMLSounds.MOOSE_HURT, s -> s.with(allSounds("minecraft:mob/ravager/hurt", se -> se.pitch(1.4f).volume(0.75f))));
        add(NMLSounds.MOOSE_STEP, s -> s.with(allSounds("minecraft:mob/cow/step", se -> se.pitch(0.5f))).subtitle("subtitles.block.generic.footsteps"));
        add(NMLSounds.MOOSE_EAT, s -> s.with(allSounds("minecraft:entity/horse/eat", se -> se.pitch(0.85f))));
        add(NMLSounds.MOOSE_REJECTS_FOOD, s -> s.with(allSounds("minecraft:mob/horse/breathe", se -> se.pitch(0.85f))));
        add(NMLSounds.MOOSE_REJECTS_SADDLE, s -> s
                .with(sound("minecraft:mob/horse/donkey/idle1").pitch(0.7f), sound("minecraft:mob/horse/donkey/idle1").pitch(0.6f))
                .with(allSounds("minecraft:mob/horse/breathe", se -> se.pitch(0.85f))
        ));
        add(NMLSounds.MOOSE_SWINGS, s -> s.with(allSounds("entity/moose/swing")));
        add(NMLSounds.MOOSE_SWINGS_BLUNDER, s -> s.with(allSounds("entity/moose/swing", se -> se.pitch(0.5f))));
        add(NMLSounds.MOOSE_SWINGS_PERFECT, s -> s.with(allSounds("entity/moose/swing_perfect")));
        add(NMLSounds.MOOSE_HITS_TARGET, s -> s.subtitle(null).with(sound("entity/moose/hit")));
        add(NMLSounds.MOOSE_STOMPS, s -> s.with(allSounds("entity/moose/stomp", se -> se.pitch(0.5f))));
        add(NMLSounds.MOOSE_SHOWS_WARNING, s -> s.with(allSounds("entity/moose/warn")));
        add(NMLSounds.MOOSE_SHEDS_ANTLERS, s -> s.with(allSounds("minecraft:item/axe/strip", se -> se.volume(0.9f))));

        add(NMLSounds.STICKY_CAULDRON_SLIDE, s -> s.with(allSounds("minecraft:block/honeyblock/slide", se -> se.volume(0.8f))));

        add(NMLSounds.GOOSE_AMBIENT, s -> s.with(allSounds("entity/goose/idle", se -> se.volume(0.8f))));
        add(NMLSounds.GOOSE_DEATH, s -> s.with(allSounds("entity/goose/death")));
        add(NMLSounds.GOOSE_HURT, s -> s.with(allSounds("entity/goose/hurt", se -> se.volume(0.8f))));
        add(NMLSounds.GOOSE_STEP, s -> s.with(allSounds("minecraft:mob/chicken/step", se -> se.pitch(0.75f))).subtitle("subtitles.block.generic.footsteps"));

        //Tortoise
        add(NMLSounds.TORTOISE_AMBIENT, s -> s.with(sound("minecraft:entity.turtle.ambient_land", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_HURT, s -> s.with(sound("minecraft:entity.turtle.hurt", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_HURT_BABY, s -> s.with(sound("minecraft:entity.turtle.hurt_baby", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_DEATH, s -> s.with(sound("minecraft:entity.turtle.death", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_DEATH_BABY, s -> s.with(sound("minecraft:entity.turtle.death_baby", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_SWIM, s -> s.with(sound("minecraft:entity.turtle.swim", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_SHELL_DEFLECT, s -> s.with(sound("minecraft:item.shield.block", EVENT).pitch(0.2f)));
        add(NMLSounds.TORTOISE_LAY_EGG, s -> s.with(sound("minecraft:entity.turtle.lay_egg", EVENT).pitch(0.8f)));
        add(NMLSounds.TORTOISE_EAT, s -> s.with(sound("minecraft:entity.player.burp", EVENT).pitch(0.8f)));

        //Explosives
        add(NMLSounds.BOMB_THROW, s -> s.with(sound("minecraft:entity.splash_potion.throw", EVENT)));
        add(NMLSounds.BOMB_FUSED, s -> s.with(sound("minecraft:entity.tnt.primed", EVENT)));

        //Armor Equip (placeholder: vanilla equip sounds until unique audio lands)
        add(NMLSounds.TORTOISE_ARMOR_EQUIP, s -> s.with(sound("minecraft:item.armor.equip_turtle", EVENT)));
        add(NMLSounds.ANCIENT_BRONZE_MASK_EQUIP, s -> s.with(sound("minecraft:item.armor.equip_gold", EVENT)));

        //Player
        add(NMLSounds.PLAYER_DRINK_MILK, s -> s.with(allSounds("minecraft:mob/wandering_trader/drink_milk")));
        add(NMLSounds.PLAYER_HURT_SPIKE_TRAP, s -> s.subtitle(null).with(allSounds("entity/player/hurt_spike_trap")));

        //Buddy
        add(NMLSounds.BUDDY_AMBIENT, s -> s.with(allSounds("entity/buddy/idle", se -> se.volume(0.75f))));
        add(NMLSounds.BUDDY_DEATH, s -> s.with(allSounds("entity/buddy/death")));
        add(NMLSounds.BUDDY_HURT, s -> s.with(allSounds("entity/buddy/hurt", se -> se.volume(0.9f))));
        add(NMLSounds.BUDDY_BONE_BREAK, s -> s.with(allSounds("entity/buddy/bone_break")));

        //Living Urn
        add(NMLSounds.LIVING_URN_SHATTERS, s -> s.with(allSounds("minecraft:block/decorated_pot/shatter")));

        //Living Pot
        add(NMLSounds.LIVING_POT_CHARGE, s -> s.subtitle("nomansland.subtitle.entity.living_pot.charge").with(sound("entity.iron_golem.attack", EVENT)));

        //Friend Moon
        add(NMLSounds.FRIEND_MOON_SPEAK, s -> s.with(allSounds("entity/friend_moon/speak")));
        add(NMLSounds.FRIEND_MOON_SPEAK_BGR, s -> s.subtitle(null).with(allSounds("entity/friend_moon/speak_bgr")));
        add(NMLSounds.FRIEND_MOON_SPEAK_SAD, s -> s.with(allSounds("entity/friend_moon/speak_sad")));
        add(NMLSounds.FRIEND_MOON_SPEAK_AMBIENT_LOOP, s -> s.with(sound("entity/friend_moon/speak_ambient_loop").stream()).subtitle(null));
        add(NMLSounds.FRIEND_MOON_OFFERING_LOOP, s -> s.with(sound("entity/friend_moon/offering_loop").stream()).subtitle(null));

        //Music
        add(NMLSounds.CAVE_MUSIC, s -> s.subtitle(null).with(
                sound("nomansland:music/drifter").stream(),
                sound("minecraft:music/game/deeper").stream().volume(0.4f),
                sound("minecraft:music/game/oxygene").stream(),
                sound("minecraft:music/game/key").stream(),
                sound("minecraft:music/game/an_ordinary_day").stream().volume(0.4f),
                sound("minecraft:music/game/minecraft").stream(),
                sound("minecraft:music/game/mice_on_venus").stream(),
                sound("minecraft:music/game/dry_hands").stream(),
                sound("minecraft:music/game/one_more_day").stream().volume(0.4f),
                sound("minecraft:music/game/swamp/firebugs").stream().volume(0.4f),
                sound("minecraft:music/game/floating_dream").stream().volume(0.4f),
                sound("minecraft:music/game/watcher").stream().volume(0.4f),
                sound("minecraft:music/game/puzzlebox").stream().volume(0.4f),
                sound("minecraft:music/game/pokopoko").stream().volume(0.4f),
                sound("minecraft:music/game/yakusoku").stream().volume(0.4f),
                sound("minecraft:music/game/eld_unknown").stream().volume(0.4f),
                sound("minecraft:music/game/endless").stream().volume(0.4f)
        ));

        add(NMLSounds.CAVE_DEPTH_MUSIC, s -> s.subtitle(null).with(
                sound("nomansland:music/drifter").weight(15).stream(),
                sound("minecraft:music/game/oxygene").weight(5).stream(),
                sound("minecraft:music/game/key").weight(5).stream(),
                sound("minecraft:music/game/minecraft").weight(3).stream(),
                sound("minecraft:music/game/mice_on_venus").weight(5).stream(),
                sound("minecraft:music/game/dry_hands").weight(5).stream(),
                sound("minecraft:music/game/yakusoku").weight(3).stream().volume(0.4f),
                sound("minecraft:music/game/endless").weight(3).stream().volume(0.4f)
        ));

        add(NMLSounds.ALCHEMIST_RUINS_MUSIC, s -> s.subtitle(null).with(
                sound("nomansland:music/nomad").weight(25).stream(),
                sound("minecraft:music/game/oxygene").weight(5).stream(),
                sound("minecraft:music/game/key").weight(5).stream(),
                sound("minecraft:music/game/minecraft").weight(3).stream(),
                sound("minecraft:music/game/mice_on_venus").weight(3).stream(),
                sound("minecraft:music/game/dry_hands").weight(3).stream()
        ));

        add(NMLSounds.MUSHROOM_BIOME_MUSIC, s -> s.subtitle(null).with(
                sound("nomansland:music/anachronistic").weight(15).stream().volume(0.7f),
                sound("minecraft:music/game/puzzlebox").weight(3).stream().volume(0.4f),
                sound("minecraft:music/game/pokopoko").weight(3).stream().volume(0.4f),
                sound("minecraft:music/game/endless").weight(5).stream().volume(0.4f),
                sound("minecraft:music/game/swamp/firebugs").weight(7).stream().volume(0.4f)
        ));

        add(NMLSounds.ANACHRONISTIC_MUSIC, s -> s.subtitle(null).with(sound("music/anachronistic").stream()));

        add(NMLSounds.MUSIC_DISC_GUIDANCE, s -> s.subtitle(null).with(sound("records/guidance").stream()));

        //Misc
        add(NMLSounds.DROPLET_FALLS, s -> s.with(allSounds("minecraft:block/beehive/drip", se -> se.attenuationDistance(8).volume(0.3f))));
    }
}