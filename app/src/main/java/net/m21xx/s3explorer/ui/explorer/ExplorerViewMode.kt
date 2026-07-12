package net.m21xx.s3explorer.ui.explorer

enum class ExplorerViewMode {
    DETAILED_LIST,
    COMPACT_LIST,
    GALLERY_SMALL,
    GALLERY_LARGE;

    fun next(): ExplorerViewMode {
        val values = entries.toTypedArray()
        val nextOrdinal = (this.ordinal + 1) % values.size
        return values[nextOrdinal]
    }
}
