package wbs.enchants.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;


public class ComponentUtils {
    public static List<Component> wrapToPages(Component page, int charactersPerLine, int maxLines) {
        List<Component> asLines = wrapToLines(page, charactersPerLine);

        List<Component> pageComponents = new ArrayList<>();
        for (int i = 0; i < asLines.size(); i += maxLines) {
            pageComponents.add(
                    Component.join(JoinConfiguration.newlines(), asLines.subList(i, Math.min(i + maxLines, asLines.size())))
            );
        }
        return pageComponents;
    }

    private static final TextComponent UNSUPPORTED = Component.text("ERROR WRAPPING").color(NamedTextColor.DARK_RED);

    // Credit Minikloon on GitHub:
    // https://gist.github.com/Minikloon/e6a7679d171b90dc4e0731db46d77c84
    public static List<Component> wrapToLines(final Component component, final int length) {
        if (!(component instanceof final TextComponent text)) {
            return Collections.singletonList(component);
        }

        final List<Component> wrapped = new ArrayList<>();
        final List<TextComponent> parts = flattenTextComponents(text);

        Component currentLine = Component.empty();
        int lineLength = 0;

        for (final TextComponent part : parts) {

            final Style style = part.style();
            final String content = part.content();
            final String[] words = content.split("(?<=\\s)|(?=\\n)");

            for (final String word : words) {

                if (word.isEmpty()) {
                    continue;
                }

                final int wordLength = word.length();
                final int totalLength = lineLength + wordLength;
                if (totalLength > length || word.contains("\n")) {
                    wrapped.add(currentLine);
                    currentLine = Component.empty().style(style);
                    lineLength = 0;
                }

                if (!word.equals("\n")) {
                    currentLine = currentLine.append(Component.text(word).style(style));
                    lineLength += wordLength;
                }
            }
        }

        if (lineLength > 0) {
            wrapped.add(currentLine);
        }

        return wrapped;
    }

    private static List<TextComponent> flattenTextComponents(final TextComponent component) {

        final List<TextComponent> flattened = new ArrayList<>();
        final Style style = component.style();
        final Style enforcedState = enforceStyleStates(style);
        final TextComponent first = component.style(enforcedState);

        final LinkedList<TextComponent> toCheck = new LinkedList<>();
        toCheck.add(first);

        while (!toCheck.isEmpty()) {

            final TextComponent parent = toCheck.pop();
            final String content = parent.content();
            if (!content.isEmpty()) {
                flattened.add(parent);
            }

            final List<Component> children = parent.children();
            final List<Component> reversed = children.reversed();
            for (final Component child : reversed) {
                if (child instanceof final TextComponent text) {
                    final Style parentStyle = parent.style();
                    final Style textStyle = text.style();
                    final Style merge = parentStyle.merge(textStyle);
                    final TextComponent childComponent = text.style(merge);
                    toCheck.add(childComponent);
                } else {
                    toCheck.add(UNSUPPORTED);
                }
            }
        }
        return flattened;
    }

    private static Style enforceStyleStates(final Style style) {
        final Style.Builder builder = style.toBuilder();
        final Map<TextDecoration, TextDecoration.State> map = style.decorations();
        map.forEach((decoration, state) -> {
            if (state == TextDecoration.State.NOT_SET) {
                builder.decoration(decoration, false);
            }
        });
        return builder.build();
    }
}
