package com.worldcup.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("flagUtil")
public class FlagUtil {

    private static final Map<String, String> FLAGS = Map.ofEntries(
        Map.entry("Mexico",          "🇲🇽"),
        Map.entry("South Africa",    "🇿🇦"),
        Map.entry("South Korea",     "🇰🇷"),
        Map.entry("Czechia",         "🇨🇿"),
        Map.entry("Canada",          "🇨🇦"),
        Map.entry("Bosnia",          "🇧🇦"),
        Map.entry("Qatar",           "🇶🇦"),
        Map.entry("Switzerland",     "🇨🇭"),
        Map.entry("Brazil",          "🇧🇷"),
        Map.entry("Morocco",         "🇲🇦"),
        Map.entry("Haiti",           "🇭🇹"),
        Map.entry("Scotland",        "🏴󠁧󠁢󠁳󠁣󠁴󠁿"),
        Map.entry("USA",             "🇺🇸"),
        Map.entry("Paraguay",        "🇵🇾"),
        Map.entry("Australia",       "🇦🇺"),
        Map.entry("Turkey",          "🇹🇷"),
        Map.entry("Germany",         "🇩🇪"),
        Map.entry("Curacao",         "🇨🇼"),
        Map.entry("Cote d'Ivoire",   "🇨🇮"),
        Map.entry("Ecuador",         "🇪🇨"),
        Map.entry("Netherlands",     "🇳🇱"),
        Map.entry("Japan",           "🇯🇵"),
        Map.entry("Sweden",          "🇸🇪"),
        Map.entry("Tunisia",         "🇹🇳"),
        Map.entry("Belgium",         "🇧🇪"),
        Map.entry("Egypt",           "🇪🇬"),
        Map.entry("IR Iran",         "🇮🇷"),
        Map.entry("New Zealand",     "🇳🇿"),
        Map.entry("Spain",           "🇪🇸"),
        Map.entry("Cabo Verde",      "🇨🇻"),
        Map.entry("Saudi Arabia",    "🇸🇦"),
        Map.entry("Uruguay",         "🇺🇾"),
        Map.entry("France",          "🇫🇷"),
        Map.entry("Senegal",         "🇸🇳"),
        Map.entry("Iraq",            "🇮🇶"),
        Map.entry("Norway",          "🇳🇴"),
        Map.entry("Argentina",       "🇦🇷"),
        Map.entry("Algeria",         "🇩🇿"),
        Map.entry("Austria",         "🇦🇹"),
        Map.entry("Jordan",          "🇯🇴"),
        Map.entry("Portugal",        "🇵🇹"),
        Map.entry("Congo",           "🇨🇩"),
        Map.entry("Uzbekistan",      "🇺🇿"),
        Map.entry("Colombia",        "🇨🇴"),
        Map.entry("England",         "🏴󠁧󠁢󠁥󠁮󠁧󠁿"),
        Map.entry("Croatia",         "🇭🇷"),
        Map.entry("Ghana",           "🇬🇭"),
        Map.entry("Panama",          "🇵🇦")
    );

    public String get(String country) {
        return FLAGS.getOrDefault(country, "🏳️");
    }
}
