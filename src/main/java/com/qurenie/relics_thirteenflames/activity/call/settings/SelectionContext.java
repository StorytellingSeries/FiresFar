package com.qurenie.relics_thirteenflames.activity.call.settings;

import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @param isRemoved if activity is going to be deselected
 * @param prevOne is null if not {@link #isRemoved}, contains previous selected activity otherwise
 */
public record SelectionContext(boolean isRemoved, @Nullable CallInput prevOne) {
}
