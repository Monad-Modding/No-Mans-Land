package com.farcr.nomansland.common.dreams;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/*
* Generalized Class to allow for mutation & other things
* also for extensibility, since "has experienced dream" will probably
* turn into a list of dreams the player has experienced and information
* associated with them if necessary !!!
*
* TODO maybe move time to individual players? it doesn't really matter
 */
public class DreamStorage {
    public static DreamStorage fromCodec(
        ArrayList<DreamType> experiencedDreams,
        HashMap<DreamType, Integer> timeForDream
    ) {
        DreamStorage dreamInfo = new DreamStorage();
        dreamInfo.experiencedDreams = experiencedDreams;
        dreamInfo.timeForDream = timeForDream;
        return dreamInfo;
    }

    public static final Codec<DreamStorage> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(DreamType.CODEC).fieldOf("experiencedDreams").xmap(
                    ArrayList::new,
                list -> list
                )
                .forGetter(storage -> storage.experiencedDreams),
            Codec.unboundedMap(DreamType.CODEC, Codec.INT).xmap(
                    HashMap::new, map -> map
                ).fieldOf("dreamConditionTimes")
                .forGetter(storage -> storage.timeForDream)
        ).apply(instance, DreamStorage::fromCodec)
    );


    private ArrayList<DreamType> experiencedDreams = new ArrayList<>();
    private HashMap<DreamType, Integer> timeForDream = new HashMap<>();
    public HashMap<DreamType, Integer> getDreamTimes() { return timeForDream; }
    public void setTimeRemainingForDream(DreamType dreamType, int time) {
        timeForDream.put(dreamType, time);
    }

    public boolean removeInformationAboutDream(DreamType dreamType) {
        Optional<?> value = Optional.ofNullable(timeForDream.remove(dreamType));
        boolean removed = experiencedDreams.remove(dreamType);
        return (value.isPresent() || removed);
    }

    public boolean getHasExperiencedDream(DreamType dreamType) {
        return experiencedDreams.contains(dreamType);
    }

    public int getTimeRemainingForDream(DreamType dreamType) {
        return timeForDream.getOrDefault(dreamType, 0);
    }

    public void setDreamExperienced(DreamType dreamType) {
        if (!experiencedDreams.contains(dreamType)) experiencedDreams.add(dreamType);
        timeForDream.remove(dreamType);
    }
}
