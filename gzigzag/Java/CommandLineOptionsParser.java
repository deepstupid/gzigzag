/*   
CommandLineOptionsParser.java
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

/** Command line options parser.  */
class CommandLineOptionsParser {

    /** Objects of this class or of descendants of this class
     * represent valid command-line options.  By default, this option
     * does nothing except note that the option was seen.  Subclasses
     * can override <code>action</code> to provide functionality to an
     * option.
     *
     * @see HelpOption
     */
    public static class Option {
        /** The name of this option.  A dash will be automatically
         * appended to this name to produce the string that is matched
         * against in command line. */
        public final String name;

        /** A list of argument names for this option.  The option will
         * take as many mandatory arguments as there are elements in
         * this array.  The names of the arguments are purely
         * documentary and are used in help screens.  
         *
         * @see org.gzigzag.CommandLineOptionsParser.HelpOption
         */
        public final String[] argNames;

        /** A help text for this option.  The text will be
         * word-wrapped; newlines embedded in the text are
         * honoured. 
         *
         * @see org.gzigzag.CommandLineOptionsParser.HelpOption
         */
        public final String helpText;
        
        /** The action for this option.  This method will be invoked
         * when this option is noticed.
         *
         * @param clop The instance of
         * <code>CommandLineOptionsParser</code> that is running this
         * parse.
         * @param args The arguments given on the command line to this
         * option.  This array will have exactly as many elements as
         * the <code>argNames</code> attribute.
         */
        public void action(CommandLineOptionsParser clop, String[] args) {}

        /** Constructs an option.
         *
         * @param name The value to be given to the <code>name</code>
         * attribute.
         * @param argNames The value to be given to the
         * <code>argNames</code> attribute.
         * @param helpText The value to be given to the
         * <code>helpText</code> attribute.
         */
        public Option(String name, String[] argNames, String helpText) {
            this.name = name;
            this.argNames = argNames;
            this.helpText = helpText;
        }
    }

    /** A command-line option for showing help about command-line
     * options.  */
    public static class HelpOption extends Option {
        /** Constructs a HelpOption.
         * @param name The name for this option (a dash will be
         * automatically prepended to create the string that is
         * matched against the command line).
         * @param helpText The help text for this option.  This text
         * will be word-wrapped.  Embedded newlines are honoured.
         * @param exitAfter Should the program be exited after showing
         * the help?
         * @param prologue Text to be shown before the list of
         * options.  This text will be word-wrapped.  Embedded
         * newlines are honoured.
         * @param epilogue Text to be shown before the list of
         * options.  This text will be word-wrapped.  Embedded
         * newlines are honoured.
         */
        public HelpOption(String name, String helpText, boolean exitAfter,
                          String prologue, String epilogue) {
            super(name, new String[0], helpText);
            this.exitAfter = exitAfter;
            this.prologue = prologue;
            this.epilogue = epilogue;
        }

        public void action(CommandLineOptionsParser clop, String[] args) {
            int rightmargin = 75;
            System.out.println(breakToLines(prologue, rightmargin, 0));
            for (Enumeration e = clop.options_in_order.elements();
                 e.hasMoreElements();) {
                Option opt = (Option)e.nextElement();
                int expltab = 15;
                int nametab = 3;
                int seplen = 2;
                String help = breakToLines(opt.helpText, rightmargin,
                                           expltab + nametab + seplen);
                String name = "-" + opt.name;
                for (int i = 0; i < opt.argNames.length; i++) {
                    name += " " + opt.argNames[i];
                }
                if (name.length() > expltab) {
                    help = spaces(nametab) + name + '\n' + help;
                } else {
                    help = spaces(nametab) + name
                        + spaces(seplen + expltab - name.length())
                        + help.substring(expltab + nametab + seplen);
                }
                System.out.println(help);
            } 
            System.out.println(breakToLines(epilogue, rightmargin, 0));
            if (exitAfter) SafeExit.exit(0);
        }

        private String spaces(int n) {
            StringBuffer sb = new StringBuffer(n);
            for (int i = 0; i < n; i++) sb.append(' ');
            return new String(sb);
        }

        private int breakpoint(String s, int start, int maxlen) {
            if (s.length() - start < maxlen) return s.length();
            int bp = start + maxlen < s.length() ? start + maxlen : s.length();
            for (int i = 0; i < maxlen && i + start < s.length(); i++) {
                int c = s.charAt(start + i);
                if (c == '\n') return start + i;
                if (c == '\t' || c == ' ') bp = start + i;
            }
            return bp;
        }

