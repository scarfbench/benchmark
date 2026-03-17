package org.springframework.tutorial.decorators.aspect;

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
