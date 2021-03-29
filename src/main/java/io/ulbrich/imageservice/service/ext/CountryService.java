package io.ulbrich.imageservice.service.ext;

import io.ulbrich.imageservice.model.ext.Country;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/v2")
@RegisterRestClient(configKey = "country-api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public interface CountryService {
    @GET
    @Path("/name/{name}")
    Set<Country> getByName(@PathParam String name);
}
