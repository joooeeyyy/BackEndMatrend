package com.tekbridge.alertapp.Servcies;

import com.google.cloud.vision.v1.BoundingPoly;

public class BoundPolyAndDescription {

    String description;
    BoundingPoly boundingPoly;

    boolean noText;

    public BoundPolyAndDescription(boolean noText , String description, BoundingPoly boundingPoly) {
        this.boundingPoly = noText ? null: boundingPoly;
        this.description = description;
        this.noText =noText;
    }

    public BoundingPoly getBoundingPoly() {
        return boundingPoly;
    }

    public void setBoundingPoly(BoundingPoly boundingPoly) {
        this.boundingPoly = boundingPoly;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNoText() {
        return noText;
    }

    public void setNoText(boolean noText) {
        this.noText = noText;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
