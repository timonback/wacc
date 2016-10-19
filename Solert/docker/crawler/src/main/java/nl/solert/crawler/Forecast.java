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

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 *
 * @author loetermann
 */
@Table
public class Forecast {

    @PartitionKey
    @Column
    private String location;

    @ClusteringColumn(value = 1, asc = false)
    private Date datetime;

    @Column
    private int value;

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public Forecast() throws ParseException {
        this("Earth", "1970-1-1T00:00:00", 0);
    }

    public Forecast(String location, String datetime, int value) throws ParseException {
        this.location = location;
        this.datetime = TIME_FORMAT.parse(datetime);
        this.value = value;
    }

    public String getLocation() {
        return location;
    }

    public Date getDatetime() {
        return datetime;
    }

    public int getValue() {
        return value;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "{" + getLocation() + " at " + getDatetime() + ": " + getValue() + "}";
    }

}
