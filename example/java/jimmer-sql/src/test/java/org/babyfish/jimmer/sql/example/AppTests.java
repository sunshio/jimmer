package org.babyfish.jimmer.sql.example;

import io.swagger.v3.core.jackson.ModelResolver;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.query.Example;
import org.babyfish.jimmer.sql.example.model.Book;
import org.babyfish.jimmer.sql.example.model.BookDraft;
import org.babyfish.jimmer.sql.example.model.BookTable;
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
        sqlClient.createQuery(BookTable.$).where(BookTable.$.eq(Example.of(BookDraft.$.produce(book->{
            book.applyStore(store->{
//                store.setId(1);
               store.setName("111111111");
            });
        })))).select(BookTable.$).execute();
    }
}
