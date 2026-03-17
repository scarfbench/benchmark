package io.quarkuscoffeeshop.coffeeshop.utils;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.Location;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderSource;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.CommandItem;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.CommandType;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class TestUtils {

    public static PlaceOrderCommand mockPlaceOrderCommand() {

        return new PlaceOrderCommand(
                UUID.randomUUID().toString(),
                OrderSource.COUNTER,
                Location.ATLANTA,
                Optional.of("StarshipCaptain"),
                Optional.of(Arrays.asList(new CommandItem(Item.COFFEE_BLACK, "Kirk", BigDecimal.valueOf(3.00)))),
                Optional.empty(), CommandType.PLACE_ORDER);
    }

}
