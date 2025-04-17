package com.camel.odoo;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class DynamicERPRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // ✅ Single dynamic REST config
        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")
                .port(port)
                .bindingMode(RestBindingMode.off)
                .dataFormatProperty("prettyPrint", "true");

        // 📥 POST: /erp/sync
        // 📤 GET: /erp/get-data
        rest("/erp")
                .post("/sync")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:start")
                .get("/get-data")
                .produces("application/json")
                .to("direct:getData");

        // 🚨 Global error handler
        onException(Exception.class)
                .log("🚨 Error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody().constant("{\"error\": \"Internal Server Error\"}");

        // 🔁 ERP Sync Handler
        from("direct:start")
                .routeId("erp-dynamic-route")
                .log("Received request: ${body}")
                .process(new com.camel.odoo.processor.DynamicERPProcessor())
                .log("Response: ${body}");

        // 🔍 ERP Data Fetch Handler
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

                    serviceBean.getClass().getMethod("handle", Exchange.class).invoke(serviceBean, exchange);
                })
                .log("📤 Returning data: ${body}");
    }
}
