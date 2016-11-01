/*
 * Copyright (C) 2016 loetermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.solert.crawler;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.generated.manager.Forecast_Manager;
import info.archinnov.achilles.generated.manager.Location_Manager;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author loetermann
 */
public class Crawler
{

    public static String URL_24H, URL_3H;
    private final LocationService locationService = new LocationService();

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            String keyspace = Env.KEYSPACE.getValue();
            String keyspaceReplication = Env.KEYSPACE_REPLICATION.getValue();
            long updateTime = Long.parseLong(Env.UPDATE_TIME.getValue());
            URL_3H = Env.URL_3H.getValue();
            URL_24H = Env.URL_24H.getValue();

            Crawler crawler = new Crawler(keyspace, keyspaceReplication, args);
            while (true)
            {
                long startTime = System.currentTimeMillis();
                crawler.updateForecasts();
                long timeNeeded = System.currentTimeMillis() - startTime;
                if (timeNeeded < updateTime)
                {
                    Thread.sleep(updateTime - timeNeeded);
                }
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private Forecast_Manager forecastManager;
    private Location_Manager locationManager;

    public Crawler(String keyspace, String keyspaceReplication, String... cassandraAddresses)
    {
        Cluster cluster = Cluster.builder().addContactPoints(cassandraAddresses).build();
        try
        {
            connectToCassandra(keyspace, keyspaceReplication, cluster);
        }
        catch (InvalidQueryException ex)
        {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Crawler.class.getName())
                  .log(Level.INFO, "Trying to create keyspace {0}", keyspace);
            cluster.connect().execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
                            + " WITH replication = " + keyspaceReplication + ";");

            connectToCassandra(keyspace, keyspaceReplication, cluster);
        }
    }

    private void connectToCassandra(String keyspace, String keyspaceReplication, Cluster cluster)
    {
        Logger.getLogger(Crawler.class.getName()).log(Level.INFO, "Connecting to cassandra...");
        ManagerFactory managerFactory = ManagerFactoryBuilder.builder(cluster)
                                                             .withDefaultKeyspaceName(keyspace)
                                                             .doForceSchemaCreation(true)
                                                             .build();
        forecastManager = managerFactory.forForecast();
        locationManager = managerFactory.forLocation();

        for (String locationName : LocationService.getDefaultLocations())
        {
            Location location = locationService.getLocation(locationName);
            if (location.getLat() != 0 && location.getLon() != 0)
            {
                locationManager.crud().insert(location).execute();
            }
        }

        Logger.getLogger(Crawler.class.getName()).log(Level.INFO, "Connection established");
    }

    public void updateForecasts()
    {
        System.out.println("[" + new Date() + "] Updating forecasts for all cities ...");
        Iterator<Location> it = locationManager.dsl()
                                               .select()
                                               .allColumns_FromBaseTable()
                                               .without_WHERE_Clause()
                                               .iterator();
        while (it.hasNext())
        {
            updateForecastForLocation(it.next());
        }
        System.out.println("[" + new Date() + "] Done.");
    }

    public void updateForecastForLocation(Location location)
    {
        System.out.print("[" + new Date() + "]    Updating forecast for: " + location.getName()
                        + " ");
        for (Forecast forecast : getSunForecast(location, false))
        {
            forecastManager.crud().insert(forecast).execute();
            System.out.print(".");
        }
        for (Forecast forecast : getSunForecast(location, true))
        {
            forecastManager.crud().insert(forecast).execute();
            System.out.print(".");
        }
        System.out.println(" Done.");
    }

    public Forecast[] getSunForecast(Location location, boolean threeHourForecast)
    {
        try
        {
            URL url = new URL((threeHourForecast ? URL_3H : URL_24H) + "?lat=" + location.getLat()
                            + "&lon=" + location.getLon());
            String response = IOUtils.toString(url, Charsets.UTF_8);
            JsonArray forecasts = new JsonParser().parse(response)
                                                  .getAsJsonObject()
                                                  .get("forecasts")
                                                  .getAsJsonArray();
            Forecast[] result = new Forecast[forecasts.size()];
            for (int i = 0; i < result.length; i++)
            {
                String datetime = forecasts.get(i).getAsJsonObject().get("utcdatetime").getAsString();
                int value = forecasts.get(i).getAsJsonObject().get("value").getAsInt();
                result[i] = new Forecast(location.getName(), datetime, value);
            }
            return result;
        }
        catch (IOException | ParseException ex)
        {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Forecast[0];
    }

}
