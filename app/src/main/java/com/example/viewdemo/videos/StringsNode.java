package com.example.viewdemo.videos;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/14
 */
public class StringsNode {
    private String[][] strings;
    private StringsNode next;

    public StringsNode(String[][] strings) {
        this.strings = strings;
    }

    public String[][] getStrings() {
        return strings;
    }

    public void setNext(StringsNode next) {
        this.next = next;
    }

    public boolean hasNext() {
        return next != null;
    }

    public StringsNode next() {
        return next;
    }
}
