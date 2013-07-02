package net.minecraft.src;

class EnumDoorHelper
{
    static final int[] doorEnum = new int[EnumDoor.values().length];

    static
    {
        try
        {
            doorEnum[EnumDoor.OPENING.ordinal()] = 1;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            doorEnum[EnumDoor.WOOD_DOOR.ordinal()] = 2;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            doorEnum[EnumDoor.GRATES.ordinal()] = 3;
        }
        catch (NoSuchFieldError ignored)
        {

        }

        try
        {
            doorEnum[EnumDoor.IRON_DOOR.ordinal()] = 4;
        }
        catch (NoSuchFieldError ignored)
        {

        }
    }
}
