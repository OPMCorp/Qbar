package net.qbar.common.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.qbar.QBar;
import net.qbar.common.item.ItemBase;
import net.qbar.common.item.ItemPunchedCard;
import net.qbar.common.item.ItemWrench;

@ObjectHolder(QBar.MODID)
public class QBarItems
{
    @ObjectHolder("punched_card")
    public static final ItemBase PUNCHED_CARD = null;

    @ObjectHolder("wrench")
    public static final ItemBase WRENCH       = null;

    public static final void registerItems()
    {
        QBarItems.registerItem(new ItemPunchedCard());
        QBarItems.registerItem(new ItemWrench());
    }

    private static final void registerItem(final ItemBase item)
    {
        item.setRegistryName(QBar.MODID, item.name);
        GameRegistry.register(item);

        QBar.proxy.registerItemRenderer(item, 0, item.name);
    }
}
