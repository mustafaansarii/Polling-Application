package com.polling.polling_app_server;

import com.example.polls.PollsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PollsApplication.class)  // Explicitly specify the main application class
public class PollingAppServerApplicationTests {

	@Test
	void contextLoads() {
	}
}
