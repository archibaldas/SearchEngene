package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.services.CleanUpService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanUpServiceImpl implements CleanUpService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void cleanUpAllTablesInDatabase() {
        Integer count  = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM site;", Integer.class);

        if (count == null || count == 0) {
            log.info("База данных уже пуста, очистка не требуется");
            return;
        }

        log.info("Найдено {} сайтов, начинаем очистку...", count);

        jdbcTemplate.execute("TRUNCATE TABLE site, page, lemma, index RESTART IDENTITY CASCADE");

        log.info("База данных очищена, ID сброшены");
    }
}
