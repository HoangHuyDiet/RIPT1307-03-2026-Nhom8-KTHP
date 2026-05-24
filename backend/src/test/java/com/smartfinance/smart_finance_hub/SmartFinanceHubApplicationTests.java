package com.smartfinance.smart_finance_hub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.profiles.active=test",
		"spring.datasource.url=jdbc:h2:mem:smart_finance_hub_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;NON_KEYWORDS=MONTH",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.properties.hibernate.dialect=",
		"spring.jpa.hibernate.ddl-auto=update",
		"spring.jpa.show-sql=false",
		"spring.jpa.properties.hibernate.format_sql=false",
		"logging.level.org.hibernate.SQL=OFF",
		"logging.level.org.springframework=INFO",
		"debug=false"
})
class SmartFinanceHubApplicationTests {

	@Test
	void contextLoads() {
	}

}
