/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.loader;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.acmeair.service.BookingService;

@ApplicationScoped
public class BookingLoader {

	@Inject
	BookingService bookingService;

	private static Logger logger = Logger.getLogger(BookingLoader.class.getName());

	/**
	 * Delete booking entries.
	 */
	public String clearBookingDb() {
		double length = 0;

		try {
			long start = System.currentTimeMillis();
			logger.info("Start clearing session");
			bookingService.dropBookings();
			long stop = System.currentTimeMillis();
			logger.info("Finished clearing in " + (stop - start) / 1000.0 + " seconds");
			length = (stop - start) / 1000.0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Cleared bookings in " + length + " seconds";
	}
}
