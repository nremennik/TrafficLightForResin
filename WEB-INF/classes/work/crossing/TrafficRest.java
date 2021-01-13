package work.crossing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ws.rs.*;

// Requirement needs java EE servlet container

@Path("/cars/")
public class TrafficRest
{
    @GET
    @Consumes("*/*")
    @Produces("application/json")
    @Path("{param1}/{param2}")
    public List<Car> getMessage(@PathParam("param1") String from,
                                @PathParam("param2") String to)
    {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime fromDate = LocalDateTime.parse(from, dateTimeFormatter);
        LocalDateTime toDate = LocalDateTime.parse(to, dateTimeFormatter);

        TrafficDao trafficDao = new TrafficDao();
        List<Car> carslist = trafficDao.selectPassedCars(fromDate, toDate);
        return carslist;
    }


    @GET
    @Produces("text/plain")
    public String getMessage()
    {
        StartTrafficLight trafficLight = StartTrafficLight.getInstance();
        // get waiting cars and getCrossedGreenCars
        return ("Hi there!\nIt's GET without parameters. Intended to create new info.");
    }


    @POST
    @Produces("application/json")
    //@Path("hi")
    public String postMessage()
    {
        return ("Hi there!\nIt's post. Intended to create new info.");
    }

    @PUT
    @Produces("text/plain")
    //@Path("hi")
    public String putMessage()
    {
        return ("Hi there!\nIt's put. Intended for update/replace existing info");
    }


    @DELETE
    @Produces("text/plain")
    //@Path("hi")
    public String deleteMessage()
    {
        return ("Hi there!\nIt's delete. Intended to delete existing info.");
    }

    @PATCH
    @Produces("text/plain")
    //@Path("hi")
    public String patchMessage()
    {
        return ("Hi there!\nIt's patch. Intended to partially update existing info.");
    }

    @HEAD
    @Produces("text/plain")
    //@Path("hi")
    public String headMessage()
    {
        return ("Hi there!\nIt's head. Intended to get a part of existing info.");
    }
}
