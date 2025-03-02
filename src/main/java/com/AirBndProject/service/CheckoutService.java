package com.AirBndProject.service;

import com.AirBndProject.entities.Booking;

public interface CheckoutService
{
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
