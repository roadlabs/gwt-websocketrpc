# Under Construction (2010-01-25) #
Although this tutorial is not complete, below is the current outline:


---

## Using a Web Server supporting WebSockets (Jetty 7.0.1) ##
  1. Download Jetty 7.0.1 Distribution [jetty-distribution-7.0.1.v20091125.zip](http://dist.codehaus.org/jetty/jetty-7.0.1/jetty-distribution-7.0.1.v20091125.zip)
  1. Add Jetty Websocket jar to project's Java Build Path (`jetty-websocket-7.0.1.v20091125`)


---

## Adding gwt-websocketrpc to your Google Web Application project ##
  1. Download gwt-websocketrpc [websocketrpc-0.1.0a.zip](http://gwt-websocketrpc.googlecode.com/files/gwt-websocketrpc-0.1.0a.zip)
  1. Extracting and using gwt-websocketrpc jars
    * `gwt-websocketrpc-0.1.0a.jar`: Add to project's Java Build Path
    * `gwt-websocketrpct-servlet-0.1.0a.jar`: Add to project's `war/WEB-INF/lib`


---

## Using gwt-websocketrpc ##
### Module XML: Inherit gwt-websocketrpc module ###
```
<inherits name='org.gwt_websocketrpc.Gwt_websocketrpc' />
```

### RPC Service Interface: Extend WsRpcService ###
```
@RemoteServiceRelativePath("stock")
public interface StockService extends WsRpcService {
    Double getStockPrice(String stockSymbol);
}
```

### RPC Server Implementation: Extend WsRpcServlet ###
```
public class StockServiceImpl extends WsRpcServlet implements StockService {
    public Double getStockPrice(String stockSymbol) {
        // ... Retrieve stockPrice for stock symbol
        return stockPrice;
    }    
}
```


---

## Compile ##


---

## Running on Jetty ##