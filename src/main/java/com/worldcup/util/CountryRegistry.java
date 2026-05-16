package com.worldcup.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CountryRegistry {

    public record Country(String name, String flag, String group) {}

    private static final String FALLBACK_FLAG = "🏳️";

    private static final List<Country> COUNTRIES = List.of(
        // Group A
        new Country("Mexico",        "🇲🇽", "A"),
        new Country("South Africa",  "🇿🇦", "A"),
        new Country("South Korea",   "🇰🇷", "A"),
        new Country("Czechia",       "🇨🇿", "A"),
        // Group B
        new Country("Canada",        "🇨🇦", "B"),
        new Country("Bosnia",        "🇧🇦", "B"),
        new Country("Qatar",         "🇶🇦", "B"),
        new Country("Switzerland",   "🇨🇭", "B"),
        // Group C
        new Country("Brazil",        "🇧🇷", "C"),
        new Country("Morocco",       "🇲🇦", "C"),
        new Country("Haiti",         "🇭🇹", "C"),
        new Country("Scotland",      "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "C"),
        // Group D
        new Country("USA",           "🇺🇸", "D"),
        new Country("Paraguay",      "🇵🇾", "D"),
        new Country("Australia",     "🇦🇺", "D"),
        new Country("Turkey",        "🇹🇷", "D"),
        // Group E
        new Country("Germany",       "🇩🇪", "E"),
        new Country("Curacao",       "🇨🇼", "E"),
        new Country("Cote d'Ivoire", "🇨🇮", "E"),
        new Country("Ecuador",       "🇪🇨", "E"),
        // Group F
        new Country("Netherlands",   "🇳🇱", "F"),
        new Country("Japan",         "🇯🇵", "F"),
        new Country("Sweden",        "🇸🇪", "F"),
        new Country("Tunisia",       "🇹🇳", "F"),
        // Group G
        new Country("Belgium",       "🇧🇪", "G"),
        new Country("Egypt",         "🇪🇬", "G"),
        new Country("IR Iran",       "🇮🇷", "G"),
        new Country("New Zealand",   "🇳🇿", "G"),
        // Group H
        new Country("Spain",         "🇪🇸", "H"),
        new Country("Cabo Verde",    "🇨🇻", "H"),
        new Country("Saudi Arabia",  "🇸🇦", "H"),
        new Country("Uruguay",       "🇺🇾", "H"),
        // Group I
        new Country("France",        "🇫🇷", "I"),
        new Country("Senegal",       "🇸🇳", "I"),
        new Country("Iraq",          "🇮🇶", "I"),
        new Country("Norway",        "🇳🇴", "I"),
        // Group J
        new Country("Argentina",     "🇦🇷", "J"),
        new Country("Algeria",       "🇩🇿", "J"),
        new Country("Austria",       "🇦🇹", "J"),
        new Country("Jordan",        "🇯🇴", "J"),
        // Group K
        new Country("Portugal",      "🇵🇹", "K"),
        new Country("Congo",         "🇨🇩", "K"),
        new Country("Uzbekistan",    "🇺🇿", "K"),
        new Country("Colombia",      "🇨🇴", "K"),
        // Group L
        new Country("England",       "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "L"),
        new Country("Croatia",       "🇭🇷", "L"),
        new Country("Ghana",         "🇬🇭", "L"),
        new Country("Panama",        "🇵🇦", "L")
    );

    private static final Map<String, Country> BY_NAME;
    private static final Map<String, List<Country>> BY_GROUP;

    static {
        Map<String, Country> byName = new HashMap<>();
        Map<String, List<Country>> byGroup = new LinkedHashMap<>();
        for (Country c : COUNTRIES) {
            byName.put(c.name(), c);
            byGroup.computeIfAbsent(c.group(), k -> new ArrayList<>()).add(c);
        }
        BY_NAME = Collections.unmodifiableMap(byName);
        Map<String, List<Country>> wrapped = new LinkedHashMap<>();
        byGroup.forEach((k, v) -> wrapped.put(k, Collections.unmodifiableList(v)));
        BY_GROUP = Collections.unmodifiableMap(wrapped);
    }

    public String getFlag(String country) {
        Country c = BY_NAME.get(country);
        return c != null ? c.flag() : FALLBACK_FLAG;
    }

    public Map<String, List<Country>> getGroups() {
        return BY_GROUP;
    }

    public static String groupOf(String country) {
        if (country == null) return null;
        Country c = BY_NAME.get(country);
        return c != null ? c.group() : null;
    }
}
