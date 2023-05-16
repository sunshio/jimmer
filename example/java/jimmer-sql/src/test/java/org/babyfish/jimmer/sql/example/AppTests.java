package org.babyfish.jimmer.sql.example;

import io.swagger.v3.core.jackson.ModelResolver;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.example.model.BookDraft;
import org.babyfish.jimmer.sql.example.model.input.BookInput;
import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

@SpringBootTest
public class AppTests {

    @Autowired
    private JSqlClient sqlClient;

    @Test
    public void test() {
        sqlClient.getEntities().save(BookDraft.$.produce(book->{
            book.setName("11");
            book.setEdition(1);
            book.setPrice(null);
            book.setAuthorIds(new ArrayList<>());
//            book.set
        }));
    }
}
