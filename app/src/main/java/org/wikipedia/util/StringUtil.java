package org.wikipedia.util;

import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class StringUtil {
    private static final String CSV_DELIMITER = ",";

    @NonNull
    public static String listToCsv(@NonNull List<String> list) {
        return TextUtils.join(CSV_DELIMITER, list);
    }

    /** @return Nonnull immutable list. */
    @NonNull
    public static List<String> csvToList(@NonNull String csv) {
        return delimiterStringToList(csv, CSV_DELIMITER);
    }

    /** @return Nonnull immutable list. */
    @NonNull
    public static List<String> delimiterStringToList(@NonNull String delimitedString,
                                                     @NonNull String delimiter) {
        return Arrays.asList(TextUtils.split(delimitedString, delimiter));
    }

    /**
     * Creates an MD5 hash of the provided string and returns its ASCII representation
     * @param s String to hash
     * @return ASCII MD5 representation of the string passed in
     */
    @NonNull public static String md5string(@NonNull String s) {
        StringBuilder hexStr = new StringBuilder();
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(StandardCharsets.UTF_8));
            byte[] messageDigest = digest.digest();

            final int maxByteVal = 0xFF;
            for (byte b : messageDigest) {
                hexStr.append(Integer.toHexString(maxByteVal & b));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hexStr.toString();
    }

    /**
     * Remove leading and trailing whitespace from a CharSequence. This is useful after using
     * the fromHtml() function to convert HTML to a CharSequence.
     * @param str CharSequence to be trimmed.
     * @return The trimmed CharSequence.
     */
    @NonNull public static CharSequence strip(@Nullable CharSequence str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        int len = str.length();
        int start = 0;
        int end = len - 1;
        while (start < len && Character.isWhitespace(str.charAt(start))) {
            start++;
        }
        while (end > 0 && Character.isWhitespace(str.charAt(end))) {
            end--;
        }
        if (end > start) {
            return str.subSequence(start, end + 1);
        }
        return "";
    }

    @NonNull
    public static String intToHexStr(int i) {
        return String.format("x%08x", i);
    }

    public static String addUnderscores(@NonNull String text) {
        return text.replace(" ", "_");
    }

    public static String removeUnderscores(@NonNull String text) {
        return text.replace("_", " ");
    }

    public static boolean hasSectionAnchor(@NonNull String text) {
        return text.contains("#");
    }

    public static String removeSectionAnchor(String text) {
        return text.substring(0, text.indexOf("#"));
    }

    public static String removeNamespace(@NonNull String text) {
        if (text.length() > text.indexOf(":")) {
            return text.substring(text.indexOf(":") + 1);
        } else {
            return text;
        }
    }

    public static String removeHTMLTags(@NonNull String text) {
        return fromHtml(text).toString();
    }

    public static String sanitizeText(@NonNull String selectedText) {
        return selectedText.replaceAll("\\[\\d+\\]", "") // [1]
                // https://en.wikipedia.org/wiki/Phonetic_symbols_in_Unicode
                .replaceAll("\\s*/[^/]+/;?\\s*", "")
                .replaceAll("\\(\\s*;\\s*", "\\(") // (; -> (    hacky way for IPA remnants
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    // Compare two strings based on their normalized form, using the Unicode Normalization Form C.
    // This should be used when comparing or verifying strings that will be exchanged between
    // different platforms (iOS, desktop, etc) that may encode strings using inconsistent
    // composition, especially for accents, diacritics, etc.
    public static boolean normalizedEquals(@Nullable String str1, @Nullable String str2) {
        if (str1 == null || str2 == null) {
            return (str1 == null && str2 == null);
        }
        return Normalizer.normalize(str1, Normalizer.Form.NFC)
                .equals(Normalizer.normalize(str2, Normalizer.Form.NFC));
    }

    /**
     * @param source String that may contain HTML tags.
     * @return returned Spanned string that may contain spans parsed from the HTML source.
     */
    @NonNull public static Spanned fromHtml(@Nullable String source) {
        if (source == null) {
            return new SpannedString("");
        }
        if (!source.contains("<") && !source.contains("&")) {
            // If the string doesn't contain any hints of HTML entities, then skip the expensive
            // processing that fromHtml() performs.
            return new SpannedString(source);
        }
        source = source.replaceAll("&#8206;", "\u200E")
                .replaceAll("&#8207;", "\u200F")
                .replaceAll("&amp;", "&");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(source);
        }
    }

    @NonNull
    public static SpannableStringBuilder boldenSubstrings(@NonNull String text, @NonNull List<String> subStrings) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        String textLowerCase = text.toLowerCase();
        for (String subString : subStrings) {
            int index = textLowerCase.indexOf(subString.toLowerCase());
            if (index != -1 && index + subString.length() < sb.length()) {
                sb.setSpan(new TypefaceSpan("sans-serif-medium"),
                        index, index + subString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return sb;
    }

    public static void highlightEditText(@NonNull EditText editText, @NonNull String parentText, @NonNull String highlightText) {
        String[] words = highlightText.split("\\s+");
        int pos = 0;
        for (String word : words) {
            pos = parentText.indexOf(word, pos);
            if (pos == -1) {
                break;
            }
        }
        if (pos == -1) {
            pos = parentText.indexOf(words[words.length - 1]);
        }
        if (pos >= 0) {
            // TODO: Programmatic selection doesn't seem to work with RTL content...
            editText.setSelection(pos, pos + words[words.length - 1].length());
            editText.performLongClick();
        }
    }

    public static void boldenKeywordText(@NonNull TextView textView, @NonNull String parentText, @Nullable String searchQuery) {
        int startIndex = indexOf(parentText, searchQuery);
        if (startIndex >= 0) {
            parentText = parentText.substring(0, startIndex)
                    + "<strong>"
                    + parentText.substring(startIndex, startIndex + searchQuery.length())
                    + "</strong>"
                    + parentText.substring(startIndex + searchQuery.length());
            textView.setText(StringUtil.fromHtml(parentText));
        } else {
            textView.setText(parentText);
        }
    }

    // case insensitive indexOf, also more lenient with similar chars, like chars with accents
    private static int indexOf(@NonNull String original, @Nullable String search) {
        if (!TextUtils.isEmpty(search)) {
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
            for (int i = 0; i <= original.length() - search.length(); i++) {
                if (collator.equals(search, original.substring(i, i + search.length()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @NonNull
    public static String getBase26String(@IntRange(from = 1) int number) {
        final int base = 26;
        String str = "";
        while (--number >= 0) {
            str = (char)('A' + number % base) + str;
            number /= base;
        }
        return str;
    }

    @NonNull
    public static String listToJsonArrayString(@NonNull List<String> list) {
        return new JSONArray(list).toString();
    }

    public static String stringToListMapToJSONString(@Nullable Map<String, List<Integer>> map) {
        return new Gson().toJson(map);
    }

    public static String listToJSONString(@Nullable List<Integer> list) {
        return new Gson().toJson(list);
    }

    private StringUtil() {
    }
}
