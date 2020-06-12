package org.apache.commons.text;


import org.apache.commons.text.matcher.StringMatcher;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.matcher.StringMatcherFactory;

public class StringSubstitutorProduct {
	private StringMatcher prefixMatcher;
	private StringMatcher suffixMatcher;
	private StringMatcher valueDelimiterMatcher;

	public StringMatcher getPrefixMatcher() {
		return prefixMatcher;
	}

	public StringMatcher getSuffixMatcher() {
		return suffixMatcher;
	}

	public StringMatcher getValueDelimiterMatcher() {
		return valueDelimiterMatcher;
	}

	/**
	* Sets the variable prefix matcher currently in use. <p> The variable prefix is the character or characters that identify the start of a variable. This prefix is expressed in terms of a matcher allowing advanced prefix matches.
	* @param prefixMatcher  the prefix matcher to use, null ignored
	* @return  this, to enable chaining
	* @throws IllegalArgumentException  if the prefix matcher is null
	*/
	public StringSubstitutor setVariablePrefixMatcher(final StringMatcher prefixMatcher,
			StringSubstitutor stringSubstitutor) {
		Validate.isTrue(prefixMatcher != null, "Variable prefix matcher must not be null!");
		this.prefixMatcher = prefixMatcher;
		return stringSubstitutor;
	}

	/**
	* Sets the variable prefix to use. <p> The variable prefix is the character or characters that identify the start of a variable. This method allows a string prefix to be easily set.
	* @param prefix  the prefix for variables, not null
	* @return  this, to enable chaining
	* @throws IllegalArgumentException  if the prefix is null
	*/
	public StringSubstitutor setVariablePrefix(final String prefix, StringSubstitutor stringSubstitutor) {
		Validate.isTrue(prefix != null, "Variable prefix must not be null!");
		return setVariablePrefixMatcher(StringMatcherFactory.INSTANCE.stringMatcher(prefix), stringSubstitutor);
	}

	/**
	* Sets the variable suffix matcher currently in use. <p> The variable suffix is the character or characters that identify the end of a variable. This suffix is expressed in terms of a matcher allowing advanced suffix matches.
	* @param suffixMatcher  the suffix matcher to use, null ignored
	* @return  this, to enable chaining
	* @throws IllegalArgumentException  if the suffix matcher is null
	*/
	public StringSubstitutor setVariableSuffixMatcher(final StringMatcher suffixMatcher,
			StringSubstitutor stringSubstitutor) {
		Validate.isTrue(suffixMatcher != null, "Variable suffix matcher must not be null!");
		this.suffixMatcher = suffixMatcher;
		return stringSubstitutor;
	}

	/**
	* Sets the variable suffix to use. <p> The variable suffix is the character or characters that identify the end of a variable. This method allows a string suffix to be easily set.
	* @param suffix  the suffix for variables, not null
	* @return  this, to enable chaining
	* @throws IllegalArgumentException  if the suffix is null
	*/
	public StringSubstitutor setVariableSuffix(final String suffix, StringSubstitutor stringSubstitutor) {
		Validate.isTrue(suffix != null, "Variable suffix must not be null!");
		return setVariableSuffixMatcher(StringMatcherFactory.INSTANCE.stringMatcher(suffix), stringSubstitutor);
	}

	/**
	* Sets the variable default value delimiter matcher to use. <p> The variable default value delimiter is the character or characters that delimite the variable name and the variable default value. This delimiter is expressed in terms of a matcher allowing advanced variable default value delimiter matches. <p> If the <code>valueDelimiterMatcher</code> is null, then the variable default value resolution becomes disabled.
	* @param valueDelimiterMatcher  variable default value delimiter matcher to use, may be null
	* @return  this, to enable chaining
	*/
	public StringSubstitutor setValueDelimiterMatcher(final StringMatcher valueDelimiterMatcher,
			StringSubstitutor stringSubstitutor) {
		this.valueDelimiterMatcher = valueDelimiterMatcher;
		return stringSubstitutor;
	}

	/**
	* Sets the variable default value delimiter to use. <p> The variable default value delimiter is the character or characters that delimite the variable name and the variable default value. This method allows a string variable default value delimiter to be easily set. <p> If the <code>valueDelimiter</code> is null or empty string, then the variable default value resolution becomes disabled.
	* @param valueDelimiter  the variable default value delimiter string to use, may be null or empty
	* @return  this, to enable chaining
	*/
	public StringSubstitutor setValueDelimiter(final String valueDelimiter, StringSubstitutor stringSubstitutor) {
		if (valueDelimiter == null || valueDelimiter.length() == 0) {
			setValueDelimiterMatcher(null, stringSubstitutor);
			return stringSubstitutor;
		}
		return setValueDelimiterMatcher(StringMatcherFactory.INSTANCE.stringMatcher(valueDelimiter), stringSubstitutor);
	}
}