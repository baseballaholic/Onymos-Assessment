
public class Order {
	
	private static final int MAX_TICKERS = 1024;
	private static final int MAX_ORDERS = 1000;
	
    private static Order[][] buyOrders = new Order[MAX_TICKERS][MAX_ORDERS];
    private static Order[][] sellOrders = new Order[MAX_TICKERS][MAX_ORDERS];
    private static int[] buyOrderCount = new int[MAX_TICKERS];
    private static int[] sellOrderCount = new int[MAX_TICKERS];
    private static String[] tickerSymbols = new String[MAX_TICKERS];
	private static int tickerCount = 0;
	private String orderType;
	private String tickerSymbol;
	private int quantity;
	private double price;
	
	
	public Order(String orderType, String tickerSymbol, int quantity, double price) {
		this.orderType = orderType;
		this.tickerSymbol = tickerSymbol;
		this.quantity = quantity;
		this.price = price;
	}
	
	public Order() {
		
	}
	
	public static synchronized void addOrder(String orderType, String tickerSymbol, int quantity, double price) {
		Order o = new Order(orderType, tickerSymbol, quantity, price);
		int index = findIndex(tickerSymbol);
		if(orderType.toLowerCase().equals("buy")) {
			buyOrders[index][buyOrderCount[index]++] = o;
			// We sort the buy orders from highest to lowest
	        for (int i = buyOrderCount[index] - 1; i > 0; i--) {
	            if (buyOrders[index][i].price > buyOrders[index][i - 1].price) {
	                Order temp = buyOrders[index][i];
	                buyOrders[index][i] = buyOrders[index][i - 1];
	                buyOrders[index][i - 1] = temp;
	            }
	        }
		} else if(orderType.toLowerCase().equals("sell")) {
			sellOrders[index][sellOrderCount[index]++] = o;
			// We sort the sell orders from lowest to highest
	        for (int i = sellOrderCount[index] - 1; i > 0; i--) {
	            if (sellOrders[index][i].price < sellOrders[index][i - 1].price) {
	                Order temp = sellOrders[index][i];
	                sellOrders[index][i] = sellOrders[index][i - 1];
	                sellOrders[index][i - 1] = temp;
	            }
	        }
		}
		matchOrder(tickerSymbol);
	}
    
	public static synchronized void matchOrder(String tickerSymbol) {
		int index = findIndex(tickerSymbol);
		int i = 0;
		int j = 0;
		while(i < buyOrderCount[index] && j < sellOrderCount[index]) {
			
			Order buyOrder = buyOrders[index][i]; // Grab the buy order
            Order sellOrder = sellOrders[index][j]; // Grab the sell order
            
			if(buyOrder.price >= sellOrder.price) {
				int tradeAmount = Math.min(buyOrder.quantity, sellOrder.quantity); // We need the amount of stocks we are trading for
                System.out.printf("\nTrade Executed: %d shares of %s at $%.2f\n", tradeAmount, tickerSymbol, sellOrder.price);
                
                buyOrder.quantity -= tradeAmount; // We need to subtract the amount of stocks from the buy order and sell order as we just traded them
    			sellOrder.quantity -= tradeAmount;
    			
    			// If this buy or sell order is finished, update the counter so we can move on to the next order
    			if (buyOrder.quantity == 0) {
                    i++;
                }
                if (sellOrder.quantity == 0) {
                    j++;
                }
			} else {
				break;  // We have no more orders to match so we exit the while loop
			}
		}
	}
	
	public static synchronized int findIndex(String tickerSymbol) {
		for(int i = 0;i<tickerSymbols.length;i++) {
			if(tickerSymbols[i] == tickerSymbol) {
				return i;
			}
		}
		if (tickerCount < MAX_TICKERS) {
            tickerSymbols[tickerCount] = tickerSymbol;
            return tickerCount++;
        } else {
            throw new IllegalStateException("Maximum number of tickers reached");
        }
	}
    
	public static void simulateTrades() {
		String[] randomTickers = { "AAPL", "BUD", "GOOG", "AMZN", "TSLA", "MSFT", "META", "NFLX", "NVDA", "DIS" };
        int transactionCount = 50; // Simulate 100 random transactions
        for (int i = 0; i < transactionCount; i++) {
            String ticker = randomTickers[(int) (Math.random() * randomTickers.length)];
            String orderType = (Math.random() > 0.5) ? "Buy" : "Sell";
            int quantity = (int) (Math.random() * 100) + 1; // Random quantity between 1 and 100
            double price = Math.random() * 500; // Random price between 0 and 500
            price = Math.round(price * 100.0) / 100.0; // Round the price to two decimal places

            System.out.printf("\nAdding %s Order: %d shares of %s at $%.2f\n", orderType, quantity, ticker, price);
            addOrder(orderType, ticker, quantity, price);

            // Simulate time delay between transactions to mimic real-time trading
            try {
                Thread.sleep(100); // Sleep for 100 ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
	}
	
	public static void main(String[] args) {
        simulateTrades();
    }
	
}
