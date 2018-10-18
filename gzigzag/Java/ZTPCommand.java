/*   
ZTPCommand.java
 *    
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Antti-Juhani Kaijanaho
 */
package org.gzigzag;
import java.util.*;

/** ZTP command parser.  After instance construction, the attribute
 * "name" contains the command name and the attribute "args" contains
 * the arguments of the command.  Since this class is also used in the
 * conversion space dumper, it is in the main package instead of the
 * ztp package. */
public class ZTPCommand {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    public String name;
    public String[] args;
    
    public static class RV {
        public String token;
        public String rest;
        public RV(String token, String rest) {
            this.token = token;
            this.rest = rest;
        }
        public String toString() {
            return "[token: " + token + " | rest: " + rest + "]";
        }
    }
    
    public static RV lwsp(String str) {
        int i;
        for (i = 0; i < str.length(); i++) {
            if (str.charAt(i) != 9 && str.charAt(i) != 32) break;
        }
        return new RV(str.substring(0, i), str.substring(i));
    }

    public static boolean ASCII_word_char(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '/';
    }
    
    public static RV ASCII_word(String line) {
        int rlen = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!ASCII_word_char(c)) break;
            ++rlen;
        }
        if (rlen == 0) return null;
        return new RV(line.substring(0, rlen), line.substring(rlen));
    }
    
    public static boolean ASCII_num_char(char c) {
        return '0' <= c && c <= '9';
    }
    
    public static RV ASCII_number(String line) {
        int rlen = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!ASCII_num_char(c)) break;
            ++rlen;
        }
        if (rlen == 0) return null;
        return new RV(line.substring(0, rlen), line.substring(rlen));
    }
    
    public static RV quoted_string(String line) {
        if (line.charAt(0) != 34) return null;
        String rv = "";
        int i;
        boolean ok = false;
        for (i = 1; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i+1) == '"') {
                    ++i;
                } else {
                    ok = true;
                    break;
                }
            }
            if (c == '\\') {
                if (i + 1 == line.length()) {
                    throw new SyntaxError("unterminated escape sequence");
                }
                if (line.charAt(i+1) == '\\' || line.charAt(i+1) == '"') {
                    c = line.charAt(++i);
                } else if (line.charAt(i+1) == 'n') {
                   c = '\n';
                    ++i;
                } else {
                    throw new SyntaxError("invalid escape sequence");
                }
            }
            rv += c;
        }
        if (!ok) {
            throw new SyntaxError("unterminated quoted string");
        }
        return new RV(rv, line.substring(i+1));
    }

    private static String encode_quoted(String s) {
        String rv = "\"";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"') {
                rv += "\"\"";
            } else if (s.charAt(i) == '\\') {
                rv += "\\\\";
            } else if (s.charAt(i) == '\n') {
                rv += "\\n";
            } else {
                rv += s.charAt(i);
            }
        }
        rv += "\"";
        return rv;
    }
    
    /** Encodes s as one token for the Command parser. */
    public static String encode(String s) {
        if (s.length() == 0) return "\"\"";
        if (ASCII_num_char(s.charAt(0))) {
            for (int i = 1; i < s.length(); i++) {
                if (!ASCII_num_char(s.charAt(i))) {
                    return encode_quoted(s);
                }
            }
            return s;
        }
        if (ASCII_word_char(s.charAt(0))) {
            for (int i = 1; i < s.length(); i++) {
                if (!ASCII_word_char(s.charAt(i))) {
                    return encode_quoted(s);
                }
            }
            return s;
        }
        return encode_quoted(s);
    }

    public ZTPCommand(String line) {
        RV rv;
        
        rv = ASCII_word(line);
        if (rv == null) throw new SyntaxError("error parsing command name");
        name = rv.token;
        line = lwsp(rv.rest).rest;
        Vector v = new Vector();
        while (line.length() > 0 && !line.equals("\n")
               && !line.equals("\r") && !line.equals("\r\n")) {
            p("line = " + line);
            rv = quoted_string(line);
            if (rv != null) {
                p("arg parsed as a quoted string, token = " + rv.token);
            } else {
                rv = ASCII_word(line);
                if (rv != null) {
                    p("arg parsed as an ASCII word, token = " + rv.token);
                } else {
                    rv = ASCII_number(line);
                    if (rv != null) {
                        p("arg parsed as an ASCII number, token = " + rv.token);
                    } else {
                        throw new SyntaxError("error parsing command arguments");
                    }
                }
            }
            v.addElement(rv.token);
            line = lwsp(rv.rest).rest;
        }
        args = new String[v.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = (String)v.elementAt(i);
        }
        p("args.length = " + args.length);
    }
    
    public String toString() {
        String rv = name;
        for (int i = 0; i < args.length; i++) {
            rv += "_" + args[i];
        }
        return rv;
    }
}

