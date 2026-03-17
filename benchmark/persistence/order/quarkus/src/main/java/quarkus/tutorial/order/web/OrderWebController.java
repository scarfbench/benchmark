package quarkus.tutorial.order.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class OrderWebController {

    @Inject
    OrderController orderController;

    @Inject
    Template orders;

    @Inject
    Template lineItems;

    @GET
    @Path("/orders")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getOrders(@QueryParam("vendorName") String vendorName, @QueryParam("vendorSearchResults") List<String> vendorSearchResults) {
        return orders.data("orders", orderController.getOrders())
                     .data("vendorName", vendorName)
                     .data("vendorSearchResults", vendorSearchResults)
                     .data("findVendorTableDisabled", vendorSearchResults != null);
    }

    @POST
    @Path("/submitOrder")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance submitOrder(
            @FormParam("newOrderId") Integer newOrderId,
            @FormParam("newOrderStatus") String newOrderStatus,
            @FormParam("newOrderDiscount") int newOrderDiscount,
            @FormParam("newOrderShippingInfo") String newOrderShippingInfo) {
        orderController.submitOrder(newOrderId, newOrderStatus.charAt(0), newOrderDiscount, newOrderShippingInfo);
        return orders.data("orders", orderController.getOrders())
                     .data("vendorName", null)
                     .data("vendorSearchResults", null)
                     .data("findVendorTableDisabled", false);
    }

    @POST
    @Path("/removeOrder")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance removeOrder(@FormParam("orderId") Integer orderId) {
        orderController.removeOrder(orderId);
        return orders.data("orders", orderController.getOrders())
                     .data("vendorName", null)
                     .data("vendorSearchResults", null)
                     .data("findVendorTableDisabled", false);
    }

    @GET
    @Path("/lineItems")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLineItems(@QueryParam("orderId") Integer orderId) {
        return lineItems.data("currentOrder", orderId)
                        .data("lineItems", orderController.getLineItems(orderId))
                        .data("newOrderParts", orderController.getNewOrderParts());
    }

    @POST
    @Path("/addLineItem")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance addLineItem(
            @FormParam("currentOrder") Integer currentOrder,
            @FormParam("selectedPartNumber") String selectedPartNumber,
            @FormParam("selectedPartRevision") int selectedPartRevision) {
        orderController.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision);
        return lineItems.data("currentOrder", currentOrder)
                        .data("lineItems", orderController.getLineItems(currentOrder))
                        .data("newOrderParts", orderController.getNewOrderParts());
    }

    @POST
    @Path("/findVendor")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance findVendor(@FormParam("vendorName") String vendorName) {
        return orders.data("orders", orderController.getOrders())
                     .data("vendorName", vendorName)
                     .data("vendorSearchResults", orderController.findVendor(vendorName))
                     .data("findVendorTableDisabled", true);
    }
}