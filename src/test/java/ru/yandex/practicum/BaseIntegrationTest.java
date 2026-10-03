package ru.yandex.practicum;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.configuration.TestDataSourceConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WebConfiguration.class, TestDataSourceConfiguration.class})
@WebAppConfiguration
public abstract class BaseIntegrationTest {
}