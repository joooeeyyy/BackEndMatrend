package com.tekbridge.alertapp.Models;

import java.awt.*;

public class WordBox {
    private String word;
    private Rectangle bounds;

    public WordBox(String word, Rectangle bounds) {
        this.word = word;
        this.bounds = bounds;
    }

    public String getWord() {
        return word;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}