package ssdl.technion.ac.il.locationnotification;

/**
 * Created by yoav on 15/05/2015.
 */
public class Information {
    int iconId;
    String title;

    public Information(String s, int image) {
        this.iconId=image;
        this.title=s;
    }
}
