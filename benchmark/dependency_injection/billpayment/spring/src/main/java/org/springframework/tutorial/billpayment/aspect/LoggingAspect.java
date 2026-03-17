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
package org.springframework.tutorial.billpayment.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(Logged) || @within(Logged)")
    public Object logMethodEntry(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Entering method: "
                + joinPoint.getSignature().getName() + " in class "
                + joinPoint.getSignature().getDeclaringType().getName());

        return joinPoint.proceed();
    }
}
