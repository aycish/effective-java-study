package me.jounhee.chapter02.item13.clone_use_constructor;

public class Item implements Cloneable {

    private String name;
    @Override
    public Item clone() {
        Item result = null;
        try {
            result = (Item) super.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
