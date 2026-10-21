package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.model.BillhookBassModel;
import com.farcr.nomansland.client.model.BuddyModel;
import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.client.model.armor.AncientBronzeMaskModel;
import com.farcr.nomansland.client.model.armor.GasMaskModel;
import com.farcr.nomansland.client.model.deer.DeerModel;
import com.farcr.nomansland.client.model.frienderman.FriendermanModel;
import com.farcr.nomansland.client.model.goose.GooseModel;
import com.farcr.nomansland.client.model.living_pot.LivingPotModel;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseShellModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class NMLModelLayers {

    //Creatures
    public static final ModelLayerLocation LIVING_POT_LAYER = new ModelLayerLocation(NoMansLand.location("living_pot"), "main");
    public static final ModelLayerLocation BURIED_LAYER = new ModelLayerLocation(NoMansLand.location("buried"), "main");
    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("moose/maple"), "main");
    public static final ModelLayerLocation BASS_LAYER = new ModelLayerLocation(NoMansLand.location("bass"), "main");
    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(NoMansLand.location("deer"), "main");
    public static final ModelLayerLocation GOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("goose"), "main");
    public static final ModelLayerLocation TORTOISE_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise"), "main");
    public static final ModelLayerLocation BUDDY_LAYER = new ModelLayerLocation(NoMansLand.location("buddy"), "main");
    public static final ModelLayerLocation FRIENDERMAN_LAYER = new ModelLayerLocation(NoMansLand.location("frienderman"), "main");

    //Armor
    public static final ModelLayerLocation ANCIENT_BRONZE_MASK_LAYER = new ModelLayerLocation(NoMansLand.location("ancient_bronze_mask"), "main");
    public static final ModelLayerLocation TORTOISE_SHELL_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise_shell"), "main");
    public static final ModelLayerLocation GAS_MASK_LAYER = new ModelLayerLocation(NoMansLand.location("gas_mask"), "main");

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NMLModelLayers.LIVING_POT_LAYER, LivingPotModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BURIED_LAYER, BuriedModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.MOOSE_LAYER, MooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BASS_LAYER, BillhookBassModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.DEER_LAYER, DeerModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GOOSE_LAYER, GooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_LAYER, TortoiseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BUDDY_LAYER, BuddyModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.FRIENDERMAN_LAYER, FriendermanModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER, AncientBronzeMaskModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_SHELL_LAYER, TortoiseShellModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GAS_MASK_LAYER, GasMaskModel::createBodyLayer);
    }
}