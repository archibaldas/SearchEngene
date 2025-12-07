package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.services.CleanUpService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanUpServiceImpl implements CleanUpService {

    private final JdbcTemplate jdbcTemplate;
    private static final String SET_FOREIGN_KEY_CHECKS_COMMAND = "SET FOREIGN_KEY_CHECKS = ";
    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE ";

    private List<String> getTableNames () {
        return jdbcTemplate.queryForList("SHOW TABLES", String.class);
    }

    @Override
    @Transactional
    public void cleanUpAllTablesInDatabase() {
        List<String> tables = getTableNames();
        if(tables.isEmpty()){
            log.info("В базе нет данных, очистка не требуется");
            return;
        }
        log.info("Очистка базы данных перед индексацией.");
        jdbcTemplate.execute(SET_FOREIGN_KEY_CHECKS_COMMAND + 0);

        for(String tableName : tables){
            jdbcTemplate.execute(TRUNCATE_TABLE + "`" + tableName + "`");
        }

        jdbcTemplate.execute(SET_FOREIGN_KEY_CHECKS_COMMAND + 1);
        log.info("База данных очищена");
    }
}
