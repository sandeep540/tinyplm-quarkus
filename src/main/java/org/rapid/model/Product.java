package org.rapid.model;


import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

public record Product(
         String id,
         String name,
         String brand,
         Integer price) {

    public static Uni<Product> findById(PgPool client, String id) {
        return client.preparedQuery("SELECT id, name, brand, price FROM product WHERE id like $1")
                .execute(Tuple.of(id))
                .onItem()
                .transform(m -> m.iterator().hasNext() ? from(m.iterator().next()) : null);
    }

    public static Multi<Product> findAll(PgPool client) {
        return client.query("SELECT * FROM product").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Product::from);
    }

    public static Uni<String> save(PgPool client, Product product) {
        return client
                .preparedQuery("INSERT INTO product (id, name, brand, price) VALUES ($1, $2, $3, $4) RETURNING id")
                .execute(Tuple.of(product.id, product.name, product.brand, product.price))
                .onItem()
                .transform(m -> m.iterator().next().getString("id"));
    }

    public static Uni<Boolean> delete(PgPool client, String id) {
        return client.preparedQuery("DELETE FROM product WHERE id like $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static Product from(Row row) {
        return new Product(row.getString("id"), row.getString("name"), row.getString("brand"), row.getInteger("price"));
    }
}
