package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PersistentLocationType implements PersistentDataType<String, Location> {

    public static final PersistentLocationType INSTANCE = new PersistentLocationType();

    @NotNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NotNull
    @Override
    public Class<Location> getComplexType() {
        return Location.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull Location location, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        String locationString = "";

        World world = location.getWorld();
        if (world != null) {
            locationString = world.getName();
        }

        locationString += ";" + location.getX() + "," + location.getY() + "," + location.getZ();

        locationString += ";" + location.getPitch() + "," + location.getYaw();

        return locationString;
    }

    @NotNull
    @Override
    public Location fromPrimitive(@NotNull String locationString, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        String[] args = locationString.split(";");
        if (args.length != 3) {
            throw new IllegalArgumentException("Invalid string location format: " + locationString);
        }

        World world = null;

        String worldName = args[0];
        if (worldName.length() > 0) {
            world = Bukkit.getWorld(worldName);
        }

        String[] coordArgs = args[1].split(",");
        if (coordArgs.length != 3) {
            throw new IllegalArgumentException("Invalid string location format (3 coords needed): " + locationString);
        }

        double x = Double.parseDouble(coordArgs[0]);
        double y = Double.parseDouble(coordArgs[1]);
        double z = Double.parseDouble(coordArgs[2]);

        String[] pitchYawArgs = args[2].split(",");
        if (pitchYawArgs.length != 2) {
            throw new IllegalArgumentException("Invalid string location format (pitch & yaw needed): " + locationString);
        }
        float pitch = Float.parseFloat(pitchYawArgs[0]);
        float yaw = Float.parseFloat(pitchYawArgs[1]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
