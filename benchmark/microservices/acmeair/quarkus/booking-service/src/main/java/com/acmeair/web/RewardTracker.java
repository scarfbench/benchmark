/*
Copyright 2019- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.FlightClient;

@ApplicationScoped
public class RewardTracker {


  @Inject
  private CustomerClient customerClient;
  
  @Inject
  private FlightClient flightClient; 
 
    
  @ConfigProperty(name = "track.reward.miles", defaultValue = "true")
  private boolean TRACK_REWARD_MILES;

  @PostConstruct
  private void initialize() {
    System.out.println("TRACK_REWARD_MILES: " + TRACK_REWARD_MILES);
  }
    
  public boolean trackRewardMiles() {
    return TRACK_REWARD_MILES;
  }
  
  /**
   * Update rewards.
   */
  public Long updateRewardMiles(String userid, String flightSegId, boolean add) {       
   
      Long miles = flightClient.getRewardMiles(userid, flightSegId, add);   
           
      Long totalMiles = customerClient.updateTotalMiles(userid, miles.toString());
      
     
      return totalMiles;
  }
}
