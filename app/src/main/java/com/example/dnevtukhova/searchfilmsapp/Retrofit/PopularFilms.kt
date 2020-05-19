package com.example.dnevtukhova.searchfilmsapp.Retrofit

data class PopularFilms (
    val page: Int,
    val results: Array<FilmModel>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PopularFilms

        if (page != other.page) return false
        if (!results.contentEquals(other.results)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = page
        result = 31 * result + results.contentHashCode()
        return result
    }
}
