package ssdl.technion.ac.il.locationnotification.utilities;

import java.io.Serializable;

/**
 * Created by Eran on 5/20/2015.
 */
public class MyLocation implements Serializable {
    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                '}';
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public MyLocation( Double latitude,Double longitude,int radius ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    Double latitude;
    Double longitude;
    int radius;
}
