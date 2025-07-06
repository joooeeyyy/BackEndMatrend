package com.tekbridge.alertapp.Design.FactoryD;

public class MsiManufacturere extends Company {
    @Override
    public Gpu createGpu() {
        return new MsiGpu();
    }
}
