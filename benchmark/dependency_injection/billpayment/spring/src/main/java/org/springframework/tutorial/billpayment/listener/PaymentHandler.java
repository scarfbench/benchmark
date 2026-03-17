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
package org.springframework.tutorial.billpayment.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.tutorial.billpayment.aspect.Logged;
import org.springframework.tutorial.billpayment.event.PaymentEvent;

/**
 * Handler for PaymentEvent.
 */
@Logged
@Component
public class PaymentHandler {

    private static final Logger logger = Logger.getLogger(PaymentHandler.class.getCanonicalName());

    public PaymentHandler() {
        logger.log(Level.INFO, "PaymentHandler created.");
    }

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        if ("Credit".equals(event.getPaymentType())) {
            creditPayment(event);
        } else if ("Debit".equals(event.getPaymentType())) {
            debitPayment(event);
        }
    }

    private void creditPayment(PaymentEvent event) {
        logger.log(Level.INFO, "PaymentHandler - Credit Handler: {0}",
                event.toString());

        // call a specific Credit handler class...
    }

    private void debitPayment(PaymentEvent event) {
        logger.log(Level.INFO, "PaymentHandler - Debit Handler: {0}",
                event.toString());

        // call a specific Debit handler class...
    }
}
