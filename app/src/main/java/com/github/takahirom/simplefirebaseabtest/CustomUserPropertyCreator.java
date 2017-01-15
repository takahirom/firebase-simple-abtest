package com.github.takahirom.simplefirebaseabtest;

import com.github.takahirom.fireannotation.CustomValueCreator;

import java.util.HashMap;
import java.util.Map;

class CustomUserPropertyCreator extends CustomValueCreator<MainActivity> {
    private static final String ANALYTICS_USER_PROPERTY_BUTTON_COLOR_KEY = "button_color";
    @Override
    public Map<String, String> getValue(MainActivity annotatedObject) {
        final HashMap<String, String> userProperty = new HashMap<>();
        userProperty.put(ANALYTICS_USER_PROPERTY_BUTTON_COLOR_KEY, annotatedObject.getButtonColor().replace("#", ""));
        return userProperty;
    }
}
