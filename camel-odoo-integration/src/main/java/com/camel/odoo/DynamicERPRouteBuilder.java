package com.camel.odoo;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class DynamicERPRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Set up REST configuration
        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")
                .port(8080)
                .bindingMode(RestBindingMode.off)
                .dataFormatProperty("prettyPrint", "true");

        // Expose POST endpoint
        rest("/erp")
                .post("/sync")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:start")
                // Expose GET endpoint
                .get("/get-data")
                .produces("application/json")
                .to("direct:getData");

        onException(Exception.class)
                .log("🚨 Error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody().constant("{\"error\": \"Internal Server Error\"}");

        // Main route to handle the ERP JSON dynamically
        from("direct:start")
                .routeId("erp-dynamic-route")
                .log("Received request: ${body}")
                .process(new com.camel.odoo.processor.DynamicERPProcessor())
                .log("Response: ${body}");

        from("direct:getData")
                .routeId("erp-get-data-route")
                .log("📥 Received GET request for ERP: ${header.erp}")
                .process(exchange -> {
                    String erp = exchange.getIn().getHeader("erp", String.class);
                    if (erp == null) {
                        throw new IllegalArgumentException("Missing required 'erp' header");
                    }

                    String beanName = erp.toLowerCase() + "Service";
                    Object serviceBean = exchange.getContext().getRegistry().lookupByName(beanName);

                    if (serviceBean == null) {
                        throw new IllegalArgumentException("No service registered for ERP: " + erp);
                    }

                    // Call the handle method manually (you can cast to a common interface if needed)
                    serviceBean.getClass().getMethod("handle", Exchange.class).invoke(serviceBean, exchange);
                })
                .log("📤 Returning data: ${body}");

    }
}
