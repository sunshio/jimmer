package org.babyfish.jimmer.sql.kt.ast.query.impl

import org.babyfish.jimmer.sql.ast.query.Order
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor

interface KConfigurableRootQueryImplementor<E: Any, R> : KConfigurableRootQuery<E, R> {

    val javaOrders: List<Order>

    val javaSqlClient: JSqlClientImplementor
}