package com.github.ohtomi.kotlin.sandbox.dsl

sealed class QueryState {
    // These object are phantom types

    object From : QueryState()
    object Where : QueryState()
    object Print : QueryState()
}

class Query<S : QueryState>(val column: String, val tableName: String, val predicate: String) {

    companion object {
        fun select(column: String) =
                Query<QueryState.From>(column, "", "")
    }
}

fun Query<QueryState.From>.from(tableName: String) =
        Query<QueryState.Where>(this.column, tableName, this.predicate)

fun Query<QueryState.Where>.where(predicate: String) =
        Query<QueryState.Print>(this.column, this.tableName, predicate)

fun Query<QueryState.Print>.print() =
        "SELECT $column FROM $tableName WHERE $predicate ;"
