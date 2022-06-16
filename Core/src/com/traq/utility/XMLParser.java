package com.traq.utility;

import com.traq.beanobjects.Route;
import com.traq.beanobjects.Location;
import com.traq.beanobjects.Country;

/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 2 Dec, 2015
 * Time: 5:44:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLParser {

    private String response;

    public XMLParser() {
    }

    public XMLParser(String response) {
        this.response = response;
    }

    public Route getRoute(){
        Route route = new Route();

        return route;

    }

    public Location getLocation(){
        Location location = new Location();

        return location;

    }


    public Country getCountry(){
        Country country = new Country();

        return country;
    }
}
