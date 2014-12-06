/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;


/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
	private WorkloadConfiguration configuration = null;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;

	public Worker(WorkloadConfiguration config) {
		configuration = config;
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 * 
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 * 
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			if (chooseInteraction < configuration
					.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration
					.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction(configuration.getNumBooksWithLeastCopies(), configuration.getStockManager());
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction(configuration.getBookStore());
				numSuccessfulFrequentBookStoreInteraction++;
			}
		} catch (BookStoreException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
		//The list of books already in the store.
		List<StockBook> inTheStore = configuration.getStockManager().getBooks();
		//The list of randomly created set of books.
		Set<ImmutableStockBook> nextSetOfStockBooks = configuration.getBookSetGenerator().nextSetOfStockBooks(configuration.getStockManager(), configuration.getNumBooksToAdd());
		
		// The boocks which must be added. 
		Set<StockBook> toBeAdded = new HashSet<StockBook>(); 
		
		/* This for loop is implementing It then checks if the set of ISBNs is in the
l		list of books fetched. Finally, it invokes addBooks with the set of books
n		of found in the list returned by getBooks. */
		
		for (StockBook stockBook: nextSetOfStockBooks){
			
			if (!inTheStore.contains(stockBook.getISBN())){
				
				toBeAdded.add(stockBook);
			}
		}
		
		configuration.getStockManager().addBooks(toBeAdded);
	}

	/**
	 * Runs the stock replenishment interaction
	 * 
	 * @throws BookStoreException
	 * 
	 * 
	 */
	
	
	private void runFrequentStockManagerInteraction(int k, StockManager stockManager) throws BookStoreException {
		
		List<StockBook> allStockBooks = stockManager.getBooks();
		Collections.sort(allStockBooks, new Comparator<StockBook>() {
			
			@Override
			public int compare(StockBook o1, StockBook o2) {
				// with this compare we get a list with the book with the lowest number of
				// copies as first book in the list
				return o1.getNumCopies()-o2.getNumCopies();
			}
		});
		Set<StockBook> toHaveCopiesAdded = new HashSet<StockBook>(); // holds books with k lowerst copies
		
		// select the k books with smallest quantities in stock, add it to the toHaveCopiesAdded list
		
		for (int i = 0; i < k-1; i++){
			toHaveCopiesAdded.add(allStockBooks.get(i));
		}
		
		Set<BookCopy> copies = new HashSet<BookCopy>(); // This is what the StockManager will copy. 
		
		for (StockBook stockBook: toHaveCopiesAdded){
			
			BookCopy newCopy = new BookCopy(stockBook.getISBN(), configuration.getNumAddCopies());
			copies.add(newCopy);
		}
		
		stockManager.addCopies(copies);
	}
	
	/**
	 * Runs the customer interaction
	 * 
	 * @throws BookStoreException
	 */
	
	/* I'm a little confused about this method because getEdirotPicks requires a number
	 * paramter which I assume is configuration.getNumEditorPicksToGet(), but then getting 
	 * a subset using sampleFromSetOfISBNS request another number parameter. I don't know 
	 * what that parameter is.
	 */
	
	private void runFrequentBookStoreInteraction(BookStore bookStore) throws BookStoreException {
		
		Set<Integer> isbns = new HashSet<Integer>();
		
		for(Book b: bookStore.getEditorPicks(configuration.getNumEditorPicksToGet())){
			
			isbns.add(b.getISBN());
		}
	
		Set<Integer> toBuyIsbn = configuration.getBookSetGenerator().sampleFromSetOfISBNs(isbns, configuration.getNumEditorPicksToGet()); // I'm not sure if this parameter is correct. 
		Set<BookCopy> bookCopyToBuy = new HashSet<BookCopy>();
		
		for (Integer isbn: toBuyIsbn){
			
			bookCopyToBuy.add(new BookCopy(isbn, configuration.getNumBooksToBuy()));
		}
		
		bookStore.buyBooks(bookCopyToBuy);
	}
}
