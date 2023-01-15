package practise.stock;

import practise.Practise;
import utils.logger.Logger;

import java.util.ArrayList;
import java.util.Map;

public class StockHandler {
    //TODO documentations
    private Practise practise;
    private ArrayList<StockItem> stock;
    private int criticalStockLevel;
    private int refillAmount;
    private Map<Item, Double> prices;

    public StockHandler(Practise practise, int criticalStockLevel, int refillAmount) {
        this.practise = practise;
        this.stock = new ArrayList<>();
        this.criticalStockLevel = criticalStockLevel;
        this.refillAmount = refillAmount;
    }


    public int getStock(Item item) throws Exception {
        for (StockItem i : stock) {
            if (i.getItem().equals(item)) {
                return i.getStock();
            }
        }
        throw new Exception("Item not in assortment");
    }

    public String getAllStocks() throws Exception {
        String buffer = "";
        for (StockItem item : stock) {
            buffer += String.format("%s: %s; ", item.getItem().name(), item.getStock());
        }
        if (buffer.isEmpty()) throw new Exception("No items in stock.");
        return buffer;
    }

    public void buy(Item item, int amount, double pricePerPiece) {
        if (prices.containsKey(item)) {
            prices.replace(item, pricePerPiece);
        } else prices.put(item, pricePerPiece);

        for (StockItem i : stock) {
            if (i.getItem().equals(item)) {
                i.increaseStock(amount);
                practise.decreaseBudget(amount * pricePerPiece);
                return;
            }
        }
        stock.add(new StockItem(item, amount));
        practise.decreaseBudget(amount * pricePerPiece);
    }

    public void take(Item item, int amount) throws Exception {
        for (StockItem i : stock) {
            if (i.getItem().equals(item)) {
                if (i.getStock() - amount < 0) throw new Exception(String.format("Not enough of \"%s\" in stock",
                        i.getItem().name()));
                i.decreaseStock(amount);
                lowLevelCheck(i);
                return;
            }
        }
        throw new Exception("Item not in assortment");
    }

    private void lowLevelCheck(StockItem item) {
        if (item.getStock() <= criticalStockLevel) {
            Logger logger = new Logger();
            logger.warning(String.format("Stock level for item %s critical.",
                    item.getItem().name()));
            buy(item.getItem(), refillAmount, prices.get(item.getItem()));
            if (item.getStock() > criticalStockLevel) logger.success("Item refilled.");
        }
    }
}
