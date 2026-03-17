/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.springframework.tutorial.billpayment.controller;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.tutorial.billpayment.aspect.Logged;
import org.springframework.tutorial.billpayment.payment.PaymentService;
import org.springframework.tutorial.billpayment.payment.PaymentType;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Digits;

/**
 * Web controller that handles payment requests and delegates to PaymentService.
 * Check server log output for event handling output.
 */
@Controller
@Validated
public class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getCanonicalName());

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("paymentOption", 1); // Default to DEBIT
        model.addAttribute("value", BigDecimal.ZERO);
        return "index";
    }

    /**
     * Processes a payment.
     *
     * @return the response page location
     */
    @PostMapping("/pay")
    @Logged
    public String pay(@RequestParam("paymentOption") int paymentOption,
            @RequestParam("value") @Digits(integer = 10, fraction = 2, message = "Invalid value") BigDecimal value,
            Model model) {

        PaymentType paymentType = (paymentOption == 1) ? PaymentType.DEBIT : PaymentType.CREDIT;
        String result = paymentService.processPayment(paymentType, value);

        model.addAttribute("result", result);
        model.addAttribute("paymentType", paymentType.name());
        model.addAttribute("value", value);

        return "response";
    }

    /**
     * Resets the values in the index page.
     */
    @PostMapping("/reset")
    @Logged
    public String reset(Model model) {
        model.addAttribute("paymentOption", 1); // Default to DEBIT
        model.addAttribute("value", BigDecimal.ZERO);
        return "index";
    }
}
