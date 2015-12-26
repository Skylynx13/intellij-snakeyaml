package lv.kid.vermut.intellij.yaml.lexer;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.Scanner;
import org.yaml.snakeyaml.scanner.ScannerException;
import org.yaml.snakeyaml.scanner.ScannerWhitespaceImpl;
import org.yaml.snakeyaml.tokens.ErrorToken;
import org.yaml.snakeyaml.tokens.StreamEndToken;
import org.yaml.snakeyaml.tokens.Token;

import java.io.Reader;

/**
 * Created by Pavels.Veretennikovs on 2015.06.27..
 */
public class ErrorReportingScanner implements ScannerEx {
    protected final static PsiBuilder.Marker EMPTY_MARKER = new PsiBuilder.Marker() {
        @NotNull
        @Override
        public PsiBuilder.Marker precede() {
            return null;
        }

        @Override
        public void drop() {

        }

        @Override
        public void rollbackTo() {

        }

        @Override
        public void done(@NotNull IElementType type) {

        }

        @Override
        public void collapse(@NotNull IElementType type) {

        }

        @Override
        public void doneBefore(@NotNull IElementType type, @NotNull PsiBuilder.Marker before) {

        }

        @Override
        public void doneBefore(@NotNull IElementType type, @NotNull PsiBuilder.Marker before, String errorMessage) {

        }

        @Override
        public void error(String message) {

        }

        @Override
        public void errorBefore(String message, @NotNull PsiBuilder.Marker before) {

        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder left, @Nullable WhitespacesAndCommentsBinder right) {

        }
    };
    private final Scanner scanner;
    private final StreamReader streamReader;
    protected Token hangingVirtualToken;

    public ErrorReportingScanner(Reader reader) {
        streamReader = new StreamReader(reader);
        scanner = new ScannerWhitespaceImpl(streamReader);
    }

    @Override
    public boolean checkToken(Token.ID... choices) {
        return scanner.checkToken(choices);
    }

    @SuppressWarnings({"StatementWithEmptyBody", "EmptyCatchBlock"})
    @Override
    public Token peekToken() {
        try {
            return scanner.peekToken();
        } catch (ScannerException e) {
            Mark start = streamReader.getMark();
            try {
                do {
                    streamReader.forward();
                } while (!readerOnWhitespace());
                streamReader.forward();
            } catch (StringIndexOutOfBoundsException ignored) {
                // No more data, make sure we have nothing in scanner cache
                try {
                    while (scanner.getToken() != null) {
                    }
                } catch (ScannerException also_ignored) {
                }

                // Got nothing while forwarding the stream, this is the end
                if (streamReader.getMark().getIndex() == start.getIndex())
                    return new StreamEndToken(streamReader.getMark(), streamReader.getMark());
            }
            hangingVirtualToken = new ErrorToken(start, streamReader.getMark());
            return hangingVirtualToken;
        } catch (StringIndexOutOfBoundsException ignored) {
            return new StreamEndToken(streamReader.getMark(), streamReader.getMark());
        }
    }

    @Override
    public Token getToken() {
        if (hangingVirtualToken != null) {
            Token result = hangingVirtualToken;
            hangingVirtualToken = null;
            return result;
        }
        return scanner.getToken();
    }

    @Override
    public void catchUpWithScanner() {
    }

    @Override
    public void setPeekMode(boolean peekMode) {
    }

    @Override
    public PsiBuilder.Marker getMarker() {
        return EMPTY_MARKER;
    }

    @Override
    public void markError(String error) {
    }

    private boolean readerOnWhitespace() {
        return streamReader.peek() == ' '
                || streamReader.peek() == '\n'
                || streamReader.peek() == '\t';
    }
}