package com.albertbonet.testlibrary.location;

public interface GPSInterface {
    public void locationChanged(double longitude, double latitude, float accuracy);
    public void gpsDisabled();
}
