package test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import test.pages.master.Page;

public class HttpResponse {

    private final OutputStream output;

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public void writeRedirect(String url) throws IOException {
        writeCookies();

        output.write("Location: ".getBytes());
        output.write(url.getBytes());
        output.write("\r\n".getBytes());

        closeHeaders();
    }

    public void writePage(Page page) throws IOException {
        writeCookies();
        output.write("Content-Type: text/html\r\n".getBytes());

        closeHeaders();

        page.write(new IndentedHtmlText() {

            @Override
            protected void append(String text) {
                try {
                    output.write(text.getBytes());
                } catch (IOException ex) {
                    //TODO: log
                }
            }
        });

        
    }


    private void closeHeaders() throws IOException {
        output.write("\r\n".getBytes());
    }

    private void writeCookies() throws IOException {
        output.write("Set-Cookie: session_key=".getBytes());
        output.write(Context.getSession().getKey().getBytes());
        output.write("; path=/; Max-Age=1296000; Version=1 \r\n".getBytes());
    }
}