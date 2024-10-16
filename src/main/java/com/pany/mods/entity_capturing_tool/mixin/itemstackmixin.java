package com.pany.mods.entity_capturing_tool.mixin;

import com.pany.mods.entity_capturing_tool.EntityCapturingTool;
import com.pany.mods.entity_capturing_tool.Helpers.ContainedObject;
import com.pany.mods.entity_capturing_tool.Helpers.ContainmentRenderingObject;
import com.pany.mods.entity_capturing_tool.Helpers.OtherHelper;
import com.pany.mods.entity_capturing_tool.injectedinterfaces.itemstackcontainedobject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.pany.mods.entity_capturing_tool.mixin.MixinConfig.MixinPriority;

@Mixin(value = ItemStack.class, priority = MixinPriority )
public class itemstackmixin implements itemstackcontainedobject {
    @Unique
    private ContainmentRenderingObject Contained = null;
    @Unique
    private boolean IsDirty = false;

    @Override
    public ContainmentRenderingObject GetContainedRender() {
        boolean isEnderCage = OtherHelper.isEndercage(((ItemStack)(Object)this).getItem());
        if ( !isEnderCage) {
            return null;
        }
        if (Contained == null) {
            Contained = new ContainmentRenderingObject((ItemStack)(Object)this);
        }
        return Contained;
    }
    @Override
    public boolean GetIfRenderDirty() {
        return IsDirty;
    }


    @Override
    public void SetIfRenderDirty(boolean Val) {
        IsDirty = Val;
    }

    //@Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtElement;",at = @At("TAIL"))
    private void endercage$containedupdate(RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<NbtElement> cir) {
        ItemStack ActualThis = ((ItemStack)(Object)this);
        if (ActualThis != null && ActualThis.getItem().equals(EntityCapturingTool.EnderCageItem) ) {
            ActualThis.SetIfRenderDirty(true);
        }
    }

    //@Inject(method = "setSubNbt",at = @At("TAIL"))
    private void endercage$containedupdate2(String key, NbtElement element, CallbackInfo ci) {
        if (key.equals(true)) {
            ItemStack ActualThis = ((ItemStack)(Object)this);
            if (!ActualThis.isEmpty() && ActualThis.getItem().equals(EntityCapturingTool.EnderCageBlock.asItem())) {
                ActualThis.SetIfRenderDirty(true);
            }
        }
    }
    //@Inject(method = "removeSubNbt",at = @At("TAIL"))
    private void endercage$containedupdate2(String key, CallbackInfo ci) {
        if (key.equals(true)) {
            ItemStack ActualThis = ((ItemStack)(Object)this);
            if (!ActualThis.isEmpty() && ActualThis.getItem().equals(EntityCapturingTool.EnderCageBlock.asItem())) {
                ActualThis.SetIfRenderDirty(true);
            }
        }
    }


}
