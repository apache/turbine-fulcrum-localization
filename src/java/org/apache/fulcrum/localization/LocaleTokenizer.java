package org.apache.fulcrum.localization;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Parses the HTTP <code>Accept-Language</code> header as per section
 * 14.4 of RFC 2068 (HTTP 1.1 header field definitions).
 *
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id$
 */
public class LocaleTokenizer
    implements Iterator<Object>
{
    /**
     * Separates elements of the <code>Accept-Language</code> HTTP
     * header.
     */
    private static final String LOCALE_SEPARATOR = ",";

    /**
     * Separates locale from quality within elements.
     */
    private static final char QUALITY_SEPARATOR = ';';

    /**
     * The default quality value for an <code>AcceptLanguage</code>
     * object.
     */
    protected static final Float DEFAULT_QUALITY = 1.0f;

    /**
     * The parsed locales.
     */
    private ArrayList<AcceptLanguage> locales = new ArrayList<>(3);

    /**
     * Parses the <code>Accept-Language</code> header.
     *
     * @param header The <code>Accept-Language</code> header
     * (i.e. <code>en, es;q=0.8, zh-TW;q=0.1</code>).
     */
    public LocaleTokenizer(String header)
    {
        StringTokenizer tok = new StringTokenizer(header, LOCALE_SEPARATOR);
        while (tok.hasMoreTokens())
        {
            AcceptLanguage acceptLang = new AcceptLanguage();
            String element = tok.nextToken().trim();
            int index;

            // Record and cut off any quality value that comes after a
            // semi-colon.
            if ( (index = element.indexOf(QUALITY_SEPARATOR)) != -1 )
            {
                String q = element.substring(index);
                element = element.substring(0, index);
                if ( (index = q.indexOf('=')) != -1 )
                {
                    try
                    {
                        acceptLang.quality =
                            Float.valueOf(q.substring(index + 1));
                    }
                    catch (NumberFormatException useDefault)
                    {
                        // ignore
                    }
                }
            }

            element = element.trim();

            // Create a Locale from the language.  A dash may separate the
            // language from the country.
            if ( (index = element.indexOf('-')) == -1 )
            {
                // No dash means no country.
                acceptLang.locale = new Locale(element, "");
            }
            else
            {
                acceptLang.locale = new Locale(element.substring(0, index),
                                               element.substring(index + 1));
            }

            locales.add(acceptLang);
        }

        // Sort by quality in descending order.
        locales.sort(Collections.reverseOrder());
    }

    /**
     * @return Whether there are more locales.
     */
    public boolean hasNext()
    {
        return !locales.isEmpty();
    }

    /**
     * Creates a <code>Locale</code> from the next element of the
     * <code>Accept-Language</code> header.
     *
     * @return The next highest-rated <code>Locale</code>.
     * @throws NoSuchElementException No more locales.
     */
    public Object next()
    {
        if (locales.isEmpty())
        {
            throw new NoSuchElementException();
        }
        return (locales.remove(0)).locale;
    }

    /**
     * Not implemented.
     */
    @Override
    public final void remove()
    {
        throw new UnsupportedOperationException(getClass().getName() +
                                                " does not support remove()");
    }

    /**
     * Struct representing an element of the HTTP
     * <code>Accept-Language</code> header.
     */
    protected static class AcceptLanguage implements Comparable<Object>
    {
        /**
         * The language and country.
         */
        Locale locale;

        /**
         * The quality of our locale (as values approach
         * <code>1.0</code>, they indicate increased user preference).
         */
        Float quality = DEFAULT_QUALITY;
        
		@Override
        public int compareTo(Object acceptLang)
        {
			if ( acceptLang == null )
				throw new NullPointerException("AcceptLanguage not found");
			
			if ( acceptLang instanceof AcceptLanguage )
			{
				Float q2 = ((AcceptLanguage) acceptLang).quality;
				return quality.compareTo( q2 );
			} else {
				throw new NullPointerException("The object to compare is not the correct type");
			}
				
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AcceptLanguage)) return false;
            AcceptLanguage that = (AcceptLanguage) o;
            return quality.equals(that.quality);
        }

        @Override
        public int hashCode() {
            return Objects.hash(locale, quality);
        }
    }
}
