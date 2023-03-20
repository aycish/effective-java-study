package me.jounhee.chapter02.item13.clone_use_constructor;

public class SubItem extends Item implements Cloneable {

    private String name;

    public static void main(String[] args) {
        SubItem item = new SubItem();
        SubItem clone = (SubItem) item.clone();

        System.out.println(clone != item);
        System.out.println(clone.getClass() == item.getClass());
        System.out.println(clone.equals(item));
    }
}
