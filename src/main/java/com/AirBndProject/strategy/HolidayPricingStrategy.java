package com.AirBndProject.strategy;

import com.AirBndProject.entities.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy
{
    private final PricingStrategy wrapper;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapper.calculatePrice(inventory);
        boolean isTodayHoliday = true; // TODO: call an api to check if today is holiday or not
        if(isTodayHoliday)
        {
            price =price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }

}
