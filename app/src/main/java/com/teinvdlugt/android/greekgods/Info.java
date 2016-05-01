package com.teinvdlugt.android.greekgods;

import java.io.Serializable;

public class Info implements Serializable {
    public static final int INFO_TYPE_PERSON = 0;
    public static final int INFO_TYPE_RELATION = 1;

    public final int id;
    public final int infoType;

    public Info(int id, int infoType) {
        this.id = id;
        this.infoType = infoType;
    }
}
