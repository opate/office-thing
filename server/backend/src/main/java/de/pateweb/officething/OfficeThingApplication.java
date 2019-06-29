package de.pateweb.officething;

import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan	
public class OfficeThingApplication {

    @PostConstruct
    void started() {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
	
	public static void main(String[] args) {
        Properties properties = System.getProperties();
        properties.setProperty("java.security.egd", "file:///dev/urandom");
        
		SpringApplication.run(OfficeThingApplication.class, args);
	}

}
