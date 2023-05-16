package org.babyfish.jimmer.sql.example.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.babyfish.jimmer.sql.LogicalDeleted;
import org.babyfish.jimmer.sql.MappedSuperclass;

import java.time.LocalDateTime;

/*
 * see CommonEntityDraftInterceptor
 */
@MappedSuperclass
public interface BaseEntity {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdTime();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime modifiedTime();

    @LogicalDeleted(restoredValue = "0", value = "1", useMultiViewCache = true)
    int deletedInd();
}
