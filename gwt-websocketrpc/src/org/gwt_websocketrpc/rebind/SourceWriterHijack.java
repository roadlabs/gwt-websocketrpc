package org.gwt_websocketrpc.rebind;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

// Oh fuggin-a...
class SourceWriterHijack implements SourceWriter {

    private final SourceWriter w;
    private final String printFind;
    private final String printReplace;
    
    public SourceWriterHijack(SourceWriter w, String printFind,
            String printReplace) {
        this.w = w;
        this.printFind = printFind;
        this.printReplace = printReplace;
    }

    public void beginJavaDocComment() {
        w.beginJavaDocComment();
    }

    public void commit(TreeLogger logger) {
        w.commit(logger);
    }

    public void endJavaDocComment() {
        w.endJavaDocComment();
    }

    public void indent() {
        w.indent();
    }

    public void indentln(String s) {
        w.indentln(s);
    }

    public void outdent() {
        w.outdent();
    }

    public void print(String s) {
        w.print(s.equals(printFind) ? printReplace : s);
    }

    public void println() {
        w.println();
    }

    public void println(String s) {
        w.println(s);
    }
}
