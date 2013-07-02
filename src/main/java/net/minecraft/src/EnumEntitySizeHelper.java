package net.minecraft.src;

class EnumEntitySizeHelper
{
    static final int[] field_96565_a = new int[EnumEntitySize.values().length];

    static
    {
        try
        {
            field_96565_a[EnumEntitySize.SIZE_1.ordinal()] = 1;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            field_96565_a[EnumEntitySize.SIZE_2.ordinal()] = 2;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            field_96565_a[EnumEntitySize.SIZE_3.ordinal()] = 3;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            field_96565_a[EnumEntitySize.SIZE_4.ordinal()] = 4;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            field_96565_a[EnumEntitySize.SIZE_5.ordinal()] = 5;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            field_96565_a[EnumEntitySize.SIZE_6.ordinal()] = 6;
        }
        catch (NoSuchFieldError ignored)
        {

        }
    }
}
