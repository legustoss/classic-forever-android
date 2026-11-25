package app.gamenative.ui.screen.wow

internal enum class BackPlacement { TOP, BOTTOM }

/** Compact portrait keeps the lower Back target; wider landscape keeps the top-bar one. */
internal fun backPlacementFor(compact: Boolean): BackPlacement =
    if (compact) BackPlacement.BOTTOM else BackPlacement.TOP