        private String breakToLines(String s, int rmargin, int indent) {
            int maxlen = rmargin - indent;
            StringBuffer sb = new StringBuffer();
            String ind = spaces(indent);
            int a = 0;
            while (a < s.length()) {
                int b = breakpoint(s, a, maxlen);
                sb.append(ind);
                sb.append(s.substring(a, b));
                sb.append('\n');
                if (b < s.length() &&
                    (s.charAt(b) == '\n'
                     || s.charAt(b) == ' '
                     || s.charAt(b) == '\t')) {
                    ++b;
                }
                a = b;
            }
            return new String(sb);
        }

        private final boolean exitAfter;
        private final String prologue;
        private final String epilogue;
    }

    /** Constructs a command line options parser.
     * @param options An array of preconstructed options.
     * @see #put(Option)
     */
    public CommandLineOptionsParser(Option[] options) {
        for (int i = 0; i < options.length; i++) {
            put(options[i]);
        }
    }

    /** Constructs a command line options parser with an initially
     * empty set of acceptable options. */
    public CommandLineOptionsParser() {}

    /** Adds <code>option</code> to the list of valid options.  */
    public void put(Option option) {
        options.put(option.name, option);
        options_in_order.addElement(option);
    }

    /** Adds a new option to the list of valid options.  The option
     * object will be a new instance of class <code>Option</code>.
     * The arguments of this method are passed unchanged to the
     * constructor.
     * @see Option
     */
    public void put(String name, String[] argNames, String helpText) {
        put(new Option(name, argNames, helpText));
    }

    /** Adds a help option to the list of valid options.  The option
     * object will be a new instance of <code>HelpOption</code>.  The
     * arguments of this method are passed unchanged to the
     * constructor. 
     * @see HelpOption
     */
    public void putHelp(String name, String helpText, boolean exitAfter,
                        String prologue, String epilogue) {
        put(new HelpOption(name, helpText, exitAfter, prologue, epilogue));
    }

    /** Marks all options unseen. */
    public void clearArgs() {
        optargs = new Hashtable();
    }

    /** Gets the list of arguments for the option specified.    
     * @param name The name of the option whose arguments are to be returned.
     * @return null, if the option has not been seen, otherwise the
     * same array or a copy of the same array that the
     * <code>action</code> method of <code>Option</code> gets.
     */
    public String[] getArgs(String name) {
        Object o = optargs.get(name);
        if (o == null) return null;
        return (String[]) o;
    }

    /** Gets a specific argument for the option specified.
     * @param name The name of the option whose argument is to be
     * returned
     * @param i The ordinal number of the argument to be returned (0
     * for the first argument, 1 for the second and so on) 
     * @return null, if the option has not seen, otherwise the
     * <code>i</code>th (0-based) argument to the option.
     */
    public String getArg(String name, int i) {
        return getArg(name, i, null);
    }

    /** Gets a specific argument for the option specified.
     * @param name The name of the option whose argument is to be
     * returned
     * @param i The ordinal number of the argument to be returned (0
     * for the first argument, 1 for the second and so on) 
     * @param deflt The object to be returned if the option has not
     * been seen.
     * @return <code>deflt</code>, if the option has not seen, otherwise the
     * <code>i</code>th (0-based) argument to the option.
     */
    public String getArg(String name, int i, String deflt) {
        String[] args = getArgs(name);
        if (args == null) return deflt;
        return args[i];
    }

    /** Determines whether the specified option was seen in parses
     * since the arguments were last cleared. */
    public boolean seen(String name) {
        return optargs.containsKey(name);
    }

    /** Parse command-line options from args and return the first
     * index to attrs that is not an option, or attrs.length if there
     * are none such . */
    public int parse(String[] args) {
        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].length() == 0 || args[i].charAt(0) != '-') {
                return i;
            }
            
            if (!options.containsKey(args[i].substring(1))) {
                throw new SyntaxError("unknown option: " + args[i]);
            }

            Option opt = (Option)options.get(args[i].substring(1));
            String[] oargs = new String[opt.argNames.length];
            for (int j = 0; j < oargs.length; j++) {
                ++i;
                if (i >= args.length) {
                    throw new SyntaxError("too few arguments to option: -" + opt.name);
                }
                oargs[j] = args[i];
            }
            optargs.put(opt.name, oargs);
            opt.action(this, oargs);
        }
        return i;
    }

    private Hashtable options = new Hashtable();
    private Vector options_in_order = new Vector();

    private Hashtable optargs = new Hashtable();


}
