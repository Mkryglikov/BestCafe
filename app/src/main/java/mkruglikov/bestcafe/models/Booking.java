package mkruglikov.bestcafe.models;

import org.parceler.Parcel;

@Parcel
public class Booking {
    public Booking() {
    }

    private String id;
    private String userId;
    private long day;
    private long month;
    private long year;
    private long hour;
    private long minute;
    private long people;
    private boolean active;

    public Booking(String id, String userId, long day, long month, long year, long hour, long minute, long people, boolean active) {
        this.id = id;
        this.userId = userId;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.people = people;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getDay() {
        return day;
    }

    public long getMonth() {
        return month;
    }

    public long getYear() {
        return year;
    }

    public long getHour() {
        return hour;
    }

    public long getMinute() {
        return minute;
    }

    public long getPeople() {
        return people;
    }

    public boolean getActive() {
        return active;
    }
}
