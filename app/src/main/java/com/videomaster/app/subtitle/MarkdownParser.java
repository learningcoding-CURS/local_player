package com.videomaster.app.subtitle;

import com.videomaster.app.interfaces.ISubtitleParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses Markdown (.md) files as subtitles.
 *
 * Markdown syntax stripping rules applied before timing:
 *   - ATX headings  (#, ##, …)         → heading text only
 *   - Setext headings (=== / ---)       → skipped (separator lines)
 *   - Bold/Italic (**text**, *text*, __text__, _text_)  → plain text
 *   - Inline code (`code`)             → code text
 *   - Fenced code blocks (``` … ```)   → whole block skipped
 *   - Block-quote prefix (> )          → removed
 *   - Unordered list bullets (- / * / +) → removed
 *   - Ordered list numbers (1. )       → removed
 *   - Links [label](url)               → label only
 *   - Images ![alt](url)               → alt text only
 *   - HTML tags (<tag>)                → removed
 *   - Horizontal rules (---, ***, ___) → skipped
 *   - Empty lines                      → paragraph separator
 *
 * Timing model is identical to TxtParser (reading-speed based).
 */
public class MarkdownParser implements ISubtitleParser {

    private static final int  CHARS_PER_MINUTE = 300;
    private static final long MIN_DURATION_MS  = 2_000;
    private static final long MAX_DURATION_MS  = 8_000;
    private static final long GAP_MS           = 300;

    // ── Regex patterns ────────────────────────────────────────────────────

    private static final Pattern P_HEADING       = Pattern.compile("^#{1,6}\\s+(.+)$");
    private static final Pattern P_HR            = Pattern.compile("^([-*_]){3,}\\s*$");
    private static final Pattern P_SETEXT_SEP    = Pattern.compile("^[=\\-]{2,}\\s*$");
    private static final Pattern P_FENCE_OPEN    = Pattern.compile("^```.*$");
    private static final Pattern P_BLOCKQUOTE    = Pattern.compile("^>+\\s?");
    private static final Pattern P_UL_BULLET     = Pattern.compile("^[*\\-+]\\s+");
    private static final Pattern P_OL_BULLET     = Pattern.compile("^\\d+\\.\\s+");
    private static final Pattern P_IMAGE         = Pattern.compile("!\\[([^]]*)]\\([^)]*\\)");
    private static final Pattern P_LINK          = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
    private static final Pattern P_BOLD_ITALIC   = Pattern.compile("(\\*{1,3}|_{1,3})(.+?)\\1");
    private static final Pattern P_CODE_INLINE   = Pattern.compile("`([^`]+)`");
    private static final Pattern P_HTML_TAG      = Pattern.compile("<[^>]+>");
    private static final Pattern P_EXTRA_SPACES  = Pattern.compile("\\s{2,}");

    @Override
    public String getFormatName() { return "Markdown"; }

    @Override
    public String[] getSupportedExtensions() { return new String[]{".md", ".markdown"}; }

    @Override
    public List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        List<String> paragraphs = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean inFenceBlock = false;

        String line;
        while ((line = reader.readLine()) != null) {
            // Strip BOM
            if (buf.length() == 0 && paragraphs.isEmpty() && line.startsWith("\uFEFF"))
                line = line.substring(1);

            // Fenced code block toggle
            if (P_FENCE_OPEN.matcher(line).matches()) {
                inFenceBlock = !inFenceBlock;
                continue;
            }
            if (inFenceBlock) continue;

            // Horizontal rules and setext separators → paragraph boundary
            if (P_HR.matcher(line).matches() || P_SETEXT_SEP.matcher(line).matches()) {
                flushBuf(buf, paragraphs);
                continue;
            }

            // Empty line → paragraph boundary
            if (line.trim().isEmpty()) {
                flushBuf(buf, paragraphs);
                continue;
            }

            // Strip markdown syntax and get plain text
            String plain = stripMarkdown(line);
            if (plain.isEmpty()) continue;

            if (buf.length() > 0) buf.append(' ');
            buf.append(plain);
        }
        flushBuf(buf, paragraphs);

        return buildEntries(paragraphs);
    }

    private static void flushBuf(StringBuilder buf, List<String> out) {
        String s = buf.toString().trim();
        if (!s.isEmpty()) out.add(s);
        buf.setLength(0);
    }

    private static String stripMarkdown(String line) {
        // ATX heading
        java.util.regex.Matcher m = P_HEADING.matcher(line);
        if (m.matches()) line = m.group(1);

        // Block-quote prefix
        line = P_BLOCKQUOTE.matcher(line).replaceAll("");

        // List bullets
        line = P_UL_BULLET.matcher(line).replaceFirst("");
        line = P_OL_BULLET.matcher(line).replaceFirst("");

        // Images before links (to avoid nested conflicts)
        line = P_IMAGE.matcher(line).replaceAll("$1");

        // Links
        line = P_LINK.matcher(line).replaceAll("$1");

        // Bold / italic markers
        line = P_BOLD_ITALIC.matcher(line).replaceAll("$2");

        // Inline code
        line = P_CODE_INLINE.matcher(line).replaceAll("$1");

        // HTML tags
        line = P_HTML_TAG.matcher(line).replaceAll("");

        // Collapse extra whitespace
        line = P_EXTRA_SPACES.matcher(line.trim()).replaceAll(" ");

        return line.trim();
    }

    private List<SubtitleEntry> buildEntries(List<String> paragraphs) {
        List<SubtitleEntry> entries = new ArrayList<>();
        long cursor = 0;
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i);
            long dur = durationFor(text);
            entries.add(new SubtitleEntry(i + 1, cursor, cursor + dur, text));
            cursor += dur + GAP_MS;
        }
        return entries;
    }

    private long durationFor(String text) {
        long d = (long) text.length() * 60_000L / CHARS_PER_MINUTE;
        return Math.max(MIN_DURATION_MS, Math.min(MAX_DURATION_MS, d));
    }
}
