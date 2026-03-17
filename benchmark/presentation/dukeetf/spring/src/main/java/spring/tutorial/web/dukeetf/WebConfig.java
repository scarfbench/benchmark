package spring.tutorial.web.dukeetf;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

  @Bean
  public DukeETFServlet dukeETFServlet(PriceVolumeBean priceVolumeBean) {
    return new DukeETFServlet(priceVolumeBean);
  }

  @Bean
  public ServletRegistrationBean<DukeETFServlet> dukeETFServletRegistration(DukeETFServlet servlet) {
    return new ServletRegistrationBean<>(servlet, "/dukeetf");
  }
}
