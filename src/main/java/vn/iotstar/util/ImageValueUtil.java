package vn.iotstar.util;

public final class ImageValueUtil {

    private ImageValueUtil() {}

    // format: url|publicId
    public static String build(String url, String publicId) {
        if (url == null) url = "";
        if (publicId == null) publicId = "";
        return url + "|" + publicId;
    }

    public static String extractUrl(String value) {
        if (value == null || value.isBlank()) return null;
        int idx = value.indexOf('|');
        if (idx < 0) return value;
        String url = value.substring(0, idx);
        return url.isBlank() ? null : url;
    }

    public static String extractPublicId(String value) {
        if (value == null || value.isBlank()) return null;
        int idx = value.indexOf('|');
        if (idx < 0) return null;
        String pid = value.substring(idx + 1);
        return pid.isBlank() ? null : pid;
    }
}