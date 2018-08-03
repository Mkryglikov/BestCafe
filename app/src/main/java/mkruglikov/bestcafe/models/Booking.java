package mkruglikov.bestcafe.models;

public class Booking {

    private final String id;
    private final String userId;
    private final long day;
    private final long month;
    private final long year;
    private final long hour;
    private final long minute;
    private final long people;
    private final boolean active;

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
