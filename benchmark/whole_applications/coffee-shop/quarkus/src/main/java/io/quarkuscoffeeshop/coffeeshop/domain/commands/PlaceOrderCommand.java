package io.quarkuscoffeeshop.coffeeshop.domain.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkuscoffeeshop.coffeeshop.domain.Location;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class PlaceOrderCommand {

    private final CommandType commandType = CommandType.PLACE_ORDER;

    private final String id;

    private final OrderSource orderSource;

    private final Location location;

    private final String loyaltyMemberId;

    private final List<CommandItem> baristaItems;

    private final List<CommandItem> kitchenItems;

    private final Instant timestamp;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PlaceOrderCommand(
            @JsonProperty("id") final String id,
            @JsonProperty("orderSource") final OrderSource orderSource,
            @JsonProperty("storeId") final Location location,
            @JsonProperty("rewardsId") final Optional<String> loyaltyMemberId,
            @JsonProperty("baristaItems") Optional<List<CommandItem>> baristaItems,
            @JsonProperty("kitchenItems") Optional<List<CommandItem>> kitchenItems,
            @JsonProperty("commandType") final CommandType commandType) {
        this.id = id;
        this.orderSource = orderSource;
        this.location = location;
        if (loyaltyMemberId.isPresent()) {
            this.loyaltyMemberId = loyaltyMemberId.get();
        }else{
            this.loyaltyMemberId = null;
        }
        if (baristaItems.isPresent()) {
            this.baristaItems = baristaItems.get();
        }else{
            this.baristaItems = null;
        }
        if (kitchenItems.isPresent()) {
            this.kitchenItems = kitchenItems.get();
        }else{
            this.kitchenItems = null;
        }
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "PlaceOrderCommand{" +
                "id='" + id + '\'' +
                ", orderSource=" + orderSource +
                ", location=" + location +
                ", loyaltyMemberId='" + loyaltyMemberId + '\'' +
                ", baristaItems=" + baristaItems +
                ", kitchenItems=" + kitchenItems +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceOrderCommand that = (PlaceOrderCommand) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (orderSource != that.orderSource) return false;
        if (location != that.location) return false;
        if (loyaltyMemberId != null ? !loyaltyMemberId.equals(that.loyaltyMemberId) : that.loyaltyMemberId != null)
            return false;
        if (baristaItems != null ? !baristaItems.equals(that.baristaItems) : that.baristaItems != null)
            return false;
        if (kitchenItems != null ? !kitchenItems.equals(that.kitchenItems) : that.kitchenItems != null)
            return false;
        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (loyaltyMemberId != null ? loyaltyMemberId.hashCode() : 0);
        result = 31 * result + (baristaItems != null ? baristaItems.hashCode() : 0);
        result = 31 * result + (kitchenItems != null ? kitchenItems.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    public Optional<List<CommandItem>> getBaristaItems() {
        return Optional.ofNullable(baristaItems);
    }

    public Optional<List<CommandItem>> getKitchenItems() {
        return Optional.ofNullable(kitchenItems);
    }

    public Optional<String> getLoyaltyMemberId() {
        return Optional.ofNullable(loyaltyMemberId);
    }

    public String getId() {
        return id;
    }

    public OrderSource getOrderSource() {
        return orderSource;
    }

    public Location getLocation() {
        return location;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
