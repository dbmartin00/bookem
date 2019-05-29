package snap.io.demo.bookem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactoryBuilder;

public class BookEm {

	class BookEmHost {
		String guid;
		float satisfaction;
		int listing_views;
		float percentage_days_vacant;

		public BookEmHost(String guid, float satisfaction, int listing_views, float percentage_days_vacant) {
			super();
			this.guid = guid;
			this.satisfaction = satisfaction;
			this.listing_views = listing_views;
			this.percentage_days_vacant = percentage_days_vacant;
		}

		public String getGuid() {
			return guid;
		}

		public void setGuid(String guid) {
			this.guid = guid;
		}

		public float getSatisfaction() {
			return satisfaction;
		}

		public void setSatisfaction(float satisfaction) {
			this.satisfaction = satisfaction;
		}

		public int getListing_views() {
			return listing_views;
		}

		public void setListing_views(int listing_views) {
			this.listing_views = listing_views;
		}

		public float getPercentage_days_vacant() {
			return percentage_days_vacant;
		}

		public void setPercentage_days_vacant(float percentage_days_vacant) {
			this.percentage_days_vacant = percentage_days_vacant;
		}

	}

	public static final int TOTAL_HOST_COUNT = 10000;

	Collection<BookEmHost> allHosts = new LinkedList<BookEmHost>();

	Random random = new Random(System.currentTimeMillis());
	private void runSeason() throws Exception {
		long total_start = System.currentTimeMillis();
		
		// Construct one season of BookEm hosts
		System.out.println("Start construction of " + TOTAL_HOST_COUNT + " unique hosts");
		for(int i = 0; i < TOTAL_HOST_COUNT; i++) {

			float satisfaction = 40 + random.nextInt(30);
			int listing_views = random.nextInt(25);
			float percentage_days_vacant = (listing_views > 0 ? (random.nextInt(20) + 40) : 100);

			BookEmHost host = new BookEmHost("" + UUID.randomUUID(), satisfaction, listing_views, percentage_days_vacant);
			allHosts.add(host);
		}

		// Connect Split Client
		SplitClientConfig config = SplitClientConfig.builder()
				.setBlockUntilReadyTimeout(60000)
				.build();

		SplitClient client = SplitFactoryBuilder.build("*** FIXME PROVIDE SDK KEY ***", config).client();
		try {
			client.blockUntilReady();
		} catch (TimeoutException | InterruptedException e) {
			throw e;
		}

		try {
			Map<String, Object> properties_us = new TreeMap<String, Object>();
			properties_us.put("region", "us");
			Map<String, Object> properties_eu = new TreeMap<String, Object>();
			properties_eu.put("region", "eu");
			Map<String, Object> properties_jp = new TreeMap<String, Object>();
			properties_jp.put("region", "jp");
			
			int count = 1;
			// Check if each host gets "instant booking" feature
			for(BookEmHost host : allHosts) {

				// generates an Impression with no further action
				String treatment = client.getTreatment("" + host.getGuid(), "instant_booking");
				if(treatment.equalsIgnoreCase("on")) {
					host.setListing_views(host.getListing_views() + random.nextInt(25));
					host.setPercentage_days_vacant(host.getPercentage_days_vacant() - random.nextInt(10));
					host.setSatisfaction(host.getSatisfaction() - random.nextInt(30));
					System.out.print("a");
				} else if (treatment.equalsIgnoreCase("off")){
					System.out.print("b");
				} else {
					System.out.println("x");
				}
				
				// CRITICAL send the event data to Split with an SDK track call

				
				Map<String, Object> properties = new TreeMap<String, Object>();
				int region = random.nextInt(3);
				if(region == 0) {
					properties = properties_us;
				} else if (region == 1) {
					properties = properties_eu;
				} else if (region == 2) {
					properties = properties_jp;
				}
				client.track(host.getGuid(), "user", "host_satisfaction", host.getSatisfaction(), properties);
				client.track(host.getGuid(), "user", "listing_views", host.getListing_views(), properties);
				client.track(host.getGuid(), "user", "percentage_days_vacant_per_month", host.getPercentage_days_vacant(), properties);

//				client.track(host.getGuid(), "user", "host_satisfaction", host.getSatisfaction());
//				client.track(host.getGuid(), "user", "listing_views", host.getListing_views());
//				client.track(host.getGuid(), "user", "percentage_days_vacant_per_month", host.getPercentage_days_vacant());
		
				
				
				if(count++ % 100 == 0) {
					System.out.println();
					Thread.sleep(100);
				}

			}

		} finally {
			client.destroy();
		}

		System.out.println("finished a season in " + (System.currentTimeMillis() - total_start) + "ms");
	}

	public void execute() throws Exception {
		while(true) {
			runSeason();
		}
	}


	// Add the critics
//	allHosts.add(new BookEmHost("aaa-guide", 4 + random.nextInt(3), 0, 100));
//	allHosts.add(new BookEmHost("fodors-guide", 4 + random.nextInt(3), 0, 100));
//	allHosts.add(new BookEmHost("michelin-guide", 4 + random.nextInt(3), 0, 100));
//	allHosts.add(new BookEmHost("lonelyplanet-guide", 4 + random.nextInt(3), 0, 100));
//	allHosts.add(new BookEmHost("bourdain-guide", 4 + random.nextInt(3), 0, 100));

}
