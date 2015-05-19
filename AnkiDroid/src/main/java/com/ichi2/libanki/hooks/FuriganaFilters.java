/***************************************************************************************
 * Copyright (c) 2012 Kostas Spyropoulos <inigo.aldana@gmail.com>                       *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.libanki.hooks;

import com.ichi2.compat.CompatHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuriganaFilters {
    private static final Pattern READING = Pattern.compile(" ?([^ >]+?)\\[(.+?)\\]");
    // private static final Pattern READING = Pattern.compile(" ?([-+×÷%\\.\\w]+?)\\[(.+?)\\]",
    //     Pattern.UNICODE_CHARACTER_CLASS);
    // private static final Pattern READING = Pattern.compile("(?U) ?([-+×÷%\\.\\w]+?)\\[(.+?)\\]");

    // Bah. Using Pattern.UNICODE_CHARACTER_CLASS doesn’t compile. No wonder that using the “(?U)” tag does not
    // work. Using the “\\w” pattern without a Unicode-indicator seems to work with Japanese text, but beter not
    // risk anything and use the more standard matching and add code point 0x20 spaces.

    // Since there is no ruby tag support in Android before 3.0 (SDK version 11), we must use an alternative
    // approach to align the elements. Anki does the same thing in aqt/qt.py for earlier versions of qt.
    // The fallback approach relies on CSS in the file /assets/ruby.css
    private static final String FURIGANA = CompatHelper.isHoneycomb() ?
        "<ruby class='furigana'><rb>$1</rb><rt>$2</rt></ruby>" :
        "<span class='furigana legacy_ruby_rb'><span class='legacy_ruby_rt'>$2</span>$1</span>";
    private static final String FURIKANJI = CompatHelper.isHoneycomb() ?
        "<ruby class='furikanji'><rb>$2</rb><rt>$1</rt></ruby>" :
        "<span class='furikanji legacy_ruby_rb'><span class='legacy_ruby_rt'>$1</span>$2</span>";


    public void install(Hooks h) {
        h.addHook("fmod_kanji", new Kanji());
        h.addHook("fmod_kana", new Kana());
        h.addHook("fmod_furigana", new Furigana());
        h.addHook("fmod_furikanji", new Furikanji());
    }


    private static String noSound(Matcher match, String repl) {
        if (match.group(2).startsWith("sound:")) {
            // return without modification
            return match.group(0);
        } else {
            return READING.matcher(match.group(0)).replaceAll(repl);
        }
    }

    public class Kanji extends Hook {
        @Override
        public Object runFilter(Object arg, Object... args) {
            Matcher m = READING.matcher((String) arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, noSound(m, "$1"));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    public class Kana extends Hook {
        @Override
        public Object runFilter(Object arg, Object... args) {
            Matcher m = READING.matcher((String) arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, noSound(m, "$2"));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    public class Furigana extends Hook {
        @Override
        public Object runFilter(Object arg, Object... args) {
            Matcher m = READING.matcher((String) arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, noSound(m, FURIGANA));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    public class Furikanji extends Hook {
        @Override
        public Object runFilter(Object arg, Object... args) {
            Matcher m = READING.matcher((String) arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, noSound(m, FURIKANJI));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }
}
