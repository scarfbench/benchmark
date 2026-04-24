/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.loader;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acmeair.service.BookingService;

@Component
public class BookingLoader {

	@Autowired
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