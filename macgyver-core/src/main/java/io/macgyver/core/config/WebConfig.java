package io.macgyver.core.config;

import io.macgyver.core.web.CoreApiController;
import io.macgyver.core.web.HomeController;
import io.macgyver.core.web.MacgyverWeb;
import io.macgyver.core.web.navigation.MenuManager;
import io.macgyver.core.web.navigation.StandardMenuDecorator;
import io.macgyver.core.web.vaadin.MacGyverUI;
import io.macgyver.core.web.vaadin.MacGyverVaadinServlet;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.vaadin.server.VaadinServlet;

@Configuration
@ComponentScan(basePackageClasses = { HomeController.class })
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true,prePostEnabled=true)
public class WebConfig implements EnvironmentAware {

	

	@Autowired
	private final org.springframework.core.io.ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Override
	public void setEnvironment(Environment environment) {

	}

	@Bean
	public CoreApiController macCoreApiController() {
		return new CoreApiController();
	}

	@Bean
	public BeanPostProcessor macHandlerMappingPostProcessor() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean,
					String beanName) throws BeansException {
				if (bean instanceof RequestMappingHandlerMapping
						&& "requestMappingHandlerMapping".equals(beanName)) {
					RequestMappingHandlerMapping m = ((RequestMappingHandlerMapping) bean);
					
					
					
				}

				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean,
					String beanName) throws BeansException {
				return bean;
			}
		};
	}

	@Bean
	public MacgyverWeb macWebConfig() {
		return new MacgyverWeb();
	}

	@Bean
	public MenuManager macMenuManager() {
		return new MenuManager();
	}

	@Bean
	public StandardMenuDecorator macStandardMenuDecorator() {
		return new StandardMenuDecorator();
	}


	@Bean
	public ServletRegistrationBean macVaadinServlet() {
		ServletRegistrationBean sb = new ServletRegistrationBean(new MacGyverVaadinServlet(), "/ui/*","/VAADIN/*");
		sb.addInitParameter("ui", MacGyverUI.class.getName());
		
		return sb;
	}
	
	
}
