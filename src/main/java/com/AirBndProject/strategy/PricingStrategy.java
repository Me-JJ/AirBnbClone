package com.AirBndProject.strategy;

import com.AirBndProject.entities.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy
{
    BigDecimal calculatePrice(Inventory inventory);
}
