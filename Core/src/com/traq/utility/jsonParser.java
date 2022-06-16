package com.traq.utility;

/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 2 Dec, 2015
 * Time: 5:59:05 PM
 * To change this template use File | Settings | File Templates.
 */

import org.json.JSONException;
import org.json.JSONObject;
import com.traq.beanobjects.Route;
import com.traq.beanobjects.Location;
import com.traq.beanobjects.Country;

public class jsonParser {
    private JSONObject jsonResponse;
    private JSONObject json;

    public jsonParser(JSONObject jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public Route getRoute(){
        Route route = new Route();
        JSONObject innerJsonObject;

        //reading inner object from json object
        try{
            innerJsonObject = jsonResponse.getJSONObject("routes");
            route.setStartPoint(innerJsonObject.getJSONObject("legs").getString("start_address"));

        }catch(JSONException je){
            
        }

        return route;
    }

    public Location getLocation(){
        JSONObject json = null;
        Location location = new Location();

        return location;
    }

    public Country getCountry(){
        JSONObject innerJsonObject;
        Country country = new Country();
        //reading inner object from json object
        try{
            innerJsonObject = jsonResponse.getJSONObject("routes");
            country.setName(innerJsonObject.getJSONObject("legs").getString("start_address"));

        }catch(JSONException je){

        }

        return country;
    }


}
