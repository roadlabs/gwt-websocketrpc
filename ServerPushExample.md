# Summary #
This example assumes you already understand the basics of the [GWT RPC Framework](http://code.google.com/webtoolkit/doc/latest/DevGuideServerCommunication.html).

  1. Add `org.gwt_websocketrpc.Gwt_websocketrpc` to your module
  1. Sync service extends WsRpcService (instead of RpcService)
  1. Annotate sync service server push service methods with @ServerPushEnabled
  1. Async Service method return type must be Request type
  1. Server implementation extends WsRpcServlet (instead of RpcServlet)

# Module XML #
```
<inherits name='org.gwt_websocketrpc.Gwt_websocketrpc' />
```

# Service #
```
/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("stock")
public interface StockService extends WsRpcService {

    @ServerPushEnabled
    Double getStockUpdates(String stockSymbol);
}
```

# Asynchronous Service #
```
/**
 * The async counterpart of <code>StockService</code>.
 */
public interface StockServiceAsync {

    // !!! Request return type is NECESSARY for Server Push Enabled service methods.
    // This allows client code to cancel callback (Request.cancel()).
    Request getStockUpdates(String stockSymbol, AsyncCallback<Double> updateallback);
}
```

# Server Implementation of Service #
```
/**
 * The server side implementation StockService.
 */
public class StockServiceImpl extends WsRpcServlet implements StockService {
    
    /**
     * Simulates 3 stock updates 
     */
    public static class SimulatedStockUpdate implements Runnable {
        private final HandlerCallback cb;
        
        public SimulatedStockUpdate(HandlerCallback cb) {
            this.cb = cb;
        }

        public void run() {
            try{
                for(int i=0; i<3; ++i){
                    Thread.sleep(1000);
                    
                    // Send client stock price update
                    cb.call(getCurrentStockPrice());
                }
            } catch (Exception e) {}
        }
        
        public Double getCurrentStockPrice(){
            return Math.random();
        }
    }
    
    public Double getStockUpdates(String stockSymbol) {
        // Start simulated stock updates
        new Thread(new SimulatedStockUpdate(
            // Gets the server push handler callback
            getHandlerCallback()
        )).start();
        
        // Designates that stock price update will be sent 
        // asynchronously through a server push. 
        throw new ResponseSentLater();
    }    
}
```