package com.example.foodtrack.util;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {
    private final Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        mPattern = Pattern.compile("^\\d{0," + digitsBeforeZero + "}(\\.\\d{0," + digitsAfterZero + "})?$");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String newText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
        Matcher matcher = mPattern.matcher(newText);
        return matcher.matches() ? null : "";
    }
}