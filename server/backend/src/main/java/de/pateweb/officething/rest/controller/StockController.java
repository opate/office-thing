package de.pateweb.officething.rest.controller;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.rest.dto.StockDTO;
import de.pateweb.officething.stock.Stock;
import de.pateweb.officething.stock.StockRepository;
import de.pateweb.officething.utils.TemporalUtils;

@RestController
public class StockController{

	private static final Logger LOG = LoggerFactory.getLogger(StockController.class);
	
	@Autowired
	StockRepository stockRepository;
	
    /**
     * Return current Frankfurt stock value. Needs to be delted when
     * client uses new method
     *
     * @return
     */
    @Deprecated
    @GetMapping("/other/2sostock")
    public String getCurrentStock() {
        LOG.debug("getCurrentStock()");

        String stock = "-1";

        try {
            Stock oldStock = stockRepository.findById(1L).get();
            stock = Float.toString(oldStock.getStockValue());
            
            //TODO fix process if no value found, Optional#Empty
        } catch (NoSuchElementException e) {
            LOG.debug("No value in database.");
        }        

        return "<field name=\"price\">" + stock + "</field>";
    }

    /**
     * Returns JSON
     *
     * @return
     */
    @GetMapping("/stock")
    public StockDTO getStock() {
    	
        LOG.debug("getStock()");

        Stock stockResult;

        try {
            stockResult = stockRepository.findById(1L).get();  
            
            //TODO change to Optional#Empty
        } catch (NoSuchElementException e) {
            return new StockDTO();
        }

        return toStockDTO(stockResult);
    }	
    
    private StockDTO toStockDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setStockValue(stock.getStockValue());
        dto.setStockUpdatedAt(TemporalUtils.instantToUsersZdt(stock.getStockUpdatedAt()));
        return dto;
    }    
     
}
