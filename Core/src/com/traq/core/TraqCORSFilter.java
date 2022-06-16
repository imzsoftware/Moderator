package com.traq.core;

import com.sun.jersey.core.util.Priority;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

/**
 * Created by Amit on 10/10/18.
 */
@Provider
public class TraqCORSFilter implements ContainerResponseFilter{

    public ContainerResponse filter(ContainerRequest req, ContainerResponse traqContainerResponse) {

        ResponseBuilder traqResponseBuilder = Response.fromResponse(traqContainerResponse.getResponse());

        // *(allow from all servers)
        traqResponseBuilder.header("Access-Control-Allow-Origin", "*")

                // As a part of the response to a request, which HTTP methods can be used during the actual request.
                .header("Access-Control-Allow-Methods", "API, GET, POST, PUT, UPDATE, OPTIONS")

                // How long the results of a request can be cached in a result cache.
                .header("Access-Control-Max-Age", "151200")

                // As part of the response to a request, which HTTP headers can be used during the actual request.
                .header("Access-Control-Allow-Headers", "x-requested-with,Content-Type");

        String traqRequestHeader = req.getHeaderValue("Access-Control-Request-Headers");

        if (null != traqRequestHeader && !traqRequestHeader.equals(null)) {
            traqResponseBuilder.header("Access-Control-Allow-Headers", traqRequestHeader);
        }

        traqContainerResponse.setResponse(traqResponseBuilder.build());
        return traqContainerResponse;
    }



}
