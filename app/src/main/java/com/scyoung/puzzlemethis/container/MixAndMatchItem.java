package com.scyoung.puzzlemethis.container;

/**
 * Created by scyoung on 5/16/16.
 */
public class MixAndMatchItem implements Comparable {
    String imageFileKey = null;
    String name = null;
    String category = null;
    boolean selected = false;

    public MixAndMatchItem(String imageFileKey, String name, String category, boolean selected) {
        this.imageFileKey = imageFileKey;
        this.name = name;
        this.category = category;
        this.selected = selected;
    }

    public String getImageFileKey() {
        return imageFileKey;
    }

    public void setImageFileKey(String imageFileKey) {
        this.imageFileKey = imageFileKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int compareTo(Object another) {
        MixAndMatchItem compareTo = (MixAndMatchItem)another;
        String concat = getCategory() + getName();
        String anotherConcat = compareTo.getCategory() + compareTo.getName();
        return concat.compareToIgnoreCase(anotherConcat);
    }
}
