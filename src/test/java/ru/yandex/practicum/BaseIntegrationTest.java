package ru.yandex.practicum;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.configuration.TestDataSourceConfiguration;
import ru.yandex.practicum.configuration.WebConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WebConfiguration.class, TestDataSourceConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}