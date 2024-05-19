package com.group7.dbms;

import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.SessionFactory;

import spark.Spark;


public class Main {

    private static Gson partialRepresentationGson;
    private static Gson fullRepresentationGson;

    private static ProductsDAO productsDAO;

    public static void main(String[] args)
    throws Exception {
        partialRepresentationGson = setUpGson(RepresentationType.PARTIAL);
        fullRepresentationGson = setUpGson(RepresentationType.FULL);
        SessionFactory sessionFactory = setUpSessionFactory();
        productsDAO = new HibernateProductsDAO(sessionFactory);
        ProductController productController = new ProductController(
            productsDAO,
            fullRepresentationGson::fromJson
        );

        Spark.get("/products/", productController::getAllProducts, partialRepresentationGson::toJson);
        Spark.redirect.get("/products", "/products/");
        Spark.get("/products/:product-id", productController::getByID, fullRepresentationGson::toJson);

        Spark.post("/products/", productController::save, fullRepresentationGson::toJson);
        Spark.redirect.post("/products", "/products/");

        Spark.put("/products/:product-id", productController::update);

        Spark.delete("/products/:product-id", productController::remove);

        Spark.after((req, res) -> {
            res.type("application/json");
        });

        Spark.awaitInitialization();
    }

    private static SessionFactory setUpSessionFactory()
    throws Exception {
        SessionFactory sessionFactory;
        final StandardServiceRegistry registry =
            new StandardServiceRegistryBuilder()
            .build();
        try {
            sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(Bakery.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Employee.class)
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Product.class)
                .addAnnotatedClass(Recipe.class)
                .addAnnotatedClass(Feedback.class)
                .buildMetadata()
                .buildSessionFactory();
        } catch (Exception exception) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw(exception);
        }
        return sessionFactory;
    }

    private static Gson setUpGson(RepresentationType type) {
        return new GsonBuilder()
            .setExclusionStrategies(new RepresentationExclusionStrategy(type))
            .create();
    }

    // private static void closeSessionFactory() {
    //     if (sessionFactory != null)
    //         sessionFactory.close();
    // }

}
