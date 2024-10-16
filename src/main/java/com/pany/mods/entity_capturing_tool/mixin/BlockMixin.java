package com.pany.mods.entity_capturing_tool.mixin;

import com.pany.mods.entity_capturing_tool.EntityCapturingTool;
import com.pany.mods.entity_capturing_tool.Helpers.ContainmentHandler;
import com.pany.mods.entity_capturing_tool.Helpers.OtherHelper;
import com.pany.mods.entity_capturing_tool.blocks.endercageblock.EnderCage;
import com.pany.mods.entity_capturing_tool.blocks.endercageblock.EnderCageDiamondEntity;
import com.pany.mods.entity_capturing_tool.blocks.endercageblock.EnderCageEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Block.class)
public abstract class BlockMixin {

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
    at = @At(value = "HEAD"),
    cancellable = true)
    private static void enderCage$copyBlockDataEntity(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci){
        if (!(blockEntity instanceof EnderCageEntity enderCageEntity))
            return;

        ItemStack enderCage;

        if(enderCageEntity instanceof EnderCageDiamondEntity)
            enderCage = EntityCapturingTool.EnderCageDiamondItem.getDefaultStack();
        else
            enderCage = EntityCapturingTool.EnderCageItem.getDefaultStack();

        if (world instanceof ServerWorld) {


            //NbtComponent blockEntityData = blockEntity.getComponents().get(DataComponentTypes.BLOCK_ENTITY_DATA);
            //NbtCompound nbt = enderCageEntity.ContainedEntity.ToNbt();
            NbtCompound nbt = new NbtCompound();

            nbt.putString("id","mob_container");
            nbt.put(ContainmentHandler.Keys.EntityContained,enderCageEntity.ContainedEntity.ToNbt());

            enderCage.set(DataComponentTypes.BLOCK_ENTITY_DATA,NbtComponent.of(nbt));

            Block.dropStack(world, pos, enderCage);
            state.onStacksDropped((ServerWorld)world, pos, tool, true);
            ci.cancel();
        }

    }
}
