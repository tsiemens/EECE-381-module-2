package com.group10.battleship.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

public class EditTextIntPreference extends EditTextPreference {

	private Integer mInteger;

	public EditTextIntPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
	}

	public EditTextIntPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
	}

	public EditTextIntPreference(Context context) {
		super(context);
		getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
	}

	@Override public void setText(String text) {
		final boolean wasBlocking = shouldDisableDependents();
		mInteger = parseInteger(text);
		if (mInteger != null)
			persistInt(mInteger);
		
		final boolean isBlocking = shouldDisableDependents(); 
		if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
	}
	
	 @Override
	 protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		 int restoreIntVal = mInteger != null ? mInteger.intValue() : -1;
	     setText(restoreValue ? String.valueOf(getPersistedInt(restoreIntVal)) : ((Integer)defaultValue).toString());
	 }
	 
	 @Override
	 protected Object onGetDefaultValue(TypedArray a, int index) {
		 return a.getInteger(index, 0);
	 }

	@Override public String getText() {
		return mInteger != null ? mInteger.toString() : null;
	}

	private static Integer parseInteger(String text) {
		try { return Integer.parseInt(text); }
		catch (NumberFormatException e) { return null; }
	} 
}
