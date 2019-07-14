package de.pateweb.officething.stock.rest;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.stock.Stock;
import de.pateweb.officething.stock.StockRepository;
import de.pateweb.officething.utils.TemporalUtils;

@RestController
public class StockController {

	private static final Logger LOG = LoggerFactory.getLogger(StockController.class);

	@Autowired
	StockRepository stockRepository;

	/**
	 * Return current Frankfurt stock value. Needs to be delted when client uses new
	 * method
	 *
	 * @return
	 */
	@Deprecated
	@GetMapping("/other/2sostock")
	public String getCurrentStock() {
		LOG.debug("getCurrentStock()");

		String stock = "-1";

		Optional<Stock> oldStockCandidate = stockRepository.findById(1L);
		if (oldStockCandidate.isPresent()) {
			Stock oldStock = oldStockCandidate.get();
			stock = Float.toString(oldStock.getStockValue());
		}

		return "<field name=\"price\">" + stock + "</field>";
	}

	@GetMapping("/stock")
	public StockDTO getStock() {

		LOG.debug("getStock()");

		Stock stockResult;

		Optional<Stock> stockCandidate = stockRepository.findById(1L);

		if (stockCandidate.isPresent()) {
			stockResult = stockCandidate.get();
		} else {
			stockResult = new Stock();
			stockResult.setStockUpdatedAt(Instant.now());
			stockResult.setStockValue(-1);
		}

		StockDTO stockDto = new StockDTO();
		stockDto.setStockValue(stockResult.getStockValue());
		stockDto.setStockUpdatedAt(TemporalUtils.instantToUsersZdt(stockResult.getStockUpdatedAt()));
		return stockDto;
	}

}
