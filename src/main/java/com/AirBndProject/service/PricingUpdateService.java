package com.AirBndProject.service;

import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.HotelMinPrice;
import com.AirBndProject.entities.Inventory;
import com.AirBndProject.repository.HotelMinPriceRepository;
import com.AirBndProject.repository.HotelRepository;
import com.AirBndProject.repository.InventoryRepository;
import com.AirBndProject.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingUpdateService
{
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    // scheduler to update the inventory and hotel min price tables every hour

    public void updatePrices()
    {
        int page = 0;
        int batchSize = 100;

        while(true)
        {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if (hotelPage.isEmpty())
            {
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }

    }

    private void updateHotelPrice(Hotel hotel)
    {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        updateInventoryPrice(inventoryList);

        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate)
    {
        //compute min price per day for the hotel
        Map<LocalDate,BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e -> e.getValue().orElse(BigDecimal.ZERO)));

        //prepare hotelPrice entities in bulk
        List<HotelMinPrice> hotelMinPrices = new ArrayList<>();
        dailyMinPrices.forEach(((date, price) ->
        {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));

            hotelPrice.setPrice(price);
            hotelMinPrices.add(hotelPrice);
        }));

        hotelMinPriceRepository.saveAll(hotelMinPrices);

    }

    private void updateInventoryPrice(List<Inventory> inventoryList)
    {
        inventoryList.forEach(inventory -> {
            BigDecimal decimalPrice=pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(decimalPrice);
        });

        inventoryRepository.saveAll(inventoryList);
    }
}
