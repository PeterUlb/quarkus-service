package io.ulbrich.imageservice.api;

import io.ulbrich.imageservice.model.ext.Country;
import io.ulbrich.imageservice.service.ext.CountryService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/country")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CountryResource {
    @Inject
    @RestClient
    CountryService countryService;

    @GET
    @Path("/{name}")
    public Set<Country> name(@PathParam("name") String name) {
        return countryService.getByName(name);
    }
}
