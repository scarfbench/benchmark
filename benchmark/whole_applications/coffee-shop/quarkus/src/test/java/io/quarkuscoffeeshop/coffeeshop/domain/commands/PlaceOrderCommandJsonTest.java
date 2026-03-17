package io.quarkuscoffeeshop.coffeeshop.domain.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkuscoffeeshop.coffeeshop.domain.Location;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderSource;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlaceOrderCommandJsonTest {


    @Test
    public void testDeserialization() {
        String json = "{\"id\":\"1e08c459-7e9e-463d-9c19-608688d1a63e\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"name\":\"Jeremy\",\"item\":\"COFFEE_BLACK\",\"price\":3.50}],\"kitchenItems\":[]}";
        String json1 = "{\"commandType\":\"PLACE_ORDER\",\"id\":\"1e08c459-7e9e-463d-9c19-608688d1a63e\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"name\":\"Jeremy\",\"item\":\"COFFEE_BLACK\",\"price\":3.50}],\"kitchenItems\":[]}";

        OrderCommand orderCommand = JsonUtil.fromJson(json1, OrderCommand.class);
        assertNotNull(orderCommand);
        assertEquals("1e08c459-7e9e-463d-9c19-608688d1a63e", orderCommand.id);
        assertEquals(OrderSource.COUNTER, orderCommand.orderSource);
        assertEquals(Location.ATLANTA, orderCommand.location);
        assertEquals("StarshipCaptain", orderCommand.loyaltyMemberId);
        assertNotNull(orderCommand.baristaItems);
        assertEquals(CommandType.PLACE_ORDER, orderCommand.commandType);
    }

    static class OrderCommand{

        final CommandType commandType = CommandType.PLACE_ORDER;
        final String id;
        final OrderSource orderSource;
        final Location location;
        final String loyaltyMemberId;
        final List<CommandItem> baristaItems;
        final List<CommandItem> kitchenItems;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public OrderCommand(
                @JsonProperty("id") final String id,
                @JsonProperty("orderSource") final OrderSource orderSource,
                @JsonProperty("location") final Location location,
                @JsonProperty("loyaltyMemberId") final Optional<String> loyaltyMemberId,
                @JsonProperty("baristaItems") final Optional<List<CommandItem>> baristaItems,
                @JsonProperty("kitchenItems") final Optional<List<CommandItem>> kitchenItems,
                @JsonProperty("commandType") final CommandType commandType
                ) {
            this.id = id;
            this.orderSource = orderSource;
            this.location = location;
            if (loyaltyMemberId.isPresent()) {
                this.loyaltyMemberId = loyaltyMemberId.get();
            }else {
                this.loyaltyMemberId = null;
            }
            if (baristaItems.isPresent()) {
                this.baristaItems = baristaItems.get();
            }else {
                this.baristaItems = null;
            }
            if (kitchenItems.isPresent()) {
                this.kitchenItems = baristaItems.get();
            }else {
                this.kitchenItems = null;
            }
        }
    }
}
