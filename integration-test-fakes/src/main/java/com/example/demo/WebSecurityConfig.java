package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/service/local/**").hasRole("NEXUS")
                .antMatchers("/artifactory/**").hasRole("ARTIFACTORY")
                .and()
                .httpBasic();
        http.csrf().disable();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        UserDetails artifactoryUser =
                User.withDefaultPasswordEncoder()
                        .username("xenit-private")
                        .password("xenit-password")
                        .roles("ARTIFACTORY")
                        .build();

        UserDetails nexusUser =
                User.withDefaultPasswordEncoder()
                        .username("maven-central")
                        .password("some-password")
                        .roles("NEXUS")
                        .build();

        return new InMemoryUserDetailsManager(artifactoryUser, nexusUser);
    }
}
