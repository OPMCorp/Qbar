package net.qbar.common.gui;

public enum EGui
{
    BOILER(true), EXTRACTOR(true), KEYPUNCH(true);

    private final boolean containerBuilder;

    private EGui(final boolean containerBuilder)
    {
        this.containerBuilder = containerBuilder;
    }

    public boolean useContainerBuilder()
    {
        return this.containerBuilder;
    }
}
