package com.AirBndProject.controller;

import com.AirBndProject.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/webhook")
@RequiredArgsConstructor
@RestController
public class WebhookController
{
    private final BookingService bookingService;

//    @Value("${stripe.webhook.key}")
    private String endPointSecret="whsec_f7d6ad38d2873bc4857b5a2b63381d423cb2c7ab3b6b55696e1876ea34c20eb3";

    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endPointSecret);
            bookingService.capturePayment(event);
            return ResponseEntity.noContent().build();
        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
