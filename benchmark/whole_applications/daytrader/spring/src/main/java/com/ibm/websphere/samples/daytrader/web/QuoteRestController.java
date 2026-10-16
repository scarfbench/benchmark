package com.ibm.websphere.samples.daytrader.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Spring equivalent of the Jakarta JAX-RS QuoteResource at /rest/quotes.
 */
@RestController
@RequestMapping("/rest/quotes")
public class QuoteRestController {

    private final ApplicationContext applicationContext;

    public QuoteRestController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/{symbols}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuoteDataBean> quotesGet(@PathVariable("symbols") String symbols) {
        return getQuotes(symbols);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuoteDataBean> quotesPost(@RequestParam("symbols") String symbols) {
        return getQuotes(symbols);
    }

    private List<QuoteDataBean> getQuotes(String symbols) {
        List<QuoteDataBean> quotes = new ArrayList<>();
        try {
            String key = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
            TradeServices tradeService = applicationContext.getBean(key, TradeServices.class);
            for (String symbol : symbols.split(",")) {
                quotes.add(tradeService.getQuote(symbol));
            }
        } catch (Exception e) {
            Log.error(e, "QuoteRestController.getQuotes");
        }
        return quotes;
    }
}
