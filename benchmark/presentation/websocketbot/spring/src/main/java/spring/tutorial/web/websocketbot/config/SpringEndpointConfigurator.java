package spring.tutorial.web.websocketbot.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringEndpointConfigurator extends ServerEndpointConfig.Configurator
    implements ApplicationContextAware {

  private static AutowireCapableBeanFactory beanFactory;

  @Override
  public void setApplicationContext(ApplicationContext ctx) {
    beanFactory = ctx.getAutowireCapableBeanFactory();
  }

  @Override
  public <T> T getEndpointInstance(Class<T> clazz) {
    return beanFactory.createBean(clazz); 
  }
}
