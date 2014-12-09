/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.business.StockBook;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws Exception {
		//if non local test you have to run this, but NOTE every time restart the server
		// also you have to manually increase the parameter and switch the boolean to false
		// in the configuration adjust warmup to 100, actual runs to 200
		//execution(10);
		
		//use this if local test
		for(int i = 10; i<=200; i = i +10){
			execution(i);
		}
	}
	

	public static void execution(int clientNumber) throws Exception {
		int numConcurrentWorkloadThreads = clientNumber;
		String serverAddress = "http://localhost:8081";
		boolean localTest = true;
		List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();

		// Initialize the RPC interfaces if its not a localTest, the variable is
		// overriden if the property is set
		String localTestProperty = System
				.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
		localTest = (localTestProperty != null) ? Boolean
				.parseBoolean(localTestProperty) : localTest;

		BookStore bookStore = null;
		StockManager stockManager = null;
		if (localTest) {
			CertainBookStore store = new CertainBookStore();
			bookStore = store;
			stockManager = store;
		} else {
			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);
		}

		// Generate data in the bookstore before running the workload
		initializeBookStoreData(bookStore, stockManager);

		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);

		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			WorkloadConfiguration config = new WorkloadConfiguration(bookStore,
					stockManager);
			Worker workerTask = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults.add(exec.submit(workerTask));
		}

		// Get the results from the threads using the futures returned
		for (Future<WorkerRunResult> futureRunResult : runResults) {
			WorkerRunResult runResult = futureRunResult.get(); // blocking call
			workerRunResults.add(runResult);
		}

		exec.shutdownNow(); // shutdown the executor

		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore).stop();
			((StockManagerHTTPProxy) stockManager).stop();
		}

		reportMetric(workerRunResults);
	}
	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults) {
		// TODO: You should aggregate metrics and output them for plotting here
		
		double totalTime = 0;
		double totalSuccessfulInteractions = 0;
		
		for (WorkerRunResult workerRunResult: workerRunResults){
			totalTime += workerRunResult.getElapsedTimeInNanoSecs();
			totalSuccessfulInteractions += workerRunResult.getSuccessfulInteractions();
		}
		
		double latency = totalTime/workerRunResults.size(); // latency = average time to generate a response
		double throughput = totalSuccessfulInteractions/totalTime; // throughput = average successful interactions per time period
		//System.out.println("Please notice, the time is in ns! Factor 1ns = 1,0*10^-9 s");
		//System.out.println("total time "+totalTime);
		//System.out.println("totalSuccessfulInteractions" + totalSuccessfulInteractions);
		System.out.println("workerRundResults: "+workerRunResults.size());
		System.out.println("Latency: "+latency);
		System.out.println("Throughput: "+throughput);
	}
	

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 * stockManager adds 100 copies of 4 books and 300 copies of a book that has a longer title. 
	 * 
	 */
	public static void initializeBookStoreData(BookStore bookStore,
			StockManager stockManager) throws BookStoreException {
		
		Set<StockBook> booksInStock = new HashSet<StockBook>();
		
		StockBook testOfThrones = new ImmutableStockBook(3044560, "Test of Thrones",
				"George RR Testin'", (float) 10, 100, 0, 0, 0, false);
		booksInStock.add(testOfThrones);
		
		StockBook jkUnit =  new ImmutableStockBook(3044561, "Harry Potter and JUnit",
				"JK Unit", (float) 10, 100, 0, 0, 0, false);
		booksInStock.add(jkUnit);
		
		StockBook interstellar =  new ImmutableStockBook(3044566, "Intetstellar",
				"So great", (float) 10, 100, 0, 0, 0, false);
		booksInStock.add(interstellar);
		
		StockBook blahBook =  new ImmutableStockBook(3044567, "Blah",
				"Blah Author", (float) 10, 100, 0, 0, 0, false);
		booksInStock.add(blahBook);
		
		StockBook longTitleBook =  new ImmutableStockBook(3044599, "This Book Has a Really Really Long Title For Testing Purposes and Such",
				"Blah Author", (float) 10, 300, 0, 0, 0, false);
		booksInStock.add(longTitleBook);

		stockManager.addBooks(booksInStock);
	}
}
