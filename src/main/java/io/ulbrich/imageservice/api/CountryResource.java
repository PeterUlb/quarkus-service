package io.ulbrich.imageservice.api;

import io.ulbrich.imageservice.model.ext.Country;
import io.ulbrich.imageservice.service.ext.CountryService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public Set<Country> name(@PathParam String name) {
        return countryService.getByName(name);
    }
}
