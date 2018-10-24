package snap.io.demo.bookem;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactory;
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
		System.out.println("Start construction of " + TOTAL_HOST_COUNT + " unique hosts");
		for(int i = 0; i < TOTAL_HOST_COUNT; i++) {

			float satisfaction = 40 + random.nextInt(30);
			int listing_views = random.nextInt(25);
			float percentage_days_vacant = (listing_views > 0 ? (random.nextInt(20) + 40) : 100);
			
			BookEmHost host = new BookEmHost("" + UUID.randomUUID(), satisfaction, listing_views, percentage_days_vacant);
			allHosts.add(host);
		}
		
		// Add the critics
		allHosts.add(new BookEmHost("aaa-guide", 4 + random.nextInt(3), 0, 100));
		allHosts.add(new BookEmHost("fodors-guide", 4 + random.nextInt(3), 0, 100));
		allHosts.add(new BookEmHost("michelin-guide", 4 + random.nextInt(3), 0, 100));
		allHosts.add(new BookEmHost("lonelyplanet-guide", 4 + random.nextInt(3), 0, 100));
		allHosts.add(new BookEmHost("bourdain-guide", 4 + random.nextInt(3), 0, 100));
		
		SplitClientConfig config = SplitClientConfig.builder().ready(60000).eventFlushIntervalInMillis(1000).build();
		SplitFactory splitFactory = SplitFactoryBuilder.build("67ustpreuviaiv22pfc6ke8tleu42f8bem77", config);
		SplitClient client = splitFactory.client();
		
		try {
			int count = 1;
			for(BookEmHost host : allHosts) {
				boolean hasGottenTreatment = false;
				
				if(client.getTreatment("" + host.getGuid(), "instant_booking").equalsIgnoreCase("on")) {
					host.setListing_views(host.getListing_views() + random.nextInt(25));
					host.setPercentage_days_vacant(host.getPercentage_days_vacant() - random.nextInt(10));
					host.setSatisfaction(host.getSatisfaction() - random.nextInt(30));
					System.out.print("a");
					hasGottenTreatment = true;
				}
				
				Map<String, Object> attributes = new HashMap<String, Object>();
				String region = random.nextInt(100) < 80 ? "EU" : "US";
				attributes.put("region", region);
				if(client.getTreatment(host.getGuid(), "search_engine_optimization", attributes).equalsIgnoreCase("on")) {
					host.setListing_views(host.getListing_views() + 40 + random.nextInt(20));
					host.setPercentage_days_vacant(host.getPercentage_days_vacant() - random.nextInt(3));
					System.out.print("b");
					hasGottenTreatment = true;
				}
				
				if(client.getTreatment("" + host.getGuid(), "background_check").equalsIgnoreCase("on")) {
					host.setSatisfaction(host.getSatisfaction() + random.nextInt(15));
					System.out.print("c");
					hasGottenTreatment = true;
				}
				
				if(!hasGottenTreatment) {
					System.out.print("+");
				}
				
				client.track(host.getGuid(), "host", "host_satisfaction", host.getSatisfaction());
				client.track(host.getGuid(), "host", "listing_views", host.getListing_views());
				client.track(host.getGuid(), "host", "percentage_days_vacant_per_month", host.getPercentage_days_vacant());
				
				String treatment = client.getTreatment(host.getGuid(), "new_service");
				System.out.print(treatment);
				if(treatment.equalsIgnoreCase("housekeeping")) {
					client.track(host.getGuid(), "host", "bem_housekeeping", random.nextInt(10));					
				} else if (treatment.equalsIgnoreCase("accounting")) {
					client.track(host.getGuid(), "host", "bem_accounting", random.nextInt(2));					
					
				} else if (treatment.equalsIgnoreCase("gardening")) {
					client.track(host.getGuid(), "host", "bem_gardening", random.nextInt(5));					
					
				} else if (treatment.equalsIgnoreCase("laundry")) {
					client.track(host.getGuid(), "host", "bem_laundry", random.nextInt(20));					
					
				} else if (treatment.equalsIgnoreCase("food_delivery")) {
					client.track(host.getGuid(), "host", "bem_food_delivery", random.nextInt(5));					
				}
				
				if(count++ % 100 == 0) {
					System.out.println();
					Thread.sleep(100);
				}

			}

		} finally {
			splitFactory.destroy();
		}

		System.out.println("finished a season in " + (System.currentTimeMillis() - total_start) + "ms");
	}
	
	public void execute() throws Exception {
		while(true) {
			runSeason();
		}
	}



	
}
