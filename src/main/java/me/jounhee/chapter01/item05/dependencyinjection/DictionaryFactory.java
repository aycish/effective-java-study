package me.jounhee.chapter01.item05.dependencyinjection;

import me.jounhee.chapter01.item05.DefaultDictionary;

public class DictionaryFactory {
    public static DefaultDictionary get() {
        return new DefaultDictionary();
    }
}
