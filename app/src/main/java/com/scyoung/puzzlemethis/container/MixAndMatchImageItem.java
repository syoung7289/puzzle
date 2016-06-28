package com.scyoung.puzzlemethis.container;

import android.widget.ImageView;

/**
 * Created by scyoung on 5/16/16.
 */
public class MixAndMatchImageItem implements Comparable {
    String imageFileKey = null;
    String name = null;
    String category = null;
    ImageView overlay = null;
    boolean selected = false;
    int position;

    public MixAndMatchImageItem(String imageFileKey, String name, String category, boolean selected) {
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

    public ImageView getOverlay() {
        return overlay;
    }

    public void setOverlay(ImageView overlay) {
        this.overlay = overlay;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(Object another) {
        MixAndMatchImageItem compareTo = (MixAndMatchImageItem)another;
        String concat = getCategory() + getName();
        String anotherConcat = compareTo.getCategory() + compareTo.getName();
        return concat.compareToIgnoreCase(anotherConcat);
    }
}
