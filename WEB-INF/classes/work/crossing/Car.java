package work.crossing;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class Car
{
    String source = null;
    String destination; // name of the destination road
    String name; // License plate
    boolean violator = false; // Is it violator? Violators will run on red;

    static final int VIOLATION_PROBABILITY = 10;

    LocalDateTime arrived = null;
    LocalDateTime crossed = null;

    private Car()
    {
    }

    public Car(String name, String destination)
    {
        this.name = name;
        this.destination = destination;
        if (ThreadLocalRandom.current().nextInt(0, VIOLATION_PROBABILITY) == 0) // Violation probability
        {
            violator = true;
        }
    }

    public String getDestination()
    {
        return (destination);
    }

    public java.lang.String getName()
    {
        return (name);
    }

    public LocalDateTime getArrived()
    {
        return (arrived);
    }

    public void setArrived(LocalDateTime arrived)
    {
        this.arrived = arrived;
    }

    public LocalDateTime getCrossed()
    {
        return (crossed);
    }

    public void setCrossed(LocalDateTime crossed)
    {
        this.crossed = crossed;
    }

    public boolean isViolator()
    {
        return (violator);
    }

    public void setViolator(boolean violator)
    {
        this.violator = violator;
    }

    public String getSource()
    {
        return (source);
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String toString()
    {
        return ("Car: " + getName() + " from " + getSource() + ", to " + getDestination() +
                (isViolator()?" (violator!) ":". ") +
                "Arrived at " + getArrived() + ", crossed at " + getCrossed());
    }
}
