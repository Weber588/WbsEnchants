package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import wbs.enchants.WbsEnchants;

public class EventUtils {
    public static <T extends Event> void register(Class<T> eventClass, EventHandlerMethod<T> handler) {
        Bukkit.getPluginManager().registerEvent(eventClass,
                handler,
                EventPriority.NORMAL,
                (ignored, event) -> execute(eventClass, handler, event),
                WbsEnchants.getInstance(),
                true);
    }

    private static <T extends Event> void execute(Class<T> eventClass, EventHandlerMethod<T> handler, Event event) {
        if (!eventClass.isInstance(event)) {
            return;
        }
        T castEvent = eventClass.cast(event);
        handler.handle(castEvent);
    }

    @FunctionalInterface
    public interface EventHandlerMethod<T extends Event> extends Listener {
        void handle(T event);
    }
}
