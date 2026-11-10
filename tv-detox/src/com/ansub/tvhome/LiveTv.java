package com.ansub.tvhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// The Live TV row: channel tiles that open a live channel straight inside its streaming app.
// Each channel is a name and a link. Links can be:
//   https://www.zee5.com/...0-9-xxxx            Zee5 (any Zee5 page link that has the channel id)
//   https://www.sunnxt.com/live/.../194392/...   Sun NXT web page, turned into its TV app link
//   sunnxt://playback?source=deeplink&id=194392  Sun NXT TV app link
//   https://www.hotstar.com/...                  JioHotstar
//   app:com.etvwin.androidtv                     just opens an app (for apps with no channel links, like ETV Win)
//   any other link                               handed to whichever app on the TV takes it
// Saved on the TV as one "name<TAB>link" per line; until something is changed, DEFAULTS is used.
final class LiveTv {
    static final String PREF = "channels";

    static final String ZEE5 = "com.graymatrix.did", SUNNXT = "com.suntv.sunnxt",
            HOTSTAR = "in.startv.hotstar", ETVWIN = "com.etvwin.androidtv";

    private static String zee(String id) { return "https://www.zee5.com/live-tv/x/" + id; }
    private static String sun(String id) { return "sunnxt://playback?source=deeplink&id=" + id; }
    private static final String ETV = "app:" + ETVWIN;

    // Every Zee5 and Sun NXT link here was opened on a Haier MatrixTV DE and played the named channel.
    static final String[][] DEFAULTS = {
        {"TV9 Telugu", zee("0-9-258")},
        {"Zee Telugu HD", zee("0-9-zeetelugu")},
        {"Zee Cinemalu HD", zee("0-9-zeecinemalu")},
        {"&Pictures HD", zee("0-9-tvpictureshd")},
        {"Zee Cinema", zee("0-9-zeecinema")},
        {"Zee Classic", zee("0-9-176")},
        {"Zee Action", zee("0-9-zeeaction")},
        {"Zee Bollywood", zee("0-9-zeeclassic")},
        {"&TV HD", zee("0-9-tvhd_0")},
        {"Gemini HD", sun("194392")},
        {"Gemini Movies HD", sun("192537")},
        {"Gemini Movies", sun("194384")},
        {"Gemini Music HD", sun("194393")},
        {"Gemini Comedy", sun("194394")},
        {"Gemini Life", sun("194337")},
        {"Kushi TV", sun("194346")},
        {"Surya HD", sun("194397")},
        {"Surya Movies", sun("194385")},
        {"Udaya HD", sun("194399")},
        {"Udaya Movies", sun("194386")},
        {"Sun Neo HD", sun("194389")},
        {"ETV Telugu", ETV},
        {"ETV Plus", ETV},
        {"ETV Cinema", ETV},
        {"ETV Music", ETV},
        {"ETV Life", ETV},
        {"ETV Andhra Pradesh", ETV},
    };

    static final class Channel {
        final String name, link;
        Channel(String name, String link) { this.name = name; this.link = link; }
    }

    static List<Channel> load(SharedPreferences prefs) {
        List<Channel> out = new ArrayList<>();
        String saved = prefs.getString(PREF, null);
        if (saved == null) {
            for (String[] c : DEFAULTS) out.add(new Channel(c[0], c[1]));
            return out;
        }
        for (String line : saved.split("\n")) {
            int tab = line.indexOf('\t');
            if (tab > 0 && tab < line.length() - 1) out.add(new Channel(line.substring(0, tab), line.substring(tab + 1)));
        }
        return out;
    }

    static void save(SharedPreferences prefs, List<Channel> list) {
        StringBuilder b = new StringBuilder();
        for (Channel c : list) {
            if (b.length() > 0) b.append('\n');
            b.append(clean(c.name)).append('\t').append(clean(c.link));
        }
        prefs.edit().putString(PREF, b.toString()).apply();
    }

    static void reset(SharedPreferences prefs) { prefs.edit().remove(PREF).apply(); }

    static String clean(String s) {
        return s == null ? "" : s.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static final Pattern SUN_WEB_ID = Pattern.compile("sunnxt\\.com/.*?/(\\d{4,})(/|$|\\?)");

    // Turns what people paste into the link the app on the TV understands.
    static String normalise(String link) {
        link = clean(link);
        if (link.isEmpty()) return link;
        if (!link.contains(":") && !link.startsWith("/")) link = "https://" + link; // "www.zee5.com/..." pasted bare
        Matcher m = SUN_WEB_ID.matcher(link);
        if (link.matches("(?i)https?://(www\\.)?sunnxt\\.com/.*") && m.find()) return sun(m.group(1));
        return link;
    }

    // Which app a link belongs to, or null to let Android pick.
    static String packageFor(String link) {
        String l = link.toLowerCase(java.util.Locale.ROOT);
        if (l.startsWith("app:")) return link.substring(4).trim();
        if (l.startsWith("sunnxt:") || l.matches("https?://(www\\.)?sunnxt\\.com/.*")) return SUNNXT;
        if (l.startsWith("zee5:") || l.matches("https?://(www\\.)?zee5\\.com/.*")) return ZEE5;
        if (l.startsWith("hotstar:") || l.matches("https?://(www\\.)?hotstar\\.com/.*")
                || l.matches("https?://hotstar\\.onelink\\.me/.*")) return HOTSTAR;
        return null;
    }

    // The intent that opens this channel, or null if no app on the TV can.
    static Intent intentFor(PackageManager pm, String link, Intent appLaunch) {
        link = normalise(link);
        if (link.isEmpty()) return null;
        String pkg = packageFor(link);
        Intent i;
        if (link.toLowerCase(java.util.Locale.ROOT).startsWith("app:")) {
            i = appLaunch;
            if (i == null) return null;
        } else {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (pkg != null && appLaunch != null) i.setPackage(pkg);
            else if (pkg != null) return null; // the app this link is for isn't installed
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }
}
