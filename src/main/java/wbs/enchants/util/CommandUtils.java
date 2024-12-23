package wbs.enchants.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandUtils {
    public static final char SYNTAX_ESCAPE = '\\';

    public static String readStringUntilOrEnd(StringReader reader, char terminator) throws CommandSyntaxException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (reader.canRead()) {
            final char c = reader.read();
            if (escaped) {
                if (c == terminator || c == SYNTAX_ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    reader.setCursor(reader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, String.valueOf(c));
                }
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true;
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
