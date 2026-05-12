package com.worldcup.util;

import org.springframework.stereotype.Component;

@Component("flagUtil")
public class FlagUtil {

    private final CountryRegistry registry;

    public FlagUtil(CountryRegistry registry) {
        this.registry = registry;
    }

    public String get(String country) {
        return registry.getFlag(country);
    }
}
