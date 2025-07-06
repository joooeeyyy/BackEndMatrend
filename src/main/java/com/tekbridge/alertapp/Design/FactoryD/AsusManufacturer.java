package com.tekbridge.alertapp.Design.FactoryD;

public class AsusManufacturer extends Company {
    @Override
    public Gpu createGpu() {
        return new AsusGpu();
    }
}
