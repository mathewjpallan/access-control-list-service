package com.tarento.acl;

import com.tarento.acl.common.AccessControlManager;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AclApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(AclApplication.class, args);
		AccessControlManager.init();
	}

	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

}
