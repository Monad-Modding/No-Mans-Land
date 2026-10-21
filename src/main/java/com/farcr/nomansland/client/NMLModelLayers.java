package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.model.BillhookBassModel;
import com.farcr.nomansland.client.model.BuddyModel;
import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.client.model.cave_carp.CaveCarpModel;
import com.farcr.nomansland.client.model.armor.AncientBronzeMaskModel;
import com.farcr.nomansland.client.model.armor.GasMaskModel;
import com.farcr.nomansland.client.model.beetle.BeetleModel;
import com.farcr.nomansland.client.model.centipede.CentipedeHeadModel;
import com.farcr.nomansland.client.model.centipede.CentipedeSegmentModel;
import com.farcr.nomansland.client.model.centipede.CentipedeTailModel;
import com.farcr.nomansland.client.model.beetle.DungBallModel;
import com.farcr.nomansland.client.model.beetle.GrubModel;
import com.farcr.nomansland.client.model.clod.ClodModel;
import com.farcr.nomansland.client.model.deer.DeerModel;
import com.farcr.nomansland.client.model.frienderman.FriendermanModel;
import com.farcr.nomansland.client.model.goose.GooseModel;
import com.farcr.nomansland.client.model.living_pot.LivingPotModel;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.client.model.remnant.RemnantModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseShellModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class NMLModelLayers {

    //Creatures
    public static final ModelLayerLocation LIVING_POT_LAYER = new ModelLayerLocation(NoMansLand.location("living_pot"), "main");
    public static final ModelLayerLocation BURIED_LAYER = new ModelLayerLocation(NoMansLand.location("buried"), "main");
    public static final ModelLayerLocation BURIED_INNER_ARMOR = new ModelLayerLocation(NoMansLand.location("buried"), "inner_armor");
    public static final ModelLayerLocation BURIED_OUTER_ARMOR = new ModelLayerLocation(NoMansLand.location("buried"), "outer_armor");
    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("moose/maple"), "main");
    public static final ModelLayerLocation BASS_LAYER = new ModelLayerLocation(NoMansLand.location("bass"), "main");
    public static final ModelLayerLocation CAVE_CARP_LAYER = new ModelLayerLocation(NoMansLand.location("cave_carp"), "main");
    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(NoMansLand.location("deer"), "main");
    public static final ModelLayerLocation GOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("goose"), "main");
    public static final ModelLayerLocation CLOD_LAYER = new ModelLayerLocation(NoMansLand.location("clod"), "main");
    public static final ModelLayerLocation TORTOISE_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise"), "main");
    public static final ModelLayerLocation BUDDY_LAYER = new ModelLayerLocation(NoMansLand.location("buddy"), "main");
    public static final ModelLayerLocation FRIENDERMAN_LAYER = new ModelLayerLocation(NoMansLand.location("frienderman"), "main");
    public static final ModelLayerLocation CENTIPEDE_HEAD = new ModelLayerLocation(NoMansLand.location("centipede"), "head");
    public static final ModelLayerLocation CENTIPEDE_FRONT = new ModelLayerLocation(NoMansLand.location("centipede"), "front");
    public static final ModelLayerLocation CENTIPEDE_MIDDLE = new ModelLayerLocation(NoMansLand.location("centipede"), "middle");
    public static final ModelLayerLocation CENTIPEDE_TAIL = new ModelLayerLocation(NoMansLand.location("centipede"), "tail");
    public static final ModelLayerLocation REMNANT_LAYER = new ModelLayerLocation(NoMansLand.location("remnant"), "main");
    public static final ModelLayerLocation BEETLE_LAYER = new ModelLayerLocation(NoMansLand.location("beetle"), "main");
    public static final ModelLayerLocation GRUB_LAYER = new ModelLayerLocation(NoMansLand.location("grub"), "main");
    public static final ModelLayerLocation DUNG_BALL_LAYER = new ModelLayerLocation(NoMansLand.location("dung_ball"), "main");

    //Armor
    public static final ModelLayerLocation ANCIENT_BRONZE_MASK_LAYER = new ModelLayerLocation(NoMansLand.location("ancient_bronze_mask"), "main");
    public static final ModelLayerLocation TORTOISE_SHELL_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise_shell"), "main");
    public static final ModelLayerLocation GAS_MASK_LAYER = new ModelLayerLocation(NoMansLand.location("gas_mask"), "main");

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NMLModelLayers.LIVING_POT_LAYER, LivingPotModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BURIED_LAYER, BuriedModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BURIED_INNER_ARMOR, () -> BuriedModel.createArmorLayer(new CubeDeformation(0.5F)));
        event.registerLayerDefinition(NMLModelLayers.BURIED_OUTER_ARMOR, () -> BuriedModel.createArmorLayer(new CubeDeformation(1.0F)));
        event.registerLayerDefinition(NMLModelLayers.MOOSE_LAYER, MooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BASS_LAYER, BillhookBassModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.CAVE_CARP_LAYER, CaveCarpModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.DEER_LAYER, DeerModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GOOSE_LAYER, GooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.CLOD_LAYER, ClodModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_LAYER, TortoiseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BUDDY_LAYER, BuddyModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.FRIENDERMAN_LAYER, FriendermanModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.CENTIPEDE_HEAD, CentipedeHeadModel::createLayer);
        event.registerLayerDefinition(NMLModelLayers.CENTIPEDE_FRONT, () -> CentipedeSegmentModel.createLayer(32, 0, 32, 21));
        event.registerLayerDefinition(NMLModelLayers.CENTIPEDE_MIDDLE, () -> CentipedeSegmentModel.createLayer(0, 0, 0, 21));
        event.registerLayerDefinition(NMLModelLayers.CENTIPEDE_TAIL, CentipedeTailModel::createLayer);
        event.registerLayerDefinition(NMLModelLayers.REMNANT_LAYER, RemnantModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BEETLE_LAYER, BeetleModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GRUB_LAYER, GrubModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.DUNG_BALL_LAYER, DungBallModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER, AncientBronzeMaskModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_SHELL_LAYER, TortoiseShellModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GAS_MASK_LAYER, GasMaskModel::createBodyLayer);
    }
}