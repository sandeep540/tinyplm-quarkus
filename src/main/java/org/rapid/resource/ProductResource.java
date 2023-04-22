package org.rapid.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.rapid.model.Product;

import java.net.URI;

@Path("/api/products")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped

public class ProductResource {

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    private static final Logger LOG = Logger.getLogger(ProductResource.class);

    @GET
    @Path("{id}")
    public Uni<Product> getSingle(String id) {
        return Product.findById(client, id);
    }

    @GET
    public Multi<Product> get() {
        LOG.info("get All method");
        return Product.findAll(client);
    }

    @POST
    public Uni<Response> create(Product product) {
        return Product.save(client, product)
                .onItem()
                .transform(id -> URI.create("/" + id))
                .onItem()
                .transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(String id) {
        return Product.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : RestResponse.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
