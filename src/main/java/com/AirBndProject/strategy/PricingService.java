package com.AirBndProject.strategy;

import com.AirBndProject.entities.Inventory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class PricingService
{
    public BigDecimal calculateDynamicPricing(Inventory inventory)
    {
        PricingStrategy pricingStrategy = new BasePriceStrategy();
        log.info("base -> {}",pricingStrategy);
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        log.info("surge -> {}",pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        log.info("occupancy -> {}",pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        log.info("urgency -> {}",pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);
        log.info("holiday -> {}",pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);
    }

    //return the sum of price of this inventory
    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList)
    {
        return inventoryList.stream()
                .map(this::calculateDynamicPricing)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

}
