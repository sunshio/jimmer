package org.babyfish.jimmer.sql.filter;

import org.babyfish.jimmer.sql.ast.table.Props;
import org.babyfish.jimmer.sql.event.EntityEvent;

import java.util.SortedMap;

public interface Filter<P extends Props> {

    void filter(FilterArgs<P> args);
}
