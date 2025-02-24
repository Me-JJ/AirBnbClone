package com.AirBndProject.strategy;

import com.AirBndProject.entities.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public class BasePriceStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}
