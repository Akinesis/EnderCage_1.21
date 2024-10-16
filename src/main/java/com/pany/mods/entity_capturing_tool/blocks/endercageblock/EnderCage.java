package com.pany.mods.entity_capturing_tool.blocks.endercageblock;

import com.mojang.serialization.MapCodec;
import com.pany.mods.entity_capturing_tool.EntityCapturingTool;
import com.pany.mods.entity_capturing_tool.Helpers.ContainedObject;
import com.pany.mods.entity_capturing_tool.Helpers.ContainmentHandler;
import com.pany.mods.entity_capturing_tool.Helpers.Shapes.EnderCageShapes;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderCage extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec<EnderCage> CODEC = createCodec(EnderCage::new);
    protected static final VoxelShape SHAPE = EnderCageShapes.EnderCageShape();
    public static final float EyeWidth = (1f/16f)*8f;

    public EnderCage(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public Item asItem() {
        return EntityCapturingTool.EnderCageItem;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnderCageEntity(pos,state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == EntityCapturingTool.EnderCageBlockEntity ? EnderCageEntity::tick : null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        EnderCageEntity cageEntity = null;
        Hand hand = player.getActiveHand();
        try {
            cageEntity = (EnderCageEntity)world.getBlockEntity(pos);
        } catch (Exception ignored) {}
        if (cageEntity != null && hand == Hand.MAIN_HAND && !world.isClient ) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack == null || stack.isEmpty() || stack.getItem() == Items.AIR) {
                boolean EntityContained = cageEntity.ContainedEntity.ContainsEntity();
                // Entity Name
                MutableText DataText = Text.translatable("text.endercage.capturedentity").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
                DataText.append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                MutableText EntityAppend = Text.translatable("text.endercage.noentity").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
                if (EntityContained) {
                    EntityType<?> type = cageEntity.ContainedEntity.GetEntityType();
                    if (type != null) {
                        EntityAppend = type.getName().copy().setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
                    }
                }
                DataText.append(EntityAppend);
                // Silent
                DataText.append(Text.literal("\n"));
                DataText.append( Text.translatable("text.endercage.silent").setStyle(Style.EMPTY.withColor(Formatting.GRAY))  );
                DataText.append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                boolean Silent = false;
                if (EntityContained && cageEntity.GetIfSilent()) {
                    Silent = cageEntity.GetSilentProperty();
                }
                Formatting SilentAnsColor = Silent ? Formatting.GREEN : Formatting.RED;
                String SilentAnsText = Silent ? "text.endercage.yes" : "text.endercage.no";
                DataText.append( Text.translatable(SilentAnsText).setStyle(Style.EMPTY.withColor(SilentAnsColor)) );
                player.sendMessage(DataText);
                return ActionResult.PASS;
            } else if (stack.getItem().equals(Items.FEATHER) && !cageEntity.GetSilentProperty()) {
                cageEntity.SetSilentProperty(true);
                world.playSound(null,pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS,1f,1f);
                Vec3d Center = pos.toCenterPos();
                ( (ServerWorld)world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK,Blocks.WHITE_WOOL.getDefaultState()),Center.getX(),Center.getY(),Center.getZ(),30,0.5,0.5,0.5,0);
                stack.decrement(1);
                return ActionResult.PASS;
            } else if (stack.getItem().equals(EntityCapturingTool.EnderCageBlock.asItem())) {
                ContainedObject StackObject = new ContainedObject(stack);
                if (!StackObject.TransferTo(cageEntity.ContainedEntity,player)) {
                    boolean Success = cageEntity.ContainedEntity.TransferTo(StackObject,player);
                    if (Success) {
                        world.playSound(null,pos, SoundEvents.ENTITY_ITEM_PICKUP,SoundCategory.BLOCKS,2,1.75f);
                    }
                } else if (  !(StackObject.ContainsEntity() && cageEntity.ContainedEntity.ContainsEntity()) ) {
                    world.playSound(null,pos, SoundEvents.ENTITY_ITEM_PICKUP,SoundCategory.BLOCKS,2,0.75f);
                }
                return ActionResult.PASS;
            }
        }
        return ActionResult.FAIL;
    }

    /*
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof EnderCageEntity enderCage) {
            NbtCompound nbtCompound = new NbtCompound();
            enderCage.writeNbt(nbtCompound);
            System.out.println(world.isClient ? "Client" : "World");
            System.out.println(nbtCompound.toString());
        }
    }

     */

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        if(stack.get(DataComponentTypes.BLOCK_ENTITY_DATA) == null){
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(new NbtCompound()));
        }

        NbtCompound nbt = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA).copyNbt();
        // Show Silence
        MutableText SilentText = Text.translatable("text.endercage.silent").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        SilentText.append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        boolean Silent = false;
        if (nbt != null && nbt.contains(ContainmentHandler.Keys.SilentKey)) {
            Silent = nbt.getBoolean(ContainmentHandler.Keys.SilentKey);
        }
        Formatting SilentAnsColor = Silent ? Formatting.GREEN : Formatting.RED;
        String SilentAnsText = Silent ? "text.endercage.yes" : "text.endercage.no";
        SilentText.append( Text.translatable(SilentAnsText).setStyle(Style.EMPTY.withColor(SilentAnsColor)) );
        // Show Entity name
        MutableText EntityText = Text.translatable("text.endercage.capturedentity").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        EntityText.append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        MutableText EntityAppend = Text.translatable("text.endercage.noentity").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
        if (nbt != null && nbt.contains( ContainmentHandler.Keys.EntityContained ) && nbt.getCompound(ContainmentHandler.Keys.EntityContained).contains(ContainmentHandler.Keys.MobType)) {
            try {
                EntityType<?> type = Registries.ENTITY_TYPE.get(Identifier.of(nbt.getCompound(ContainmentHandler.Keys.EntityContained).getString(ContainmentHandler.Keys.MobType)));
                EntityAppend = type.getName().copy().setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
            } catch (Exception ignored) {}
        }
        EntityText.append(EntityAppend);
        // Adding
        tooltip.add(EntityText);
        tooltip.add(SilentText);
        super.appendTooltip(stack, context, tooltip, options);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        EnderCageEntity cageEntity = (EnderCageEntity)world.getBlockEntity(pos);
        return cageEntity.ContainedEntity.ContainsEntity() ? 15 : 0;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) {
            return;
        }
        EnderCageEntity cageEntity = (EnderCageEntity)world.getBlockEntity(pos);
        cageEntity.UpdateRedstoneState(world.isReceivingRedstonePower(pos));
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

}
