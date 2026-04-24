/*******************************************************************************
* Copyright (c) 2019 IBM Corp.
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
package com.acmeair.web;

import javax.annotation.PostConstruct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.FlightClient;

@Component
public class RewardTracker {


  @Autowired
  private CustomerClient customerClient;
  
  @Autowired
  private FlightClient flightClient; 
 
    
  @Value("${track.reward.miles:true}")
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
