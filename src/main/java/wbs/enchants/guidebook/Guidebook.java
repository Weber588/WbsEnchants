package wbs.enchants.guidebook;

import com.google.common.collect.Multimap;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchants;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.util.ComponentUtils;

import java.util.*;

public class Guidebook {
    static final int MAX_LINES = 14;
    static final int WIDTH_PER_LINE = 122;
    static final int AVERAGE_CHAR_WIDTH = 5;

    public static Guidebook getGuidebook() {
        WbsEnchants plugin = WbsEnchants.getInstance();

        Component title = Component.text("Enchanting Guide")
                .color(plugin.getTextHighlightColour())
                .style(Style.style(TextDecoration.UNDERLINED, TextDecoration.BOLD));

        Component author = Component.text(plugin.getName())
                .color(plugin.getTextColour())
                .style(Style.style(TextDecoration.ITALIC));

        Guidebook guidebook = new Guidebook(title, author);

        Multimap<String, EnchantmentDefinition> definitions = EnchantManager.getDefinitionsByNamespace();

        for (String namespace : definitions.keySet()) {
            Chapter chapter = new Chapter(Component.text(namespace));
            for (EnchantmentDefinition definition : definitions.get(namespace)) {
                List<Component> description = definition.getDetailComponents(true);
                chapter.addSection(Component.join(JoinConfiguration.newlines(), description));
            }

            guidebook.addChapter(chapter);
        }

        return guidebook;
    }

    private final Component title;
    private final Component author;

    private final List<Chapter> chapters = new ArrayList<>();

    public Guidebook(Component title, Component author) {
        this.title = title;
        this.author = author;
    }

    public Book toBook() {
        Book.Builder builder = Book.builder()
                .author(author)
                .title(title);

        List<Component> allPages = new ArrayList<>();
        LinkedHashMap<Integer, Chapter> chaptersByPage = new LinkedHashMap<>();

        int currentPageAfterContents = 0;
        for (Chapter chapter : chapters) {
            chaptersByPage.put(currentPageAfterContents, chapter);

            for (Section section : chapter.getSections()) {
                List<Component> pages = section.getPages();

                allPages.addAll(pages);
                currentPageAfterContents += pages.size();
            }
        }

        // Create a dummy table of contents so we know how many pages it takes up. Then, we can figure out the offset and actually add click events.
        Component dummy = Component.empty();
        for (Integer pageNumber : chaptersByPage.keySet()) {
            Chapter chapter = chaptersByPage.get(pageNumber);

            dummy = dummy.append(chapter.title)
                    .append(Component.newline());
        }
        int tableOfContentsSize = new Section(dummy).getPages().size();

        Component tableOfContents = Component.empty();

        for (Integer pageNumber : chaptersByPage.keySet()) {
            Chapter chapter = chaptersByPage.get(pageNumber);

            tableOfContents = tableOfContents.append(chapter.title)
                    .clickEvent(ClickEvent.changePage(pageNumber + tableOfContentsSize))
                    .append(Component.newline());
        }

        new Section(tableOfContents).getPages()
                .reversed()
                .forEach(allPages::addFirst);

        builder.pages(allPages);

        return builder.build();
    }

    protected Guidebook addChapter(Chapter chapter) {
        chapters.add(chapter);
        return this;
    }
    protected Guidebook addChapter(int index, Chapter chapter) {
        chapters.add(index, chapter);
        return this;
    }

    public static class Chapter {
        private final Component title;
        private final List<Section> sections = new ArrayList<>();

        public Chapter(Component title) {
            this.title = title;
        }

        public Chapter addSection(Section section) {
            sections.add(section);
            return this;
        }
        public Chapter addSection(Component raw) {
            addSection(new Section(raw));
            return this;
        }

        public List<Section> getSections() {
            return this.sections;
        }
    }

    public record Section(Component raw) {
        public List<Component> getPages() {
            return ComponentUtils.wrapToPages(raw, WIDTH_PER_LINE / AVERAGE_CHAR_WIDTH, MAX_LINES);
        }
    }
}
