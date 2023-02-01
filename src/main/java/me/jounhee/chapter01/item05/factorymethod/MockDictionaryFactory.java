package me.jounhee.chapter01.item05.factorymethod;

import me.jounhee.chapter01.item05.Dictionary;
import me.jounhee.chapter01.item05.MockDictionary;

public class MockDictionaryFactory implements DictionaryFactory {
    @Override
    public Dictionary getDictionary() {
        return new MockDictionary();
    }
}
