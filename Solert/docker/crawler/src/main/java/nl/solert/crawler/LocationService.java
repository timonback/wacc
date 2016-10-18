package nl.solert.crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kenai.jffi.Array;

public class LocationService
{
    private static final String GMAPS_URL =
                                          "http://maps.googleapis.com/maps/api/geocode/json?address=";

    public Location getLocation(String locationName)
    {
        String response = "";
        try
        {
            URL url = new URL(getLocationServiceUrl(locationName));
            response = IOUtils.toString(url, Charsets.UTF_8);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JsonObject locationInfo = new JsonParser().parse(response).getAsJsonObject();

        JsonArray resultsObj = locationInfo.get("results").getAsJsonArray();
        if (0 < resultsObj.size())
        {
            JsonObject firstResult = resultsObj.get(0).getAsJsonObject();
            JsonObject geometryObj = firstResult.get("geometry").getAsJsonObject();
            JsonObject locationObj = geometryObj.get("location").getAsJsonObject();
            double lat = locationObj.get("lat").getAsDouble();
            double lng = locationObj.get("lng").getAsDouble();

            Location location = new Location(locationName, lat, lng);
            return location;
        }
        else
        {
            System.err.println("Unknown location: " + locationName);
            return new Location("invalid", 0, 0);
        }
    }

    public static List<String> getDefaultLocations()
    {
        List<String> locations = new ArrayList<String>();
        locations.add("Amsterdam");
        locations.add("Rotterdam");
        locations.add("The Hague");
        locations.add("Utrecht");
        locations.add("Eindhoven");
        locations.add("Tilburg");
        locations.add("Groningen");
        locations.add("Almere");
        locations.add("Breda");
        locations.add("Nijmegen");
        locations.add("Apeldoorn");
        locations.add("Haarlem");
        locations.add("Amersdfoort");
        locations.add("Arnhem");
        locations.add("Yaanstad");
        locations.add("'s-Hertogenbosch");
        locations.add("Haarlemmermeer");
        locations.add("Zwolle");
        locations.add("Zoetermeer");
        return locations;
    }

    private String getLocationServiceUrl(String location) throws UnsupportedEncodingException
    {
        return GMAPS_URL + URLEncoder.encode(location, "UTF-8");
    }
}
