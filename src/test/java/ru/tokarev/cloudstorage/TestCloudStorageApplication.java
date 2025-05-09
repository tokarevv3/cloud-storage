package ru.tokarev.cloudstorage;

import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.tokarev.cloudstorage.filter.JwtAuthenticationFilter;

@TestConfiguration
@SpringBootTest
public class TestCloudStorageApplication {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return Mockito.mock(JwtAuthenticationFilter.class);
    }
}
