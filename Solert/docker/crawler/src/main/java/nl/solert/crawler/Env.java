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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author loetermann
 */
public enum Env {

    UPDATE_TIME("30000"),
    KEYSPACE("SOLERT"),
    KEYSPACE_REPLICATION("{'class':'SimpleStrategy', 'replication_factor':1}"),
    URL_24H("http://api.buienradar.nl/data/graphdata/1.0/sunforecast/24hours"),
    URL_3H("http://graphdata.buienradar.nl/forecast/jsonsun/");

    private final String defaultValue;

    private Env(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        try {
            String env = System.getenv(name());
            return env == null ? getDefaultValue() : env;
        } catch (SecurityException ex) {
            Logger.getLogger(Env.class.getName()).log(Level.WARNING,
                    "Could not access environment variable, returning default", ex);
            return getDefaultValue();
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
