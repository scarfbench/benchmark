package com.example.orderspring.web;

import com.example.orderspring.entity.CustomerOrder;
import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.Part;
import com.example.orderspring.entity.Vendor;
import com.example.orderspring.service.OrderService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;

@Named("orderManager")
@SessionScoped
public class OrderManager implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private OrderService orderService;

    private List<CustomerOrder> orders;
    private List<LineItem> lineItems;
    private List<Part> parts;
    private List<Vendor> vendorSearchResults;

    private Integer currentOrder;
    private Integer newOrderId;
    private String newOrderShippingInfo;
    private Character newOrderStatus = 'P';
    private Integer newOrderDiscount = 0;
    private String vendorName;
    private boolean findVendorTableDisabled = true;
    
    private List<Part> newOrderParts;
    private String selectedPartNumber;
    private Integer selectedPartRevision;
    private Long selectedVendorPartNumber;

    public List<CustomerOrder> getOrders() {
        if (orders == null) {
            orders = orderService.getAllOrders();
        }
        return orders;
    }
    public void setOrders(List<CustomerOrder> orders) { this.orders = orders; }

    public List<LineItem> getLineItems() {
        if (lineItems == null && currentOrder != null) {
            lineItems = orderService.getLineItemsByOrderId(currentOrder);
        }
        return lineItems;
    }
    public void setLineItems(List<LineItem> lineItems) { this.lineItems = lineItems; }

    public List<Part> getParts() {
        if (parts == null) {
            parts = orderService.getAllParts();
        }
        return parts;
    }
    public void setParts(List<Part> parts) { this.parts = parts; }

    public List<Vendor> getVendorSearchResults() { return vendorSearchResults; }
    public void setVendorSearchResults(List<Vendor> vendorSearchResults) { this.vendorSearchResults = vendorSearchResults; }

    public Integer getCurrentOrder() { return currentOrder; }
    public void setCurrentOrder(Integer currentOrder) {
        this.currentOrder = currentOrder;
        this.lineItems = null;
    }

    public Integer getNewOrderId() { return newOrderId; }
    public void setNewOrderId(Integer newOrderId) { this.newOrderId = newOrderId; }

    public String getNewOrderShippingInfo() { return newOrderShippingInfo; }
    public void setNewOrderShippingInfo(String newOrderShippingInfo) { this.newOrderShippingInfo = newOrderShippingInfo; }

    public Character getNewOrderStatus() { return newOrderStatus; }
    public void setNewOrderStatus(Character newOrderStatus) { this.newOrderStatus = newOrderStatus; }

    public Integer getNewOrderDiscount() { return newOrderDiscount; }
    public void setNewOrderDiscount(Integer newOrderDiscount) { this.newOrderDiscount = newOrderDiscount; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public boolean isFindVendorTableDisabled() { return findVendorTableDisabled; }
    public void setFindVendorTableDisabled(boolean findVendorTableDisabled) { this.findVendorTableDisabled = findVendorTableDisabled; }

    public List<Part> getNewOrderParts() {
        if (newOrderParts == null) {
            newOrderParts = orderService.getAllParts();
        }
        return newOrderParts;
    }
    public void setNewOrderParts(List<Part> newOrderParts) { this.newOrderParts = newOrderParts; }

    public String getSelectedPartNumber() { return selectedPartNumber; }
    public void setSelectedPartNumber(String selectedPartNumber) { this.selectedPartNumber = selectedPartNumber; }

    public Integer getSelectedPartRevision() { return selectedPartRevision; }
    public void setSelectedPartRevision(Integer selectedPartRevision) { this.selectedPartRevision = selectedPartRevision; }

    public Long getSelectedVendorPartNumber() { return selectedVendorPartNumber; }
    public void setSelectedVendorPartNumber(Long selectedVendorPartNumber) { this.selectedVendorPartNumber = selectedVendorPartNumber; }

    public String submitOrder() {
        try {
            if (newOrderId == null) return "order";
            char status = (newOrderStatus != null ? newOrderStatus : 'P');
            int discount = (newOrderDiscount != null ? newOrderDiscount : 0);
            orderService.createOrder(newOrderId, status, discount, newOrderShippingInfo);
            newOrderId = null;
            newOrderShippingInfo = null;
            newOrderStatus = 'P';
            newOrderDiscount = 0;
            orders = null;
            if (currentOrder != null) lineItems = null;
            return "order";
        } catch (Exception e) {
            e.printStackTrace();
            return "order";
        }
    }

    public String removeOrder() {
        try {
            if (currentOrder != null) {
                orderService.removeOrder(currentOrder);
                orders = null;
                lineItems = null;
                currentOrder = null;
            }
            return "order";
        } catch (Exception e) {
            e.printStackTrace();
            return "order";
        }
    }

    public void removeOrder(ActionEvent event) {
        String id = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("deleteOrderId");
        if (id != null && !id.isBlank()) {
            Integer orderId = Integer.valueOf(id);
            orderService.removeOrder(orderId);
            orders = null;
            lineItems = null;
            if (orderId.equals(currentOrder)) currentOrder = null;
        }
    }

    public String findVendor() {
        try {
            vendorSearchResults = orderService.findVendorsByName(vendorName);
            findVendorTableDisabled = false;
            return "order";
        } catch (Exception e) {
            e.printStackTrace();
            findVendorTableDisabled = true;
            return "order";
        }
    }

    public String addLineItem() {
        try {
            if (currentOrder != null && selectedPartNumber != null && selectedPartRevision != null) {
                orderService.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision, 1);
                lineItems = null;
            }
            return "lineItem";
        } catch (Exception e) {
            e.printStackTrace();
            return "lineItem";
        }
    }

    public String lineItem() { return "lineItem"; }
    public String order() { return "order"; }
}
