package net.metisapp.metisapi;

import io.sentry.Sentry;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetisApplication{

    public static void main(String[] args) {
		SpringApplication.run(MetisApplication.class, args);
	}

}
