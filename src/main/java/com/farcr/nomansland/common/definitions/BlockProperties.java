package com.farcr.nomansland.common.definitions;

import com.farcr.nomansland.datagen.loot.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record BlockProperties(BlockLootType lootType, boolean customLang) {

    public static BlockProperties custom(boolean customLang) {
        return new BlockProperties(new CustomBlockLootType(), customLang);
    }

    // MISC
    public static BlockProperties simplePath(Block block) {
        return new BlockProperties(new OtherBlockLootType(() -> block), false);
    } // paths
    public static BlockProperties simplePath(BlockDefinition<?> block) {
        return new BlockProperties(new OtherBlockLootType(block::get), false);
    } // paths
    public static BlockProperties path() {
        return new BlockProperties(new CustomBlockLootType(), false);
    } // paths
    public static BlockProperties fishBarrel() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // fish_barrel
    public static BlockProperties physicsBarrel() {
        return new BlockProperties(new SelfBlockLootType(), false);
    }
    public static BlockProperties dirtLike() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // shovel
    public static BlockProperties cauldron() {
        return new BlockProperties(new OtherBlockLootType(()-> Blocks.CAULDRON), true);
    } // cauldrons
    public static BlockProperties icicles() {
        return new BlockProperties(new CustomBlockLootType(), false);
    } // icicles
    public static BlockProperties froglight() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // froglights

    // PLANTS
    public static BlockProperties smallFlower() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // small_flowers
    public static BlockProperties flowerbed() {
        return new BlockProperties(new ShearsBlockLootType(), false);
    } // flowerbeds
    public static BlockProperties sapling() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // saplings
    public static BlockProperties flowerPot(BlockDefinition<?> plant) {
        return new BlockProperties(new FlowerPotBlockLootType(plant::get), false);
    } // flower_pots
    public static BlockProperties candleCake(Block candle) {
        return new BlockProperties(new CandleCakeBlockLootType(candle), false);
    } // candle_cakes

    // STONE
    public static BlockProperties stoneLike() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // pickaxe
    public static BlockProperties stoneLikeStairs() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // pickaxe, stairs
    public static BlockProperties stoneLikeSlab() {
        return new BlockProperties(new SlabBlockLootType(), false);
    } // pickaxe, slabs
    public static BlockProperties stoneLikeWall() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // pickaxe, walls

    // WOOD
    public static BlockProperties log() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // no easily-defined tags - logs are weird!
    public static BlockProperties planks() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // planks
    public static BlockProperties woodenButton(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // wooden_buttons
    public static BlockProperties woodenDoor(BlockDefinition<?> planks) {
        return new BlockProperties(new DoorBlockLootType(), false);
    } // wooden_doors
    public static BlockProperties woodenFence(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // wooden_fences
    public static BlockProperties fenceGate(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // fence_gates
    public static BlockProperties woodenPressurePlate(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // wooden_pressure_plates
    public static BlockProperties woodenSlab(BlockDefinition<?> planks) {
        return new BlockProperties(new SlabBlockLootType(), false);
    } // wooden_slabs
    public static BlockProperties woodenStairs(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // wooden_stairs
    public static BlockProperties woodenTrapdoor(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // wooden_trapdoors
    public static BlockProperties bookshelf(BlockDefinition<?> planks) {
        return new BlockProperties(new BookshelfBlockLootType(), false);
    } // bookshelves
    public static BlockProperties bookshelf(Block planks) {
        return new BlockProperties(new BookshelfBlockLootType(), false);
    } // bookshelves
    public static BlockProperties trimmedPlanks(BlockDefinition<?> planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // trimmed_planks
    public static BlockProperties trimmedPlanks(Block planks) {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // trimmed_planks
    public static BlockProperties sign() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // hard to determine
    public static BlockProperties hangingSign() {
        return new BlockProperties(new SelfBlockLootType(), false);
    } // also hard to determine
}
