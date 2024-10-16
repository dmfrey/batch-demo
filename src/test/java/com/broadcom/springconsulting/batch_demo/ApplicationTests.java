package com.broadcom.springconsulting.batch_demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import( TestcontainersConfiguration.class )
@SpringBootTest(
		properties = {
				"spring.batch.job.enabled=false"
		}
)
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
