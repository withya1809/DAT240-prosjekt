package no.uis.imagegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The spring application entry point.
 */
@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories("no.uis.repositories")
@ComponentScan({
        "no.uis.websocket",
        "no.uis.imagegame",
        "no.uis.players",
        "no.uis.party",
        "no.uis.configs",
})
public class ImagegameApplication implements WebMvcConfigurer {

    /**
     * Main entry point method
     */
    public static void main(String[] args) {
        SpringApplication.run(ImagegameApplication.class, args);
    }

    /**
     * Tells SpringBoot where our resource folder is located.
     * @param registry Automated SpringBoot registry used to configure resource
     *                 handlers.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

}
