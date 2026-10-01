package net.sabio.moreweapons.items;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

public class PouchMenu extends AbstractContainerMenu {
    private final ItemStack slingshot;
    private final Level level;
    private static final int[][] LAYOUT = {{}, {4}, {3, 5}, {3, 4, 5}};
    private final int[] open;
    private final SimpleContainer container = new SimpleContainer(9);

    public PouchMenu(int containerId, Inventory inventory, ItemStack slingshot) {
        super(MenuType.GENERIC_9x1, containerId);
        this.slingshot = slingshot;
        this.level = inventory.player.level();
        this.open = LAYOUT[SlingshotItem.pouchSlots(slingshot, level)];
        NonNullList<ItemStack> stored = SlingshotItem.readPouch(slingshot);

        ItemStack pane = new ItemStack(Items.STAINED_GLASS_PANE.gray());
        pane.set(DataComponents.CUSTOM_NAME, Component.literal("Disabled Slot"));

        for (int i = 0; i < 9; i++) {
            int pouchIdx = Arrays.binarySearch(open, i);
            if (pouchIdx >= 0) {
                container.setItem(i, stored.get(pouchIdx));
                addSlot(new Slot(container, i, 8 + i * 18, 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return SlingshotItem.isAmmoBlock(stack, slingshot, PouchMenu.this.level);
                    }
                });
            } else {
                container.setItem(i, pane.copy());
                addSlot(new Slot(container, i, 8 + i * 18, 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new GuardedSlot(inventory, col + row * 9 + 9, 8 + col * 18, 49 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new GuardedSlot(inventory, col, 8 + col * 18, 107));
        }

        SlingshotItem.writePouch(slingshot, List.of());
    }

    private class GuardedSlot extends Slot {
        GuardedSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return getItem() != slingshot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack != slingshot;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.getSlot(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, open[0], open[open.length - 1] + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        NonNullList<ItemStack> contents = NonNullList.withSize(3, ItemStack.EMPTY);
        for (int p = 0; p < open.length; p++) {
            contents.set(p, container.removeItemNoUpdate(open[p]));
        }

        if (stillHeld(player)) {
            SlingshotItem.writePouch(slingshot, contents);
        } else {
            for (ItemStack leftover : contents) {
                if (!leftover.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(leftover);
                }
            }
        }
    }

    private boolean stillHeld(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == slingshot) {
                return true;
            }
        }
        return false;
    }
}
