package org.babyfish.jimmer.example.save;

import org.babyfish.jimmer.example.save.common.AbstractMutationTest;
import org.babyfish.jimmer.example.save.common.ExecutedStatement;
import org.babyfish.jimmer.example.save.model.*;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Recommended learning sequence: 1
 *
 * <p>[Current: SaveModeTest] -> IncompleteObjectTest -> ManyToOneTest ->
 * OneToManyTest -> ManyToManyTest -> RecursiveTest -> TriggerTest</p>
 */
public class MyTest extends AbstractMutationTest {
    @Test
    public void testUpsertExistingDataById() {



        SimpleSaveResult<Book> result = sql()
                .getEntities()
                .saveCommand(
                        BookDraft.$.produce(book -> {
                            book.setId(10L);
                            book.setName("PL/SQL in Action");
                            book.setEdition(2);
                            book.setPrice(new BigDecimal("10"));
                            book.addIntoAuthors(a->{
                               a.setFirstName("111111");
                               a.setLastName("aaaaa");
                               a.setGender(Gender.FEMALE);
                            });
                        })
                ).setAutoAttachingAll().execute();
        System.out.println("aaaaa");
    }
}
