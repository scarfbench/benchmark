package spring.tutorial.web.dukeetf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DukeETFServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger("DukeETFServlet");
    private static final long serialVersionUID = 2114153638027156979L;
    private Queue<AsyncContext> requestQueue;
    private final PriceVolumeBean priceVolumeBean;

    public DukeETFServlet(PriceVolumeBean priceVolumeBean) {
        this.priceVolumeBean = priceVolumeBean;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        requestQueue = new ConcurrentLinkedQueue<>();
        priceVolumeBean.registerServlet(this);
    }

    public void send(double price, int volume) {
        for (AsyncContext acontext : requestQueue) {
            try {
                String msg = String.format("%.2f / %d", price, volume);
                PrintWriter writer = acontext.getResponse().getWriter();
                writer.write(msg);
                logger.log(Level.INFO, "Sent: {0}", msg);
                acontext.complete();
            } catch (IOException ex) {
                logger.log(Level.INFO, ex.toString());
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");
        final AsyncContext acontext = request.startAsync();
        acontext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent ae) throws IOException {
                requestQueue.remove(acontext);
                logger.log(Level.INFO, "Connection closed.");
            }
            @Override
            public void onTimeout(AsyncEvent ae) throws IOException {
                requestQueue.remove(acontext);
                logger.log(Level.INFO, "Connection timeout.");
            }
            @Override
            public void onError(AsyncEvent ae) throws IOException {
                requestQueue.remove(acontext);
                logger.log(Level.INFO, "Connection error.");
            }
            @Override
            public void onStartAsync(AsyncEvent ae) throws IOException { }
        });
        requestQueue.add(acontext);
        logger.log(Level.INFO, "Connection open.");
    }
}
