package de.pateweb.officething.stock;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Octavian Pate
 */
@Component
@EnableScheduling
public class StockUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(StockUpdater.class);

	// properties set in Other Sources/src/main/resources/application.properties
	@Value("${stock.websiteurl}")
	String stockWebsiteUrl;

	@Autowired
	StockRepository stockRepository;

	@PostConstruct
	public void doOnceAtStartup() {

		LOG.debug("stock, doOnceAtStartup()");

		checkStock();
	}
//      second, minute, hour, day, month, weekday
//      "0 0 * * * *" = the top of every hour of every day.
//      "*/10 * * * * *" = every ten seconds.
//      "0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
//      "0 0 6,19 * * *" = 6:00 AM and 7:00 PM every day.
//      "0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.
//      "0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
//      "0 0 0 25 12 ?" = every Christmas Day at midnight
	// every minute to test
	// @Scheduled(cron = "* */1 * * *")

	// Fire at every hour between 7-17h on Monday till Fryday
	@Scheduled(cron = "0 0 6-17 * * MON-FRI")
	public void checkStock() {

		LOG.debug("checkStock()");

		String result = requestStockValue();

		if (!result.equals("-1")) {

			NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
			Number number = -1;
			
			try {
				number = format.parse(result);
			} catch (ParseException ex) {
				LOG.error("Cannot parse stock value. {}", ex.getMessage());
				return;
			}

			Float stockValue = number.floatValue();
			LOG.info("parsed new stock value: {}", stockValue);

			Optional<Stock> oldStockEntity = stockRepository.findById(1L);

			Instant now = Instant.now();

			if (oldStockEntity.isPresent()) {

				Stock oldStock = oldStockEntity.get();
				oldStock.setStockValue(stockValue);
				oldStock.setStockUpdatedAt(now);

				stockRepository.save(oldStock);

			} else {

				Stock stockEntity = new Stock();
				stockEntity.setId(1L);
				stockEntity.setStockValue(stockValue);
				stockEntity.setStockUpdatedAt(now);

				stockRepository.save(stockEntity);
			}

		}
		else
		{
			LOG.warn("Cannot get stock value. Next try in 1 hour.");
		}
	}

	private String requestStockValue() {

		try {

			Document doc = Jsoup.connect(stockWebsiteUrl)
					.userAgent(
							"Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.get();

			return doc.select("span[data-role=currentvalue]").text();

		} catch (IOException e) {
			LOG.error("Connection error to stock website. {}", e.getMessage());
			return "-1";
		}

	}

}
