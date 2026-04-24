/*
Copyright 2019- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair;

//import java.util.List;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
//import org.springframework.format.FormatterRegistry;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.validation.MessageCodesResolver;
//import org.springframework.validation.Validator;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
//import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
//import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.quarkus.runtime.Quarkus;

//@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//public class FlightServiceQuarkusApplication implements WebMvcConfigurer {
public class FlightServiceQuarkusApplication {
//	public static void main(String[] args) {
//		SpringApplication.run(FlightServiceQuarkusApplication.class, args);
    public static void main(String... args) {
        Quarkus.run(args);
    }

//	@Override
//	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> arg0) {
//	}

//	@Override
//	public void addCorsMappings(CorsRegistry arg0) {
//	}

//	@Override
//	public void addFormatters(FormatterRegistry arg0) {
//	}

//	@Override
//	public void addInterceptors(InterceptorRegistry arg0) {
//	}

//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry arg0) {
//	}

//	@Override
//	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> arg0) {
//	}

//	@Override
//	public void addViewControllers(ViewControllerRegistry arg0) {
//	}

//	@Override
//	public void configureAsyncSupport(AsyncSupportConfigurer arg0) {
//	}

//	@Override
//	public void configureContentNegotiation(ContentNegotiationConfigurer arg0) {
//	}

//	@Override
//	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer arg0) {
//	}

//	@Override
//	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> arg0) {
//	}

//	@Override
//	public void configureMessageConverters(List<HttpMessageConverter<?>> arg0) {
//	}

//	@Override
//	public void configurePathMatch(PathMatchConfigurer pmc) {
//		pmc.setUseRegisteredSuffixPatternMatch(true);
//	}

//	@Override
//	public void configureViewResolvers(ViewResolverRegistry arg0) {
//	}

//	@Override
//	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> arg0) {
//	}

//	@Override
//	public void extendMessageConverters(List<HttpMessageConverter<?>> arg0) {
//	}

//	@Override
//	public MessageCodesResolver getMessageCodesResolver() {
//		return null;
//	}

//	@Override
//	public Validator getValidator() {
//		return null;
//	}
}
